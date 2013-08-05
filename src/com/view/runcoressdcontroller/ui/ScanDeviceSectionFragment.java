/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：蓝牙扫描清单界面Fragment
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.view.runcoressdcontroller.R;
import com.view.runcoressdcontroller.utils.ATCmdBean;
import com.view.runcoressdcontroller.utils.CommonDefine;
import com.view.runcoressdcontroller.utils.SSDCapability;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.support.v4.app.Fragment;

/**
 * 蓝牙扫描清单的fragment
 * @author allen
 */
public class ScanDeviceSectionFragment extends Fragment {
	public static final String TAG = "ScanDeviceSectionFragment";

    // 加密盘能力规格清单（从XML中读取）
	private ArrayList<SSDCapability> mDevCapabilities = null;
    
	// 蓝牙适配器
    private BluetoothAdapter mBtAdapter;
    // 蓝牙设备列表适配器
    private SimpleAdapter mScannedDevicesSimpleAdapter;
    // 蓝牙设备基本信息列表
    private List<Map<String, Object>> mDeviceMapList = new ArrayList<Map<String, Object>>(); 
    // 蓝牙设备对象列表
	private List<BluetoothDevice> mScannedDevices = new ArrayList<BluetoothDevice>();
    // 进度对话框
    private ProgressDialog mProDialog;

    // AT命令发起者信息
    String mOwner = null; 
    
    // 当前是否有设备连接中的标志（同时只能有一个设备连接）
    boolean mDevConnected = false;
    
    // 定义与宿主Activity通信的回调接口
    OnNewDeviceListener mActivityCallback;
    
    TextView mTips = null;

    /**
     * 宿主Activity需要实现该接口
     */
    public interface OnNewDeviceListener {
        public void onNewDeviceConnected(SSDDevice newDev);
    }

