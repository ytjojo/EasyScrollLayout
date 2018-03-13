package com.github.ytjojo.scrollmaster.demo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shizhefei.view.indicator.IndicatorViewPager;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

public class ImageViewgerAdapte extends IndicatorViewPager.IndicatorViewPagerAdapter {
    private int[] images = {R.mipmap.p1, R.mipmap.p2, R.mipmap.p3, R.mipmap.p4};
    @Override
    public View getViewForTab(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = new View(container.getContext());
        }
        return convertView;
    }

    @Override
    public View getViewForPage(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = new ImageView(container.getContext());
            convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        ImageView imageView = (ImageView) convertView;
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(images[position]);
        return convertView;
    }


    @Override
    public int getCount() {
        return images.length;
    }
}
