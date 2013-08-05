/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺Դ���豸�嵥����Fragment
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
 * 
 */

package com.view.runcoressdcontroller.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.view.runcoressdcontroller.R;
import com.view.runcoressdcontroller.utils.ATCmdBean;
import com.view.runcoressdcontroller.utils.CommonDefine;
import com.view.runcoressdcontroller.utils.DBManager;
import com.view.runcoressdcontroller.utils.SSDCapability;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.Fragment;

/**
 * Դ���豸�嵥����fragment
 * @author allen
 */
public class BindedDeviceSectionFragment extends Fragment {
	public static final String TAG = "BindedDeviceSectionFragment";

	// sqlite���ݿ�������ʵ��
	private DBManager mDbMgr = null;
	
	// ����������
    private BluetoothAdapter mBtAdapter;
    
    // ��������������嵥����XML�ж�ȡ��
	private ArrayList<SSDCapability> mDevCapabilities = null;
	// Դ���豸�б�������
    private SimpleAdapter mSSDSimpleAdapter;
    // Դ���豸������Ϣ�б�
    private List<Map<String, Object>> mConnectedSSDlist = new ArrayList<Map<String, Object>>();
    // Դ���豸�����б�
    private List<SSDDevice> mSSDDevices;
    // ���ȶԻ���
    private ProgressDialog mProDialog;
    // ������ײ�����ʾ��Ϣ
    TextView mTips = null;
    
    // ��ǰ��������ڴ�����豸����
    private SSDDevice mWorkingDevice = null;
    
    // ����
	private Vibrator vibrator;
    // ���ڹ�������������ʾ��ק������
	private WindowManager windowManager = null;
	// ���ڲ�����������ʾ��ק������
    private LayoutParams windowParams = null;
    // ��������ʾ�����ض���
	private Animation showAnim, hideAnim;
	// �豸��ק��������λ�ú���ʾɾ���仯��ͼ��view
	private ImageView delImageView;
	// �����䲼��
	private LinearLayout menu;
	// ����ק�豸ԭʼ��view����ק�����ɵ���ʱview
	private View dragItemView, dragView;
	// ��ק���ɵ���ʱview�е��ı�view
	private TextView item;
	// ��ק���ɵ���ʱview�е�ͼƬview
	private ImageView head;
	// ����ѡ����ק�ı�־λ����ֹ�����䲼���ظ���ʾ
	private boolean isChoiceMode = false;
	// ��קview��ԭʼ����
	private int oldX, oldY;
	// ��ǰ��ק���豸���б��е�����
    private int index; 
    
    // �豸IMEI
    String mIMEI = null;
    
    // AT���������Ϣ
    String mOwner = null;

    // ��ǰ�Ƿ����豸�����еı�־��ͬʱֻ����һ���豸���ӣ�
    boolean mDevConnected = false;

    /**
     * �չ�����
     */
	public BindedDeviceSectionFragment() {
	}
	
