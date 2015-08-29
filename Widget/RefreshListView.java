package com.itheima.smartbeijing.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itheima.smartbeijing.R;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * @包名:com.itheima.smartbeijing.widget
 * @类名:RefreshListView
 * @作者:陈火炬
 * @时间:2015-8-10 下午9:56:54
 * 
 * @描述:下拉刷新，上拉加载
 * 
 * @SVN版本号:$Rev: 33 $
 * @更新人:$Author: chj $
 * @更新描述:TODO
 * 
 */
public class RefreshListView extends ListView implements OnScrollListener
{

	private static final String	TAG						= "RefreshListView";
	private static final int	STATE_PULL_DOWN_REFRESH	= 0;						// 下拉刷新状态
	private static final int	STATE_RELEASE_REFRESH	= 1;						// 释放刷新状态
	private static final int	STATE_RELEASEING		= 2;						// 正在刷新状态

	private LinearLayout		mHeaderLayout;										// 包含刷新的view和自定义的headerView
	private View				mCustomHeaderView;									// 自定义的headerView
	private View				mRefreshView;										// 刷新的view
	private int					mRefreshViewHeight;								// 刷新view的高度

	private float				mDownY;											// 起始位置的坐标
	private float				mDiffY;											// 滑动的距离

	private int					mCurrentState			= STATE_PULL_DOWN_REFRESH;	// 当前状态，默认值

	private ProgressBar			mPBar;												// 进度
	private ImageView			mIvArrow;											// 箭头
	private TextView			mTvState;											// 状态显示
	private TextView			mTvUpdateTime;										// 时间显示

	private View				mFooterLayout;										// 底部加载更多的布局
	private int					mFooterHeight;										// 底部加载的高度
	private static boolean		isLoadingMore			= false;					// 标记是否加载更多

	private RotateAnimation		mDownToUpAnimation;								// 给释放刷新准备的
	private RotateAnimation		mUpToDownAnimation;								// 给下拉刷新准备的
	private static final long	DURATION				= 300;						// 动画持续时间

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 加载头布局
		initHeaderLayout();
		// 加载尾布局
		initFooterLayout();

