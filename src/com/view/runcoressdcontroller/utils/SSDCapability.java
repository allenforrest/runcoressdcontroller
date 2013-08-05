/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺�����̹����������
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
 * �����̹������������
 * @author allen
 */
public class SSDCapability implements Serializable {
	
	// �ͺ�����
	String name;
	// ����ID
	String vid;
	// �ͺ�ID
	String pid;
	// �������̸���
	int diskNum;
	// �Ƿ�֧������
	boolean supportBT;
	// �Ƿ�֧���޸���������
	boolean supportRunPwd;
	// �Ƿ�֧���޸���������
	boolean supportKillPwd;
	// �Ƿ�֧�ַ���ж����
	boolean supportAntiBreak;
	// �Ƿ�֧�ַ���ι���
	boolean supportAntiEject;
	// �Ƿ�֧���������ٹ���
	boolean supportSuicide;
	// �Ƿ�֧����������
	boolean supportBTKill;
	// �Ƿ�֧��3G����
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
		return "�豸����: [name=" + name + ", vid=" + vid + ", pid=" + pid + ", diskNum=" + diskNum + ", supportBT=" + supportBT
				 + ", supportRunPwd=" + supportRunPwd + ", supportKillPwd=" + supportKillPwd + ", supportAntiBreak=" + supportAntiBreak
				 + ", supportAntiEject=" + supportAntiEject + ", supportSuicide=" + supportSuicide + ", supportBTKill=" + supportBTKill
				 + ", support3GKill=" + support3GKill;
	}
	
}
