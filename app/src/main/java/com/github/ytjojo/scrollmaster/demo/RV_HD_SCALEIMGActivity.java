package com.github.ytjojo.scrollmaster.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.ScrollMasterView;
import com.github.ytjojo.scrollmaster.Utils;
import com.github.ytjojo.scrollmaster.behavior.TransiationScaleBehvior;
import com.github.ytjojo.scrollmaster.demo.util.StatusBarUtil;
import com.github.ytjojo.scrollmaster.util.AnimateScrimUtil;

/**
 * Created by Administrator on 2018/4/1 0001.
 */

public class RV_HD_SCALEIMGActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv_hd_ab_trsimg);
        final ScrollMasterView scrollMasterView = (ScrollMasterView) findViewById(R.id.scrollmasterview);
        scrollMasterView.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                scrollMasterView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollMasterView.setTopHeaderLoadComplete();
                    }
                }, 3000);
            }
        });
        View headerView = findViewById(R.id.header);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final View imgPhotoView = findViewById(R.id.iv_photo);
        StatusBarUtil.immersive(this);
        StatusBarUtil.setPaddingSmart(this,toolbar);
        StatusBarUtil.setPaddingSmart(this,findViewById(R.id.materiaRefreshHeader));
        final float statusBarHeight = StatusBarUtil.getStatusBarHeight(this);
        headerView.setMinimumHeight((int) (headerView.getMinimumHeight()+ statusBarHeight));
        final int actionbarSize = getResources().getDimensionPixelOffset(R.dimen.actionBarSize);
        final int minImageHeight = (int) (actionbarSize * 0.85f);
        final int minMarginLeft = (int) Utils.dipToPixels(this,120);
        TransiationScaleBehvior.setTranslationScale(scrollMasterView,imgPhotoView,minImageHeight,minMarginLeft,actionbarSize,statusBarHeight);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_simple));
        AnimateScrimUtil.setScrimBackgroud(scrollMasterView,toolbar,getResources().getColor(R.color.colorPrimary));

    }
}
