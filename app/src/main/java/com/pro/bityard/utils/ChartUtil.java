package com.pro.bityard.utils;


import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pro.bityard.R;
import com.pro.bityard.base.AppContext;
import com.pro.bityard.chart.KData;
import com.pro.bityard.entity.KlineEntity;
import com.pro.bityard.entity.QuoteChartEntity;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pro.bityard.utils.Util.parseServerTime;

public class ChartUtil {
    private static String TAG = "ChartUtil";
    private static double volume;


    /*获取当前行情的ContCode*/
    public static List<KData> klineList(QuoteChartEntity data) {
        if (data == null) {
            return null;
        } else {

            List<KlineEntity> klineEntityList;
            List<Long> t = data.getT();
            List<Double> c = data.getC();
            List<Double> op = data.getO();
            List<Double> h = data.getH();
            List<Double> l = data.getL();
            List<Double> v = data.getV();

            klineEntityList = new ArrayList<>();
            for (int i = 0; i < t.size(); i++) {
                if (op.get(i) != null && c.get(i) != null && h.get(i) != null && l.get(i) != null && v.get(i) != null) {
                    klineEntityList.add(new KlineEntity(t.get(i), op.get(i), c.get(i), h.get(i), l.get(i), v.get(i)));
                }
            }

            List<KData> dataList = new ArrayList<>();
            long start;
            double openPrice;
            double closePrice;
            double maxPrice;
            double minPrice;
            double volume;

            for (int i = 0; i < klineEntityList.size(); i++) {
                start = klineEntityList.get(i).getTime() * 1000;
                openPrice = klineEntityList.get(i).getOpenPrice();
                closePrice = klineEntityList.get(i).getClosePrice();
                maxPrice = klineEntityList.get(i).getHighPrice();
                minPrice = klineEntityList.get(i).getLowPrice();
                volume = klineEntityList.get(i).getVolume();
                dataList.add(new KData(start, openPrice, closePrice, maxPrice, minPrice, volume));

            }

            return agoToNow(dataList);
        }
    }


    /*5分 获取当时时间戳的分钟 除5求余 比如7/5 余2 那就是把当前最新的数据设置成2分钟前的时间戳*/
    public static long setMinTime(List<KData> refreshList, int times) {
        long time;
        long nowTime = refreshList.get(refreshList.size() - 1).getTime();
        Date date = parseServerTime(Util.stampToDate(nowTime));
        Calendar todayCal = Calendar.getInstance();
        todayCal.setFirstDayOfWeek(Calendar.MONDAY);
        todayCal.setTime(date);
        int a = todayCal.get(Calendar.MINUTE) % times;
        if (a == 0) {
            time = nowTime;

        } else {
            todayCal.add(Calendar.MINUTE, -a);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time = Util.dateToStampLong(sdf.format(todayCal.getTimeInMillis()));
        }
        return time;
    }

    /*5分 获取当时时间戳的分钟 除5求余 比如7/5 余2 那就是把当前最新的数据设置成2分钟前的时间戳*/
    public static long setWeekTime(List<KData> refreshList) {
        long time;
        long nowTime = refreshList.get(refreshList.size() - 1).getTime();
        Date date = parseServerTime(Util.stampToDate(nowTime));
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(date);
        todayCal.setFirstDayOfWeek(Calendar.MONDAY);

        Integer i = getWeekCount(nowTime);
        int a = i % 7;
        if (a == 0) {
            time = nowTime;
        } else {
            todayCal.add(Calendar.DAY_OF_WEEK, -(a - 1));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time = Util.dateToStampLong(sdf.format(todayCal.getTimeInMillis()));
        }
        return time;
    }

    public static long setMonthTime(List<KData> refreshList) {
        long time;
        long nowTime = refreshList.get(refreshList.size() - 1).getTime();

        Date date = parseServerTime(Util.stampToDate(nowTime));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        String format2 = sdf2.format(calendar.getTimeInMillis());
        String[] split = format2.split("-");

        int dayOfMonth = getDayOfMonth(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

        int a = Integer.parseInt(split[2]) % dayOfMonth;

        if (a == 0) {
            time = nowTime;
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -(a - 1));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time = Util.dateToStampLong(sdf.format(calendar.getTimeInMillis()));
        }
        return time;
    }


