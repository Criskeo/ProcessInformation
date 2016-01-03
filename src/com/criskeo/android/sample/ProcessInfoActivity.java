package com.criskeo.android.sample;

import java.util.ArrayList;
import java.util.List;

import com.criskeo.android.sample.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class ProcessInfoActivity extends Activity {

	private static String TAG = "ProcessInfoActivity";

	private ActivityManager mActivityManager = null;
	// ProcessInfo Model类 用来保存所有进程信息
	private List<ProcessInfo> processInfoList = null;

	private Button btnStart;// 开始按钮
	private Button btnStop;// 结束按钮
	private ListView listviewProcess;// 进程列表
	private TextView tvTotalProcessNo;// 运行的程序总数
	private TextView tvBatteryInfo;// 电池信息
	private EditText filterEditText;// 输入信息
	private int BatteryN; // 目前电量
	private int BatteryV; // 电池电压
	private double BatteryT; // 电池温度
	private String BatteryStatus; // 电池状态
	private String BatteryTemp; // 电池使用情况
	private String FILEPATH = "ProcessInfoLog";
	private String FILENAME = "";

	String filterStr = null;
	String subStr = "";
	String subFileStr = "";
	String batteryInfoStr;
	long rxResult;
	long txResult;
	String rxResultStr = null;
	String txResultStr = null;

	List<Long> rxList = new ArrayList<Long>();
	List<Long> txList = new ArrayList<Long>();
	List<Long> rxResultList = new ArrayList<Long>();
	List<Long> txResultList = new ArrayList<Long>();
	int i = 0;
	int count = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_process_list);

		btnStart = (Button) findViewById(R.id.btnStart);
		btnStop = (Button) findViewById(R.id.btnStop);
		listviewProcess = (ListView) findViewById(R.id.listviewProcess);
		tvTotalProcessNo = (TextView) findViewById(R.id.tvTotalProcessNo);
		filterEditText = (EditText) findViewById(R.id.filterEditText);
		tvBatteryInfo = (TextView) findViewById(R.id.tvBatteryInfo);

		// 注册一个系统 BroadcastReceiver，作为访问电池计量之用这个不能直接在AndroidManifest.xml中注册
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 将输入的文字转化为字符串
				filterStr = filterEditText.getText().toString();

				handler.postDelayed(runnable, 3000);
				Toast.makeText(ProcessInfoActivity.this, "开始记录!", Toast.LENGTH_SHORT).show();
				btnStart.setText("正在记录");
			}
		});

		// 按停止按钮时获取当前时间 并在sdcard/ProcessInfomation中以当前时间为文件名写入相关信息
		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// 在内存卡上新建文件夹ProcessInfomation
				try {
					//String baseDir = ;
					String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FILEPATH + File.separator;
					File dir = new File(filePath);
					dir.mkdirs();

					// 写文件
					File fileName = new File(filePath + getCurrentTime() + ".txt");
					String fileHeadStr = "时间" + "\t" + "进程名" + "\t" + "CPU" + "\t" + "PID" + "\t" + "UID" + "\t" + "占用内存大小" + "\t" + "数据接收量" + "\t"
							+ "数据发送量" + "\t" + "电量" + "\t" + "电池使用状态" + "\t" + "电压" + "\t" + "温度" + "\r\n";
					subFileStr = fileHeadStr + subStr;
					FileOutputStream fos = new FileOutputStream(fileName);
					fos.write(subFileStr.getBytes());
					// 写完之后清空字符串
					subFileStr = "";
					subStr = "";
					fos.flush();
					fos.close();
				}

				catch (Exception e) {
					e.printStackTrace();
				}

				handler.removeCallbacks(runnable);

				rxList = new ArrayList<Long>();
				txList = new ArrayList<Long>();
				i = 0;
				count = 0;
				// rxList.clear();
				// txList.clear();

				rxResultList.clear();
				txResultList.clear();
				// rxResultStr="";
				// txResultStr="";

				Toast.makeText(ProcessInfoActivity.this, "停止记录!", Toast.LENGTH_SHORT).show();
				btnStart.setText("开始记录");
			}
		});

		// 获得ActivityManager服务的对象
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		// 获得系统进程信息
		getRunningAppProcessInfo();
		// 为ListView构建适配器对象

		ProcessInfoAdapter mprocessInfoAdapter = new ProcessInfoAdapter(this, processInfoList);
		listviewProcess.setAdapter(mprocessInfoAdapter);

		tvTotalProcessNo.setText("当前进程共有：" + processInfoList.size() + "个");

	}

	// 获取系统时间,返回yyyyMMdd_HHmmss格式的time字符串
	public static String getCurrentTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String time = formatter.format(curDate);
		return time;
	}

	// 获取电池信息
	/* 创建广播接收器 */
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			/*
			 * 如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()
			 */
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				BatteryN = intent.getIntExtra("level", 0); // 目前电量
				BatteryV = intent.getIntExtra("voltage", 0); // 电池电压
				BatteryT = intent.getIntExtra("temperature", 0); // 电池温度

				switch (intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)) {
				case BatteryManager.BATTERY_STATUS_CHARGING:
					BatteryStatus = "充电中";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					BatteryStatus = "放电中";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					BatteryStatus = "未充电";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					BatteryStatus = "充满电";
					break;
				case BatteryManager.BATTERY_STATUS_UNKNOWN:
					BatteryStatus = "未知状态";
					break;
				}

				switch (intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
				case BatteryManager.BATTERY_HEALTH_UNKNOWN:
					BatteryTemp = "未知错误";
					break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
					BatteryTemp = "状态良好";
					break;
				case BatteryManager.BATTERY_HEALTH_DEAD:
					BatteryTemp = "电池没有电";
					break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
					BatteryTemp = "电池电压过高";
					break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
					BatteryTemp = "电池过热";
					break;
				}
				// BatteryTemp + "\t"
				tvBatteryInfo.setText(BatteryN + "%" + "\t" + BatteryStatus + "\t" + BatteryV + "mV" + "\t" +(BatteryT * 0.1) + "℃");
			}
		}
	};

	// 整合字符串
	public String record() {
		try {
			String time = getCurrentTime();
			String newProcessName = null;
			String newProcessPid = null;
			String newProcessUid = null;
			String newProcessMemSize = null;
			String newProcessRx = null;
			String newProcessTx = null;
			String cpuRateStr = null;

			processInfoList = new ArrayList<ProcessInfo>();
			List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

			for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
				int pid = appProcessInfo.pid;
				int uid = appProcessInfo.uid;
				String processName = appProcessInfo.processName;
				int[] myMempid = new int[] { pid };
				Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(myMempid);
				int memSize = memoryInfo[0].getTotalPss();

				long rx = TrafficStats.getUidRxBytes(uid);
				long tx = TrafficStats.getUidTxBytes(uid);

				ProcessInfo newProcessInfo = new ProcessInfo();
				newProcessInfo.setPid(pid);
				newProcessInfo.setUid(uid);
				newProcessInfo.setMemSize(memSize);
				newProcessInfo.setPocessName(processName);
				newProcessInfo.setRx(rx);
				newProcessInfo.setTx(tx);

				newProcessName = newProcessInfo.getProcessName();
				newProcessPid = newProcessInfo.getPid() + "";
				newProcessUid = newProcessInfo.getUid() + "";
				newProcessMemSize = newProcessInfo.getMemSize() + "";
				newProcessRx = newProcessInfo.getRx() / 1024 + "";
				newProcessTx = newProcessInfo.getTx() / 1024 + "";

				if (newProcessName.contains(filterStr)) {
					float CpuRate;
					float totalCpuTime1 = getTotalCpuTime();
					long processCpuTime1;
					String[] cpuInfos1 = null;
					BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/stat")), 1000);
					String load1 = reader1.readLine();
					reader1.close();
					cpuInfos1 = load1.split(" ");
					processCpuTime1 = Long.parseLong(cpuInfos1[13]) + Long.parseLong(cpuInfos1[14]) + Long.parseLong(cpuInfos1[15])
							+ Long.parseLong(cpuInfos1[16]);

					Thread.sleep(1000);

					float totalCpuTime2 = getTotalCpuTime();
					long processCpuTime2;
					String[] cpuInfos2 = null;
					BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/stat")), 1000);
					String load2 = reader2.readLine();
					reader2.close();
					cpuInfos2 = load2.split(" ");
					processCpuTime2 = Long.parseLong(cpuInfos2[13]) + Long.parseLong(cpuInfos2[14]) + Long.parseLong(cpuInfos2[15])
							+ Long.parseLong(cpuInfos2[16]);

					CpuRate = 100 * (processCpuTime2 - processCpuTime1) / (totalCpuTime2 - totalCpuTime1);
					cpuRateStr = (int) CpuRate + "";

					rxList.add(TrafficStats.getUidRxBytes(uid));
					rxResult = rxList.get(i) - rxList.get(0);
					rxResultList.add(rxResult);
					rxResultStr = rxResultList.get(i) / 1024 + "";

					txList.add(TrafficStats.getUidTxBytes(uid));
					txResult = txList.get(i) - txList.get(0);
					txResultList.add(txResult);
					txResultStr = txResultList.get(i) / 1024 + "";

					i++;
					// 整合字符串
					String recordStr = time + "\t" + newProcessName + "\t" + cpuRateStr + "%" + "\t" + newProcessPid + "\t" + newProcessUid + "\t"
							+ newProcessMemSize + "KB" + "\t" + rxResultStr + "KB" + "\t" + txResultStr + "KB" + "\t";
					batteryInfoStr = tvBatteryInfo.getText().toString() + "\r\n";
					// 写入信息 总的字符串为 每条记录加上实时的电池信息
					subStr = subStr + recordStr + batteryInfoStr;
					count++;

				}
				else
					;
			}
			if (count != 0) {

				// Toast.makeText(BrowseProcessInfoActivity.this, "正在记录...",
				// Toast.LENGTH_SHORT).show();
			}
			else if (count == 0) {
				// 说明没有找到相应的应用程序
				Toast.makeText(ProcessInfoActivity.this, "未找到相应的应用程序.", Toast.LENGTH_SHORT).show();
			}
			;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return subStr;
	}

	// 获取系统总CPU使用时间
	public static long getTotalCpuTime() {
		String[] cpuInfos = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
			String load = reader.readLine();
			reader.close();
			cpuInfos = load.split(" ");
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		long totalCpuTime = Long.parseLong(cpuInfos[2]) + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4]) + Long.parseLong(cpuInfos[6])
				+ Long.parseLong(cpuInfos[5]) + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
		return totalCpuTime;
	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			record();
			handler.postDelayed(this, 3000);
		}
	};

	// 获得系统进程信息
	private void getRunningAppProcessInfo() {

		// ProcessInfo Model类 用来保存所有进程信息
		processInfoList = new ArrayList<ProcessInfo>();

		// 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			// 进程ID号
			int pid = appProcessInfo.pid;
			// 用户ID 类似于Linux的权限不同，ID也就不同 比如 root等
			int uid = appProcessInfo.uid;
			// 进程名，默认是包名或者由属性android：process=""指定
			String processName = appProcessInfo.processName;
			// 获得该进程占用的内存
			int[] myMempid = new int[] { pid };
			// 此MemoryInfo位于android.os.Debug.MemoryInfo包中，用来统计进程的内存信息
			Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(myMempid);
			// 获取进程占内存用信息 kb单位
			// dalvikPrivateDirty
			int memSize = memoryInfo[0].getTotalPss();
			long rx = TrafficStats.getUidRxBytes(uid);
			long tx = TrafficStats.getUidTxBytes(uid);
			// Log.i(TAG, "processName: " + processName + "  pid: " + pid
			// + " uid:" + uid + " memorySize is -->" + memSize + "KB");
			// 构造一个ProcessInfo对象
			ProcessInfo processInfo = new ProcessInfo();
			processInfo.setPid(pid);
			processInfo.setUid(uid);
			processInfo.setMemSize(memSize);
			processInfo.setPocessName(processName);
			processInfo.setRx(rx);
			processInfo.setTx(tx);
			processInfoList.add(processInfo);

			// 获得每个进程里运行的应用程序(包),即每个应用程序的包名
			// 运行在该进程下的所有应用程序包名
			String[] packageList = appProcessInfo.pkgList;
			Log.i(TAG, "process id is " + pid + "has " + packageList.length);
			for (String pkg : packageList) {
				Log.i(TAG, "packageName " + pkg + " in process id is -->" + pid);
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			handler.removeCallbacks(runnable);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
