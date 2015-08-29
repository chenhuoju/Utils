package com.itheima.smartbeijing.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @����:com.itheima.wisdombeijing.utils
 * @����:CacheUtils
 * @����:�»��
 * @ʱ��:2015-8-5 ����1:30:26
 * 
 * 
 * @����:ͨ��sharePreferences�洢����
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
	 * ��ȡboolean���͵Ļ�������,û�еĻ�Ĭ��ֵ��false
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
	 * ��ȡboolean�Ļ�������
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 *            : û��ʱ��Ĭ��ֵ
	 * @return
	 */
	public static boolean getBoolean(Context context, String key, boolean defValue)
	{
		SharedPreferences sp = getPreferences(context);
		return sp.getBoolean(key, defValue);
	}

	/**
	 * ����boolean���͵Ļ���
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
	 * ��ȡString���͵Ļ�������,û�еĻ�Ĭ��ֵ��""
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
	 * ��ȡString�Ļ�������
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 *            : û��ʱ��Ĭ��ֵ
	 * @return
	 */
	public static String getString(Context context, String key, String defValue)
	{
		SharedPreferences sp = getPreferences(context);
		return sp.getString(key, defValue);
	}

	/**
	 * ����String���͵Ļ���
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
