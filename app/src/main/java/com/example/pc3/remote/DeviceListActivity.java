package com.example.pc3.remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

// 단말기 검색 액티비티
public class DeviceListActivity extends Activity
implements AdapterView.OnItemClickListener {
	public static String EXTRA_DEVICE_ADDRESS="device_address";
	
	private BluetoothAdapter btAdapter; // BT 어댑터
	private ArrayAdapter<String> devices; // 디바이스군
	
	// 어플리케이션 생성 시 불린다.
	@Override
		protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setResult(Activity.RESULT_CANCELED);
		
		// 레이아웃의 생성
		LinearLayout layout=new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);
		
		// 디바이스
		devices=new ArrayAdapter<String>(this,R.layout.rowdata);
		
		// 리스트 뷰의 생성
		ListView listView=new ListView(this);
		setLLParams(listView);
		listView.setAdapter(devices);
		layout.addView(listView);
		listView.setOnItemClickListener(this);
		
		// 브로드캐스트 리시버의 추가
		IntentFilter filter;
		filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver,filter);
		filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver,filter);
		
		// Bluetooth 단말기의 검색 시작 (2)
		btAdapter=BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices=btAdapter.getBondedDevices();
		if (pairedDevices.size()>0) {
			for (BluetoothDevice device:pairedDevices) {
				devices.add(device.getName()+
					System.getProperty("line.separator")+
					device.getAddress());
			}
		}
		if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}
	
	// 어플리케이션 파괴 시 불린다.
	@Override
		protected void onDestroy() {
		super.onDestroy();
		if (btAdapter!=null) btAdapter.cancelDiscovery();
		this.unregisterReceiver(receiver);
	}
	
	// 클릭 시 불린다.
	public void onItemClick(AdapterView<?>av,View v,int arg2,long arg3) {
		// Bluetooth 단말기의 검색 취소
		btAdapter.cancelDiscovery();
		
		// 반환값의 지정
		String info =((TextView) v).getText().toString();
		String address=info.substring(info.length()-17);
		Intent intent =new Intent();
		intent.putExtra(EXTRA_DEVICE_ADDRESS,address);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
	
	// 리니어 레이아웃의 파라미터 지정
	private static void setLLParams(View view) {
		view.setLayoutParams(new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT));
	}
	
	// 브로드캐스트 리시버
	private final BroadcastReceiver receiver=new BroadcastReceiver() {
		// Bluetooth 단말기의 검색 결과 취득 (3)
		@Override
			public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			
			// Bluetooth 단말기 발견
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device=intent.
					getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState()!=BluetoothDevice.BOND_BONDED) {
					devices.add(device.getName()+
						System.getProperty("line.separator")+
						device.getAddress());
				}
			}
			// Bluetooth 단말기 검색 완료
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				android.util.Log.e("","Bluetooth ");
			}
		}
	};
}