package org.secmem.remoteroid.activity;

import org.secmem.remoteroid.R;
import org.secmem.remoteroid.intent.RemoteroidIntent;
import org.secmem.remoteroid.lib.api.API;
import org.secmem.remoteroid.lib.data.Account;
import org.secmem.remoteroid.lib.data.Device;
import org.secmem.remoteroid.lib.request.Request;
import org.secmem.remoteroid.lib.request.Response;
import org.secmem.remoteroid.lib.util.DeviceUUIDGenerator;
import org.secmem.remoteroid.util.DeviceUUIDGeneratorImpl;
import org.secmem.remoteroid.util.DialogAsyncTask;
import org.secmem.remoteroid.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;

public class AccountInfoActivity extends SherlockActivity {

	private Button btnLogout;
	private TextView tvUserAccount;
	private TextView tvDeviceRegistrationStatus;
	private Button btnRegisterDevice;
	private Button btnChangeDeviceNickname;
	
	private static final String SENDER_ID = "816046818963";
	
	private BroadcastReceiver deviceRegistrationReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(RemoteroidIntent.ACTION_DEVICE_REGISTRATION_COMPLETE)){
				new GetDeviceInfoTask(AccountInfoActivity.this).setFinishOnCancel(true).execute();
			}else{
				// Registration failed
				btnRegisterDevice.setEnabled(true);
				Toast.makeText(getApplicationContext(), "Device registration failed.", Toast.LENGTH_SHORT).show();
			}
		}
		
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_account_info);
	    
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.bg_red));
	    
	    if(!Util.Connection.isUserAccountSet(getApplicationContext())){
	    	// If user did not logged-in into remoteroid account,
	    	// proceed to login screen.
	    	startActivity(new Intent(RemoteroidIntent.ACTION_LOGIN));
	    	finish();
	    	return;
	    }
	    tvDeviceRegistrationStatus = (TextView)findViewById(R.id.activity_account_info_device_registration_status);
	    
	    btnRegisterDevice = (Button)findViewById(R.id.activity_account_info_register_device);
	    btnChangeDeviceNickname = (Button)findViewById(R.id.activity_account_info_set_device_nickname);
	    
	    btnRegisterDevice.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				btnRegisterDevice.setEnabled(false);
				GCMRegistrar.checkDevice(AccountInfoActivity.this);
			    GCMRegistrar.checkManifest(AccountInfoActivity.this);
			    final String regId = GCMRegistrar.getRegistrationId(AccountInfoActivity.this);
			    
			    if(regId.equals("")){
			    	GCMRegistrar.register(AccountInfoActivity.this, SENDER_ID);
			    }else{
			    	System.out.println("Registered.");
			    	
			    	Account account = Util.Connection.getUserAccount(getApplicationContext());
					final Device device = new Device();
					device.setOwnerAccount(account);
					device.setNickname(Util.Connection.getDeviceNickname(getApplicationContext()));
					device.setDeviceUUID(new DeviceUUIDGeneratorImpl(getApplicationContext()));
					device.setRegistrationKey(regId);
					
			    	new AsyncTask<Void, Void, Response>(){
						
						@SuppressWarnings("deprecation")
						@Override
						protected void onPreExecute(){
							super.onPreExecute();
							Notification notification = new Notification();
							PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent()/* Null intent*/, 0);
							notification.icon = R.drawable.ic_launcher;
							notification.tickerText = "Registering device...";
							notification.when = System.currentTimeMillis();
							notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), "Registering device...", intent);
							
							NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
							notifManager.notify(0, notification);
						}

						@Override
						protected Response doInBackground(Void... params) {
							Request request = Request.Builder.setRequest(API.Device.ADD_DEVICE).setPayload(device).build();
							return request.sendRequest();
						}

						@Override
						protected void onPostExecute(Response result) {
							super.onPostExecute(result);
							NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
							notifManager.cancelAll();
							
							if(result.isSucceed()){
								Toast.makeText(getApplicationContext(), "Device registered.", Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(getApplicationContext(), "Cannot register device.", Toast.LENGTH_SHORT).show();
							}
						}
						
					}.execute();
			    }
			}
	    	
	    });
	    
	    btnChangeDeviceNickname.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
	    	
	    });
	    
	    tvUserAccount = (TextView)findViewById(R.id.activity_account_info_user_account);
	    tvUserAccount.setText(Util.Connection.getUserEmail(getApplicationContext()));
	    
	    btnLogout = (Button)findViewById(R.id.activity_account_info_logout);
	    btnLogout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(AccountInfoActivity.this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.proceed_logout)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Util.Connection.setUserAccountEnabled(getApplicationContext(), false);
							finish();
							
						}
					}).setNegativeButton(android.R.string.no, null)
					.show();
			}
	    	
	    });
	    
	    new GetDeviceInfoTask(this).setFinishOnCancel(true).execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(RemoteroidIntent.ACTION_DEVICE_REGISTRATION_COMPLETE);
		filter.addAction(RemoteroidIntent.ACTION_DEVICE_REGISTRATION_FAILED);
		registerReceiver(deviceRegistrationReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(deviceRegistrationReceiver);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class GetDeviceInfoTask extends DialogAsyncTask<Void, Void, Response>{

		public GetDeviceInfoTask(Activity context) {
			super(context);
		}

		@Override
		protected Response doInBackground(Void... args) {
			Account account = Util.Connection.getUserAccount(getApplicationContext());
			Device device = new Device();
			DeviceUUIDGenerator generator = new DeviceUUIDGeneratorImpl(getApplicationContext());
			device.setDeviceUUID(generator);
			
			device.setOwnerAccount(account);
			
			Request request = Request.Builder.setRequest(API.Device.RETRIEVE_DEVICE_INFO).setPayload(device).build();
			
			return request.sendRequest();
		}

		@Override
		protected void onPostExecute(Response result) {
			super.onPostExecute(result);
			if(result.isSucceed()){
				Device device = result.getPayloadAsDevice();
				btnChangeDeviceNickname.setVisibility(View.VISIBLE);
				btnChangeDeviceNickname.setText(device.getNickname());
				btnRegisterDevice.setVisibility(View.GONE);
				tvDeviceRegistrationStatus.setText("Device name");
				tvDeviceRegistrationStatus.setVisibility(View.VISIBLE);
			}else{
				btnChangeDeviceNickname.setVisibility(View.GONE);
				btnRegisterDevice.setVisibility(View.VISIBLE);
				tvDeviceRegistrationStatus.setText("Device not registered.");
				tvDeviceRegistrationStatus.setVisibility(View.VISIBLE);
			}
		}
		
	}

}
