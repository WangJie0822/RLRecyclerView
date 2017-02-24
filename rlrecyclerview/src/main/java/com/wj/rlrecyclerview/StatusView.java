package com.wj.rlrecyclerview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * 封装了状态设置相关的控件
 *
 * @author 王杰
 */
abstract class StatusView extends FrameLayout {

    /** 加载中 */
    public static final int STATUS_LOADING = 0xAAA1;
    /** 松开以加载 */
    public static final int STATUS_LOOSEN = 0xAAA2;
    /** 下拉中 */
    public static final int STATUS_PULLING = 0xAAA3;
    /** 加载完成 */
    public static final int STATUS_FINISH = 0xAAA4;
    /** 加载更多提示、数据不足一页时展示 */
    public static final int STATUS_TIPS = 0xAAA5;

    /** 标记-控件状态 */
    protected int status;

    StatusView(Context context) {
        this(context, null);
    }

    StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 加载布局
        LayoutInflater.from(context).inflate(setLayout(), this, true);
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @LayoutRes
    protected abstract int setLayout();

    /**
     * 正在加载中
     */
    public void onLoading() {
        setStatus(STATUS_LOADING);
    }

    /**
     * 松开以加载
     */
    public void onLoosen() {
        setStatus(STATUS_LOOSEN);
    }

    /**
     * 下拉状态
     *
     * @param percent 下拉进度 <p>单位：百分比</p> <p>范围：0~100</p>
     */
    public void onPulling(int percent) {
        setStatus(STATUS_PULLING);
    }

    /**
     * 加载完成
     */
    public void onFinish() {
        setStatus(STATUS_FINISH);
    }

    /**
     * 数据不足一页时加载更多显示
     */
    public void onTips() {
        setStatus(STATUS_TIPS);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
