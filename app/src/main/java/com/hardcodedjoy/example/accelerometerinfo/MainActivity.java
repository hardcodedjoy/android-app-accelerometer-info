package com.hardcodedjoy.example.accelerometerinfo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    static private final int SAMPLING_PERIOD_MICROS = 10000;
    static private final int PLOT_BUFFER_MILLIS = 5000;

    private  TextView tvText;

    private SensorEventListener sensorEventListener;
    private float[] buffer;
    private int indexInBuffer;
    private PlotView[] pvPlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensor();
    }

    private void initGUI() {
        setTitle(getString(R.string.app_name));
        setContentView(R.layout.layout_main);

        tvText = findViewById(R.id.tv_text);

        LinearLayout llPlots = findViewById(R.id.ll_plots);
        llPlots.removeAllViews();

        // buffer will contain data of all 3 channels (x, y, z)
        buffer = new float[3 * PLOT_BUFFER_MILLIS * 1000 / SAMPLING_PERIOD_MICROS];
        indexInBuffer = 0;

        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.weight = 1.0f;

        pvPlots = new PlotView[3];
        for(int i=0; i<pvPlots.length; i++) {
            PlotView plotView = new PlotView(this);
            pvPlots[i] = plotView;
            plotView.setBuffer(buffer);
            plotView.setNumChannels(3);
            plotView.setChannel(i);
            plotView.setBackgroundColor(0xFF404040);
            plotView.setColor(0xFF00FF00);
            plotView.setLineWidth(3);
            if(i < pvPlots.length-1) {
                params.setMargins(0, 10, 0, 10);
            } else {
                params.setMargins(0, 10, 0, 0);
            }
            llPlots.addView(plotView, params);
        }
    }

    private void initSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                onSensorData(event.values[0], event.values[1], event.values[2]);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
        sensorManager.registerListener(sensorEventListener, accelerometer, SAMPLING_PERIOD_MICROS);
    }

    private void stopSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
    }

    private void onSensorData(float x, float y, float z) {

        String s = "";
        s += "acc_x = " + String.format(Locale.US, "%.3f", x) + " m/s²\n";
        s += "acc_y = " + String.format(Locale.US, "%.3f", y) + " m/s²\n";
        s += "acc_z = " + String.format(Locale.US, "%.3f", z) + " m/s²";

        tvText.setText(s);

        // plots accept +/- 1.0 range
        // so we scale to have the displayed range of +/- 10 m/s²:

        x /= 10;
        y /= 10;
        z /= 10;

        buffer[indexInBuffer++] = x;
        buffer[indexInBuffer++] = y;
        buffer[indexInBuffer++] = z;

        indexInBuffer %= buffer.length;
        for(PlotView plotView : pvPlots) {
            plotView.postInvalidate();
        }
    }
}