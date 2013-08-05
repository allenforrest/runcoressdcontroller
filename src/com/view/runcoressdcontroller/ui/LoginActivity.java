/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺��¼����Activity
 * ���̣�Դ�Ƽ���SSD��ȫ����APP 
 * 
 * ���ߣ� Allen Xu
 * �汾��1.3
 * �������ڣ�2013-07-25
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
 * ��¼����Activity
 * @author allen
 */
public class LoginActivity extends Activity implements OnEditorActionListener {
    private static final String TAG = "LoginActivity";
	
    // ��¼�û����ı��༭��
    private EditText loginUserEditText;
    // ��¼�����ı��༭��
	private EditText loginPwdEditText;
	// ��¼��ť
	private Button loginBtn;
	
	// �û���
	private String mUser;
	// ����
	private String mPwd;
	
	/**
	 * Activity����ʱ�Ļص�����
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// �豸���洰���ޱ�����ʾ
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
		
		// ����ѡ���ж�ȡԤ�Ƶ��û��������루TODO: ֧�ֺ����޸��û����������������ѡ���У���δʵ�֣�
		SharedPreferences settings = getSharedPreferences(CommonDefine.PREFS_NAME, 0);
		mUser = settings.getString("user", CommonDefine.DFT_LOGIN_USER);
		mPwd = settings.getString("passwd", CommonDefine.DFT_LOGIN_PWD);		
	}

	/**
	 * �ı��༭��������ʱ�����̵�����Ͱ�ť�Ĵ���
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
	 * �ж��û��������룬ʵ�ֵ�¼�����л�
	 * @param user �û�������û���
	 * @param password �û����������
	 */
	private void doLoginWithPwd(String user, String password) {
		if (!mUser.equals(user) || !mPwd.equals(password)) {
			
			Toast.makeText(LoginActivity.this, CommonDefine.DISPINFO_LOGIN_PWD_ERROR, Toast.LENGTH_SHORT).show();
			Log.e(TAG, "login failed, password error!");
			loginPwdEditText.setText("");
			
		} else {
			// ��¼��ȷ���л����豸�����棨DeviceActivity��
			Log.d(TAG, "login succ!");
			Intent devIntent = new Intent(LoginActivity.this, DeviceActivity.class);
			devIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(devIntent);
			
			// ����¼����Activity�������٣�����DeviceActivity�޷����ص������ֱ���˳�APP
			LoginActivity.this.finish();
		}
	}

}
