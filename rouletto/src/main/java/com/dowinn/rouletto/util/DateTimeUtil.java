package com.dowinn.rouletto.util;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static boolean checkValidTime(LocalDateTime start, int windowSeconds) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return false;
        }
        return !now.isAfter(start.plusSeconds(windowSeconds));
    }

    public static Integer remainingSeconds(LocalDateTime start, int delay) {
        LocalDateTime endTime = start.plusSeconds(delay);
        LocalDateTime now = LocalDateTime.now();
        long remaining = Duration.between(now, endTime).getSeconds();
        return (int) Math.max(remaining, 0);
    }

    public static Integer completedSeconds(LocalDateTime start, int delay) {
        LocalDateTime endTime = start.plusSeconds(delay);
        LocalDateTime now = LocalDateTime.now();
        long remaining=0;
        if(now.isBefore(endTime))
          remaining = Duration.between(now, endTime).getSeconds();

        return (int) Math.max(delay-remaining, 0);
    }

   /* public static Date parseDate(String date){
       LocalDate localDate=LocalDate.parse(date);
       return Date.valueOf(localDate);
    }*/

    public static LocalDateTime parseDate(String date){
        return LocalDate.parse(date).atStartOfDay(); // 00:00:00
    }

    public static LocalDateTime parseEndDate(String date){
        return LocalDate.parse(date).atTime(LocalTime.MAX); // 23:59:59.999999999
    }

     public static boolean isAfter(LocalDateTime localDateTime){
        return LocalDateTime.now().isAfter(localDateTime);
     }

     public static LocalDateTime addDelayInMinutes(Integer minutes){
        return LocalDateTime.now().plusMinutes(minutes);
     }


     public static String getDate(LocalDateTime localDateTime){
         DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         return localDateTime.format(dateTimeFormatter);
     }
}
