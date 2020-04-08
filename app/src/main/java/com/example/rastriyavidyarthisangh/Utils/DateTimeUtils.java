package com.example.rastriyavidyarthisangh.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String getCurrentDateAndTime(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm dd-MMM-yyyy", new Locale("en"));
        return format.format(date);
    }

    public static long getCurrentTimeInMilliSeconds(){
        return System.currentTimeMillis();
    }

    public static String getDateAndTimeToBeDisplayedInEvent(long timeStamp){
        Date date = new Date(timeStamp);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm aa dd-MMM-yyyy", new Locale("en"));
        return format.format(date);
    }

}
