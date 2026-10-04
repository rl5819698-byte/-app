package com.rl5819698.airpodspro3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public final class AirPodsScanner {
    public interface Listener {
        void onFound(AirPodsStatus status, BluetoothDevice device);
        void onError(String message);
    }
    private final BluetoothAdapter adapter;
    private final Listener listener;
    private boolean scanning;
    public AirPodsScanner(BluetoothAdapter adapter, Listener listener) {
        this.adapter = adapter; this.listener = listener;
    }
    public boolean isScanning() { return scanning; }
    public void start() {
        if (adapter == null || !adapter.isEnabled()) { listener.onError("Bluetooth is disabled."); return; }
        if (scanning) return;
        scanning = adapter.startLeScan(callback);
        if (!scanning) listener.onError("BLE scan could not start.");
    }
    public void stop() {
        if (!scanning) return;
        adapter.stopLeScan(callback);
        scanning = false;
    }
    private final BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            AirPodsStatus status = parse(device, rssi, scanRecord);
            if (status != null) listener.onFound(status, device);
        }
    };
    private AirPodsStatus parse(BluetoothDevice device, int rssi, byte[] record) {
        byte[] data = manufacturerData(record);
        if (data == null || data.length < 16) return null;
        if ((data[0] & 255) != 0x07 || (data[1] & 255) != 0x19) return null;
        String hex = toHex(data);
        if (hex.length() < 32) return null;

        int left = Character.digit(hex.charAt(12),16);
        int right = Character.digit(hex.charAt(13),16);
        int box = Character.digit(hex.charAt(15),16);
        int charge = Character.digit(hex.charAt(14),16);
        int inEar = Character.digit(hex.charAt(11),16);
        boolean flipped = (Character.digit(hex.charAt(10),16) & 2) == 0;
        if (flipped) { int t=left; left=right; right=t; }

        boolean lc=(charge & (flipped ? 2:1))!=0;
        boolean rc=(charge & (flipped ? 1:2))!=0;
        boolean cc=(charge & 4)!=0;
        boolean le=(inEar & (flipped ? 8:2))!=0;
        boolean re=(inEar & (flipped ? 2:8))!=0;

        String id=hex.substring(6,10), model;
        if ("2720".equals(id)) model="AirPods Pro 3";
        else if ("1420".equals(id) || "2420".equals(id)) model="AirPods Pro 2";
        else if ("0E20".equals(id)) model="AirPods Pro";
        else if ("1320".equals(id)) model="AirPods 3";
        else if ("0F20".equals(id)) model="AirPods 2";
        else if ("0220".equals(id)) model="AirPods";
        else model="Apple Earbuds ("+id+")";

        return new AirPodsStatus(device == null ? "unknown" : device.getAddress(), model,
                AirPodsStatus.battery(left), AirPodsStatus.battery(right), AirPodsStatus.battery(box),
                lc,rc,cc,le,re,rssi);
    }
    private byte[] manufacturerData(byte[] record) {
        if (record == null) return null;
        int i=0;
        while (i < record.length) {
            int len=record[i]&255; if (len==0) break;
            int typeIndex=i+1; if (typeIndex>=record.length) break;
            int type=record[typeIndex]&255;
            if (type==0xFF && len>=3 && typeIndex+2<record.length) {
                int company=(record[typeIndex+1]&255)|((record[typeIndex+2]&255)<<8);
                if (company==0x004C) {
                    int start=typeIndex+3, n=len-2;
                    if (start+n<=record.length) {
                        byte[] out=new byte[n]; System.arraycopy(record,start,out,0,n); return out;
                    }
                }
            }
            i+=len+1;
        }
        return null;
    }
    private static String toHex(byte[] data) {
        char[] h="0123456789ABCDEF".toCharArray(); StringBuilder s=new StringBuilder(data.length*2);
        for(byte b:data){int v=b&255;s.append(h[v>>>4]).append(h[v&15]);}
        return s.toString();
    }
}
