package com.rl5819698.airpodspro3;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends Activity implements AirPodsScanner.Listener {
    private static final int REQ_LOCATION = 42;
    private final Handler handler = new Handler();
    private AirPodsScanner scanner;
    private TextView state, battery, details;
    private Button scanButton;
    private AirPodsStatus latest;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        scanner=new AirPodsScanner(adapter,this);
        if(adapter==null) showState("המכשיר אינו תומך ב-Bluetooth");
    }

    private void buildUi() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18,14,18,18); root.setBackgroundColor(0xFFF4F4F6);

        TextView title=text("AirPods Pro 3",24,true); root.addView(title,lp(-1, -2));
        state=text("מוכן לסריקה",16,false); root.addView(state,lp(-1,-2));

        scanButton=button("סרוק AirPods",18);
        scanButton.setOnClickListener(new View.OnClickListener(){public void onClick(View v){if(scanner.isScanning())stopScan();else requestAndScan();}});
        root.addView(scanButton,lp(-1,56));

        battery=text("שמאל: —    ימין: —    מארז: —",18,true); battery.setPadding(0,18,0,8);
        root.addView(battery,lp(-1,-2));

        details=text("פתח את המארז ליד הטלפון ולחץ על סרוק.\n\nניווט: חצים / TAB בין הכפתורים, Enter לבחירה.",15,false);
        details.setGravity(Gravity.RIGHT); root.addView(details,lp(-1,-2));

        ScrollView scroll=new ScrollView(this);
        LinearLayout features=new LinearLayout(this); features.setOrientation(LinearLayout.VERTICAL);
        features.setPadding(0,12,0,12);

        addFeature(features,"Noise Control","ANC / Transparency / Adaptive Audio","ב-Android 4.4 אין API ציבורי ל-L2CAP המשמש את ערוץ ה-AACP.");
        addFeature(features,"Conversation Awareness","תלוי בקושחת האוזניות","שינוי בפועל דורש ערוץ AACP קנייני.");
        addFeature(features,"Spatial Audio","Head tracking / Personalized Spatial Audio","הגדרות מלאות דורשות APIs ופרוטוקול שאינם חשופים ב-API 19.");
        addFeature(features,"Device Info","Model / RSSI / last seen",null);
        Button bt=button("Bluetooth Settings",16); bt.setOnClickListener(new View.OnClickListener(){public void onClick(View v){try{startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));}catch(Exception ignored){}}});
        features.addView(bt,lp(-1,60));

        scroll.addView(features); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        setContentView(root);
    }

    private void addFeature(LinearLayout p,String title,String sub,final String message){
        Button b=button(title+"\n"+sub,16); b.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){if(message!=null)Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();else if(latest==null)showState("עדיין לא התקבל Beacon");else showStatus(latest);}});
        p.addView(b,lp(-1,68));
    }

    private void requestAndScan(){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION); return;
        }
        BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter();
        if(a!=null&&!a.isEnabled()){try{startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));}catch(Exception ignored){} return;}
        startScan();
    }
    private void startScan(){scanner.start();if(scanner.isScanning()){scanButton.setText("עצור סריקה");showState("סורק BLE…");}}
    private void stopScan(){scanner.stop();scanButton.setText("סרוק AirPods");showState(latest==null?"הסריקה הופסקה":"נמצאו נתונים אחרונים");}

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_LOCATION&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)startScan();
        else if(r==REQ_LOCATION)showState("נדרשת הרשאת מיקום לצורך BLE scan");
    }

    @Override public void onFound(final AirPodsStatus s, BluetoothDevice d){
        handler.post(new Runnable(){public void run(){latest=s;showStatus(s);}});
    }
    @Override public void onError(final String m){
        handler.post(new Runnable(){public void run(){showState(m);Toast.makeText(MainActivity.this,m,Toast.LENGTH_SHORT).show();}});
    }

    private void showStatus(AirPodsStatus s){
        String l=s.left>=0?s.left+"%":"—", r=s.right>=0?s.right+"%":"—", c=s.caseBattery>=0?s.caseBattery+"%":"—";
        battery.setText("שמאל: "+l+(s.leftCharging?" ⚡":"")+"    ימין: "+r+(s.rightCharging?" ⚡":"")+"    מארז: "+c+(s.caseCharging?" ⚡":""));
        String m=s.model+((s.leftInEar||s.rightInEar)?" • in-ear":"");
        details.setText(String.format(Locale.US,"דגם: %s\nBluetooth: %s\nRSSI: %d dBm\nזוהה: %tT",
                m,s.mac,s.rssi,s.timestamp));
        showState("נמצא: "+s.model);
    }
    private void showState(String s){state.setText(s);}
    private TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xFF1D1D1F);if(bold)t.setTypeface(null,1);t.setGravity(Gravity.RIGHT);return t;}
    private Button button(String s,int size){Button b=new Button(this);b.setText(s);b.setTextSize(size);b.setFocusable(true);b.setFocusableInTouchMode(true);return b;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    @Override public boolean onKeyDown(int keyCode,KeyEvent e){if(keyCode==KeyEvent.KEYCODE_BACK&&scanner!=null&&scanner.isScanning()){stopScan();return true;}return super.onKeyDown(keyCode,e);}
    @Override protected void onDestroy(){if(scanner!=null)scanner.stop();super.onDestroy();}
}
