package dairungarbayev.app.alarmclock;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmDetailFragment extends Fragment implements RingtoneChoiceDialogFragment.OnRingtoneChosen {

    public interface OnAlarmSettingsSet{
        void save(ArrayList<Integer> weekdays, long date, Uri ringtoneUri, int volume,
                  boolean isVibrationEnabled, int hour, int minute);
        void edit(int index, ArrayList<Integer> weekdays, long date, Uri ringtoneUri, int volume,
                  boolean isVibrationEnabled, int hour, int minute);
        void delete(int index);
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

    private int index;
    private ArrayList<Integer> checkedWeekdays = new ArrayList<>();
    private Uri ringtoneUri;
    private int volume;
    private boolean isVibrationEnabled;
    private boolean isNew;
    private int hour, minute;
    private long date = Calendar.getInstance().getTimeInMillis();

    private DatePickerDialog.OnDateSetListener dateSetListener;

    public AlarmDetailFragment() {
        isNew = true;
    }

    public AlarmDetailFragment(int index, ArrayList<Integer> weekdays, Uri ringtoneUri, int volume,
                               boolean isVibrationEnabled, int hour, int minute){
        this.index = index;
        this.checkedWeekdays = weekdays;
        this.ringtoneUri = ringtoneUri;
        this.volume = volume;
        this.isVibrationEnabled = isVibrationEnabled;
        this.hour = hour;
        this.minute = minute;
        isNew = false;
    }

    public AlarmDetailFragment(int index, long date, Uri ringtoneUri, int volume,
                               boolean isVibrationEnabled, int hour, int minute){
        this.index = index;
        this.date = date;
        this.ringtoneUri = ringtoneUri;
        this.volume = volume;
        this.isVibrationEnabled = isVibrationEnabled;
        this.hour = hour;
        this.minute = minute;
        isNew = false;
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
        setOverViewTextView();
        if (isChecked && !checkedWeekdays.contains(dayOfWeek)){
            checkedWeekdays.add(dayOfWeek);
        } else if (!isChecked && checkedWeekdays.contains(dayOfWeek)){
            checkedWeekdays.remove(dayOfWeek);
        }
        Log.d(TAG, "onWeekdayCheckedChanged: " + checkedWeekdays);
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

        int[] calendarWeekdayConsts = new int[7];
        calendarWeekdayConsts[0] = Calendar.MONDAY;
        calendarWeekdayConsts[1] = Calendar.TUESDAY;
        calendarWeekdayConsts[2] = Calendar.WEDNESDAY;
        calendarWeekdayConsts[3] = Calendar.THURSDAY;
        calendarWeekdayConsts[4] = Calendar.FRIDAY;
        calendarWeekdayConsts[5] = Calendar.SATURDAY;
        calendarWeekdayConsts[6] = Calendar.SUNDAY;

        for (int i = 0; i<7; i++){
            if (checkedWeekdays.contains(calendarWeekdayConsts[i])){
                buffer.append(weekdaysString[i]);
                buffer.append(" ");
            }
        }

        overviewTextView.setText(buffer.toString());
    }

    private void setOverViewTextOneShot(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        if (calendar.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()){
            date = calendar.getTimeInMillis();
        } else date = calendar.getTimeInMillis() + 86400000L;

        Date dateObj = new Date(date);
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd");
        String dateFormat = format.format(dateObj);
        overviewTextView.setText(dateFormat);
    }

    private View.OnClickListener ringtoneChoiceOpener = v -> {
        RingtoneChoiceDialogFragment dialog = new RingtoneChoiceDialogFragment();
        dialog.setTargetFragment(AlarmDetailFragment.this, 222);
        dialog.show(getFragmentManager(), "RingtoneChoiceDialog");
    };

    private void setRingtoneNameTextView(){
        if (isNew){
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        ringtoneName.setText(RingtoneManager.getRingtone(getContext(),ringtoneUri).getTitle(getContext()));
    }

    private void setOnRingtoneChoiceOpener(){
        ringtoneName.setOnClickListener(ringtoneChoiceOpener);
        ringtoneLabel.setOnClickListener(ringtoneChoiceOpener);
    }

    @Override
    public void sendRingtoneData(String title, Uri uri) {
        ringtoneName.setText(title);
        ringtoneUri = uri;
    }

    private void listenForVibrationSetting(){
        if (!isNew){
            vibrationSwitch.setChecked(isVibrationEnabled);
        }
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVibrationEnabled = isChecked;
        });
    }

    private void setSeekBar(){
        AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        ringtoneVolumeBar.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        if (isNew) {
            ringtoneVolumeBar.setProgress(manager.getStreamVolume(AudioManager.STREAM_ALARM));
        } else ringtoneVolumeBar.setProgress(volume);

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
        return checkedWeekdays.isEmpty();
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
            setOverViewTextView();
        });
    }

    private void setChooseDateButton(){
        if (isNew) {
            date = Calendar.getInstance().getTimeInMillis();
        }
        setOverViewTextView();

        dateSetListener = (view, year, month, dayOfMonth) -> {
            date = getTimeInMillis(year, month, dayOfMonth);
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

    private void setCancelButtonOnListener(){
        cancelButton.setOnClickListener(v -> {

        });
    }

    private void setSaveButtonOnListener(){
        saveButton.setOnClickListener(v -> {

        });
    }

    private void setDeleteButton(){
        if (isNew){
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {

            });
        }
    }

    private void setOverViewTextView(){
        if (isRepeating()){
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
