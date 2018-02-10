package com.jarvanmo.exoplayerview.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.jarvanmo.exoplayerview.R;

/**
 * Created by mo on 17-4-19.
 * Copyright © 2017, cnyanglao, Co,. Ltd. All Rights Reserve
 */

public class BatteryStatusView extends AppCompatImageView {


    public BatteryStatusView(Context context) {
        this(context, null);
    }

    public BatteryStatusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryStatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.stat_sys_battery);
        setImageLevel(100);
    }


    private BroadcastReceiver mPowerConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (isCharging) {
                setImageResource(R.drawable.stat_sys_battery_charge);
            } else {
                setImageResource(R.drawable.stat_sys_battery);
            }
            setImageLevel(level);

        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(mPowerConnectionReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mPowerConnectionReceiver);
    }


}