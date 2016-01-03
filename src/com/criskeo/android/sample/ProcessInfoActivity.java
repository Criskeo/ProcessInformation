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
	// ProcessInfo Model�� �����������н�����Ϣ
	private List<ProcessInfo> processInfoList = null;

	private Button btnStart;// ��ʼ��ť
	private Button btnStop;// ������ť
	private ListView listviewProcess;// �����б�
	private TextView tvTotalProcessNo;// ���еĳ�������
	private TextView tvBatteryInfo;// �����Ϣ
	private EditText filterEditText;// ������Ϣ
	private int BatteryN; // Ŀǰ����
	private int BatteryV; // ��ص�ѹ
	private double BatteryT; // ����¶�
	private String BatteryStatus; // ���״̬
	private String BatteryTemp; // ���ʹ�����
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

		// ע��һ��ϵͳ BroadcastReceiver����Ϊ���ʵ�ؼ���֮���������ֱ����AndroidManifest.xml��ע��
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// �����������ת��Ϊ�ַ���
				filterStr = filterEditText.getText().toString();

				handler.postDelayed(runnable, 3000);
				Toast.makeText(ProcessInfoActivity.this, "��ʼ��¼!", Toast.LENGTH_SHORT).show();
				btnStart.setText("���ڼ�¼");
			}
		});

		// ��ֹͣ��ťʱ��ȡ��ǰʱ�� ����sdcard/ProcessInfomation���Ե�ǰʱ��Ϊ�ļ���д�������Ϣ
		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// ���ڴ濨���½��ļ���ProcessInfomation
				try {
					//String baseDir = ;
					String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FILEPATH + File.separator;
					File dir = new File(filePath);
					dir.mkdirs();

					// д�ļ�
					File fileName = new File(filePath + getCurrentTime() + ".txt");
					String fileHeadStr = "ʱ��" + "\t" + "������" + "\t" + "CPU" + "\t" + "PID" + "\t" + "UID" + "\t" + "ռ���ڴ��С" + "\t" + "���ݽ�����" + "\t"
							+ "���ݷ�����" + "\t" + "����" + "\t" + "���ʹ��״̬" + "\t" + "��ѹ" + "\t" + "�¶�" + "\r\n";
					subFileStr = fileHeadStr + subStr;
					FileOutputStream fos = new FileOutputStream(fileName);
					fos.write(subFileStr.getBytes());
					// д��֮������ַ���
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

				Toast.makeText(ProcessInfoActivity.this, "ֹͣ��¼!", Toast.LENGTH_SHORT).show();
				btnStart.setText("��ʼ��¼");
			}
		});

		// ���ActivityManager����Ķ���
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		// ���ϵͳ������Ϣ
		getRunningAppProcessInfo();
		// ΪListView��������������

		ProcessInfoAdapter mprocessInfoAdapter = new ProcessInfoAdapter(this, processInfoList);
		listviewProcess.setAdapter(mprocessInfoAdapter);

		tvTotalProcessNo.setText("��ǰ���̹��У�" + processInfoList.size() + "��");

	}

	// ��ȡϵͳʱ��,����yyyyMMdd_HHmmss��ʽ��time�ַ���
	public static String getCurrentTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String time = formatter.format(curDate);
		return time;
	}

	// ��ȡ�����Ϣ
	/* �����㲥������ */
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			/*
			 * �����׽����action��ACTION_BATTERY_CHANGED�� ������onBatteryInfoReceiver()
			 */
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				BatteryN = intent.getIntExtra("level", 0); // Ŀǰ����
				BatteryV = intent.getIntExtra("voltage", 0); // ��ص�ѹ
				BatteryT = intent.getIntExtra("temperature", 0); // ����¶�

				switch (intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)) {
				case BatteryManager.BATTERY_STATUS_CHARGING:
					BatteryStatus = "�����";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					BatteryStatus = "�ŵ���";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					BatteryStatus = "δ���";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					BatteryStatus = "������";
					break;
				case BatteryManager.BATTERY_STATUS_UNKNOWN:
					BatteryStatus = "δ֪״̬";
					break;
				}

				switch (intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
				case BatteryManager.BATTERY_HEALTH_UNKNOWN:
					BatteryTemp = "δ֪����";
					break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
					BatteryTemp = "״̬����";
					break;
				case BatteryManager.BATTERY_HEALTH_DEAD:
					BatteryTemp = "���û�е�";
					break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
					BatteryTemp = "��ص�ѹ����";
					break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
					BatteryTemp = "��ع���";
					break;
				}
				// BatteryTemp + "\t"
				tvBatteryInfo.setText(BatteryN + "%" + "\t" + BatteryStatus + "\t" + BatteryV + "mV" + "\t" +(BatteryT * 0.1) + "��");
			}
		}
	};

	// �����ַ���
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
					// �����ַ���
					String recordStr = time + "\t" + newProcessName + "\t" + cpuRateStr + "%" + "\t" + newProcessPid + "\t" + newProcessUid + "\t"
							+ newProcessMemSize + "KB" + "\t" + rxResultStr + "KB" + "\t" + txResultStr + "KB" + "\t";
					batteryInfoStr = tvBatteryInfo.getText().toString() + "\r\n";
					// д����Ϣ �ܵ��ַ���Ϊ ÿ����¼����ʵʱ�ĵ����Ϣ
					subStr = subStr + recordStr + batteryInfoStr;
					count++;

				}
				else
					;
			}
			if (count != 0) {

				// Toast.makeText(BrowseProcessInfoActivity.this, "���ڼ�¼...",
				// Toast.LENGTH_SHORT).show();
			}
			else if (count == 0) {
				// ˵��û���ҵ���Ӧ��Ӧ�ó���
				Toast.makeText(ProcessInfoActivity.this, "δ�ҵ���Ӧ��Ӧ�ó���.", Toast.LENGTH_SHORT).show();
			}
			;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return subStr;
	}

	// ��ȡϵͳ��CPUʹ��ʱ��
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

	// ���ϵͳ������Ϣ
	private void getRunningAppProcessInfo() {

		// ProcessInfo Model�� �����������н�����Ϣ
		processInfoList = new ArrayList<ProcessInfo>();

		// ͨ������ActivityManager��getRunningAppProcesses()�������ϵͳ�������������еĽ���
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			// ����ID��
			int pid = appProcessInfo.pid;
			// �û�ID ������Linux��Ȩ�޲�ͬ��IDҲ�Ͳ�ͬ ���� root��
			int uid = appProcessInfo.uid;
			// ��������Ĭ���ǰ�������������android��process=""ָ��
			String processName = appProcessInfo.processName;
			// ��øý���ռ�õ��ڴ�
			int[] myMempid = new int[] { pid };
			// ��MemoryInfoλ��android.os.Debug.MemoryInfo���У�����ͳ�ƽ��̵��ڴ���Ϣ
			Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(myMempid);
			// ��ȡ����ռ�ڴ�����Ϣ kb��λ
			// dalvikPrivateDirty
			int memSize = memoryInfo[0].getTotalPss();
			long rx = TrafficStats.getUidRxBytes(uid);
			long tx = TrafficStats.getUidTxBytes(uid);
			// Log.i(TAG, "processName: " + processName + "  pid: " + pid
			// + " uid:" + uid + " memorySize is -->" + memSize + "KB");
			// ����һ��ProcessInfo����
			ProcessInfo processInfo = new ProcessInfo();
			processInfo.setPid(pid);
			processInfo.setUid(uid);
			processInfo.setMemSize(memSize);
			processInfo.setPocessName(processName);
			processInfo.setRx(rx);
			processInfo.setTx(tx);
			processInfoList.add(processInfo);

			// ���ÿ�����������е�Ӧ�ó���(��),��ÿ��Ӧ�ó���İ���
			// �����ڸý����µ�����Ӧ�ó������
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