	/**
	 * Fragment����ʱ�Ļص�����
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // ��ȡϵͳ����������
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // ��ȡ�豸IMEI
	    TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
	    mIMEI = telephonyManager.getDeviceId();

        // ��ȡ����
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
	    
        Log.d(TAG, "IMEI: " + mIMEI);
        
		// ע������Service�㲥������
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_SUCCESS);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_FAILED);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);  
		
		// ��������ΪAT���������Ϣ
		mOwner = BindedDeviceSectionFragment.class.getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
        
        // ����sqlite���ݿ������
        mDbMgr = new DBManager(getActivity());
        
        // ��Fragment������Activity��ȡ�������ļ������豸��������б�
		mDevCapabilities = (ArrayList<SSDCapability>) getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_DEVCAP);        
	}

	/**
	 * Fragment����viewʱ�Ļص�����
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        Log.d(BindedDeviceSectionFragment.TAG, "onCreateView()");
		
		View rootView = inflater.inflate(R.layout.fragment_device_binded,
				container, false);

		// �������豸�б�ĵ��/����/�����¼�������
        ListView ssdDevicesListView = (ListView) rootView.findViewById(R.id.binded_devices);
        ssdDevicesListView.setOnItemClickListener(mDeviceClickListener);
        ssdDevicesListView.setOnItemLongClickListener(mDeviceLongClickListener);
        ssdDevicesListView.setOnTouchListener(mDeviceTouchListener);

        // �����ݿ��ж���������豸�嵥
        mSSDDevices = mDbMgr.queryAll();
        for(SSDDevice dev: mSSDDevices) {
        	Log.d(TAG, "query db SSD: " + dev.getName() + " address: " + dev.getAddress() + " bind: " + dev.isBinded());
        	dev.setConnected(false);
        	HashMap map = getDeviceInfoMap(dev);
        	mConnectedSSDlist.add(map);
        }
        
        // �����ݿ��е��豸�嵥��ʼ�����������豸�б�������
        mSSDSimpleAdapter = new SimpleAdapter(getActivity(), mConnectedSSDlist, R.layout.device_item, 
        		new String[]{"title", "info", "devimg", "arrowimg", "flagimg"}, 
        		new int[]{R.id.title, R.id.info, R.id.devimg, R.id.arrowimg, R.id.flagimg});
        
        ssdDevicesListView.setAdapter(mSSDSimpleAdapter);
        
        // ��ʼ�������ʾ��Ϣ
        mTips = (TextView) rootView.findViewById(R.id.tips_binded_devices);
        
        if (mConnectedSSDlist.isEmpty()) {
        	mTips.setText(R.string.binded_tips_init);
        }
        else {
        	mTips.setText(R.string.binded_tips_bind);
        }

        menu = (LinearLayout)rootView.findViewById(R.id.menu);
		menu.setVisibility(View.GONE);
		delImageView = (ImageView)menu.findViewById(R.id.delimg);
        
		// �����䲼����ʾ����
		showAnim = new TranslateAnimation( Animation.RELATIVE_TO_SELF,0.0f
				, Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 1.0f
				, Animation.RELATIVE_TO_SELF, 0.0f);
		showAnim.setDuration(300);
		// �����䲼�����ض���
		hideAnim = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 1.0f);
		hideAnim.setDuration(200);
		
		return rootView;
	}

	/**
	 * Fragment����ʱ�Ļص�����
	 */
	@Override
	public void onDestroy() {  
        super.onDestroy();  
        mDbMgr.closeDB();  
    }  

	/**
	 * Fragment����ʱ�Ļص�����
	 */	
	private SSDDevice getSSDByAddr(String addr) {
		for (SSDDevice dev : mSSDDevices) {
			if (dev.getAddress().equals(addr)) {
				return dev;
			}
		}
		
		return null;
	}
	
    /**
     *  �����豸�б�������
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	String address;
            
        	// ��ȡ�豸������ַ
			TextView nameView = (TextView)v.findViewById(R.id.info);
			String info = nameView.getText().toString();
			if (info.length() > 17) {
				address = info.substring(info.length() - 17);
			}
			else {
				return;
			}
			
			// �����ǰ�Ѿ����豸���ӣ���������������
            if (mDevConnected) {
            	if(!mWorkingDevice.getAddress().equals(address)) {
    				Toast.makeText(getActivity(), CommonDefine.DISPINFO_CONNECTED_STATUS, Toast.LENGTH_SHORT).show();
    				return;
            	}
            }			
            
            // ����������ַ��ȡ�豸����
            mWorkingDevice = getSSDByAddr(address);
            if (mWorkingDevice == null) {
            	Log.e(TAG, "select a disk(" + address + ") on listview, but it does not exist in device array");
            }

            if(!mWorkingDevice.isConnected()) {
            	// �豸δ���ӣ���������
                Intent selectDeviceIntent = new Intent(CommonDefine.ACTION_SELECTED_DEVICE);
    			selectDeviceIntent.putExtra(CommonDefine.DEVICE, address);
    			selectDeviceIntent.putExtra(CommonDefine.OWNER, mOwner);
    			getActivity().sendBroadcast(selectDeviceIntent);

    			nameView.setText("�豸������......");
    			mProDialog = new ProgressDialog(getActivity());  
    			mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
    			mProDialog.setTitle(R.string.conn_diag_title);  
    			mProDialog.setMessage(getResources().getString(R.string.conn_diag_msg));  
    			mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
    			mProDialog.setIndeterminate(false);
    			mProDialog.setCancelable(false);  
                mProDialog.show();             	
            }
            
            if(mWorkingDevice.isConnected() && !mWorkingDevice.isBinded()) {
            	// �豸�����ӡ�δ�󶨣������
                Intent serverIntent = new Intent(getActivity(), BindActionActivity.class);
                serverIntent.putExtra(CommonDefine.SSDDEV, mWorkingDevice);
                serverIntent.putExtra(CommonDefine.BINDFLAG, true);
                startActivityForResult(serverIntent, CommonDefine.REQUEST_BIND_DISK); 	
            }
            
            if(mWorkingDevice.isConnected() && mWorkingDevice.isBinded()) {
            	// �豸�����ӡ��Ѱ󶨣���������
                Intent serverIntent = new Intent(getActivity(), RunActionActivity.class);
                startActivityForResult(serverIntent, CommonDefine.REQUEST_RUN_DISK);	
            }
            
        }
    };	
    
    /**
     *  �����豸�б�������
     */
    private OnItemLongClickListener mDeviceLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// ��ȡ��ǰ��������view
			dragItemView = arg1;
			// ����ǰview������
			dragItemView.setVisibility(View.INVISIBLE);
			
