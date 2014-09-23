package com.recursivepenguin.glassdatedisplay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDisplayService extends Service {

	private WindowManager windowManager;
	private TextView floatingDate;
	private TextView battery;
	private Intent batteryStatus;
	private Runnable hide;
	BroadcastReceiver broadcastReceiver;
	BroadcastReceiver batteryBroadcastReceiver;
	private View mainView;

	Handler handler;

	@Override
	public IBinder onBind(Intent intent) {
		// Not used
		return null;
	}

	static SimpleDateFormat format = new SimpleDateFormat("EE dd MMM yyyy");

	@Override
	public void onCreate() {
		super.onCreate();

		handler = new Handler(Looper.getMainLooper());
		mainView = LayoutInflater.from(this).inflate(R.layout.main_layout, null);
		floatingDate = (TextView) mainView.findViewById(R.id.date);

		battery = (TextView) mainView.findViewById(R.id.batteryLvl);

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		floatingDate.setText(format.format(new Date()));

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		windowManager.addView(mainView, params);

		hide = new Runnable() {
			@Override
			public void run() {
				mainView.setVisibility(View.GONE);

			}
		};

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent intent) {
				if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
					floatingDate.setText(format.format(new Date()));
				}
			}
		};

		batteryBroadcastReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
					int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
					battery.setText(String.valueOf(batteryLevel) + "%");
				}
			}
		};

		registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

		batteryStatus = registerReceiver(batteryBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		battery.setText(String.valueOf(batteryLevel) + "%");

		//Useless if not  running on a phone
//		Notification notification = new Notification.Builder(this)
//				.setContentTitle("Glass time")
//				.getNotification();
//
//		startForeground(123, notification);

		scheduleFadeout();

		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				mainView.setVisibility(View.VISIBLE);
				scheduleFadeout();

			}
		}, new IntentFilter(Intent.ACTION_SCREEN_ON));
	}

	private void scheduleFadeout() {

		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(hide, 2500);

	}

	@Override
	public void onDestroy() {

		stopForeground(true);

		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}

		super.onDestroy();
		if (mainView != null) {
			windowManager.removeView(mainView);
		}
	}

}
