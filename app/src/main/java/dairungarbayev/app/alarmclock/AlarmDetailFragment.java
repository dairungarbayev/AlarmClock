package dairungarbayev.app.alarmclock;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmDetailFragment extends Fragment implements RingtoneChoiceDialogFragment.OnRingtoneChosen {

    public interface OnAlarmSettingsSetListener{
        void save(CustomAlarm alarm);
        void edit(CustomAlarm alarm);
        void delete(CustomAlarm alarm);
        void cancel();
    }

    private static final String TAG = "AlarmDetailFragment";

    private Button cancelButton, saveButton, deleteButton, chooseDateButton;
    private TextView overviewTextView;
    private TimePicker timePicker;
    private ToggleButton mondayToggle, tuesdayToggle, wednesdayToggle, thursdayToggle,
                            fridayToggle, saturdayToggle, sundayToggle;
    private TextView ringtoneName, ringtoneLabel;
    private Switch vibrationSwitch;
    private SeekBar ringtoneVolumeBar;

    private ArrayList<Integer> checkedWeekdays = new ArrayList<>();
    /*private Uri ringtoneUri;
    private int volume;
    private boolean isVibrationEnabled;
    private int hour, minute;
    private long date;*/
    private boolean isNew;

    private CustomAlarm alarm;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private OnAlarmSettingsSetListener alarmSettingsSetListener;

    public AlarmDetailFragment(CustomAlarm alarm) {
        this.alarm = alarm;
        setFields();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            alarmSettingsSetListener = (OnAlarmSettingsSetListener) getActivity();
        } catch (ClassCastException e){
            Log.e(TAG, "onAttach: " + e.getMessage() );
        }
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
        getReferencesToViews();
        setTogglesOnListeners();
        checkToggles();
        setRingtoneNameTextView();
        setOnRingtoneChoiceOpener();
        listenForVibrationSetting();
        setSeekBar();
        setTimePicker();
        setChooseDateButton();
        setOverViewTextView();
        setSaveButtonOnListener();
        setDeleteButton();
        setCancelButtonOnListener();
    }

    private void setFields(){
        this.checkedWeekdays = alarm.getCheckedWeekdays();
        this.isNew = alarm.isNew();
    }

    private void getReferencesToViews(){
        View view = getView();
        cancelButton = view.findViewById(R.id.button_cancel);
        saveButton = view.findViewById(R.id.button_save);
        deleteButton = view.findViewById(R.id.button_delete);
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

        ringtoneName = view.findViewById(R.id.label_ringtone_name);
        ringtoneLabel = view.findViewById(R.id.label_final_ringtone);
        vibrationSwitch = view.findViewById(R.id.switch_vibration);
        ringtoneVolumeBar = view.findViewById(R.id.seek_bar_ringtone_volume);
    }

    private void setTogglesOnListeners(){
        mondayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onWeekdayCheckedChanged(isChecked, Calendar.MONDAY);
        });
        tuesdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onWeekdayCheckedChanged(isChecked, Calendar.TUESDAY);
        });
        wednesdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onWeekdayCheckedChanged(isChecked, Calendar.WEDNESDAY);
        });
        thursdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onWeekdayCheckedChanged(isChecked, Calendar.THURSDAY);
        });
        fridayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onWeekdayCheckedChanged(isChecked, Calendar.FRIDAY);
        });
        saturdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onWeekdayCheckedChanged(isChecked, Calendar.SATURDAY);
        });
        sundayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onWeekdayCheckedChanged(isChecked, Calendar.SUNDAY);
        });
    }

    private void onWeekdayCheckedChanged(boolean isChecked, int dayOfWeek){
        if (!isRepeating() && isChecked){
            String msg = getString(R.string.repeat_enabled_message);
            Toast toast = Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT);
            toast.show();
        }
        if (isChecked && !checkedWeekdays.contains(dayOfWeek)){
            checkedWeekdays.add(dayOfWeek);
        } else if (!isChecked && checkedWeekdays.contains(dayOfWeek)){
            checkedWeekdays.remove(Integer.valueOf(dayOfWeek));
        }
        setOverViewTextView();
    }

    private void checkToggles(){
        if (checkedWeekdays.contains(Calendar.MONDAY)){
            mondayToggle.setChecked(true);
        }
        if (checkedWeekdays.contains(Calendar.TUESDAY)){
            tuesdayToggle.setChecked(true);
        }
        if (checkedWeekdays.contains(Calendar.WEDNESDAY)){
            wednesdayToggle.setChecked(true);
        }
        if (checkedWeekdays.contains(Calendar.THURSDAY)){
            thursdayToggle.setChecked(true);
        }
        if (checkedWeekdays.contains(Calendar.FRIDAY)){
            fridayToggle.setChecked(true);
        }
        if (checkedWeekdays.contains(Calendar.SATURDAY)){
            saturdayToggle.setChecked(true);
        }
        if (checkedWeekdays.contains(Calendar.SUNDAY)){
            sundayToggle.setChecked(true);
        }
    }

    private View.OnClickListener ringtoneChoiceOpener = v -> {
        RingtoneChoiceDialogFragment dialog = new RingtoneChoiceDialogFragment(getContext(),alarm.getRingtoneUri());
        dialog.setTargetFragment(AlarmDetailFragment.this, 222);
        dialog.show(getFragmentManager(), "RingtoneChoiceDialog");
    };

    private void setRingtoneNameTextView(){
        ringtoneName.setText(RingtoneManager.getRingtone(getContext(),alarm.getRingtoneUri()).getTitle(getContext()));
    }

    private void setOnRingtoneChoiceOpener(){
        ringtoneName.setOnClickListener(ringtoneChoiceOpener);
        ringtoneLabel.setOnClickListener(ringtoneChoiceOpener);
    }

    @Override
    public void sendRingtoneData(String title, Uri uri) {
        ringtoneName.setText(title);
        alarm.setRingtoneUri(uri);
    }

    private void listenForVibrationSetting(){
        vibrationSwitch.setChecked(alarm.isVibrationOn());

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.setVibrationOn(isChecked);
        });
    }

    private void setSeekBar(){
        AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        ringtoneVolumeBar.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        ringtoneVolumeBar.setProgress(alarm.getVolume());

        ringtoneVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alarm.setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private boolean isRepeating(){
        return !checkedWeekdays.isEmpty();
    }

    private void setTimePicker(){
        timePicker.setIs24HourView(true);
        timePicker.setHour(alarm.getHour());
        timePicker.setMinute(alarm.getMinute());
        timePicker.setOnTimeChangedListener((view, newHour, newMinute) -> {
            alarm.setHour(newHour);
            alarm.setMinute(newMinute);
            setOverViewTextView();
        });
    }

    private void setChooseDateButton(){
        setOverViewTextView();

        dateSetListener = (view, year, month, dayOfMonth) -> {
            alarm.setDate(getTimeInMillis(year, month, dayOfMonth));
            if (isRepeating()){
                String msg = getResources().getString(R.string.repeat_disabled_message);
                Toast toast = Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT);
                toast.show();
            }

            mondayToggle.setChecked(false);
            tuesdayToggle.setChecked(false);
            wednesdayToggle.setChecked(false);
            thursdayToggle.setChecked(false);
            fridayToggle.setChecked(false);
            saturdayToggle.setChecked(false);
            sundayToggle.setChecked(false);

            setOverViewTextView();
            checkedWeekdays.clear();
        };
        chooseDateButton.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    getContext(),
                    dateSetListener,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            dialog.show();
        });
    }

    private void setAlarm(){
        alarm.setCheckedWeekdays(checkedWeekdays);
        alarm.setNotNew();
    }

    private void setCancelButtonOnListener(){
        cancelButton.setOnClickListener(v -> {
            alarmSettingsSetListener.cancel();
        });
    }

    private void setSaveButtonOnListener(){
        saveButton.setOnClickListener(v -> {
            setAlarm();
            if (isNew) {
                alarmSettingsSetListener.save(alarm);
            } else alarmSettingsSetListener.edit(alarm);
        });
    }

    private void setDeleteButton(){
        if (isNew){
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                alarmSettingsSetListener.delete(alarm);
            });
        }
    }

    private void setOverViewTextView(){
        if (isRepeating()){
            overviewTextView.setText(Statics.getOverviewTextRepeating(getContext(),checkedWeekdays));
        } else {
            overviewTextView.setText(Statics.getOverviewTextOneShot(alarm.getDate()));
        }
    }

    private long getTimeInMillis(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0,0,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }
}
