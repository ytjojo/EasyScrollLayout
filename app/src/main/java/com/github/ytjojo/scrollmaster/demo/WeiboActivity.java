package com.github.ytjojo.scrollmaster.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.ScrollMasterView;

/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class WeiboActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layoutstart);
        final ScrollMasterView easyScrollLayout = (ScrollMasterView) findViewById(R.id.easyScrolllayout);
        easyScrollLayout.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                easyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        easyScrollLayout.setTopHeaderLoadComplete();
                    }
                },4000);
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_simple));
    }
}
