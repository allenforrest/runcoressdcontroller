/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺sqlite���ݿ����ʷ�װ��
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
 * 
 */

package com.view.runcoressdcontroller.utils;

import java.util.ArrayList;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 *  ���ݿ���ʷ�װ��
 *  @author allen
 */
public class DBManager {  
    private DBHelper helper;  
    private SQLiteDatabase db;  
      
    /**
     * ������
     * @param context �������ݿ��Activity������
     */
    public DBManager(Context context) {  
        helper = new DBHelper(context);  
        db = helper.getWritableDatabase();  
    }  
      
    /** 
     * ����һ���������豸�����¼
     * @param dev �������豸����
     */  
    public void add(SSDDevice dev) {  
        db.beginTransaction();  //��ʼ����  

        int bindFlag;
        if (dev.isBinded()) {
        	bindFlag = 1;
        }
        else {
        	bindFlag = 0;
        }
        
        try {  
            db.execSQL("INSERT INTO " + CommonDefine.TABLE_NAME + " VALUES(null, ?, ?, ?, ?, ?, ?)", 
            		new Object[]{dev.getName(), dev.getDevName(), dev.getAddress(), dev.getVid(), dev.getPid(), bindFlag});  
            db.setTransactionSuccessful();  //��������ɹ����  
        } 
        finally {  
            db.endTransaction();  
        }  
    }  
      
    /** 
     * ���¼������豸�����¼ 
     * @param dev �������豸����
     */  
    public void update(SSDDevice dev) {  
        ContentValues cv = new ContentValues();
        int bindFlag;
        if (dev.isBinded()) {
        	bindFlag = 1;
        }
        else {
        	bindFlag = 0;
        }        

        cv.put("name", dev.getName());  
        cv.put("devName", dev.getDevName());  
        cv.put("address", dev.getAddress());  
        cv.put("vid", dev.getVid());  
        cv.put("pid", dev.getPid());  
        cv.put("isBinded", bindFlag);  

        db.update(CommonDefine.TABLE_NAME, cv, "address = ?", new String[]{dev.getAddress()});  
    }  
      
    /** 
     * ɾ���������豸�����¼
     * @param dev �������豸����
     */  
    public void delete(SSDDevice dev) {  
        db.delete(CommonDefine.TABLE_NAME, "address = ?", new String[]{dev.getAddress()});  
    }  
    
    /**
     * �����豸��ַ��ѯ�������豸�����¼
     * @param address ������ַ
     * @return �������豸����
     */
    public SSDDevice query(String address) {
    	SSDDevice dev = null;
        boolean isBinded;
    	
    	Cursor c = db.query(CommonDefine.TABLE_NAME, null, 
    			"address = ?", new String[]{address}, null, null, null);
    	
    	if (c.getCount() > 0) {
    		c.moveToNext();
    		dev = new SSDDevice();
    		dev.setName(c.getString(c.getColumnIndex("name")));
    		dev.setDevName(c.getString(c.getColumnIndex("devName")));
    		dev.setAddress(c.getString(c.getColumnIndex("address")));
    		dev.setVid(c.getString(c.getColumnIndex("vid")));
    		dev.setPid(c.getString(c.getColumnIndex("pid"))); 
    		if (c.getInt(c.getColumnIndex("isBinded")) == 1) {
    			isBinded = true;
    		} else {
    			isBinded = false;
    		}
    		dev.setBinded(isBinded);		
    	}

		return dev;
    }
    
    /** 
     * ��ѯ�������м������豸�����¼
     * @return List<SSDDevice> �������豸�����嵥
     */  
    public List<SSDDevice> queryAll() {  
        ArrayList<SSDDevice> devices = new ArrayList<SSDDevice>(); 
        boolean isBinded;
        
        Cursor c = queryTheCursor();  
        while (c.moveToNext()) {  
			SSDDevice dev = new SSDDevice();
			dev.setName(c.getString(c.getColumnIndex("name")));
			dev.setDevName(c.getString(c.getColumnIndex("devName")));
			dev.setAddress(c.getString(c.getColumnIndex("address")));
			dev.setVid(c.getString(c.getColumnIndex("vid")));
			dev.setPid(c.getString(c.getColumnIndex("pid")));
			
			if (c.getInt(c.getColumnIndex("isBinded")) == 1) {
				isBinded = true;
			} else {
				isBinded = false;
			}
			
			dev.setBinded(isBinded);
			devices.add(dev);  
        }  
        c.close();  
        return devices;  
    }  
      
    /** 
     * ��ѯ�������м�¼��ԭʼ������
     * @return  Cursor ���ݱ��¼�α� 
     */  
    public Cursor queryTheCursor() {  
        Cursor c = db.rawQuery("SELECT * FROM ssd", null);  
        return c;  
    }  
      
    /** 
     * �ر����ݿ�
     */  
    public void closeDB() {  
        db.close();  
    }  
}
