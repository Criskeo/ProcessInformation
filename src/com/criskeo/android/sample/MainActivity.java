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
		// ��ת����ʾ������Ϣ����
		btProcessInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						ProcessInfoActivity.class);
				startActivity(intent);
			}
		});

		// ���ActivityManager����Ķ���
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		// ��ȡȫ���ڴ���Ϣ
		String totalMemStr = getSystemTotalMemorySize();
		Log.i(TAG, "The Totall Memory Size is" + totalMemStr);
		// ��ʾ
		tvTotalMem.setText(totalMemStr);
		// ��ÿ����ڴ���Ϣ
		String availMemStr = getSystemAvaialbeMemorySize();
		Log.i(TAG, "The Availabel Memory Size is" + availMemStr);
		// ��ʾ
		tvAvailMem.setText(availMemStr);
	}

	private String getSystemTotalMemorySize() {
		// TODO Auto-generated method stub
		MemoryInfo memoryInfo = new MemoryInfo();
		// ���ϵͳȫ���ڴ棬������MemoryInfo������
		mActivityManager.getMemoryInfo(memoryInfo);
		long totalMemSize = memoryInfo.totalMem;
		// �ַ�����ת��
		String totalMemStr = formateFileSize(totalMemSize);
		return totalMemStr;

	}

	// ���ϵͳ�����ڴ���Ϣ
	private String getSystemAvaialbeMemorySize() {
		// ���MemoryInfo����
		MemoryInfo memoryInfo = new MemoryInfo();
		// ���ϵͳ�����ڴ棬������MemoryInfo������
		mActivityManager.getMemoryInfo(memoryInfo);
		long availMemSize = memoryInfo.availMem;
		// �ַ�����ת��
		String availMemStr = formateFileSize(availMemSize);
		return availMemStr;
	}

	// ����ϵͳ�������ַ���ת�� long -String KB/MB
	private String formateFileSize(long size) {
		return Formatter.formatFileSize(MainActivity.this, size);
	}

}