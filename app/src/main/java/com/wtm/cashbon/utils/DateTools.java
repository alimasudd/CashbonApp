package com.wtm.cashbon.utils;

import android.content.Context;
import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by @alimasudd on 9/12/2019.
 */
public class DateTools {

    public static String format(String waktu, String formatAwal, String formatAkhir) {

        SimpleDateFormat fromdate = new SimpleDateFormat(formatAwal);
        SimpleDateFormat todate = new SimpleDateFormat(formatAkhir);

        String reformatdate = "";

        try {
            reformatdate = todate.format(fromdate.parse(waktu));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return reformatdate;

    }

    public static HashMap<String, String> getDateDetail(String dateString) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            calendar.setTime(sdf.parse(dateString));// all done
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int mont = calendar.get(Calendar.MONTH);
        int hour = calendar.get(Calendar.HOUR);

        String days = null;
        String monts = null;

        switch (day) {
            case Calendar.SUNDAY:
                days = "Minggu";
                break;
            case Calendar.MONDAY:
                days = "Senin";
                break;
            case Calendar.TUESDAY:
                days = "Selasa";
                break;
            case Calendar.WEDNESDAY:
                days = "Rabu";
                break;
            case Calendar.THURSDAY:
                days = "Kamis";
                break;
            case Calendar.FRIDAY:
                days = "Jumat";
                break;
            case Calendar.SATURDAY:
                days = "Sabtu";
                break;
        }


        switch (mont) {
            case Calendar.JANUARY:
                monts = "Januari";
                break;
            case Calendar.FEBRUARY:
                monts = "Februari";
                break;
            case Calendar.MARCH:
                monts = "Maret";
                break;
            case Calendar.APRIL:
                monts = "April";
                break;
            case Calendar.MAY:
                monts = "Mei";
                break;
            case Calendar.JUNE:
                monts = "Juni";
                break;
            case Calendar.JULY:
                monts = "Juli";
                break;
            case Calendar.AUGUST:
                monts = "Agustus";
                break;
            case Calendar.SEPTEMBER:
                monts = "September";
                break;
            case Calendar.OCTOBER:
                monts = "Oktober";
                break;
            case Calendar.NOVEMBER:
                monts = "November";
                break;
            case Calendar.DECEMBER:
                monts = "Desember";
                break;
        }

        HashMap<String, String> dates = new HashMap<>();
        dates.put("hari", days);
        dates.put("tanggal", String.valueOf(date));
        dates.put("bulan", monts);
        dates.put("tahun", String.valueOf(year));
        return dates;
    }


    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public static String changeFormat(String waktu, String formatAwal, String formatAkhir) {

        SimpleDateFormat fromdate = new SimpleDateFormat(formatAwal);
        SimpleDateFormat todate = new SimpleDateFormat(formatAkhir);

        String reformatdate = "";

        try {
            reformatdate = todate.format(fromdate.parse(waktu));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return reformatdate;

    }

    public static void dateDialog(Context context, final EditText editText) {
        DateTime now = DateTime.now();
        MonthAdapter.CalendarDay DEFAULT_START_DATE = new MonthAdapter.CalendarDay(now.getYear(), now.getMonthOfYear()-1, now.getDayOfMonth());
        MonthAdapter.CalendarDay DEFAULT_END_DATE = new MonthAdapter.CalendarDay(now.getYear(), now.getMonthOfYear()-1, now.getDayOfMonth()+100);

        CalendarDatePickerDialogFragment cdp1 = new CalendarDatePickerDialogFragment()
//                .setPreselectedDate(2020, 0, 1)
                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {

                        int month = monthOfYear + 1;
                        String dateString = year + "-" + month + "-" + dayOfMonth;
                        String tgl = DateTools.format(dateString, "yyyy-M-dd", "yyyy-MM-dd");
                        editText.setText(tgl);
                    }
                });
        cdp1.setDateRange(DEFAULT_START_DATE, DEFAULT_END_DATE);
        cdp1.show(((FragmentActivity) context).getSupportFragmentManager(), null);

    }

    public static void dateDialogCustom(Context context, final EditText editText) {
        DateTime now = DateTime.now();
        MonthAdapter.CalendarDay DEFAULT_START_DATE = new MonthAdapter.CalendarDay(now.getYear(), now.getMonthOfYear()-1, now.getDayOfMonth());
        MonthAdapter.CalendarDay DEFAULT_END_DATE = new MonthAdapter.CalendarDay(now.getYear(), now.getMonthOfYear()-1, now.getDayOfMonth()+7);

        CalendarDatePickerDialogFragment cdp1 = new CalendarDatePickerDialogFragment()
//                .setPreselectedDate(2020, 0, 1)
                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {

                        int month = monthOfYear + 1;
                        String dateString = year + "-" + month + "-" + dayOfMonth;
                        String tgl = DateTools.format(dateString, "yyyy-M-dd", "yyyy-MM-dd");
                        editText.setText(tgl);
                    }
                });
        cdp1.setDateRange(DEFAULT_START_DATE, DEFAULT_END_DATE);
        cdp1.show(((FragmentActivity) context).getSupportFragmentManager(), null);

    }

    public static void dateDialogCustomMonth(Context context, final EditText editText) {
        DateTime now = DateTime.now();
        MonthAdapter.CalendarDay DEFAULT_START_DATE = new MonthAdapter.CalendarDay(now.getYear(), now.getMonthOfYear()-1, now.getDayOfMonth());
        MonthAdapter.CalendarDay DEFAULT_END_DATE = new MonthAdapter.CalendarDay(now.getYear(), now.getMonthOfYear()-1, now.getDayOfMonth()+30);

        CalendarDatePickerDialogFragment cdp1 = new CalendarDatePickerDialogFragment()
//                .setPreselectedDate(2020, 0, 1)
                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {

                        int month = monthOfYear + 1;
                        String dateString = year + "-" + month + "-" + dayOfMonth;
                        String tgl = DateTools.format(dateString, "yyyy-M-dd", "yyyy-MM-dd");
                        editText.setText(tgl);
                    }
                });
        cdp1.setDateRange(DEFAULT_START_DATE, DEFAULT_END_DATE);
        cdp1.show(((FragmentActivity) context).getSupportFragmentManager(), null);

    }

    public static int getAge(int DOByear, int DOBmonth, int DOBday) {

        int age;

        final Calendar calenderToday = Calendar.getInstance();
        int currentYear = calenderToday.get(Calendar.YEAR);
        int currentMonth = 1 + calenderToday.get(Calendar.MONTH);
        int todayDay = calenderToday.get(Calendar.DAY_OF_MONTH);

        age = currentYear - DOByear;

        if(DOBmonth > currentMonth) {
            --age;
        } else if(DOBmonth == currentMonth) {
            if(DOBday > todayDay){
                --age;
            }
        }
        return age;
    }
}
