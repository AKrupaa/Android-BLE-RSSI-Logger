package com.example.ble_rssi_plotter.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ble_rssi_plotter.R;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Provide recycler view of recycler_view_row_ble.xml scheme
 * @param List<ScanResult> list of ScanResult to shows data in recyclerView
 */
public class RecycleViewBLEAdapter extends RecyclerView.Adapter<RecycleViewBLEAdapter.ViewHolder> {

    private final List<ScanResult> scanResultList;
    private ItemClickListener mClickListener;

    // pass data into constructor
    public RecycleViewBLEAdapter(List<ScanResult> scanResultList) {
        this.scanResultList = scanResultList;
    }

    // inflates the row layout from xml when needed
    /*
    When you write an XML layout, it will be inflated by the Android OS which basically means
    that it will be rendered by creating view object in memory.
    Let's call that implicit inflation (the OS will inflate the view for you)
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_row_ble, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextViews in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult scanResult = scanResultList.get(position);
        final RxBleDevice bleDevice = scanResult.getBleDevice();
        holder.lineNo1.setText(String.format(Locale.getDefault(), "%s (%s)", bleDevice.getMacAddress(), bleDevice.getName()));
        holder.lineNo2.setText(String.format(Locale.getDefault(), "RSSI: %d", scanResult.getRssi()));

        // set on click lister on one element in recycle view
        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onItemClick(v, position, scanResult);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return scanResultList.size();
    }


    private static final Comparator<ScanResult> SORTING_COMPARATOR = (lhs, rhs) ->
            lhs.getBleDevice().getMacAddress().compareTo(rhs.getBleDevice().getMacAddress());

    public void addScanResult(ScanResult bleScanResult) {
        // Not the best way to ensure distinct devices, just for sake on the demo.

        for (int i = 0; i < scanResultList.size(); i++) {

            if (scanResultList.get(i).getBleDevice().equals(bleScanResult.getBleDevice())) {
                scanResultList.set(i, bleScanResult);
                notifyItemChanged(i);
                return;
            }
        }

        scanResultList.add(bleScanResult);
        Collections.sort(scanResultList, SORTING_COMPARATOR);
        notifyDataSetChanged();
    }

    public void clearScanResults() {
        scanResultList.clear();
        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView lineNo1;
        public final TextView lineNo2;
//        public final TextView line1;
//        public final TextView line2;
//        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            lineNo1 = (TextView) view.findViewById(R.id.line1_tv);
            lineNo2 = (TextView) view.findViewById(R.id.line2_tv);
        }

        @Override
        public String toString() {
            return "ViewHolder{" +
                    "mView=" + mView +
                    ", lineNo1=" + lineNo1 +
                    ", lineNo2=" + lineNo2 +
                    '}';
        }
    }

    // setter
    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, ScanResult scanResult);
    }
}
