package com.example.cafe5mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.cafe5mobile.databinding.ActivityRepeatLastBinding;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RepeatLastActivity extends ActivityRoot implements View.OnClickListener {

    private ActivityRepeatLastBinding _b;
    private boolean mStopWatch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityRepeatLastBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.txtRepeatGoods.setOnClickListener(this);
        try {
            JsonObject jo = JsonParser.parseString(Preference.getString("repeat_last")).getAsJsonObject();
            _b.txtRepeatGoods.setText(jo.get("name").getAsString());
            _b.edtQty.setText(jo.get("qty").getAsString());
            _b.edtPrice.setText(jo.get("price").getAsString());
            _b.edtAmount.setText(jo.get("amount").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        _b.edtQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mStopWatch) {
                    return;
                }
                mStopWatch = true;
                try {
                    _b.edtAmount.setText(String.valueOf(Double.valueOf(editable.toString()) * Double.valueOf(_b.edtPrice.getText().toString())));
                } catch (Exception e) {
                    e.printStackTrace();
                    _b.edtAmount.setText("0");
                }
                mStopWatch = false;
            }
        });
        _b.edtQty.selectAll();
        _b.edtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mStopWatch) {
                    return;
                }
                mStopWatch = true;
                try {
                    _b.edtAmount.setText(String.valueOf(Double.valueOf(editable.toString()) * Double.valueOf(_b.edtQty.getText().toString())));
                } catch (Exception e) {
                    e.printStackTrace();
                    _b.edtAmount.setText("0");
                }
                mStopWatch = false;
            }
        });
        _b.edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mStopWatch) {
                    return;
                }
                mStopWatch = true;

                try {
                    double p = Double.valueOf(_b.edtQty.getText().toString());
                    if (p > 0) {
                        _b.edtPrice.setText(String.valueOf(Double.valueOf(editable.toString()) / p));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    _b.edtPrice.setText("0");
                }
                mStopWatch = false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtRepeatGoods:
                JsonObject jo = JsonParser.parseString(Preference.getString("repeat_last")).getAsJsonObject();
                jo.addProperty("qty", Double.valueOf(_b.edtQty.getText().toString()));
                jo.addProperty("price", Double.valueOf(_b.edtPrice.getText().toString()));
                jo.addProperty("amount", Double.valueOf(_b.edtAmount.getText().toString()));
                jo.addProperty("windowuuid", Preference.getString("windowuuid"));
                Preference.setString("repeat_last", jo.toString());
                sendRequest(Server.WHAT_STORE_APPEND_ITEM, jo.toString().replace("\"", "\\\""));
                finish();
        }
    }

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
                                    Dlg.createDialog(RepeatLastActivity.this, jo.get("message").getAsString()).setOk(null);
                                }
                                break;
                        }
                    }
                    break;
                case Server.BROADCAST_SOCKET_ERROR:
                    Dlg.createDialog(RepeatLastActivity.this, intent.getStringExtra(Server.SOCKET_REPLY)).setOk(null);
                    break;
            }
        }
    };
}