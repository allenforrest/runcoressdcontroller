/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：加密盘设备对象定义
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
 * 加密盘设备对象类
 * @author allen
 */
public class SSDDevice implements Serializable {
	
	// 加密盘型号名称
	String name;
	// 蓝牙设备名称
	String devName;
	// 蓝牙地址
	String address;
	// 厂商ID
	String vid;
	// 型号ID
	String pid;
	// 包含磁盘个数
	int diskNum;
	// 防拆卸开关状态
	boolean breakSw;
	// 防插拔开关状态
	boolean ejectSw;
	// 智能销毁密码最大重试次数
	int maxRetries;
	// 默认启动盘
	int dftBootDisk;
	// 设备是否已连接
	boolean isConnected;
	// 设备是否已绑定
	boolean isBinded;
	
	// 对应的加密盘规格能力参数
	SSDCapability capablity;
	
	// 本终端绑定信息
	String mac;
	String sn;

	public SSDDevice(String name, String devName, String address, String vid,
			String pid, int diskNum, boolean breakSw, boolean ejectSw,
			int maxRetries, int dftBootDisk, boolean isConnected, boolean isBinded) {
		super();
		this.name = name;
		this.devName = devName;
		this.address = address;
		this.vid = vid;
		this.pid = pid;
		this.diskNum = diskNum;
		this.breakSw = breakSw;
		this.ejectSw = ejectSw;
		this.maxRetries = maxRetries;
		this.dftBootDisk = dftBootDisk;
		this.isConnected = isConnected;
		this.isBinded = isBinded;
	}
	
	public SSDDevice() {
		this.name = null;
		this.devName = null;
		this.address = null;
		this.vid = null;
		this.pid = null;
		this.diskNum = 1;
		this.breakSw = false;
		this.ejectSw = false;
		this.maxRetries = 3;
		this.dftBootDisk = 1;
		this.isConnected = false;
		this.isBinded = false;
		this.capablity = null;
		this.mac = null;
		this.sn = null;
	}
	
	public void update(SSDDevice dev) {
		this.name = dev.getName();
		this.devName = dev.getDevName();
		this.address = dev.getAddress();
		this.vid = dev.getVid();
		this.pid = dev.getPid();
		this.diskNum = dev.getDiskNum();
		this.breakSw = dev.isBreakSw();
		this.ejectSw = dev.isEjectSw();
		this.maxRetries = dev.getMaxRetries();
		this.dftBootDisk = dev.getDftBootDisk();
		this.isConnected = dev.isConnected();
		this.isBinded = dev.isBinded();
		this.capablity = dev.getCapablity();
		this.mac = dev.getMac();
		this.sn = dev.getSn();		
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public int getDiskNum() {
		return diskNum;
	}

	public void setDiskNum(int diskNum) {
		this.diskNum = diskNum;
	}

	public boolean isBreakSw() {
		return breakSw;
	}

	public void setBreakSw(boolean breakSw) {
		this.breakSw = breakSw;
	}

	public boolean isEjectSw() {
		return ejectSw;
	}

	public void setEjectSw(boolean ejectSw) {
		this.ejectSw = ejectSw;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getDftBootDisk() {
		return dftBootDisk;
	}

	public void setDftBootDisk(int dftBootDisk) {
		this.dftBootDisk = dftBootDisk;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	
	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public boolean isBinded() {
		return isBinded;
	}

	public void setBinded(boolean isBinded) {
		this.isBinded = isBinded;
	}

	public void setCapablity(SSDCapability capablity) {
		this.capablity = capablity;
	}

	public SSDCapability getCapablity() {
		return capablity;
	}
	
	@Override
	public String toString() {
		return "加密盘设备: [name=" + name + ", devName=" + devName + ", address=" + address + ", vid=" + vid + ", pid=" + pid 
				+ ", diskNum=" + diskNum + ", breakSw=" + breakSw + ", ejectSw=" + ejectSw + ", maxRetries=" + maxRetries
				+ ", dftBootDisk=" + dftBootDisk + ", isConnected=" + isConnected + ", isBinded=" + isBinded + ", capablity=" + capablity;
	}	
}
