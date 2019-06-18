package dairungarbayev.app.alarmclock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class JumpingButtonActivity extends AppCompatActivity {

    private FloatingActionButton turnOffButton;

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
        turnOffButton = findViewById(R.id.turn_off_button);
        turnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
