package com.example.ble_rssi_plotter;

import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.Arrays;

import io.reactivex.disposables.Disposable;


public class SimpleThread implements Runnable {

    private AnnounceFromThread announceFromThread;
    private String timeOfDumpingEditText;
    Disposable scanDisposable;
    RxBleClient rxBleClient;

    public SimpleThread(String timeOfDumpingEditText, RxBleClient rxBleClient, AnnounceFromThread announceFromThread) {
        this.timeOfDumpingEditText = timeOfDumpingEditText;
        this.rxBleClient = rxBleClient;
        this.announceFromThread = announceFromThread;
    }

    @Override
    public void run() {
        Integer time = 0;
        try {
            time = Integer.parseInt(timeOfDumpingEditText);
            time = time * 1000; // [ms]
            scanBleDevices();
            while (time > 0) {
//                    sleep(1000);
//                    handler.post(this);
//                Toast.makeText(this, "some", )
                Log.i("THREAD", "DZIALA I WYPISUJE");
                time -= 1;
            }
//            announceFromThread.onEnd("Ukonczono!");
        } catch (NumberFormatException nfe) {
            Log.e("Number Format", Arrays.toString(nfe.getStackTrace()));
        } catch (IllegalThreadStateException itse) {
            Log.e("ILLEGAL THREAD", Arrays.toString(itse.getStackTrace()));
        } finally {
            scanDisposable.dispose();
            announceFromThread.onEnd("Ukonczono!");
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
        void onEnd(String value);
    }

//    public void setAnnounceFromThread(AnnounceFromThread announceFromThread) {
//        this.announceFromThread = announceFromThread;
//    }
}

