package dairungarbayev.app.alarmclock;

import android.content.Context;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomAlarm {

    private static int counter = 0;
    private ArrayList<Integer> checkedWeekdays = new ArrayList<>();
    private long date;
    private Uri ringtoneUri;
    private int volume;
    private boolean vibrationOn, isNew;
    private int hour, minute, id;

    public CustomAlarm(Context context){
        this.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.volume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
        this.vibrationOn = false;
        this.hour = 6;
        this.minute = 0;
        this.date = getDefaultDate();
        this.id = counter;
        this.isNew = true;
        counter++;
    }

    public CustomAlarm(String json){
        Gson gson = new Gson();
        CustomAlarmData alarm = gson.fromJson(json,CustomAlarmData.class);
        this.ringtoneUri = Uri.parse(alarm.ringtoneUriString);
        this.volume = alarm.volume;
        this.vibrationOn = alarm.vibrationOn;
        this.date = alarm.date;
        this.checkedWeekdays = alarm.checkedWeekdays;
        this.hour = alarm.hour;
        this.minute = alarm.minute;
        this.id = alarm.id;
        this.isNew = false;
    }

    public void setCheckedWeekdays(ArrayList<Integer> checkedWeekdays) {
        this.checkedWeekdays = checkedWeekdays;
    }

    void setDate(long date) {
        this.date = date;
    }

    void setRingtoneUri(Uri ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    void setVolume(int volume) {
        this.volume = volume;
    }

    void setVibrationOn(boolean vibrationOn) {
        this.vibrationOn = vibrationOn;
    }

    void setHour(int hour) {
        this.hour = hour;
    }

    void setMinute(int minute) {
        this.minute = minute;
    }

    ArrayList<Integer> getCheckedWeekdays() {
        return checkedWeekdays;
    }

    long getDate() {
        return date;
    }

    Uri getRingtoneUri() {
        return ringtoneUri;
    }

    int getVolume() {
        return volume;
    }

    boolean isVibrationOn() {
        return vibrationOn;
    }

    int getHour() {
        return hour;
    }

    int getMinute() {
        return minute;
    }

    boolean isNew() {
        return isNew;
    }

    int getId() {
        return id;
    }

    private long getDefaultDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        if (calendar.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()){
            return calendar.getTimeInMillis();
        } else return calendar.getTimeInMillis() + 86400000L;
    }

    public String toJsonString(){
        CustomAlarmData data = new CustomAlarmData();
        data.hour = this.hour;
        data.minute = this.minute;
        data.id = this.id;
        data.checkedWeekdays = this.checkedWeekdays;
        data.date = this.date;
        data.ringtoneUriString = this.ringtoneUri.toString();
        data.vibrationOn = this.vibrationOn;
        data.volume = this.volume;

        Gson gson = new Gson();
        return gson.toJson(data);
    }

    private class CustomAlarmData {
        private int hour, minute, id;
        private ArrayList<Integer> checkedWeekdays = new ArrayList<>();
        private long date;
        private String ringtoneUriString;
        private int volume;
        private boolean vibrationOn;

        private CustomAlarmData() {
        }
    }
}
