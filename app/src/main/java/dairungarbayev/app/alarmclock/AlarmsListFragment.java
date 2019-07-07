package dairungarbayev.app.alarmclock;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmsListFragment extends Fragment implements AlarmDetailFragment.OnAlarmSettingsSetListener {

    private final String ALARM_JSON_KEY = "alarm_json_";

    private Button addButton;
    private LinearLayout alarmsListLayout;

    private ArrayList<CustomAlarm> alarmsList = new ArrayList<>();

    public AlarmsListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarms_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getReferencesToViews();
        getData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveData();
    }

    private void getReferencesToViews(){
        View view = getView();
        alarmsListLayout = view.findViewById(R.id.alarms_list_holder);
        addButton = view.findViewById(R.id.button_add_alarm);
    }

    private void getData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        int size = prefs.getAll().size();

        if (size != 0){
            for (int i = 0; i < size; i++){
                alarmsList.add(new CustomAlarm(prefs.getString(ALARM_JSON_KEY+i, "")));

                editor.remove(ALARM_JSON_KEY+i);
            }
            editor.apply();
        }
    }

    @Override
    public void save(CustomAlarm alarm) {
        alarmsList.add(alarm);
    }

    @Override
    public void edit(CustomAlarm alarm) {
        for (int i = 0; i < alarmsList.size(); i++){
            if (alarmsList.get(i).getId() == alarm.getId()){
                alarmsList.set(i, alarm);
                break;
            }
        }
    }

    @Override
    public void delete(CustomAlarm alarm) {
        for (int i = 0; i < alarmsList.size(); i++){
            if (alarmsList.get(i).getId() == alarm.getId()){
                alarmsList.remove(i);
                break;
            }
        }
    }

    private void saveData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < alarmsList.size(); i++){
            editor.putString(ALARM_JSON_KEY+i, alarmsList.get(i).toJsonString());
        }
        editor.apply();
    }
}
