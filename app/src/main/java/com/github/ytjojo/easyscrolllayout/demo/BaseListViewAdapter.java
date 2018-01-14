package com.github.ytjojo.easyscrolllayout.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/14 0014.
 */

public abstract class  BaseListViewAdapter <T> extends BaseAdapter{
    ArrayList<T> mDataList;
    int mLayoutId;
    public BaseListViewAdapter(ArrayList<T> list,int layoutId){
        this.mDataList = list;
        this.mLayoutId = layoutId;
    }
    @Override
    public int getCount() {
       return mDataList==null?0:mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList==null?null:mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView ==null){
           convertView = LayoutInflater.from(parent.getContext()).inflate(mLayoutId,parent,false);
        }
        bindData2View(position,mDataList.get(position),convertView,parent);
        return convertView;
    }
    public abstract void bindData2View(int position,T model, View convertView, ViewGroup parent);
}
