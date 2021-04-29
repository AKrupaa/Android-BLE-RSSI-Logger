package com.example.ble_rssi_plotter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ble_rssi_plotter.Adapter.RecycleViewBLEAdapter;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    // contains rxBleClient
    private RxBleClient rxBleClient;
    // for ButterKnife
    private Unbinder unbinder;
    // for results of rxBleClient
    private List<ScanResult> scanResultList;
    // for dealing with scan (RxJava) RxBleClient
    private Disposable scanDisposable;
    // recycle viewer adapter
    RecycleViewBLEAdapter recycleViewBLEAdapter;

    @BindView(R.id.ble_rv)
    RecyclerView recyclerView;

    @BindView(R.id.toggle_scan_ble_button)
    Button toggleScanBleButton;

    @OnClick(R.id.toggle_scan_ble_button)
    public void onScanBleButtonClick() {
        if (isScanning()) {
            scanDisposable.dispose();
        } else {
            scanBleDevices();
        }
        updateButtonUIState();
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

        // Use this check to determine whether SampleApplication is supported on the device. Then
        // you can selectively disable SampleApplication-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.BLE_is_not_supported, Toast.LENGTH_LONG).show();
            finish();
        } else {
//            good to know ;)
            Toast.makeText(this, R.string.BLE_is_supported, Toast.LENGTH_SHORT).show();
        }

        //        get RxBleClient
        rxBleClient = SampleApplication.getRxBleClient(this);
//        Context context = view.getContext();
        scanResultList = new ArrayList<ScanResult>(0);
        recycleViewBLEAdapter = new RecycleViewBLEAdapter(scanResultList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(recycleViewBLEAdapter);

        recycleViewBLEAdapter.setClickListener(this::onAdapterItemClick);
    }

    private void onAdapterItemClick(View view, int position, ScanResult scanResult) {
        Toast.makeText(this, "Nie klikaj, bo wybuchnie!", Toast.LENGTH_SHORT).show();
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

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void updateButtonUIState() {
        toggleScanBleButton.setText(isScanning() ? getString(R.string.stop_scan) : getString(R.string.find_devices));
    }

    private void dispose() {
        scanDisposable = null;
        recycleViewBLEAdapter.clearScanResults();
        updateButtonUIState();
    }

    private void scanBleDevices() {
        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build(),
                new ScanFilter.Builder()
                        .build())
                .doFinally(this::dispose)
                .subscribe(
                        scanResult -> {
                            // Process scan result here.
                            recycleViewBLEAdapter.addScanResult(scanResult);
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.e("BLE search error", Arrays.toString(throwable.getStackTrace()));
                        }
                );
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