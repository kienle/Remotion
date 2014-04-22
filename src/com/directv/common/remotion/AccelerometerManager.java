package com.directv.common.remotion;
import java.util.List;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;
 
public class AccelerometerManager {
  
    private static Context mContext;
     
     
    /** Accuracy configuration */
    private static float threshold  = 15.0f; 
    private static int interval     = 200;
  
    private static Sensor mSensor;
    private static SensorManager mSensorManager;
    // you could use an OrientationListener array instead
    // if you plans to use more than one listener
    private static AccelerometerListener mListener;
  
    // indicates whether or not Accelerometer Sensor is supported
    private static Boolean mSupported;
    // indicates whether or not Accelerometer Sensor is running
    private static boolean mIsRunning = false;
  
    /**
     * Returns true if the manager is listening to orientation changes
     */
    public static boolean isListening() {
        return mIsRunning;
    }
  
    /**
     * Unregisters listeners
     */
    public static void stopListening() {
        mIsRunning = false;
        try {
            if (mSensorManager != null && sensorEventListener != null) {
                mSensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {}
    }
  
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public static boolean isSupported(Context context) {
        mContext = context;
        if (mSupported == null) {
            if (mContext != null) {
                mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
                 
                // Get all sensors in device
                List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                mSupported = Boolean.valueOf(sensors.size() > 0);
            } else {
                mSupported = Boolean.FALSE;
            }
        }
        return mSupported;
    }
  
    /**
     * Configure the listener for shaking
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void configure(int threshold, int interval) {
        AccelerometerManager.threshold = threshold;
        AccelerometerManager.interval = interval;
    }
  
    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     */
    public static void startListening( AccelerometerListener accelerometerListener) {
         
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
         
        // Take all sensors in device
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
         
        if (sensors.size() > 0) {
            mSensor = sensors.get(0);
             
            // Register Accelerometer Listener
            mIsRunning = mSensorManager.registerListener(
                    sensorEventListener, mSensor, 
                    SensorManager.SENSOR_DELAY_GAME);             
            mListener = accelerometerListener;
        }
    }
  
    /**
     * Configures threshold and interval
     * And registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void startListening(
            AccelerometerListener accelerometerListener, 
            int threshold, int interval) {
        configure(threshold, interval);
        startListening(accelerometerListener);
    }
  
    /**
     * The listener that listen to events from the accelerometer listener
     */
    private static SensorEventListener sensorEventListener = 
        new SensorEventListener() {
  
        private long now = 0;
        private long timeDiff = 0;
        private long lastUpdate = 0;
        private long lastShake = 0;
  
        private float x = 0;
        private float y = 0;
        private float z = 0;
        private float lastX = 0;
        private float lastY = 0;
        private float lastZ = 0;
        private float force = 0;
  
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
  
        public void onSensorChanged(SensorEvent event) {
            // use the event timestamp as reference
            // so the manager precision won't depends 
            // on the AccelerometerListener implementation
            // processing time
            now = event.timestamp;
  
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
  
            // if not interesting in shake events
            // just remove the whole if then else block
            if (lastUpdate == 0) {
                lastUpdate = now;
                lastShake = now;
                lastX = x;
                lastY = y;
                lastZ = z;
                Toast.makeText(mContext, "No Motion detected", Toast.LENGTH_SHORT).show();
                 
            } else {
                timeDiff = now - lastUpdate;
                
                if (timeDiff > 0) {
                     
                    /*force = Math.abs(x + y + z - lastX - lastY - lastZ) 
                                / timeDiff;*/
                    force = Math.abs(x + y + z - lastX - lastY - lastZ);
                     
                    if (Float.compare(force, threshold) > 0 ) {
                        //Toast.makeText(Accelerometer.getContext(), 
                        //(now-lastShake)+"  >= "+interval, 1000).show();
                         
                        if (now - lastShake >= interval) { 
                            // trigger shake event
                            mListener.onShake(force);
                        } else {
                            Toast.makeText(mContext, "No Motion detected", Toast.LENGTH_SHORT).show();
                        }
                        
                        lastShake = now;
                    }
                    
                    lastX = x;
                    lastY = y;
                    lastZ = z;
                    lastUpdate = now; 
                } else {
                    Toast.makeText(mContext,"No Motion detected", Toast.LENGTH_SHORT).show();
                }
            }
            
            // trigger change event
            mListener.onAccelerationChanged(event, x, y, z);
        }
  
    };
  
}