		initAnimation();
	}

	public RefreshListView(Context context) {
		super(context);

		// 加载头布局
		initHeaderLayout();
		// 加载尾布局
		initFooterLayout();

		initAnimation();
	}

	/**
	 * 初始化头布局
	 */
	private void initHeaderLayout()
	{
		// 加载头布局
		mHeaderLayout = (LinearLayout) View.inflate(getContext(), R.layout.refresh_header_layout, null);

		// 添加到listView的headerView中
		this.addHeaderView(mHeaderLayout);

		// 需要隐藏刷新的view
		mRefreshView = mHeaderLayout.findViewById(R.id.refresh_header_refresh_part);
		mPBar = (ProgressBar) mHeaderLayout.findViewById(R.id.refresh_header_pBar);
		mIvArrow = (ImageView) mHeaderLayout.findViewById(R.id.refresh_header_arrow);
		mTvState = (TextView) mHeaderLayout.findViewById(R.id.refresh_header_tv_state);
		mTvUpdateTime = (TextView) mHeaderLayout.findViewById(R.id.refresh_header_tv_time);

		// 给头布局设置PaddingTop为负数来隐藏控件
		mRefreshView.measure(0, 0);// 主动去测量控件的宽高
		mRefreshViewHeight = mRefreshView.getMeasuredHeight();
		// Log.e(TAG, "高度：" + mRefreshViewHeight);
		mHeaderLayout.setPadding(0, -mRefreshViewHeight, 0, 0);
	}

	/**
	 * 初始化尾布局
	 * 
	 * @return
	 */
	private void initFooterLayout()
	{
		mFooterLayout = View.inflate(getContext(), R.layout.refresh_footer_layout, null);
		// 添加到listView的footerView中
		this.addFooterView(mFooterLayout);
		// 隐藏footerLayout
		mFooterLayout.measure(0, 0);
		mFooterHeight = mFooterLayout.getMeasuredHeight();
		mFooterLayout.setPadding(0, -mFooterHeight, 0, 0);
		// 设置当listView滑动时的监听
		this.setOnScrollListener(this);
	}

	/**
	 * 初始化动画
	 */
	private void initAnimation()
	{
		mDownToUpAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mDownToUpAnimation.setDuration(DURATION);// 设置动画持续时间
		mDownToUpAnimation.setFillEnabled(true);
		mDownToUpAnimation.setFillAfter(true);// 保持动画结束的状态

		mUpToDownAnimation = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mUpToDownAnimation.setDuration(DURATION);// 设置动画持续时间
		mUpToDownAnimation.setFillEnabled(true);
		mUpToDownAnimation.setFillAfter(true);// 保持动画结束的状态
	}

	/**
	 * 用户自定义headerView部分
	 * 
	 * @param headerView
	 */
	public void addCustomHeaderView(View headerView)
	{
		this.mCustomHeaderView = headerView;
		mHeaderLayout.addView(headerView);
	}

	/**
	 * 处理触摸屏幕事件方法
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		switch (ev.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				mDownY = ev.getY();

				break;
			case MotionEvent.ACTION_MOVE:
				float moveY = ev.getY();

				mDiffY = moveY - mDownY;

				// 如果当前状态是正在刷新，我们就不作处理
				if (mCurrentState == STATE_RELEASEING)
				{
					break;
				}

				// 获得listView的坐标
				int[] listViewLocation = new int[2];// 0:x 1:y
				getLocationOnScreen(listViewLocation);
				// Log.e(TAG, "listView y:" + listViewLocation[1]);

				int[] customLocation = new int[2];
				mCustomHeaderView.getLocationOnScreen(customLocation);
				// Log.e(TAG, "mCustomHeaderView y:" + customLocation[1]);

				if (customLocation[1] < listViewLocation[1])
				{
					// 不响应自己的事件
					break;
				}

				// mDiffY>0:下拉
				// mDiffY<0:s上拉
				// 如果第一item可见是，才可以下拉 && 往下拉
				// int position = getFirstVisiblePosition();
				if (getFirstVisiblePosition() == 0 && mDiffY > 0)
				{
					// 给头布局设置PaddingTop
					int hiddenHeight = (int) (mRefreshViewHeight - mDiffY + 0.5f);
					mHeaderLayout.setPadding(0, -hiddenHeight, 0, 0);

					if (mDiffY < mRefreshViewHeight && mCurrentState == STATE_RELEASE_REFRESH)
					{
						// mDiffY<mRefreshViewHeight:下拉刷新
						// 更新状态
						mCurrentState = STATE_PULL_DOWN_REFRESH;

						// UI更新
						// Log.e(TAG, "下拉刷新");
						refreshUI();
					}
					else if (mDiffY >= mRefreshViewHeight && mCurrentState == STATE_PULL_DOWN_REFRESH)
					{
						// mDiffY>mRefreshViewHeight:释放刷新
						mCurrentState = STATE_RELEASE_REFRESH;

						// UI更新
						// Log.e(TAG, "释放刷新");
						refreshUI();
					}
					// 需要自己响应touch
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mDownY = 0;

				// 释放后的操作
				if (mCurrentState == STATE_PULL_DOWN_REFRESH)
				{
					// 如果是下拉刷新状态，就隐藏刷新的view,设置得太突然
					// mHeaderLayout.setPadding(0, -mRefreshViewHeight, 0, 0);
					int currentPaddingTop = (int) (mDiffY - mRefreshViewHeight + 0.5f);
					doHeaderPaddingAnimation(currentPaddingTop, -mRefreshViewHeight);
				}
				else if (mCurrentState == STATE_RELEASE_REFRESH)
				{
					// 如果是释放刷新状态，用户就希望刷新数据-->进入正在刷新状态
					mCurrentState = STATE_RELEASEING;

					// 设置PaddingTop为0,设置得太突然
					int currentPaddingTop = (int) (mDiffY - mRefreshViewHeight + 0.5f);

					// mHeaderLayout.setPadding(0, currentPaddingTop, 0, 0);
					// mHeaderLayout.setPadding(0, 0, 0, 0);
					doHeaderPaddingAnimation(currentPaddingTop, 0);

					// UI更新
					refreshUI();

					// 通知调用者，现在处于正在刷新状态
					if (listener != null)
					{
						listener.onRefreshing();
					}
				}
				break;
			default:
				break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 属性动画设置方法，处理头部填充动画
	 * 
	 * @param start起始值
	 * @param end结束值
	 */
	private void doHeaderPaddingAnimation(int start, int end)
	{
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.setDuration(DURATION);
		animator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				int animatedValue = (Integer) animation.getAnimatedValue();
				mHeaderLayout.setPadding(0, animatedValue, 0, 0);
			}
		});
		animator.start();
	}

	/**
	 * 属性动画设置方法，处理尾部填充动画
	 * 
	 * @param start起始值
	 * @param end结束值
	 */
	private void doFooterPaddingAnimation(int start, int end)
	{
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.setDuration(DURATION);
		animator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				int animatedValue = (Integer) animation.getAnimatedValue();
				mFooterLayout.setPadding(0, animatedValue, 0, 0);

				// 自动默认选中i
				setSelection(getAdapter().getCount());
			}
		});
		animator.start();
	}

	/**
	 * UI更新方法
	 */
	private void refreshUI()
	{
		switch (mCurrentState)
		{
			case STATE_PULL_DOWN_REFRESH:// 下拉刷新
				// 1.箭头要显示，进度条隐藏
				mIvArrow.setVisibility(View.VISIBLE);
				mPBar.setVisibility(View.GONE);
				// 2.文本状态显示
				mTvState.setText("下拉刷新");
				// 3.箭头动画
				mIvArrow.startAnimation(mUpToDownAnimation);
				break;
			case STATE_RELEASE_REFRESH:// 松开刷新
				// 1.箭头要显示，进度条隐藏
				mIvArrow.setVisibility(View.VISIBLE);
				mPBar.setVisibility(View.GONE);
				// 2.文本状态显示
				mTvState.setText("松开刷新");
				// 3.箭头动画
				mIvArrow.startAnimation(mDownToUpAnimation);
				break;
			case STATE_RELEASEING:// 正在刷新
				// 清空动画
				mIvArrow.clearAnimation();
				// 1.箭头要隐藏，进度条显示
				mIvArrow.setVisibility(View.GONE);
				mPBar.setVisibility(View.VISIBLE);
				// 2.文本状态显示
				mTvState.setText("正在刷新");
				break;
			default:
				break;
		}
	}

	/**
	 * 告知listView刷新完成
	 */
	public void refreshFinish()
	{
		if (isLoadingMore)
		{
			// 上拉加载

			// 隐藏加载更多的view
			// mFooterLayout.setPadding(0, -mFooterHeight, 0, 0);
			doFooterPaddingAnimation(0, -mFooterHeight);

			isLoadingMore = false;
		}
		else
		{
			// 下拉刷新
			Log.e(TAG, "刷新结束");

			// 设置当前更新时间
			mTvUpdateTime.setText(getCurrentTimeString());

			// 隐藏刷新的view
			// mHeaderLayout.setPadding(0, -mRefreshViewHeight, 0, 0);
			doHeaderPaddingAnimation(0, -mRefreshViewHeight);

			// 状态重置
			mCurrentState = STATE_PULL_DOWN_REFRESH;

			// UI更新
			refreshUI();
		}
	}

	private OnRefreshListener	listener;

	public void setOnRefreshListener(OnRefreshListener listener)
	{
		this.listener = listener;
	}

	/**
	 * 定义回调接口，在里面定义回调方法
	 * 
	 * 回调方法：就是自己不想调用，定义出来给其他人使用
	 */
	public interface OnRefreshListener
	{
		/**
		 * 正在刷新时的回调
		 */
		void onRefreshing();

		/**
		 * 加载更多时的回调
		 */
		void onLoadingMore();
	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	private String getCurrentTimeString()
	{
		long time = System.currentTimeMillis();

		// 设置时间显示样式
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return date.format(new Date(time));

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// 最后一个可见的时候
		int lastVisiblePosition = getLastVisiblePosition();
		if (lastVisiblePosition == getAdapter().getCount() - 1)
		{
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				|| scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
			{
				if (!isLoadingMore)
				{
					// 滑动到了底部
					Log.e(TAG, "滑动到底部");

					// UI操作
					// mFooterLayout.setPadding(0, 0, 0, 0);
					doFooterPaddingAnimation(-mFooterHeight, 0);

					// 自动默认选中i
					// setSelection(getAdapter().getCount());

					// 是滑动到底部
					isLoadingMore = true;

					// 通知调用者，状态已经变化了,现在处于加载更多状态
					if (listener != null)
					{
						listener.onLoadingMore();
					}
				}
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{

	}
}
