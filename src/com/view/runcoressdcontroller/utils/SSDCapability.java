/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：加密盘规格能力定义
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
 * 加密盘规格能力定义类
 * @author allen
 */
public class SSDCapability implements Serializable {
	
	// 型号名称
	String name;
	// 厂商ID
	String vid;
	// 型号ID
	String pid;
	// 包含磁盘个数
	int diskNum;
	// 是否支持蓝牙
	boolean supportBT;
	// 是否支持修改运行密码
	boolean supportRunPwd;
	// 是否支持修改销毁密码
	boolean supportKillPwd;
	// 是否支持防拆卸功能
	boolean supportAntiBreak;
	// 是否支持防插拔功能
	boolean supportAntiEject;
	// 是否支持智能销毁功能
	boolean supportSuicide;
	// 是否支持蓝牙销毁
	boolean supportBTKill;
	// 是否支持3G销毁
	boolean support3GKill;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public boolean isSupportBT() {
		return supportBT;
	}
	public void setSupportBT(boolean supportBT) {
		this.supportBT = supportBT;
	}
	public boolean isSupportRunPwd() {
		return supportRunPwd;
	}
	public void setSupportRunPwd(boolean supportRunPwd) {
		this.supportRunPwd = supportRunPwd;
	}
	public boolean isSupportKillPwd() {
		return supportKillPwd;
	}
	public void setSupportKillPwd(boolean supportKillPwd) {
		this.supportKillPwd = supportKillPwd;
	}
	public boolean isSupportAntiBreak() {
		return supportAntiBreak;
	}
	public void setSupportAntiBreak(boolean supportAntiBreak) {
		this.supportAntiBreak = supportAntiBreak;
	}
	public boolean isSupportAntiEject() {
		return supportAntiEject;
	}
	public void setSupportAntiEject(boolean supportAntiEject) {
		this.supportAntiEject = supportAntiEject;
	}
	public boolean isSupportSuicide() {
		return supportSuicide;
	}
	public void setSupportSuicide(boolean supportSuicide) {
		this.supportSuicide = supportSuicide;
	}
	public boolean isSupportBTKill() {
		return supportBTKill;
	}
	public void setSupportBTKill(boolean supportBTKill) {
		this.supportBTKill = supportBTKill;
	}
	public boolean isSupport3GKill() {
		return support3GKill;
	}
	public void setSupport3GKill(boolean support3gKill) {
		support3GKill = support3gKill;
	}
	
	@Override
	public String toString() {
		return "设备能力: [name=" + name + ", vid=" + vid + ", pid=" + pid + ", diskNum=" + diskNum + ", supportBT=" + supportBT
				 + ", supportRunPwd=" + supportRunPwd + ", supportKillPwd=" + supportKillPwd + ", supportAntiBreak=" + supportAntiBreak
				 + ", supportAntiEject=" + supportAntiEject + ", supportSuicide=" + supportSuicide + ", supportBTKill=" + supportBTKill
				 + ", support3GKill=" + support3GKill;
	}
	
}
