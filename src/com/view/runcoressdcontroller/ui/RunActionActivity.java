/*
 * Copyright (C) 2012-2015 View Info Tech Ltd.
 * 
 * ģ�飺����Ӳ�̵������������Activity
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
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * ����Ӳ�̵������������Activity
 * @author allen
 */
public class RunActionActivity extends Activity {

	Button bindBtn = null;
	EditText runPwdEditText = null;
	
	/**
	 * Activity�����Ļص�����
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ���ý��洰���ޱ�����
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_run_action);
		
        setResult(Activity.RESULT_CANCELED);
		
		runPwdEditText = (EditText)findViewById(R.id.editTextDiskPwd);
        
		bindBtn = (Button)findViewById(R.id.buttonRun);
		bindBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String pwdA = runPwdEditText.getText().toString().trim();
				
				// �жϿؼ���������ĺϷ���
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

	            // ������������Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
		});
		
	}

}
