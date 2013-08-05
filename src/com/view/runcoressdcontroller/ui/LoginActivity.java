/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * 模块：登录界面Activity
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
import android.content.SharedPreferences;
import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

/**
 * 登录界面Activity
 * @author allen
 */
public class LoginActivity extends Activity implements OnEditorActionListener {
    private static final String TAG = "LoginActivity";
	
    // 登录用户名文本编辑框
    private EditText loginUserEditText;
    // 登录密码文本编辑框
	private EditText loginPwdEditText;
	// 登录按钮
	private Button loginBtn;
	
	// 用户名
	private String mUser;
	// 密码
	private String mPwd;
	
	/**
	 * Activity创建时的回调方法
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 设备界面窗口无标题显示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_login);

		loginUserEditText = (EditText)findViewById(R.id.editTextLoginUser);
		loginPwdEditText = (EditText)findViewById(R.id.editTextLoginPwd);
		loginBtn = (Button)findViewById(R.id.buttonLogin);
		
		loginPwdEditText.setOnEditorActionListener(LoginActivity.this);
		
		loginBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doLoginWithPwd(loginUserEditText.getText().toString().trim(),
						loginPwdEditText.getText().toString().trim());
			}
		});
		
		// 从首选项中读取预制的用户名和密码（TODO: 支持后续修改用户名和密码更新在首选项中，暂未实现）
		SharedPreferences settings = getSharedPreferences(CommonDefine.PREFS_NAME, 0);
		mUser = settings.getString("user", CommonDefine.DFT_LOGIN_USER);
		mPwd = settings.getString("passwd", CommonDefine.DFT_LOGIN_PWD);		
	}

	/**
	 * 文本编辑框内输入时，键盘点击发送按钮的处理
	 */
	@Override  
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEND) {
			doLoginWithPwd(loginUserEditText.getText().toString().trim(),
					loginPwdEditText.getText().toString().trim());
		}
		return true;
	}

	/**
	 * 判断用户名和密码，实现登录界面切换
	 * @param user 用户输入的用户名
	 * @param password 用户输入的密码
	 */
	private void doLoginWithPwd(String user, String password) {
		if (!mUser.equals(user) || !mPwd.equals(password)) {
			
			Toast.makeText(LoginActivity.this, CommonDefine.DISPINFO_LOGIN_PWD_ERROR, Toast.LENGTH_SHORT).show();
			Log.e(TAG, "login failed, password error!");
			loginPwdEditText.setText("");
			
		} else {
			// 登录正确，切换到设备面板界面（DeviceActivity）
			Log.d(TAG, "login succ!");
			Intent devIntent = new Intent(LoginActivity.this, DeviceActivity.class);
			devIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(devIntent);
			
			// 本登录界面Activity结束销毁，即从DeviceActivity无法返回到这里，会直接退出APP
			LoginActivity.this.finish();
		}
	}

}
