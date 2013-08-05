/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺��������ͨ��service
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
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
 * ��������ͨ�Ż���Service
 * @author Allen
 * @version 1.0
 */
public class BluetoothSPPService extends Service {
	private static final String TAG = "BluetoothSPPService";
	
	// ����������
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	// ������Ϣ�շ��߳�
	private BluetoothCommThread communThread = null;
	
	// �����豸�����߳�
	private BluetoothConnThread connThread = null;
	
	// ���ڱ�ʾintent�ķ����ߣ�������Ϣ����
	private String intentOwner = null;
	
	/**
	 * Activity����Service��Ϣ�Ľ�����
	 */
	private BroadcastReceiver activityMsgReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "receive " + action + " action");
			
			// ѡ�������豸�������ӵ�action
			if (CommonDefine.ACTION_SELECTED_DEVICE.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				intentOwner = intent.getExtras().getString(CommonDefine.OWNER);
				
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
				
				//�����豸�����߳�
				connThread = new BluetoothConnThread(handler, device);
				connThread.start();
				
			// �ͷ������豸���ӵ�action
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
			// ֹͣ��service��action
			else if (CommonDefine.ACTION_STOP_SERVICE.equals(action)) {
				//ֹͣ��̨����
				StopThreads();
				unregisterReceiver(activityMsgReceiver);
				stopSelf();
				
			} 
			// ����AT�����action
			else if (CommonDefine.ACTION_AT_CMD.equals(action)) {
				//��ȡ����
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
	 * ���������߳�(ConnThread/CommThread)��Ϣ��Handler
	 */
	Handler handler = new Handler() {
		
		BluetoothSocket socket = null;
		@Override
		public void handleMessage(Message msg) {
			//������Ϣ
			switch (msg.what) {
				case CommonDefine.MESSAGE_CONNECT_FAILED:
					// �������Ӵ���㲥
					socket = (BluetoothSocket)msg.obj;
					
					Intent failedIntent = new Intent(CommonDefine.ACTION_CONNECT_FAILED);
					failedIntent.putExtra(CommonDefine.DEVICE, socket.getRemoteDevice().getAddress());
					failedIntent.putExtra(CommonDefine.OWNER, intentOwner);
					sendBroadcast(failedIntent);
					break;

				case CommonDefine.MESSAGE_CONNECT_LOST:
					// �������Ӷ�ʧ�㲥
					socket = (BluetoothSocket)msg.obj;
					
					Intent lostIntent = new Intent(CommonDefine.ACTION_CONNECT_LOST);
					lostIntent.putExtra(CommonDefine.DEVICE, socket.getRemoteDevice().getAddress());
					sendBroadcast(lostIntent);
					break;
					
				case CommonDefine.MESSAGE_CONNECT_SUCC:
					// ���ӳɹ�
					socket = (BluetoothSocket)msg.obj;
					
					communThread = new BluetoothCommThread(handler, socket);
					communThread.start();
					
					// �������ӳɹ��㲥
					Intent succIntent = new Intent(CommonDefine.ACTION_CONNECT_SUCCESS);
					succIntent.putExtra(CommonDefine.DEVICE, socket.getRemoteDevice().getAddress());
					succIntent.putExtra(CommonDefine.OWNER, intentOwner);
					sendBroadcast(succIntent);
					break;
					
				case CommonDefine.MESSAGE_AT_ACK:
					// AT������Ӧ
					// �������ݹ㲥���������ݶ���
					Intent dataIntent = new Intent(CommonDefine.ACTION_AT_ACK);
					dataIntent.putExtra(CommonDefine.MSG, (Serializable)msg.obj);
					sendBroadcast(dataIntent);
					break;
				
			}
			super.handleMessage(msg);
		}
		
	};
	
	/**
	 * ��ȡ������Ϣ�շ��߳�ʵ��
	 * @return ������Ϣ�շ��߳�ʵ��
	 */
	public BluetoothCommThread getBluetoothCommunThread() {
		return communThread;
	}
	
	/**
	 * Service��Activity��ʱ�Ļص�����
	 */	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Service����ʱ�Ļص�����
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		//controlReceiver��IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(CommonDefine.ACTION_SELECTED_DEVICE);
		controlFilter.addAction(CommonDefine.ACTION_DISCONNECT_DEVICE);
		controlFilter.addAction(CommonDefine.ACTION_STOP_SERVICE);
		controlFilter.addAction(CommonDefine.ACTION_AT_CMD);
		
		//ע��BroadcastReceiver
		registerReceiver(activityMsgReceiver, controlFilter);
		super.onCreate();
	}
	
	/**
	 * Service����ʱ�Ļص�����
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		StopThreads();
		//�����
		unregisterReceiver(activityMsgReceiver);
		super.onDestroy();
	}
	
	/**
	 * ֹͣ�������Ӻ���Ϣ�շ��߳�
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
