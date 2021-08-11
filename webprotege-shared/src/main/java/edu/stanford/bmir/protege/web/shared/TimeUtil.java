package edu.stanford.bmir.protege.web.shared;

import java.util.Date;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/04/2013
 */
public class TimeUtil {


    private static final int ONE_SECOND = 1000;

    public static final long ONE_MINUTE = 60 * ONE_SECOND;

    public static final long ONE_HOUR = 60 * ONE_MINUTE;

    public static final long ONE_DAY = 24 * ONE_HOUR;

    private static final long ONE_WEEK = 7 * ONE_DAY;

    private static final long ONE_MONTH = 31 * ONE_DAY;

    private static final long ONE_YEAR = 52 * ONE_WEEK;


    private static final String [] DAY_FORMAT = {"1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日", "11日", "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日", "21日", "22日", "23日", "24日", "25日", "26日", "27日", "28日", "29日", "30日", "39日"};

    private static final String [] MONTH_FORMAT = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};

    public static long getCurrentTime() {
        return new Date().getTime();
    }

    public static String getTimeRendering(final long timestamp) {
        return getTimeRendering(timestamp, getCurrentTime());
    }

    public static String getTimeRendering(final long timestamp, final long referenceTimestamp) {
        if(isLessThanOneMinuteAgo(timestamp, referenceTimestamp)) {
            return getLessThanOneMinuteAgoRendering();
        }

        if(isLessThanOneHourAgo(timestamp, referenceTimestamp)) {
            return getNMinutesAgoRendering(timestamp, referenceTimestamp);
        }

        if (isSameCalendarDay(timestamp, referenceTimestamp)) {
            return getNHoursAgoRendering(timestamp, referenceTimestamp);
        }

        if(isYesterday(timestamp, referenceTimestamp)) {
            return getYesterdayRendering(timestamp);
        }

        if(isLessThanOneMonthAgo(timestamp, referenceTimestamp)) {
            return getNDaysAgoRendering(timestamp, referenceTimestamp);
        }

        return getMinimalDayMonthYearRendering(timestamp);
    }

    /**
     * Determines if the specified timestamp is less than one minute before the reference timestamp.
     * @param timestamp The timestamp to test for.
     * @param referenceTimestamp The reference timestamp which the offset is calculated against.
     * @return {@code true} if the value of {@code timestamp} is less than one minute before the value of
     * {@code referenceTimestamp}, otherwise {@code false}.
     */
    public static boolean isLessThanOneMinuteAgo(long timestamp, long referenceTimestamp) {
        final long delta = referenceTimestamp - timestamp;
        return 0 <= delta && delta < ONE_MINUTE;
    }

    /**
     * Determines if the specified timestamp is less than hour before the reference timestamp.
     * @param timestamp The timestamp to test for.
     * @param referenceTimestamp The reference timestamp which the offset is calculated against.
     * @return {@code true} if the value of {@code timestamp} is less than one hour before the value of
     * {@code referenceTimestamp}, otherwise {@code false}.
     */
    public static boolean isLessThanOneHourAgo(long timestamp, long referenceTimestamp) {
        final long delta = referenceTimestamp - timestamp;
        return 0 <= delta && delta < ONE_HOUR;
    }

    /**
     * Determines if the specified timestamp is less than one week before the reference timestamp.
     * @param timestamp The timestamp to test for.
     * @param referenceTimestamp The reference timestamp which the offset is calculated against.
     * @return {@code true} if the value of {@code timestamp} is less than one week before the value of
     * {@code referenceTimestamp}, otherwise {@code false}.
     */
    public static boolean isLessThanOneWeekAgo(long timestamp, long referenceTimestamp) {
        final long delta = referenceTimestamp - timestamp;
        return 0 <= delta && delta < ONE_WEEK;
    }

    /**
     * Determines if the specified timestamp is less than one month before the reference timestamp.
     * @param timestamp The timestamp to test for.
     * @param referenceTimestamp The reference timestamp which the offset is calculated against.
     * @return {@code true} if the value of {@code timestamp} is less than one month before the value of
     * {@code referenceTimestamp}, otherwise {@code false}.
     */
    public static boolean isLessThanOneMonthAgo(long timestamp, long referenceTimestamp) {
        long daysBetween = getDaysBetween(timestamp, referenceTimestamp);
        return daysBetween <= 31;
    }

    /**
     * Determines if the specified timestamps denote time points within the same calendar day.
     * @param timestamp The timestamp
     * @param referenceTimestamp The timestamp to compare to.
     * @return {@code true} if the specified timestamps denote the same day, otherwise {@code false}.
     */
    @SuppressWarnings("deprecation")
    public static boolean isSameCalendarDay(long timestamp, long referenceTimestamp) {
        int daysBetween = getDaysBetween(timestamp, referenceTimestamp);
        return daysBetween == 0;
    }

    private static int getDaysBetween(long timestamp, long referenceTimestamp) {
        // Taken from CalendarUtil
        return getDaysBetween(new Date(timestamp), new Date(referenceTimestamp));
    }


    /**
     * Taken from {@link com.google.gwt.user.datepicker.client.CalendarUtil}
     */
    private static int getDaysBetween(Date start, Date finish) {
        // Convert the dates to the same time
        start = copyDate(start);
        resetTime(start);
        finish = copyDate(finish);
        resetTime(finish);

        long aTime = start.getTime();
        long bTime = finish.getTime();

        long adjust = 60 * 60 * 1000;
        adjust = (bTime > aTime) ? adjust : -adjust;

        return (int) ((bTime - aTime + adjust) / (24 * 60 * 60 * 1000));
    }


    /**
     * Taken from {@link com.google.gwt.user.datepicker.client.CalendarUtil}
     */
    private static Date copyDate(Date date) {
        if (date == null) {
            return null;
        }
        Date newDate = new Date();
        newDate.setTime(date.getTime());
        return newDate;
    }

    /**
     * Taken from {@link com.google.gwt.user.datepicker.client.CalendarUtil}
     */
    private static void resetTime(Date date) {
        long msec = resetMilliseconds(date.getTime());
        date.setTime(msec);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
    }

    private static long resetMilliseconds(long msec) {
        int offset = (int) (msec % 1000);
        // Normalize if time is before epoch
        if (offset < 0) {
            offset += 1000;
        }
        return msec - offset;
    }

    /**
     * Determines if the specified timestamps denote time points within the same calendar month.
     * @param timestamp The timestamp
     * @param referenceTimestamp The timestamp to compare to.
     * @return {@code true} if the specified timestamps denote the same month, otherwise {@code false}.
     */
    @SuppressWarnings("deprecation")
    public static boolean isSameCalendarMonth(long timestamp, long referenceTimestamp) {
        Date timestampDate = new Date(timestamp);
        Date referenceTimestampDate = new Date(referenceTimestamp);
        return timestampDate.getMonth() == referenceTimestampDate.getMonth() && isSameCalendarYear(timestamp, referenceTimestamp);
    }

    /**
     * Determines if the specified timestamps denote time points within the same calendar year.
     * @param timestamp The timestamp
     * @param referenceTimestamp The timestamp to compare to.
     * @return {@code true} if the specified timestamps denote the same calendar year, otherwise {@code false}.
     */
    @SuppressWarnings("deprecation")
    public static boolean isSameCalendarYear(long timestamp, long referenceTimestamp) {
        Date timestampDate = new Date(timestamp);
        Date referenceTimestampDate = new Date(referenceTimestamp);
        return timestampDate.getYear() == referenceTimestampDate.getYear();
    }


    /**
     * Determines if the specified timestamps denote time points one calendar day apart (this could be less than 24
     * hours apart).
     * @param timestamp The timestamp
     * @param referenceTimestamp The timestamp to compare to.
     * @return {@code true} if the specified timestamps denote one calendar day apart, otherwise {@code false}.
     */
    @SuppressWarnings("deprecation")
    public static boolean isYesterday(long timestamp, long referenceTimestamp) {
        return  getDaysBetween(timestamp, referenceTimestamp) == 1;
    }



    private static String getLessThanOneMinuteAgoRendering() {
        return "不到一分钟前";
    }


    private static String getNMinutesAgoRendering(long timestamp, long referenceTimestamp) {
        int mins = (int) ((referenceTimestamp - timestamp) / ONE_MINUTE);
        if(mins == 1) {
            return "一分钟前";
        }
        else {
            return mins + "分钟前";
        }
    }

    private static String getYesterdayRendering(long timestamp) {
        Date date = new Date(timestamp);
        int hours = date.getHours();
        String hoursFormat = Integer.toString(hours);
        int minutes = date.getMinutes();
        String minutesFormat;
        if(minutes < 10) {
            minutesFormat = "0" + minutes;
        }
        else {
            minutesFormat = Integer.toString(minutes);
        }
        return "昨天" + hoursFormat + ":" + minutesFormat;
    }

    private static String getNHoursAgoRendering(long timestamp, long referenceTimestamp) {
        int hours = (int) ((referenceTimestamp - timestamp) / ONE_HOUR);
        if(hours == 1) {
            return "一小时前";
        }
        else {
            return hours + "小时前";
        }
    }

    private static String getNDaysAgoRendering(long timestamp, long referenceTimestamp) {
        int days = getDaysBetween(timestamp, referenceTimestamp);
        if(days == 1) {
            return "一天前";
        }
        else {
            return days + "天前";
        }
    }


    private static String getMinimalDayMonthYearRendering(long timestamp) {
        final Date timestampDate = new Date(timestamp);
        return formatDay(timestampDate.getDate()) + " " + formatMonth(timestampDate.getMonth()) + " " + formatYear(timestampDate);
    }

    private static String formatYear(Date timestampDate) {
        return Integer.toString(timestampDate.getYear() + 1900);
    }


    public static String formatDay(int day) {
        return DAY_FORMAT[day - 1];
    }

    public static String formatMonth(int month) {
        return MONTH_FORMAT[month];
    }

}
