package com.pro.bityard.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.google.gson.Gson;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.api.OnResult;
import com.pro.bityard.api.TradeResult;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.entity.BalanceEntity;
import com.pro.bityard.entity.ChargeUnitEntity;
import com.pro.bityard.entity.FundItemEntity;
import com.pro.bityard.entity.PositionEntity;
import com.pro.bityard.entity.QuoteCodeEntity;
import com.pro.bityard.entity.TradeListEntity;
import com.pro.bityard.manger.BalanceManger;
import com.pro.bityard.manger.NetIncomeManger;
import com.pro.bityard.manger.TradeListManger;
import com.pro.bityard.view.DecimalEditText;
import com.pro.switchlibrary.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pro.bityard.api.NetManger.SUCCESS;
import static com.pro.bityard.utils.Util.filter2;
import static com.pro.bityard.utils.Util.getAllSatisfyStr;
import static java.lang.Double.parseDouble;

public class TradeUtil {
    private static String TAG = "TradeUtil";

    public static long tenMin = 10 * 60 * 1000;


    public static double scale(int priceDigit) {
        if (priceDigit == 1) {
            return 0.1;
        } else if (priceDigit == 2) {
            return 0.01;
        } else if (priceDigit == 3) {
            return 0.001;
        } else if (priceDigit == 4) {
            return 0.0001;
        } else if (priceDigit == 5) {
            return 0.00001;
        } else if (priceDigit == 6) {
            return 0.000001;
        } else if (priceDigit == 7) {
            return 0.0000001;
        } else if (priceDigit == 8) {
            return 0.00000001;
        } else {
            return 0;
        }
    }


    public static String scaleString(int priceDigit) {
        if (priceDigit == 1) {
            return "0.1";
        } else if (priceDigit == 2) {
            return "0.01";
        } else if (priceDigit == 3) {
            return "0.001";
        } else if (priceDigit == 4) {
            return "0.0001";
        } else if (priceDigit == 5) {
            return "0.00001";
        } else if (priceDigit == 6) {
            return "0.000001";
        } else if (priceDigit == 7) {
            return "0.0000001";
        } else if (priceDigit == 9) {
            return "0.00000001";
        } else {
            return "0";
        }
    }

    /*保留两位小数*/
    public static String getNumberFormat(double value, int scale) {
        BigDecimal bd = new BigDecimal(value);
        String mon = bd.setScale(scale, RoundingMode.DOWN).toString();//保留两位数字，并且是截断不进行四舍五
        return mon;
    }

    public static String justDisplay(double value) {
        BigDecimal bg = new BigDecimal(value + "");
        return bg.toString();
    }


    /*long转时间*/
    public static String dateToStamp(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String format = simpleDateFormat.format(date);
        return format;
    }

    /*long转时间*/
    public static String dateToStampHour(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
        Date date = new Date(time);
        String format = simpleDateFormat.format(date);
        return format;
    }

    /*long转时间*/
    public static String dateToStampWithout(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String format = simpleDateFormat.format(date);
        return format;
    }

    public static String numberHalfUp(double value, int scale) {
        if (value != 0) {
            BigDecimal bd = new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
            String mon = bd.toString();//保留两位数字，四舍五
            return mon;
        } else {
            if (scale == 7) {
                DecimalFormat format = new DecimalFormat("0.0000000");
                BigDecimal bigDecimal = new BigDecimal(value);
                String mon = format.format(bigDecimal);//保留两位数字，四舍五
                return mon;
            } else if (scale == 8) {
                DecimalFormat format = new DecimalFormat("0.00000000");
                BigDecimal bigDecimal = new BigDecimal(value);
                String mon = format.format(bigDecimal);//保留两位数字，四舍五
                return mon;
            } else {
                BigDecimal bd = new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
                String mon = bd.toString();//保留两位数字，四舍五
                return mon;
            }
        }


    }

    /*止损价*/
    public static String StopLossPrice(boolean isBusy, double price, int priceDigit, double lever, double margin, double stopLoss) {
        String stopLossPrice;
        if (isBusy == true) {
            //买多 止损价  =  开仓价 - （开仓价 / 杠杆 * 止损比例）
            stopLossPrice = getNumberFormat(sub(price, mul(div(price, lever, 10), div(stopLoss, margin, 10))), priceDigit);
        } else {
            //买空 止损价  =  开仓价 +（开仓价 / 杠杆 * 止损比例）
            stopLossPrice = getNumberFormat(add(price, mul(div(price, lever, 10), div(stopLoss, margin, 10))), priceDigit);
        }
        return stopLossPrice;
    }

    /*止盈价*/
    public static String StopProfitPrice(boolean isBusy, double price, int priceDigit, double lever, double margin, double stopProfit) {
        String stopProfitPrice;
        if (isBusy == true) {
            //买多 止盈价  =  开仓价 + （开仓价 / 杠杆 * 止损比例）
            stopProfitPrice = getNumberFormat(add(price, mul(div(price, lever, 10), div(stopProfit, margin, 10))), priceDigit);
        } else {
            //买空 止盈价  =  开仓价 -（开仓价 / 杠杆 * 止损比例）
            stopProfitPrice = getNumberFormat(sub(price, mul(div(price, lever, 10), div(stopProfit, margin, 10))), priceDigit);
        }
        return stopProfitPrice;
    }

    /*盈利金额*/
    public static String ProfitAmount(boolean isBusy, double price, int priceDigit, double lever, double margin, double stopProfitPrice) {
        //盈利 金额
        String profitAmount;

        if (isBusy == true) {
            // (stopProfitPrice-price)/(price/lever)  *margin;
            double sub = sub(stopProfitPrice, price);
            double div = div(Math.abs(sub), div(price, lever, 10), 10);
            double mul = mul(div, margin);
            profitAmount = getNumberFormat(mul, 2);

        } else {
            double sub = sub(price, stopProfitPrice);
            double div = div(Math.abs(sub), div(price, lever, 10), 10);
            double mul = mul(div, margin);
            profitAmount = getNumberFormat(mul, 2);

        }

        return profitAmount;
    }


    /*亏损金额*/
    public static String lossAmount(boolean isBusy, double price, int priceDigit, double lever, double margin, double stopProfitPrice) {
        //盈利 金额

        String lossAmount;
        Log.d(TAG, "lossAmount: " + price + "  stopProfitPrice:" + stopProfitPrice + "  lever:" + lever + "  margin:" + margin);

        if (isBusy == true) {
            // (stopProfitPrice-price)/(price/lever)  *margin;
            double sub = sub(price, stopProfitPrice);
            double div = div(Math.abs(sub), div(price, lever, 20), 20);
            double mul = mul(div, margin);
            lossAmount = getNumberFormat(mul, 2);

        } else {
            double sub = sub(stopProfitPrice, price);
            double div = div(Math.abs(sub), div(price, lever, 20), 20);
            double mul = mul(div, margin);

            lossAmount = getNumberFormat(mul, 2);

        }
        return lossAmount;
    }


    /*订单盈亏*/
    public static String income(boolean isBuy, double price, double opPrice, double volume, int priceDigit) {
        String income;
        if (opPrice == 0) {
            return "0";
        } else {
            if (isBuy) {
                income = getNumberFormat(mul(sub(price, opPrice), volume), priceDigit);
            } else {
                income = getNumberFormat(mul(sub(opPrice, price), volume), priceDigit);
            }
            return income;
        }
    }


