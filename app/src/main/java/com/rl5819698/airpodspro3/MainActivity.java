package com.rl5819698.airpodspro3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(18, 18, 18, 18);
        root.setBackgroundColor(0xFF101216);

        TextView title = new TextView(this);
        title.setText("מצב מפתח");
        title.setTextSize(26);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title, new LinearLayout.LayoutParams(-1, 55));

        TextView info = new TextView(this);
        info.setText("כלי מהיר לפתיחת אפשרויות המפתחים של Android");
        info.setTextSize(15);
        info.setTextColor(0xFFB8C0CC);
        info.setGravity(Gravity.CENTER);
        root.addView(info, new LinearLayout.LayoutParams(-1, 55));

        Button open = new Button(this);
        open.setText("1  •  פתח מצב מפתח");
        open.setTextSize(18);
        open.setOnClickListener(v -> openDeveloperOptions());
        root.addView(open, new LinearLayout.LayoutParams(-1, 65));

        status = new TextView(this);
        status.setText("מוכן  •  לחץ 1");
        status.setTextSize(15);
        status.setTextColor(0xFF8FD3FF);
        status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(-1, 55));

        TextView help = new TextView(this);
        help.setText("7 = פתיחה\n0 = חזרה\n\nהאפליקציה אינה כוללת Bluetooth או פונקציות של אוזניות.");
        help.setTextSize(14);
        help.setTextColor(0xFF7E8794);
        help.setGravity(Gravity.CENTER);
        root.addView(help, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(root);
    }

    private void openDeveloperOptions() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            status.setText("אפשרויות המפתחים נפתחו");
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                status.setText("נפתחו הגדרות Android");
            } catch (Exception ignored) {
                status.setText("לא ניתן לפתוח את ההגדרות");
            }
        }
        Toast.makeText(this,
                "נפתח מסך אפשרויות המפתחים. את ההפעלה עצמה צריך לבצע במכשיר.",
                Toast.LENGTH_LONG).show();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_7:
                openDeveloperOptions();
                return true;
            case KeyEvent.KEYCODE_0:
                finish();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
