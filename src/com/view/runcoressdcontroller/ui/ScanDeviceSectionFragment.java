/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺����ɨ���嵥����Fragment
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
import java.util.Set;

import com.view.runcoressdcontroller.R;
import com.view.runcoressdcontroller.utils.ATCmdBean;
import com.view.runcoressdcontroller.utils.CommonDefine;
import com.view.runcoressdcontroller.utils.SSDCapability;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.support.v4.app.Fragment;

/**
 * ����ɨ���嵥��fragment
 * @author allen
 */
public class ScanDeviceSectionFragment extends Fragment {
	public static final String TAG = "ScanDeviceSectionFragment";

    // ��������������嵥����XML�ж�ȡ��
	private ArrayList<SSDCapability> mDevCapabilities = null;
    
	// ����������
    private BluetoothAdapter mBtAdapter;
    // �����豸�б�������
    private SimpleAdapter mScannedDevicesSimpleAdapter;
    // �����豸������Ϣ�б�
    private List<Map<String, Object>> mDeviceMapList = new ArrayList<Map<String, Object>>(); 
    // �����豸�����б�
	private List<BluetoothDevice> mScannedDevices = new ArrayList<BluetoothDevice>();
    // ���ȶԻ���
    private ProgressDialog mProDialog;

    // AT���������Ϣ
    String mOwner = null; 
    
    // ��ǰ�Ƿ����豸�����еı�־��ͬʱֻ����һ���豸���ӣ�
    boolean mDevConnected = false;
    
    // ����������Activityͨ�ŵĻص��ӿ�
    OnNewDeviceListener mActivityCallback;
    
    TextView mTips = null;

    /**
     * ����Activity��Ҫʵ�ָýӿ�
     */
    public interface OnNewDeviceListener {
        public void onNewDeviceConnected(SSDDevice newDev);
    }

