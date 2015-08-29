package com.itheima.smartbeijing.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @包名:com.itheima.wisdombeijing.utils
 * @类名:CacheUtils
 * @作者:陈火炬
 * @时间:2015-8-5 下午1:30:26
 * 
 * 
 * @描述:通过sharePreferences存储数据
 */
public class CacheUtils
{

	private static final String			SP_NAME	= "wisdombeijing";
	private static SharedPreferences	sp;

	private static SharedPreferences getPreferences(Context context)
	{
		if (sp == null)
		{
			sp = context.getSharedPreferences(SP_NAME,
												Context.MODE_PRIVATE);
		}

		return sp;
	}

	/**
	 * 获取boolean类型的缓存数据,没有的话默认值是false
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(Context context, String key)
	{
		SharedPreferences sp = getPreferences(context);
		return sp.getBoolean(key, false);
	}

	/**
	 * 获取boolean的缓存数据
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 *            : 没有时的默认值
	 * @return
	 */
	public static boolean getBoolean(Context context, String key, boolean defValue)
	{
		SharedPreferences sp = getPreferences(context);
		return sp.getBoolean(key, defValue);
	}

	/**
	 * 设置boolean类型的缓存
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setBoolean(Context context, String key, boolean value)
	{
		SharedPreferences sp = getPreferences(context);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 获取String类型的缓存数据,没有的话默认值是""
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getString(Context context, String key)
	{
		SharedPreferences sp = getPreferences(context);
		return sp.getString(key, "");
	}

	/**
	 * 获取String的缓存数据
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 *            : 没有时的默认值
	 * @return
	 */
	public static String getString(Context context, String key, String defValue)
	{
		SharedPreferences sp = getPreferences(context);
		return sp.getString(key, defValue);
	}

	/**
	 * 设置String类型的缓存
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setString(Context context, String key, String value)
	{
		SharedPreferences sp = getPreferences(context);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
