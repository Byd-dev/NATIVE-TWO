package com.pro.bityard.manger;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.entity.QuoteChartEntity;
import com.pro.switchlibrary.SPUtils;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class Quote3MinCurrentManger extends Observable {


    private static Quote3MinCurrentManger quote3MinCurrentManger;

    private String code;


    public static Quote3MinCurrentManger getInstance() {
        if (quote3MinCurrentManger == null) {
            synchronized (Quote3MinCurrentManger.class) {
                if (quote3MinCurrentManger == null) {
                    quote3MinCurrentManger = new Quote3MinCurrentManger();
                }
            }

        }
        return quote3MinCurrentManger;

    }

    private Timer mTimer;

    public void startScheduleJob(long delay, long interval, String code) {
        this.code = code;
        if (mTimer != null) cancelTimer();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.sendEmptyMessage(0);
                }
            }
        }, delay, interval);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String quote_host = SPUtils.getString(AppConfig.QUOTE_HOST, null);
            if (quote_host == null) {
                NetManger.getInstance().initQuote();
                return;
            } else {
                quote(quote_host, code);
            }
        }
    };


    public void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }


    public void quote(String quote_host, String quote_code) {

        if (quote_host == null && quote_code == null) {
            NetManger.getInstance().initQuote();
        } else {
            NetManger.getInstance().getQuoteChart(quote_host, "/quota.jsp", quote_code, "3", (state, response) -> {
                if (state.equals(BUSY)) {

                } else if (state.equals(SUCCESS)) {

                    QuoteChartEntity quoteChartEntity = new Gson().fromJson(response.toString(), QuoteChartEntity.class);
                    if (quoteChartEntity.getS().equals("ok")) {

                        postQuote(quoteChartEntity);
                    }

                } else if (state.equals(FAILURE)) {
                }
            });

        }

    }

    public void postQuote(QuoteChartEntity data) {
        setChanged();
        notifyObservers(data);

    }

    /**
     * 清理消息监听
     */
    public void clear() {
        deleteObservers();
        quote3MinCurrentManger = null;
    }


}