    /**
     * Fragment����Activityʱ�Ļص�����
     */
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
        	mActivityCallback = (OnNewDeviceListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNewDeviceListener");
        }
    }

	/**
	 * �չ�����
	 */
	public ScanDeviceSectionFragment() {
	}

	/**
	 * Fragment����ʱ�Ļص�����
	 */	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mOwner = this.getClass().getName();
		
        Log.d(TAG, "onCreate(): " + mOwner);
	}
	
	/**
	 * Fragment����viewʱ�Ļص�����
	 */	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        
		View rootView = inflater.inflate(R.layout.fragment_device_scan,
				container, false);

		mDevCapabilities = (ArrayList<SSDCapability>) getArguments().getSerializable(CommonDefine.FRAGMENT_ARG_DEVCAP);
		
        // ��ʼ������ɨ��İ�ť
        Button scanButton = (Button) rootView.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
                doDiscovery();
                // ����ProgressDialog����  
                mProDialog = new ProgressDialog(getActivity());  
                mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
                mProDialog.setTitle(R.string.scan_diag_title);  
                mProDialog.setMessage(getResources().getString(R.string.scan_diag_msg));  
                mProDialog.setIcon(android.R.drawable.stat_sys_data_bluetooth);  
                mProDialog.setIndeterminate(false);
                mProDialog.setCancelable(true);  
                mProDialog.setOnCancelListener(new DiscDialogListener());
                mProDialog.show(); 
            }
        });

        ListView scannedDevicesListView = (ListView) rootView.findViewById(R.id.scanned_devices);
        scannedDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// ע������Service�㲥������
		IntentFilter serviceFilter = new IntentFilter();
		serviceFilter.addAction(CommonDefine.ACTION_AT_ACK);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_SUCCESS);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_FAILED);
		serviceFilter.addAction(CommonDefine.ACTION_CONNECT_LOST);
		getActivity().registerReceiver(mServiceReceiver, serviceFilter);
        
        // ע��ϵͳ�㲥����������������������
        IntentFilter systemfilter = new IntentFilter();
        systemfilter.addAction(BluetoothDevice.ACTION_FOUND);
        systemfilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        
        getActivity().registerReceiver(mSystemReceiver, systemfilter);

        // ��ȡϵͳ����������
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // ��������Ե������豸��ʼ���豸�б�
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        Map<String, Object> map;
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
    	        map = new HashMap<String, Object>();
    	        map.put("title", device.getName());
    	        map.put("info", "������ַ: " + device.getAddress());
    	        map.put("devimg", R.drawable.ok_gray);
    	        map.put("arrowimg", R.drawable.general__shared__more);
    	        
    	        mDeviceMapList.add(map);
    	        mScannedDevices.add(device);
            }
        }

        mScannedDevicesSimpleAdapter = new SimpleAdapter(getActivity(), mDeviceMapList, R.layout.device_item, 
        		new String[]{"title", "info", "devimg", "arrowimg", "flagimg"}, 
        		new int[]{R.id.title, R.id.info, R.id.devimg, R.id.arrowimg, R.id.flagimg});
        scannedDevicesListView.setAdapter(mScannedDevicesSimpleAdapter);

        mTips = (TextView) rootView.findViewById(R.id.tips_scanned_devices);
        mTips.setText(R.string.scanned_tips_init);
		return rootView;
	}
	
	/**
	 * ����ɨ��״̬�Ի���ļ�����
	 */
    class DiscDialogListener implements DialogInterface.OnCancelListener {  
        @Override  
        public void onCancel(DialogInterface dialog) {
            dialog.cancel(); 
	        if (mBtAdapter.isDiscovering()) {
	            mBtAdapter.cancelDiscovery();
	        }
        }  
    } 

    /**
     * �����������֣�������
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // ��������е��豸�嵥
        mDeviceMapList.clear();
        mScannedDevices.clear();
        mScannedDevicesSimpleAdapter.notifyDataSetChanged();
        
        mBtAdapter.startDiscovery();
    }
    
    /**
     * ��ѯ�豸��Ϣ
     * @param address �����豸��ַ
     */
    private void doGetDeviceInfo(String address) {
        Log.d(TAG, "doGetDeviceInfo()");
        
        ATCmdBean atMsg = new ATCmdBean(mOwner, CommonDefine.CMDCODE_GET_INFO);
        atMsg.setAddress(address);
        
        Intent getDevInfoIntent = new Intent(CommonDefine.ACTION_AT_CMD);
        getDevInfoIntent.putExtra(CommonDefine.MSG, atMsg);
		getActivity().sendBroadcast(getDevInfoIntent);
    }

    /**
     * �����µ�Դ�Ƽ������豸����ͨ��Activity֪ͨBindedDeviceSectionFragment
     * @param atGetInfo ��ѯ�豸��Ϣ��AT��Ӧ
     */
    private void doNotifyNewDevConn(ATCmdBean atGetInfo) {
        Log.d(TAG, "doNotifyNewDevConn()");
        
        String[] devInfos = atGetInfo.getDevInfo();
        
        // ����AT��Ӧ�����µļ������豸����
        SSDDevice ssd = new SSDDevice();

        ssd.setAddress(atGetInfo.getAddress());
        ssd.setDevName(mBtAdapter.getRemoteDevice(ssd.getAddress()).getName());
        
        ssd.setVid(devInfos[0]);
        ssd.setPid(devInfos[1]);
        if (devInfos[2].equals("1")) {
            ssd.setBinded(true);
        } else {
        	ssd.setBinded(false);
        }
        if (devInfos[3].equals("1")) {
            ssd.setBreakSw(true);
        } else {
        	ssd.setBreakSw(false);
        }    
        if (devInfos[4].equals("1")) {
            ssd.setEjectSw(true);
        } else {
        	ssd.setEjectSw(false);
        } 
        ssd.setMaxRetries(Integer.parseInt(devInfos[5]));
        ssd.setDftBootDisk(Integer.parseInt(devInfos[6]));
        ssd.setConnected(true);

        for(SSDCapability devCap : mDevCapabilities) {
        	if (ssd.getPid().equals(devCap.getPid()) && ssd.getVid().equals(devCap.getVid())) {
        		ssd.setCapablity(devCap);
        		break;
        	}
        }

        if (ssd.getCapablity() != null) {
            ssd.setName(ssd.getCapablity().getName());
            ssd.setDiskNum(ssd.getCapablity().getDiskNum());
        }
        else {
            ssd.setName(CommonDefine.PRODUCT_NAME_UNKNOWN);
            ssd.setDiskNum(1);
        }
        
        // ͨ��Activity֪ͨBindedDeviceSectionFragment
        mActivityCallback.onNewDeviceConnected(ssd);

        // ����View
        for(Map<String, Object> map : mDeviceMapList) {
        	String info = (String)map.get("info");
        	if (info.indexOf(atGetInfo.getAddress()) > 0) {
        		map.remove("flagimg");
        		map.remove("devimg");
        		map.put("devimg", R.drawable.ok2);
        		break;
        	}
        }				
        
        mScannedDevicesSimpleAdapter.notifyDataSetChanged();        

		Toast.makeText(getActivity(), ssd.getDevName() + "���ӳɹ���ʶ��Ϊ�Ϸ��ļ����̣��ͺţ�" + ssd.getName(), Toast.LENGTH_LONG).show();
    }

    /**
     * �����Ͽ��������豸������
     * @param address �����豸��ַ
     */
    private void doDisconnDev(String address) {
        Log.d(TAG, "doDisconnDev()");

        Intent disconnIntent = new Intent(CommonDefine.ACTION_DISCONNECT_DEVICE);
        disconnIntent.putExtra(CommonDefine.DEVICE, address);
		getActivity().sendBroadcast(disconnIntent);
    }
    
    /**
     *  �����豸�б�������
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();
            
			// �����ǰ�Ѿ����豸���ӣ���������������
            if (mDevConnected) {
				Toast.makeText(getActivity(), CommonDefine.DISPINFO_CONNECTED_STATUS, Toast.LENGTH_SHORT).show();
				return;
            }
            
			TextView nameView = (TextView)v.findViewById(R.id.info);
			String info = nameView.getText().toString();
            String address = info.substring(info.length() - 17);

            // ������豸����������
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
    };

    /**
     *  ����ϵͳ�㲥���������������豸�����������¼���
     */
    private final BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
	        Map<String, Object> map;

            // ����һ�������豸
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "find a new device " + device.getName() + ", mac " + device.getAddress());
                
                if (device.getName() != null) {
	            	// ÿɨ�赽һ�������豸�������б�
	                map = new HashMap<String, Object>();
	    	        map.put("title", device.getName());
	    	        map.put("info", "������ַ: " + device.getAddress());
	    	        map.put("devimg", R.drawable.ok2);
	    	        map.put("arrowimg", R.drawable.general__shared__more);
	    	        map.put("flagimg", R.drawable.plugin_new);
	    	        
	    	        mDeviceMapList.add(map);
	    	        mScannedDevices.add(device);
	    	        mScannedDevicesSimpleAdapter.notifyDataSetChanged();
                }
            } 
            // ������������
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	mProDialog.cancel();
            	
            	// ����ɨ������󣬰�ʣ������Ե�δ��ɨ�跶Χ�ڵ��豸Ҳ�г���
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                    	if (mScannedDevices.contains(device) == false) {
                	        map = new HashMap<String, Object>();
                	        map.put("title", device.getName());
                	        map.put("info", "������ַ: " + device.getAddress());
                	        map.put("devimg", R.drawable.ok_gray);
                	        map.put("arrowimg", R.drawable.general__shared__more);
                	        
                	        mDeviceMapList.add(map);
                	        mScannedDevices.add(device);
                    	}
                    }
                }
    	        mScannedDevicesSimpleAdapter.notifyDataSetChanged();
    	        mTips.setText(R.string.scanned_tips_conn);

            }
        }
    };

    /**
     *  ��������ͨ��service�㲥���������������ӳɹ�/ʧ��/��ʧ��AT������Ӧ���¼���
     */
    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // AT������Ӧ
            if (CommonDefine.ACTION_AT_ACK.equals(action)) {
            	
				ATCmdBean data = (ATCmdBean)intent.getSerializableExtra(CommonDefine.MSG);
            	Log.d(TAG, "recv at cmd: " + data.getCmdCode() + ", ack: " + data.getAckCode());
				
				if(!data.getOwner().equals(mOwner)) {
	            	Log.d(TAG, "msg owner:" + data.getOwner() + ", my owner " + mOwner);
					return;
				}

				if (mProDialog != null && mProDialog.isShowing()) {
                	mProDialog.cancel();
            	}					
				
				if(data.getCmdCode() == CommonDefine.CMDCODE_GET_INFO) {
	            	String name = mBtAdapter.getRemoteDevice(data.getAddress()).getName();
					
	            	if (data.getAckCode() == CommonDefine.ACKCODE_OK) {
						doNotifyNewDevConn(data);
					}
	            	else {
	            		Log.e(TAG, "��ѯ " + name + " �豸��Ϣʧ�ܣ������룺" + data.getAckCode());
						Toast.makeText(getActivity(), name + " ���ǺϷ���Դ���豸", Toast.LENGTH_SHORT).show();
						doDisconnDev(data.getAddress());
						mDevConnected = false;
						
					}
				}
            }

            // ���ӳɹ�
            else if (CommonDefine.ACTION_CONNECT_SUCCESS.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				String owner = intent.getExtras().getString(CommonDefine.OWNER);
				
				if (owner.equals(mOwner)) {
				
	                mProDialog.setMessage(getResources().getString(R.string.getinfo_diag_msg));  
	            	
			        // ����View
			        for(Map<String, Object> map : mDeviceMapList) {
			        	String info = (String)map.get("info");
			        	if (info.indexOf(address) > 0) {
			        		map.remove("flagimg");
			        		map.remove("devimg");
			        		map.put("devimg", R.drawable.ok2);
			        		break;
			        	}
			        }				
			        
					mDevConnected = true;
			        
					// �������ӳɹ��󣬳����·�AT��ѯ�豸��Ϣ
			        doGetDeviceInfo(address);
				}
            }

            // ����ʧ��
            else if (CommonDefine.ACTION_CONNECT_FAILED.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
				String owner = intent.getExtras().getString(CommonDefine.OWNER);
				
				if (owner.equals(mOwner)) {
					
	            	String name = mBtAdapter.getRemoteDevice(address).getName();
	            	if (mProDialog != null && mProDialog.isShowing()) {
	                	mProDialog.cancel();
	            	}					

	            	Toast.makeText(getActivity(), name + " ����ʧ��", Toast.LENGTH_SHORT).show();
				}
			}
            
            // ���Ӷ�ʧ
            else if (CommonDefine.ACTION_CONNECT_LOST.equals(action)) {
				String address = intent.getExtras().getString(CommonDefine.DEVICE);
            	String name = mBtAdapter.getRemoteDevice(address).getName();

                // ����View
                for(Map<String, Object> map : mDeviceMapList) {
                	String info = (String)map.get("info");
                	if (info.indexOf(address) > 0) {
                		map.remove("flagimg");
                		map.remove("devimg");
                		map.put("devimg", R.drawable.ok2);
                		break;
                	}
                }
				mDevConnected = false;

            	Toast.makeText(getActivity(), name + " ���Ӷ�ʧ", Toast.LENGTH_SHORT).show();
            }
	        mScannedDevicesSimpleAdapter.notifyDataSetChanged();

        }
    };
    
}
