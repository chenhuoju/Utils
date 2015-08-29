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
 * @包名:com.itheima.smartbeijing.utils
 * @类名:ImageHelper
 * @作者:陈火炬
 * @时间:2015-8-12 下午6:06:10
 * 
 * @描述:图片缓存加载--->三级缓存机制
 * 
 * @SVN版本号:$Rev: 37 $
 * @更新人:$Author: chj $
 * @更新描述:TODO
 * 
 */
public class ImageHelper
{
	private static LruCache<String, Bitmap>		mCaches	= null; // 三级缓存
	private ImageHandler						mHandler;
	private static ExecutorService				mPool;			// 线程池
	private static Map<ImageView, Future<?>>	mFutures;

	private String								mCacheDir;		// 缓存目录

	// private Map<String, SoftReference<Bitmap>> map=null;//软缓存

	public ImageHelper(Context context) {
		if (mCaches == null)
		{
			// lrc最大占用的应用的内存
			int maxSize = (int) (Runtime.getRuntime().freeMemory() / 4);
			// 实例化缓存区对象，获取缓存区大小
			mCaches = new LruCache<String, Bitmap>(maxSize) {
				@Override
				protected int sizeOf(String key, Bitmap value)
				{
					// bitmap的大小
					return value.getByteCount();
				}
			};
		}

		mCacheDir = getCacheDir(context);

		// 初始化handler
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
	 * 展示图片方法
	 * 
	 * @param view
	 * @param url
	 */
	public void display(ImageView view, String url)
	{
		// 1.到内存中取图片
		Bitmap bitmap = mCaches.get(url);
		if (bitmap != null)
		{
			// 内存中有
			view.setImageBitmap(bitmap);
			return;
		}

		// 内存中没有
		// 2.到本地取数据
		bitmap = getBitmapFromLocal(url);
		if (bitmap != null)
		{
			// 本地有
			view.setImageBitmap(bitmap);
			return;
		}

		// 本地也没有
		// 3.到网络中获取数据
		getBitmapFromNet(view, url);
	}

	/**
	 * 从网络获取图片数据，是异步操作，得开启线程
	 * 
	 * @param view
	 * @param url
	 */
	private void getBitmapFromNet(ImageView view, String url)
	{
		// 去网络获取数据
		// new Thread(new ImageRequestTask(view, url)).start();

		// 使用了线程池进行管理
		// mPool.execute(new ImageRequestTask(view, url));//这个方法有安全隐患，可能会图片顺序错位

		// 判断是否有已经在执行的
		Future<?> future = mFutures.get(view);
		if (future != null && !future.isDone() && !future.isCancelled())
		{
			// 正在执行
			future.cancel(true);
			future = null;
		}

		future = mPool.submit(new ImageRequestTask(view, url));
		// 将future存起来，key-value key:ImageView,value:future
		mFutures.put(view, future);
	}

	/**
	 * 从本地获取图片数据
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
				// 把文件解析成bitmap
				Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

				// 存到内存
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
	 * 获取缓存目录
	 * 
	 * @param context
	 * @return
	 */
	private String getCacheDir(Context context)
	{
		// 如果有sdcard就用Android/data/appname

		if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState())
		{
			// 存在sdcard
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
	 * 图片请求任务类，实现Runnable接口
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
			// 去网络获取图片数据
			try
			{
				// URL newUrl = new URL(url + "");
				// HttpURLConnection conn = (HttpURLConnection)
				// newUrl.openConnection();
				// 优化之后
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setConnectTimeout(5000);// 设置连接超时
				conn.setReadTimeout(5000);// 设置读取超时
				conn.setRequestMethod("GET");// 设置连接方式
				conn.connect();// 开启连接
				int responseCode = conn.getResponseCode();// 获取响应码
				if (responseCode == 200)
				{
					// 访问网络成功
					// 获取流，将流解析成bitmap
					InputStream is = conn.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(is);

					// 存到本地
					writeToLocal(bitmap, url);

					// 存到内存
					mCaches.put(url, bitmap);

					// 主线程中让图片显示
					// view.setImageBitmap(bitmap);//不可以，子线程不能刷新UI
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

			// 设置UI显示
			display(view, url);
		}
	}

	/**
	 * 将从网络获取的图片 数据存到本地
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

			// format:压缩格式;quality:压缩质量;stream:压缩流
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
