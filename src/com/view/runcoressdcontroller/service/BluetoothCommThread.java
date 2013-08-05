/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺������Ϣ�շ��߳�
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
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
 * �����豸��Ϣ�շ��߳�
 * @author Allen
 */
public class BluetoothCommThread extends Thread {
    private static final String TAG = "BluetoothCommThread";

	private Handler serviceHandler;				// �����߳���Ϣ��Service Handler
	private Handler threadHandler;				// ����Service��Ϣ���߳�Handler
	private BluetoothSocket socket;				// �����豸socket
	private InputStream inStream;				// ����������
	private OutputStream outStream;				// ���������
	private BufferedReader bufReader;			// ��������ַ�������
	
	/**
	 * ������
	 * @param handler service���̵߳���Ϣhandler
	 * @param socket ��������socket
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
			//��������ʧ����Ϣ
			serviceHandler.obtainMessage(CommonDefine.MESSAGE_CONNECT_LOST).sendToTarget();
			e.printStackTrace();
		}
	}
	
	/**
	 * �̵߳��������
	 */
	@Override
	public void run() {
		Looper.prepare();
		threadHandler = new Handler() {

			/**
			 * ��Ϣѭ���д���ÿһ���������̵߳���Ϣ
			 */
			@Override
			public void handleMessage(Message msg) {
				String line;
				Message serviceMsg = null;
				boolean timeOut = false;
				
				// ������Ϣ
				switch (msg.what) {
					case CommonDefine.MESSAGE_AT_CMD:
						ATCmdBean atCmdObj = (ATCmdBean)msg.obj;

						// ���AT������豸�뵱ǰ���ӵ��豸�Ƿ����
						if (!atCmdObj.getAddress().equals(socket.getRemoteDevice().getAddress())) {
							Log.e(TAG, "receive AT cmd msg to dev " + atCmdObj.getAddress() + 
									", \nbut now connected dev is " + socket.getRemoteDevice().getAddress());
							break;
						}
						
						// ����Ӧ��ʱ��������
						for (int loop = 0; loop < CommonDefine.AT_CMD_MAX_RETRIES; loop++) {
							String writeData = atCmdObj.genAtCmd();
							
							// Ĭ��������Ϊ��ʱ
							atCmdObj.setAtAck("");
							
							Log.d(TAG, "Send AT cmd to MCU(try" + loop + "): " + writeData);
				            try {
				            	outStream.write(writeData.getBytes());
				            	outStream.write("\r\n".getBytes());
				            } catch (IOException e) {
				                Log.e(TAG, "Exception during write", e);
			                    break;
				            }
				            
				            
				            // ������AT��ȴ���Ӧ
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
			                
							// ������Ӧ������ȷ�ķ��ز���
			                atCmdObj.setAtAck(line);
			    			break;
						}

						// ������Ӧ��Ϣ���ͻ�service���߳�
						serviceMsg = serviceHandler.obtainMessage();
						serviceMsg.what = CommonDefine.MESSAGE_AT_ACK;
						serviceMsg.obj = atCmdObj;
						serviceMsg.sendToTarget();
		    			break;
				
				}
				super.handleMessage(msg);
			}
			
		};
		
		// ������Ϣѭ��
		Looper.loop();

		cancel();
	}

	/**
	 * ���̵߳���Ϣ������д����Ϣ����
	 * @param obj ��Ϣ���ݵĶ���(ATCmdBean)
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
	 * ��ֹ���̣߳��ر�����socket
	 */
    public void cancel() {
		//�ر���
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
     * ��ȡ��������socket
     * @return ��������socket
     */
    public BluetoothSocket getSocket() {
    	return this.socket;
    }
	
}
