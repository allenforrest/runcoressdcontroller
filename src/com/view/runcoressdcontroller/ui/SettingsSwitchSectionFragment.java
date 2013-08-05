/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：被动防御界面Fragment
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

/**
 * 被动防御界面fragment
 * @author allen
 */
public class SettingsSwitchSectionFragment extends Fragment {
	public static final String TAG = "SettingsSwitchSectionFragment";
	
	// 进度对话框
    private ProgressDialog mProDialog;
	
    // 当前运行的设备
	private SSDDevice mWorkingDev = null;

	// 防拆卸功能开关
	ImageView mBreakToggleView = null;
	// 防插拔功能开关
	ImageView mEjectToggleView = null;
	
	boolean mBreakToggle = false;
	boolean mEjectToggle = false;

	// 默认启动盘
	CheckBox mBootABox = null;
	CheckBox mBootBBox = null;
	
	int mDftBoot = 1;
	
	// AT命令发送者信息
	String mOwner = null;

    /**
     * 空构造器
     */	
	public SettingsSwitchSectionFragment() {
	}

	/**
	 * Fragment创建时的回调方法
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mWorkingDev = (SSDDevice)getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_SSDDEV);
		
    	mBreakToggle = mWorkingDev.isBreakSw();
    	mEjectToggle = mWorkingDev.isEjectSw();
    	mDftBoot = mWorkingDev.getDftBootDisk();

		// 注册蓝牙Service广播接收器
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);  
    	
		mOwner = SettingsSwitchSectionFragment.class.getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
	}
	
	/**
	 * Fragment创建view时的回调方法
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        
		View rootView = inflater.inflate(R.layout.fragment_setting_sw,
				container, false);


		mBreakToggleView = (ImageView)rootView.findViewById(R.id.toggleimg_break);
		mEjectToggleView = (ImageView)rootView.findViewById(R.id.toggleimg_eject);

		mBootABox = (CheckBox)rootView.findViewById(R.id.check_boot_sw_a);
		mBootBBox = (CheckBox)rootView.findViewById(R.id.check_boot_sw_b);
		
		// 防拆卸按钮点击处理
		mBreakToggleView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preConfigDfSwitch(CommonDefine.AT_PARA_DF_BREAK_SW);
			}
		});
		
		// 防插拔按钮点击处理
		mEjectToggleView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preConfigDfSwitch(CommonDefine.AT_PARA_DF_EJECT_SW);
			}
		});		
		
		mBootABox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO: 暂不实现
				/*
				if (mDftBoot == 1) {
					mBootABox.setChecked(true);
				}
				
				mDftBoot = 1;
				preConfigDftBoot();
				*/
			}
		});			
		
		mBootBBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO: 暂不实现
				/*
				if (mDftBoot == 1) {
					mBootBBox.setChecked(true);
				}
				
				mDftBoot = 2;
				preConfigDftBoot();
				*/
			}
		});		
		return rootView;
	}

	/**
	 * 设置默认启动盘预处理
	 */
	private void preConfigDftBoot() {
        Log.d(TAG, "preConfigDftBoot()");

    	String[] paras = {String.valueOf(mDftBoot), "", mWorkingDev.getMac(), mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_SET_BOOT, paras);
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
	 * 设置防御开关预处理
	 * @param swType 防御类型（防拆卸、防插拔）
	 */
	private void preConfigDfSwitch(String swType) {
        Log.d(TAG, "preConfigDfSwitch(): break:" + mBreakToggle + "eject: " + mEjectToggle);
        
        String sw;
    	
        if (swType.equals(CommonDefine.AT_PARA_DF_BREAK_SW)) {
        	if (mBreakToggle) {
        		sw = CommonDefine.AT_PARA_SW_OFF;
        	} else {
        		sw = CommonDefine.AT_PARA_SW_ON;
        	}
        }
        else {
        	if (mEjectToggle) {
        		sw = CommonDefine.AT_PARA_SW_OFF;
        	} else {
        		sw = CommonDefine.AT_PARA_SW_ON;
        	}
        }
        
        // 构造设置防御开关的AT命令下发给设备
    	String[] paras = {swType, sw, mWorkingDev.getMac(), mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_DF_SW, paras);
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
	 * 设置防御开关后处理
	 * @param data 设置防御开关的AT命令响应
	 */
	private void postConfigDfSwitch(ATCmdBean data) {
		
		int retCode = data.getAckCode();
		if (retCode == CommonDefine.ACKCODE_OK) {
			if(data.getCmdParas()[0].equals(CommonDefine.AT_PARA_DF_BREAK_SW)) {
				if (data.getCmdParas()[1].equals(CommonDefine.AT_PARA_SW_ON)) {
					mBreakToggle = true;
				} else {
					mBreakToggle = false;
				}
			}
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_SET_PARA_SUCC, Toast.LENGTH_SHORT).show();
			
		} else {
			if(data.getCmdParas()[0].equals(CommonDefine.AT_PARA_DF_BREAK_SW)) {
				if (data.getCmdParas()[1].equals(CommonDefine.AT_PARA_SW_ON)) {
					mBreakToggle = false;
				} else {
					mBreakToggle = true;
				}
			}	
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_SET_PARA_FAIL, Toast.LENGTH_SHORT).show();
		}
		
		// 根据设置成功或失败后的最新开关状态更新设备和控件
		updateDevice();
		updateControllers();
	}
	
	/**
	 * 设置默认启动盘后处理
	 * @param data 设置默认启动盘的AT命令响应
	 */
	private void postConfigDftBoot(ATCmdBean data) {
		
		int retCode = data.getAckCode();
		if (retCode == CommonDefine.ACKCODE_OK) {
			mDftBoot = Integer.parseInt(data.getCmdParas()[0]);
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_SET_PARA_SUCC, Toast.LENGTH_SHORT).show();
			
		} else {
			if(data.getCmdParas()[0].equals(CommonDefine.AT_PARA_BOOT_A)) {
				mDftBoot = Integer.parseInt(CommonDefine.AT_PARA_BOOT_B);
			} else {
				mDftBoot = Integer.parseInt(CommonDefine.AT_PARA_BOOT_A);
			}
			Toast.makeText(getActivity(), CommonDefine.DISPINFO_SET_PARA_FAIL, Toast.LENGTH_SHORT).show();
		}
		
		updateDevice();
		updateControllers();
	}	
	
	/**
	 * 更新设备对象中的防御开关和默认启动盘
	 */
	private void updateDevice() {
		
		mWorkingDev.setBreakSw(mBreakToggle);
		mWorkingDev.setEjectSw(mEjectToggle);
		mWorkingDev.setDftBootDisk(mDftBoot);
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
	
    // 蓝牙Service广播的接收器
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
				if (data.getCmdCode() == CommonDefine.CMDCODE_DF_SW) {
					postConfigDfSwitch(data);
				}
				else if (data.getCmdCode() == CommonDefine.CMDCODE_SET_BOOT) {
					postConfigDftBoot(data);
				}
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

    	TextView breakTitleView = (TextView)getActivity().findViewById(R.id.title_sw_break);
    	TextView ejectTitleView = (TextView)getActivity().findViewById(R.id.title_sw_eject);
    	TextView dftBootTitleView = (TextView)getActivity().findViewById(R.id.title_sw_bt);
    	
    	SSDCapability cap = mWorkingDev.getCapablity();
    	
    	// 更新控件是否禁用
    	if (cap.isSupportAntiBreak() == false) {
    		breakTitleView.setTextColor(Color.GRAY);
    		mBreakToggleView.setImageResource(R.drawable.reading__custom_typesetting_view__discard_normal);
    		mBreakToggleView.setClickable(false);
    		
    	}
    	
    	if (cap.isSupportAntiEject() == false) {
    		ejectTitleView.setTextColor(Color.GRAY);
    		mEjectToggleView.setImageResource(R.drawable.reading__custom_typesetting_view__discard_normal);
    		mEjectToggleView.setClickable(false);
    	}

    	// FIXME: 当前双盘启动盘设置的方案还未明确，先屏蔽掉参数界面
		mBootABox.setChecked(true);
		mBootBBox.setChecked(false);
    	
		mBootABox.setEnabled(false);
		mBootBBox.setEnabled(false);
		dftBootTitleView.setTextColor(Color.GRAY);

    	if (cap.getDiskNum() == 1) {
    	}       	
    	
    	// 更新控件配置状态
    	if(mBreakToggle == true) {
    		mBreakToggleView.setImageResource(R.drawable.general__shared__switch_selected);
    	} else {
    		mBreakToggleView.setImageResource(R.drawable.general__shared__switch_normal);
    	}
    	
    	if(mEjectToggle == true) {
    		mEjectToggleView.setImageResource(R.drawable.general__shared__switch_selected);
    	} else {
    		mEjectToggleView.setImageResource(R.drawable.general__shared__switch_normal);
    	}    	
    }

}
