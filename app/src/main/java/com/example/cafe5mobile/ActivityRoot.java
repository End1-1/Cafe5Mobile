package com.example.cafe5mobile;

import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ActivityRoot extends AppCompatActivity  {

    protected void sendRequest(int what, String datastr) {
        String message = String.format("{\"what\":%d, \"uuid\":\"%s\", \"windowuuid\":\"%s\", \"data\":\"%s\"}", what, Preference.getString("uuid"), Preference.getString("windowuuid"), datastr);
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            DatagramSocket ds = new DatagramSocket();
            ds.setSoTimeout(2000);
            //ds.setBroadcast(true);
            DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(Preference.getString("server_ip")), Server.PORT);
            ds.send(dp);

            byte[] recvBuf = new byte[15000];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            ds.receive(packet);
            Preference.setString("server_ip", packet.getAddress().getHostAddress());
            Log.i("TAG TAG TAG TAG", "Packet received from: " + packet.getAddress().getHostAddress());
            datastr = new String(packet.getData()).trim();
            Log.i("TAG TAG TAG TAG", "Packet received; data: " + datastr);
            Intent localIntent = new Intent(Server.LOCAL_MESSAGE_ACTION)
                    .putExtra(Server.DATA_TYPE, Server.BROADCAST_SOCKET_DATA)
                    .putExtra(Server.SOCKET_REPLY, datastr);
            LocalBroadcastManager.getInstance(ActivityRoot.this).sendBroadcast(localIntent);
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
