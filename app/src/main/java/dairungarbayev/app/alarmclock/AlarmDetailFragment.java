package dairungarbayev.app.alarmclock;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmDetailFragment extends Fragment {

    private Button cancelButton, saveButton, chooseDateButton;
    private TextView overviewTextView;
    private TimePicker timePicker;
    private ToggleButton mondayToggle, tuesdayToggle, wednesdayToggle, thursdayToggle,
                            fridayToggle, saturdayToggle, sundayToggle;
    private TextView alarmLabel, ringtoneName;
    private Switch vibrationSwitch;
    private SeekBar ringtoneVolumeBar;
    private LinearLayout layoutRingtoneChoice;
    private CardView cardAlarmLabel;


    public AlarmDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        cancelButton = view.findViewById(R.id.button_cancel);
        saveButton = view.findViewById(R.id.button_save);
        chooseDateButton = view.findViewById(R.id.button_choose_date);

        overviewTextView = view.findViewById(R.id.text_view_overview);
        timePicker = view.findViewById(R.id.time_picker);

        mondayToggle = view.findViewById(R.id.toggle_monday);
        tuesdayToggle = view.findViewById(R.id.toggle_tuesday);
        wednesdayToggle = view.findViewById(R.id.toggle_wednesday);
        thursdayToggle = view.findViewById(R.id.toggle_thursday);
        fridayToggle = view.findViewById(R.id.toggle_friday);
        saturdayToggle = view.findViewById(R.id.toggle_saturday);
        sundayToggle = view.findViewById(R.id.toggle_sunday);

        cardAlarmLabel = view.findViewById(R.id.card_alarm_label);
        alarmLabel = view.findViewById(R.id.text_view_alarm_label);
        ringtoneName = view.findViewById(R.id.label_ringtone_name);
        layoutRingtoneChoice = view.findViewById(R.id.layout_ringtone_choice);
        vibrationSwitch = view.findViewById(R.id.switch_vibration);
        ringtoneVolumeBar = view.findViewById(R.id.seek_bar_ringtone_volume);
        //TODO get references to card views where appropriate
        //fix ringtone card - the purpose of the app is to wake smn up, not put notifications and reminders
    }
}
