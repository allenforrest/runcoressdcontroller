/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：蓝牙串口通信service
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.service;

import java.io.Serializable;

import com.view.runcoressdcontroller.utils.CommonDefine;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.util.Log;

/**
 * 蓝牙串口通信基础Service
 * @author Allen
 * @version 1.0
 */
public class BluetoothSPPService extends Service {
	private static final String TAG = "BluetoothSPPService";
	
	// 蓝牙适配器
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	// 蓝牙消息收发线程
	private BluetoothCommThread communThread = null;
	
	// 蓝牙设备连接线程
	private BluetoothConnThread connThread = null;
	
	// 用于表示intent的发送者，避免消息混淆
	private String intentOwner = null;
	
	/**
	 * Activity发给Service消息的接收器
	 */
	private BroadcastReceiver activityMsgReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "receive " + action + " action");
			
			// 选择蓝牙设备发起连接的action
			if (CommonDefine.ACTION_SELECTED_DEVICE.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				intentOwner = intent.getExtras().getString(CommonDefine.OWNER);
				
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
				
				//开启设备连接线程
				connThread = new BluetoothConnThread(handler, device);
				connThread.start();
				
			// 释放蓝牙设备连接的action
			} else if (CommonDefine.ACTION_DISCONNECT_DEVICE.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				
				if (connThread != null) {
					BluetoothDevice device = connThread.getDevice();
					if (!device.getAddress().equals(address)) {
						Log.e(TAG, "receive disconn to address " + address + ", but now connecting address " + device.getAddress());
						return;
					}
				}
				
				if (communThread != null) {
					BluetoothSocket socket = communThread.getSocket();
					if (!socket.getRemoteDevice().getAddress().equals(address)) {
						Log.e(TAG, "receive disconn to address " + address + ", but now communication address " + socket.getRemoteDevice().getAddress());
						return;
					}
				}
				StopThreads();

			}
			// 停止本service的action
			else if (CommonDefine.ACTION_STOP_SERVICE.equals(action)) {
				//停止后台服务
				StopThreads();
				unregisterReceiver(activityMsgReceiver);
				stopSelf();
				
			} 
			// 发送AT命令的action
			else if (CommonDefine.ACTION_AT_CMD.equals(action)) {
				//获取数据
				Object data = intent.getSerializableExtra(CommonDefine.MSG);
				if (communThread != null) {
					communThread.sendMsg(data);
				} else {
					Log.e(TAG, "send msg but no commun thread, discard it.");
				}
				
			}			
			else {
				Log.d(TAG, "do nothing");
			}
		}
	};

	
	/**
	 * 接收其他线程(ConnThread/CommThread)消息的Handler
	 */
	Handler handler = new Handler() {
		
		BluetoothSocket socket = null;
		@Override
		public void handleMessage(Message msg) {
			//处理消息
			switch (msg.what) {
				case CommonDefine.MESSAGE_CONNECT_FAILED:
					// 发送连接错误广播
					socket = (BluetoothSocket)msg.obj;
					
					Intent failedIntent = new Intent(CommonDefine.ACTION_CONNECT_FAILED);
					failedIntent.putExtra(CommonDefine.DEVICE, socket.getRemoteDevice().getAddress());
					failedIntent.putExtra(CommonDefine.OWNER, intentOwner);
					sendBroadcast(failedIntent);
					break;

				case CommonDefine.MESSAGE_CONNECT_LOST:
					// 发送连接丢失广播
					socket = (BluetoothSocket)msg.obj;
					
					Intent lostIntent = new Intent(CommonDefine.ACTION_CONNECT_LOST);
					lostIntent.putExtra(CommonDefine.DEVICE, socket.getRemoteDevice().getAddress());
					sendBroadcast(lostIntent);
					break;
					
				case CommonDefine.MESSAGE_CONNECT_SUCC:
					// 连接成功
					socket = (BluetoothSocket)msg.obj;
					
					communThread = new BluetoothCommThread(handler, socket);
					communThread.start();
					
					// 发送连接成功广播
					Intent succIntent = new Intent(CommonDefine.ACTION_CONNECT_SUCCESS);
					succIntent.putExtra(CommonDefine.DEVICE, socket.getRemoteDevice().getAddress());
					succIntent.putExtra(CommonDefine.OWNER, intentOwner);
					sendBroadcast(succIntent);
					break;
					
				case CommonDefine.MESSAGE_AT_ACK:
					// AT命令响应
					// 发送数据广播（包含数据对象）
					Intent dataIntent = new Intent(CommonDefine.ACTION_AT_ACK);
					dataIntent.putExtra(CommonDefine.MSG, (Serializable)msg.obj);
					sendBroadcast(dataIntent);
					break;
				
			}
			super.handleMessage(msg);
		}
		
	};
	
	/**
	 * 获取蓝牙消息收发线程实例
	 * @return 蓝牙消息收发线程实例
	 */
	public BluetoothCommThread getBluetoothCommunThread() {
		return communThread;
	}
	
	/**
	 * Service与Activity绑定时的回调函数
	 */	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Service创建时的回调函数
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		//controlReceiver的IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(CommonDefine.ACTION_SELECTED_DEVICE);
		controlFilter.addAction(CommonDefine.ACTION_DISCONNECT_DEVICE);
		controlFilter.addAction(CommonDefine.ACTION_STOP_SERVICE);
		controlFilter.addAction(CommonDefine.ACTION_AT_CMD);
		
		//注册BroadcastReceiver
		registerReceiver(activityMsgReceiver, controlFilter);
		super.onCreate();
	}
	
	/**
	 * Service销毁时的回调函数
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		StopThreads();
		//解除绑定
		unregisterReceiver(activityMsgReceiver);
		super.onDestroy();
	}
	
	/**
	 * 停止所有连接和消息收发线程
	 */
	public void StopThreads() {

		if (communThread != null) {
			communThread.cancel();
			communThread.interrupt();
			communThread = null;
		}		

		if (connThread != null) {
			connThread.cancel();
			connThread = null;
		}
		
	}
}
