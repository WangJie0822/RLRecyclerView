package com.wj.rlrecyclerview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 下拉刷新控件
 */
class RLRefreshView extends StatusView {

    private TextView tv;
    private ImageView iv;

    RLRefreshView(Context context) {
        this(context, null);
    }

    RLRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    RLRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        tv = (TextView) findViewById(R.id.tv);
        iv = (ImageView) findViewById(R.id.iv);
    }

    @Override
    protected int setLayout() {
        // TODO 在这里设置加载更多控件布局id
        return R.layout.layout_refresh;
    }

    @Override
    public void onLoading() {
        super.onLoading();

        // TODO 在这里设置正在加载效果

        tv.setText("正在刷新");
        iv.setImageResource(R.drawable.loading);
        ((AnimationDrawable)iv.getDrawable()).start();
    }

    @Override
    public void onFinish() {
        super.onFinish();

        // TODO 在这里设置加载完成后效果

        tv.setText("刷新完成");
        Drawable drawable = iv.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable) drawable).stop();
        }
    }

    @Override
    public void onPulling(int percent) {
        super.onPulling(percent);

        // TODO 在这里设置下拉过程效果

        tv.setText("下拉可以刷新");
        if (percent < 20) {
            iv.setImageResource(R.drawable.pull_end_image_frame_01);
        } else if (percent < 40) {
            iv.setImageResource(R.drawable.pull_end_image_frame_02);
        } else if (percent < 60) {
            iv.setImageResource(R.drawable.pull_end_image_frame_03);
        } else if (percent < 80) {
            iv.setImageResource(R.drawable.pull_end_image_frame_04);
        } else {
            iv.setImageResource(R.drawable.pull_end_image_frame_05);
        }

    }

    @Override
    public void onLoosen() {
        super.onLoosen();

        // TODO 在这里设置释放加载效果

        tv.setText("释放刷新");
    }
}
