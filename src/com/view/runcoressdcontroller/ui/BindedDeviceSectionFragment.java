/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：源科设备清单界面Fragment
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
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
 * 源科设备清单界面fragment
 * @author allen
 */
public class BindedDeviceSectionFragment extends Fragment {
	public static final String TAG = "BindedDeviceSectionFragment";

	// sqlite数据库管理对象实例
	private DBManager mDbMgr = null;
	
	// 蓝牙适配器
    private BluetoothAdapter mBtAdapter;
    
    // 加密盘能力规格清单（从XML中读取）
	private ArrayList<SSDCapability> mDevCapabilities = null;
	// 源科设备列表适配器
    private SimpleAdapter mSSDSimpleAdapter;
    // 源科设备基本信息列表
    private List<Map<String, Object>> mConnectedSSDlist = new ArrayList<Map<String, Object>>();
    // 源科设备对象列表
    private List<SSDDevice> mSSDDevices;
    // 进度对话框
    private ProgressDialog mProDialog;
    // 界面最底部的提示信息
    TextView mTips = null;
    
    // 当前点击后正在处理的设备对象
    private SSDDevice mWorkingDevice = null;
    
    // 振动器
	private Vibrator vibrator;
    // 窗口管理器（用于显示拖拽动画）
	private WindowManager windowManager = null;
	// 窗口参数（用于显示拖拽动画）
    private LayoutParams windowParams = null;
    // 垃圾箱显示和隐藏动画
	private Animation showAnim, hideAnim;
	// 设备拖拽到垃圾箱位置后显示删除变化的图像view
	private ImageView delImageView;
	// 垃圾箱布局
	private LinearLayout menu;
	// 被拖拽设备原始的view和拖拽中生成的临时view
	private View dragItemView, dragView;
	// 拖拽生成的临时view中的文本view
	private TextView item;
	// 拖拽生成的临时view中的图片view
	private ImageView head;
	// 长按选中拖拽的标志位，防止垃圾箱布局重复显示
	private boolean isChoiceMode = false;
	// 拖拽view的原始坐标
	private int oldX, oldY;
	// 当前拖拽的设备在列表中的索引
    private int index; 
    
    // 设备IMEI
    String mIMEI = null;
    
    // AT命令发起者信息
    String mOwner = null;

    // 当前是否有设备连接中的标志（同时只能有一个设备连接）
    boolean mDevConnected = false;

    /**
     * 空构造器
     */
	public BindedDeviceSectionFragment() {
	}
	
	/**
	 * Fragment创建时的回调函数
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // 获取系统蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // 获取设备IMEI
	    TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
	    mIMEI = telephonyManager.getDeviceId();

        // 获取震动器
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
	    
        Log.d(TAG, "IMEI: " + mIMEI);
        
		// 注册蓝牙Service广播接收器
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_SUCCESS);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_FAILED);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);  
		
		// 类名称作为AT命令发起者信息
		mOwner = BindedDeviceSectionFragment.class.getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
        
        // 创建sqlite数据库管理器
        mDbMgr = new DBManager(getActivity());
        
        // 从Fragment依附的Activity获取到完整的加密盘设备能力规格列表
		mDevCapabilities = (ArrayList<SSDCapability>) getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_DEVCAP);        
	}

	/**
	 * Fragment创建view时的回调函数
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        Log.d(BindedDeviceSectionFragment.TAG, "onCreateView()");
		
		View rootView = inflater.inflate(R.layout.fragment_device_binded,
				container, false);

		// 加密盘设备列表的点击/长按/触摸事件监听器
        ListView ssdDevicesListView = (ListView) rootView.findViewById(R.id.binded_devices);
        ssdDevicesListView.setOnItemClickListener(mDeviceClickListener);
        ssdDevicesListView.setOnItemLongClickListener(mDeviceLongClickListener);
        ssdDevicesListView.setOnTouchListener(mDeviceTouchListener);

        // 从数据库中读出保存的设备清单
        mSSDDevices = mDbMgr.queryAll();
        for(SSDDevice dev: mSSDDevices) {
        	Log.d(TAG, "query db SSD: " + dev.getName() + " address: " + dev.getAddress() + " bind: " + dev.isBinded());
        	dev.setConnected(false);
        	HashMap map = getDeviceInfoMap(dev);
        	mConnectedSSDlist.add(map);
        }
        
        // 将数据库中的设备清单初始化到加密盘设备列表适配器
        mSSDSimpleAdapter = new SimpleAdapter(getActivity(), mConnectedSSDlist, R.layout.device_item, 
        		new String[]{"title", "info", "devimg", "arrowimg", "flagimg"}, 
        		new int[]{R.id.title, R.id.info, R.id.devimg, R.id.arrowimg, R.id.flagimg});
        
        ssdDevicesListView.setAdapter(mSSDSimpleAdapter);
        
        // 初始界面的提示信息
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
        
		// 垃圾箱布局显示动画
		showAnim = new TranslateAnimation( Animation.RELATIVE_TO_SELF,0.0f
				, Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 1.0f
				, Animation.RELATIVE_TO_SELF, 0.0f);
		showAnim.setDuration(300);
		// 垃圾箱布局隐藏动画
		hideAnim = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 0.0f
				, Animation.RELATIVE_TO_SELF, 1.0f);
		hideAnim.setDuration(200);
		
		return rootView;
	}

	/**
	 * Fragment销毁时的回调函数
	 */
	@Override
	public void onDestroy() {  
        super.onDestroy();  
        mDbMgr.closeDB();  
    }  

