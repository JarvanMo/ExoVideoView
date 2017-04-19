package com.jarvanmo.exoplayerview.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by mo on 17-4-19.
 * Copyright © 2017, cnyanglao, Co,. Ltd. All Rights Reserve
 */

public class BatteryLevelView extends View {

    /**
     * 画笔信息
     */
    private Paint mBatteryPaint;
    private Paint mPowerPaint;
    private float mBatteryStroke = 2f;
    /**
     * 屏幕高宽
     */
    private int measureWidth;
    private int measureHeight;
    /**
     *
     * 电池参数
     */
    private float mBatteryHeight = 20f; // 电池的高度
    private float mBatteryWidth = 40f; // 电池的宽度
    private float mCapHeight = 10f;
    private float mCapWidth = 5f;
    /**
     *
     * 电池电量
     */
    private float mPowerPadding = 1;
    private float mPowerHeight = mBatteryHeight - mBatteryStroke
            - mPowerPadding * 2; // 电池身体的高度
    private float mPowerWidth = mBatteryWidth - mBatteryStroke - mPowerPadding
            * 2;// 电池身体的总宽度
    private float mPower = 10f;
    /**
     *
     * 矩形
     */
    private RectF mBatteryRect;
    private RectF mCapRect;
    private RectF mPowerRect;


    private boolean mIsCharging = false;

    public BatteryLevelView(Context context) {
        this(context,null);
    }

    public BatteryLevelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BatteryLevelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    public void initView() {

        mBatteryPaint = new Paint();// 设置电池画笔
        mBatteryPaint.setColor(Color.GRAY);
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setStyle(Paint.Style.STROKE);
        mBatteryPaint.setStrokeWidth(mBatteryStroke);

        mPowerPaint = new Paint();//设置电量画笔
        mPowerPaint.setColor(getPowerColor());
        mPowerPaint.setAntiAlias(true);
        mPowerPaint.setStyle(Paint.Style.FILL);
        mPowerPaint.setStrokeWidth(mBatteryStroke);

        mBatteryRect = new RectF(mCapWidth, 0, mBatteryWidth, mBatteryHeight);//设置电池矩形

        mCapRect = new RectF(0, (mBatteryHeight - mCapHeight) / 2, mCapWidth,
                (mBatteryHeight - mCapHeight) / 2 + mCapHeight);//设置电池盖矩形

        mPowerRect = new RectF(mCapWidth + mBatteryStroke / 2 + mPowerPadding
                + mPowerWidth * ((100f - mPower) / 100f), // 需要调整左边的位置
                mPowerPadding + mBatteryStroke / 2, // 需要考虑到 画笔的宽度
                mBatteryWidth - mPowerPadding * 2, mBatteryStroke / 2
                + mPowerPadding + mPowerHeight);// 设置电量矩形
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(measureWidth / 2, measureHeight / 2);
        canvas.drawRoundRect(mBatteryRect, 2f, 2f, mBatteryPaint); // 画电池轮廓需要考虑 画笔的宽度
        canvas.drawRoundRect(mCapRect, 2f, 2f, mBatteryPaint);// 画电池盖
        canvas.drawRect(mPowerRect, mPowerPaint);// 画电量
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(measureWidth, measureHeight);
    }


    public void setPower(float power) {
        mPower = power;
        if (mPower < 0) {
            mPower = 0;
        }
        mPowerPaint.setColor(getPowerColor());

        mPowerRect = new RectF(mCapWidth + mBatteryStroke / 2 + mPowerPadding
                + mPowerWidth * ((100f - mPower) / 100f), // 需要调整左边的位置
                mPowerPadding + mBatteryStroke / 2, // 需要考虑到 画笔的宽度
                mBatteryWidth - mPowerPadding * 2, mBatteryStroke / 2
                + mPowerPadding + mPowerHeight);
        invalidate();
    }



    private int getPowerColor(){
        if(mIsCharging){
            return Color.GREEN;
        }

        if(mPower <= 15){
            return  Color.RED;
        }else if(mPower<= 30){
            return Color.YELLOW;
        }else {
            return Color.WHITE;
        }
    }

    private BroadcastReceiver mPowerConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            mIsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float percent = ((float)(level * 100)) / scale;
            setPower(percent);
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(mPowerConnectionReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mPowerConnectionReceiver);
    }


}