package com.gdou.cart.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class DateStrUtil {

    public static Date DateStrToDate(String dateStr){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);

        return Timestamp.valueOf(dateTime);
    }
}
