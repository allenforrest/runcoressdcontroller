/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：绑定/解绑等密码输入界面Activity
 * 工程：源科加密SSD安全控制APP 
 * 
 * 作者： Allen Xu
 * 版本：1.3
 * 创建日期：2013-07-25
 * 
 */

package com.view.runcoressdcontroller.ui;

import com.view.runcoressdcontroller.R;
import com.view.runcoressdcontroller.utils.CommonDefine;
import com.view.runcoressdcontroller.utils.SSDDevice;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 绑定/解绑/开启等密码输入界面Activity
 * @author allen
 */
public class BindActionActivity extends Activity {

	Button bindBtn = null;
	TextView bindTitleText = null;
	EditText bindPwdAEditText = null;
	EditText bindPwdBEditText = null;
	SSDDevice dev;
	boolean bindFlag;
	
	/**
	 * Activity创建时的回调函数
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_bind_action);

        setResult(Activity.RESULT_CANCELED);

        // 输入密码的控件
		bindPwdAEditText = (EditText)findViewById(R.id.editTextDiskAPwd);
		bindPwdBEditText = (EditText)findViewById(R.id.editTextDiskBPwd);
		bindTitleText = (TextView)findViewById(R.id.bind_action_title);
		
		// 根据intent中携带的类型决定实现的功能
        Intent data = getIntent();
        dev = (SSDDevice)data.getSerializableExtra(CommonDefine.SSDDEV);
        bindFlag = data.getExtras().getBoolean(CommonDefine.BINDFLAG);
        
        // 绑定和解绑显示不同的标题
        if(bindFlag) {
        	bindTitleText.setText(R.string.title_bind_diag_title);
        } else {
        	bindTitleText.setText(R.string.title_unbind_diag_title);
        }
        
        // 单盘只显示一个密码输入框
        if (dev.getDiskNum() == 1) {
        	bindPwdBEditText.setVisibility(View.GONE);
            bindPwdAEditText.setHint(R.string.disk_pwd_hint);
        }
        
        // 按钮点击处理
		bindBtn = (Button)findViewById(R.id.buttonBind);
		bindBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String pwdA = bindPwdAEditText.getText().toString().trim();
				String pwdB = bindPwdBEditText.getText().toString().trim();
				
				// 判断控件输入参数的合法性
				if ((dev.getDiskNum() == 1 && "".equals(pwdA)) ||
					(dev.getDiskNum() == 2 && ("".equals(pwdA) || "".equals(pwdB)))) {
	            	Toast.makeText(BindActionActivity.this, CommonDefine.DISPINFO_PWD_NULL, Toast.LENGTH_SHORT).show();
	            	return;
				}
				
				if (pwdA.indexOf(",") >= 0 || pwdB.indexOf(",") >= 0) {
	            	Toast.makeText(BindActionActivity.this, CommonDefine.DISPINFO_PWD_INVALID, Toast.LENGTH_SHORT).show();
	            	return;
				}
				
				if (dev.getDiskNum() == 2 && pwdA.equals(pwdB)) {
	            	Toast.makeText(BindActionActivity.this, CommonDefine.DISPINFO_PWD_CONFLICT, Toast.LENGTH_SHORT).show();
					return;
				}
				
	            Intent intent = new Intent();
	            intent.putExtra(CommonDefine.BINDFLAG, bindFlag);
	            intent.putExtra(CommonDefine.PWD_A, pwdA);
	            intent.putExtra(CommonDefine.PWD_B, pwdB);
	            
	            // 保存结果，结束Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
		});
		
	}

}
