package com.criskeo.android.sample;

import com.criskeo.android.sample.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static String TAG = "MainActivity";
	private ActivityManager mActivityManager = null;
	private TextView tvTotalMem;
	private TextView tvAvailMem;
	private Button btProcessInfo;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tvTotalMem = (TextView) findViewById(R.id.tvTotalMemory);
		tvAvailMem = (TextView) findViewById(R.id.tvAvailMemory);
		btProcessInfo = (Button) findViewById(R.id.btProcessInfo);
		// tvBatteryInfo = (TextView) findViewById(R.id.tvBatteryInfo);
		// 跳转到显示进程信息界面
		btProcessInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, ProcessInfoActivity.class);
				startActivity(intent);
			}
		});

		// 获得ActivityManager服务的对象
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		// 获取全部内存信息
		String totalMemStr = getSystemTotalMemorySize();
		Log.i(TAG, "The Totall Memory Size is" + totalMemStr);
		tvTotalMem.setText(totalMemStr);
		// 获得可用内存信息
		String availMemStr = getSystemAvaialbeMemorySize();
		Log.i(TAG, "The Availabel Memory Size is" + availMemStr);
		tvAvailMem.setText(availMemStr);
	}

	private String getSystemTotalMemorySize() {
		// TODO Auto-generated method stub
		MemoryInfo memoryInfo = new MemoryInfo();
		// 获得系统全部内存，保存在MemoryInfo对象上
		mActivityManager.getMemoryInfo(memoryInfo);
		long totalMemSize = memoryInfo.totalMem;
		// 字符类型转换
		String totalMemStr = formateFileSize(totalMemSize);
		return totalMemStr;

	}

	// 获得系统可用内存信息
	private String getSystemAvaialbeMemorySize() {
		// 获得MemoryInfo对象
		MemoryInfo memoryInfo = new MemoryInfo();
		// 获得系统可用内存，保存在MemoryInfo对象上
		mActivityManager.getMemoryInfo(memoryInfo);
		long availMemSize = memoryInfo.availMem;
		// 字符类型转换
		String availMemStr = formateFileSize(availMemSize);
		return availMemStr;
	}

	// 调用系统函数，字符串转换 long -String KB/MB
	private String formateFileSize(long size) {
		return Formatter.formatFileSize(MainActivity.this, size);
	}

}