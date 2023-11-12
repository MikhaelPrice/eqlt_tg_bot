package com.real_estate.eqlt.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

    private static final String USER_REGISTER_TIME_PATTERN = "dd-MM-yyyy HH:mm:ss";

    public static String userRegisterDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Timestamp(System.currentTimeMillis()).getTime());
        SimpleDateFormat format = new SimpleDateFormat(USER_REGISTER_TIME_PATTERN);
        Date date = calendar.getTime();
        return format.format(date);
    }
}
