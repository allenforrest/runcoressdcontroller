/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：加密盘参数设置界面Activity
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.ui;

import java.util.Locale;

import com.view.runcoressdcontroller.R;
import com.view.runcoressdcontroller.utils.CommonDefine;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 参数设置Activity
 * @author allen	
 */
public class SettingActivity extends FragmentActivity {

	// 将两个Fragment作为两个section页显示的适配器
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	// 当前的加密盘设备对象
    SSDDevice mWorkingDev = null;
    
    /**
     * Activity创建时的回调方法
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_setting);
		
		// 创建一个支持多个Fragment的section页适配器
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// 创建ViewPager对象
		mViewPager = (ViewPager) findViewById(R.id.pager_settings);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// 设置初始页面位置（默认为被动防御配置页面）
		mViewPager.setCurrentItem(CommonDefine.SECTION_FUNC_SW);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		// 从上一级Activity（BindedDeviceSectionFragment）获取当前加密盘设备对象
		Intent intent = getIntent();
		mWorkingDev = (SSDDevice)intent.getSerializableExtra(CommonDefine.SSDDEV);
		
		ImageView headImg = (ImageView)findViewById(R.id.settings_dev_img);

		TextView headTitle = (TextView)findViewById(R.id.settings_dev_title);
		TextView headInfo = (TextView)findViewById(R.id.settings_dev_info);
		
		headTitle.setText(mWorkingDev.getName());
		headInfo.setText("设备：" + mWorkingDev.getDevName() + "\n蓝牙地址：" + mWorkingDev.getAddress());
		
        if (mWorkingDev.getName().indexOf(CommonDefine.PRODUCT_AEGIS) >= 0) {
    		headImg.setImageResource(R.drawable.bookshelf__menu_normal);
        }
        else if (mWorkingDev.getName().indexOf(CommonDefine.PRODUCT_TAIJI) >= 0) {
    		headImg.setImageResource(R.drawable.bookshelf__menu_normal);
        }
        else {
    		headImg.setImageResource(R.drawable.bookshelf__menu_normal);
        }
	}

	/**
	 * 页面切换事件的监听器
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			TextView headTitle = null;
			
			// 根据切换的页面更新界面顶部的标题栏
			switch (arg0) {
			case CommonDefine.SECTION_PASSWORD:
				headTitle = (TextView)SettingActivity.this.findViewById(R.id.title_settings);
				headTitle.setText(R.string.title_settings_pwd);
				break;

			case CommonDefine.SECTION_FUNC_SW:
				headTitle = (TextView)SettingActivity.this.findViewById(R.id.title_settings);
				headTitle.setText(R.string.title_settings_sw);

				break;

			case CommonDefine.SECTION_KILL_HD:
				headTitle = (TextView)SettingActivity.this.findViewById(R.id.title_settings);
				headTitle.setText(R.string.title_settings_kill);

				break;
				
			}
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
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
			
			if (position == CommonDefine.SECTION_PASSWORD) {
				fragment = new SettingsPwdSectionFragment();
			}
			else if (position == CommonDefine.SECTION_FUNC_SW) {
				fragment = new SettingsSwitchSectionFragment();
			}
			else {
				fragment = new SettingsKillSectionFragment();
			}

			Bundle args = new Bundle();
			args.putSerializable(CommonDefine.FRAGMENT_ARG_SSDDEV, mWorkingDev);
			fragment.setArguments(args);
			return fragment;
		}

		/**
		 * 获取Fragment个数（2个）
		 */
		@Override
		public int getCount() {
			return 3;
		}

		/**
		 * 根据位置索引获取页面标题
		 */		
		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_settings_pwd).toUpperCase(l);
			case 1:
				return getString(R.string.title_settings_sw).toUpperCase(l);
			case 2:
				return getString(R.string.title_settings_kill).toUpperCase(l);
			}
			return null;
		}
	}
}
