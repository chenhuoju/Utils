package com.itheima.smartbeijing.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

/**
 * @����:com.itheima.smartbeijing.utils
 * @����:ImageHelper
 * @����:�»��
 * @ʱ��:2015-8-12 ����6:06:10
 * 
 * @����:ͼƬ�������--->�����������
 * 
 * @SVN�汾��:$Rev: 37 $
 * @������:$Author: chj $
 * @��������:TODO
 * 
 */
public class ImageHelper
{
	private static LruCache<String, Bitmap>		mCaches	= null; // ��������
	private ImageHandler						mHandler;
	private static ExecutorService				mPool;			// �̳߳�
	private static Map<ImageView, Future<?>>	mFutures;

	private String								mCacheDir;		// ����Ŀ¼

	// private Map<String, SoftReference<Bitmap>> map=null;//����

	public ImageHelper(Context context) {
		if (mCaches == null)
		{
			// lrc���ռ�õ�Ӧ�õ��ڴ�
			int maxSize = (int) (Runtime.getRuntime().freeMemory() / 4);
			// ʵ�������������󣬻�ȡ��������С
			mCaches = new LruCache<String, Bitmap>(maxSize) {
				@Override
				protected int sizeOf(String key, Bitmap value)
				{
					// bitmap�Ĵ�С
					return value.getByteCount();
				}
			};
		}

		mCacheDir = getCacheDir(context);

		// ��ʼ��handler
		if (mHandler == null)
		{
			mHandler = new ImageHandler();
		}

		if (mPool == null)
		{
			mPool = Executors.newFixedThreadPool(3);
		}

		if (mFutures == null)
		{
			mFutures = new LinkedHashMap<ImageView, Future<?>>();
		}
	}

	/**
	 * չʾͼƬ����
	 * 
	 * @param view
	 * @param url
	 */
	public void display(ImageView view, String url)
	{
		// 1.���ڴ���ȡͼƬ
		Bitmap bitmap = mCaches.get(url);
		if (bitmap != null)
		{
			// �ڴ�����
			view.setImageBitmap(bitmap);
			return;
		}

		// �ڴ���û��
		// 2.������ȡ����
		bitmap = getBitmapFromLocal(url);
		if (bitmap != null)
		{
			// ������
			view.setImageBitmap(bitmap);
			return;
		}

		// ����Ҳû��
		// 3.�������л�ȡ����
		getBitmapFromNet(view, url);
	}

	/**
	 * �������ȡͼƬ���ݣ����첽�������ÿ����߳�
	 * 
	 * @param view
	 * @param url
	 */
	private void getBitmapFromNet(ImageView view, String url)
	{
		// ȥ�����ȡ����
		// new Thread(new ImageRequestTask(view, url)).start();

		// ʹ�����̳߳ؽ��й���
		// mPool.execute(new ImageRequestTask(view, url));//��������а�ȫ���������ܻ�ͼƬ˳���λ

		// �ж��Ƿ����Ѿ���ִ�е�
		Future<?> future = mFutures.get(view);
		if (future != null && !future.isDone() && !future.isCancelled())
		{
			// ����ִ��
			future.cancel(true);
			future = null;
		}

		future = mPool.submit(new ImageRequestTask(view, url));
		// ��future��������key-value key:ImageView,value:future
		mFutures.put(view, future);
	}

	/**
	 * �ӱ��ػ�ȡͼƬ����
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getBitmapFromLocal(String url)
	{
		String name;
		try
		{
			name = MD5Encoder.encode(url);
			File file = new File(mCacheDir, name);

			if (file.exists())
			{
				// ���ļ�������bitmap
				Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

				// �浽�ڴ�
				mCaches.put(url, bitmap);

				return bitmap;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ��ȡ����Ŀ¼
	 * 
	 * @param context
	 * @return
	 */
	private String getCacheDir(Context context)
	{
		// �����sdcard����Android/data/appname

		if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState())
		{
			// ����sdcard
			File sdDir = Environment.getExternalStorageDirectory();
			File dir = new File(sdDir, "/Android/data/" + context.getPackageName() + "/bitmap/");

			if (!dir.exists())
			{
				dir.mkdirs();
			}

			return dir.getAbsolutePath();
		}
		else
		{
			return context.getCacheDir().getAbsolutePath();
		}
	}

	/**
	 * ͼƬ���������࣬ʵ��Runnable�ӿ�
	 */
	class ImageRequestTask implements Runnable
	{

		private String		url;
		private ImageView	view;

		public ImageRequestTask(ImageView view, String url) {
			this.view = view;
			this.url = url;
		}

		@Override
		public void run()
		{
			// ȥ�����ȡͼƬ����
			try
			{
				// URL newUrl = new URL(url + "");
				// HttpURLConnection conn = (HttpURLConnection)
				// newUrl.openConnection();
				// �Ż�֮��
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setConnectTimeout(5000);// �������ӳ�ʱ
				conn.setReadTimeout(5000);// ���ö�ȡ��ʱ
				conn.setRequestMethod("GET");// �������ӷ�ʽ
				conn.connect();// ��������
				int responseCode = conn.getResponseCode();// ��ȡ��Ӧ��
				if (responseCode == 200)
				{
					// ��������ɹ�
					// ��ȡ��������������bitmap
					InputStream is = conn.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(is);

					// �浽����
					writeToLocal(bitmap, url);

					// �浽�ڴ�
					mCaches.put(url, bitmap);

					// ���߳�����ͼƬ��ʾ
					// view.setImageBitmap(bitmap);//�����ԣ����̲߳���ˢ��UI
					Message msg = Message.obtain();
					msg.obj = new Object[] { view, url };
					mHandler.sendMessage(msg);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	class ImageHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			Object[] objs = (Object[]) msg.obj;

			ImageView view = (ImageView) objs[0];
			String url = (String) objs[1];

			// ����UI��ʾ
			display(view, url);
		}
	}

	/**
	 * ���������ȡ��ͼƬ ���ݴ浽����
	 * 
	 * @param bitmap
	 * @param url
	 */
	private void writeToLocal(Bitmap bitmap, String url)
	{
		FileOutputStream fos = null;
		try
		{
			String name = MD5Encoder.encode(url);
			File file = new File(mCacheDir, name);
			fos = new FileOutputStream(file);

			// format:ѹ����ʽ;quality:ѹ������;stream:ѹ����
			bitmap.compress(CompressFormat.JPEG, 100, fos);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
					fos = null;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
