/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：AT命令传输对象
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.utils;

import java.io.Serializable;

/**
 * 用于传输的AT命令消息类
 * @author allen
 */
public class ATCmdBean implements Serializable{

	// AT命令发送者
	private String owner = "";
			
	// AT命令（APP发送给设备）
	private String atCmd = "";
	// AT响应（设备返回给APP）
	private String atAck = "";
	
	// 设备蓝牙地址
	private String address = "";
	
	// AT命令码定义（APP内部模块间使用）
	private int cmdCode = -1;
	// AT响应码定义（APP内部模块间使用）
	private int ackCode = CommonDefine.ACKCODE_NONE;
	
	// AT命令参数列表
	private String[] cmdParas = {"", "", "", "", ""};
	// AT响应参数列表
	private String[] ackParas = {"", "", "", "", "", "", ""};
	
	public ATCmdBean(String owner, int cmdCode){
		this.owner = owner;
		this.cmdCode = cmdCode;
	}
	
	public ATCmdBean(String owner, int cmdCode, String[] paras){
		this.owner = owner;
		this.cmdCode = cmdCode;
		cmdParas = paras;
	}

	/**
	 * 根据AT命令码和参数列表生成最终发送给设备的完整AT命令字符串
	 * @return AT命令字符串
	 */
	public String genAtCmd() {
		switch (cmdCode) {
			case CommonDefine.CMDCODE_GET_INFO:
				atCmd = CommonDefine.AT_CMD_INFO;
				break;
	
			case CommonDefine.CMDCODE_DEV_BIND:
				atCmd = String.format(CommonDefine.AT_CMD_BIND, cmdParas[0], cmdParas[1], cmdParas[2], cmdParas[3], cmdParas[4]);
				break;
				
			case CommonDefine.CMDCODE_OPEN_SSD:
				atCmd = String.format(CommonDefine.AT_CMD_RUN, cmdParas[0], cmdParas[1], cmdParas[2]);
				break;
				
			case CommonDefine.CMDCODE_MOD_PWD:
				atCmd = String.format(CommonDefine.AT_CMD_PWD, cmdParas[0], cmdParas[1], cmdParas[2], cmdParas[3], cmdParas[4]);
				break;

			case CommonDefine.CMDCODE_SET_BOOT:
				atCmd = String.format(CommonDefine.AT_CMD_BOOT, cmdParas[0], cmdParas[1], cmdParas[2], cmdParas[3]);
				break;

			case CommonDefine.CMDCODE_DF_SW:
				atCmd = String.format(CommonDefine.AT_CMD_DF, cmdParas[0], cmdParas[1], cmdParas[2], cmdParas[3]);
				break;

			case CommonDefine.CMDCODE_RETRY:
				atCmd = String.format(CommonDefine.AT_CMD_RETRY, cmdParas[0], cmdParas[1], cmdParas[2]);
				break;

			case CommonDefine.CMDCODE_KILL:
				atCmd = String.format(CommonDefine.AT_CMD_KILL, cmdParas[0], cmdParas[1], cmdParas[2]);
				break;
		}
		
		return this.atCmd;
	}

	/**
	 * 解析设备返回的AT响应，解析出返回参数和响应码
	 * @param ack AT响应字符串
	 */
	public void setAtAck(String ack) {
		this.atAck = ack;

        if (this.atAck.startsWith(CommonDefine.AT_ACK_OK) == true) {
			ackCode = CommonDefine.ACKCODE_OK;
			return;
        }
        
        if (this.atAck.startsWith(CommonDefine.AT_ACK_INFO) == true) {
        	int pos = this.atAck.indexOf(CommonDefine.AT_ACK_INFO) + CommonDefine.AT_ACK_INFO.length();
        	String[] params = this.atAck.substring(pos).trim().split(",");
        	if (params.length != ackParas.length) {
    			ackCode = CommonDefine.ACKCODE_ERR_PARA;
    			return;
        	}
        	
        	ackParas = params;
        	ackCode = CommonDefine.ACKCODE_OK;
        	return;        			
        }
        
        if (this.atAck.indexOf(CommonDefine.AT_ACK_ERR) >= 0) {
        	int pos = this.atAck.indexOf(CommonDefine.AT_ACK_ERR) + CommonDefine.AT_ACK_ERR.length();
        	ackParas[0] = this.atAck.substring(pos).trim();
        	try {
            	ackCode = Integer.parseInt(ackParas[0]);
        	} catch (Exception e) {
    			ackCode = CommonDefine.ACKCODE_ERR_PARA;
        	}

        	return;
        }
        
        if ("".equals(this.atAck)) {
            ackCode = CommonDefine.ACKCODE_ERR_TIMEOUT;
            return;
        }
        
        ackCode = CommonDefine.ACKCODE_ERR_PARA;

	}
	
	/**
	 * 获取命令码
	 * @return 命令码
	 */
	public int getCmdCode() {
		return this.cmdCode;
	}
	
	/**
	 * 获取响应码
	 * @return 响应码
	 */
	public int getAckCode() {
		return this.ackCode;
	}

	/**
	 * 获取AT命令参数列表
	 * @return AT命令参数列表
	 */
	public String[] getCmdParas() {
		return cmdParas;
	}
	
	/**
	 * 获取设备信息（根据查询设备信息的AT命令响应得到）
	 * @return 查询设备信息的AT响应参数列表
	 */
	public String[] getDevInfo() {
		if (cmdCode == CommonDefine.CMDCODE_GET_INFO && ackCode == CommonDefine.ACKCODE_OK) {
			return ackParas;
		}
		else {
			return null;
		}
	}
	
	/**
	 * 设置该AT命令发送目的设备的蓝牙地址
	 * @param address 设备蓝牙地址
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	
	/**
	 * 获取AT命令发送目的设备的蓝牙地址
	 * @return 设备蓝牙地址
	 */
	public String getAddress() {
		return this.address;
	}
	
	/**
	 * 获取AT命令发送者信息
	 * @return AT命令发送者信息
	 */
	public String getOwner() {
		return this.owner;
	}
}
