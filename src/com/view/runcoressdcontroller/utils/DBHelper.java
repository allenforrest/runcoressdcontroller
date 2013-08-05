/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：sqlite数据库管理类
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.utils;

import android.content.Context;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper; 


/**
 * 用于sqlite数据库初始化
 * @author allen
 */
public class DBHelper extends SQLiteOpenHelper {  
  
    public DBHelper(Context context) {  
        // CursorFactory设置为null,使用默认值  
        super(context, CommonDefine.DATABASE_NAME, null, CommonDefine.DATABASE_VERSION);  
    }  
  
    /**
     * 数据库第一次创建时的回调方法
     */
    @Override  
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CommonDefine.TABLE_NAME +  
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
        		"name VARCHAR, devName VARCHAR, address VARCHAR, vid VARCHAR, pid VARCHAR, isBinded INTEGER)");  
    }  
  
    /**
     * 数据库升级时的回调方法
     */
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
    }  
}
