/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺AT��������
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
 * 
 */

package com.view.runcoressdcontroller.utils;

import java.io.Serializable;

/**
 * ���ڴ����AT������Ϣ��
 * @author allen
 */
public class ATCmdBean implements Serializable{

	// AT�������
	private String owner = "";
			
	// AT���APP���͸��豸��
	private String atCmd = "";
	// AT��Ӧ���豸���ظ�APP��
	private String atAck = "";
	
	// �豸������ַ
	private String address = "";
	
	// AT�����붨�壨APP�ڲ�ģ���ʹ�ã�
	private int cmdCode = -1;
	// AT��Ӧ�붨�壨APP�ڲ�ģ���ʹ�ã�
	private int ackCode = CommonDefine.ACKCODE_NONE;
	
	// AT��������б�
	private String[] cmdParas = {"", "", "", "", ""};
	// AT��Ӧ�����б�
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
	 * ����AT������Ͳ����б��������շ��͸��豸������AT�����ַ���
	 * @return AT�����ַ���
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
	 * �����豸���ص�AT��Ӧ�����������ز�������Ӧ��
	 * @param ack AT��Ӧ�ַ���
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
	 * ��ȡ������
	 * @return ������
	 */
	public int getCmdCode() {
		return this.cmdCode;
	}
	
	/**
	 * ��ȡ��Ӧ��
	 * @return ��Ӧ��
	 */
	public int getAckCode() {
		return this.ackCode;
	}

	/**
	 * ��ȡAT��������б�
	 * @return AT��������б�
	 */
	public String[] getCmdParas() {
		return cmdParas;
	}
	
	/**
	 * ��ȡ�豸��Ϣ�����ݲ�ѯ�豸��Ϣ��AT������Ӧ�õ���
	 * @return ��ѯ�豸��Ϣ��AT��Ӧ�����б�
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
	 * ���ø�AT�����Ŀ���豸��������ַ
	 * @param address �豸������ַ
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	
	/**
	 * ��ȡAT�����Ŀ���豸��������ַ
	 * @return �豸������ַ
	 */
	public String getAddress() {
		return this.address;
	}
	
	/**
	 * ��ȡAT���������Ϣ
	 * @return AT���������Ϣ
	 */
	public String getOwner() {
		return this.owner;
	}
}
