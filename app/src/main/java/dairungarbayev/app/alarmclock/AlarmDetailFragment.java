package dairungarbayev.app.alarmclock;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmDetailFragment extends Fragment implements RingtoneChoiceDialogFragment.OnRingtoneChosen {

    private Button cancelButton, saveButton, chooseDateButton;
    private TextView overviewTextView;
    private TimePicker timePicker;
    private ToggleButton mondayToggle, tuesdayToggle, wednesdayToggle, thursdayToggle,
                            fridayToggle, saturdayToggle, sundayToggle;
    private TextView ringtoneName, ringtoneLabel;
    private Switch vibrationSwitch;
    private SeekBar ringtoneVolumeBar;

    private boolean[] weekdays = new boolean[7];
    private String ringtoneTitle;
    private Uri ringtoneUri;
    private int volume;
    private boolean isVibrationEnabled;
    private boolean isNew, isRepeating;
    private int hour, minute;
    private long date;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    public AlarmDetailFragment() {
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
        setOnRingtoneChoiceOpener();
        listenForVibrationSetting();
        setSeekBar();
        setTimePicker();
        setChooseDateButton();
        setOverViewTextView();
    }

    private void getReferencesToViews(){
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

        ringtoneName = view.findViewById(R.id.label_ringtone_name);
        ringtoneLabel = view.findViewById(R.id.label_final_ringtone);
        vibrationSwitch = view.findViewById(R.id.switch_vibration);
        ringtoneVolumeBar = view.findViewById(R.id.seek_bar_ringtone_volume);
    }

    private void setTogglesOnListeners(){
        mondayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekdays[0] = isChecked;
            onWeekdayCheckedChanged(isChecked);
        });
        tuesdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekdays[1] = isChecked;
            onWeekdayCheckedChanged(isChecked);
        });
        wednesdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekdays[2] = isChecked;
            onWeekdayCheckedChanged(isChecked);
        });
        thursdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekdays[3] = isChecked;
            onWeekdayCheckedChanged(isChecked);
        });
        fridayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekdays[4] = isChecked;
            onWeekdayCheckedChanged(isChecked);
        });
        saturdayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekdays[5] = isChecked;
            onWeekdayCheckedChanged(isChecked);
        });
        sundayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekdays[6] = isChecked;
            onWeekdayCheckedChanged(isChecked);
        });
    }

    private void onWeekdayCheckedChanged(boolean isChecked){
        if (!isRepeating && isChecked){
            String msg = getString(R.string.repeat_enabled_message);
            Toast toast = Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT);
            toast.show();
        }
        isRepeating = isRepeating();
        setOverViewTextView();
    }

    private void setOverviewTextRepeating(){
        StringBuffer buffer = new StringBuffer();
        String[] weekdaysString = new String[7];
        weekdaysString[0] = getResources().getString(R.string.monday);
        weekdaysString[1] = getResources().getString(R.string.tuesday);
        weekdaysString[2] = getResources().getString(R.string.wednesday);
        weekdaysString[3] = getResources().getString(R.string.thursday);
        weekdaysString[4] = getResources().getString(R.string.friday);
        weekdaysString[5] = getResources().getString(R.string.saturday);
        weekdaysString[6] = getResources().getString(R.string.sunday);

        for (int i = 0; i<7; i++){
            if (weekdays[i]){
                buffer.append(weekdaysString[i]);
                buffer.append(" ");
            }
        }

        overviewTextView.setText(buffer.toString());
    }

    private void setOverViewTextOneShot(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        Date date = new Date(cal.getTimeInMillis());
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd");
        String dateFormat = format.format(date);
        overviewTextView.setText(dateFormat);
    }

    private View.OnClickListener ringtoneChoiceOpener = v -> {
        RingtoneChoiceDialogFragment dialog = new RingtoneChoiceDialogFragment();
        dialog.setTargetFragment(AlarmDetailFragment.this, 222);
        dialog.show(getFragmentManager(), "RingtoneChoiceDialog");
    };

    private void setOnRingtoneChoiceOpener(){
        ringtoneName.setOnClickListener(ringtoneChoiceOpener);
        ringtoneLabel.setOnClickListener(ringtoneChoiceOpener);
    }

    @Override
    public void sendRingtoneData(String title, Uri uri) {
        ringtoneTitle = title;
        ringtoneUri = uri;
    }

    private void listenForVibrationSetting(){
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVibrationEnabled = isChecked;
        });
    }

    private void setSeekBar(){
        AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        ringtoneVolumeBar.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        ringtoneVolumeBar.setProgress(manager.getStreamVolume(AudioManager.STREAM_ALARM));

        ringtoneVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = progress;
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
        boolean isRepeating = false;
        for (int i = 0; i<7; i++){
            if (weekdays[i]){
                isRepeating = true;
            }
        }
        return isRepeating;
    }

    private void setTimePicker(){
        timePicker.setIs24HourView(true);
        Calendar cal = Calendar.getInstance();
        if (isNew){
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
        }
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        timePicker.setOnTimeChangedListener((view, newHour, newMinute) -> {
            hour = newHour;
            minute = newMinute;
        });
    }

    private void setChooseDateButton(){
        date = Calendar.getInstance().getTimeInMillis();
        setOverViewTextView();

        dateSetListener = (view, year, month, dayOfMonth) -> {
            date = getTimeInMillis(year, month, dayOfMonth);
            if (isRepeating){
                String msg = getResources().getString(R.string.repeat_disabled_message);
                Toast toast = Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT);
                toast.show();
            }
            isRepeating = false;
            setOverViewTextView();
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

    private void setOverViewTextView(){
        if (isRepeating){
            setOverviewTextRepeating();
        } else {
            setOverViewTextOneShot();
        }
    }

    private long getTimeInMillis(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0,0,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }
}