			// ������ק��̬��view
			dragView = getActivity().getLayoutInflater().inflate(R.layout.device_item, null);
			
			item = (TextView)dragView.findViewById(R.id.title);
			item.setText((String)mConnectedSSDlist.get(arg2).get("title"));
			
			item = (TextView)dragView.findViewById(R.id.info);
			item.setText((String)mConnectedSSDlist.get(arg2).get("info"));

			head = (ImageView)dragView.findViewById(R.id.devimg);
			head.setBackgroundResource(R.drawable.head1);
			
			head = (ImageView)dragView.findViewById(R.id.arrowimg);
			head.setVisibility(View.INVISIBLE);
		
			// ��ʼ��ק
			startDrag();

			// ��20����
			vibrator.vibrate(20);
			
			// ��ֹ��γ���ʱ�����䲼�ַ�������
			if (!isChoiceMode) {
				menu.setVisibility(View.VISIBLE);
				menu.startAnimation(showAnim);
			}
			isChoiceMode = true;
			
			// ��¼��ǰ��ק�е��豸����
			index = arg2;
			
			return false;
		}
	};
	
    /**
     *  �����豸�б�������
     */
	private OnTouchListener mDeviceTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (dragView != null) {
				int[] menuLoc = new int[2];
				menu.getLocationOnScreen(menuLoc);
			
				switch (event.getAction()) {
				// �����������ɿ���
				case MotionEvent.ACTION_UP:
					stopDrag();
					
					dragItemView.setVisibility(View.VISIBLE);

					// ���������������ɿ�������ɾ������
					if ((int)event.getRawX() > menuLoc[0] && (int)event.getRawY() > menuLoc[1]) {
						doDelDevice();
					}
					menu.startAnimation(hideAnim);
					menu.setVisibility(View.GONE);
					isChoiceMode = false;	

					break;
				
				// �����ƶ�
				case MotionEvent.ACTION_MOVE:
					drag((int)event.getX() - oldX, (int)event.getY() - oldY);
					
					if ((int)event.getRawX() > menuLoc[0] && (int)event.getRawY() > menuLoc[1]) {
						delImageView.setBackgroundResource(R.color.rred);
					}
					else {
						delImageView.setBackgroundResource(R.drawable.menu);
					}
					break;
				}
			}
			
			// ������ʼ����¼����
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				oldX = (int)event.getX();
				oldY = (int)event.getY();
			}
			return false;
		}
    };
	
	/**
	 * ��ʼ��ק����ʼ�����ںͲ��ֲ���
	 */
	private void startDrag() {
		windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.TOP | Gravity.LEFT;
		windowParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		windowParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
		windowParams.x = dragItemView.getLeft() + 10;
		windowParams.y = dragItemView.getTop() + 100;
		windowParams.alpha = 0.8f;
		windowParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS|0x00000010;
		windowManager = (WindowManager)getActivity().getSystemService(
				Context.WINDOW_SERVICE);
		windowManager.addView(dragView, windowParams);
	}
	
	/**
	 * ��ק���̴���
	 * @param x ��ק����view��������x
	 * @param y ��ק����view��������y
	 */
	private void drag(int x, int y){
		windowParams.x = x + dragItemView.getLeft() + 10;
		windowParams.y = y + dragItemView.getTop() + 100;
		windowManager.updateViewLayout(dragView, windowParams);
	}
	
	/**
	 * ֹͣ��ק
	 */
	private void  stopDrag() {
		if(dragView != null){
			windowManager.removeView(dragView);
			dragView = null;
		}
	}
	
	/**
	 * ɾ���豸����
	 */
	private void delDev() {
        Log.d(TAG, "delDev()");
		
        Toast.makeText(getActivity(), mSSDDevices.get(index).getName() + " �豸ɾ���ɹ�", Toast.LENGTH_LONG).show();
		
        mDbMgr.delete(mSSDDevices.get(index));
		mConnectedSSDlist.remove(index);
		mSSDDevices.remove(index);
		
        mSSDSimpleAdapter.notifyDataSetChanged();		
	}
	
	/**
	 * ��קɾ���豸�Ĵ������
	 */
    private void doDelDevice() {
        Log.d(TAG, "doDelDevice()");
        
        SSDDevice ssd = mSSDDevices.get(index);
        Log.d(TAG, "remove dev " + ssd.getName() + " (" + ssd.getAddress() + 
        		"), conn: " + ssd.isConnected() + " bind: " + ssd.isBinded());
        
        if(!ssd.isConnected()) {
        	// δ���ӵ��豸ֱ��ɾ��
        	delDev();
        	return;
        }

        if(ssd.isConnected() && !ssd.isBinded()) {
        	// �����ӡ�δ�󶨵��豸���ȷ����������ֱ��ɾ��
        	doDisconnDev(mSSDDevices.get(index).getAddress());
        	delDev();
        	return;
        }
        
        if(ssd.isConnected() && ssd.isBinded()) {
        	// �����ӡ��Ѱ󶨵��豸���ȷ���ȡ���󶨡��ٶ��������ɾ��
            Intent serverIntent = new Intent(getActivity(), BindActionActivity.class);
            serverIntent.putExtra(CommonDefine.SSDDEV, ssd);
            serverIntent.putExtra(CommonDefine.BINDFLAG, false);
            startActivityForResult(serverIntent, CommonDefine.REQUEST_BIND_DISK);        	
        }
		
    }	
    
    /**
     * ����豸��Ԥ�ȴ���
     * @param data BindActionActivity���ص�����
     */
    private void preUnbindDevice(Intent data) {
        Log.d(TAG, "preUnbindDevice()");
    	
        // ��BindActionActivity��ȡ�����������
    	String pwdA = data.getExtras().getString(CommonDefine.PWD_A);
    	String pwdB = data.getExtras().getString(CommonDefine.PWD_B);
    	
    	if (pwdB == null || "".equals(pwdB)) {
    		pwdB = pwdA;
    	}
    	
    	// �������AT������豸
    	String[] paras = {CommonDefine.AT_PARA_UNBIND, pwdA, pwdB, mBtAdapter.getAddress(), mIMEI};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_DEV_BIND, paras);
        atMsg.setAddress(mSSDDevices.get(index).getAddress());
        
        Log.d(TAG, "unbind msg: " + atMsg.genAtCmd());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// �ȴ���Ӧ
		mProDialog = new ProgressDialog(getActivity());  
		mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
		mProDialog.setTitle(R.string.unbind_ssd_title);  
		mProDialog.setMessage(getResources().getString(R.string.unbind_ssd_msg));  
		mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
		mProDialog.setIndeterminate(false);
		mProDialog.setCancelable(false);  
        mProDialog.show();		
    }
    
    /**
     * ���豸��Ԥ�ȴ���
     * @param data BindActionActivity���ص�����
     */    
    private void preBindDevice(Intent data) {
        Log.d(TAG, "preBindDevice()");
    	
        // ��BindActionActivity��ȡ�����������
    	String pwdA = data.getExtras().getString(CommonDefine.PWD_A);
    	String pwdB = data.getExtras().getString(CommonDefine.PWD_B);
    	
    	if (pwdB == null || pwdB.length() == 0) {
    		pwdB = pwdA;
    	}
    	    	
    	// ����󶨵�AT������豸
    	String[] paras = {CommonDefine.AT_PARA_BIND, pwdA, pwdB, mBtAdapter.getAddress(), mIMEI};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_DEV_BIND, paras);
        atMsg.setAddress(mWorkingDevice.getAddress());
        
        Log.d(TAG, "bind msg: " + atMsg.genAtCmd());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// �ȴ���Ӧ
		mProDialog = new ProgressDialog(getActivity());  
		mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
		mProDialog.setTitle(R.string.bind_ssd_title);  
		mProDialog.setMessage(getResources().getString(R.string.bind_ssd_msg));  
		mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
		mProDialog.setIndeterminate(false);
		mProDialog.setCancelable(false);  
        mProDialog.show();		
    }
    
    /**
     * �����豸��Ԥ����
     * @param data BindActionActivity���ص�����
     */
    private void preRunDevice(Intent data) {
        Log.d(TAG, "preRunDevice()");
    	
        // ��BindActionActivity��ȡ�����������
    	String pwd = data.getExtras().getString(CommonDefine.PWD_A);
    	
    	// ���쿪����AT������豸
    	String[] paras = {pwd, mBtAdapter.getAddress(), mIMEI};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_OPEN_SSD, paras);
        atMsg.setAddress(mWorkingDevice.getAddress());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// �ȴ���Ӧ
		mProDialog = new ProgressDialog(getActivity());  
		mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
		mProDialog.setTitle(R.string.run_ssd_title);  
		mProDialog.setMessage(getResources().getString(R.string.run_ssd_msg));  
		mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
		mProDialog.setIndeterminate(false);
		mProDialog.setCancelable(false);  
        mProDialog.show();		
    }
    
    /**
     * ���豸�ĺ���
     * @param data ����ı�AT������Ӧ
     */
    private void postBindDevice(ATCmdBean data) {
        Log.d(TAG, "postBindDevice()");
        
        // �����豸�İ�״̬
        mWorkingDevice.setBinded(true);
        
        for(SSDDevice dev : mSSDDevices) {
        	if (dev.getAddress() == mWorkingDevice.getAddress()) {
        		dev.setBinded(true);
        		break;
        	}
        }
        
        // �󶨺�����豸�б����ı�־ͼ��
        for(Map<String, Object> map : mConnectedSSDlist) {
        	String info = (String)map.get("info");
        	if (info.indexOf(mWorkingDevice.getAddress()) > 0) {
        		map.remove("flagimg");
        		map.put("flagimg", R.drawable.ok_yellow);
        		break;
        	}
        }
        
        mSSDSimpleAdapter.notifyDataSetChanged();	
    }
    
    /**
     * ����豸�ĺ���
     * @param data ����豸��AT������Ӧ
     */
    private void postUnbindDevice(ATCmdBean data) {
        Log.d(TAG, "postUnbindDevice()");
        Toast.makeText(getActivity(), mSSDDevices.get(index).getName() + " �豸����󶨳ɹ�", Toast.LENGTH_LONG).show();
        
        // ���°�״̬��ɾ���豸����
        mSSDDevices.get(index).setBinded(false);
        doDelDevice();
    }
    
    /**
     * �����豸�ĺ���
     * @param data �����豸��AT������Ӧ
     */
    private void postRunDevice(ATCmdBean data) {
        Log.d(TAG, "postRunDevice()");
        
        // �����豸�İ���Ϣ���ն�����MAC��IMEI��
        mWorkingDevice.setMac(mBtAdapter.getAddress());
        mWorkingDevice.setSn(mIMEI);

        // �����豸�Ĳ������ý���Activity
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        intent.putExtra(CommonDefine.SSDDEV, mWorkingDevice);
        startActivity(intent);
    }
    
    /** 
     * ����Activity������Ӧ�Ļص�����
     */
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        
        switch (requestCode) {
        // ��/����豸��������Ӧ�ĺ�����
        case CommonDefine.REQUEST_BIND_DISK:
            if (resultCode == Activity.RESULT_OK) {
            	boolean flag = data.getExtras().getBoolean(CommonDefine.BINDFLAG);
            	if(flag == true) {
                	preBindDevice(data);
            	} else {
                	preUnbindDevice(data);
            	}
            }
            break;
        // �����豸�����ú�����
        case CommonDefine.REQUEST_RUN_DISK:
            if (resultCode == Activity.RESULT_OK) {
            	preRunDevice(data);
            }
            break;
        }
    }

    /**
     * Fragment����ָ��Ļص�����
     */
	@Override
	public void onResume() {
        Log.d(TAG, "onResume()");
		super.onResume();
		
		updateBackground();
	}
	
	/**
	 * ���ݼ������豸�����ȡ���豸������Ϣmap
	 * @param dev �������豸����
	 * @return �豸������Ϣmap
	 */
	private HashMap getDeviceInfoMap(SSDDevice dev) {
        HashMap map = new HashMap<String, Object>();
        map.put("title", dev.getName());
        map.put("info", "�豸��: " + dev.getDevName() + "\n������ַ: " + dev.getAddress());
        map.put("arrowimg", R.drawable.general__shared__more);

        if (dev.getName().indexOf(CommonDefine.PRODUCT_AEGIS) >= 0) {
            map.put("devimg", R.drawable.head2);
        }
        else if (dev.getName().indexOf(CommonDefine.PRODUCT_TAIJI) >= 0) {
            map.put("devimg", R.drawable.head3);
        }
        else {
            map.put("devimg", R.drawable.head1);
        }
        
        if (!dev.isConnected() && !dev.isBinded()) {
            map.put("flagimg", R.drawable.ok_gray);
        } 
        else if (dev.isConnected() && !dev.isBinded()) {
            map.put("flagimg", R.drawable.ok2);
        }
        else if (dev.isConnected() && dev.isBinded()) {
            map.put("flagimg", R.drawable.ok_yellow);
        }
        
        return map;
	}
	
	/**
	 * �����б�������±���ͼ
	 */
	private void updateBackground() {
		ImageView img = (ImageView)getActivity().findViewById(R.id.imageView1);
		if (mConnectedSSDlist.size() == 0) {
			img.setVisibility(View.VISIBLE);	
        	mTips.setText(R.string.binded_tips_init);
			
		} else {
			img.setVisibility(View.GONE);
        	mTips.setText(R.string.binded_tips_bind);

		}
	}
	
	/**
	 * ����һ���������豸�����½����б����ݿ⣩
	 * @param dev �������豸����
	 */
	private void addDeviceInfo(SSDDevice dev) {
		HashMap map = getDeviceInfoMap(dev);

		// ���豸�޸ı�־ͼ
		map.remove("flagimg");
        map.put("flagimg", R.drawable.plugin_new);
		
        mConnectedSSDlist.add(map);
        mSSDDevices.add(dev);
        mDbMgr.add(dev);

        mSSDSimpleAdapter.notifyDataSetChanged();	
	}
	
	/**
	 * ���¼������豸��Ϣ
	 * @param dev �������豸����
	 */
	private void updateDeviceInfo(SSDDevice dev) {
		mDbMgr.update(dev);
		
		for(SSDDevice ssd: mSSDDevices) {
			if(ssd.getAddress().equals(dev.getAddress())) {
				ssd.update(dev);
				break;
			}
		}
		
		HashMap newMap = getDeviceInfoMap(dev);
        for(Map<String, Object> map : mConnectedSSDlist) {
        	String info = (String)map.get("info");
        	if (info.indexOf(dev.getAddress()) > 0) {
        		map.putAll(newMap);
        		break;
        	}
        }
        
        mSSDSimpleAdapter.notifyDataSetChanged();	
    }
	
	/**
	 * �������ֺ����Ӻ�ʶ���һ���µļ������豸����ScanDeviceSectionFragmentͨ��������Activity�ص�֪ͨ������
	 * @param device
	 */
	public void addNewDevice(SSDDevice device) {
		Log.d(TAG, "a runcore SSD device connected: " + device.getName());

		SSDDevice devInDb = mDbMgr.query(device.getAddress());
		
		// �����µļ������豸
		if (devInDb == null) {
			Log.d(TAG, "new device.");
			addDeviceInfo(device);
		}
		else {
			// ��֪�ļ������豸�������豸��Ϣ
			Log.d(TAG, "old device.");
			updateDeviceInfo(device);			
		}
		
		updateBackground();
        mTips.setText(R.string.binded_tips_bind);
	}

	/**
	 * �豸���������Ӷ�ʧ����
	 * @param address �豸��������ַ
	 */
	public void notifyConnLost(String address) {
		
		SSDDevice dev = getSSDByAddr(address);
		
		// �����豸״̬����ʾ��Ϣ
		if (dev != null) {
			dev.setConnected(false);
			updateDeviceInfo(dev);
			
			Toast.makeText(getActivity(), dev.getName() + " ���������Ӷ�ʧ�������豸״̬", Toast.LENGTH_LONG).show();
		}
		else {
			Log.e(TAG, "device address " + address + " conn lost, but the device not exists yet");
		}
	}
	
    /**
     * ��ѯ�豸��ϢԤ����
     * @param address �豸��������ַ
     */
    private void preGetDeviceInfo(String address) {
        Log.d(TAG, "preGetDeviceInfo()");
        
        // �����ѯ�豸��Ϣ��AT����͸��豸
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_GET_INFO);
        atMsg.setAddress(address);
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
    }
    
    /**
     * ��ѯ�豸��Ϣ����
     * @param atGetInfo ��ѯ�豸��Ϣ��AT������Ӧ
     */
    private void postGetDeviceInfo(ATCmdBean atGetInfo) {
        Log.d(TAG, "postGetDeviceInfo()");
        
        // ����AT������Ӧ�����豸��Ϣ
        String[] devInfos = atGetInfo.getDevInfo();
        
        mWorkingDevice.setVid(devInfos[0]);
        mWorkingDevice.setPid(devInfos[1]);
        if (devInfos[2].equals("1")) {
        	mWorkingDevice.setBinded(true);
        } else {
        	mWorkingDevice.setBinded(false);
        }
        if (devInfos[3].equals("1")) {
        	mWorkingDevice.setBreakSw(true);
        } else {
        	mWorkingDevice.setBreakSw(false);
        }    
        if (devInfos[4].equals("1")) {
        	mWorkingDevice.setEjectSw(true);
        } else {
        	mWorkingDevice.setEjectSw(false);
        } 
        mWorkingDevice.setMaxRetries(Integer.parseInt(devInfos[5]));
        mWorkingDevice.setDftBootDisk(Integer.parseInt(devInfos[6]));
        mWorkingDevice.setConnected(true);

        for(SSDCapability devCap : mDevCapabilities) {
        	if (mWorkingDevice.getPid().equals(devCap.getPid()) && mWorkingDevice.getVid().equals(devCap.getVid())) {
        		mWorkingDevice.setCapablity(devCap);
        		break;
        	}
        }

        if (mWorkingDevice.getCapablity() != null) {
        	mWorkingDevice.setName(mWorkingDevice.getCapablity().getName());
        	mWorkingDevice.setDiskNum(mWorkingDevice.getCapablity().getDiskNum());
        }
        else {
        	mWorkingDevice.setName(CommonDefine.PRODUCT_NAME_UNKNOWN);
        	mWorkingDevice.setDiskNum(1);
        }
        
        updateDeviceInfo(mWorkingDevice);        
    }
    
    /**
     * �����Ͽ��������豸������
     * @param address �豸��������ַ
     */
    private void doDisconnDev(String address) {
        Log.d(TAG, "doDisconnDev()");

        Intent disconnIntent = new Intent(CommonDefine.ACTION_DISCONNECT_DEVICE);
        disconnIntent.putExtra(CommonDefine.DEVICE, address);
		getActivity().sendBroadcast(disconnIntent);
		
        mDevConnected = false;
    }
    
    /**
     * ��������ͨ��service�㲥��Ϣ�Ľ�����
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
				
				// ��ѯ�豸��Ϣ��Ӧ
				if(data.getCmdCode() == CommonDefine.CMDCODE_GET_INFO) {
	            	String name = mWorkingDevice.getName();
					
	            	if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
						Toast.makeText(getActivity(), name + " �������ӳɹ�", Toast.LENGTH_SHORT).show();
						postGetDeviceInfo(data);           		
					}
	            	else {
	            		Log.e(TAG, "��ѯ " + mWorkingDevice.getDevName() + " �豸��Ϣʧ�ܣ������룺" + data.getAckCode());
						Toast.makeText(getActivity(), name + " ��ѯ�豸������Ϣʧ�ܣ������豸״̬", Toast.LENGTH_SHORT).show();
						doDisconnDev(data.getAddress());
					}
				}
				
				// ���豸��Ӧ
				else if (data.getCmdCode() == CommonDefine.CMDCODE_DEV_BIND) {
	            	String name = mBtAdapter.getRemoteDevice(data.getAddress()).getName();
					
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}
	            	
	            	if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
	            		if (data.getCmdParas()[0].equals(CommonDefine.AT_PARA_UNBIND)) {
	            			postUnbindDevice(data);
	            		}
	            		else {
							postBindDevice(data);
	            		}
					}
	            	else {
	            		Log.e(TAG, "�� " + name + " �豸ʧ�ܣ������룺" + data.getAckCode());
	            		if (data.getAckCode() == CommonDefine.ACKCODE_ERR_BIND_DEV) {
	            			Toast.makeText(getActivity(), name + " �ѱ������ն˰�", Toast.LENGTH_LONG).show();
	            		}
	            		else if (data.getAckCode() == CommonDefine.ACKCODE_ERR_PASSWD) {
	            			Toast.makeText(getActivity(), name + " �����������", Toast.LENGTH_LONG).show();
	            		}
					}
				}
				
				// ������������Ӧ
				else if (data.getCmdCode() == CommonDefine.CMDCODE_OPEN_SSD) {
	            	String name = mBtAdapter.getRemoteDevice(data.getAddress()).getName();
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}
	            	
	            	if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
						postRunDevice(data);
					}
	            	else {
	            		Log.e(TAG, "����" + name + " �豸ʧ�ܣ������룺" + data.getAckCode());
	            		if (data.getAckCode() == CommonDefine.ACKCODE_ERR_BIND_DEV) {
	            			Toast.makeText(getActivity(), name + " �ѱ������ն˰�", Toast.LENGTH_SHORT).show();
	            		}
	            		else if (data.getAckCode() == CommonDefine.ACKCODE_ERR_PASSWD) {
	            			Toast.makeText(getActivity(), name + " �����������", Toast.LENGTH_SHORT).show();
	            		}
	            		else {
	            			Toast.makeText(getActivity(), name + " �豸�쳣���޷�����", Toast.LENGTH_SHORT).show();
	            		}
					}
				}
            }
            
            // �豸���ӳɹ�
            else if (CommonDefine.ACTION_CONNECT_SUCCESS.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				String owner = intent.getExtras().getString(CommonDefine.OWNER);

				if (owner.equals(mOwner)) {
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}

	                mProDialog.setMessage(getResources().getString(R.string.getinfo_diag_msg));  
					// �������ӳɹ��󣬳����·�AT��ѯ�豸��Ϣ
			        preGetDeviceInfo(address);
			        
			        mDevConnected = true;
				}
            }

            // �豸����ʧ��
            else if (CommonDefine.ACTION_CONNECT_FAILED.equals(action)) {
            	
				String owner = intent.getExtras().getString(CommonDefine.OWNER);
				
				if (owner.equals(mOwner)) {
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}					

					Toast.makeText(getActivity(), mWorkingDevice.getName() + " ����ʧ�ܣ������豸״̬", Toast.LENGTH_LONG).show();
					
			        mSSDSimpleAdapter.notifyDataSetChanged();
				}
            }            

            // �豸���Ӷ�ʧ
            else if (CommonDefine.ACTION_CONNECT_LOST.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);

				notifyConnLost(address);
		        mDevConnected = false;
				
            }
            mSSDSimpleAdapter.notifyDataSetChanged();

        }
    };
	
}