    public static long getTimeNow() {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.add(Calendar.DAY_OF_WEEK, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Long today = c.getTimeInMillis();
        return today;
    }

    public static String getWeekDay(Long time) {
        //获取当前的毫秒值
        Date date = new Date(time);
        String Week = "";
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        int wek = c.get(Calendar.DAY_OF_WEEK);
        if (wek == 1) {
            Week += "日";
        }
        if (wek == 2) {
            Week += "一";
        }
        if (wek == 3) {
            Week += "二";
        }
        if (wek == 4) {
            Week += "三";
        }
        if (wek == 5) {
            Week += "四";
        }
        if (wek == 6) {
            Week += "五";
        }
        if (wek == 7) {
            Week += "六";
        }
        return Week;
    }

    /*当天0点时间*/
    public static String getTodayZero() {
        Date date = new Date();
        long l = 24 * 60 * 60 * 1000; //每天的毫秒数
        long zeroT = (date.getTime() - (date.getTime() % l) - 8 * 60 * 60 * 1000);  //今天零点零分零秒的毫秒数
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(zeroT);
    }

    /*当天0点时间*/
    public static String getSelectZero(String selectStart) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(selectStart);
            long l = 24 * 60 * 60 * 1000; //每天的毫秒数
            long zeroT = (date.getTime() - (date.getTime() % l) - 8 * 60 * 60 * 1000);  //今天零点零分零秒的毫秒数
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(zeroT);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*当天0点时间*/
    public static String getDaysBefore(int days) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        //过去七天
        c.setTime(new Date());
        c.add(Calendar.DATE, -days);
        Date d = c.getTime();
        return  format.format(d);
    }

