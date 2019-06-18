package dairungarbayev.app.alarmclock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.set_alarm);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),JumpingButtonActivity.class);
                PendingIntent pi = PendingIntent.getActivity(
                        getApplicationContext(),333,intent,PendingIntent.FLAG_CANCEL_CURRENT);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 10);
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
            }
        });
    }
}
