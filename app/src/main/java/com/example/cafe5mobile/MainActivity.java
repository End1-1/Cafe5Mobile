package com.example.cafe5mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends ActivityRoot implements View.OnClickListener {

    private static final int RecordAudioRequestCode = 1;

    private ActivityMainBinding _b;
    private SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    private ArrayList<String> mSpeechResult = new ArrayList();
    private static String mUuid = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.btnConnect.setOnClickListener(this);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver, new IntentFilter(Server.LOCAL_MESSAGE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
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
        Preference.setString("uuid", mUuid);
        String message = String.format("{\"what\":%d, \"uuid\":\"%s\"}", Server.WHAT_GETSERVER, mUuid);
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            DatagramSocket ds = new DatagramSocket();
            ds.setSoTimeout(2000);
            ds.setBroadcast(true);
            DatagramPacket dp = new DatagramPacket(data, data.length, getBroadcastAddress(), Server.PORT);
            ds.send(dp);

            byte[] recvBuf = new byte[15000];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            ds.receive(packet);
            Preference.setString("server_ip", packet.getAddress().getHostAddress());
            Log.i("TAG TAG TAG TAG", "Packet received from: " + packet.getAddress().getHostAddress());
            String datastr = new String(packet.getData()).trim();
            Log.i("TAG TAG TAG TAG", "Packet received; data: " + datastr);
            Intent localIntent = new Intent(Server.LOCAL_MESSAGE_ACTION)
                    .putExtra(Server.DATA_TYPE, Server.BROADCAST_SOCKET_DATA)
                    .putExtra(Server.SOCKET_REPLY, datastr);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(localIntent);
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Runnable mBroadcastReceiver = new Runnable() {
        @Override
        public void run() {
            //receiveBroadcastMessage();
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
                            .putExtra(Server.DATA_TYPE, Server.BROADCAST_SOCKET_DATA)
                            .putExtra(Server.SOCKET_REPLY, data);
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(localIntent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(Server.DATA_TYPE, 0)) {
                case Server.BROADCAST_SOCKET_DATA:
                    JsonObject jo = JsonParser.parseString(intent.getStringExtra(Server.SOCKET_REPLY)).getAsJsonObject();
                    if (jo.get("reply") != null) {
                        switch (jo.get("what").getAsInt()) {
                            case Server.WHAT_GETSERVER:
                                if (jo.get("accept").getAsInt() == 1) {
                                    Preference.setString("server_uuid", jo.get("server_uuid").getAsString());
                                    _b.btnConnect.setImageDrawable(getDrawable(R.drawable.wifi));
                                }
                                break;
                            case Server.WHAT_PARSE_STORE_STRING:
                                if (jo.get("error") == null) {
                                    mSpeechResult.clear();
                                    _b.rvResult.getAdapter().notifyDataSetChanged();
                                    Intent suggestIntent = new Intent(MainActivity.this, SuggestGoodsActivity.class);
                                    suggestIntent.putExtra("data", jo.toString());
                                    startActivity(suggestIntent);
                                }
                                break;
                        }
                    }
                    break;
                default:
                    break;
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                sendServerRequest();
                break;
        }
    }

    class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private RvSpeechResultBinding _vh;

            public ResultViewHolder(@NonNull RvSpeechResultBinding vh) {
                super(vh.getRoot());
                vh.getRoot().setOnClickListener(this);
                _vh = vh;
            }

            public void onBind(int position) {
                _vh.txtResult.setText(mSpeechResult.get(position));
            }

            @Override
            public void onClick(View view) {
                int i = getAdapterPosition();
                String str = mSpeechResult.get(i);
                JsonObject jo = new JsonObject();
                jo.addProperty("data", str);
                sendRequest(Server.WHAT_PARSE_STORE_STRING, jo.toString().replace("\"", "\\\""));
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