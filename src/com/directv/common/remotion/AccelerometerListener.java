package com.directv.common.remotion;

import android.hardware.SensorEvent;

public interface AccelerometerListener {
      
    public void onAccelerationChanged(SensorEvent event, float x, float y, float z);
  
    public void onShake(float force);
  
}