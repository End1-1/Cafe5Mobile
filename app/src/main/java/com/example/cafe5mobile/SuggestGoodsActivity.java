package com.example.cafe5mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cafe5mobile.databinding.ActivitySuggestGoodsBinding;
import com.example.cafe5mobile.databinding.RvSuggestGoodsBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SuggestGoodsActivity extends ActivityRoot {

    private ActivitySuggestGoodsBinding _b;
    GoodsArrayReply mGoodsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivitySuggestGoodsBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        JsonObject jo = JsonParser.parseString(getIntent().getStringExtra("data")).getAsJsonObject();
        Gson g = new GsonBuilder().create();
        mGoodsArray = g.fromJson(jo.getAsJsonObject("data"), GoodsArrayReply.class);
        _b.rvSuggestGoods.setLayoutManager(new GridLayoutManager(this, 2));
        _b.rvSuggestGoods.setAdapter(new GoodsAdapter());
        _b.edtQty.setText(String.valueOf(mGoodsArray.qty));
        _b.txtUnit.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Server.LOCAL_MESSAGE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private class GoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class SuggestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private RvSuggestGoodsBinding _vh;

            public SuggestViewHolder(@NonNull RvSuggestGoodsBinding vh) {
                super(vh.getRoot());
                _vh = vh;
                _vh.getRoot().setOnClickListener(this);
            }

            public void onBind(int position) {
                _vh.txtName.setText(mGoodsArray.goods.get(position).f_name);
            }

            @Override
            public void onClick(View view) {
                if (_b.edtPrice.getText().toString().isEmpty()) {
                    _b.edtPrice.setText("0");
                }
                if (_b.edtQty.getText().toString().isEmpty()) {
                    _b.edtQty.setText("0");
                }
                if (_b.edtAmount.getText().toString().isEmpty()) {
                    _b.edtAmount.setText("0");
                }
                int i = getAdapterPosition();
                Goods g = mGoodsArray.goods.get(i);
                JsonObject jo = new JsonObject();
                jo.addProperty("unit", g.f_unitname);
                jo.addProperty("id", g.f_id);
                jo.addProperty("name", g.f_name);
                jo.addProperty("scancode", g.f_scancode);
                jo.addProperty("qty", Double.valueOf(_b.edtQty.getText().toString()));
                jo.addProperty("price", Double.valueOf(_b.edtPrice.getText().toString()));
                jo.addProperty("amount", Double.valueOf(_b.edtAmount.getText().toString()));
                SuggestGoodsActivity.this.sendRequest(Server.WHAT_STORE_APPEND_ITEM, jo.toString().replace("\"", "\\\""));
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RvSuggestGoodsBinding vh = RvSuggestGoodsBinding.inflate(getLayoutInflater(), parent, false);
            return new SuggestViewHolder(vh);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((SuggestViewHolder) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return mGoodsArray.goods.size();
        }
    };

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(Server.DATA_TYPE, 0)) {
                case Server.BROADCAST_SOCKET_DATA:
                    JsonObject jo = JsonParser.parseString(intent.getStringExtra(Server.SOCKET_REPLY)).getAsJsonObject();
                    if (jo.get("reply") != null) {
                        switch (jo.get("what").getAsInt()) {
                            case Server.WHAT_STORE_APPEND_ITEM:
                                if (jo.get("error") == null) {
                                    finish();
                                } else {
                                    Dlg.createDialog(SuggestGoodsActivity.this, jo.get("message").getAsString()).setOk(null);
                                }
                                break;
                        }
                    }
                    break;
                case Server.BROADCAST_SOCKET_ERROR:
                    Dlg.createDialog(SuggestGoodsActivity.this, intent.getStringExtra(Server.SOCKET_REPLY)).setOk(null);
                    break;
            }
        }
    };
}