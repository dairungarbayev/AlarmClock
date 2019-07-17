package dairungarbayev.app.alarmclock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity
        implements AlarmDetailFragment.OnAlarmSettingsSetListener, AlarmsListFragment.AlarmsListInterface {

    private final String ALARM_JSON_KEY = "alarm_json_";
    private final String ALARMS_COUNTER_KEY = "alarms_counter";
    private final int CAMERA_PERMISSION_CODE = 123;
    private ArrayList<CustomAlarm> alarmsList = new ArrayList<>();

    private boolean isListInflated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissionsGranted();

        getData();

        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        AlarmsListFragment alarmsListFragment = new AlarmsListFragment(alarmsList);
        transaction.add(R.id.main_activity_view_holder,alarmsListFragment);
        transaction.commit();
        isListInflated = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();

        if (isListInflated){
            FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
            AlarmsListFragment alarmsListFragment = new AlarmsListFragment(alarmsList);
            transaction.replace(R.id.main_activity_view_holder,alarmsListFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void checkPermissionsGranted(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                new AlertDialog.Builder(this)
                        .setMessage(R.string.camera_permission_denied)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, (dialog, which) -> checkPermissionsGranted())
                        .setNegativeButton(R.string.cancel, (dialog, which) -> MainActivity.this.finish())
                        .create().show();
            }
        }

    }

    private void getData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        CustomAlarm.setCounter(prefs.getInt(ALARMS_COUNTER_KEY,1));
        editor.remove(ALARMS_COUNTER_KEY);
        editor.apply();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();

        ArrayList<String> alarmJsons = new ArrayList(prefs.getAll().values());

        alarmsList.clear();

        if (alarmJsons.size() != 0){
            for (int i = 0; i < alarmJsons.size(); i++){
                if (!alarmJsons.get(i).isEmpty()) {
                    alarmsList.add(new CustomAlarm(getApplicationContext(), alarmJsons.get(i)));
                }
            }
            editor.putInt(ALARMS_COUNTER_KEY, CustomAlarm.getCounter());
            editor.apply();

            if (!alarmsList.isEmpty()){
                Collections.sort(alarmsList, new Comparator<CustomAlarm>() {
                    @Override
                    public int compare(CustomAlarm alarm1, CustomAlarm alarm2) {
                        return alarm1.getId() - alarm2.getId();
                    }
                });
            }
        }
    }

    @Override
    public void save(CustomAlarm alarm) {
        alarmsList.add(alarm);
        alarm.setAlarmOn();
        alarm.showTimeIntervalToast();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(ALARM_JSON_KEY+alarm.getId(),alarm.toJsonString());
        editor.putInt(ALARMS_COUNTER_KEY,CustomAlarm.getCounter());
        editor.apply();

        replaceSettingsToList();
    }

    @Override
    public void edit(CustomAlarm alarm) {
        editAlarm(alarm);

        alarm.setAlarmOn();
        alarm.showTimeIntervalToast();

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(ALARM_JSON_KEY+alarm.getId());
        editor.apply();

        replaceSettingsToList();
    }

    @Override
    public void cancel() {
        replaceSettingsToList();
    }

    private void replaceSettingsToList(){
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        FragmentTransaction transaction = manager.beginTransaction();
        AlarmsListFragment alarmsListFragment = new AlarmsListFragment(alarmsList);
        transaction.replace(R.id.main_activity_view_holder,alarmsListFragment);
        transaction.commit();
        isListInflated = true;
    }

    @Override
    public void listToNewSettings() {
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        AlarmDetailFragment detailFragment = new AlarmDetailFragment(new CustomAlarm(getApplicationContext()));
        transaction.replace(R.id.main_activity_view_holder,detailFragment).addToBackStack("AlarmsListFragment");
        transaction.commit();
        isListInflated = false;
    }

    @Override
    public void listToExistingSettings(CustomAlarm alarm) {
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        AlarmDetailFragment detailFragment = new AlarmDetailFragment(alarm);
        transaction.replace(R.id.main_activity_view_holder,detailFragment).addToBackStack("AlarmsListFragment");
        transaction.commit();
        isListInflated = false;
    }

    @Override
    public void editAlarmState(CustomAlarm alarm) {
        editAlarm(alarm);
    }

    private void editAlarm(CustomAlarm alarm){
        for (int i = 0; i < alarmsList.size(); i++){
            if (alarmsList.get(i).getId() == alarm.getId()){
                alarmsList.set(i, alarm);
                break;
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(ALARM_JSON_KEY+alarm.getId(),alarm.toJsonString());
        editor.apply();
    }

    private void saveData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        if (prefs.getAll().size() != 0){
            editor.clear();
            editor.apply();
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = prefs.edit();
        }
        CustomAlarm alarm;
        for (int i = 0; i < alarmsList.size(); i++){
            alarm = alarmsList.get(i);
            editor.putString(ALARM_JSON_KEY+alarm.getId(), alarm.toJsonString());
        }
        editor.putInt(ALARMS_COUNTER_KEY, CustomAlarm.getCounter());
        editor.apply();
    }
}
