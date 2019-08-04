package dairungarbayev.app.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            if (prefs.getAll().size() > 0) {
                ArrayList<String> jsonList = new ArrayList(prefs.getAll().values());
                for (String json : jsonList){
                    CustomAlarm alarm = new CustomAlarm(context.getApplicationContext(),json);
                    if (alarm.getState()){
                        alarm.setAlarmOn();
                    }
                }
            }
        }
    }
}
