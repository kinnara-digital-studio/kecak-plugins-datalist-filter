package com.kinnarastudio.kecakplugins.datalist.filter.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author aristo
 *
 * Processing calendar, bounching over date
 */
public class CalendarProcessor {
    private final Calendar calendar;

    /**
     * Get instance for current date
     *
     * @return
     */
    public static CalendarProcessor getInstance() {
        return getInstance(new Date());
    }

    /**
     * Get instance for specific date
     *
     * @param date
     * @return
     */
    public static CalendarProcessor getInstance(Date date) {
        return new CalendarProcessor(date);
    }

    /**
     * Main constructor
     *
     * @param date
     */
    private CalendarProcessor(Date date) {
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
    }

    public CalendarProcessor prevDate() {
        return date(-1);
    }

    public CalendarProcessor nextDate() {
        return date(1);
    }

    public CalendarProcessor prevWeek() {
        return week(-1);
    }
    
    public CalendarProcessor nextWeek() {
        return week(1);
    }
    
    public CalendarProcessor prevMonth() {
        return month(-1);
    }

    public CalendarProcessor nextMonth() {
        return month(1);
    }

    public CalendarProcessor prevYear() {
        return year(-1);
    }
    
    public CalendarProcessor nextYear() {
        return year(1);
    }

    public CalendarProcessor date(int amount) {
        calendar.add(Calendar.DATE, amount);
        return this;
    }
    
    public CalendarProcessor week(int amount) {
        calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, amount);
        return this;
    }
    
    public CalendarProcessor month(int amount) {
        calendar.add(Calendar.MONTH, amount);
        return this;
    }
    
    public CalendarProcessor year(int amount) {
        calendar.add(Calendar.YEAR, amount);
        return this;
    }

    /**
     * get date object
     *
     * @return
     */
    public Date getDate() {
        return getCalendar().getTime();
    }

    /**
     * Get calendar object
     *
     * @return
     */
    public Calendar getCalendar() {
        return (Calendar) calendar.clone();
    }

    /**
     * Get formatted date
     *
     * @param dateFormat
     * @return
     */
    public String format(DateFormat dateFormat) {
        return dateFormat.format(getDate());
    }
}
