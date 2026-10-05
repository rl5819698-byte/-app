package com.rl5819698.airpodspro3;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;

public final class ProximityFinder {
    public interface Listener { void onLevel(int rssi, String text); }
    private final Handler handler = new Handler();
    private final ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    private Listener listener;
    private boolean active;
    public ProximityFinder(Listener listener){this.listener=listener;}
    public void start(){active=true;}
    public void stop(){active=false;}
    public boolean isActive(){return active;}
    public void update(final int rssi){
        if(!active)return;
        final String text;
        if(rssi >= -55) text="קרוב מאוד";
        else if(rssi >= -65) text="קרוב";
        else if(rssi >= -75) text="בינוני";
        else text="רחוק / אות חלש";
        handler.post(new Runnable(){public void run(){if(listener!=null)listener.onLevel(rssi,text);}});
        if(rssi >= -65) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
    }
    public void close(){active=false;tone.release();}
}
