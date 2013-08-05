/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：各模块间公共定义
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.utils;

import java.util.UUID;

/**
 * 公共定义类
 * @author allen
 */
public class CommonDefine {

	// 首选项名称（用于保存登陆用户名和密码）
	public static final String PREFS_NAME = "runcore";
	
	// sqlite数据库名/表名/版本号
	public static final String DATABASE_NAME = "runcore";
	public static final String TABLE_NAME = "ssd";
	public static final int DATABASE_VERSION = 1;
	
	// 默认的登陆用户名和密码
    public static final String DFT_LOGIN_USER = "runcore";
    public static final String DFT_LOGIN_PWD = "123456";
	
	// 未知加密盘型号（与DeviceCapability.xml中无法匹配的设备）
	public static final String PRODUCT_NAME_UNKNOWN = "未知型号";
	
	// 蓝牙串口协议的UUID
	public static final UUID BT_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// 蓝牙串口通信Service与蓝牙连接/消息收发线程之间的消息类型
    public static final int MESSAGE_CONNECT_FAILED = 1;
    public static final int MESSAGE_CONNECT_LOST = 2;
    public static final int MESSAGE_CONNECT_SUCC = 3;
    public static final int MESSAGE_AT_CMD = 4;
    public static final int MESSAGE_AT_ACK = 5;
    public static final int MESSAGE_TOAST = 6;

    // 设备面板Activity传递给Fragment的参数类型（设备能力规格表、加密盘设备对象）
    public static final String FRAGMENT_ARG_DEVCAP = "fragment_arg_dev_cap";
    public static final String FRAGMENT_ARG_SSDDEV = "fragment_arg_ssd_dev";

    // 界面提示消息（Toast）
    public static final String DISPINFO_CONNECTED_STATUS = "当前已有设备连接中，不允许发起新连接";
    public static final String DISPINFO_CONNECTION_FAILED = "蓝牙连接硬盘失败";
    public static final String DISPINFO_CONNECTION_LOST = "硬盘的蓝牙连接丢失";
    public static final String DISPINFO_LOGIN_PWD_ERROR = "安全登录密码错误";
    public static final String DISPINFO_MOD_PWD_FAIL = "密码修改失败";
    public static final String DISPINFO_MOD_PWD_SUCC = "密码修改成功";
    public static final String DISPINFO_PWD_INVALID = "密码只能由大小写字母和数字组合而成";
    public static final String DISPINFO_PWD_CONFLICT = "加密双盘的密码不允许相同";
    public static final String DISPINFO_PWD_NULL = "密码不允许为空";
    public static final String DISPINFO_PWD_NO_CHANGE = "新老密码不能相同";
    public static final String DISPINFO_RETRIES_OUT_OF_RANGE = "密码最大重试次数必须为1-9之间的数字";
    public static final String DISPINFO_KILL_FAIL = "磁盘销毁失败";
    public static final String DISPINFO_KILL_SUCC = "磁盘销毁成功";
    public static final String DISPINFO_SET_PARA_FAIL = "参数设置失败";
    public static final String DISPINFO_SET_PARA_SUCC = "参数设置成功";

    // 加密盘型号关键字
    public static final String PRODUCT_AEGIS = "宙斯盾";
    public static final String PRODUCT_TAIJI = "太极侠";
    public static final String PRODUCT_REDFLAG = "红旗";
    
    // 设备面板Activity里的两个Fragment索引
    public static final int SECTION_BINDED_DEV = 1;
    public static final int SECTION_SCAN_DEV = 0;

    // 参数配置Activity里的三个Fragment索引
    public static final int SECTION_PASSWORD = 0;
    public static final int SECTION_FUNC_SW = 1;
    public static final int SECTION_KILL_HD = 2;
	
    // 发给BindActionActivity的请求类型（绑定/解绑、开启）
    public static final int REQUEST_BIND_DISK = 100;
    public static final int REQUEST_RUN_DISK = 101;
    
    
    /** 
     * 各界面Activity与蓝牙串口通信service之间通信Intent的action
     */
	// Action：选择的用于连接的设备
	public static final String ACTION_SELECTED_DEVICE = "ACTION_SELECTED_DEVICE";

	// Action：断开当前连接的设备
	public static final String ACTION_DISCONNECT_DEVICE = "ACTION_DISCONNECT_DEVICE";
	
	// Action：关闭后台Service
	public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
	
	// Action：下发给蓝牙设备的AT命令
	public static final String ACTION_AT_CMD = "ACTION_AT_CMD";
	
	// Action：蓝牙设备返回的AT响应
	public static final String ACTION_AT_ACK = "ACTION_AT_ACK";
	
	// Action：连接成功
	public static final String ACTION_CONNECT_SUCCESS = "ACTION_CONNECT_SUCCESS";
	
	// Action：连接失败
	public static final String ACTION_CONNECT_FAILED = "ACTION_CONNECT_FAILED";
    
	// Action：连接丢失
	public static final String ACTION_CONNECT_LOST = "ACTION_CONNECT_LOST";
    
	/**
	 * 各界面Activity与蓝牙串口通信service之间通信Intent中传递的参数对象
	 */
	public static final String DEVICE = "DEVICE";
	public static final String OWNER = "OWNER";
	public static final String SSDDEV = "SSDDEV";
	public static final String BINDFLAG = "BINDFLAG";
	public static final String PWD_A = "PWD_A";
	public static final String PWD_B = "PWD_B";
	public static final String MSG = "MSG";
	
	/**
	 * 各模块与蓝牙串口通信service之间传递AT命令消息时指定的命令码
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
	 * 蓝牙串口通信service与各模块之间传递AT命令响应时指定的响应码
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
	 * APP与MCU通信的AT命令集 
	 */ 
	// 公共响应 
	public static final String AT_ACK_OK = "OK";
	public static final String AT_ACK_ERR = "+CMEE:";
	
	// 查询硬盘信息 
	public static final String AT_CMD_INFO = "AT+INFO?";
	public static final String AT_ACK_INFO = "+INFO:";

	// 终端绑定命令
	public static final String AT_CMD_BIND = "AT+BIND=%s,%s,%s,%s,%s";
	
	// 开启加密盘
	public static final String AT_CMD_RUN = "AT+RUN=%s,%s,%s";

	// 修改硬盘密码
	public static final String AT_CMD_PWD = "AT+PWD=%s,%s,%s,%s,%s";

	// 设置启动盘
	public static final String AT_CMD_BOOT = "AT+BOOT=%s,%s,%s,%s";

	// 设置被动防御开关
	public static final String AT_CMD_DF = "AT+DF=%s,%s,%s,%s";

	// 设置智能销毁密码次数
	public static final String AT_CMD_RETRY = "AT+RETRY=%s,%s,%s";

	// 主动销毁硬盘
	public static final String AT_CMD_KILL = "AT+KILL=%s,%s,%s";
	
	/**
	 * APP与MCU通信的AT命令参数
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
