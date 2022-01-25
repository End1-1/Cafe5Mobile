package com.example.cafe5mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cafe5mobile.databinding.ActivityMainBinding;
import com.example.cafe5mobile.databinding.RvSpeechResultBinding;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int RecordAudioRequestCode = 1;

    private ActivityMainBinding _b;
    private SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    private ArrayList<String> mSpeechResult = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.rvResult.setLayoutManager(new LinearLayoutManager(this));
        _b.rvResult.setAdapter(new ResultAdapter());

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hy-AM");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UNICODE_LOCALE_EXTENSION);

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d("SPEEEEECH", "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("SPEEEEECH", "Begining. Listening. speech");
//                editText.setText("");
//                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                _b.btnMic.setImageResource(R.drawable.micb);
                mSpeechResult = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                _b.rvResult.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        _b.btnMic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mSpeechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    _b.btnMic.setImageResource(R.drawable.micw);
                    mSpeechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });

        new Thread(mBroadcastReceiver).start();
        sendServerRequest();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    private void sendServerRequest() {
        String message = String.format("{\"what\":%d}", Server.WHAT_GETSERVER);
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            DatagramSocket ds = new DatagramSocket();
            ds.setBroadcast(true);
            DatagramPacket dp = new DatagramPacket(data, data.length, getBroadcastAddress(), Server.PORT);
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Runnable mBroadcastReceiver = new Runnable() {
        @Override
        public void run() {
            receiveBroadcastMessage();
        }

        private void receiveBroadcastMessage() {
            StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                DatagramSocket ds = new DatagramSocket(Server.PORT, InetAddress.getByName("0.0.0.0"));
                while (true) {
                    Log.i("TAG TAG TAG TAG", "Ready to receive broadcast packets!");

                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    ds.receive(packet);

                    Log.i("TAG TAG TAG TAG", "Packet received from: " + packet.getAddress().getHostAddress());
                    String data = new String(packet.getData()).trim();
                    Log.i("TAG TAG TAG TAG", "Packet received; data: " + data);

                    Intent localIntent = new Intent(Server.LOCAL_MESSAGE_ACTION)
                            .putExtra(Server.DATA, data);
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(localIntent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private InetAddress getBroadcastAddress() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class ResultViewHolder extends RecyclerView.ViewHolder {

            private RvSpeechResultBinding _vh;

            public ResultViewHolder(@NonNull RvSpeechResultBinding vh) {
                super(vh.getRoot());
                _vh = vh;
            }

            public void onBind(int position) {
                _vh.txtResult.setText(mSpeechResult.get(position));
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RvSpeechResultBinding vh = RvSpeechResultBinding.inflate(getLayoutInflater(), parent, false);
            return new ResultViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ResultViewHolder) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return mSpeechResult.size();
        }
    }
}