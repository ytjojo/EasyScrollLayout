package com.github.ytjojo.scrollmaster.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2017/11/26 0026.
 */

public class RecyclerViewFragment extends Fragment {

    RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) LayoutInflater.from(getActivity()).inflate(R.layout.layout_recyclerview,container,false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Logger.e(dy + "dy   recyclerView" );
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mLastY = (int) event.getY();
                        mFirstY =mLastY;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        mLastY = (int) event.getY();
                        Logger.e("mFirstY" + mFirstY  + " mLastY" + mLastY);
                        break;
                }

                return false;
            }
        });
        return  recyclerView;
    }
    int mLastY;
    int mFirstY;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_simple));

    }
}
