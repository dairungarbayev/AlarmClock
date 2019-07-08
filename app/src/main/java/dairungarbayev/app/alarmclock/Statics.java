package dairungarbayev.app.alarmclock;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

class Statics {

    static String getOverviewTextRepeating(Context context, ArrayList<Integer> checkedWeekdays){
        StringBuffer buffer = new StringBuffer();
        String[] weekdaysString = new String[7];
        weekdaysString[0] = context.getResources().getString(R.string.monday);
        weekdaysString[1] = context.getResources().getString(R.string.tuesday);
        weekdaysString[2] = context.getResources().getString(R.string.wednesday);
        weekdaysString[3] = context.getResources().getString(R.string.thursday);
        weekdaysString[4] = context.getResources().getString(R.string.friday);
        weekdaysString[5] = context.getResources().getString(R.string.saturday);
        weekdaysString[6] = context.getResources().getString(R.string.sunday);

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
        return buffer.toString();
    }

    static String getOverviewTextOneShot(long date){
        Date dateObj = new Date(date);
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd");
        return format.format(dateObj);
    }
}
