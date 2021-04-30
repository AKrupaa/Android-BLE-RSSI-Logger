package com.example.ble_rssi_plotter;

import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.disposables.Disposable;


public class SimpleThread implements Runnable {
    private final AnnounceFromThread announceFromThread;
    private final String quantityDumpingRowsEditText;
    Disposable scanDisposable;
    RxBleClient rxBleClient;
    ArrayList<ScanResult> resultArrayList = new ArrayList<ScanResult>(0);

    public SimpleThread(String quantityDumpingRowsEditText, RxBleClient rxBleClient, AnnounceFromThread announceFromThread) {
        this.quantityDumpingRowsEditText = quantityDumpingRowsEditText;
        this.rxBleClient = rxBleClient;
        this.announceFromThread = announceFromThread;
    }

    @Override
    public void run() {
//        Integer quantity = 0;
        try {
            int quantity = Integer.parseInt(quantityDumpingRowsEditText);
//            time = time * 1000; // [ms]
            scanBleDevices();
            while (resultArrayList.size() < quantity) {
//                Log.i("THREAD", "DZIALA I WYPISUJE");
//                time -= 1;
            }
//            announceFromThread.onEnd("Ukonczono!");
        } catch (NumberFormatException nfe) {
            Log.e("Number Format", Arrays.toString(nfe.getStackTrace()));
        } catch (IllegalThreadStateException itse) {
            Log.e("ILLEGAL THREAD", Arrays.toString(itse.getStackTrace()));
        } finally {
            scanDisposable.dispose();
            announceFromThread.onEnd(resultArrayList);
//            announceFromThread.onEnd("Ukonczono!");
        }
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
                            Log.i("Scan Result", scanResult.toString());
                            resultArrayList.add(scanResult);
//                            recycleViewBLEAdapter.addScanResult(scanResult);
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.e("BLE search error", Arrays.toString(throwable.getStackTrace()));
                        }
                );
    }

    private void dispose() {
        scanDisposable = null;
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    public interface AnnounceFromThread {
        void onEnd(ArrayList<ScanResult> scanResults);
    }

//    public void setAnnounceFromThread(AnnounceFromThread announceFromThread) {
//        this.announceFromThread = announceFromThread;
//    }
}

