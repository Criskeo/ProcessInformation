package com.criskeo.android.sample;

import java.util.List;

import com.criskeo.android.sample.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

//自定义适配器类，提供给listView的自定义view
public class ProcessInfoAdapter extends BaseAdapter {

	private List<ProcessInfo> mlistProcessInfo = null;

	LayoutInflater infater = null;

	public ProcessInfoAdapter(Context context, List<ProcessInfo> apps) {
		infater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mlistProcessInfo = apps;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		System.out.println("size" + mlistProcessInfo.size());
		return mlistProcessInfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mlistProcessInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		System.out.println("getView at " + position);
		View view = null;
		ViewHolder holder = null;
		if (convertview == null || convertview.getTag() == null) {
			view = infater.inflate(R.layout.browse_process_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertview;
			holder = (ViewHolder) convertview.getTag();
		}
		ProcessInfo processInfo = (ProcessInfo) getItem(position);
		holder.tvPID.setText(processInfo.getPid() + "");
		holder.tvUID.setText(processInfo.getUid() + "");
		holder.tvProcessMemSize.setText(processInfo.getMemSize() + "KB");
		holder.tvProcessName.setText(processInfo.getProcessName());
		holder.tvRx.setText(processInfo.getRx() / 1024 + "KB");
		holder.tvTx.setText(processInfo.getTx() / 1024 + "KB");

		return view;
	}

	class ViewHolder {
		TextView tvPID; // 进程ID
		TextView tvUID; // 用户ID
		TextView tvProcessMemSize; // 进程占用内存大小
		TextView tvProcessName; // 进程名
		TextView tvRx;
		TextView tvTx;

		public ViewHolder(View view) {
			this.tvPID = (TextView) view.findViewById(R.id.tvProcessPID);
			this.tvUID = (TextView) view.findViewById(R.id.tvProcessUID);
			this.tvProcessMemSize = (TextView) view
					.findViewById(R.id.tvProcessMemSize);
			this.tvProcessName = (TextView) view
					.findViewById(R.id.tvProcessName);
			this.tvRx = (TextView) view.findViewById(R.id.tvRx);
			this.tvTx = (TextView) view.findViewById(R.id.tvTx);
		}
	}

}