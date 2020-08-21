package weaver.micro.devkit.time;

import static weaver.micro.devkit.core.CacheBase.*;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 日期与时间接口，基于{@code java.time}
 * 以系统当前时区为准，获取时间戳请使用{@link weaver.micro.devkit.core.SystemAPI#currentTimestamp()}
 *
 * @author ruan4261
 */
public interface DateTimeAPI {

    int FAIL_ZONE_ID = 0x7f;// 127:一个不存在的时区偏移量，他可以是-18~18以外的任何数，至于为什么是127（:D）

    static LocalDateTime CURRENT_DATE_TIME() {
        return LocalDateTime.now();
    }

    static LocalDate CURRENT_DATE() {
        return LocalDate.now();
    }

    static LocalTime CURRENT_TIME() {
        return LocalTime.now();
    }

    /**
     * 使用系统默认时区
     * Formatter: yyyy-MM-dd
     */
    static String date() {
        return CURRENT_DATE().format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * 使用系统默认时区
     * Formatter: HH:mm:ss
     */
    static String time() {
        return CURRENT_TIME().format(DEFAULT_TIME_FORMATTER);
    }

    /**
     * 使用系统默认时区
     * Formatter: yyyy-MM-dd HH:mm:ss
     */
    static String dateTime() {
        return CURRENT_DATE_TIME().format(DEFAULT_DATETIME_FORMATTER);
    }

    /* 日期偏移量、秒偏移量 */

    static String dateWithOffset(long offsetDay) {
        return CURRENT_DATE().plusDays(offsetDay).format(DEFAULT_DATE_FORMATTER);
    }

    static String timeWithOffset(long offsetSecond) {
        return CURRENT_TIME().plusSeconds(offsetSecond).format(DEFAULT_TIME_FORMATTER);
    }

    static String dateTimeWithOffset(long offsetDay, long offsetSecond) {
        return CURRENT_DATE_TIME().plusDays(offsetDay).plusSeconds(offsetSecond).format(DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 将时间戳格式化
     * 使用自定义的时区偏移量
     *
     * @param timestamp  时间戳
     * @param zoneOffset 时区与格林威治标准的小时偏移量，比如中国时区的偏移量是+8
     * @param formatter  格式化标准
     */
    static String format(long timestamp, int zoneOffset, DateTimeFormatter formatter) {
        ZoneId zone;
        if (zoneOffset < -18 || zoneOffset > 18)
            zone = ZoneId.systemDefault();
        else
            zone = ZoneOffset.ofHours(zoneOffset);
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone).format(formatter);
    }

    /** overload */
    static String format(long timestamp, DateTimeFormatter formatter) {
        return format(timestamp, FAIL_ZONE_ID, formatter);
    }

    /* Custom pattern */

    static String format(long timestamp, int zoneOffset, String pattern) {
        return format(timestamp, zoneOffset, DateTimeFormatter.ofPattern(pattern));
    }

    static String format(long timestamp, String pattern) {
        return format(timestamp, FAIL_ZONE_ID, DateTimeFormatter.ofPattern(pattern));
    }

    static String format(String pattern) {
        return CURRENT_DATE_TIME().format(DateTimeFormatter.ofPattern(pattern));
    }

    /* DateTime */

    static String dateTime(long timestamp, int zoneOffset) {
        return format(timestamp, zoneOffset, DEFAULT_DATETIME_FORMATTER);
    }

    static String dateTime(long timestamp) {
        return format(timestamp, FAIL_ZONE_ID, DEFAULT_DATETIME_FORMATTER);
    }

    /* DateTimeWithMillis */

    static String dateTimeWithMillis(long timestamp, int zoneOffset) {
        return format(timestamp, zoneOffset, MILLIS_DATETIME_FORMATTER);
    }

    static String dateTimeWithMillis(long timestamp) {
        return format(timestamp, FAIL_ZONE_ID, MILLIS_DATETIME_FORMATTER);
    }

    /* Date */

    static String date(long timestamp, int zoneOffset) {
        return format(timestamp, zoneOffset, DEFAULT_DATE_FORMATTER);
    }

    static String date(long timestamp) {
        return format(timestamp, FAIL_ZONE_ID, DEFAULT_DATE_FORMATTER);
    }

    /* Time */

    static String time(long timestamp, int zoneOffset) {
        return format(timestamp, zoneOffset, DEFAULT_TIME_FORMATTER);
    }

    static String time(long timestamp) {
        return format(timestamp, FAIL_ZONE_ID, DEFAULT_TIME_FORMATTER);
    }

    /* TimeWithMillis */

    static String timeWithMillis(long timestamp, int zoneOffset) {
        return format(timestamp, zoneOffset, MILLIS_TIME_FORMATTER);
    }

    static String timeWithMillis(long timestamp) {
        return format(timestamp, FAIL_ZONE_ID, MILLIS_TIME_FORMATTER);
    }

}
