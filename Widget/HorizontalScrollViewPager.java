package com.itheima.smartbeijing.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @����:com.itheima.smartbeijing.widget
 * @����:HorizontalScrollViewPager
 * @����:�»��
 * @ʱ��:2015-8-10 ����3:40:14
 * 
 * @����:ˮƽ������ViewPager���ø�ViewPager��ռtouch�¼�
 * 
 * @SVN�汾��:$Rev: 25 $
 * @������:$Author: chj $
 * @��������:TODO
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
		// 1.��������
		// ������ڳ�ȥ����ҳ�棬��ָ�������󻬶����ͽ�����һ��ҳ�棬���Լ���Ӧtouch�¼�

		// ��������һ��ҳ�棬��ָ�������󻬶������ø�������Ӧtouch�¼�---> �������һ��ViewPager���Լ�����

		// 2.��������
		// ������ڵ�һ��ҳ�棬��ָ�������һ��������ø�������Ӧtouch�¼�

		// ������ڳ�ȥ��һ��ҳ�棬��ָ�������һ������ͽ�����һ��ҳ��,���Լ���Ӧtouch�¼�

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

				// diffX>0:��������
				// diffX<0:��������

				if (Math.abs(diffX) > Math.abs(diffY))
				{
					// ��Ϊ�û�����Ϊˮƽ����

					if (diffX > 0 && getCurrentItem() == 0)
					{
						// ������ڵ�һ��ҳ�棬��ָ�������һ��������ø�������Ӧtouch�¼�
						getParent().requestDisallowInterceptTouchEvent(false);
					}
					else if (diffX > 0 && getCurrentItem() != 0)
					{
						// ������ڳ�ȥ��һ��ҳ�棬��ָ�������һ������ͽ�����һ��ҳ��,���Լ���Ӧtouch�¼�
						getParent().requestDisallowInterceptTouchEvent(true);
					}
					else if (diffX < 0 && getCurrentItem() == (getAdapter().getCount() - 1))
					{
						// ��������
						// ����������һ����ҳ�棬��ָ�������󻬶������ø�������Ӧtouch�¼�
						getParent().requestDisallowInterceptTouchEvent(false);
					}
					else
					{
						// ��������
						// ������ڳ�ȥ����ҳ�棬��ָ�������󻬶����ͽ�����һ��ҳ�棬���Լ���Ӧtouch�¼�
						getParent().requestDisallowInterceptTouchEvent(true);
					}
				}
				else
				{
					// touch����������
					getParent().requestDisallowInterceptTouchEvent(false);
				}
				break;
			default:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}
}
