/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺�����̲������ý���Activity
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
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
 * ��������Activity
 * @author allen	
 */
public class SettingActivity extends FragmentActivity {

	// ������Fragment��Ϊ����sectionҳ��ʾ��������
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	// ��ǰ�ļ������豸����
    SSDDevice mWorkingDev = null;
    
    /**
     * Activity����ʱ�Ļص�����
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_setting);
		
		// ����һ��֧�ֶ��Fragment��sectionҳ������
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// ����ViewPager����
		mViewPager = (ViewPager) findViewById(R.id.pager_settings);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// ���ó�ʼҳ��λ�ã�Ĭ��Ϊ������������ҳ�棩
		mViewPager.setCurrentItem(CommonDefine.SECTION_FUNC_SW);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		// ����һ��Activity��BindedDeviceSectionFragment����ȡ��ǰ�������豸����
		Intent intent = getIntent();
		mWorkingDev = (SSDDevice)intent.getSerializableExtra(CommonDefine.SSDDEV);
		
		ImageView headImg = (ImageView)findViewById(R.id.settings_dev_img);

		TextView headTitle = (TextView)findViewById(R.id.settings_dev_title);
		TextView headInfo = (TextView)findViewById(R.id.settings_dev_info);
		
		headTitle.setText(mWorkingDev.getName());
		headInfo.setText("�豸��" + mWorkingDev.getDevName() + "\n������ַ��" + mWorkingDev.getAddress());
		
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
	 * ҳ���л��¼��ļ�����
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			TextView headTitle = null;
			
			// �����л���ҳ����½��涥���ı�����
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
		 * ��ȡFragment������2����
		 */
		@Override
		public int getCount() {
			return 3;
		}

		/**
		 * ����λ��������ȡҳ�����
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
