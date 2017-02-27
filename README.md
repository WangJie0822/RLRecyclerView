# RLRecyclerView

## 介绍
* 这是一个集成了下拉刷新、上拉加载更多，并且可以简单的添加头布局、脚布局的RecyclerView，
* 使用方式和RecyclerView完全一致，不需要进行过多的设置。
![](https://github.com/Lorry0822/RLRecyclerView/blob/master/RMPIC/b.gif)

## 使用
* 使用前，可以从Github上将目录下的 rlrecyclerview 下载下来，这是一个Android Library 项目，
你可以直接在项目中引用。![](https://github.com/Lorry0822/RLRecyclerView/blob/master/RMPIC/a.png)


		dependencies {
		    compile project(':rlrecyclerview')
		}


* 在布局文件中直接使用，也无需过多的设置
		
		<com.wj.rlrecyclerview.RLRecyclerView
		        android:id="@+id/rv"
		        android:layout_width="match_parent"
		        android:layout_height="match_parent"/>

* findViewById 后，和RecyclerView一样设置适配器即可

* RLRecyclerView中，提供了相关方法开启刷新、加载更多等功能：

			/** 功能模式-关闭 */
			public static final String REFRESH_MODE_NONE;
			/** 功能模式-全部开启 */
			public static final String REFRESH_MODE_BOTH;
			/** 功能模式-下拉刷新 */
			public static final String REFRESH_MODE_REFRESH;
			/** 功能模式-上拉加载更多 */
			public static final String REFRESH_MODE_LOADMORE;

		    /**
		      * 设置功能类型
		      *
		      * @param mode 功能类型
		      */
		    public void setMode(String mode) 
		
		    /**
		      * 设置自动刷新开关
		      *
		      * @param autoRefresh true：开启自动刷新 false：关闭自动刷新
		      */
		    public void setAutoRefresh(boolean autoRefresh)

		   /**
		     * 添加头布局，可多次添加，不可重复添加同一对象
		     *
		     * @param headerView 头布局
		     */
		    public void addHeaderView(View headerView)
		
		    /**
		     * 清空头布局
		     */
		    public void clearHeaders() 
		
		    /**
		     * 添加脚布局，可多次添加，不可重复添加同一对象
		     *
		     * @param footerView 脚布局
		     */
		    public void addFooterView(View footerView)
		
		    /**
		     * 清空脚布局
		     */
		    public void clearFooters()
		
		    /**
		     * 设置刷新事件监听
		     *
		     * @param listener 刷新事件监听接口{@link OnRefreshListener}
		     */
		    public void setOnRLListener(OnRefreshListener listener)
		
		    /**
		     * 设置加载更多事件监听
		     *
		     * @param listener 加载更多事件监听接口{@link OnLoadMoreListener}
		     */
		    public void setOnRLListener(OnLoadMoreListener listener)
		
		    /**
		     * 设置刷新、加载更多事件监听
		     *
		     * @param listener 刷新、加载更多事件监听接口{@link OnRLListener}
		     */
		    public void setOnRLListener(OnRLListener listener) 

		   /**
		     * 刷新、加载更多完成调用
		     */
		    public void onComplete() 

* 通过这些方法，你可以非常方便的使用相关功能。
* 项目中，已经实现了简单的动画效果，如果需要自己定制刷新、加载动画，你可以找到项目目录下有RLRefreshView、RLLoadMoreView
* 两个控件，通过修改里面的方法就可以实现你自己的刷新、加载效果，修改的地方我已经用 // TODO 注明，你可以很轻易地找到并修改。

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
		    public void onLoading()
		
		    /**
		     * 松开以加载
		     */
		    public void onLoosen()
		
		    /**
		     * 下拉状态
		     *
		     * @param percent 下拉进度 <p>单位：百分比</p> <p>范围：0~100</p>
		     */
		    public void onPulling(int percent)
		
		    /**
		     * 加载完成
		     */
		    public void onFinish() 
		
		    /**
		     * 数据不足一页时加载更多显示
		     */
		    public void onTips()

## 总结

* 像这样的控件现在其实有很多，但是我还是选择自己写了一个，当然了，其中肯定有许多我没有发现的，开发中也有很多的困扰，
* 其中就借鉴了郭霖大大的[ Android下拉刷新完全解析，教你如何一分钟实现下拉刷新功能](http://blog.csdn.net/guolin_blog/article/details/9255575)，以及 [XRecyclerView](https://github.com/jianghejie/XRecyclerView) 的部分源码，
* 感谢各位前辈的无私分享，在开发的过程中我学到了很多。如果你在使用中发现什么问题，请告诉我，感谢大家的使用和指正。


