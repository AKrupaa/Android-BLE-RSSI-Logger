package com.example.ble_rssi_plotter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    // contains rxBleClient
    private RxBleClient rxBleClient;
    // for ButterKnife
    private Unbinder unbinder;
    // for results of rxBleClient
    private List<ScanResult> scanResultList;

    @BindView(R.id.start_scanning_ble)
    Button scanBleButton;

    @OnClick(R.id.start_scanning_ble)
    public void onScanBleButtonClick() {
        ;
    }

    @BindView(R.id.file_name)
    EditText filenameEditText;

    @BindView(R.id.time_of_dumping)
    EditText timeOfDumpingEditText;

    @BindView(R.id.start_dumping_to_file)
    Button dumpToFileButton;

    @OnClick(R.id.start_dumping_to_file)
    public void onStartDumpingToFileClick() {
        ;
    }

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
        unbinder = ButterKnife.bind(this);
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


    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        } else {
            checkPermission();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
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