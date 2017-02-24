package com.wj.rlrecyclerview;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;

/**
 * 带上拉刷新下拉加载功能的RecyclerView
 * 可添加头布局、脚布局
 *
 * @author 王杰
 */
public class RLRecyclerView extends RecyclerView implements View.OnTouchListener {

    /** 刷新模式-关闭 */
    public static final String REFRESH_MODE_NONE = "refresh_mode_none";
    /** 刷新模式-全部开启 */
    public static final String REFRESH_MODE_BOTH = "refresh_mode_both";
    /** 刷新模式-下拉刷新 */
    public static final String REFRESH_MODE_REFRESH = "refresh_mode_refresh";
    /** 刷新模式-上拉加载更多 */
    public static final String REFRESH_MODE_LOADMORE = "refresh_mode_loadmore";

    /** 标记-刷新类型 */
    private String refreshType;
    /** 标记-自动刷新 */
    private boolean autoRefresh;

    /** 头布局集合 */
    private ArrayList<View> mHeaders;
    /** 脚布局集合 */
    private ArrayList<View> mFooters;

    /** 刷新控件 */
    private RLRefreshView mRefresh;
    /** 加载更多控件 */
    private RLLoadMoreView mLoadMore;
    /** 刷新控件布局对象 */
    private LayoutParams mRefreshLayoutParams;
    /** 加载更多布局对象 */
    private LayoutParams mLoadMoreLayoutParams;
    /** 隐藏刷新控件所需高度 */
    private int hideRefreshHeight;
    /** 隐藏加载更多所需高度 */
    private int hideLoadMoreHeight;

    /** 使用者设置的Adapter */
    private Adapter mAdapter;
    /** 实现相关功能封装的Adapter */
    private InsideAdapter innerAdapter;

    /** 在被判定为滚动之前用户手指可以移动的最大值 */
    private int touchSlop;

    /** 标记-是否第一次加载，防止onGlobalLayout多次调用 */
    private boolean noFirst;

    public RLRecyclerView(Context context) {
        this(context, null);
    }

    public RLRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RLRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    /** 标记-是否已执行 */
    private boolean isNoFirstLayout;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (isNoFirstLayout) { // 只执行一次
            return;
        }
        isNoFirstLayout = true;

