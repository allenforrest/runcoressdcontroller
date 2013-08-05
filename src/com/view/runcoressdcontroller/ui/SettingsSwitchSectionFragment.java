/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺������������Fragment
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
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
 * ������������fragment
 * @author allen
 */
public class SettingsSwitchSectionFragment extends Fragment {
	public static final String TAG = "SettingsSwitchSectionFragment";
	
	// ���ȶԻ���
    private ProgressDialog mProDialog;
	
    // ��ǰ���е��豸
	private SSDDevice mWorkingDev = null;

	// ����ж���ܿ���
	ImageView mBreakToggleView = null;
	// ����ι��ܿ���
	ImageView mEjectToggleView = null;
	
	boolean mBreakToggle = false;
	boolean mEjectToggle = false;

	// Ĭ��������
	CheckBox mBootABox = null;
	CheckBox mBootBBox = null;
	
	int mDftBoot = 1;
	
	// AT���������Ϣ
	String mOwner = null;

    /**
     * �չ�����
     */	
	public SettingsSwitchSectionFragment() {
	}

	/**
	 * Fragment����ʱ�Ļص�����
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mWorkingDev = (SSDDevice)getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_SSDDEV);
		
    	mBreakToggle = mWorkingDev.isBreakSw();
    	mEjectToggle = mWorkingDev.isEjectSw();
    	mDftBoot = mWorkingDev.getDftBootDisk();

		// ע������Service�㲥������
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);  
    	
		mOwner = SettingsSwitchSectionFragment.class.getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
	}
	
	/**
	 * Fragment����viewʱ�Ļص�����
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
		
		// ����ж��ť�������
		mBreakToggleView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preConfigDfSwitch(CommonDefine.AT_PARA_DF_BREAK_SW);
			}
		});
		
		// ����ΰ�ť�������
		mEjectToggleView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preConfigDfSwitch(CommonDefine.AT_PARA_DF_EJECT_SW);
			}
		});		
		
		mBootABox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO: �ݲ�ʵ��
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
				// TODO: �ݲ�ʵ��
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
	 * ����Ĭ��������Ԥ����
	 */
	private void preConfigDftBoot() {
        Log.d(TAG, "preConfigDftBoot()");

    	String[] paras = {String.valueOf(mDftBoot), "", mWorkingDev.getMac(), mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_SET_BOOT, paras);
        atMsg.setAddress(mWorkingDev.getAddress());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// �ȴ���Ӧ
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
	 * ���÷�������Ԥ����
	 * @param swType �������ͣ�����ж������Σ�
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
        
        // �������÷������ص�AT�����·����豸
    	String[] paras = {swType, sw, mWorkingDev.getMac(), mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_DF_SW, paras);
        atMsg.setAddress(mWorkingDev.getAddress());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// �ȴ���Ӧ
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
	 * ���÷������غ���
	 * @param data ���÷������ص�AT������Ӧ
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
		
		// �������óɹ���ʧ�ܺ�����¿���״̬�����豸�Ϳؼ�
		updateDevice();
		updateControllers();
	}
	
	/**
	 * ����Ĭ�������̺���
	 * @param data ����Ĭ�������̵�AT������Ӧ
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
	 * �����豸�����еķ������غ�Ĭ��������
	 */
	private void updateDevice() {
		
		mWorkingDev.setBreakSw(mBreakToggle);
		mWorkingDev.setEjectSw(mEjectToggle);
		mWorkingDev.setDftBootDisk(mDftBoot);
	}
	
	/**
	 * Fragment�ָ���ʾ�Ļص�����
	 */
	@Override
	public void onResume() {
        Log.d(TAG, "onResume()");
		super.onResume();
		
		updateControllers();
	}
	
    // ����Service�㲥�Ľ�����
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
     * ���ݲ����͹��������пؼ���״̬����Ϣ
     */ 
    private void updateControllers() {

    	TextView breakTitleView = (TextView)getActivity().findViewById(R.id.title_sw_break);
    	TextView ejectTitleView = (TextView)getActivity().findViewById(R.id.title_sw_eject);
    	TextView dftBootTitleView = (TextView)getActivity().findViewById(R.id.title_sw_bt);
    	
    	SSDCapability cap = mWorkingDev.getCapablity();
    	
    	// ���¿ؼ��Ƿ����
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

    	// FIXME: ��ǰ˫�����������õķ�����δ��ȷ�������ε���������
		mBootABox.setChecked(true);
		mBootBBox.setChecked(false);
    	
		mBootABox.setEnabled(false);
		mBootBBox.setEnabled(false);
		dftBootTitleView.setTextColor(Color.GRAY);

    	if (cap.getDiskNum() == 1) {
    	}       	
    	
    	// ���¿ؼ�����״̬
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
