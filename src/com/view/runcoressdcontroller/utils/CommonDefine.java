/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺��ģ��乫������
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
 * 
 */

package com.view.runcoressdcontroller.utils;

import java.util.UUID;

/**
 * ����������
 * @author allen
 */
public class CommonDefine {

	// ��ѡ�����ƣ����ڱ����½�û��������룩
	public static final String PREFS_NAME = "runcore";
	
	// sqlite���ݿ���/����/�汾��
	public static final String DATABASE_NAME = "runcore";
	public static final String TABLE_NAME = "ssd";
	public static final int DATABASE_VERSION = 1;
	
	// Ĭ�ϵĵ�½�û���������
    public static final String DFT_LOGIN_USER = "runcore";
    public static final String DFT_LOGIN_PWD = "123456";
	
	// δ֪�������ͺţ���DeviceCapability.xml���޷�ƥ����豸��
	public static final String PRODUCT_NAME_UNKNOWN = "δ֪�ͺ�";
	
	// ��������Э���UUID
	public static final UUID BT_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// ��������ͨ��Service����������/��Ϣ�շ��߳�֮�����Ϣ����
    public static final int MESSAGE_CONNECT_FAILED = 1;
    public static final int MESSAGE_CONNECT_LOST = 2;
    public static final int MESSAGE_CONNECT_SUCC = 3;
    public static final int MESSAGE_AT_CMD = 4;
    public static final int MESSAGE_AT_ACK = 5;
    public static final int MESSAGE_TOAST = 6;

    // �豸���Activity���ݸ�Fragment�Ĳ������ͣ��豸���������������豸����
    public static final String FRAGMENT_ARG_DEVCAP = "fragment_arg_dev_cap";
    public static final String FRAGMENT_ARG_SSDDEV = "fragment_arg_ssd_dev";

    // ������ʾ��Ϣ��Toast��
    public static final String DISPINFO_CONNECTED_STATUS = "��ǰ�����豸�����У���������������";
    public static final String DISPINFO_CONNECTION_FAILED = "��������Ӳ��ʧ��";
    public static final String DISPINFO_CONNECTION_LOST = "Ӳ�̵��������Ӷ�ʧ";
    public static final String DISPINFO_LOGIN_PWD_ERROR = "��ȫ��¼�������";
    public static final String DISPINFO_MOD_PWD_FAIL = "�����޸�ʧ��";
    public static final String DISPINFO_MOD_PWD_SUCC = "�����޸ĳɹ�";
    public static final String DISPINFO_PWD_INVALID = "����ֻ���ɴ�Сд��ĸ��������϶���";
    public static final String DISPINFO_PWD_CONFLICT = "����˫�̵����벻������ͬ";
    public static final String DISPINFO_PWD_NULL = "���벻����Ϊ��";
    public static final String DISPINFO_PWD_NO_CHANGE = "�������벻����ͬ";
    public static final String DISPINFO_RETRIES_OUT_OF_RANGE = "����������Դ�������Ϊ1-9֮�������";
    public static final String DISPINFO_KILL_FAIL = "��������ʧ��";
    public static final String DISPINFO_KILL_SUCC = "�������ٳɹ�";
    public static final String DISPINFO_SET_PARA_FAIL = "��������ʧ��";
    public static final String DISPINFO_SET_PARA_SUCC = "�������óɹ�";

    // �������ͺŹؼ���
    public static final String PRODUCT_AEGIS = "��˹��";
    public static final String PRODUCT_TAIJI = "̫����";
    public static final String PRODUCT_REDFLAG = "����";
    
    // �豸���Activity�������Fragment����
    public static final int SECTION_BINDED_DEV = 1;
    public static final int SECTION_SCAN_DEV = 0;

    // ��������Activity�������Fragment����
    public static final int SECTION_PASSWORD = 0;
    public static final int SECTION_FUNC_SW = 1;
    public static final int SECTION_KILL_HD = 2;
	
    // ����BindActionActivity���������ͣ���/��󡢿�����
    public static final int REQUEST_BIND_DISK = 100;
    public static final int REQUEST_RUN_DISK = 101;
    
    
    /** 
     * ������Activity����������ͨ��service֮��ͨ��Intent��action
     */
	// Action��ѡ����������ӵ��豸
	public static final String ACTION_SELECTED_DEVICE = "ACTION_SELECTED_DEVICE";

	// Action���Ͽ���ǰ���ӵ��豸
	public static final String ACTION_DISCONNECT_DEVICE = "ACTION_DISCONNECT_DEVICE";
	
