package com.github.ytjojo.scrollmaster.styles;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.UIHandler;
import com.github.ytjojo.scrollmaster.Utils;

public class MaterialHeaderView extends FrameLayout implements UIHandler {

    private final static int DEFAULT_PROGRESS_SIZE = 50;
    private final static int BIG_PROGRESS_SIZE = 60;
    private final static String Tag = MaterialHeaderView.class.getSimpleName();
    private int progressTextColor;
    private int[] mSchemeColors;
    private int progressStokeWidth =3;
    private boolean isShowArrow = false, isShowProgressBg = true;
    private int progressValue, progressValueMax = 100;
    private int textType;
    private int progressBg = 0xFFFAFAFA;
    private int mProgressViewSize = DEFAULT_PROGRESS_SIZE;
    private static float density;
    ImageView mImageView;

    public MaterialHeaderView(Context context) {
        this(context, null);
    }

    public MaterialHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(getLayoutParams().height== ViewGroup.LayoutParams.WRAP_CONTENT){
            setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) Utils.dipToPixels(getContext(),60),MeasureSpec.EXACTLY));
        }
    }

    protected void init(AttributeSet attrs, int defStyle) {
        if (isInEditMode()) return;
        setClipChildren(false);
        setClipToPadding(false);
        setWillNotDraw(false);
        mSchemeColors = new int[4];
        mSchemeColors[0] = 0xffF44336;
        mSchemeColors[1] = 0xff4CAF50;
        mSchemeColors[2] = 0xff03A9F4;
        mSchemeColors[3] = 0xffFFEB3B;

        density = getContext().getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) density * mProgressViewSize, (int) density * mProgressViewSize);
        layoutParams.gravity = Gravity.CENTER;
//        addView(circleProgressBar);
        mImageView = new ImageView(getContext());
        int padding = (int) Utils.dipToPixels(getContext(),3);
        this.setPadding(0,padding,0,padding);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        materialDrawable = new MaterialDrawable(getContext(),this);
        materialDrawable.setBackgroundColor(progressBg);
        materialDrawable.setColorSchemeColors(mSchemeColors);
        materialDrawable.setStartEndTrim(0,0.8f);
        mImageView.setImageDrawable(materialDrawable);
        addView(mImageView,layoutParams);


    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        materialDrawable.stop();
    }

    MaterialDrawable materialDrawable;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    public void onUIReset(BaseRefreshIndicator indicator) {
        ViewCompat.setTranslationY(mImageView, 0);
        ViewCompat.setScaleX(mImageView, 0);
        ViewCompat.setScaleY(mImageView, 0);
        materialDrawable.stop();

    }

    @Override
    public void onUIRefreshPrepare(BaseRefreshIndicator indicator) {
        ViewCompat.setPivotX(mImageView,0.5f*mImageView.getWidth());
        ViewCompat.setPivotY(mImageView,0.5f* mImageView.getHeight());
        ViewCompat.setScaleX(mImageView, 0.001f);
        ViewCompat.setScaleY(mImageView, 0.001f);
        materialDrawable.setStartEndTrim(0,0.8f);
        materialDrawable.setArrowScale(1);
        materialDrawable.showArrow(true);
    }

    @Override
    public void onUIRefreshBegin(BaseRefreshIndicator indicator) {
//        materialDrawable.setStartEndTrim(0,0.8f);
        materialDrawable.start();
    }

    @Override
    public void onUIRefreshComplete(BaseRefreshIndicator indicator) {
    }

    @Override
    public void onUIReleaseBeforeRefresh(BaseRefreshIndicator indicator) {
//        materialDrawable.showArrow(false);
//        materialDrawable.setProgressRotation(0.8f);
    }

    @Override
    public void onUIScrollChanged(BaseRefreshIndicator indicator, int scrollValue, byte status) {
        float progress = indicator.getProgress(scrollValue);
        if(status!= BaseRefreshIndicator.PTR_STATUS_LOADING){
            materialDrawable.setProgressRotation(Math.min(progress*0.7f,1));
        }
        if(progress> 1.15f){
            progress = 1.15f;
        }
        ViewCompat.setScaleX(mImageView, progress);
        ViewCompat.setScaleY(mImageView, progress);

    }
}