    /**
     * Fragment依附Activity时的回调方法
     */
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
        	mActivityCallback = (OnNewDeviceListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNewDeviceListener");
        }
    }

	/**
	 * 空构造器
	 */
	public ScanDeviceSectionFragment() {
	}

	/**
	 * Fragment创建时的回调函数
	 */	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mOwner = this.getClass().getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
	}
	
	/**
	 * Fragment创建view时的回调函数
	 */	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        
		View rootView = inflater.inflate(R.layout.fragment_device_scan,
				container, false);

		mDevCapabilities = (ArrayList<SSDCapability>) getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_DEVCAP);
		
        // 初始化蓝牙扫描的按钮
        Button scanButton = (Button) rootView.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
                doDiscovery();
                // 创建ProgressDialog对象  
                mProDialog = new ProgressDialog(getActivity());  
                mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
                mProDialog.setTitle(R.string.scan_diag_title);  
                mProDialog.setMessage(getResources().getString(R.string.scan_diag_msg));  
                mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
                mProDialog.setIndeterminate(false);
                mProDialog.setCancelable(true);  
                mProDialog.setOnCancelListener(new DiscDialogListener());
                mProDialog.show(); 
            }
        });

        ListView scannedDevicesListView = (ListView) rootView.findViewById(R.id.scanned_devices);
        scannedDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// 注册蓝牙Service广播接收器
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_SUCCESS);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_FAILED);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);
        
        // 注册系统广播接收器（用于蓝牙搜索）
        IntentFilter systemfilter = new IntentFilter();
        systemfilter.addAction(BluetoothDevice.ACTION_FOUND);
        systemfilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        
        getActivity().registerReceiver(mSystemReceiver, systemfilter);

        // 获取系统蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // 先用已配对的蓝牙设备初始化设备列表
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        Map<String, Object> map;
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
    	        map = new HashMap<String, Object>();
    	        map.put("title", device.getName());
    	        map.put("info", "蓝牙地址: " + device.getAddress());
    	        map.put("devimg", R.drawable.ok_gray);
    	        map.put("arrowimg", R.drawable.general__shared__more);
    	        
    	        mDeviceMapList.add(map);
    	        mScannedDevices.add(device);
            }
        }

        mScannedDevicesSimpleAdapter = new SimpleAdapter(getActivity(), mDeviceMapList, R.layout.device_item, 
        		new String[]{"title", "info", "devimg", "arrowimg", "flagimg"}, 
        		new int[]{R.id.title, R.id.info, R.id.devimg, R.id.arrowimg, R.id.flagimg});
        scannedDevicesListView.setAdapter(mScannedDevicesSimpleAdapter);

        mTips = (TextView) rootView.findViewById(R.id.tips_scanned_devices);
        mTips.setText(R.string.scanned_tips_init);
		return rootView;
	}
	
	/**
	 * 蓝牙扫描状态对话框的监听器
	 */
    class DiscDialogListener implements DialogInterface.OnCancelListener {  
        @Override  
        public void onCancel(DialogInterface dialog) {
            dialog.cancel(); 
	        if (mBtAdapter.isDiscovering()) {
	            mBtAdapter.cancelDiscovery();
	        }
        }  
    } 

    /**
     * 启动蓝牙发现（搜索）
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // 先清除已有的设备清单
        mDeviceMapList.clear();
        mScannedDevices.clear();
        mScannedDevicesSimpleAdapter.notifyDataSetChanged();
        
        mBtAdapter.startDiscovery();
    }
    
    /**
     * 查询设备信息
     * @param address 蓝牙设备地址
     */
    private void doGetDeviceInfo(String address) {
        Log.d(TAG, "doGetDeviceInfo()");
        
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_GET_INFO);
        atMsg.setAddress(address);
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
    }

    /**
     * 创建新的源科加密盘设备，并通过Activity通知BindedDeviceSectionFragment
     * @param atGetInfo 查询设备信息的AT响应
     */
    private void doNotifyNewDevConn(ATCmdBean atGetInfo) {
        Log.d(TAG, "doNotifyNewDevConn()");
        
        String[] devInfos = atGetInfo.getDevInfo();
        
        // 根据AT响应构造新的加密盘设备对象
        SSDDevice ssd = new SSDDevice();

        ssd.setAddress(atGetInfo.getAddress());
        ssd.setDevName(mBtAdapter.getRemoteDevice(ssd.getAddress()).getName());
        
        ssd.setVid(devInfos[0]);
        ssd.setPid(devInfos[1]);
        if (devInfos[2].equals("1")) {
            ssd.setBinded(true);
        } else {
        	ssd.setBinded(false);
        }
        if (devInfos[3].equals("1")) {
            ssd.setBreakSw(true);
        } else {
        	ssd.setBreakSw(false);
        }    
        if (devInfos[4].equals("1")) {
            ssd.setEjectSw(true);
        } else {
        	ssd.setEjectSw(false);
        } 
        ssd.setMaxRetries(Integer.parseInt(devInfos[5]));
        ssd.setDftBootDisk(Integer.parseInt(devInfos[6]));
        ssd.setConnected(true);

        for(SSDCapability devCap : mDevCapabilities) {
        	if (ssd.getPid().equals(devCap.getPid()) && ssd.getVid().equals(devCap.getVid())) {
        		ssd.setCapablity(devCap);
        		break;
        	}
        }

        if (ssd.getCapablity() != null) {
            ssd.setName(ssd.getCapablity().getName());
            ssd.setDiskNum(ssd.getCapablity().getDiskNum());
        }
        else {
            ssd.setName(CommonDefine.PRODUCT_NAME_UNKNOWN);
            ssd.setDiskNum(1);
        }
        
        // 通过Activity通知BindedDeviceSectionFragment
        mActivityCallback.onNewDeviceConnected(ssd);

        // 更新View
        for(Map<String, Object> map : mDeviceMapList) {
        	String info = (String)map.get("info");
        	if (info.indexOf(atGetInfo.getAddress()) > 0) {
        		map.remove("flagimg");
        		map.remove("devimg");
        		map.put("devimg", R.drawable.ok2);
        		break;
        	}
        }				
        
        mScannedDevicesSimpleAdapter.notifyDataSetChanged();        

		Toast.makeText(getActivity(), ssd.getDevName() + "连接成功，识别为合法的加密盘，型号：" + ssd.getName(), Toast.LENGTH_LONG).show();
    }

    /**
     * 主动断开与蓝牙设备的连接
     * @param address 蓝牙设备地址
     */
    private void doDisconnDev(String address) {
        Log.d(TAG, "doDisconnDev()");

        Intent disconnIntent = new Intent(CommonDefine.ACTION_DISCONNECT_DEVICE);
        disconnIntent.putExtra(CommonDefine.DEVICE, address);
		getActivity().sendBroadcast(disconnIntent);
    }
    
    /**
     *  蓝牙设备列表点击处理
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();
            
			// 如果当前已经有设备连接，不允许发起新连接
            if (mDevConnected) {
				Toast.makeText(getActivity(), CommonDefine.DISPINFO_CONNECTED_STATUS, Toast.LENGTH_SHORT).show();
				return;
            }
            
			TextView nameView = (TextView)v.findViewById(R.id.info);
			String info = nameView.getText().toString();
            String address = info.substring(info.length() - 17);

            // 发起该设备的蓝牙连接
            Intent selectDeviceIntent = new Intent(CommonDefine.ACTION_SELECTED_DEVICE);
			selectDeviceIntent.putExtra(CommonDefine.DEVICE, address);
			selectDeviceIntent.putExtra(CommonDefine.OWNER, mOwner);
			getActivity().sendBroadcast(selectDeviceIntent);

			nameView.setText("设备连接中......");
			mProDialog = new ProgressDialog(getActivity());  
			mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
			mProDialog.setTitle(R.string.conn_diag_title);  
			mProDialog.setMessage(getResources().getString(R.string.conn_diag_msg));  
			mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
			mProDialog.setIndeterminate(false);
			mProDialog.setCancelable(false);  
            mProDialog.show(); 
        }
    };

    /**
     *  蓝牙系统广播接收器（处理发现设备、搜索结束事件）
     */
    private final BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
	        Map<String, Object> map;

            // 发现一个蓝牙设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "find a new device " + device.getName() + ", mac " + device.getAddress());
                
                if (device.getName() != null) {
	            	// 每扫描到一个蓝牙设备，加入列表
	                map = new HashMap<String, Object>();
	    	        map.put("title", device.getName());
	    	        map.put("info", "蓝牙地址: " + device.getAddress());
	    	        map.put("devimg", R.drawable.ok2);
	    	        map.put("arrowimg", R.drawable.general__shared__more);
	    	        map.put("flagimg", R.drawable.plugin_new);
	    	        
	    	        mDeviceMapList.add(map);
	    	        mScannedDevices.add(device);
	    	        mScannedDevicesSimpleAdapter.notifyDataSetChanged();
                }
            } 
            // 蓝牙搜索结束
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	mProDialog.cancel();
            	
            	// 蓝牙扫描结束后，把剩下已配对但未在扫描范围内的设备也列出来
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                    	if (mScannedDevices.contains(device) == false) {
                	        map = new HashMap<String, Object>();
                	        map.put("title", device.getName());
                	        map.put("info", "蓝牙地址: " + device.getAddress());
                	        map.put("devimg", R.drawable.ok_gray);
                	        map.put("arrowimg", R.drawable.general__shared__more);
                	        
                	        mDeviceMapList.add(map);
                	        mScannedDevices.add(device);
                    	}
                    }
                }
    	        mScannedDevicesSimpleAdapter.notifyDataSetChanged();
    	        mTips.setText(R.string.scanned_tips_conn);

            }
        }
    };

    /**
     *  蓝牙串口通信service广播接收器（处理连接成功/失败/丢失、AT命令响应等事件）
     */
    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // AT命令响应
            if (CommonDefine.ACTION_AT_ACK.equals(action)) {
            	
				ATCmdBean data = (ATCmdBean)intent.getSerializableExtra(CommonDefine.MSG);
            	Log.d(TAG, "recv at cmd: " + data.getCmdCode() + ", ack: " + data.getAckCode());
				
				if(!data.getOwner().equals(mOwner)) {
	            	Log.d(TAG, "msg owner:" + data.getOwner() + ", my owner " + mOwner);
					return;
				}

				if (mProDialog != null && mProDialog.isShowing()) {
                	mProDialog.cancel();
            	}					
				
				if(data.getCmdCode() == CommonDefine.CMDCODE_GET_INFO) {
	            	String name = mBtAdapter.getRemoteDevice(data.getAddress()).getName();
					
	            	if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
						doNotifyNewDevConn(data);
					}
	            	else {
	            		Log.e(TAG, "查询 " + name + " 设备信息失败，错误码：" + data.getAckCode());
						Toast.makeText(getActivity(), name + " 不是合法的源科设备", Toast.LENGTH_SHORT).show();
						doDisconnDev(data.getAddress());
						mDevConnected = false;
						
					}
				}
            }

            // 连接成功
            else if (CommonDefine.ACTION_CONNECT_SUCCESS.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				String owner = intent.getExtras().getString(CommonDefine.OWNER);
				
				if (owner.equals(mOwner)) {
				
	                mProDialog.setMessage(getResources().getString(R.string.getinfo_diag_msg));  
	            	
			        // 更新View
			        for(Map<String, Object> map : mDeviceMapList) {
			        	String info = (String)map.get("info");
			        	if (info.indexOf(address) > 0) {
			        		map.remove("flagimg");
			        		map.remove("devimg");
			        		map.put("devimg", R.drawable.ok2);
			        		break;
			        	}
			        }				
			        
					mDevConnected = true;
			        
					// 蓝牙连接成功后，尝试下发AT查询设备信息
			        doGetDeviceInfo(address);
				}
            }

            // 连接失败
            else if (CommonDefine.ACTION_CONNECT_FAILED.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				String owner = intent.getExtras().getString(CommonDefine.OWNER);
				
				if (owner.equals(mOwner)) {
					
	            	String name = mBtAdapter.getRemoteDevice(address).getName();
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}					

	            	Toast.makeText(getActivity(), name + " 连接失败", Toast.LENGTH_SHORT).show();
				}
			}
            
            // 连接丢失
            else if (CommonDefine.ACTION_CONNECT_LOST.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
            	String name = mBtAdapter.getRemoteDevice(address).getName();

                // 更新View
                for(Map<String, Object> map : mDeviceMapList) {
                	String info = (String)map.get("info");
                	if (info.indexOf(address) > 0) {
                		map.remove("flagimg");
                		map.remove("devimg");
                		map.put("devimg", R.drawable.ok2);
                		break;
                	}
                }
				mDevConnected = false;

            	Toast.makeText(getActivity(), name + " 连接丢失", Toast.LENGTH_SHORT).show();
            }
	        mScannedDevicesSimpleAdapter.notifyDataSetChanged();

        }
    };
    
}
