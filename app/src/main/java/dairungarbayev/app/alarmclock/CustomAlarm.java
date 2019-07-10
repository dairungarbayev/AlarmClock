package dairungarbayev.app.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

class CustomAlarm {

    private final String ALARM_JSON = "alarm_json";

    private static int counter = 0;
    private ArrayList<Integer> checkedWeekdays = new ArrayList<>();
    private long date;
    private Uri ringtoneUri;
    private int volume;
    private boolean vibrationOn, isNew;
    private int hour, minute, id;
    private Context appContext;
    private boolean state = false;

    CustomAlarm(Context context){
        this.ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.volume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
        this.vibrationOn = false;
        this.hour = 6;
        this.minute = 0;
        this.date = getDefaultDate();
        this.id = counter;
        this.isNew = true;
        this.appContext = context;
        counter++;
    }

    CustomAlarm(Context context, String json){
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
        this.appContext = context;
        this.state = alarm.state;
    }

    void setAlarmOn(){
        getAlarmManager().set(AlarmManager.RTC_WAKEUP,getNextAlarmTime(),getPendingIntent());
        state = true;

        long interval = getNextAlarmTime() - Calendar.getInstance().getTimeInMillis();
        long days = interval / 86400000L;
        long hours = (interval - 86400000L*days)/3600000;
        long minutes = (interval - 86400000L*days - 3600000*hours)/60000;
        String intervalText = appContext.getResources().getString(R.string.next_alarm_set_for)+" ";
        String minutesText = minutes + " " + appContext.getResources().getString(R.string.minute) + " ";
        String hoursText;
        if (hours != 0){
            hoursText = hours + " " + appContext.getResources().getString(R.string.hour) + " ";
        } else hoursText = "";
        String daysText;
        if (days != 0){
            daysText = hours + " " + appContext.getResources().getString(R.string.day) + " ";
        } else daysText = "";
        intervalText += daysText + hoursText + minutesText;
        Toast toast = Toast.makeText(appContext,intervalText,Toast.LENGTH_SHORT);
        toast.show();
    }

    void cancelAlarm(){
        getAlarmManager().cancel(getPendingIntent());
        state = false;
    }

    private AlarmManager getAlarmManager(){
        return  (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getPendingIntent(){
        Intent intent = new Intent(appContext, JumpingButtonActivity.class);
        intent.putExtra(ALARM_JSON,toJsonString());
        return PendingIntent.getActivity(appContext,id,intent,PendingIntent.FLAG_CANCEL_CURRENT);
    }

    long getNextAlarmTime(){
        Calendar next = Calendar.getInstance();
        if (isRepeating()){
            ArrayList<Long> timeList = new ArrayList<>();
            for (int i = 0; i<checkedWeekdays.size(); i++){
                Calendar cal = Calendar.getInstance();
                long timeInMillis = cal.getTimeInMillis();
                for (int j = 0; j<7; j++){
                    cal.setTimeInMillis(timeInMillis);
                    if (cal.get(Calendar.DAY_OF_WEEK) == checkedWeekdays.get(i)){
                        timeList.add(timeInMillis);
                        break;
                    } else timeInMillis += 86400000L;
                }
            }
            long min = timeList.get(0);
            for (int i = 1; i < timeList.size(); i++){
                if (timeList.get(i) < min){
                    min = timeList.get(i);
                }
            }
            next.setTimeInMillis(min);
        } else next.setTimeInMillis(getDate());

        next.set(Calendar.HOUR_OF_DAY,hour);
        next.set(Calendar.MINUTE,minute);
        next.set(Calendar.SECOND,0);
        next.set(Calendar.MILLISECOND,0);
        if (!isRepeating()){
            if (next.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
                date += 86400000L;
                next.setTimeInMillis(date);
                next.set(Calendar.HOUR_OF_DAY,hour);
                next.set(Calendar.MINUTE,minute);
                next.set(Calendar.SECOND,0);
                next.set(Calendar.MILLISECOND,0);
            }
        }
        return next.getTimeInMillis();
    }

    void setCheckedWeekdays(ArrayList<Integer> checkedWeekdays) {
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

    public void setNotNew() {
        isNew = false;
    }

    ArrayList<Integer> getCheckedWeekdays() {
        return checkedWeekdays;
    }

    long getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        Calendar newCal = Calendar.getInstance();
        newCal.setTimeInMillis(date);
        while (calendar.compareTo(newCal) > 0){
            date += 86400000L;
            calendar.setTimeInMillis(date);
        }
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

    public boolean getState() {
        return state;
    }

    private long getDefaultDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }

    String toJsonString(){
        CustomAlarmData data = new CustomAlarmData();
        data.hour = this.hour;
        data.minute = this.minute;
        data.id = this.id;
        data.checkedWeekdays = this.checkedWeekdays;
        data.date = this.date;
        data.ringtoneUriString = this.ringtoneUri.toString();
        data.vibrationOn = this.vibrationOn;
        data.volume = this.volume;
        data.state = this.state;

        Gson gson = new Gson();
        return gson.toJson(data);
    }

    boolean isRepeating(){
        return !checkedWeekdays.isEmpty();
    }

    private class CustomAlarmData {
        private int hour, minute, id;
        private ArrayList<Integer> checkedWeekdays = new ArrayList<>();
        private long date;
        private String ringtoneUriString;
        private int volume;
        private boolean vibrationOn, state;

        private CustomAlarmData() {
        }
    }
}
