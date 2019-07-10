package dairungarbayev.app.alarmclock;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmsListFragment extends Fragment {

    interface AlarmsListInterface{
        void listToNewSettings();
        void listToExistingSettings(CustomAlarm alarm);
    }

    private static final String TAG = "AlarmsListFragment";

    private FloatingActionButton addButton;
    private LinearLayout alarmsListLayout;

    private ArrayList<CustomAlarm> alarms;

    private AlarmsListInterface alarmsListInterface;

    public AlarmsListFragment(ArrayList<CustomAlarm> alarms) {
        this.alarms = alarms;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            alarmsListInterface = (AlarmsListInterface) getActivity();
        } catch (ClassCastException e){
            Log.e(TAG, "onAttach: "+e.getMessage() );
        }
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
        populateList();
        setAddButton();
    }

    private void getReferencesToViews(){
        View view = getView();
        alarmsListLayout = view.findViewById(R.id.alarms_list_holder);
        addButton = view.findViewById(R.id.button_add_alarm);
    }

    private void populateList(){
        for (int i = 0; i<alarms.size(); i++){
            addAlarmCard(alarms.get(i));
        }
    }

    private void addAlarmCard(CustomAlarm alarm){
        CardView cardView = new CardView(getContext());
        RelativeLayout layout = new RelativeLayout(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);
        cardView.addView(layout);

        int margin = getResources().getDimensionPixelSize(R.dimen.alarm_card_margin);

        TextView timeView = new TextView(getContext());
        String timeText = String.format("%02d",alarm.getHour())+":"+String.format("%02d",alarm.getMinute());
        timeView.setText(timeText);
        timeView.setTextSize(getResources().getDimensionPixelSize(R.dimen.time_text_size));
        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        timeParams.topMargin = margin;
        timeParams.bottomMargin = margin;
        timeParams.leftMargin = margin;
        timeView.setLayoutParams(timeParams);
        layout.addView(timeView);

        TextView overview = new TextView(getContext());
        if (alarm.isRepeating()){
            overview.setText(Statics.getOverviewTextRepeating(getContext(),alarm.getCheckedWeekdays()));
        } else overview.setText(Statics.getOverviewTextOneShot(alarm.getNextAlarmTime()));
        overview.setTextSize(getResources().getDimensionPixelSize(R.dimen.time_text_size));
        RelativeLayout.LayoutParams overviewParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        overviewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        overview.setLayoutParams(overviewParams);
        layout.addView(overview);

        Switch toggle = new Switch(getContext());
        RelativeLayout.LayoutParams toggleParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toggleParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        toggleParams.addRule(RelativeLayout.CENTER_VERTICAL);
        toggle.setLayoutParams(toggleParams);
        layout.addView(toggle);
        toggle.setChecked(alarm.getState());

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                alarm.setAlarmOn();
                alarm.showTimeIntervalToast();
            } else alarm.cancelAlarm();
        });

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(cardParams);
        alarmsListLayout.addView(cardView);

        cardView.setOnClickListener(v -> alarmsListInterface.listToExistingSettings(alarm));
    }

    private void setAddButton(){
        addButton.setOnClickListener(v -> alarmsListInterface.listToNewSettings());
    }
}
