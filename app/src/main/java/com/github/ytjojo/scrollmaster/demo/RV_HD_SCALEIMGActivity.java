package com.github.ytjojo.scrollmaster.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.github.ytjojo.scrollmaster.ScrollMasterView;
import com.github.ytjojo.scrollmaster.Utils;
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
        ScrollMasterView scrollMasterView = (ScrollMasterView) findViewById(R.id.scrollmasterview);
        View headerView = findViewById(R.id.header);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final View imgPhotoView = findViewById(R.id.iv_photo);
        StatusBarUtil.immersive(this);
        StatusBarUtil.setPaddingSmart(this,toolbar);
        final float statusBarHeight = StatusBarUtil.getStatusBarHeight(this);
        headerView.setMinimumHeight((int) (headerView.getMinimumHeight()+ statusBarHeight));
        final int actionbarSize = getResources().getDimensionPixelOffset(R.dimen.actionBarSize);
        final float minImageHeight = actionbarSize * 0.85f;
        final float imageWidth = Utils.dipToPixels(this,60);
        final float ratio = minImageHeight/imageWidth ;
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        final int minMarginLeft = (int) Utils.dipToPixels(this,120);

        scrollMasterView.addOnScrollListener(new ScrollMasterView.OnScollListener() {
            @Override
            public void onScroll(float offsetRatio, int positionOffsetPixels, int offsetRange) {
                ViewCompat.setPivotX(imgPhotoView,0.5f);
                ViewCompat.setPivotX(imgPhotoView,0.5f);
                float scale =  ratio+(1-offsetRatio)*ratio;
                imgPhotoView.setScaleX(scale);
                imgPhotoView.setScaleY(scale);
                imgPhotoView.setTranslationX(-(imgPhotoView.getLeft()-minMarginLeft)*offsetRatio);
                float minMarginTop = actionbarSize /2+ statusBarHeight - minImageHeight/2;
                imgPhotoView.setTranslationY(((imgPhotoView.getTop())*offsetRatio ) );
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_simple));
        AnimateScrimUtil.setScrimBackgroud(scrollMasterView,toolbar,getResources().getColor(R.color.colorPrimary));

    }
}
