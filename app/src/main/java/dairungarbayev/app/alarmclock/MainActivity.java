package dairungarbayev.app.alarmclock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements AlarmDetailFragment.OnAlarmSettingsSetListener, AlarmsListFragment.AlarmsListInterface {

    private final String ALARM_JSON_KEY = "alarm_json_";
    private ArrayList<CustomAlarm> alarmsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getData();

        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        AlarmsListFragment alarmsListFragment = new AlarmsListFragment(alarmsList);
        transaction.add(R.id.main_activity_view_holder,alarmsListFragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
    }

    private void getData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        int size = prefs.getAll().size();

        if (size != 0){
            for (int i = 0; i < size; i++){
                alarmsList.add(new CustomAlarm(getApplicationContext(),prefs.getString(ALARM_JSON_KEY+i, "")));

                editor.remove(ALARM_JSON_KEY+i);
            }
            editor.apply();
        }
    }

    @Override
    public void save(CustomAlarm alarm) {
        alarmsList.add(alarm);
        alarm.setAlarmOn();
        replaceSettingsToList();
    }

    @Override
    public void edit(CustomAlarm alarm) {
        for (int i = 0; i < alarmsList.size(); i++){
            if (alarmsList.get(i).getId() == alarm.getId()){
                alarmsList.set(i, alarm);
                alarmsList.get(i).setAlarmOn();
                break;
            }
        }
        replaceSettingsToList();
    }

    @Override
    public void delete(CustomAlarm alarm) {
        alarm.cancelAlarm();
        for (int i = 0; i < alarmsList.size(); i++){
            if (alarmsList.get(i).getId() == alarm.getId()){
                alarmsList.get(i).cancelAlarm();
                alarmsList.remove(i);
                break;
            }
        }
        replaceSettingsToList();
    }

    @Override
    public void cancel() {
        replaceSettingsToList();
    }

    private void replaceSettingsToList(){
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        AlarmsListFragment alarmsListFragment = new AlarmsListFragment(alarmsList);
        transaction.replace(R.id.main_activity_view_holder,alarmsListFragment);
        transaction.commit();
    }

    @Override
    public void listToNewSettings() {
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        AlarmDetailFragment detailFragment = new AlarmDetailFragment(new CustomAlarm(getApplicationContext()));
        transaction.replace(R.id.main_activity_view_holder,detailFragment);
        transaction.commit();
    }

    @Override
    public void listToExistingSettings(CustomAlarm alarm) {
        if (alarm.getState()){
            alarm.cancelAlarm();
        }
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        AlarmDetailFragment detailFragment = new AlarmDetailFragment(alarm);
        transaction.replace(R.id.main_activity_view_holder,detailFragment);
        transaction.commit();
    }

    private void saveData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < alarmsList.size(); i++){
            editor.putString(ALARM_JSON_KEY+i, alarmsList.get(i).toJsonString());
        }
        editor.apply();
    }
}