	/**
	 * Fragment销毁时的回调函数
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
     *  蓝牙设备列表点击处理
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	String address;
            
        	// 获取设备蓝牙地址
			TextView nameView = (TextView)v.findViewById(R.id.info);
			String info = nameView.getText().toString();
			if (info.length() > 17) {
				address = info.substring(info.length() - 17);
			}
			else {
				return;
			}
			
			// 如果当前已经有设备连接，不允许发起新连接
            if (mDevConnected) {
            	if(!mWorkingDevice.getAddress().equals(address)) {
    				Toast.makeText(getActivity(), CommonDefine.DISPINFO_CONNECTED_STATUS, Toast.LENGTH_SHORT).show();
    				return;
            	}
            }			
            
            // 根据蓝牙地址获取设备对象
            mWorkingDevice = getSSDByAddr(address);
            if (mWorkingDevice == null) {
            	Log.e(TAG, "select a disk(" + address + ") on listview, but it does not exist in device array");
            }

            if(!mWorkingDevice.isConnected()) {
            	// 设备未连接，发起连接
                Intent selectDeviceIntent = new Intent(CommonDefine.ACTION_SELECTED_DEVICE);
    			selectDeviceIntent.putExtra(CommonDefine.DEVICE, address);
    			selectDeviceIntent.putExtra(CommonDefine.OWNER, mOwner);
    			getActivity().sendBroadcast(selectDeviceIntent);

    			nameView.setText("设备连接中......");
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
            	// 设备已连接、未绑定，发起绑定
                Intent serverIntent = new Intent(getActivity(), BindActionActivity.class);
                serverIntent.putExtra(CommonDefine.SSDDEV, mWorkingDevice);
                serverIntent.putExtra(CommonDefine.BINDFLAG, true);
                startActivityForResult(serverIntent, CommonDefine.REQUEST_BIND_DISK); 	
            }
            
            if(mWorkingDevice.isConnected() && mWorkingDevice.isBinded()) {
            	// 设备已连接、已绑定，开启磁盘
                Intent serverIntent = new Intent(getActivity(), RunActionActivity.class);
                startActivityForResult(serverIntent, CommonDefine.REQUEST_RUN_DISK);	
            }
            
        }
    };	
    
    /**
     *  蓝牙设备列表长按处理
     */
    private OnItemLongClickListener mDeviceLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// 获取当前所长按的view
			dragItemView = arg1;
			// 将当前view先隐藏
			dragItemView.setVisibility(View.INVISIBLE);
			
			// 生成拖拽暂态的view
			dragView = getActivity().getLayoutInflater().inflate(R.layout.device_item, null);
			
