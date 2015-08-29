package com.itheima.smartbeijing.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @包名:com.itheima.smartbeijing.widget
 * @类名:HorizontalScrollViewPager
 * @作者:陈火炬
 * @时间:2015-8-10 下午3:40:14
 * 
 * @描述:水平滚动的ViewPager不让父ViewPager抢占touch事件
 * 
 * @SVN版本号:$Rev: 25 $
 * @更新人:$Author: chj $
 * @更新描述:TODO
 * 
 */
public class HorizontalScrollViewPager extends ViewPager
{

	private float	downX;
	private float	downY;
	private float	moveX;
	private float	moveY;

	public HorizontalScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HorizontalScrollViewPager(Context context) {
		super(context);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		// 1.从右往左
		// 如果是在除去最后的页面，手指从右往左滑动，就进入下一个页面，即自己响应touch事件

		// 如果是最后一个页面，手指从右往左滑动，就让父容器响应touch事件---> 到了最后一个ViewPager会自己处理

		// 2.从左往右
		// 如果是在第一个页面，手指从左往右滑动，就让父容器响应touch事件

		// 如果是在除去第一的页面，手指从左往右滑动，就进入上一个页面,即自己响应touch事件

		switch (ev.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				getParent().requestDisallowInterceptTouchEvent(true);

				downX = ev.getX();
				downY = ev.getY();

				break;
			case MotionEvent.ACTION_MOVE:
				moveX = ev.getX();
				moveY = ev.getY();

				float diffX = moveX - downX;
				float diffY = moveY - downY;

				// diffX>0:从左往右
				// diffX<0:从右往左

				if (Math.abs(diffX) > Math.abs(diffY))
				{
					// 认为用户操作为水平操作

					if (diffX > 0 && getCurrentItem() == 0)
					{
						// 如果是在第一个页面，手指从左往右滑动，就让父容器响应touch事件
						getParent().requestDisallowInterceptTouchEvent(false);
					}
					else if (diffX > 0 && getCurrentItem() != 0)
					{
						// 如果是在除去第一的页面，手指从左往右滑动，就进入上一个页面,即自己响应touch事件
						getParent().requestDisallowInterceptTouchEvent(true);
					}
					else if (diffX < 0 && getCurrentItem() == (getAdapter().getCount() - 1))
					{
						// 从右往左
						// 如果是在最后一个的页面，手指从右往左滑动，就让父容器响应touch事件
						getParent().requestDisallowInterceptTouchEvent(false);
					}
					else
					{
						// 从右往左
						// 如果是在除去最后的页面，手指从右往左滑动，就进入下一个页面，即自己响应touch事件
						getParent().requestDisallowInterceptTouchEvent(true);
					}
				}
				else
				{
					// touch交给父容器
					getParent().requestDisallowInterceptTouchEvent(false);
				}
				break;
			default:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}
}
