package com.cooweather.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoUpdateReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Intent i = new Intent(arg0,AutoUpdateService.class);
		arg0.startActivity(i);
	}

}