    /*浮动盈亏 需要的订单盈亏*/
    public static String incomeAdd(boolean isBuy, double price, double opPrice, double volume, int priceDigit) {
        String income;
        if (opPrice == 0) {
            return "0";
        } else {
            if (isBuy) {
                income = getNumberFormat(mul(sub(price, opPrice), volume), priceDigit);
            } else {
                income = getNumberFormat(mul(sub(opPrice, price), volume), priceDigit);
            }
            return income;
        }

    }


    /*加法*/
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /*加法*/
    public static String addBig(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).stripTrailingZeros().toPlainString();
    }

    /*减法*/
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /*减法*/
    public static String subBig(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).stripTrailingZeros().toPlainString();
    }


    public static double compare(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        int i = b1.compareTo(b2);
        return i;
    }

    /*乘法*/
    public static double mul(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }

    /*乘法*/
    public static String mulBig(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).stripTrailingZeros().toPlainString();
    }

    /*除法*/
    public static double div(double d1, double d2, int len) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /*除法*/
    public static String divBig(double d1, double d2, int len) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
    }

    /*价格*/
    public static void price(List<String> quoteList, String contractCode, TradeResult result) {
        if (quoteList != null) {
            for (String value : quoteList) {
                String[] split1 = value.split(",");
                if (contractCode.equals(split1[0])) {
                    result.setResult(split1[2]);
                }
            }
        }


    }

    public static String removeDigital(String value) {
        Pattern p = Pattern.compile("[\\d]");
        Matcher matcher = p.matcher(value);
        String result = matcher.replaceAll("");
        return result;
    }


    /*持仓价格*/
    public static void positionPrice(boolean isBuy, List<String> quoteList, String contractCode, TradeResult result) {
        if (quoteList != null) {
            for (String value : quoteList) {
                String[] split1 = value.split(",");
                if (removeDigital(contractCode).equals(removeDigital(split1[0]))) {
                    if (isBuy) {
                        result.setResult(split1[4]);
                    } else {
                        //原来是5 改了websocket之后就变成了6
                        result.setResult(split1[6]);
                    }
                }
            }
        }


    }

    /*净盈亏*/
    public static String netIncome(double income, double service) {
        String netIncome;
        netIncome = getNumberFormat((sub(income, service)), 2);
        return netIncome;
    }

    /*净盈亏所有*/
    public static void getNetIncome(List<String> quoteList, List<PositionEntity.DataBean> positionList, TradeResult tradeResult) {
        if (positionList == null) {
            tradeResult.setResult(null);
            return;
        }
        List<Double> incomeList = new ArrayList<>();
        for (PositionEntity.DataBean dataBean : positionList) {
            //这里判断的原因是这里增加了挂单的列表 需要过滤
            if (dataBean.getOpPrice() != 0.0) {
                boolean isBuy = dataBean.isIsBuy();
                double opPrice = dataBean.getOpPrice();
                double volume = dataBean.getVolume();
                double serviceCharge = dataBean.getServiceCharge();
                TradeUtil.positionPrice(isBuy, quoteList, dataBean.getContractCode(), response -> {
                    String income1 = income(isBuy, Double.parseDouble(response.toString()), opPrice, volume, 2);
                    // String s = netIncome(Double.parseDouble(income1), serviceCharge);
                    Log.d("hold", "getNetIncome:253:  " + income1);

                    incomeList.add(Double.parseDouble(income1));
                });
            } else {
                incomeList.add(0.0);
            }

        }

        Log.d("hold", "getNetIncome:253:  " + incomeList);
        if (incomeList.size() > 0) {
            double income = 0.0;
            for (int i = 0; i < incomeList.size(); i++) {
                income = TradeUtil.add(income, incomeList.get(i));
            }
            tradeResult.setResult(income);
        }
    }

    public static void myNetIncome(String tradeType, List<PositionEntity.DataBean> positionList, List<String> quoteList, OnResult onResult) {

        TradeUtil.getNetIncome(quoteList, positionList, response1 -> TradeUtil.getMargin(positionList, response2 -> {
            double margin;
            double income;
            if (positionList == null) {
                margin = 0.0;
                income = 0.0;
            } else {
                margin = Double.parseDouble(response2.toString());
                income = Double.parseDouble(response1.toString());
            }
            StringBuilder stringBuilder = new StringBuilder();

            StringBuilder append = stringBuilder.append(tradeType).append(",").append(income)
                    .append(",").append(margin);
            //总净值=可用余额-冻结资金+总净盈亏+其他钱包换算成USDT额
            //账户净值=可用余额+占用保证金+浮动盈亏
            onResult.setResult(append.toString());
        }));


    }


    /*总盈亏算法 持仓列表的盈亏之和*/
    public static void getIncome(List<String> quoteList, PositionEntity positionEntity, TradeResult tradeResult) {
        List<Double> incomeList = new ArrayList<>();
        for (PositionEntity.DataBean dataBean : positionEntity.getData()) {
            boolean isBuy = dataBean.isIsBuy();
            int priceDigit = dataBean.getPriceDigit();
            TradeUtil.positionPrice(isBuy, quoteList, dataBean.getContractCode(), response -> {
                String valueIncome = TradeUtil.incomeAdd(isBuy, parseDouble(response.toString()), dataBean.getOpPrice(), dataBean.getVolume(), priceDigit);
                incomeList.add(Double.parseDouble(valueIncome));
            });
        }
        if (incomeList.size() > 0) {
            double income = 0.0;
            for (int i = 0; i < incomeList.size(); i++) {
                income = TradeUtil.add(income, incomeList.get(i));
            }
            //   Log.d("print", "getIncome: 总盈亏: "+income);
            tradeResult.setResult(Double.parseDouble(numberHalfUp(income, 2)));
        }
    }


    /*冻结资金  所有的保证金和*/
    public static void getMargin(List<PositionEntity.DataBean> positionList, TradeResult tradeResult) {
        if (positionList == null) {
            tradeResult.setResult(null);
            return;
        }
        List<Double> marginList = new ArrayList<>();
        for (PositionEntity.DataBean dataBean : positionList) {
            double margin = dataBean.getMargin();
            marginList.add(margin);


        }
        if (marginList.size() > 0) {
            double margin = 0.0;
            for (int i = 0; i < marginList.size(); i++) {
                margin = TradeUtil.add(margin, marginList.get(i));
            }
            tradeResult.setResult(margin);
        } else {

        }

    }

    /*持仓的列表ID*/
    public static String positionIdList(PositionEntity positionEntity) {
        String substring;
        StringBuilder stringBuilder = new StringBuilder();
        if (positionEntity.getData().size() > 0) {
            for (PositionEntity.DataBean dataBean : positionEntity.getData()) {
                String id = dataBean.getId();
                stringBuilder.append(id + ",");
            }
            substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
        } else {
            substring = null;
        }

        return substring;
    }

    /*盈利百分比=盈利/保证金*/
    public static String profitRate(double content, double margin) {
        Log.d(TAG, "profitRate: " + content + "  --  " + margin);
        double div = div(content, margin, 10);
        double mul = mul(div, 100);
        String numberFormat = getNumberFormat(mul, 2);
        return numberFormat + "%";
    }

    public static String profitRateWithout(double content, double margin) {
        double div = div(content, margin, 10);
        String numberFormat = numberHalfUp(div, 2);
        return numberFormat;
    }


    /*亏损百分比=亏损/保证金*/
    public static String lossRate(double content, double margin) {
        Log.d(TAG, "profitRate: " + content + "  --  " + margin);
        double div = div(content, margin, 20);
        double mul = mul(div, 100);
        String numberFormat = getNumberFormat(mul, 2);
        return numberFormat + "%";
    }

    public static String lossRateWithout(double content, double margin) {
        Log.d(TAG, "profitRate: " + content + "  --  " + margin);
        double div = div(content, margin, 20);
        String numberFormat = numberHalfUp(div, 2);
        return numberFormat;
    }

    /* 杠杆 = 仓位*开仓价格/保证金 */
    public static String lever(double margin, double opPrice, double volume) {
        double mul = mul(volume, opPrice);
        double div = div(mul, margin, 10);
        return getNumberFormat(div, 2);
    }

    /* 仓位 =  杠杆 * 保证金 / 开仓价格*/
    public static String volume(double lever, String margin, double opPrice) {
        if (margin == null) {
            return null;
        }
        double mul = mul(lever, Double.parseDouble(margin));
        double div = div(mul, opPrice, 10);
        return numberHalfUp(div, 4);
    }

    /*保证金=仓位*开仓价格/杠杆*/
    public static String maxMargin(double lever, double margin, double opPrice, double volume, double deposit) {
        //Wilde, [15.05.20 15:06]
        //我用 最大保证金减去当前保证金  做一个值
        //Wilde, [15.05.20 15:06]
        //用最小杠杆· 带入当前的仓位去算保证金 然后减去当前保证金
        //Wilde, [15.05.20 15:06]
        //取两个里面的最小值
        double mul = mul(volume, opPrice);
        double div = div(mul, lever, 10);
        double sub = sub(div, margin);
        double sub1 = sub(deposit, margin);
        double small = small(sub, sub1);
        return getNumberFormat(small, 2);
    }

    public static String minMargin(double margin, double opPrice, double volume) {
        double big = big(sub(div(mul(volume, opPrice), 100, 10), margin), 0);
        return getNumberFormat(big, 2);
    }


    public static double big(double a, double b) {
        BigDecimal a1 = new BigDecimal(a);
        BigDecimal b1 = new BigDecimal(b);
        int bj = a1.compareTo(b1);
        if (bj > 0) {
            return a;
        } else {
            return b;
        }
    }

    public static double small(double a, double b) {
        BigDecimal a1 = new BigDecimal(a);
        BigDecimal b1 = new BigDecimal(b);
        int bj = a1.compareTo(b1);
        if (bj > 0) {
            return b;
        } else {
            return a;
        }
    }


    /*总计支付*/
    public static String total(String margin, String service, String deduction) {
        double marginDou = Double.parseDouble(margin);
        double serviceDou = Double.parseDouble(service);
        double deductionDou = Double.parseDouble(deduction);

        double add = TradeUtil.add(marginDou, serviceDou);
        double sub = TradeUtil.sub(add, deductionDou);
        return TradeUtil.numberHalfUp(sub, 2);
    }

    /*抵扣*/
    public static String deduction(String margin, String prizeTrade, String service) {
        String s = numberHalfUp(TradeUtil.mul(Double.parseDouble(margin), Double.parseDouble(prizeTrade)), 2);
        double add = add(Double.parseDouble(s), Double.parseDouble(service));
        return numberHalfUp(add, 2);
    }


    /*计算手续费  保证金*杠杆*费率**/
    public static String serviceCharge(ChargeUnitEntity chargeUnitEntity, int coinFormula, String margin, double lever) {
        String charge = null;
        if (chargeUnitEntity == null) {
            return null;
        }
        if (margin == null) {
            return null;
        }
        double div = div(chargeUnitEntity.getChargeCoinList().get(1), 1000, 10);
        if (coinFormula == 3) {
            charge = getNumberFormat(mul(mul(Double.parseDouble(margin), lever), div), 2);
        }
        return charge;
    }

    /*是否递延*/
    public static String defer(String tradeType, boolean isDefer) {
        String defer;
        if (tradeType.equals("1")) {
            defer = isDefer ? "true" : "false";
        } else {
            defer = "false";
        }
        return defer;
    }

    /*下单保证金*/
    public static String marginOrder(String orderType, String marginMarket, String marginLimit) {
        String margin;
        if (orderType.equals("0")) {
            if (marginMarket.length() == 0) {
                margin = null;
            } else {
                margin = marginMarket;
            }
        } else {
            if (marginLimit.length() == 0) {
                margin = null;
            } else {
                margin = marginLimit;
            }
        }
        return margin;
    }

    /*下单 价格*/
    public static String priceOrder(String orderType, String limitPrice) {
        String priceOrder;
        if (orderType.equals("0")) {
            priceOrder = "0";
        } else {
            if (limitPrice.length() == 0) {
                priceOrder = null;
            } else {
                priceOrder = limitPrice;
            }
        }
        return priceOrder;
    }

    /*计算递延费   保证金*杠杆*费率 *0.00045*/
    public static String deferFee(int priceDigit, String defer, double deferBase, String margin, double lever) {
        if (margin == null) {
            return null;
        }
        String deferFee;
        if (defer.equals("true")) {
            deferFee = TradeUtil.numberHalfUp(mul(mul(Double.parseDouble(margin), lever), deferBase), 4);
        } else {
            deferFee = "0";
        }
        return deferFee;

    }

    /*计算单个汇率*/
    public static void getRate(BalanceEntity balanceEntity, String moneyType, TradeResult tradeResult) {
        List<Double> balanceList = new ArrayList<>();
        for (BalanceEntity.DataBean data : balanceEntity.getData()) {
            String currency = data.getCurrency();
            Log.d(TAG, "getRate:505:  " + currency);
            NetManger.getInstance().rate(data, moneyType, currency, "USDT", (state, response) -> {
                if (state.equals(SUCCESS)) {
                    balanceList.add(Double.parseDouble(response.toString()));
                    double balance = 0.0;
                    for (double a : balanceList) {
                        balance = TradeUtil.add(balance, a);
                    }
                    if (balanceList.size() == balanceEntity.getData().size()) {
                        tradeResult.setResult(balance);
                    }
                }
            });
        }

    }


    /*列表汇率*/
    public static void getRateList(BalanceEntity balanceEntity, String moneyType, TradeResult tradeResult) {
        List<Double> balanceList = new ArrayList<>();
        for (BalanceEntity.DataBean data : balanceEntity.getData()) {
            String currency = data.getCurrency();
            NetManger.getInstance().rateList(data, moneyType, currency, "USDT", (state, response) -> {
                if (state.equals(SUCCESS)) {
                    balanceList.add(Double.parseDouble(response.toString()));
                    double balance = 0.0;
                    for (double a : balanceList) {
                        balance = TradeUtil.add(balance, a);
                    }
                    if (balanceList.size() == balanceEntity.getData().size()) {
                        tradeResult.setResult(balance);
                    }
                }
            });


        }

    }

    /*价格从小到大*/
    public static List<String> priceLowToHigh(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();

        Collections.sort(quoteList, (o1, o2) -> {
            String[] split1 = o1.split(",");
            String[] split2 = o2.split(",");

            double compare = compare(parseDouble(split1[2]), parseDouble(split2[2]));

           /* double sub = TradeUtil.sub(Double.parseDouble(split1[2]), Double.parseDouble(split2[2]));
            if (sub == 0) {
                return (int) TradeUtil.sub(Double.parseDouble(split2[2]), Double.parseDouble(split1[2]));
            }*/
            return (int) compare;
        });
        for (String quote : quoteList) {
            quoteList2.add(quote);
        }

        return quoteList2;
    }

    /*BTC BCH ETH*/
    public static List<String> homeHot(List<String> quoteList) {
        String json = SPUtils.getString(AppConfig.QUOTE_CODE_JSON, null);

        QuoteCodeEntity quoteCodeEntity = new Gson().fromJson(json, QuoteCodeEntity.class);
        List<String> quoteList2 = new ArrayList<>();

        for (int i = 0; i < quoteCodeEntity.getGroup().size(); i++) {
            if (quoteCodeEntity.getGroup().get(i).getName().equals(AppConfig.BEST)) {
                for (int j = 0; j < quoteCodeEntity.getGroup().get(i).getList().size(); j++) {
                    for (String mainQuote : quoteList) {
                        String[] split = mainQuote.split(",");
                        int length = split.length;
                        if (split[length - 5].equals(quoteCodeEntity.getGroup().get(i).getList().get(j))) {
                            quoteList2.add(mainQuote);
                        }
                    }
                }
            }
        }
       /* List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH)) {
                quoteList2.add(mainQuote);
            }
        }*/
        return quoteList2;
    }
  /*  public static List<String> homeHot(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String quote : quoteList) {
            String[] split = quote.split(",");
            int length = split.length;
            if (split[length - 2].toLowerCase().equals("ethusdt")
                    || split[length - 2].toLowerCase().equals("bchusdt")
                    || split[length - 2].toLowerCase().equals("btcusdt")) {
                quoteList2.add(quote);
            }
        }

        return quoteList2;
    }*/

    /*除去 BTC BCH ETH*/
    public static List<String> homeList(List<String> quoteList) {
        String json = SPUtils.getString(AppConfig.QUOTE_CODE_JSON, null);

        QuoteCodeEntity quoteCodeEntity = new Gson().fromJson(json, QuoteCodeEntity.class);
        List<String> quoteList2 = new ArrayList<>();

        for (int i = 0; i < quoteCodeEntity.getGroup().size(); i++) {
            if (quoteCodeEntity.getGroup().get(i).getName().equals(AppConfig.RECOMMEND)) {
                for (int j = 0; j < quoteCodeEntity.getGroup().get(i).getList().size(); j++) {
                    for (String mainQuote : quoteList) {
                        String[] split = mainQuote.split(",");
                        int length = split.length;
                        if (split[length - 5].equals(quoteCodeEntity.getGroup().get(i).getList().get(j))) {
                            quoteList2.add(mainQuote);
                        }
                    }
                }
            }
        }
       /* List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH)) {
                quoteList2.add(mainQuote);
            }
        }*/
        return quoteList2;
    }

   /* public static List<String> homeList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String quote : quoteList) {
            String[] split = quote.split(",");
            int length = split.length;

            if (split[length - 2].contains("DASH") || split[length - 2].contains("XRP") || split[length - 2].contains("ETC")
                    || split[length - 2].contains("TRX") || split[length - 2].contains("LTC") || split[length - 2].contains("EOS")
                    || split[length - 2].contains("LINK")) {
                quoteList2.add(quote);
            }
        }

        return quoteList2;
    }*/

    /*字母a-z*/
    public static List<String> nameLowToHigh(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        Collections.sort(quoteList, (o1, o2) -> {
            String[] split1 = o1.split(",");
            String[] split2 = o2.split(",");
            int i = split1[0].compareTo(split2[0]);
            if (i > 0) {
                return 1;
            } else {
                return -1;
            }
        });
        for (String quote : quoteList) {
            quoteList2.add(quote);
        }

        return quoteList2;
    }

    /*字母z-a*/
    public static List<String> nameHighToLow(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        Collections.sort(quoteList, (o1, o2) -> {
            String[] split1 = o1.split(",");
            String[] split2 = o2.split(",");
            int i = split1[0].compareTo(split2[0]);
            if (i > 0) {
                return -1;
            } else {
                return 1;
            }
        });
        for (String quote : quoteList) {
            quoteList2.add(quote);
        }

        return quoteList2;
    }

    /*搜素*/
    public static List<String> searchQuoteList(String content, List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            if (Objects.requireNonNull(split[split.length - 2]).toLowerCase().contains(content.toLowerCase())) {
                quoteList2.add(mainQuote);
            }
           /* if (Objects.requireNonNull(TradeUtil.listQuoteName(split[0])).toLowerCase().contains(content.toLowerCase())) {
                quoteList2.add(mainQuote);
            }*/
        }
        return quoteList2;
    }

    /*搜素币种*/
    public static List<String> searchCurrencyList(String content, List<String> currencyBaseList) {
        List<String> currencyList = new ArrayList<>();
        for (String currency : currencyBaseList) {
            if (Objects.requireNonNull(currency).toLowerCase().contains(content.toLowerCase())) {
                currencyList.add(currency);
            }
        }
        return currencyList;
    }

    /* 合约*/
    public static List<String> contractQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.CONTRACT);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                int length = split.length;
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_FT
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        Log.d("print", "derivedQuoteList:1073:  " + quoteList2);
        return quoteList2;


        /*String json = SPUtils.getString(AppConfig.QUOTE_CODE_JSON, null);

        QuoteCodeEntity quoteCodeEntity = new Gson().fromJson(json, QuoteCodeEntity.class);
        List<String> quoteList2 = new ArrayList<>();

        for (int i = 0; i < quoteCodeEntity.getGroup().size(); i++) {
            if (quoteCodeEntity.getGroup().get(i).getName().equals(AppConfig.CONTRACT) || quoteCodeEntity.getGroup().get(i).getName().equals(AppConfig.DERIVATIVES)) {
                for (int j = 0; j < quoteCodeEntity.getGroup().get(i).getList().size(); j++) {
                    for (String mainQuote : quoteList) {
                        String[] split = mainQuote.split(",");
                        int length = split.length;
                        if (split[length - 5].equals(quoteCodeEntity.getGroup().get(i).getList().get(j))) {
                            quoteList2.add(mainQuote);
                        }
                    }
                }
            }
        }*/
       /* List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH)) {
                quoteList2.add(mainQuote);
            }
        }*/
        // return quoteList2;
    }

   /* public static List<String> contractQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            // Log.d("print", "contractQuoteList:所有合约: "+split[length-2]+"-"+split[length-4]);
            if ((split[length - 4].equals(AppConfig.TYPE_FT))) {
                quoteList2.add(mainQuote);
                //   Log.d("print", "contractQuoteList:过滤好的: "+split[length-2]+"-"+split[length-4]);
            }

        }
        return quoteList2;
    }*/

    /* 加密货币*/
    public static List<String> contractMainQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.MAIN);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                int length = split.length;
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_FT
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        Log.d("print", "derivedQuoteList:1073:  " + quoteList2);
        return quoteList2;

       /* String json = SPUtils.getString(AppConfig.QUOTE_CODE_JSON, null);

        QuoteCodeEntity quoteCodeEntity = new Gson().fromJson(json, QuoteCodeEntity.class);
        List<String> quoteList2 = new ArrayList<>();

        for (int i = 0; i < quoteCodeEntity.getGroup().size(); i++) {
            if (quoteCodeEntity.getGroup().get(i).getName().equals(AppConfig.MAIN)) {
                for (int j = 0; j < quoteCodeEntity.getGroup().get(i).getList().size(); j++) {
                    for (String mainQuote : quoteList) {
                        String[] split = mainQuote.split(",");
                        int length = split.length;
                        if (split[length - 5].equals(quoteCodeEntity.getGroup().get(i).getList().get(j))) {
                            quoteList2.add(mainQuote);
                        }
                    }
                }
            }
        }*/
       /* List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH)) {
                quoteList2.add(mainQuote);
            }
        }*/
        // return quoteList2;
    }

  /*  public static List<String> contractMainQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if ((split[length - 4].equals(AppConfig.TYPE_FT) && split[length - 3].equals(AppConfig.ZONE_MAIN))) {
                quoteList2.add(mainQuote);
            }

        }
        return quoteList2;
    }*/


    /*主区*/
    public static List<String> mainQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_FT) && split[length - 3].equals(AppConfig.ZONE_MAIN)) {
                quoteList2.add(mainQuote);
            }
        }
        return quoteList2;
    }


    /* 创新区*/
    public static List<String> innovationQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_FT) && split[length - 3].equals(AppConfig.ZONE_INNOVATION)) {
                quoteList2.add(mainQuote);
            }
        }
        return quoteList2;
    }

    public static List<String> resultGroup(String type) {
        String json = SPUtils.getString(AppConfig.QUOTE_CODE_JSON, null);
        QuoteCodeEntity quoteCodeEntity = new Gson().fromJson(json, QuoteCodeEntity.class);
        List<String> groupList = new ArrayList<>();
        for (int i = 0; i <quoteCodeEntity.getGroup().size(); i++) {
            if (quoteCodeEntity.getGroup().get(i).getName().equals(type)) {
                for (int j = quoteCodeEntity.getGroup().get(i).getList().size() - 1; j >= 0; j--) {
                    groupList.add(quoteCodeEntity.getGroup().get(i).getList().get(j));
                }
            }
        }
        return groupList;
    }

    /* DEFI*/
    public static List<String> derivedQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.DERIVATIVES);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                int length = split.length;
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_FT
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        return quoteList2;
    }

    /* BSC*/
    public static List<String> bscQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.BSC);
        Log.d("print", "bscQuoteList:bsc1115: "+strings);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_CH
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        return quoteList2;
    }


    public static List<String> spiltQuoteList(List<String> quoteList, String type) {
        String json = SPUtils.getString(AppConfig.QUOTE_CODE_JSON, null);

        QuoteCodeEntity quoteCodeEntity = new Gson().fromJson(json, QuoteCodeEntity.class);
        List<String> quoteList2 = new ArrayList<>();

        for (int i = 0; i < quoteCodeEntity.getGroup().size(); i++) {
            if (quoteCodeEntity.getGroup().get(i).getName().equals(type)) {
                for (int j = 0; j < quoteCodeEntity.getGroup().get(i).getList().size(); j++) {
                    for (String mainQuote : quoteList) {
                        String[] split = mainQuote.split(",");
                        int length = split.length;
                        if (split[length - 5].equals(quoteCodeEntity.getGroup().get(i).getList().get(j))) {
                            quoteList2.add(mainQuote);
                        }
                    }
                }
            }
        }
       /* List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH)) {
                quoteList2.add(mainQuote);
            }
        }*/
        return quoteList2;
    }

    /* 现货*/
    public static List<String> spotQuoteList(List<String> quoteList) {
        //  Log.d("print", "derivedQuoteList:1056: "+quoteList.size());
        List<String> strings = TradeUtil.resultGroup(AppConfig.SPOT);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                int length = split.length;
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_CH
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        Log.d("print", "derivedQuoteList:1073:  " + quoteList2);
        return quoteList2;

    }

    /* 外汇*/

    public static List<String> foreignExchangeQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.FOREX);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                int length = split.length;
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_FT
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        return quoteList2;
    }
    /*public static List<String> foreignExchangeQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_FE)) {
                quoteList2.add(mainQuote);
            }
        }
        return quoteList2;
    }*/

    /* 现货DEFI*/
    public static List<String> spotDEFIQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.DEFI);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_CH
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        return quoteList2;
    }


    /* 现货DEFI*/
   /* public static List<String> spotDEFIQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH) && split[length - 3].equals(AppConfig.ZONE_DEFI)) {
                quoteList2.add(mainQuote);
            }
        }
        return quoteList2;
    }*/
    /* 现货pos*/
    public static List<String> spotPOSQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.POS);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_CH
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        return quoteList2;
    }

    /* 现货pos*/
  /*  public static List<String> spotPOSQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH) && split[length - 3].equals(AppConfig.ZONE_POS)) {
                quoteList2.add(mainQuote);
            }
        }
        return quoteList2;
    }
*/
    /* 现货gray*/
    public static List<String> spotGRAYQuoteList(List<String> quoteList) {
        List<String> strings = TradeUtil.resultGroup(AppConfig.GRAY);
        List<String> quoteList2 = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            for (String mainQuote : quoteList) {
                String[] split = mainQuote.split(",");
                if (filter2(split[0]).equals(strings.get(i))) {
                    quoteList2.add(mainQuote + "," + filter2(split[0])
                            + "," + AppConfig.TYPE_CH
                            + "," + 0
                            + "," + filter(split[0].replaceAll("CC", ""))
                            + "," + "USDT");
                }
            }
        }
        return quoteList2;
    }

   /* public static List<String> spotGRAYQuoteList(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        for (String mainQuote : quoteList) {
            String[] split = mainQuote.split(",");
            int length = split.length;
            if (split[length - 4].equals(AppConfig.TYPE_CH) && split[length - 3].equals(AppConfig.ZONE_GRAY)) {
                quoteList2.add(mainQuote);
            }
        }
        return quoteList2;
    }*/

    /* 自选*/
    public static List<String> optionalQuoteList(List<String> quoteList) {
        Set<String> optionalList = Util.SPDealResult(SPUtils.getString(AppConfig.KEY_OPTIONAL, null));
        //Log.d("print", "optionalQuoteList:已选全部自选:  " + optionalList);
        if (optionalList.size() == 0) {
            return null;
        } else {
            List<String> quoteList2 = new ArrayList<>();
            Iterator<String> iterator = optionalList.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                for (String mainQuote : quoteList) {
                    String[] split = mainQuote.split(",");
                    if (split[0].equals(next)) {
                        quoteList2.add(mainQuote);
                    }
                }
            }
            return quoteList2;
        }

    }


    /* 历史记录*/
    public static List<String> historyQuoteList(List<String> quoteList) {
        Set<String> optionalList = Util.SPDealResult(SPUtils.getString(AppConfig.KEY_HISTORY, null));
        // Log.d("print", "optionalQuoteList:历史记录:  " + optionalList);
        if (optionalList.size() == 0) {
            return null;
        } else {
            List<String> quoteList2 = new ArrayList<>();
            Iterator<String> iterator = optionalList.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                for (String mainQuote : quoteList) {
                    String[] split = mainQuote.split(",");
                    if (split[0].equals(next)) {
                        quoteList2.add(mainQuote);
                    }
                }
            }
            return quoteList2;
        }

    }


    /* 现货自选*/
    public static List<String> optionalSpotQuoteList(List<String> quoteList) {
        Set<String> optionalList = Util.SPDealResult(SPUtils.getString(AppConfig.KEY_OPTIONAL, null));
        if (optionalList.size() == 0) {
            return null;
        } else {
            List<String> quoteList2 = new ArrayList<>();
            Iterator<String> iterator = optionalList.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                for (String mainQuote : quoteList) {
                    String[] split = mainQuote.split(",");
                    if (split[0].equals(next)) {
                        quoteList2.add(mainQuote);
                    }
                }
            }
            return quoteList2;
        }
    }


    /*价格从大到小*/
    public static List<String> priceHighToLow(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        Collections.sort(quoteList, (o1, o2) -> {
            String[] split1 = o1.split(",");
            String[] split2 = o2.split(",");
            double compare = compare(parseDouble(split2[2]), parseDouble(split1[2]));


           /* double sub = TradeUtil.sub(Double.parseDouble(split2[2]), Double.parseDouble(split1[2]));
            if (sub == 0) {
                return (int) TradeUtil.sub(Double.parseDouble(split1[2]), Double.parseDouble(split2[2]));
            }*/
            return (int) compare;
        });
        for (String quote : quoteList) {
            quoteList2.add(quote);
        }

        return quoteList2;
    }

    /*涨跌幅从小到大*/
    public static List<String> rangeLowToHigh(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        Collections.sort(quoteList, (o1, o2) -> {
            String[] split1 = o1.split(",");
            String[] split2 = o2.split(",");
            double v = Double.valueOf(split1[2]);
            double v1 = Double.valueOf(split1[3]);
            double mul = TradeUtil.mul(TradeUtil.div(TradeUtil.sub(v, v1), v, 10), 100);
            double v2 = Double.valueOf(split2[2]);
            double v3 = Double.valueOf(split2[3]);
            double mul2 = TradeUtil.mul(TradeUtil.div(TradeUtil.sub(v2, v3), v2, 10), 100);
            double compare = compare(mul, mul2);

           /* double sub = TradeUtil.sub(mul, mul2);
            if (sub == 0) {
                return (int) TradeUtil.sub(mul2, mul);
            }*/
            return (int) compare;
        });
        for (String quote : quoteList) {
            quoteList2.add(quote);
        }

        return quoteList2;
    }

    /*涨跌幅从大到小*/
    public static List<String> rangeHighToLow(List<String> quoteList) {
        List<String> quoteList2 = new ArrayList<>();
        Collections.sort(quoteList, (o1, o2) -> {
            String[] split1 = o1.split(",");
            String[] split2 = o2.split(",");
            double v = Double.valueOf(split1[2]);
            double v1 = Double.valueOf(split1[3]);
            double mul = TradeUtil.mul(TradeUtil.div(TradeUtil.sub(v, v1), v, 10), 100);
            double v2 = Double.valueOf(split2[2]);
            double v3 = Double.valueOf(split2[3]);
            double mul2 = TradeUtil.mul(TradeUtil.div(TradeUtil.sub(v2, v3), v2, 10), 100);
            double compare = compare(mul2, mul);


           /* double sub = TradeUtil.sub(mul2, mul);
            if (sub == 0) {
                return (int) TradeUtil.sub(mul, mul2);
            }*/
            return (int) compare;
        });
        for (String quote : quoteList) {
            quoteList2.add(quote);
        }

        return quoteList2;
    }


    /*根据合约号获取相应的实时行情*/
    public static String itemQuote(String contCode, List<String> quoteList) {
        if (quoteList == null) {
            return null;
        } else {
            for (String quote : quoteList) {
                String[] split = quote.split(",");
                if (contCode.equals(split[0])) {
                    return quote;
                }
            }
        }
        return null;
    }

    /*获取当前行情的ContCode*/
    public static String itemQuoteContCode(String quote) {
        if (quote == null) {
            return null;
        } else {
            String[] split = quote.split(",");
            return split[0].replaceAll(" ", "");
        }

    }

    public static String contractCode(String quote) {
        if (quote == null) {
            return null;
        } else {
            String[] split = quote.split(",");
            return split[0].replaceAll(" ", "").replaceAll("_CC", "");
        }

    }


    /*获取当前行情的Code*/
    public static String itemQuoteCode(String quote) {
        if (quote == null) {
            return null;
        } else {
            String[] split = quote.split(",");
            Log.d(TAG, "itemQuoteCode:550: " + split[0].replaceAll("[^a-z^A-Z]", ""));
            return split[0].replaceAll("[^a-z^A-Z]", "");
        }

    }

    /*当前是否涨跌*/
    public static String itemQuoteIsRange(String quote) {
        String[] split = quote.split(",");
        if (sub(Double.parseDouble(split[9]), Double.parseDouble(split[6])) > 0) {
            return "1";
        } else if (sub(Double.parseDouble(split[9]), Double.parseDouble(split[6])) == 0) {
            return "0";
        } else {
            return "-1";
        }
    }

    /*当前是否涨跌*/
    public static String listQuoteIsRange(String quote) {
        String[] split = quote.split(",");
        return split[1];
    }


    /*获取当前价格*/
    public static String itemQuotePrice(String quote) {
        String[] split = quote.split(",");
        return split[9];
    }

    /*获取单个的合约*/
    public static Object tradeDetail(String contractCode, List<TradeListEntity> tradeListEntityList) {
        if (tradeListEntityList == null) {
            return null;
        } else {
            for (TradeListEntity data : tradeListEntityList) {
                if (contractCode.equals(data.getContractCode())) {
                    return data;
                }
            }
        }
        return null;
    }


    /*获取单个的手续费*/
    public static void chargeDetail(String code, JSONObject jsonObject, OnResult onResult) {

        if (jsonObject == null) {
            onResult.setResult(null);
        } else {
            try {
                for (int i = 0; i < jsonObject.length(); i++) {
                    JSONObject result = (JSONObject) jsonObject.get(code);
                    ChargeUnitEntity chargeUnitEntity = new Gson().fromJson(result.toString(), ChargeUnitEntity.class);
                    chargeUnitEntity.setCode(code);//因为后台返回没有加code  例如: BTCUSDT
                    onResult.setResult(chargeUnitEntity);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*获取保证金范围*/
    public static String deposit(List<String> depositList) {
        String deposit = null;
        if (depositList.size() == 0) {
            deposit = null;
        } else if (depositList.size() == 1) {
            deposit = depositList.get(0) + "";
        } else if (depositList.size() == 2) {
            deposit = depositList.get(0) + "~" + depositList.get(1);
        }
        return deposit;
    }

    public static String depositMin(List<String> depositList) {
        String deposit = null;
        if (depositList.size() == 0) {
            deposit = null;
        } else if (depositList.size() == 1) {
            deposit = depositList.get(0) + "";
        } else if (depositList.size() == 2) {
            deposit = depositList.get(0) + "";
        }
        return deposit;
    }

    public static String marginMin(List<String> depositList) {
        String deposit = null;
        if (depositList.size() == 0) {
        } else {
            deposit = depositList.get(0) + "";
        }
        return deposit;
    }

    public static String marginMax(List<String> depositList) {
        String deposit = null;
        if (depositList.size() == 0) {
        } else {
            deposit = depositList.get(1) + "";
        }
        return deposit;
    }

    /*获取做多价格*/
    public static String itemQuoteBuyMuchPrice(String quote, int spread) {
        String[] split = quote.split(",");
        if (spread == 0) {
            return split[1];
        } else if (spread == -1) {
            return split[9];
        } else {
            return getNumberFormat(sub(Double.parseDouble(split[9]), spread), decimalPoint(split[9]));
        }
    }

    /*获取做空价格*/
    public static String itemQuoteBuyEmptyPrice(String quote, int spread) {
        String[] split = quote.split(",");
        if (spread == 0) {
            return split[3];
        } else if (spread == -1) {
            return split[9];
        } else {
            return getNumberFormat(add(Double.parseDouble(split[9]), spread), decimalPoint(split[9]));
        }
    }

    /*今日最高价格*/
    public static String itemQuoteMaxPrice(String quote) {
        String[] split = quote.split(",");
        return split[7];
    }

    /*今日最低价格*/
    public static String itemQuoteMinPrice(String quote) {
        String[] split = quote.split(",");
        return split[8];
    }

    /*24H成交量*/
    public static String itemQuoteVolume(String quote) {
        String[] split = quote.split(",");
        return split[12];
    }

    public static String filter(String content) {
        StringBuilder stringBuilder;
        ArrayList<String> allSatisfyStr = getAllSatisfyStr(content, "[a-zA-Z]");
        stringBuilder = new StringBuilder();
        for (int i = 0; i < allSatisfyStr.size(); i++) {
            stringBuilder.append(allSatisfyStr.get(i));
        }
        //return stringBuilder.toString();
        if (stringBuilder.toString().contains("_")) {
            return stringBuilder.toString().replaceAll("_", "");
        } else {
            return stringBuilder.toString();
        }
    }


    /*名称*/
    public static String listQuoteName(String quote) {
        String[] split = quote.split(",");
        String cc = split[0].replaceAll("_CC", "");
        String s = Util.quoteList(cc);
        if (s != null) {
            String[] split1 = s.split(",");
            return split1[0];

        } else {
            return null;
        }
    }

    public static String listSpotQuoteName(String quote) {
        String[] split = quote.split(",");
        String cc = split[0].replaceAll("_CC", "");
        String filter = filter(cc).replaceAll("USDT", "");
        return filter;
    }


    /*获取商品类型*/
    public static String type(String quote) {
        String[] split = quote.split(",");
        return split[split.length - 4];
    }

    public static String zone(String quote) {
        String[] split = quote.split(",");
        return split[split.length - 3];
    }

    public static String name(String quote) {
        String[] split = quote.split(",");
        if (split[split.length - 2].equals("")) {
            return "null";
        } else {
            return split[split.length - 2];
        }
    }

    public static String code(String quote) {
        String[] split = quote.split(",");
        return split[split.length - 5];
    }

    public static String nameWithoutUsd(String quote) {
        String[] split = quote.split(",");
        String name = split[split.length - 2];
        if (name.toLowerCase().contains("usdt")) {
            String substring = name.substring(0, name.length() - 4);
            return substring;
        } else {
            return name;
        }
    }

    public static String currency(String quote) {
        String[] split = quote.split(",");
        return split[split.length - 1];
    }

    public static String newListQuoteName(String quote) {
        String[] split = quote.split(",");
        return split[split.length - 1];
    }

    /*usdt*/
    public static String listQuoteUSD(String quote) {
        String[] split = quote.split(",");
        String[] split1 = Util.quoteList(split[0]).split(",");
        if (split1[1].equals("null")) {
            return null;
        } else {
            return split1[1];
        }
    }


    /*名称合起来*/
    public static String listQuoteNameUSD(String quote) {
        String[] split = quote.split(",");
        return Util.quoteNme(split[0]);
    }

    /*获取当前价格*/
    public static String listQuotePrice(String quote) {
        String[] split = quote.split(",");
        return split[2];
    }

    /*成交量*/
    public static String listQuoteBuyVolume(String quote) {
        String[] split = quote.split(",");
        return split[5];
    }

    /*成交量*/
    public static String listQuoteSellVolume(String quote) {
        String[] split = quote.split(",");
        return split[7];
    }

    /*开盘价*/
    public static String itemQuoteTodayPrice(String quote) {
        String[] split = quote.split(",");
        return split[6];
    }

    /*开盘价*/
    public static String listQuoteTodayPrice(String quote) {
        String[] split = quote.split(",");
        return split[3];
    }


    /*判断当前有几位小数*/
    public static int decimalPoint(String price) {
        if (price == null) {
            return 0;
        } else {
            if (price.contains(".")) {
                return price.length() - price.indexOf(".") - 1;
            } else {
                return 0;
            }
        }


    }

    /*价格变化*/
    public static String quoteChange(String price, String todayPrice) {
        double sub = sub(Double.parseDouble(price), Double.parseDouble(todayPrice));
        String result = null;
        if (sub > 0) {
            result = "+" + numberHalfUp(sub, decimalPoint(price));
        } else if (sub < 0) {
            result = numberHalfUp(sub, decimalPoint(price));
        } else if (sub == 0) {
            result = numberHalfUp(sub, decimalPoint(price));
        }
        return result;
    }

    /*涨跌幅*/
    public static String quoteRange(String price, String todayPrice) {
        double sub = sub(Double.parseDouble(price), Double.parseDouble(todayPrice));

        double div = div(sub, Double.parseDouble(price), 10);
        double mul = mul(div, 100);
        String numberFormat = getNumberFormat(mul, 2);
        String result = null;
        if (sub > 0) {
            result = "+" + numberFormat + "%";
        } else if (sub == 0) {
            result = numberFormat + "%";
        } else if (sub < 0) {
            result = numberFormat + "%";
        }

        return result;
    }

    /*获取合约号的 Spread*/
    public static String spread(String contractCode, List<TradeListEntity> tradeListEntityList) {
        if (tradeListEntityList == null) {
            return null;
        }
        if (contractCode == null) {
            return null;
        }
        for (TradeListEntity data : tradeListEntityList) {
            if (contractCode.equals(data.getContractCode())) {
                return String.valueOf(data.getSpread());
            }
        }
        return null;
    }

    /*能否追加保证金*/
    public static boolean isAddMargin(String contractCode, double lever, double margin) {
        List<TradeListEntity> tradeListEntityList = TradeListManger.getInstance().getTradeListEntityList();
        if (tradeListEntityList == null) {
            return false;
        } else {
            TradeListEntity tradeListEntity = (TradeListEntity) TradeUtil.tradeDetail(contractCode, tradeListEntityList);
            Log.d("print", "isAddMargin:1525:  " + tradeListEntity);

            if (tradeListEntity == null) {
                return false;
            }
            Integer integer = Integer.valueOf(tradeListEntity.getLeverList().get(0));
            Integer integer1 = Integer.valueOf(tradeListEntity.getDepositList().get(1));
            if (lever <= integer || margin == integer1) {
                return false;
            } else {
                return true;
            }
        }

    }

    /*盈亏比*/
    public static String ratio(double income, double margin) {
        double div = div(income, margin, 10);
        double mul = mul(div, 100);
        return getNumberFormat(mul, 2) + "%";
    }

    public static double ratioDouble(double income, double margin) {
        double div = div(income, margin, 10);
        double mul = mul(div, 100);
        return mul;
    }

    /*计算抵扣金额 抵扣金额是=礼金抵扣+红包抵扣 */
    public static String deductionResult(String service, String margin, String prizeTrade, String luckyTrade) {
        if (service == null) {
            return null;
        }


        double prize_mul = TradeUtil.mul(parseDouble(margin), parseDouble(prizeTrade));
        double prize = BalanceManger.getInstance().getPrize();
        double prizeDou;
        if (prizeTrade != null) {
            if (prize_mul <= prize) {
                prizeDou = Double.parseDouble(TradeUtil.getNumberFormat(prize_mul, 2));
            } else {
                prizeDou = 0.00;
            }
        } else {
            prizeDou = 0.00;
        }
        double lucky_mul = TradeUtil.mul(parseDouble(service), parseDouble(luckyTrade));
        double lucky = BalanceManger.getInstance().getLucky();
        double luckyDou;
        if (luckyTrade != null) {
            if (lucky_mul <= lucky) {
                luckyDou = Double.parseDouble(TradeUtil.getNumberFormat(lucky_mul, 2));
            } else {
                luckyDou = 0.00;
            }
        } else {
            luckyDou = 0.00;
        }
        // Log.d("print", "deductionResult:红包:  " + lucky);
      /*  if (Double.parseDouble(service) <= lucky) {
            luckyDou = Double.parseDouble(TradeUtil.numberHalfUp(Double.parseDouble(service), 2));
        } else {
            luckyDou = 0.00;
        }*/
        String deduction = TradeUtil.getNumberFormat(TradeUtil.add(prizeDou, luckyDou), 2);
        return deduction;
    }


    public static void setNetIncome(String tradeType, List<PositionEntity.DataBean> positionList, List<String> quoteList) {
        Log.d("hold", "setNetIncome:204:  " + positionList);
        if (positionList == null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder append = stringBuilder.append(tradeType).append(",").append(0.0)
                    .append(",").append(0.0);
            //总净值=可用余额-冻结资金+总净盈亏+其他钱包换算成USDT额
            //账户净值=可用余额+占用保证金+浮动盈亏
            Log.d("hold", "setNetIncome:发送的数据 210:  " + append.toString());
            NetIncomeManger.getInstance().postNetIncome(append.toString());
        } else if (positionList.size() == 0) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder append = stringBuilder.append(tradeType).append(",").append(0.0)
                    .append(",").append(0.0);
            //总净值=可用余额-冻结资金+总净盈亏+其他钱包换算成USDT额
            //账户净值=可用余额+占用保证金+浮动盈亏
            Log.d("hold", "setNetIncome:发送的数据 219:  " + append.toString());
            NetIncomeManger.getInstance().postNetIncome(append.toString());
        } else {
            TradeUtil.getNetIncome(quoteList, positionList, response1 -> TradeUtil.getMargin(positionList, response2 -> {
                double margin;
                double income;
                //    Log.d("print", "setNetIncome: 207: "+positionList+"    "+response1+"    "+response2);
                if (positionList == null) {
                    margin = 0.0;
                    income = 0.0;
                } else {
                    margin = Double.parseDouble(response2.toString());
                    income = Double.parseDouble(response1.toString());
                }
                StringBuilder stringBuilder = new StringBuilder();
                StringBuilder append = stringBuilder.append(tradeType).append(",").append(income)
                        .append(",").append(margin);
                //总净值=可用余额-冻结资金+总净盈亏+其他钱包换算成USDT额
                //账户净值=可用余额+占用保证金+浮动盈亏


                Log.d("hold", "setNetIncome:发送的数据 220:  " + append.toString());
                NetIncomeManger.getInstance().postNetIncome(append.toString());
            }));
        }


    }


    public static void getScale(String name, OnResult onResult) {
        FundItemEntity fundItemEntity = SPUtils.getData(AppConfig.SCALE, FundItemEntity.class);
        if (fundItemEntity != null) {
            List<FundItemEntity.DataBean> data = fundItemEntity.getData();
            for (FundItemEntity.DataBean dataBean : data) {
                if (name.equals(dataBean.getName())) {
                    onResult.setResult(dataBean.getScale());
                }
            }
        } else {
            onResult.setResult(8);

        }
    }

    public static void getFee(String withdrawChain, String chain, OnResult onResult) {
        String[] split = withdrawChain.split(",");
        for (String chains : split) {
            if (chains.startsWith(chain)) {
                String[] split1 = chains.split("-");
                onResult.setResult(split1[1]);
            }
        }
    }


    public static void addMyself(DecimalEditText editText, double priceChange) {
        if (editText.getText().toString().length() == 0) {
            String s = TradeUtil.addBig(0, priceChange);
            BigDecimal bigDecimal = new BigDecimal(s);
            editText.setText(bigDecimal.stripTrailingZeros().toPlainString());
        } else {
            String a = TradeUtil.addBig(Double.parseDouble(editText.getText().toString()), priceChange);
            BigDecimal bigDecimal = new BigDecimal(a);
            editText.setText(bigDecimal.stripTrailingZeros().toPlainString());

        }
    }

    public static void subMyself(DecimalEditText editText, double priceChange) {
        if (editText.getText().toString().length() == 0 || Double.parseDouble(editText.getText().toString()) <= 0) {
            editText.setText(String.valueOf(0));
        } else {
            String a = TradeUtil.subBig(Double.parseDouble(editText.getText().toString()), priceChange);
            BigDecimal bigDecimal = new BigDecimal(a);
            editText.setText(bigDecimal.stripTrailingZeros().toPlainString());
        }
    }

    /*交易金额*/
    public static void setTradeAmount(DecimalEditText edit_price_limit, DecimalEditText edit_amount_limit, DecimalEditText edit_trade_amount_limit, int priceDigit) {
        String price_limit = edit_price_limit.getText().toString();
        String amount_limit = edit_amount_limit.getText().toString();
        if (amount_limit.length() != 0) {
            String amount_trade_limit = TradeUtil.mulBig(Double.parseDouble(price_limit), Double.parseDouble(amount_limit));
            String numberFormat = TradeUtil.numberHalfUp(Double.parseDouble(amount_trade_limit), priceDigit);

            edit_trade_amount_limit.setText(numberFormat);
        }
    }

    /*输入框的监听*/
    public static void setEditTrade(DecimalEditText edit_price_limit, DecimalEditText edit_amount_limit, DecimalEditText edit_trade_amount_limit, int priceDigit) {
        edit_price_limit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    TradeUtil.setTradeAmount(edit_price_limit, edit_amount_limit, edit_trade_amount_limit, priceDigit);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    public static void setEditVolume(DecimalEditText edit_trade_amount_limit, DecimalEditText edit_price_limit, DecimalEditText edit_amount_limit, int volumeDigit) {
        /*成交金额的监听*/
        edit_trade_amount_limit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value_price_limit = edit_price_limit.getText().toString();
                if (s.length() != 0) {
                    if (value_price_limit.length() != 0) {
                        String value_amount = TradeUtil.divBig(Double.parseDouble(edit_trade_amount_limit.getText().toString()), Double.parseDouble(value_price_limit), volumeDigit);
                        edit_amount_limit.setText(value_amount);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public static void getBalance(List<BalanceEntity.DataBean> dataBean, String moneyType, OnResult onResult) {
        for (BalanceEntity.DataBean data : dataBean) {
            if (data.getCurrency().equals(moneyType)) {
                onResult.setResult(data.getMoney());
            }
        }
    }


    public static void addLeverMyself(DecimalEditText editText, double priceChange) {
        if (editText.getText().toString().length() == 0) {
            String s = TradeUtil.addBig(0, priceChange);
            BigDecimal bigDecimal = new BigDecimal(s);
            editText.setText(bigDecimal.stripTrailingZeros().toPlainString());
        } else {
            String a = TradeUtil.addBig(Double.parseDouble(editText.getText().toString()), priceChange);
            BigDecimal bigDecimal = new BigDecimal(a);
            editText.setText(bigDecimal.stripTrailingZeros().toPlainString());

        }
    }

    public static void subLeverMyself(DecimalEditText editText, double priceChange) {
        if (editText.getText().toString().length() == 0 || Double.parseDouble(editText.getText().toString()) <= 0) {
            editText.setText(String.valueOf(0));
        } else {
            String a = TradeUtil.subBig(Double.parseDouble(editText.getText().toString()), priceChange);
            BigDecimal bigDecimal = new BigDecimal(a);
            editText.setText(bigDecimal.stripTrailingZeros().toPlainString());
        }
    }
}
