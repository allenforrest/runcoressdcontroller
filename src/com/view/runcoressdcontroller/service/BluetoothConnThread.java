/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：蓝牙设备连接线程
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
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
 * 蓝牙设备连接线程
 * @author Allen
 */
public class BluetoothConnThread extends Thread{
	
	private static final String TAG = "BluetoothConnThread";

	private Handler serviceHandler;			//用于向客户端Service回传消息的handler
	private BluetoothDevice serverDevice;	//服务器设备
	private BluetoothSocket socket;			//通信Socket
	
	/**
	 * 构造器
	 * @param handler service主线程的消息handler
	 * @param serverDevice 蓝牙设备对象
	 */
	public BluetoothConnThread(Handler handler, BluetoothDevice serverDevice) {
		this.serviceHandler = handler;
		this.serverDevice = serverDevice;
		
		BluetoothSocket tmp = null;

		try {
			// FIXME: 创建非安全的蓝牙socket，在这种情况下无需发起蓝牙配对过程，蓝牙空口也不会加密，后续考虑安全性可以改为安全连接
			tmp = serverDevice.createInsecureRfcommSocketToServiceRecord(CommonDefine.BT_SPP_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket create() failed", e);
        }
		socket = tmp;

	}
	
	/**
	 * 线程的运行入口
	 */	
	@Override
	public void run() {
		String status = "succ";
		
		Log.d(TAG, "thread run");
		
		// 发起蓝牙连接前务必停止蓝牙搜索
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		
		// 发起连接
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
		
		// 发送连接成功消息，消息的obj参数为连接的socket
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
	 * 中止本线程，关闭流和socket
	 */
    public void cancel() {
        try {
        	socket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }

    /**
     * 获取蓝牙设备对象
     * @return 蓝牙设备对象
     */    
    public BluetoothDevice getDevice() {
    	return this.serverDevice;
    }
	
}


