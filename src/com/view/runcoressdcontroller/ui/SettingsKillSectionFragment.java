/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺Զ�����ٽ���Fragment
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

/**
 * Զ�����ٽ���fragment
 * @author allen
 */
public class SettingsKillSectionFragment extends Fragment {
	public static final String TAG = "SettingsKillSectionFragment";
	
	// ��ǰ�ļ������豸����
	private SSDDevice mWorkingDev = null;

	// ���ȶԻ���
    private ProgressDialog mProDialog;

    // �������ٰ�ť�������ı���
	Button mBtKillBtn = null;
	EditText mBtKillPwdBox = null;

	// �޸�������������������Դ����İ�ť���ı���
	Button mModRetriesBtn = null;
	EditText mMaxRetriesBox = null;
    
	// AT��������Ϣ
    String mOwner = null;    

    /**
     * �չ�����
     */
	public SettingsKillSectionFragment() {
	}

	/**
	 * Fragment����ʱ�Ļص�����
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		mOwner = this.getClass().getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
		mWorkingDev = (SSDDevice)getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_SSDDEV);

		// ע������Service�㲥������
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);  
	}
	
	/**
	 * Fragment����viewʱ�Ļص�����
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
	 * ����Ӳ��Ԥ����
	 */
	private void preKillDisk() {
        Log.d(TAG, "preKillDisk()");

        String killPwd = mBtKillPwdBox.getText().toString().trim();

        // �ж������������Ч��
		if ("".equals(killPwd)) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_PWD_NULL, Toast.LENGTH_SHORT).show();
        	return;
		}
			
		if (killPwd.indexOf(",") >= 0) {
        	Toast.makeText(getActivity(), CommonDefine.DISPINFO_PWD_INVALID, Toast.LENGTH_SHORT).show();
        	return;
		}
		
		// �������ٵ�AT������豸
        String[] paras = {killPwd,
    			mWorkingDev.getMac(), 
    			mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_KILL, paras);
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
	 * �޸�����������������������Ԥ����
	 */
	private void preModifyPasswordRetries() {
        Log.d(TAG, "preModifyPasswordRetries()");
        int maxNum;
        
        // �ж����������������������Ч��
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
        
		// �����޸�����������Դ�����AT������豸
        String[] paras = {mMaxRetriesBox.getText().toString().trim(),
    			mWorkingDev.getMac(), 
    			mWorkingDev.getSn()};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_RETRY, paras);
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
	 * �����豸����
	 * @param data �����豸��AT������Ӧ
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
	 * �޸���������������
	 * @param data �޸�������������AT������Ӧ
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
	 * Fragment�ָ���ʾ�Ļص�����
	 */
	@Override
	public void onResume() {
        Log.d(TAG, "onResume()");
		super.onResume();
		
		updateControllers();
	}

	/**
	 * ��������ͨ��service�Ĺ㲥������
	 */
    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // AT������Ӧ
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
            
            // ���Ӷ�ʧ
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

    	TextView maxRetriesTitleView = (TextView)getActivity().findViewById(R.id.title_max_retries);
    	Button maxRetriesBtn = (Button)getActivity().findViewById(R.id.button_mod_max_retries_cfm);
    	EditText maxRetriesEdit = (EditText)getActivity().findViewById(R.id.editTextMaxRetries);
    	
    	SSDCapability cap = mWorkingDev.getCapablity();
    	
    	// ���¿ؼ��Ƿ����
    	if (cap.isSupportSuicide() == false) {
    		maxRetriesTitleView.setTextColor(Color.GRAY);
    		maxRetriesBtn.setTextColor(Color.GRAY);
    		maxRetriesBtn.setEnabled(false);
    		maxRetriesEdit.setEnabled(false);
    	}
    }
    
}