			item = (TextView)dragView.findViewById(R.id.title);
			item.setText((String)mConnectedSSDlist.get(arg2).get("title"));
			
			item = (TextView)dragView.findViewById(R.id.info);
			item.setText((String)mConnectedSSDlist.get(arg2).get("info"));

			head = (ImageView)dragView.findViewById(R.id.devimg);
			head.setBackgroundResource(R.drawable.head1);
			
			head = (ImageView)dragView.findViewById(R.id.arrowimg);
			head.setVisibility(View.INVISIBLE);
		
			// 开始拖拽
			startDrag();

			// 震动20毫秒
			vibrator.vibrate(20);
			
			// 防止多次长按时垃圾箱布局反复出现
			if (!isChoiceMode) {
				menu.setVisibility(View.VISIBLE);
				menu.startAnimation(showAnim);
			}
			isChoiceMode = true;
			
			// 记录当前拖拽中的设备索引
			index = arg2;
			
			return false;
		}
	};
	
    /**
     *  蓝牙设备列表触摸处理
     */
	private OnTouchListener mDeviceTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (dragView != null) {
				int[] menuLoc = new int[2];
				menu.getLocationOnScreen(menuLoc);
			
				switch (event.getAction()) {
				// 触摸结束（松开）
				case MotionEvent.ACTION_UP:
					stopDrag();
					
					dragItemView.setVisibility(View.VISIBLE);

					// 进入垃圾箱区域松开，触发删除流程
					if ((int)event.getRawX() > menuLoc[0] && (int)event.getRawY() > menuLoc[1]) {
						doDelDevice();
					}
					menu.startAnimation(hideAnim);
					menu.setVisibility(View.GONE);
					isChoiceMode = false;	

					break;
				
				// 触摸移动
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
			
			// 触摸开始，记录坐标
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				oldX = (int)event.getX();
				oldY = (int)event.getY();
			}
			return false;
		}
    };
	
	/**
	 * 开始拖拽，初始化窗口和布局参数
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
	 * 拖拽过程处理
	 * @param x 拖拽对象view的新坐标x
	 * @param y 拖拽对象view的新坐标y
	 */
	private void drag(int x, int y){
		windowParams.x = x + dragItemView.getLeft() + 10;
		windowParams.y = y + dragItemView.getTop() + 100;
		windowManager.updateViewLayout(dragView, windowParams);
	}
	
	/**
	 * 停止拖拽
	 */
	private void  stopDrag() {
		if(dragView != null){
			windowManager.removeView(dragView);
			dragView = null;
		}
	}
	
	/**
	 * 删除设备对象
	 */
	private void delDev() {
        Log.d(TAG, "delDev()");
		
        Toast.makeText(getActivity(), mSSDDevices.get(index).getName() + " 设备删除成功", Toast.LENGTH_LONG).show();
		
        mDbMgr.delete(mSSDDevices.get(index));
		mConnectedSSDlist.remove(index);
		mSSDDevices.remove(index);
		
        mSSDSimpleAdapter.notifyDataSetChanged();		
	}
	
	/**
	 * 拖拽删除设备的处理入口
	 */
    private void doDelDevice() {
        Log.d(TAG, "doDelDevice()");
        
        SSDDevice ssd = mSSDDevices.get(index);
        Log.d(TAG, "remove dev " + ssd.getName() + " (" + ssd.getAddress() + 
        		"), conn: " + ssd.isConnected() + " bind: " + ssd.isBinded());
        
        if(!ssd.isConnected()) {
        	// 未连接的设备直接删除
        	delDev();
        	return;
        }

        if(ssd.isConnected() && !ssd.isBinded()) {
        	// 已连接、未绑定的设备，先发起断链，再直接删除
        	doDisconnDev(mSSDDevices.get(index).getAddress());
        	delDev();
        	return;
        }
        
        if(ssd.isConnected() && ssd.isBinded()) {
        	// 已连接、已绑定的设备，先发起取消绑定、再断链、最后删除
            Intent serverIntent = new Intent(getActivity(), BindActionActivity.class);
            serverIntent.putExtra(CommonDefine.SSDDEV, ssd);
            serverIntent.putExtra(CommonDefine.BINDFLAG, false);
            startActivityForResult(serverIntent, CommonDefine.REQUEST_BIND_DISK);        	
        }
		
    }	
    
    /**
     * 解绑设备的预先处理
     * @param data BindActionActivity返回的数据
     */
    private void preUnbindDevice(Intent data) {
        Log.d(TAG, "preUnbindDevice()");
    	
        // 从BindActionActivity获取到的密码参数
    	String pwdA = data.getExtras().getString(CommonDefine.PWD_A);
    	String pwdB = data.getExtras().getString(CommonDefine.PWD_B);
    	
    	if (pwdB == null || "".equals(pwdB)) {
    		pwdB = pwdA;
    	}
    	
    	// 构造解绑的AT命令发给设备
    	String[] paras = {CommonDefine.AT_PARA_UNBIND, pwdA, pwdB, mBtAdapter.getAddress(), mIMEI};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_DEV_BIND, paras);
        atMsg.setAddress(mSSDDevices.get(index).getAddress());
        
        Log.d(TAG, "unbind msg: " + atMsg.genAtCmd());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// 等待响应
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
     * 绑定设备的预先处理
     * @param data BindActionActivity返回的数据
     */    
    private void preBindDevice(Intent data) {
        Log.d(TAG, "preBindDevice()");
    	
        // 从BindActionActivity获取到的密码参数
    	String pwdA = data.getExtras().getString(CommonDefine.PWD_A);
    	String pwdB = data.getExtras().getString(CommonDefine.PWD_B);
    	
    	if (pwdB == null || pwdB.length() == 0) {
    		pwdB = pwdA;
    	}
    	    	
    	// 构造绑定的AT命令发给设备
    	String[] paras = {CommonDefine.AT_PARA_BIND, pwdA, pwdB, mBtAdapter.getAddress(), mIMEI};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_DEV_BIND, paras);
        atMsg.setAddress(mWorkingDevice.getAddress());
        
        Log.d(TAG, "bind msg: " + atMsg.genAtCmd());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// 等待响应
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
     * 开启设备的预处理
     * @param data BindActionActivity返回的数据
     */
    private void preRunDevice(Intent data) {
        Log.d(TAG, "preRunDevice()");
    	
        // 从BindActionActivity获取到的密码参数
    	String pwd = data.getExtras().getString(CommonDefine.PWD_A);
    	
    	// 构造开启的AT命令发给设备
    	String[] paras = {pwd, mBtAdapter.getAddress(), mIMEI};
    	
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_OPEN_SSD, paras);
        atMsg.setAddress(mWorkingDevice.getAddress());
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
		
		// 等待响应
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
     * 绑定设备的后处理
     * @param data 绑定设的备AT命令响应
     */
    private void postBindDevice(ATCmdBean data) {
        Log.d(TAG, "postBindDevice()");
        
        // 更新设备的绑定状态
        mWorkingDevice.setBinded(true);
        
        for(SSDDevice dev : mSSDDevices) {
        	if (dev.getAddress() == mWorkingDevice.getAddress()) {
        		dev.setBinded(true);
        		break;
        	}
        }
        
        // 绑定后更新设备列表界面的标志图案
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
     * 解绑设备的后处理
     * @param data 解绑设备的AT命令响应
     */
    private void postUnbindDevice(ATCmdBean data) {
        Log.d(TAG, "postUnbindDevice()");
        Toast.makeText(getActivity(), mSSDDevices.get(index).getName() + " 设备解除绑定成功", Toast.LENGTH_LONG).show();
        
        // 更新绑定状态，删除设备对象
        mSSDDevices.get(index).setBinded(false);
        doDelDevice();
    }
    
    /**
     * 开启设备的后处理
     * @param data 开启设备的AT命令响应
     */
    private void postRunDevice(ATCmdBean data) {
        Log.d(TAG, "postRunDevice()");
        
        // 更新设备的绑定信息（终端蓝牙MAC和IMEI）
        mWorkingDevice.setMac(mBtAdapter.getAddress());
        mWorkingDevice.setSn(mIMEI);

        // 进入设备的参数配置界面Activity
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        intent.putExtra(CommonDefine.SSDDEV, mWorkingDevice);
        startActivity(intent);
    }
    
    /** 
     * 启动Activity返回响应的回调方法
     */
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        
        switch (requestCode) {
        // 绑定/解绑设备，调用相应的后处理方法
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
        // 开启设备，调用后处理方法
        case CommonDefine.REQUEST_RUN_DISK:
            if (resultCode == Activity.RESULT_OK) {
            	preRunDevice(data);
            }
            break;
        }
    }

    /**
     * Fragment界面恢复的回调方法
     */
	@Override
	public void onResume() {
        Log.d(TAG, "onResume()");
		super.onResume();
		
		updateBackground();
	}
	
	/**
	 * 根据加密盘设备对象获取到设备基本信息map
	 * @param dev 加密盘设备对象
	 * @return 设备基本信息map
	 */
	private HashMap getDeviceInfoMap(SSDDevice dev) {
        HashMap map = new HashMap<String, Object>();
        map.put("title", dev.getName());
        map.put("info", "设备名: " + dev.getDevName() + "\n蓝牙地址: " + dev.getAddress());
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
	 * 根据列表个数更新背景图
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
	 * 新增一个加密盘设备（更新界面列表、数据库）
	 * @param dev 加密盘设备对象
	 */
	private void addDeviceInfo(SSDDevice dev) {
		HashMap map = getDeviceInfoMap(dev);

		// 新设备修改标志图
		map.remove("flagimg");
        map.put("flagimg", R.drawable.plugin_new);
		
        mConnectedSSDlist.add(map);
        mSSDDevices.add(dev);
        mDbMgr.add(dev);

        mSSDSimpleAdapter.notifyDataSetChanged();	
	}
	
	/**
	 * 更新加密盘设备信息
	 * @param dev 加密盘设备对象
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
	 * 蓝牙发现和连接后识别出一个新的加密盘设备（由ScanDeviceSectionFragment通过依附的Activity回调通知过来）
	 * @param device
	 */
	public void addNewDevice(SSDDevice device) {
		Log.d(TAG, "a runcore SSD device connected: " + device.getName());

		SSDDevice devInDb = mDbMgr.query(device.getAddress());
		
		// 增加新的加密盘设备
		if (devInDb == null) {
			Log.d(TAG, "new device.");
			addDeviceInfo(device);
		}
		else {
			// 已知的加密盘设备，更新设备信息
			Log.d(TAG, "old device.");
			updateDeviceInfo(device);			
		}
		
		updateBackground();
        mTips.setText(R.string.binded_tips_bind);
	}

	/**
	 * 设备的蓝牙连接丢失处理
	 * @param address 设备的蓝牙地址
	 */
	public void notifyConnLost(String address) {
		
		SSDDevice dev = getSSDByAddr(address);
		
		// 更新设备状态，提示信息
		if (dev != null) {
			dev.setConnected(false);
			updateDeviceInfo(dev);
			
			Toast.makeText(getActivity(), dev.getName() + " 的蓝牙连接丢失，请检查设备状态", Toast.LENGTH_LONG).show();
		}
		else {
			Log.e(TAG, "device address " + address + " conn lost, but the device not exists yet");
		}
	}
	
    /**
     * 查询设备信息预处理
     * @param address 设备的蓝牙地址
     */
    private void preGetDeviceInfo(String address) {
        Log.d(TAG, "preGetDeviceInfo()");
        
        // 构造查询设备信息的AT命令发送给设备
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_GET_INFO);
        atMsg.setAddress(address);
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
    }
    
    /**
     * 查询设备信息后处理
     * @param atGetInfo 查询设备信息的AT命令响应
     */
    private void postGetDeviceInfo(ATCmdBean atGetInfo) {
        Log.d(TAG, "postGetDeviceInfo()");
        
        // 根据AT命令响应更新设备信息
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
     * 主动断开与蓝牙设备的连接
     * @param address 设备的蓝牙地址
     */
    private void doDisconnDev(String address) {
        Log.d(TAG, "doDisconnDev()");

        Intent disconnIntent = new Intent(CommonDefine.ACTION_DISCONNECT_DEVICE);
        disconnIntent.putExtra(CommonDefine.DEVICE, address);
		getActivity().sendBroadcast(disconnIntent);
		
        mDevConnected = false;
    }
    
    /**
     * 蓝牙串口通信service广播消息的接收器
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
				
				// 查询设备信息响应
				if(data.getCmdCode() == CommonDefine.CMDCODE_GET_INFO) {
	            	String name = mWorkingDevice.getName();
					
	            	if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
						Toast.makeText(getActivity(), name + " 蓝牙连接成功", Toast.LENGTH_SHORT).show();
						postGetDeviceInfo(data);           		
					}
	            	else {
	            		Log.e(TAG, "查询 " + mWorkingDevice.getDevName() + " 设备信息失败，错误码：" + data.getAckCode());
						Toast.makeText(getActivity(), name + " 查询设备基本信息失败，请检查设备状态", Toast.LENGTH_SHORT).show();
						doDisconnDev(data.getAddress());
					}
				}
				
				// 绑定设备响应
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
	            		Log.e(TAG, "绑定 " + name + " 设备失败，错误码：" + data.getAckCode());
	            		if (data.getAckCode() == CommonDefine.ACKCODE_ERR_BIND_DEV) {
	            			Toast.makeText(getActivity(), name + " 已被其他终端绑定", Toast.LENGTH_LONG).show();
	            		}
	            		else if (data.getAckCode() == CommonDefine.ACKCODE_ERR_PASSWD) {
	            			Toast.makeText(getActivity(), name + " 运行密码错误", Toast.LENGTH_LONG).show();
	            		}
					}
				}
				
				// 开启加密盘响应
				else if (data.getCmdCode() == CommonDefine.CMDCODE_OPEN_SSD) {
	            	String name = mBtAdapter.getRemoteDevice(data.getAddress()).getName();
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}
	            	
	            	if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
						postRunDevice(data);
					}
	            	else {
	            		Log.e(TAG, "开启" + name + " 设备失败，错误码：" + data.getAckCode());
	            		if (data.getAckCode() == CommonDefine.ACKCODE_ERR_BIND_DEV) {
	            			Toast.makeText(getActivity(), name + " 已被其他终端绑定", Toast.LENGTH_SHORT).show();
	            		}
	            		else if (data.getAckCode() == CommonDefine.ACKCODE_ERR_PASSWD) {
	            			Toast.makeText(getActivity(), name + " 运行密码错误", Toast.LENGTH_SHORT).show();
	            		}
	            		else {
	            			Toast.makeText(getActivity(), name + " 设备异常，无法开启", Toast.LENGTH_SHORT).show();
	            		}
					}
				}
            }
            
            // 设备连接成功
            else if (CommonDefine.ACTION_CONNECT_SUCCESS.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				String owner = intent.getExtras().getString(CommonDefine.OWNER);

				if (owner.equals(mOwner)) {
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}

	                mProDialog.setMessage(getResources().getString(R.string.getinfo_diag_msg));  
					// 蓝牙连接成功后，尝试下发AT查询设备信息
			        preGetDeviceInfo(address);
			        
			        mDevConnected = true;
				}
            }

            // 设备连接失败
            else if (CommonDefine.ACTION_CONNECT_FAILED.equals(action)) {
            	
				String owner = intent.getExtras().getString(CommonDefine.OWNER);
				
				if (owner.equals(mOwner)) {
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}					

					Toast.makeText(getActivity(), mWorkingDevice.getName() + " 连接失败，请检查设备状态", Toast.LENGTH_LONG).show();
					
			        mSSDSimpleAdapter.notifyDataSetChanged();
				}
            }            

            // 设备连接丢失
            else if (CommonDefine.ACTION_CONNECT_LOST.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);

				notifyConnLost(address);
		        mDevConnected = false;
				
            }
            mSSDSimpleAdapter.notifyDataSetChanged();

        }
    };
	
}
