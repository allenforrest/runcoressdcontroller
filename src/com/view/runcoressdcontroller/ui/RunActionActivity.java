/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：开启硬盘等密码输入界面Activity
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

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 开启硬盘等密码输入界面Activity
 * @author allen
 */
public class RunActionActivity extends Activity {

	Button bindBtn = null;
	EditText runPwdEditText = null;
	
	/**
	 * Activity创建的回调方法
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 设置界面窗口无标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_run_action);
		
        setResult(Activity.RESULT_CANCELED);
		
		runPwdEditText = (EditText)findViewById(R.id.editTextDiskPwd);
        
		bindBtn = (Button)findViewById(R.id.buttonRun);
		bindBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String pwdA = runPwdEditText.getText().toString().trim();
				
				// 判断控件输入参数的合法性
				if ("".equals(pwdA)) {
	            	Toast.makeText(RunActionActivity.this, CommonDefine.DISPINFO_PWD_NULL, Toast.LENGTH_SHORT).show();
	            	return;
				}
				
				if (pwdA.indexOf(",") >= 0) {
	            	Toast.makeText(RunActionActivity.this, CommonDefine.DISPINFO_PWD_INVALID, Toast.LENGTH_SHORT).show();
	            	return;
				}
				
	            Intent intent = new Intent();
	            intent.putExtra(CommonDefine.PWD_A, pwdA);

	            // 保存结果，结束Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
		});
		
	}

}
