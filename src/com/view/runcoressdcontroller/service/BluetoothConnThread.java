/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺�����豸�����߳�
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
 * 
 */

package com.view.runcoressdcontroller.service;

import java.io.IOException;

import com.view.runcoressdcontroller.utils.CommonDefine;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * �����豸�����߳�
 * @author Allen
 */
public class BluetoothConnThread extends Thread{
	
	private static final String TAG = "BluetoothConnThread";

	private Handler serviceHandler;			//������ͻ���Service�ش���Ϣ��handler
	private BluetoothDevice serverDevice;	//�������豸
	private BluetoothSocket socket;			//ͨ��Socket
	
	/**
	 * ������
	 * @param handler service���̵߳���Ϣhandler
	 * @param serverDevice �����豸����
	 */
	public BluetoothConnThread(Handler handler, BluetoothDevice serverDevice) {
		this.serviceHandler = handler;
		this.serverDevice = serverDevice;
		
		BluetoothSocket tmp = null;

		try {
			// FIXME: �����ǰ�ȫ������socket����������������跢��������Թ��̣������տ�Ҳ������ܣ��������ǰ�ȫ�Կ��Ը�Ϊ��ȫ����
			tmp = serverDevice.createInsecureRfcommSocketToServiceRecord(CommonDefine.BT_SPP_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket create() failed", e);
        }
		socket = tmp;

	}
	
	/**
	 * �̵߳��������
	 */	
	@Override
	public void run() {
		String status = "succ";
		
		Log.d(TAG, "thread run");
		
		// ������������ǰ���ֹͣ��������
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		
		// ��������
		try {
			socket.connect();
			
		} catch (Exception ex) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			status = "failed";
		}

		Log.d(TAG, "connect to device " + serverDevice.getName() + status);
		
		// �������ӳɹ���Ϣ����Ϣ��obj����Ϊ���ӵ�socket
		Message msg = serviceHandler.obtainMessage();
		if (status == "succ") {
			msg.what = CommonDefine.MESSAGE_CONNECT_SUCC;
		}
		else {
			msg.what = CommonDefine.MESSAGE_CONNECT_FAILED;
		}
		msg.obj = socket;
		msg.sendToTarget();
	}

	/**
	 * ��ֹ���̣߳��ر�����socket
	 */
    public void cancel() {
        try {
        	socket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }

    /**
     * ��ȡ�����豸����
     * @return �����豸����
     */    
    public BluetoothDevice getDevice() {
    	return this.serverDevice;
    }
	
}


