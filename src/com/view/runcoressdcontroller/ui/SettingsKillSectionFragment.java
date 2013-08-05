/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：远程销毁界面Fragment
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
import com.view.runcoressdcontroller.utils.SSDCapability;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

/**
 * 远程销毁界面fragment
 * @author allen
 */
public class SettingsKillSectionFragment extends Fragment {
	public static final String TAG = "SettingsKillSectionFragment";
	
	// 当前的加密盘设备对象
	private SSDDevice mWorkingDev = null;

	// 进度对话框
    private ProgressDialog mProDialog;

    // 蓝牙销毁按钮和密码文本框
	Button mBtKillBtn = null;
	EditText mBtKillPwdBox = null;

	// 修改智能销毁密码最大重试次数的按钮和文本框
	Button mModRetriesBtn = null;
	EditText mMaxRetriesBox = null;
    
	// AT发送者信息
    String mOwner = null;    

    /**
     * 空构造器
     */
	public SettingsKillSectionFragment() {
	}

	/**
	 * Fragment创建时的回调方法
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		mOwner = this.getClass().getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
		mWorkingDev = (SSDDevice)getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_SSDDEV);

		// 注册蓝牙Service广播接收器
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);  
	}
	
	/**
	 * Fragment创建view时的回调方法
	 */	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        
		View rootView = inflater.inflate(R.layout.fragment_setting_kill,
				container, false);

		mBtKillBtn = (Button)rootView.findViewById(R.id.button_kill_cfm);
		mModRetriesBtn = (Button)rootView.findViewById(R.id.button_mod_max_retries_cfm);
		
		mBtKillPwdBox = (EditText)rootView.findViewById(R.id.editTextKillPwd);
		mMaxRetriesBox = (EditText)rootView.findViewById(R.id.editTextMaxRetries);
		
		mBtKillBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preKillDisk();
			}
		});
		
		mModRetriesBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preModifyPasswordRetries();
			}
		});
		
		return rootView;
	}

	/**
	 * 销毁硬盘预处理
	 */
	private void preKillDisk() {
        Log.d(TAG, "preKillDisk()");

        String killPwd = mBtKillPwdBox.getText().toString().trim();

        // 判断密码输入的有效性
		if ("".equals(killPwd)) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_PWD_NULL, Toast.LENGTH_SHORT).show();
        	return;
		}
			
		if (killPwd.indexOf(",") >= 0) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_PWD_INVALID, Toast.LENGTH_SHORT).show();
        	return;
		}
		
		// 构造销毁的AT命令发给设备
        String[] paras = {killPwd,
    			mWorkingDev.getMac(), 
    			mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_KILL, paras);
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
	 * 修改智能销毁密码重试最大次数预处理
	 */
	private void preModifyPasswordRetries() {
        Log.d(TAG, "preModifyPasswordRetries()");
        int maxNum;
        
        // 判断密码重试最大次数输入的有效性
        try {
        	maxNum = Integer.parseInt(mMaxRetriesBox.getText().toString().trim());
        }
        catch (Exception e) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_RETRIES_OUT_OF_RANGE, Toast.LENGTH_SHORT).show();
        	return;         	
        }
        
        if(maxNum < 1 || maxNum > 9) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_RETRIES_OUT_OF_RANGE, Toast.LENGTH_SHORT).show();
        	return;        	
        }
        
		// 构造修改密码最大重试次数的AT命令发给设备
        String[] paras = {mMaxRetriesBox.getText().toString().trim(),
    			mWorkingDev.getMac(), 
    			mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_RETRY, paras);
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
	 * 销毁设备后处理
	 * @param data 销毁设备的AT命令响应
	 */
	private void postKillDisk(ATCmdBean data) {
		if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_KILL_SUCC, Toast.LENGTH_SHORT).show();
			mBtKillPwdBox.setText("");
		} else {
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_KILL_FAIL, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 修改密码最大次数后处理
	 * @param data 修改密码最大次数的AT命令响应
	 */
	private void postModifyPasswordRetries(ATCmdBean data) {
		if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_SET_PARA_SUCC, Toast.LENGTH_SHORT).show();
			mMaxRetriesBox.setText("");
		} else {
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_SET_PARA_FAIL, Toast.LENGTH_SHORT).show();
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

            // AT命令响应
            if (CommonDefine.ACTION_AT_ACK.equals(action)) {

				ATCmdBean data = (ATCmdBean)intent.getSerializableExtra(CommonDefine.MSG);
				
				if(!data.getOwner().equals(mOwner)) {
					return;
				}
				
				mProDialog.cancel();
				if (data.getCmdCode() == CommonDefine.CMDCODE_KILL) {
					postKillDisk(data);
				}
				else if (data.getCmdCode() == CommonDefine.CMDCODE_RETRY) {
					postModifyPasswordRetries(data);
				}
            }
            
            // 连接丢失
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

    	TextView maxRetriesTitleView = (TextView)getActivity().findViewById(R.id.title_max_retries);
    	Button maxRetriesBtn = (Button)getActivity().findViewById(R.id.button_mod_max_retries_cfm);
    	EditText maxRetriesEdit = (EditText)getActivity().findViewById(R.id.editTextMaxRetries);
    	
    	SSDCapability cap = mWorkingDev.getCapablity();
    	
    	// 更新控件是否禁用
    	if (cap.isSupportSuicide() == false) {
    		maxRetriesTitleView.setTextColor(Color.GRAY);
    		maxRetriesBtn.setTextColor(Color.GRAY);
    		maxRetriesBtn.setEnabled(false);
    		maxRetriesEdit.setEnabled(false);
    	}
    }
    
}
