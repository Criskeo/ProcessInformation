package com.criskeo.android.sample;

//Model类
public class ProcessInfo {

	private int pid; // 进程id Android规定android.system.uid=1000
	private int uid; // 进程所在的用户id ，即该进程是有谁启动的 root/普通用户等
	private int memSize; // 进程占用的内存大小,单位为kb
	private String processName; // 进程名
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