    /*当天最后一秒*/
    public static String getSelectLastTime(String selectStart) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(selectStart);
            long l = 24 * 60 * 60 * 1000; //每天的毫秒数
            long zeroT = (date.getTime() - (date.getTime() % l) - 8 * 60 * 60 * 1000);  //今天零点零分零秒的毫秒数
            long endT = zeroT + 24 * 60 * 60 * 1000 - 1;  //今天23点59分59秒的毫秒数
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endT);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /*当天最后一秒*/
    public static String getTodayLastTime() {
        Date date = new Date();
        long l = 24 * 60 * 60 * 1000; //每天的毫秒数
        long zeroT = (date.getTime() - (date.getTime() % l) - 8 * 60 * 60 * 1000);  //今天零点零分零秒的毫秒数
        long endT = zeroT + 24 * 60 * 60 * 1000 - 1;  //今天23点59分59秒的毫秒数
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endT);
    }

    /*本周的零点*/
    public static String getWeekZero() {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.clear(Calendar.MINUTE);
        ca.clear(Calendar.SECOND);
        ca.clear(Calendar.MILLISECOND);
        ca.set(Calendar.DAY_OF_WEEK, ca.getFirstDayOfWeek());
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ca.getTimeInMillis());

    }

    /*本月的零点*/
    public static String getMonthZero() {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.clear(Calendar.MINUTE);
        ca.clear(Calendar.SECOND);
        ca.clear(Calendar.MILLISECOND);
        ca.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ca.getTimeInMillis());

    }

    /*本月的零点*/
    public static String getThreeMonthZero() {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.clear(Calendar.MINUTE);
        ca.clear(Calendar.SECOND);
        ca.clear(Calendar.MILLISECOND);
        ca.set(Calendar.DAY_OF_MONTH, -3);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ca.getTimeInMillis());

    }

    public static String getDate(Long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        return format.format(d1);

    }


    public static Integer getMonthDay(String time) {
        //获取当前的毫秒值
        Date date = new Date(Long.parseLong(time));
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayInMonth = c.get(Calendar.DAY_OF_MONTH);
        Log.d(TAG, "getMonthDay:239:  " + dayInMonth);
        return dayInMonth;
    }


    public static Integer getWeekCount(Long time) {
        //获取当前的毫秒值
        Date date = new Date(time);
        int Week = 0;
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);

        int wek = c.get(Calendar.DAY_OF_WEEK);

        if (wek == 1) {
            Week = 7;
        }
        if (wek == 2) {
            Week = 1;
        }
        if (wek == 3) {
            Week = 2;
        }
        if (wek == 4) {
            Week = 3;
        }
        if (wek == 5) {
            Week = 4;
        }
        if (wek == 6) {
            Week = 5;
        }
        if (wek == 7) {
            Week = 6;
        }
        return Week;

    }

    /*获取时间范围的列表*/
    public static List<String> getWeekRange(List<KData> kDataList) {
        List<String> longList;
        if (kDataList == null) {
            return null;
        } else {
            longList = new ArrayList<>();
            for (KData data : kDataList) {
                if (getWeekDay(data.getTime()).equals("一")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(data.getTime());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    calendar.add(Calendar.DAY_OF_WEEK, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    String format = sdf.format(calendar.getTimeInMillis());
                    Long aLong = Util.dateToStampLong(format);
                    Long aLong2 = aLong + 7 * 24 * 60 * 60 * 1000;
                    longList.add(aLong + "-" + aLong2);
                }
            }
            return longList;
        }


    }

    /*获取月时间范围的列表*/
    public static List<String> getMonthRange(List<KData> kDataList) {
        List<String> longList;
        if (kDataList == null) {
            return null;
        } else {
            longList = new ArrayList<>();
            for (KData data : kDataList) {
                if (getMonthDay(String.valueOf(data.getTime())) == 1) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(data.getTime());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_WEEK, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    String format = sdf.format(calendar.getTimeInMillis());
                    Long aLong = Util.dateToStampLong(format);

                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
                    String format2 = sdf2.format(calendar.getTimeInMillis());
                    String[] split = format2.split("-");

                    calendar.add(Calendar.DAY_OF_MONTH, getDayOfMonth(Integer.parseInt(split[0]), Integer.parseInt(split[1])));

                    String format1 = sdf.format(calendar.getTimeInMillis());
                    Long aLong1 = Util.dateToStampLong(format1);

                    longList.add(aLong + "-" + aLong1);
                }
            }
            return longList;
        }


    }


    private static int getDayOfMonth(int year, int month) {
        int day;
        if (isLeapYear(year)) {
            day = 29;
        } else {
            day = 28;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                return day;

        }

        return 0;
    }

    private static boolean isLeapYear(int year) {

        if (year > 0) {
            if ((year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public static ArrayMap<Object, KData> getWeekKDataList(List<KData> kDataList) {
        List<KData> kDataList1;
        ArrayMap<Object, KData> arrayMap;
        if (kDataList == null) {
            return null;
        } else {
            List<String> timeRange = getWeekRange(kDataList);
            arrayMap = new ArrayMap<>();
            for (String dataStr : timeRange) {
                String[] split = dataStr.split("-");
                kDataList1 = new ArrayList<>();
                for (KData data : kDataList) {
                    if (data.getTime() >= Long.parseLong(split[0]) && data.getTime() < Long.parseLong(split[1])) {
                        kDataList1.add(data);
                        KData weekOneData = getOneData(kDataList1);
                        arrayMap.put(Long.parseLong(split[0]), weekOneData);
                    }
                }
            }
            return arrayMap;
        }
    }


    public static ArrayMap<Object, KData> getMonthKDataList(List<KData> kDataList) {
        List<KData> kDataList1;
        ArrayMap<Object, KData> arrayMap;
        if (kDataList == null) {
            return null;
        } else {
            List<String> timeRange = getMonthRange(kDataList);
            arrayMap = new ArrayMap<>();
            for (String dataStr : timeRange) {
                String[] split = dataStr.split("-");
                kDataList1 = new ArrayList<>();
                for (KData data : kDataList) {
                    if (data.getTime() >= Long.parseLong(split[0]) && data.getTime() < Long.parseLong(split[1])) {
                        kDataList1.add(data);
                        KData weekOneData = getOneData(kDataList1);
                        arrayMap.put(Long.parseLong(split[0]), weekOneData);
                    }
                }
            }
            return arrayMap;
        }
    }

    /*获得列表*/
    public static List<KData> KlineWeekData(ArrayMap<Object, KData> arrayMap) {
        List<KData> list;
        Set<Map.Entry<Object, KData>> entries = arrayMap.entrySet();
        Iterator<Map.Entry<Object, KData>> iterator = entries.iterator();
        list = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Object, KData> next = iterator.next();
            KData value = next.getValue();
            list.add(value);
        }
        List<KData> list1 = ChartUtil.agoToNow(list);
        return list1;

    }

    /*获得列表*/
    public static List<KData> KlineMonthData(ArrayMap<Object, KData> arrayMap) {
        List<KData> list;
        Set<Map.Entry<Object, KData>> entries = arrayMap.entrySet();
        Iterator<Map.Entry<Object, KData>> iterator = entries.iterator();
        list = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Object, KData> next = iterator.next();
            KData value = next.getValue();
            list.add(value);
        }
        List<KData> list1 = ChartUtil.agoToNow(list);
        return list1;

    }

    /*排序时间从故去到现在*/
    public static List<KData> agoToNow(List<KData> kDataList) {
        List<KData> kDataList1 = new ArrayList<>();
        Collections.sort(kDataList, (o1, o2) -> {
            long time = o1.getTime();
            long time1 = o2.getTime();
            double sub = time - time1;
            if (sub == 0) {
                return (int) (time1 - time);
            }
            return (int) sub;
        });
        for (KData kData : kDataList) {
            kDataList1.add(kData);
        }

        return kDataList1;
    }


    public static KData getOneData(List<KData> kData1WeekHistory) {

        if (kData1WeekHistory == null) {
            return null;
        } else {

            double maxPrice = kData1WeekHistory.get(kData1WeekHistory.size() - 1).getMaxPrice();
            double minPrice = kData1WeekHistory.get(kData1WeekHistory.size() - 1).getMinPrice();

            KData kData;

            for (int i = 0; i < kData1WeekHistory.size(); i++) {
                if (kData1WeekHistory.get(i).getMaxPrice() > maxPrice) {
                    maxPrice = kData1WeekHistory.get(i).getMaxPrice();
                }
                if (kData1WeekHistory.get(i).getMinPrice() < minPrice) {
                    minPrice = kData1WeekHistory.get(i).getMinPrice();
                }
                volume = +kData1WeekHistory.get(i).getVolume();
            }

            kData = new KData(kData1WeekHistory.get(0).getTime(), kData1WeekHistory.get(0).getOpenPrice(),
                    kData1WeekHistory.get(kData1WeekHistory.size() - 1).getClosePrice(), maxPrice, minPrice, volume);
            return kData;
        }
    }

    /*加法*/
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /*减法*/
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /*乘法*/
    public static double mul(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }

    /*除法*/
    public static double div(double d1, double d2, int len) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static void setIcon(String code, ImageView img_bg) {
        switch (code) {
            case "":
                img_bg.setVisibility(View.GONE);
                break;
            case "EOS":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_eos));
                break;
            case "LTC":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ltc));
                break;
            case "BCH":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_bch));
                break;
            case "ETC":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_etc));
                break;
            case "USDT":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_usdt));
                break;
            case "BTC":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_btc));
                break;
            case "ETH":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_eth));
                break;
            case "XRP":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_xrp));
                break;
            case "TRX":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_trx));
                break;
            case "HT":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ht));
                break;
            case "LINK":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_link));
                break;
            case "DASH":
                img_bg.setVisibility(View.VISIBLE);

                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_dash));
                break;
            case "ADA":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ada));
                break;
            case "BAT":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_bat));
                break;
            case "KNC":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_knc));
                break;
            case "XLM":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_xlm));
                break;
            case "XTZ":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_xtz));
                break;
            case "ZRX":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_zrx));
                break;
            case "YM":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ym));
                break;
            case "SI":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_si));
                break;
            case "NQ":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_nq));
                break;
            case "NG":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ng));
                break;
            case "HG":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_hg));
                break;
            case "GC":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_gc));
                break;
            case "CN":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_cn));
                break;
            case "CL":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_cl));
                break;
            case "DOT":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_dot));
                break;
            case "ATOM":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_atom));
                break;
            case "UNI":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_uni));
                break;
            case "DAX":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_dax));
                break;
            case "HSI":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_hsi));
                break;
            case "NK":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_nk));
                break;
            case "BTT":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_btt));
                break;
            case "KAVA":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_kava));
                break;
            case "LUNA":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_luna));
                break;
            case "BAL":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_bal));
                break;
            case "CKB":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ckb));
                break;
            case "EGLD":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_egld));
                break;
            case "FTM":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ftm));
                break;
            case "FTT":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ftt));
                break;
            case "KSM":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ksm));
                break;
            case "MATIC":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_matic));
                break;
            case "NEAR":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_near));
                break;
            case "OCEAN":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ocean));
                break;
            case "OGN":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ogn));
                break;
            case "SOL":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_sol));
                break;
            case "SUSHI":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_sushi));
                break;
            case "SXP":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_sxp));
                break;
            case "IRIS":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_iris));
                break;
            case "BNB":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_bnb));
                break;
            case "XMR":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_xmr));
                break;
            case "DOGE":
                img_bg.setVisibility(View.VISIBLE);
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_doge));
                break;
            default:
                img_bg.setImageDrawable(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_usdt));
                break;

        }
    }

    public static void setLeftImage(String code, TextView img_bg) {
        switch (code) {
            case "":
                break;
            case "EOS":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_eos), null, null, null);
                break;
            case "LTC":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ltc), null, null, null);

                break;
            case "BCH":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_bch), null, null, null);

                break;
            case "ETC":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_etc), null, null, null);

                break;
            case "USDT":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_usdt), null, null, null);

                break;
            case "BTC":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_btc), null, null, null);

                break;
            case "ETH":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_eth), null, null, null);

                break;
            case "XRP":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_xrp), null, null, null);

                break;
            case "TRX":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_trx), null, null, null);

                break;
            case "HT":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ht), null, null, null);

                break;
            case "LINK":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_link), null, null, null);

                break;
            case "DASH":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_dash), null, null, null);

                break;
            case "DOT":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_dot), null, null, null);
                break;
            case "ATOM":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_atom), null, null, null);
                break;
            case "UNI":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_uni), null, null, null);
                break;
            case "DAX":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_dax), null, null, null);
                break;
            case "HSI":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_hsi), null, null, null);
                break;
            case "NK":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_nk), null, null, null);
                break;
            case "BTT":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_btt), null, null, null);
                break;
            case "KAVA":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_kava), null, null, null);
                break;
            case "LUNA":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_luna), null, null, null);
                break;
            case "BAL":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_bal), null, null, null);
                break;
            case "CKB":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ckb), null, null, null);
                break;
            case "EGLD":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_egld), null, null, null);
                break;
            case "FTM":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ftm), null, null, null);
                break;
            case "FTT":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ftt), null, null, null);
                break;
            case "KSM":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ksm), null, null, null);
                break;
            case "MATIC":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_matic), null, null, null);
                break;
            case "NEAR":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_near), null, null, null);
                break;
            case "OCEAN":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ocean), null, null, null);
                break;
            case "OGN":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_ogn), null, null, null);
                break;
            case "SOL":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_sol), null, null, null);
                break;
            case "SUSHI":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_sushi), null, null, null);
                break;
            case "SXP":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_sxp), null, null, null);
                break;
            case "IRIS":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_iris), null, null, null);
                break;
            case "BNB":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_bnb), null, null, null);
                break;
            case "XMR":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_xmr), null, null, null);
                break;
            case "DOGE":
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_doge), null, null, null);
                break;
            default:
                img_bg.setCompoundDrawablesWithIntrinsicBounds(AppContext.getAppContext().getResources().getDrawable(R.mipmap.icon_usdt), null, null, null);

                break;

        }
    }
}
