package dairungarbayev.app.alarmclock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class JumpingButtonActivity extends AppCompatActivity {

    private final String ALARM_JSON = "alarm_json";

    private FloatingActionButton turnOffButton;
    private CustomAlarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        setContentView(R.layout.activity_jumping_button);

        String json = getIntent().getExtras().getString(ALARM_JSON);
        if (json != null && !json.isEmpty()){
            alarm = new CustomAlarm(getApplicationContext(), json);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),"Error: alarm JSON null or empty", Toast.LENGTH_SHORT);
            toast.show();
        }


        turnOffButton = findViewById(R.id.turn_off_button);
        turnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarm != null){
                    if (alarm.isRepeating()){
                        alarm.setAlarmOn();
                    } else alarm.cancelAlarm();
                }
                finish();
            }
        });
    }
}
