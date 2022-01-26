package com.example.cafe5mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

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
                int i = getAdapterPosition();
                finish();
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
}