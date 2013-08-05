/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：设备面板界面Activity（包含BindedDeviceSection和ScanDeviceSection两个Fragment）
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
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
 * 设备面板Activity
 * @author allen	
 */
@SuppressLint("DefaultLocale")
public class DeviceActivity extends FragmentActivity 
	implements ScanDeviceSectionFragment.OnNewDeviceListener {
	private static final String TAG = "DeviceActivity";

	// 将两个Fragment作为两个section页显示的适配器
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	
	// 源科设备规格能力列表
	private ArrayList<SSDCapability> mDevCapabilities = new ArrayList<SSDCapability>();
	private SSDCapability mDevCap = null;

	// 用于连续按返回键退出的判定时间
    private long mExitTime = 0;    
	
    /**
     * Activity创建时的回调方法
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_device);

		// 创建一个支持多个Fragment的section页适配器
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// 创建ViewPager对象
		mViewPager = (ViewPager) findViewById(R.id.pager_devices);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// 设置各个page之间的切换风格（缩放式样）
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
		
		// 解析设备能力表XML
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
     * 获取XML文件各层次根元素的解析结果
     * @return 返回各层次解析完成的根元素对象 
     */  
    private RootElement getRootElement(){  
          
        // rootElement代表着根节点，参数为根节点的tagName  
        RootElement rootElement = new RootElement("devices");  

        Element deviceElement = rootElement.getChild("device");  
        // 读到元素开始位置时触发，如读到<device>时  
        deviceElement.setStartElementListener(new StartElementListener() {  
            @Override  
            public void start(Attributes attributes) {  
                mDevCap = new SSDCapability();  
            }  
        });  
        
        // 读到元素结束位置时触发，如读到</device>时  
        deviceElement.setEndElementListener(new EndElementListener() {  
            @Override  
            public void end() {
            	mDevCapabilities.add(mDevCap);  
            }  
        });  
        
        // 解析name节点
        Element nameElement = deviceElement.getChild("name");  
        nameElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {
            	mDevCap.setName(body);  
            }  
        });  
          
        // 解析pid节点
        Element pidElement = deviceElement.getChild("pid");
        pidElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) { 
            	mDevCap.setPid(body);  
            }  
        }); 
        
        // 解析vid节点
        Element vidElement = deviceElement.getChild("vid");
        vidElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {
            	mDevCap.setVid(body);  
            }  
        }); 
        
        // 解析disk_num节点
        Element disNumElement = deviceElement.getChild("disk_num");
        disNumElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) { 
            	mDevCap.setDiskNum(Integer.parseInt(body));  
            }  
        }); 
        
        // 解析support_bt节点
        Element btElement = deviceElement.getChild("support_bt");
        btElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportBT(Boolean.parseBoolean(body));  
            }  
        });
        
        // 解析support_runpwd节点
        Element runPwdElement = deviceElement.getChild("support_runpwd");
        runPwdElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportRunPwd(Boolean.parseBoolean(body));  
            }  
        }); 
        
        // 解析support_killpwd节点
        Element killPwdElement = deviceElement.getChild("support_killpwd");
        killPwdElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportKillPwd(Boolean.parseBoolean(body));  
            }  
        });  
        
        // 解析support_break节点
        Element breakElement = deviceElement.getChild("support_break");
        breakElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportAntiBreak(Boolean.parseBoolean(body));  
            }  
        });  
        
        // 解析support_eject节点
        Element ejectElement = deviceElement.getChild("support_eject");
        ejectElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportAntiEject(Boolean.parseBoolean(body));  
            }  
        });  
        
        // 解析support_suicide节点
        Element suicideElement = deviceElement.getChild("support_suicide");
        suicideElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportSuicide(Boolean.parseBoolean(body));  
            }  
        });

        // 解析support_btkill节点
        Element btKillElement = deviceElement.getChild("support_btkill");
        btKillElement.setEndTextElementListener(new EndTextElementListener() {  
            @Override  
            public void end(String body) {  
            	mDevCap.setSupportBTKill(Boolean.parseBoolean(body));  
            }  
        }); 
        
        // 解析support_3gkill节点
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
     * 被ScanDeviceSectionFragment回调的新源科加密盘设备发现识别通知处理接口
     * @param newDev 新发现和识别出来的加密盘设备
     */
    @Override
	public void onNewDeviceConnected(SSDDevice newDev) {
    	BindedDeviceSectionFragment bindedFragment = 
    			(BindedDeviceSectionFragment) getSupportFragmentManager().findFragmentByTag(
    					makeFragmentName(mViewPager.getId(), CommonDefine.SECTION_BINDED_DEV));
    	if (bindedFragment != null) {
    		// 直接调用fragment的公共方法
    		bindedFragment.addNewDevice(newDev);
    	}
    	else {
    		Log.e(TAG, "new device found, but BindedDeviceSectionFragment not found!");
    	}
    }

    /**
     * 构造下属Fragment的标识tag
     * @param viewId ViewPager的ID
     * @param index Fragment在View中的位置索引
     * @return Fragment的标识tag
     */
    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }
    
    /**
     * Activity开始运行的回调方法
     */
	@Override
	protected void onStart() {
		
		Log.d(DeviceActivity.TAG, "onStart");
		// 启动蓝牙串口通信的后台service
		Intent startService = new Intent(DeviceActivity.this, BluetoothSPPService.class);
		startService(startService);
		
		super.onStart();
	}

	/**
	 * ViewPager各个页面之间的切换风格（Google提供的缩放风格）
	 */
	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
	    private static final float MIN_SCALE = 0.85f;
	    private static final float MIN_ALPHA = 0.5f;

	    @Override
		public void transformPage(View view, float position) {
	        int pageWidth = view.getWidth();
	        int pageHeight = view.getHeight();

	        if (position < -1) { // [-无穷大,-1)
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

	        } else { // (1,+无穷大]
	            view.setAlpha(0);
	        }
	    }
	    
	}
	
	/**
	 * 创建关联Section页面的Fragment对象
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * 根据位置索引获取Fragment对象实例
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
		 * 获取Fragment个数（2个）
		 */
		@Override
		public int getCount() {
			return 2;
		}

		/**
		 * 根据位置索引获取页面标题
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
	 * 按键事件处理
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
	 * 2秒内联系按两次返回键退出APP
	 */
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }	

}
