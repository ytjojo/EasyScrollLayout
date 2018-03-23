package com.github.ytjojo.scrollmaster.demo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class BaseRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    int mLayoutId;
    View mHeader;
    int mSetedItemCount = 30;

    public BaseRecyclerViewAdapter(int layoutId){
        this.mLayoutId = layoutId;
    }
    public BaseRecyclerViewAdapter(int layoutId,View header){
        this.mLayoutId = layoutId;
        this.mHeader = header;
    }
    public BaseRecyclerViewAdapter(int layoutId,int count){
        this.mLayoutId = layoutId;
        this.mSetedItemCount = count;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0){
           VH viewHolder =  new VH(LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent,false));
            return viewHolder;
        }else {
            return  new VH(mHeader);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),""+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSetedItemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if(mHeader == null){
            return 0;
        }else {
            if(position == 0){
                return 1;
            }else {
                return 0;
            }
        }

    }

    public static class VH extends RecyclerView.ViewHolder {

        public VH(View itemView) {
            super(itemView);
        }
    }
}
