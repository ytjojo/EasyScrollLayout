package com.github.ytjojo.easyscrolllayout.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Logger.e(dy + "dy");
            }
        });
        return  recyclerView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple, parent,false)) ;
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 50;
            }
        });
    }
    public static class MyViewHolder extends ViewHolder{

        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
