/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺sqlite���ݿ������
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
 * 
 */

package com.view.runcoressdcontroller.utils;

import android.content.Context;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper; 


/**
 * ����sqlite���ݿ��ʼ��
 * @author allen
 */
public class DBHelper extends SQLiteOpenHelper {  
  
    public DBHelper(Context context) {  
        // CursorFactory����Ϊnull,ʹ��Ĭ��ֵ  
        super(context, CommonDefine.DATABASE_NAME, null, CommonDefine.DATABASE_VERSION);  
    }  
  
    /**
     * ���ݿ��һ�δ���ʱ�Ļص�����
     */
    @Override  
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CommonDefine.TABLE_NAME +  
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
        		"name VARCHAR, devName VARCHAR, address VARCHAR, vid VARCHAR, pid VARCHAR, isBinded INTEGER)");  
    }  
  
    /**
     * ���ݿ�����ʱ�Ļص�����
     */
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
    }  
}