        if (canRefresh()) {
            mRefreshLayoutParams = (LayoutParams) mRefresh.getLayoutParams();
            mRefreshLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mRefresh.setLayoutParams(mRefreshLayoutParams);
            hideRefreshHeight = -mRefresh.getHeight() + 1;
        }
        if (canLoadMore()) {
            mLoadMore.onTips();
        }
    }

    /**
     * 初始化操作
     */
    private void init() {

        // 获取判断为滚动之前的最大值
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        if (TextUtils.isEmpty(refreshType)) {
            // 刷新、加载功能默认关闭
            refreshType = REFRESH_MODE_NONE;
        }

        // 初始化头布局、脚布局集合
        if (mHeaders == null) {
            mHeaders = new ArrayList<>();
        }
        if (mFooters == null) {
            mFooters = new ArrayList<>();
        }

        // 初始化刷新、加载控件
        mRefresh = new RLRefreshView(getContext());
        mLoadMore = new RLLoadMoreView(getContext());
        // 设置加载更多点击事件监听
        mLoadMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 控件为提示状态时，加载更多数据
                if (mLoadMore.getStatus() == StatusView.STATUS_TIPS) {
                    onLoadMore();
                }
            }
        });

        // 设置控件绘制完成后监听
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (noFirst) { // 只运行一次
                    return;
                }
                noFirst = true;

                if (canRefresh()) {
                    if (autoRefresh) { // 自动刷新开启，刷新数据
                        onRefresh();
                    } else { // 不自动刷新，隐藏刷新控件
                        hideRefresh();
                    }
                }
            }
        });

        // 设置触摸事件监听，处理下拉刷新、上拉加载更多
        setOnTouchListener(this);
    }

    @Override
    public void onScrollStateChanged(int state) {

        if (isNotFirstTouch) {
            return;
        }

        if (canLoadMore()) {
            if (SCROLL_STATE_IDLE == state) { // 滑动停止
                if (mLoadMore.getStatus() == StatusView.STATUS_FINISH) {
                    mLoadMore.setVisibility(VISIBLE);
                    if (mLoadMoreLayoutParams == null) {
                        mLoadMoreLayoutParams = (LayoutParams) mLoadMore.getLayoutParams();
                        if (mLoadMoreLayoutParams != null && !isNotFirstTouch) {
                            hideLoadMoreHeight = -mLoadMore.getHeight() + 1;
                            mLoadMoreLayoutParams.bottomMargin = hideLoadMoreHeight;
                            mLoadMoreLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            mLoadMore.setLayoutParams(mLoadMoreLayoutParams);
                            hideLoadMore();
                            isNotFirstTouch = true;
                        }
                    }
                }
            }
        }
    }

    /** 标记-是否获取加载更多布局属性 */
    private boolean isNotFirstTouch;
    /** 下拉刷新起始位置 */
    private float refreshY = -1;
    /** 加载更多起始位置 */
    private float loadMoreY = -1;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (!REFRESH_MODE_NONE.equals(refreshType)) { // 刷新、加载更多功能开启

            // 获取第一个及最后一个可视控件位置
            int[] pos = getVisiblePos();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE: // 移动
                    if (canRefresh()) { // 刷新功能开启
                        if (pos[0] == 0) { // 滑动到顶部
                            if (refreshY == -1) { // 未初始化
                                refreshY = event.getRawY(); // 保存滑动起始位置
                            }
                            float yMove = event.getRawY(); // 获取移动位置
                            int distance = (int) (yMove - refreshY); // 计算移动距离
                            // 如果手指是上滑状态，并且刷新布局是完全隐藏的，不做操作
                            if (distance <= 0 && mRefreshLayoutParams.topMargin <= hideRefreshHeight) {
                                return false;
                            }
                            if (distance < touchSlop) { // 移动距离小于最小响应距离，不做操作
                                return false;
                            }
                            if (mRefresh.getStatus() != StatusView.STATUS_LOADING) { // 不是正在加载中
                                if (mRefreshLayoutParams.topMargin > 0) { // 刷新控件已完全显示，设置为松开刷新
                                    mRefresh.onLoosen();
                                } else { // 没有完全显示，设置为下拉状态
                                    mRefresh.onPulling(getPercent(mRefresh));
                                }
                                // 通过偏移下拉头的topMargin值，来实现下拉效果
                                mRefreshLayoutParams.topMargin = (distance / 2) + hideRefreshHeight;
                                mRefresh.setLayoutParams(mRefreshLayoutParams);
                            }
                        }
                    }
                    if (canLoadMore()) { // 加载更多功能开启
                        if (mLoadMore.getStatus() == StatusView.STATUS_TIPS) { // 数据不足一屏，不做操作
                            return false;
                        }
                        if (pos[1] == innerAdapter.getItemCount() - 1) { // 滑动到底部
                            if (mLoadMoreLayoutParams == null) { // 获取加载更多控件布局对象
                                mLoadMoreLayoutParams = (LayoutParams) mLoadMore.getLayoutParams();
                                if (mLoadMoreLayoutParams == null) { // 布局对象为null，不做操作
                                    return false;
                                } else if (!isNotFirstTouch) { // 第一次获取，仅设置一次
                                    hideLoadMoreHeight = -mLoadMore.getHeight() + 1;
                                    mLoadMoreLayoutParams.bottomMargin = hideLoadMoreHeight;
                                    mLoadMoreLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                    mLoadMore.setLayoutParams(mLoadMoreLayoutParams);
                                    isNotFirstTouch = true;
                                }
                            }

                            if (loadMoreY == -1) { // 未初始化
                                loadMoreY = event.getRawY(); // 保存滑动起始位置
                            }
                            float yMove = event.getRawY(); // 获取移动位置
                            int distance = (int) (yMove - loadMoreY); // 计算移动距离
                            // 如果手指是下滑状态，并且加载更多布局是完全隐藏的，不做操作
                            if (distance >= 0 && mLoadMoreLayoutParams.bottomMargin <= hideLoadMoreHeight) {
                                return false;
                            }
                            if (-distance < touchSlop) { // 移动距离小于最小响应距离，不做操作
                                return false;
                            }
                            if (mLoadMore.getStatus() != StatusView.STATUS_LOADING) { // 不是正在加载中
                                if (mLoadMoreLayoutParams.bottomMargin > 0) { // 刷新控件已完全显示，设置为松开刷新
                                    mLoadMore.onLoosen();
                                } else { // 没有完全显示，设置为上拉状态
                                    mLoadMore.onPulling(getPercent(mLoadMore));
                                }
                                // 通过偏移加载更多控件的bottomMargin属性，实现上拉效果
                                mLoadMoreLayoutParams.bottomMargin = (-distance / 2) + hideLoadMoreHeight;
                                mLoadMore.setLayoutParams(mLoadMoreLayoutParams);
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (canRefresh()) {
                        if (mRefresh.getStatus() == StatusView.STATUS_LOOSEN) {
                            // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                            onRefresh();
                        } else if (mRefresh.getStatus() == StatusView.STATUS_PULLING) {
                            // 松手时如果是下拉状态，隐藏刷新控件
                            hideRefresh();
                        }
                    }
                    if (canLoadMore()) {
                        if (mLoadMore.getStatus() == StatusView.STATUS_LOOSEN) {
                            // 松手时如果是释放立即加载状态，加载更多
                            onLoadMore();
                        } else if (mLoadMore.getStatus() == StatusView.STATUS_PULLING) {
                            // 松手时如果是上拉状态，隐藏加载更多控件
                            hideLoadMore();
                        }
                    }
                    // 重置起始位置
                    refreshY = -1;
                    loadMoreY = -1;
                    break;
                default:
                    break;
            }
            return false;
        }
        return false;
    }

    /**
     * 获取RecyclerView当前显示的第一个控件和最后一个控件的位置
     *
     * @return 位置数组 <p> pos[0]：第一个显示控件的位置</p><p> pos[1]：最后一个显示控件的位置</p>
     */
    private int[] getVisiblePos() {

        int[] pos = new int[2];

        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            pos[0] = gridLayoutManager.findFirstVisibleItemPosition();
            pos[1] = gridLayoutManager.findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int spanCount = staggeredGridLayoutManager.getSpanCount();
            int[] firsts = new int[spanCount];
            int[] lasts = new int[spanCount];
            staggeredGridLayoutManager.findFirstVisibleItemPositions(firsts);
            staggeredGridLayoutManager.findLastVisibleItemPositions(lasts);
            pos[0] = findMin(firsts);
            pos[1] = findMax(lasts);
        } else {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            pos[0] = linearLayoutManager.findFirstVisibleItemPosition();
            pos[1] = linearLayoutManager.findLastVisibleItemPosition();
        }

        return pos;
    }

    /**
     * 获取数组中最大值
     *
     * @param ints int数组
     *
     * @return 最大值
     */
    private int findMax(int[] ints) {
        int max = ints[0];
        for (int value : ints) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * 获取数组中最小值
     *
     * @param ints int数组
     *
     * @return 最小值
     */
    private int findMin(int[] ints) {
        int min = ints[0];
        for (int value : ints) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * 通过控件对象，获取当前控件显示的百分比
     *
     * @param view 刷新、加载更多控件对象
     *
     * @return 显示的百分比 范围：0~100
     */
    private int getPercent(StatusView view) {

        if (view == mRefresh) { // 刷新控件
            int height = mRefresh.getHeight();
            return 100 * (height + mRefreshLayoutParams.topMargin) / height;
        } else if (view == mLoadMore) { // 加载更多控件
            int height = mLoadMore.getHeight();
            if (height == 0) {
                return 0;
            }
            return 100 * (height + mLoadMoreLayoutParams.bottomMargin) / height;
        } else {
            return 100;
        }
    }


    /**
     * 设置刷新类型
     *
     * @param refreshType 刷新类型
     */
    public void setRefreshType(String refreshType) {
        this.refreshType = refreshType;
        if (innerAdapter != null) {
            innerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取刷新类型
     *
     * @return 刷新类型
     */
    public String getRefreshType() {
        return refreshType;
    }

    /**
     * 设置自动刷新开关
     *
     * @param autoRefresh true：开启自动刷新 false：关闭自动刷新
     */
    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    /**
     * 添加头布局，可多次添加，不可重复添加同一对象
     *
     * @param headerView 头布局
     */
    public void addHeaderView(View headerView) {
        checkRepeat(headerView);
        mHeaders.add(headerView);
    }

    /**
     * 清空头布局
     */
    public void clearHeaders() {
        mHeaders.clear();
    }

    /**
     * 添加脚布局，可多次添加，不可重复添加同一对象
     *
     * @param footerView 脚布局
     */
    public void addFooterView(View footerView) {
        checkRepeat(footerView);
        mFooters.add(footerView);
    }

    /**
     * 清空脚布局
     */
    public void clearFooters() {
        mFooters.clear();
    }

    private void checkRepeat(View view) {
        for (View v : mHeaders) {
            if (v == view) {
                throw new RuntimeException("Cannot add same object twice!");
            }
        }
        for (View v : mFooters) {
            if (v == view) {
                throw new RuntimeException("Cannot add same object twice!");
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {

        mAdapter = adapter;
        innerAdapter = new InsideAdapter();

        super.setAdapter(innerAdapter);

        // 注册观察者
        mAdapter.registerAdapterDataObserver(mObserver);
    }

    /**
     * 判断刷新是否开启
     *
     * @return true：开启 false：关闭
     */

    private boolean canRefresh() {
        return REFRESH_MODE_BOTH.equals(refreshType) || REFRESH_MODE_REFRESH.equals(refreshType);
    }

    /**
     * 判断加载更多是否开启
     *
     * @return true：开启 false：关闭
     */
    private boolean canLoadMore() {
        return REFRESH_MODE_BOTH.equals(refreshType) || REFRESH_MODE_LOADMORE.equals(refreshType);
    }

    /**
     * 适配器数据观察者对象，使用者适配器数据变化时，同步通知封装适配器数据变化
     */
    private final RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            innerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            innerAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            innerAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            innerAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            innerAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            innerAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    /**
     * 隐藏下拉刷新布局
     */
    private void hideRefresh() {

        if (canRefresh()) {
            new HideRefreshTask().execute();
        }
    }

    /**
     * 隐藏上拉加载更多布局
     */
    private void hideLoadMore() {

        hideLoadMoreHeight = -mLoadMore.getHeight() + 1;

        if (canLoadMore()) {
            new HideLoadMoreTask().execute();
        }
    }

    /**
     * 刷新、加载更多完成调用
     */
    public void onComplete() {

        if (StatusView.STATUS_LOADING == mRefresh.getStatus()) {
            mRefresh.onFinish();
            hideRefresh();
        }

        if (StatusView.STATUS_LOADING == mLoadMore.getStatus()) {
            mLoadMore.onFinish();
            hideLoadMore();
        }
    }

    /**
     * 添加了头布局、脚布局、下拉刷新、上拉加载更多功能的适配器类
     */
    class InsideAdapter extends Adapter {

        /** 布局类型-刷新布局 */
        private static final int VIEW_TYPE_REFRESH = 0;
        /** 布局类型-头布局 */
        private static final int VIEW_TYPE_HEADER = 1;
        /** 布局类型-普通布局 */
        private static final int VIEW_TYPE_NORMAL = 2;
        /** 布局类型-脚布局 */
        private static final int VIEW_TYPE_FOOTER = 3;
        /** 布局类型-加载更多布局 */
        private static final int VIEW_TYPE_LOADMORE = 4;

        /** 头布局位置 */
        private int headerPosition;
        /** 脚布局位置 */
        private int footerPosition;

        @Override
        public int getItemViewType(int position) {

            if (isRefresh(position)) {
                return VIEW_TYPE_REFRESH;
            } else if (isHeader(position)) {
                return VIEW_TYPE_HEADER;
            } else if (isFooter(position)) {
                return VIEW_TYPE_FOOTER;
            } else if (isLoadMore(position)) {
                return VIEW_TYPE_LOADMORE;
            } else {
                return VIEW_TYPE_NORMAL;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewHolder holder;

            switch (viewType) {
                case VIEW_TYPE_REFRESH:
                    holder = new SimpleViewHolder(mRefresh);
                    break;
                case VIEW_TYPE_HEADER:
                    holder = new SimpleViewHolder(mHeaders.get(headerPosition++));
                    break;
                case VIEW_TYPE_NORMAL:
                    holder = mAdapter.onCreateViewHolder(parent, viewType);
                    break;
                case VIEW_TYPE_FOOTER:
                    holder = new SimpleViewHolder(mFooters.get(footerPosition++));
                    break;
                case VIEW_TYPE_LOADMORE:
                    holder = new SimpleViewHolder(mLoadMore);
                    break;
                default:
                    holder = new SimpleViewHolder(null);
                    break;
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            if (isRefresh(position) || isLoadMore(position)
                    || isHeader(position) || isFooter(position)) {
                return;
            }

            mAdapter.onBindViewHolder(holder, realPosition(position));
        }

        @Override
        public int getItemCount() {

            if (REFRESH_MODE_BOTH.equals(refreshType)) {
                return mAdapter.getItemCount() + mHeaders.size() + mFooters.size() + 2;
            } else if (REFRESH_MODE_REFRESH.equals(refreshType)
                    || REFRESH_MODE_LOADMORE.equals(refreshType)) {
                return mAdapter.getItemCount() + mHeaders.size() + mFooters.size() + 1;
            } else {
                return mAdapter.getItemCount() + mHeaders.size() + mFooters.size();
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {

            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isRefresh(position) || isLoadMore(position)
                                || isHeader(position) || isFooter(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {

            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isRefresh(holder.getLayoutPosition()) || isLoadMore(holder.getLayoutPosition())
                    || isHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }

        class SimpleViewHolder extends RecyclerView.ViewHolder {
            SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }

        /**
         * 根据下标，获取用户设置时的真实下标
         *
         * @param position 当前下标
         *
         * @return 真实下标
         */
        private int realPosition(int position) {
            return (canRefresh())
                    ? position - mHeaders.size() - 1
                    : position - mHeaders.size();
        }

        /**
         * 根据下标，判断是否是刷新布局
         *
         * @param position 下标
         *
         * @return true：刷新布局 false：不是刷新布局
         */
        private boolean isRefresh(int position) {
            return (canRefresh())
                    && position == 0;
        }

        /**
         * 根据下标，判断是否是加载更多布局
         *
         * @param position 下标
         *
         * @return true：加载更多布局 false：不是加载更多布局
         */
        private boolean isLoadMore(int position) {
            return (canLoadMore())
                    && position == getItemCount() - 1;
        }

        /**
         * 根据下标，判断是否是头布局
         *
         * @param position 下标
         *
         * @return true：头布局 false：不是头布局
         */
        private boolean isHeader(int position) {
            return (canRefresh())
                    ? position >= 1 && position < mHeaders.size() + 1
                    : position >= 0 && position < mHeaders.size();
        }

        /**
         * 根据下标，判断是否是脚布局
         *
         * @param position 下标
         *
         * @return true：头脚局 false：不是脚布局
         */
        private boolean isFooter(int position) {
            return (canLoadMore())
                    ? position >= getItemCount() - mFooters.size() - 1 && position < getItemCount() - 1
                    : position >= getItemCount() - mFooters.size() && position < getItemCount();
        }
    }

    /**
     * 隐藏刷新控件的任务
     */
    class HideRefreshTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int topMargin = mRefreshLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin - 20;
                if (topMargin <= hideRefreshHeight) {
                    topMargin = hideRefreshHeight;
                    break;
                }
                publishProgress(topMargin);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            mRefreshLayoutParams.topMargin = topMargin[0];
            mRefresh.setLayoutParams(mRefreshLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            mRefreshLayoutParams.topMargin = topMargin;
            mRefresh.setLayoutParams(mRefreshLayoutParams);
            mRefresh.onFinish();

            if (canLoadMore()) {
                // 刷新完成后检查数据是否一屏显示
                int[] pos = getVisiblePos();
                if (pos[1] == innerAdapter.getItemCount() - 1) {
                    mLoadMore.onTips();
                }
            }
        }
    }

    /**
     * 隐藏加载更多控件的任务
     */
    class HideLoadMoreTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int bottomMargin = mLoadMoreLayoutParams.bottomMargin;
            while (true) {
                bottomMargin = bottomMargin - 20;
                if (bottomMargin <= hideLoadMoreHeight) {
                    bottomMargin = hideLoadMoreHeight;
                    break;
                }
                publishProgress(bottomMargin);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return bottomMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... bottomMarin) {
            mLoadMoreLayoutParams.bottomMargin = bottomMarin[0];
            mLoadMore.setLayoutParams(mLoadMoreLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer bottomMarin) {
            mLoadMoreLayoutParams.bottomMargin = bottomMarin;
            mLoadMore.setLayoutParams(mLoadMoreLayoutParams);
            mLoadMore.onFinish();
        }
    }

    /**
     * 刷新回调，优先{@link OnRefreshListener}
     */
    private void onRefresh() {

        mRefresh.onLoading();
        mRefreshLayoutParams.topMargin = 0;
        mRefresh.setLayoutParams(mRefreshLayoutParams);

        if (refreshListener != null) {
            refreshListener.onRefresh();
            return;
        }
        if (rlListener != null) {
            rlListener.onRefresh();
        }
    }

    /**
     * 加载更多回调，优先{@link OnLoadMoreListener}
     */
    private void onLoadMore() {

        mLoadMore.onLoading();
        mLoadMoreLayoutParams = (LayoutParams) mLoadMore.getLayoutParams();
        mLoadMoreLayoutParams.bottomMargin = 0;
        mLoadMore.setLayoutParams(mLoadMoreLayoutParams);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollBy(0, mLoadMore.getHeight());
            }
        }, 20);

        if (loadMoreListener != null) {
            loadMoreListener.onLoadMore();
            return;
        }
        if (rlListener != null) {
            rlListener.onLoadMore();
        }
    }

    /**
     * 设置刷新事件监听
     *
     * @param listener 刷新事件监听接口{@link OnRefreshListener}
     */
    public void setOnRLListener(OnRefreshListener listener) {
        refreshListener = listener;
    }

    /**
     * 设置加载更多事件监听
     *
     * @param listener 加载更多事件监听接口{@link OnLoadMoreListener}
     */
    public void setOnRLListener(OnLoadMoreListener listener) {
        loadMoreListener = listener;
    }

    /**
     * 设置刷新、加载更多事件监听
     *
     * @param listener 刷新、加载更多事件监听接口{@link OnRLListener}
     */
    public void setOnRLListener(OnRLListener listener) {
        rlListener = listener;
    }

    private OnRLListener rlListener;
    private OnRefreshListener refreshListener;
    private OnLoadMoreListener loadMoreListener;

    public interface OnRLListener {
        void onRefresh();

        void onLoadMore();
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