	// Action���رպ�̨Service
	public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
	
	// Action���·��������豸��AT����
	public static final String ACTION_AT_CMD = "ACTION_AT_CMD";
	
	// Action�������豸���ص�AT��Ӧ
	public static final String ACTION_AT_ACK = "ACTION_AT_ACK";
	
	// Action�����ӳɹ�
	public static final String ACTION_CONNECT_SUCCESS = "ACTION_CONNECT_SUCCESS";
	
	// Action������ʧ��
	public static final String ACTION_CONNECT_FAILED = "ACTION_CONNECT_FAILED";
    
	// Action�����Ӷ�ʧ
	public static final String ACTION_CONNECT_LOST = "ACTION_CONNECT_LOST";
    
	/**
	 * ������Activity����������ͨ��service֮��ͨ��Intent�д��ݵĲ�������
	 */
	public static final String DEVICE = "DEVICE";
	public static final String OWNER = "OWNER";
	public static final String SSDDEV = "SSDDEV";
	public static final String BINDFLAG = "BINDFLAG";
	public static final String PWD_A = "PWD_A";
	public static final String PWD_B = "PWD_B";
	public static final String MSG = "MSG";
	
	/**
	 * ��ģ������������ͨ��service֮�䴫��AT������Ϣʱָ����������
	 */
    public static final int CMDCODE_GET_INFO = 1;
    public static final int CMDCODE_DEV_BIND = 2;
    public static final int CMDCODE_OPEN_SSD = 3;
    public static final int CMDCODE_MOD_PWD = 4;
    public static final int CMDCODE_SET_BOOT = 5;
    public static final int CMDCODE_DF_SW = 6;
    public static final int CMDCODE_RETRY = 7;
    public static final int CMDCODE_KILL = 8;
    
	/**
	 * ��������ͨ��service���ģ��֮�䴫��AT������Ӧʱָ������Ӧ��
	 */
    public static final int ACKCODE_NONE = -1;
    public static final int ACKCODE_OK = 0;
    public static final int ACKCODE_ERR_BIND_DEV = 1;
    public static final int ACKCODE_ERR_PASSWD = 2;
    public static final int ACKCODE_ERR_UNSPEC = 255;
    public static final int ACKCODE_ERR_PARA = 256;
    public static final int ACKCODE_ERR_TIMEOUT = 257;
	
    public static final int AT_CMD_MAX_RETRIES = 3;
    public static final int AT_CMD_MAX_ACK_TIME = 5;

    /**  
	 * APP��MCUͨ�ŵ�AT��� 
	 */ 
	// ������Ӧ 
	public static final String AT_ACK_OK = "OK";
	public static final String AT_ACK_ERR = "+CMEE:";
	
	// ��ѯӲ����Ϣ 
	public static final String AT_CMD_INFO = "AT+INFO?";
	public static final String AT_ACK_INFO = "+INFO:";

	// �ն˰�����
	public static final String AT_CMD_BIND = "AT+BIND=%s,%s,%s,%s,%s";
	
	// ����������
	public static final String AT_CMD_RUN = "AT+RUN=%s,%s,%s";

	// �޸�Ӳ������
	public static final String AT_CMD_PWD = "AT+PWD=%s,%s,%s,%s,%s";

	// ����������
	public static final String AT_CMD_BOOT = "AT+BOOT=%s,%s,%s,%s";

	// ���ñ�����������
	public static final String AT_CMD_DF = "AT+DF=%s,%s,%s,%s";

	// �������������������
	public static final String AT_CMD_RETRY = "AT+RETRY=%s,%s,%s";

	// ��������Ӳ��
	public static final String AT_CMD_KILL = "AT+KILL=%s,%s,%s";
	
	/**
	 * APP��MCUͨ�ŵ�AT�������
	 */
	public static final String AT_PARA_BIND = "1";
	public static final String AT_PARA_UNBIND = "0";
	
	public static final String AT_PARA_DF_BREAK_SW = "0";
	public static final String AT_PARA_DF_EJECT_SW = "1";	

	public static final String AT_PARA_SW_ON = "1";
	public static final String AT_PARA_SW_OFF = "0";	
	
	public static final String AT_PARA_BOOT_A = "1";
	public static final String AT_PARA_BOOT_B = "2";	

	public static final String AT_PARA_RUN_PWD = "0";
	public static final String AT_PARA_KILL_PWD = "1";		
}
