package com.rtfsc.library;

import android.view.View;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kim on 2016/2/24.
 */
public abstract class BaseBannerAdapter<T> {
    private List<T> mDatas;
    private OnDataChangedListener mOnDataChangedListener;

    public BaseBannerAdapter(List<T> datas) {
        if(datas==null || datas.isEmpty()){
            throw new RuntimeException("nothing to show");
        }
        this.mDatas = datas;
    }

    public BaseBannerAdapter(T[] datas) {
        this.mDatas = new ArrayList<>(Arrays.asList(datas));
    }


    /**
     * 设置banner填充的数据
     */
    public void setData(List<T> datas) {
        this.mDatas = datas;
        notifyDataChanged();
    }

    void setOnDataChangedListener(OnDataChangedListener listener) {
        mOnDataChangedListener = listener;
    }

    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    void notifyDataChanged() {
        mOnDataChangedListener.onChanged();
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    /**
     * 设置banner的样式
     */
    public abstract View getView(VerticalBannerView parent);

    /**
     * 设置banner的数据
     */
    public abstract void setItem(View view, T data);

    interface OnDataChangedListener{

        void onChanged();
    }
}
