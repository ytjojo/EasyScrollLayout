package com.github.ytjojo.scrollmaster.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.ScrollMasterView;
import com.github.ytjojo.scrollmaster.demo.util.StatusBarUtil;
import com.github.ytjojo.scrollmaster.util.AnimateScrimUtil;

/**
 * Created by Administrator on 2018/3/23 0023.
 */

public class RV_MIN_HDActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceStat) {
        super.onCreate(savedInstanceStat);
        setContentView(R.layout.activity_minheigt_recyclerview);
        final ScrollMasterView easyScrollLayout = (ScrollMasterView) findViewById(R.id.easyScrolllayout);
        easyScrollLayout.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                easyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        easyScrollLayout.setTopHeaderLoadComplete();
                    }
                }, 4000);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                DrawerRecylerViewWithHeaderActivity.MyViewHolder viewHolder;
                viewHolder = new DrawerRecylerViewWithHeaderActivity.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple, parent, false));
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 20;
            }

        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        StatusBarUtil.darkMode(this);
        StatusBarUtil.setPaddingSmart(this,toolbar);
        View header = findViewById(R.id.header);
        header.setMinimumHeight(header.getMinimumHeight()+ StatusBarUtil.getStatusBarHeight(this));
        AnimateScrimUtil.setScrimBackgroud(easyScrollLayout,toolbar,getResources().getColor(R.color.colorPrimary));

    }
}
