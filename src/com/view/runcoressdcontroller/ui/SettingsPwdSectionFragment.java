/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：修改密码界面Fragment
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.ui;

import com.view.runcoressdcontroller.R;
import com.view.runcoressdcontroller.utils.ATCmdBean;
import com.view.runcoressdcontroller.utils.CommonDefine;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.Fragment;

/**
 * 修改密码界面Fragment
 * @author allen
 */
public class SettingsPwdSectionFragment extends Fragment {
	public static final String TAG = "SettingsPwdSectionFragment";
	
	// 进度对话框
    private ProgressDialog mProDialog;
	
    // 当前运行的设备
	private SSDDevice mWorkingDev = null;
	
	// 修改运行密码和销毁密码的确认按钮
	Button mModRunPwdBtn = null;
	Button mModKillPwdBtn = null;
	
	// 密码输入框
	EditText mOldRunPwdBox = null;
	EditText mNewRunPwdBox = null;
	
	EditText mOldKillPwdBox = null;
	EditText mNewKillPwdBox = null;
	
	// AT命令发送者信息
	String mOwner = null;

    /**
     * 空构造器
     */
	public SettingsPwdSectionFragment() {
	}

	/**
	 * Fragment创建时的回调方法
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		
		mWorkingDev = (SSDDevice)getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_SSDDEV);
		mOwner = SettingsPwdSectionFragment.class.getName();
		
		// 注册蓝牙Service广播接收器
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);  
		
        Log.d(TAG, "onCreate(): " + mOwner);
	
	}
	
	/**
	 * Fragment创建view时的回调方法
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        
		View rootView = inflater.inflate(R.layout.fragment_setting_pwd,
				container, false);
		
		mModRunPwdBtn = (Button)rootView.findViewById(R.id.button_mod_run_pwd);
		mModKillPwdBtn = (Button)rootView.findViewById(R.id.button_mod_kill_pwd);
		
		mOldRunPwdBox = (EditText)rootView.findViewById(R.id.editTextOldRunPwd);
		mNewRunPwdBox = (EditText)rootView.findViewById(R.id.editTextNewRunPwd);
		
		mOldKillPwdBox = (EditText)rootView.findViewById(R.id.editTextOldKillPwd);
		mNewKillPwdBox = (EditText)rootView.findViewById(R.id.editTextNewKillPwd);
		
		// 修改运行密码按钮点击事件处理
		mModRunPwdBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preModifyPassword(CommonDefine.AT_PARA_RUN_PWD);
			}
		});
		
		// 修改销毁按钮点击事件处理
		mModKillPwdBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preModifyPassword(CommonDefine.AT_PARA_KILL_PWD);
			}
		});
		
		return rootView;
	}
	
	/**
	 * 修改密码预处理
	 * @param type 密码类型(运行密码、销毁密码）
	 */
	private void preModifyPassword(String type) {
        Log.d(TAG, "preModifyPassword()");

        String oldPwd;
        String newPwd;
        
        if (type == CommonDefine.AT_PARA_RUN_PWD) {
        	oldPwd = mOldRunPwdBox.getText().toString().trim();
        	newPwd = mNewRunPwdBox.getText().toString().trim();
        }
        else {
        	oldPwd = mOldKillPwdBox.getText().toString().trim();
        	newPwd = mNewKillPwdBox.getText().toString().trim();
        }
        
        // 判断密码输入参数的有效性
		if ("".equals(oldPwd) || "".equals(newPwd)) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_PWD_NULL, Toast.LENGTH_SHORT).show();
        	return;
		}
			
		if (oldPwd.indexOf(",") >= 0 || newPwd.indexOf(",") >= 0) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_PWD_INVALID, Toast.LENGTH_SHORT).show();
        	return;
		}
		
		if (oldPwd.equals(newPwd)) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_PWD_NO_CHANGE, Toast.LENGTH_SHORT).show();
			return;
		}        

		// 构造修改密码的AT命令下发给设备
        String[] paras = {type, 
        		oldPwd,
        		newPwd, 
    			mWorkingDev.getMac(), 
    			mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_MOD_PWD, paras);
        atMsg.setAddress(mWorkingDev.getAddress());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// 等待响应
		mProDialog = new ProgressDialog(getActivity());  
		mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
		mProDialog.setTitle(R.string.cfg_ssd_title);  
		mProDialog.setMessage(getResources().getString(R.string.cfg_ssd_msg));  
		mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
		mProDialog.setIndeterminate(false);
		mProDialog.setCancelable(false);  
        mProDialog.show();		
    }
	
	/**
	 * 修改密码后处理
	 * @param data 修改密码的AT命令响应
	 */
	private void postModifyPassword(ATCmdBean data) {
		if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_MOD_PWD_SUCC, Toast.LENGTH_SHORT).show();
			if (data.getCmdParas()[0].equals(CommonDefine.AT_PARA_RUN_PWD)) {
				mOldRunPwdBox.setText("");
				mNewRunPwdBox.setText("");
			} else {
				mOldKillPwdBox.setText("");
				mOldKillPwdBox.setText("");
			}
		} else {
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_MOD_PWD_FAIL, Toast.LENGTH_SHORT).show();
		}
		
	}

	/**
	 * Fragment恢复显示的回调方法
	 */
	@Override
	public void onResume() {
        Log.d(TAG, "onResume()");
		super.onResume();
		
		updateControllers();
	}
    
	/**
	 * 蓝牙串口通信service的广播接收器
	 */
    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (CommonDefine.ACTION_AT_ACK.equals(action)) {
				ATCmdBean data = (ATCmdBean)intent.getSerializableExtra(CommonDefine.MSG);
				
				if(!data.getOwner().equals(mOwner)) {
					return;
				}
				
				mProDialog.cancel();
				postModifyPassword(data);
            }

            else if (CommonDefine.ACTION_CONNECT_LOST.equals(action)) {
    			Toast.makeText(getActivity(), CommonDefine.DISPINFO_CONNECTION_LOST, Toast.LENGTH_LONG).show();
    			getActivity().finish();
            }            
        }
    };
    
    /**
     * 根据参数和规格更新所有控件的状态和信息
     */    
    private void updateControllers() {
    	// TODO: 目前所有硬盘型号都支持密码修改，因此本方法暂时预留
    }
        
    
}
