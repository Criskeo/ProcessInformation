package com.criskeo.android.sample;

//Model��
public class ProcessInfo {

	private int pid; // ����id Android�涨android.system.uid=1000
	private int uid; // �������ڵ��û�id �����ý�������˭������ root/��ͨ�û���
	private int memSize; // ����ռ�õ��ڴ��С,��λΪkb
	private String processName; // ������
	private long rx;//
	private long tx;

	public ProcessInfo() {
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getMemSize() {
		return memSize;
	}

	public void setMemSize(int memSize) {
		this.memSize = memSize;
	}

	public String getProcessName() {
		return processName;
	}

	public void setPocessName(String processName) {
		this.processName = processName;
	}

	public void setRx(long rx) {
		this.rx = rx;
	}

	public long getRx() {
		return rx;
	}

	public void setTx(long tx) {
		this.tx = tx;
	}

	public long getTx() {
		return tx;
	}

}
