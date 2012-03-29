package org.secmem.remoteroid.activity;

import org.secmem.remoteroid.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SetIPActivity extends Activity implements OnClickListener {

	Button okBtn;
	Button cancelBtn;
	
	EditText ipEdt;
	EditText pwdEdt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setip_activity);
		
		okBtn = (Button)findViewById(R.id.setip_btn_ok);
		cancelBtn = (Button)findViewById(R.id.setip_btn_cancel);
		
		okBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		
		ipEdt = (EditText)findViewById(R.id.setip_edt_ip);
		pwdEdt = (EditText)findViewById(R.id.setip_edt_pwd);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		
		case R.id.setip_btn_ok:
			
			break;
			
		case R.id.setip_btn_cancel:
			finish();
			break;
		}
		
	}
	
	
	
}
