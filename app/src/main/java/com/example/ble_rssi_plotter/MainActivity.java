package com.example.ble_rssi_plotter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_rssi_plotter.Adapter.RecycleViewBLEAdapter;
import com.opencsv.CSVWriter;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final Integer PERMISSION_WRITE_IDENTIFIER = 11;
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
    // array for csv
    ArrayList<ScanResult> scanResultsCSV;
    // array devices of interest
    ArrayList<ScanResult> deviceOfInterest;

    @BindView(R.id.file_directory_tv)
    TextView fileDirectoryTextView;

    @BindView(R.id.path_to_file_tv)
    TextView pathToFileTextView;

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

    @BindView(R.id.required_dumping_rows)
    EditText requiredDumpingRows;

    @BindView(R.id.start_dumping_to_file)
    Button dumpToFileButton;

    Runnable runnable;
    Thread thread;

    @OnClick(R.id.start_dumping_to_file)
    public void onStartDumpingToFileClick() {

//        dummyCodeWithThread();
        try {
            if (isScanning()) {
                scanDisposable.dispose();
            }
            updateButtonUIState();

            this.scanResultsCSV = new ArrayList<ScanResult>(0);

            int requiredRows = Integer.parseInt(String.valueOf(requiredDumpingRows.getText()));
            String filename = String.valueOf(filenameEditText.getText());

            if (filename.length() < 2) {
//                Toast.makeText(this, "Wprowadz nazwe pliku", Toast.LENGTH_SHORT).show();
                throw new Exception("Wprowadz nazwe pliku...");
            }

            File pathfile = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/csvData");

            if (!pathfile.isDirectory()) {
//              if no directory, create one
                pathfile.mkdir();
            }

            File file = new File(pathfile,
                    File.separator + filename + ".csv");
            if (file.exists()) {
                throw new IOException("Plik juz istnieje!");
            }

            toggleScanBleButton.setEnabled(false);
            dumpToFileButton.setEnabled(false);
            scanBleDevicesDelay(requiredRows, filename);
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Nalezy tam wpisac liczbe...", Toast.LENGTH_SHORT).show();
            toggleScanBleButton.setEnabled(true);
            dumpToFileButton.setEnabled(true);
        } catch (IOException ioException) {
            toggleScanBleButton.setEnabled(true);
            dumpToFileButton.setEnabled(true);
            Toast.makeText(this, ioException.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            toggleScanBleButton.setEnabled(true);
            dumpToFileButton.setEnabled(true);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void dummyCodeWithThread() {
        ArrayList<ScanResult> returnedResults;
        runnable = new SimpleThread(String.valueOf(requiredDumpingRows.getText()), rxBleClient, new SimpleThread.AnnounceFromThread() {
            @Override
            public void onEnd(ArrayList<ScanResult> scanResults) {
//                returnedResults = scanResults;
            }
        });
        thread = new Thread(runnable);
//        turn off
        if (isScanning())
            scanDisposable.dispose();
        updateButtonUIState();
        toggleScanBleButton.setEnabled(false);
        dumpToFileButton.setEnabled(false);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException ie) {
            Log.i("InterruptException", Arrays.toString(ie.getStackTrace()));
        } finally {
            toggleScanBleButton.setEnabled(true);
            dumpToFileButton.setEnabled(true);
        }
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

//        init devices of interest
        deviceOfInterest = new ArrayList<ScanResult>(0);

        File pathfile = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/csvData");
        fileDirectoryTextView.setText("File directory:");
        pathToFileTextView.setText(pathfile.getPath());

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
//        Toast.makeText(this, "Nie klikaj, bo wybuchnie!", Toast.LENGTH_SHORT).show();

        List<ScanResult> toBeDeleted = this.deviceOfInterest
                .stream()
                .filter(item -> item.getBleDevice().getMacAddress().equals(scanResult.getBleDevice().getMacAddress()))
                .collect(Collectors.toList());

        if (!toBeDeleted.isEmpty()) {
            toBeDeleted.forEach(item -> {
                Toast.makeText(this, "No longer listening to " + item.getBleDevice().getMacAddress(), Toast.LENGTH_SHORT).show();
                this.deviceOfInterest.remove(item);
            });
        } else {
            this.deviceOfInterest.add(scanResult);
            Toast.makeText(this, "Listening to " + scanResult.getBleDevice().getMacAddress(), Toast.LENGTH_SHORT).show();
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

    private void scanBleDevicesDelay(int howManyRows, String filename) {
        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build(),
                new ScanFilter.Builder()
                        .build())
                .doFinally(() -> {
                    dispose();
                    toggleScanBleButton.setEnabled(true);
                    dumpToFileButton.setEnabled(true);

                })
                .subscribe(
                        scanResult -> {
//                            I am not proud of this :-(
                            // Process scan result here.
                            recycleViewBLEAdapter.addScanResult(scanResult);
                            scanResultsCSV.add(scanResult);
                            if (scanResultsCSV.size() == howManyRows) {
//                                CSV file writer
                                try {

                                    if (ContextCompat.checkSelfPermission(
                                            getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                            PackageManager.PERMISSION_GRANTED) {
                                        // You can use the API that requires the permission.
//                                        performAction(...);

                                        File pathfile = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/csvData");

                                        if (!pathfile.isDirectory()) {
//                                            if no directory, create one
                                            pathfile.mkdir();
                                        }

                                        File file = new File(pathfile,
                                                File.separator + filename + ".csv");
                                        if (!file.exists()) {
//                                            if file does not exist, create one
                                            file.createNewFile();
                                            Log.i("File created: ", file.getName());
                                        } else {
                                            throw new IOException();
                                        }
                                        CSVWriter writer = new CSVWriter(new FileWriter(file));
//                                        CSVWriter writer = new CSVWriter(new FileWriter(pathfile + File.separator + filename + ".csv"));
                                        String[] columns = String.format("%s,%s,%s,%s", "timestamp", "MAC", "Device name", "RSSI").split(",");
                                        writer.writeNext(columns);
                                        scanResultsCSV.forEach(scan -> {
                                            String[] entries = String.format("%d,%s,%s,%d", scan.getTimestampNanos(), scan.getBleDevice().getMacAddress(), scan.getBleDevice().getName(), scan.getRssi()).split(",");
                                            Log.i("Entries", Arrays.toString(entries));
                                            writer.writeNext(entries);
                                        });
                                        writer.close();
                                        Toast.makeText(this, "Zapisano do pliku", Toast.LENGTH_SHORT).show();
                                    } else if (shouldShowRequestPermissionRationale("Failed")) {
                                        // In an educational UI, explain to the user why your app requires this
                                        // permission for a specific feature to behave as expected. In this UI,
                                        // include a "cancel" or "no thanks" button that allows the user to
                                        // continue using your app without granting the permission.
//                                        showInContextUI(...);
                                        Toast.makeText(this, "Nie przyznano dostępu", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // request a permission
                                        ActivityCompat.requestPermissions(this, new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                                    }

                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                    Toast.makeText(this, "Plik istnieje!", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Nieoczekiwany blad", Toast.LENGTH_SHORT).show();
                                } finally {
//                                    Toast.makeText(this, "Zakonczono procedure", Toast.LENGTH_SHORT).show();
                                    scanDisposable.dispose();
                                }
                            }
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.e("BLE search error", Arrays.toString(throwable.getStackTrace()));
                            Toast.makeText(this, "Nieoczekiwany blad", Toast.LENGTH_SHORT).show();
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

//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                PERMISSION_WRITE_IDENTIFIER);
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