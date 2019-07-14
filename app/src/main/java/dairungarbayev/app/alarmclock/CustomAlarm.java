package dairungarbayev.app.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class CustomAlarm {

    private static final String TAG = "CustomAlarm";

    private final String ALARM_ID = "alarm_id";

    private static int counter = 1;
    private ArrayList<Integer> checkedWeekdays = new ArrayList<>();
    private long date;
    private Uri ringtoneUri;
    private int volume;
    private boolean vibrationOn, isNew;
    private int hour, minute;
    private int id;
    private Context appContext;
    private boolean state = false;

    CustomAlarm(Context context){
        this.ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context,RingtoneManager.TYPE_ALARM);
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
        getAlarmManager().setExact(AlarmManager.RTC_WAKEUP,getNextAlarmTime(),getPendingIntent());
        state = true;
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
        intent.putExtra(ALARM_ID,id);
        return PendingIntent.getActivity(appContext,id,intent,PendingIntent.FLAG_CANCEL_CURRENT);
    }

    long getNextAlarmTime(){
        Calendar next = Calendar.getInstance();
        ArrayList<Long> timeList = new ArrayList<>();

        if (isRepeating()){
            for (int i = 0; i<checkedWeekdays.size(); i++){
                Calendar cal = Calendar.getInstance();
                long timeInMillis = cal.getTimeInMillis();
                for (int j = 0; j<9; j++){
                    cal.setTimeInMillis(timeInMillis);
                    if (cal.get(Calendar.DAY_OF_WEEK) == checkedWeekdays.get(i)){
                        timeList.add(timeInMillis);
                    }
                    timeInMillis += 86400000L;
                }
            }

            Log.d(TAG, "getNextAlarmTime: timeList: " + timeList);

            long min = Collections.min(timeList);
            next.setTimeInMillis(min);
        } else next.setTimeInMillis(getDate());

        next.set(Calendar.HOUR_OF_DAY,hour);
        next.set(Calendar.MINUTE,minute);
        next.set(Calendar.SECOND,0);
        next.set(Calendar.MILLISECOND,0);

        if (next.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
            if (isRepeating()){
                Log.d(TAG, "getNextAlarmTime: timeList: " + timeList);
                long min = Collections.min(timeList);
                timeList.remove(min);
                Log.d(TAG, "getNextAlarmTime: timeList: " + timeList);
                next.setTimeInMillis(Collections.min(timeList));
            } else {
                date += 86400000L;
                next.setTimeInMillis(date);
            }
            next.set(Calendar.HOUR_OF_DAY,hour);
            next.set(Calendar.MINUTE,minute);
            next.set(Calendar.SECOND,0);
            next.set(Calendar.MILLISECOND,0);
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

    void setNotNew() {
        isNew = false;
    }

    public static void setCounter(int counter) {
        CustomAlarm.counter = counter;
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

    boolean getState() {
        return state;
    }

    public static int getCounter() {
        return counter;
    }

    void showTimeIntervalToast(){
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
            daysText = days + " " + appContext.getResources().getString(R.string.day) + " ";
        } else daysText = "";
        intervalText += daysText + hoursText + minutesText;

        Toast toast = Toast.makeText(appContext,intervalText,Toast.LENGTH_SHORT);
        toast.show();
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
