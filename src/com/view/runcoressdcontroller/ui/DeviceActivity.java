/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺�豸������Activity������BindedDeviceSection��ScanDeviceSection����Fragment��
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
 * 
 */

package com.view.runcoressdcontroller.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.view.runcoressdcontroller.R;
import com.view.runcoressdcontroller.service.BluetoothSPPService;
import com.view.runcoressdcontroller.utils.CommonDefine;
import com.view.runcoressdcontroller.utils.SSDCapability;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.util.Log;

/**
 * �豸���Activity
 * @author allen	
 */
@SuppressLint("DefaultLocale")
public class DeviceActivity extends FragmentActivity 
	implements ScanDeviceSectionFragment.OnNewDeviceListener {
	private static final String TAG = "DeviceActivity";

	// ������Fragment��Ϊ����sectionҳ��ʾ��������
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	
	// Դ���豸��������б�
	private ArrayList<SSDCapability> mDevCapabilities = new ArrayList<SSDCapability>();
	private SSDCapability mDevCap = null;

	// �������������ؼ��˳����ж�ʱ��
    private long mExitTime = 0;    
	
    /**
     * Activity����ʱ�Ļص�����
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_device);

		// ����һ��֧�ֶ��Fragment��sectionҳ������
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// ����ViewPager����
		mViewPager = (ViewPager) findViewById(R.id.pager_devices);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// ���ø���page֮����л��������ʽ����
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
		
		// �����豸������XML
		try {  
            InputStream inputStream = getResources().getAssets().open("DeviceCapability.xml"); 
            SAXParserFactory factory = SAXParserFactory.newInstance();  
            SAXParser parser = factory.newSAXParser();  
            XMLReader reader = parser.getXMLReader();  
            reader.setContentHandler(getRootElement().getContentHandler());  
            reader.parse(new InputSource(inputStream));  
            Log.i(TAG, "parse DeviceCapability.xml complete");  
        } catch (Exception e) {  
            e.printStackTrace();  
        }

	}

	/** 
     * ��ȡXML�ļ�����θ�Ԫ�صĽ������
     * @return ���ظ���ν�����ɵĸ�Ԫ�ض��� 
     */  
    private RootElement getRootElement(){  
          
        // rootElement�����Ÿ��ڵ㣬����Ϊ���ڵ��tagName  
        RootElement rootElement = new RootElement("devices");  

        Element deviceElement = rootElement.getChild("device");  
        // ����Ԫ�ؿ�ʼλ��ʱ�����������<device>ʱ  
        deviceElement.setStartElementListener(new StartElementListener() {  
            @Override  
            public void start(Attributes attributes) {  
                mDevCap = new SSDCapability();  
            }  
        });  
        
        // ����Ԫ�ؽ���λ��ʱ�����������</device>ʱ  
        deviceElement.setEndElementListener(new EndElementListener() {  
            @Override  
            public void end() {
            	mDevCapabilities.add(mDevCap);  
            }  
        });  
        
        // ����name�ڵ�
        Element nameElement = deviceElement.getChild("name");  
        nameElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {
            	mDevCap.setName(body);  
            }  
        });  
          
        // ����pid�ڵ�
        Element pidElement = deviceElement.getChild("pid");
        pidElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) { 
            	mDevCap.setPid(body);  
            }  
        }); 
        
        // ����vid�ڵ�
        Element vidElement = deviceElement.getChild("vid");
        vidElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {
            	mDevCap.setVid(body);  
            }  
        }); 
        
        // ����disk_num�ڵ�
        Element disNumElement = deviceElement.getChild("disk_num");
        disNumElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) { 
            	mDevCap.setDiskNum(Integer.parseInt(body));  
            }  
        }); 
        
        // ����support_bt�ڵ�
        Element btElement = deviceElement.getChild("support_bt");
        btElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportBT(Boolean.parseBoolean(body));  
            }  
        });
        
        // ����support_runpwd�ڵ�
        Element runPwdElement = deviceElement.getChild("support_runpwd");
        runPwdElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportRunPwd(Boolean.parseBoolean(body));  
            }  
        }); 
        
        // ����support_killpwd�ڵ�
        Element killPwdElement = deviceElement.getChild("support_killpwd");
        killPwdElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportKillPwd(Boolean.parseBoolean(body));  
            }  
        });  
        
        // ����support_break�ڵ�
        Element breakElement = deviceElement.getChild("support_break");
        breakElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportAntiBreak(Boolean.parseBoolean(body));  
            }  
        });  
        
        // ����support_eject�ڵ�
        Element ejectElement = deviceElement.getChild("support_eject");
        ejectElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportAntiEject(Boolean.parseBoolean(body));  
            }  
        });  
        
        // ����support_suicide�ڵ�
        Element suicideElement = deviceElement.getChild("support_suicide");
        suicideElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportSuicide(Boolean.parseBoolean(body));  
            }  
        });

        // ����support_btkill�ڵ�
        Element btKillElement = deviceElement.getChild("support_btkill");
        btKillElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportBTKill(Boolean.parseBoolean(body));  
            }  
        }); 
        
        // ����support_3gkill�ڵ�
        Element mobKillElement = deviceElement.getChild("support_3gkill");
        mobKillElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupport3GKill(Boolean.parseBoolean(body));  
            }  
        }); 
        return rootElement;  
          
    }
    
    /**
     * ��ScanDeviceSectionFragment�ص�����Դ�Ƽ������豸����ʶ��֪ͨ����ӿ�
     * @param newDev �·��ֺ�ʶ������ļ������豸
     */
    @Override
	public void onNewDeviceConnected(SSDDevice newDev) {
    	BindedDeviceSectionFragment bindedFragment = 
    			(BindedDeviceSectionFragment) getSupportFragmentManager().findFragmentByTag(
    					makeFragmentName(mViewPager.getId(), CommonDefine.SECTION_BINDED_DEV));
    	if (bindedFragment != null) {
    		// ֱ�ӵ���fragment�Ĺ�������
    		bindedFragment.addNewDevice(newDev);
    	}
    	else {
    		Log.e(TAG, "new device found, but BindedDeviceSectionFragment not found!");
    	}
    }

    /**
     * ��������Fragment�ı�ʶtag
     * @param viewId ViewPager��ID
     * @param index Fragment��View�е�λ������
     * @return Fragment�ı�ʶtag
     */
    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }
    
    /**
     * Activity��ʼ���еĻص�����
     */
	@Override
	protected void onStart() {
		
		Log.d(DeviceActivity.TAG, "onStart");
		// ������������ͨ�ŵĺ�̨service
		Intent startService = new Intent(DeviceActivity.this, BluetoothSPPService.class);
		startService(startService);
		
		super.onStart();
	}

	/**
	 * ViewPager����ҳ��֮����л����Google�ṩ�����ŷ��
	 */
	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
	    private static final float MIN_SCALE = 0.85f;
	    private static final float MIN_ALPHA = 0.5f;

	    @Override
		public void transformPage(View view, float position) {
	        int pageWidth = view.getWidth();
	        int pageHeight = view.getHeight();

	        if (position < -1) { // [-�����,-1)
	            view.setAlpha(0);

	        } else if (position <= 1) { // [-1,1]
	            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
	            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
	            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
	            if (position < 0) {
	                view.setTranslationX(horzMargin - vertMargin / 2);
	            } else {
	                view.setTranslationX(-horzMargin + vertMargin / 2);
	            }

	            view.setScaleX(scaleFactor);
	            view.setScaleY(scaleFactor);

	            view.setAlpha(MIN_ALPHA +
	                    (scaleFactor - MIN_SCALE) /
	                    (1 - MIN_SCALE) * (1 - MIN_ALPHA));

	        } else { // (1,+�����]
	            view.setAlpha(0);
	        }
	    }
	    
	}
	
	/**
	 * ��������Sectionҳ���Fragment����
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * ����λ��������ȡFragment����ʵ��
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment fragment;
			
			if (position == CommonDefine.SECTION_BINDED_DEV) {
				fragment = new BindedDeviceSectionFragment();
			}
			else {
				fragment = new ScanDeviceSectionFragment();
			}
			
			Bundle args = new Bundle();
			args.putSerializable(CommonDefine.FRAGMENT_ARG_DEVCAP, mDevCapabilities);
			fragment.setArguments(args);
			return fragment;
		}

		/**
		 * ��ȡFragment������2����
		 */
		@Override
		public int getCount() {
			return 2;
		}

		/**
		 * ����λ��������ȡҳ�����
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			
			switch (position) {
				case CommonDefine.SECTION_BINDED_DEV:
					return getString(R.string.title_sectionBindList).toUpperCase(l);
					
				case CommonDefine.SECTION_SCAN_DEV:
					return getString(R.string.title_sectionScanList).toUpperCase(l);
			}
			return null;
		}
	}
	
	/**
	 * �����¼�����
	 */
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

	/**
	 * 2������ϵ�����η��ؼ��˳�APP
	 */
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����",
                    Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }	

}
