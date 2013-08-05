/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：蓝牙消息收发线程
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.view.runcoressdcontroller.utils.ATCmdBean;
import com.view.runcoressdcontroller.utils.CommonDefine;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
	
/**
 * 蓝牙设备消息收发线程
 * @author Allen
 */
public class BluetoothCommThread extends Thread {
    private static final String TAG = "BluetoothCommThread";

	private Handler serviceHandler;				// 接受线程消息的Service Handler
	private Handler threadHandler;				// 接收Service消息的线程Handler
	private BluetoothSocket socket;				// 蓝牙设备socket
	private InputStream inStream;				// 对象输入流
	private OutputStream outStream;				// 对象输出流
	private BufferedReader bufReader;			// 带缓冲的字符输入流
	
	/**
	 * 构造器
	 * @param handler service主线程的消息handler
	 * @param socket 蓝牙连接socket
	 */
	public BluetoothCommThread(Handler handler, BluetoothSocket socket) {
		this.serviceHandler = handler;
		this.socket = socket;

		try {
			this.outStream = socket.getOutputStream();
			this.inStream = socket.getInputStream();
			
			this.bufReader = new BufferedReader(new InputStreamReader(this.inStream));
		} catch (Exception e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//发送连接失败消息
			serviceHandler.obtainMessage(CommonDefine.MESSAGE_CONNECT_LOST).sendToTarget();
			e.printStackTrace();
		}
	}
	
	/**
	 * 线程的运行入口
	 */
	@Override
	public void run() {
		Looper.prepare();
		threadHandler = new Handler() {

			/**
			 * 消息循环中处理每一条发给本线程的消息
			 */
			@Override
			public void handleMessage(Message msg) {
				String line;
				Message serviceMsg = null;
				boolean timeOut = false;
				
				// 处理消息
				switch (msg.what) {
					case CommonDefine.MESSAGE_AT_CMD:
						ATCmdBean atCmdObj = (ATCmdBean)msg.obj;

						// 检查AT命令发送设备与当前连接的设备是否相符
						if (!atCmdObj.getAddress().equals(socket.getRemoteDevice().getAddress())) {
							Log.e(TAG, "receive AT cmd msg to dev " + atCmdObj.getAddress() + 
									", \nbut now connected dev is " + socket.getRemoteDevice().getAddress());
							break;
						}
						
						// 无响应超时重试三次
						for (int loop = 0; loop < CommonDefine.AT_CMD_MAX_RETRIES; loop++) {
							String writeData = atCmdObj.genAtCmd();
							
							// 默认先设置为超时
							atCmdObj.setAtAck("");
							
							Log.d(TAG, "Send AT cmd to MCU(try" + loop + "): " + writeData);
				            try {
				            	outStream.write(writeData.getBytes());
				            	outStream.write("\r\n".getBytes());
				            } catch (IOException e) {
				                Log.e(TAG, "Exception during write", e);
			                    break;
				            }
				            
				            
				            // 发送完AT后等待响应
			                try {
			                	long reqTime = System.currentTimeMillis();
					            while(bufReader.ready() == false) {
					            	long now = System.currentTimeMillis();
					            	if((now - reqTime) > CommonDefine.AT_CMD_MAX_ACK_TIME * 1000) {
										Log.d(TAG, "wait AT ack timeout");
										timeOut = true;
										break;
					            	}
					            }
					            if (timeOut == true) {
					            	continue;
					            }

			                	line = bufReader.readLine();
			                } catch (IOException e) {
			                    Log.e(TAG, "disconnected", e);
			            		serviceMsg = serviceHandler.obtainMessage();
			            		serviceMsg.what = CommonDefine.MESSAGE_CONNECT_LOST;
			            		serviceMsg.obj = socket;
			            		serviceMsg.sendToTarget();
			            		break;
			                }
							
							Log.d(TAG, "recv AT cmd from MCU: " + line);
			                
							// 根据响应解析正确的返回参数
			                atCmdObj.setAtAck(line);
			    			break;
						}

						// 构造响应消息发送回service主线程
						serviceMsg = serviceHandler.obtainMessage();
						serviceMsg.what = CommonDefine.MESSAGE_AT_ACK;
						serviceMsg.obj = atCmdObj;
						serviceMsg.sendToTarget();
		    			break;
				
				}
				super.handleMessage(msg);
			}
			
		};
		
		// 启动消息循环
		Looper.loop();

		cancel();
	}

	/**
	 * 在线程的消息队列里写入消息对象
	 * @param obj 消息内容的对象(ATCmdBean)
	 */
	public void sendMsg(Object obj) {
		if (threadHandler != null) {
			Message msg = threadHandler.obtainMessage();
			msg.what = CommonDefine.MESSAGE_AT_CMD;
			msg.obj = obj;
			msg.sendToTarget();
		}
	}

	/**
	 * 中止本线程，关闭流和socket
	 */
    public void cancel() {
		//关闭流
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (outStream != null) {
			try {
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 获取蓝牙连接socket
     * @return 蓝牙连接socket
     */
    public BluetoothSocket getSocket() {
    	return this.socket;
    }
	
}
