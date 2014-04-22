package com.directv.common.remotion;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainAccelerometer extends Activity implements AccelerometerListener {
	private static final int X_DELTA_MAX = 8;
	private static final int DELTA = 2;
	
	private float [] history = new float[2];
//	private String [] direction = {"NONE","NONE"};
	
	private boolean enableDetect;
	private boolean tiltLeft;
	private boolean tiltRight;
	private boolean tiltHoldLeftRight; 
	
	private TextView mTvStatus;
	private TextView mTvDirection;
	private TextView mTvStart;
	
	private Timer mTimerDetectLeftRight;
	private Handler mHandler = new Handler();
	private int mUpdatePeriod = 1000; 
	private int mCount = 0;
	
	private int mTiltLeftCount = 0;
	private int mTiltRightCount = 0;
	private int mTiltHoldLeftCount = 0;
	private int mTiltHoldRightCount = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTvStatus = (TextView) findViewById(R.id.tvStatus);
		mTvDirection = (TextView) findViewById(R.id.tvDirection);
		mTvStart = (TextView) findViewById(R.id.tvStart);
		
		mTvStart.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					enableDetect = true;
					mTvStatus.setText("Motion is start");
					break;
				case MotionEvent.ACTION_UP:
					enableDetect = false;
					mTvStatus.setText("Motion is stop");
					mTvDirection.setText("None");
					break;

				default:
					break;
				}
				
				return true;
			}
		});
	}

	private void setupTimer() {
		// cancel if already existed
		if (mTimerDetectLeftRight != null) {
			mTimerDetectLeftRight.cancel();
		} else {
			// recreate new
			mTimerDetectLeftRight = new Timer();
			mCount = 0;
			mTimerDetectLeftRight.schedule(new DetectMotionTask(), 0, mUpdatePeriod);
		}
	}
	
	private void stopTimer() {
		if (mTimerDetectLeftRight != null) {
			mTimerDetectLeftRight.cancel();
			mTimerDetectLeftRight = null;
		}
	}
	
	private class DetectMotionTask extends TimerTask {
		
		@Override
		public void run() {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mCount++;
//					Log.d("KienLT", "DetectMotionTask run count = " + mCount);
					if (mCount >= 3) {

						if (tiltLeft) {
							tiltLeftHold();
						} else if (tiltRight) {
							tiltRightHold();
						}
						
						tiltHoldLeftRight = true;
						if (mTimerDetectLeftRight != null) {
							mTimerDetectLeftRight.cancel();
							mTimerDetectLeftRight = null;
						}
							
					}
				}
			});
			
		}
	}
    
	public void onAccelerationChanged(SensorEvent event, float x, float y, float z) {
		if (!enableDetect) {
			return;
		}
		
		float xChange = history[0] - x;
//        float yChange = history[1] - y;
        
        history[0] = x;
        history[1] = y;
        
        // detect hold left
        if (x > X_DELTA_MAX) {
        	if (!tiltLeft) {
        		setupTimer();
        	}
        	
        	tiltLeft = true;
        	tiltRight = false;
        	
        } else
        
        // detect hold right
        if (x < -X_DELTA_MAX) {
        	if (!tiltRight) {
        		setupTimer();
        	}

        	tiltLeft = false;
        	tiltRight = true;
        }
        
        if (x > -2 && x < 2) {
        	if (tiltHoldLeftRight) {
        		tiltReturn();
        		tiltLeft = false;
        		tiltRight = false;
        		tiltHoldLeftRight = false;
    		} else {
    			// stop timer if it is holding
    			if (tiltLeft || tiltRight) {
    				stopTimer();
    			}
	        	if (tiltLeft && xChange > DELTA) {
	        		fastTiltLeft();
	        		tiltLeft = false;
	        	} else if (tiltRight && xChange < -DELTA) {
	        		fastTiltRight();
	        		tiltRight = false;
	        	}
    		}
        }
	}

	public void onShake(float force) {
		// Called when Shake Detected
		if (enableDetect) {
			Toast.makeText(getBaseContext(), "Shake detected", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
//		Toast.makeText(getBaseContext(), "Touch and hold screen to start!", Toast.LENGTH_SHORT).show();

		// Check device supported Accelerometer sensor or not
		if (AccelerometerManager.isSupported(this)) {

			// Start Accelerometer Listening
			AccelerometerManager.startListening(this);
		}
	}
	
	private void fastTiltLeft() {
		mTiltLeftCount += 1;
		mTvDirection.setText("Fast-Tilt Left " + mTiltLeftCount);
		Log.d("KienLT", "fastTiltLeft count = " + mTiltLeftCount);
	}

	private void fastTiltRight() {
		mTiltRightCount += 1;
		mTvDirection.setText("Fast-Tilt Right " + mTiltRightCount);
		Log.d("KienLT", "fastTiltRight count = " + mTiltRightCount);
	}
	
	private void tiltReturn() {
		mTvDirection.setText("Return");
	}
	
	private void tiltLeftHold() {
		mTiltHoldLeftCount += 1;
		mTvDirection.setText("Hold-Tilt Left " + mTiltHoldLeftCount);
	}
	
	private void tiltRightHold() {
		mTiltHoldRightCount += 1;
		mTvDirection.setText("Hold-Tilt Right " + mTiltHoldRightCount);
	}
	
	@Override
	public void onStop() {
		super.onStop();

		// Check device supported Accelerometer sensor or not
		if (AccelerometerManager.isListening()) {

			// Stop Accelerometer Listening
			AccelerometerManager.stopListening();

			Toast.makeText(getBaseContext(), "onStop Accelerometer Stoped", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("Sensor", "Service  distroy");

		// Check device supported Accelerometer sensor or not
		if (AccelerometerManager.isListening()) {

			// Stop Accelerometer Listening
			AccelerometerManager.stopListening();

			Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stoped",
					Toast.LENGTH_SHORT).show();
		}

	}

}