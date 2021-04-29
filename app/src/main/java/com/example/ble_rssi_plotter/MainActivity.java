package com.example.ble_rssi_plotter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private RxBleClient rxBleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//            onSaveInstanceState()
        if (savedInstanceState == null) {
//            entering application for the first time
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
//                nothing from previous activity
                ;
            } else {
//                some from previous activity
                ;
            }
        } else {
//           re-entering application
            ;
        }

//        this is fun :-)
//        for sake of practise
        ButterKnife.bind(this);
//        Turn on Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT = 1;
        this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

//        get RxBleClient
        rxBleClient = SampleApplication.getRxBleClient(this);


        // Use this check to determine whether SampleApplication is supported on the device. Then
        // you can selectively disable SampleApplication-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.BLE_is_not_supported, Toast.LENGTH_LONG).show();
            finish();
        } else {
//            good to know ;)
            Toast.makeText(this, R.string.BLE_is_supported, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
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
    protected void onRestart() {
        super.onRestart();
    }
}