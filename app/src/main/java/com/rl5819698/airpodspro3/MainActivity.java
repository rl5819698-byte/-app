package com.rl5819698.airpodspro3;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
    private static final int REQ_BT = 42;
    private AirPodsScanner scanner;
    private ProximityFinder finder;
    private TextView state, battery, details, finderText;
    private Button scanButton, findButton;
    private AirPodsStatus latest;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finder=new ProximityFinder(new ProximityFinder.Listener(){
            public void onLevel(int rssi,String text){ finderText.setText("מצא: "+text+"  |  "+rssi+" dBm"); }
        });
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        scanner=new AirPodsScanner(adapter,this);
        buildUi();
        if(adapter==null) showState("אין Bluetooth במכשיר");
    }

    private void buildUi() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10,8,10,8); root.setBackgroundColor(0xFF101216);

        TextView title=text("AIRPODS PRO 3",22,true,0xFFFFFFFF); root.addView(title,lp(-1,42));
        state=text("מוכן  •  מקשים: 1=סריקה  2=מצא  0=עצור",13,false,0xFFB8C0CC); root.addView(state,lp(-1,34));

        battery=text("L  —     R  —     CASE  —",19,true,0xFFFFFFFF); battery.setGravity(Gravity.CENTER);
        battery.setBackgroundColor(0xFF1C2027); root.addView(battery,lp(-1,52));

        finderText=text("מצא: לא פעיל",15,true,0xFF8FD3FF); finderText.setGravity(Gravity.CENTER);
        root.addView(finderText,lp(-1,42));

        scanButton=button("1  •  סרוק",17); root.addView(scanButton,lp(-1,55));
        scanButton.setOnClickListener(new View.OnClickListener(){public void onClick(View v){if(scanner.isScanning())stopScan();else requestAndScan();}});

        findButton=button("2  •  מצא בקרבה",17); root.addView(findButton,lp(-1,55));
        findButton.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggleFinder();}});

        ScrollView scroll=new ScrollView(this);
        LinearLayout menu=new LinearLayout(this); menu.setOrientation(LinearLayout.VERTICAL);
        addMenu(menu,"3  •  מצב האוזניות","סוללה / RSSI / זיהוי באוזן");
        addMenu(menu,"4  •  Bluetooth","פתיחת הגדרות Bluetooth");
        addMenu(menu,"5  •  שמאל","מעקב RSSI של האוזנייה השמאלית");
        addMenu(menu,"6  •  ימין","מעקב RSSI של האוזנייה הימנית");
        addMenu(menu,"7  •  מצב מפתחים","פתיחת אפשרויות המפתחים של Android");
        addMenu(menu,"8  •  מידע","דגם וכתובת Bluetooth");
        addMenu(menu,"9  •  עזרה","מקשי הטלפון");
        scroll.addView(menu); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));

        TextView footer=text("מקשי 0–9  •  בחירה עם 5 / OK  •  חזרה עם ←",12,false,0xFF7E8794);
        footer.setGravity(Gravity.CENTER); root.addView(footer,lp(-1,30));
        setContentView(root);
    }

    private void addMenu(LinearLayout p,final String title,final String sub){
        Button b=button(title+"\n"+sub,14); b.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ if(title.startsWith("3")) showLatest(); else if(title.startsWith("4")) openBluetooth(); else if(title.startsWith("5")||title.startsWith("6")) toggleFinder(); else if(title.startsWith("7")) openDeveloperOptions(); else if(title.startsWith("8")) showLatest(); else Toast.makeText(MainActivity.this,"1 סריקה | 2 מצא | 3 מצב | 4 Bluetooth | 0 עצור",Toast.LENGTH_LONG).show();}});
        p.addView(b,lp(-1,60));
    }

    private void toggleFinder(){
        if(!finder.isActive()){finder.start();findButton.setText("2  •  עצור מציאה");finderText.setText("מצא: מתבצע מעקב…"); if(!scanner.isScanning()) requestAndScan();}
        else {finder.stop();findButton.setText("2  •  מצא בקרבה");finderText.setText("מצא: לא פעיל");}
    }

    private void requestAndScan(){
        if(Build.VERSION.SDK_INT>=31){
            boolean scan=checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED;
            boolean connect=checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;
            if(!scan||!connect){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT},REQ_BT);return;}
        } else if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQ_BT);return;
        }
        BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter();
        if(a!=null&&!a.isEnabled()){try{startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));}catch(Exception ignored){}return;}
        startScan();
    }
    private void startScan(){scanner.start();if(scanner.isScanning()){scanButton.setText("1  •  עצור");showState("סורק BLE…");}}
    private void stopScan(){scanner.stop();scanButton.setText("1  •  סרוק");showState("הסריקה הופסקה");}

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_BT){
            boolean ok=true;
            if(Build.VERSION.SDK_INT>=31) ok=checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED;
            else if(Build.VERSION.SDK_INT>=23) ok=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
            if(ok)startScan();else showState("נדרשת הרשאת Bluetooth");
        }
    }

    @Override public void onFound(final AirPodsStatus s,BluetoothDevice d){
        runOnUiThread(new Runnable(){public void run(){latest=s;showStatus(s);if(finder.isActive())finder.update(s.rssi);}});
    }
    @Override public void onError(final String m){runOnUiThread(new Runnable(){public void run(){showState(m);Toast.makeText(MainActivity.this,m,Toast.LENGTH_SHORT).show();}});}

    private void showLatest(){if(latest==null)showState("עדיין לא נמצאו AirPods");else showStatus(latest);}
    private void showStatus(AirPodsStatus s){
        String l=s.left>=0?s.left+"%":"—",r=s.right>=0?s.right+"%":"—",c=s.caseBattery>=0?s.caseBattery+"%":"—";
        battery.setText("L "+l+(s.leftCharging?" ⚡":"")+"     R "+r+(s.rightCharging?" ⚡":"")+"     CASE "+c);
        details=text(String.format(Locale.US,"דגם: %s\nMAC: %s\nRSSI: %d dBm\nבאוזן: %s",s.model,s.mac,s.rssi,(s.leftInEar||s.rightInEar)?"כן":"לא"),14,false,0xFFFFFFFF);
        Toast.makeText(this,details.getText(),Toast.LENGTH_LONG).show();
        showState("נמצא: "+s.model);
    }
    private void openBluetooth(){try{startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));}catch(Exception e){Toast.makeText(this,"Bluetooth",Toast.LENGTH_SHORT).show();}}\n    private void openDeveloperOptions(){\n        try { startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); }\n        catch(Exception e) { try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); } catch(Exception ignored) {} }\n        Toast.makeText(this,"נפתח מסך אפשרויות המפתחים. את ההפעלה עצמה צריך לאשר במכשיר.",Toast.LENGTH_LONG).show();\n    }
    private void showState(String s){state.setText(s);}
    private TextView text(String s,int size,boolean bold,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setGravity(Gravity.RIGHT);return t;}
    private Button button(String s,int size){Button b=new Button(this);b.setText(s);b.setTextSize(size);b.setFocusable(true);b.setFocusableInTouchMode(true);return b;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    @Override public boolean onKeyDown(int keyCode,KeyEvent e){
        switch(keyCode){
            case KeyEvent.KEYCODE_0: stopScan(); if(finder.isActive())toggleFinder(); return true;
            case KeyEvent.KEYCODE_1: if(scanner.isScanning())stopScan();else requestAndScan(); return true;
            case KeyEvent.KEYCODE_2: toggleFinder(); return true;
            case KeyEvent.KEYCODE_3: showLatest(); return true;
            case KeyEvent.KEYCODE_4: openBluetooth(); return true;
            case KeyEvent.KEYCODE_5: toggleFinder(); return true;
            case KeyEvent.KEYCODE_6: toggleFinder(); return true;
            case KeyEvent.KEYCODE_7: openDeveloperOptions(); return true;
            case KeyEvent.KEYCODE_9: Toast.makeText(this,"1 סריקה | 2 מצא | 3 מצב | 4 Bluetooth | 7 מפתחים | 0 עצור",Toast.LENGTH_LONG).show(); return true;
            case KeyEvent.KEYCODE_BACK: stopScan(); if(finder.isActive())finder.stop(); return super.onKeyDown(keyCode,e);
        }
        return super.onKeyDown(keyCode,e);
    }
    @Override protected void onDestroy(){if(scanner!=null)scanner.stop();if(finder!=null)finder.close();super.onDestroy();}
}
