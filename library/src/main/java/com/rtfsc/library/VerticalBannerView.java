package com.rtfsc.library;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by kim on 2016/2/24.
 */
public class VerticalBannerView extends LinearLayout implements BaseBannerAdapter.OnDataChangedListener {
    /*默认高度*/
    private float mBannerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
    /*切换间隔*/
    private int mGap = 4000;
    /*切换效果时长*/
    private int mAnimDuration = 1000;

    private BaseBannerAdapter mAdapter;
    /*当前显示item*/
    private View mFirstView;
    /*待显示item*/
    private View mSecondView;
    /*显示完成item*/
    private View mConvertView;

    private int mPosition = 0;
    private boolean isStarted;
    private Paint mDebugPaint;

    private AnimRunnable mRunnable;
    private OnClickListener mOnClickListener;

    public VerticalBannerView(Context context) {
        this(context, null);
    }

    public VerticalBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public VerticalBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (LayoutParams.WRAP_CONTENT == getLayoutParams().height) {
            getLayoutParams().height = (int) mBannerHeight;
        } else {
            mBannerHeight = getHeight();
        }
        if (isInEditMode()) {
            setBackgroundColor(Color.GRAY);
            return;
        }
        if (mFirstView != null) {
            mFirstView.getLayoutParams().height = (int) mBannerHeight;
        }
        if (mSecondView != null) {
            mSecondView.getLayoutParams().height = (int) mBannerHeight;
        }
    }

    /**
     * animDuration 每次切换动画时间
     * gap banner切换时间
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setOrientation(VERTICAL);
        mDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VerticalBannerView);
        mGap = array.getInteger(R.styleable.VerticalBannerView_gap, mGap);
        mAnimDuration = array.getInteger(R.styleable.VerticalBannerView_animDuration, mAnimDuration);
        if (mGap <= mAnimDuration) {
            mGap = 4000;
            mAnimDuration = 1000;
        }
        array.recycle();
        mRunnable = new AnimRunnable();
    }

    public void setAdapter(BaseBannerAdapter baseBannerAdapter) {
        if (baseBannerAdapter == null) {
            throw new RuntimeException("adapter must not be null");
        }
        if (mAdapter != null) {
            throw new RuntimeException("you have already set an Adapter");
        }
        this.mAdapter = baseBannerAdapter;
        mAdapter.setOnDataChangedListener(this);
        setupAdapter();
    }

    /**
     * 初始化adapter
     */
    private void setupAdapter() {
        removeAllViews();
        int count = mAdapter.getCount();
        if (0 == count) {
            return;
        }
        if (mAdapter.getCount() == 1) {//只有一条数据
            mFirstView = mAdapter.getView(this);
            mAdapter.setItem(mFirstView, mAdapter.getItem(0));
            addView(mFirstView);
        } else {    //多条数据
            mFirstView = mAdapter.getView(this);
            mSecondView = mAdapter.getView(this);
            mAdapter.setItem(mFirstView, mAdapter.getItem(0));
            mAdapter.setItem(mSecondView, mAdapter.getItem(1));
            addView(mFirstView);
            addView(mSecondView);
            mPosition = 0;
            isStarted = false;
//            setBackgroundDrawable(mFirstView.getBackground());
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //用来预览可视化界面的
        if (isInEditMode()) {
            mDebugPaint.setColor(Color.WHITE);
            mDebugPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            mDebugPaint.setStyle(Paint.Style.STROKE);
            canvas.drawText("banner", 20, getHeight() * 2 / 3, mDebugPaint);
        }
    }

    @Override
    public void onChanged() {
        setupAdapter();
    }


    private void performSwitch() {
        //向上移动mBannerHeight个高度
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mFirstView, "translationY", ViewHelper.getTranslationY(mFirstView) - mBannerHeight);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mSecondView, "translationY", ViewHelper.getTranslationY(mSecondView) - mBannerHeight);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator1, animator2);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //重置item位置
                ViewHelper.setTranslationY(mFirstView, 0);
                ViewHelper.setTranslationY(mSecondView, 0);
                //得到第一个view,移除并赋值 然后添加到第二个位置
                View removedView = getChildAt(0);
                mPosition++;
                mAdapter.setItem(removedView, mAdapter.getItem(mPosition % mAdapter.getCount()));
                removeView(removedView);
                addView(removedView, 1);
            }
        });
        set.setDuration(mAnimDuration);
        set.start();
    }

    private class AnimRunnable implements Runnable {
        @Override
        public void run() {
            performSwitch();
            postDelayed(this, mGap);
        }
    }

    public void start() {
        if (mAdapter == null) {
            throw new RuntimeException("you must setAdapter first");
        }

        if (!isStarted && mAdapter.getCount() > 1) {
            postDelayed(mRunnable, mGap);
            isStarted = true;
        }
    }

    public void stop() {
        removeCallbacks(mRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
//        super.setOnClickListener(l);
        this.mOnClickListener = l;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_UP) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(getChildAt(0));
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
