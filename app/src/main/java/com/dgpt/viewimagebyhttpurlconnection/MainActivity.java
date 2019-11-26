package com.dgpt.viewimagebyhttpurlconnection;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final int CHANGE_UI = 1;
	protected static final int ERROR = 2;
	private EditText et_path;
	private ImageView iv;
	// 主线程创建消息处理器
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == CHANGE_UI){
				Bitmap bitmap = (Bitmap) msg.obj;
				iv.setImageBitmap(bitmap);
			}else if(msg.what == ERROR){
				Toast.makeText(MainActivity.this, "显示图片错误", Toast.LENGTH_SHORT).show();
			}
		};
	};
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		et_path = (EditText) findViewById(R.id.et_path);
		iv = (ImageView) findViewById(R.id.iv);
	}
	public void click(View view) {
		final String path = et_path.getText().toString().trim();
		if (TextUtils.isEmpty(path)) {
			Toast.makeText(this, "图片路径不能为空", Toast.LENGTH_SHORT).show();
		} else {
			//new NetCacheUtils().getBitmapFromNet(iv, path);

			//子线程请求网络,Android4.0以后访问网络不能放在主线程中
			new Thread() {
				public void run() {
					// 连接服务器 get 请求 获取图片.
					getImageByURLConnect(path);
				};
			}.start();
		}
	}

	private void getImageByURLConnect(String path) {
		try {
			URL url = new URL(path);       //创建URL对象
			// 根据url 发送 http的请求.
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 设置请求的方式
			conn.setRequestMethod("GET");
			//设置超时时间
			conn.setConnectTimeout(5000);
			// 得到服务器返回的响应码
			int code = conn.getResponseCode();
			//请求网络成功后返回码是200
			Log.i("MainActivty", code+"");
			if (code == 200) {
				//获取输入流
				InputStream is = conn.getInputStream();
				//将流转换成Bitmap对象
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				//iv.setImageBitmap(bitmap);

							/*handler.post(new Runnable() {
								@Override
								public void run() {
									iv.setImageBitmap(bitmap);
								}
							});*/

				//TODO: 告诉主线程一个消息:帮我更改界面。内容:bitmap
				Message msg = new Message();
				msg.what = CHANGE_UI;
				msg.obj = bitmap;
				handler.sendMessage(msg);
			} else {
				//返回码不是200  请求服务器失败
				Message msg = new Message();
				msg.what = ERROR;
				handler.sendMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = new Message();
			msg.what = ERROR;
			handler.sendMessage(msg);
		}
	}
}