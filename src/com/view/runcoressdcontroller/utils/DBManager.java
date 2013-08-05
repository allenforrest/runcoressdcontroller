/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：sqlite数据库库访问封装类
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
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
 *  数据库访问封装类
 *  @author allen
 */
public class DBManager {  
    private DBHelper helper;  
    private SQLiteDatabase db;  
      
    /**
     * 构造器
     * @param context 操作数据库的Activity上下文
     */
    public DBManager(Context context) {  
        helper = new DBHelper(context);  
        db = helper.getWritableDatabase();  
    }  
      
    /** 
     * 增加一个加密盘设备对象记录
     * @param dev 加密盘设备对象
     */  
    public void add(SSDDevice dev) {  
        db.beginTransaction();  //开始事务  

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
            db.setTransactionSuccessful();  //设置事务成功完成  
        } 
        finally {  
            db.endTransaction();  
        }  
    }  
      
    /** 
     * 更新加密盘设备对象记录 
     * @param dev 加密盘设备对象
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
     * 删除加密盘设备对象记录
     * @param dev 加密盘设备对象
     */  
    public void delete(SSDDevice dev) {  
        db.delete(CommonDefine.TABLE_NAME, "address = ?", new String[]{dev.getAddress()});  
    }  
    
    /**
     * 根据设备地址查询加密盘设备对象记录
     * @param address 蓝牙地址
     * @return 加密盘设备对象
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
     * 查询表中所有加密盘设备对象记录
     * @return List<SSDDevice> 加密盘设备对象清单
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
     * 查询表中所有记录（原始操作）
     * @return  Cursor 数据表记录游标 
     */  
    public Cursor queryTheCursor() {  
        Cursor c = db.rawQuery("SELECT * FROM ssd", null);  
        return c;  
    }  
      
    /** 
     * 关闭数据库
     */  
    public void closeDB() {  
        db.close();  
    }  
}
