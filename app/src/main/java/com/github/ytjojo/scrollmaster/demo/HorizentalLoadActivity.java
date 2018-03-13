package com.github.ytjojo.scrollmaster.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.ScrollMasterView;
import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2018/1/1 0001.
 */

public class HorizentalLoadActivity extends AppCompatActivity {
    ScrollMasterView mEasyScrollLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_header);
        mEasyScrollLayout = (ScrollMasterView) findViewById(R.id.horizontal_ScrollMaster);
        mEasyScrollLayout.setLeftOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                Toast.makeText(getApplicationContext(),"left request http",Toast.LENGTH_SHORT).show();
                mEasyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEasyScrollLayout.setLeftComplete();
                    }
                }, 3000);
            }
        });
        mEasyScrollLayout.setRightOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                Toast.makeText(getApplicationContext(),"right request http",Toast.LENGTH_SHORT).show();
                mEasyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEasyScrollLayout.setRightComplete();
                    }
                }, 3000);
            }
        });
        recyclerView= (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Logger.e(dy + "dy   recyclerView" );
            }
        });
        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_horizontal,10));

    }
    RecyclerView recyclerView;


}
