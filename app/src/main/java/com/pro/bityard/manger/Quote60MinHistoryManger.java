package com.pro.bityard.manger;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.entity.QuoteChartEntity;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class Quote60MinHistoryManger extends Observable {


    private static Quote60MinHistoryManger quoteHistoryManger;

    private String code;
    private int count;


    public static Quote60MinHistoryManger getInstance() {
        if (quoteHistoryManger == null) {
            synchronized (Quote60MinHistoryManger.class) {
                if (quoteHistoryManger == null) {
                    quoteHistoryManger = new Quote60MinHistoryManger();
                }
            }

        }
        return quoteHistoryManger;

    }

    private Timer mTimer;

    public void startScheduleJob(long delay, long interval, String code, int count) {
        this.code = code;
        this.count = count;
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
            quote(code, count);

        }
    };


    public void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    int getCount=0;
    public void quote(String quote_code, int count) {



        NetManger.getInstance().getQuoteHistory(count, "/api/tv/tradingView/history", quote_code, "60", (state, response) -> {
            if (state.equals(BUSY)) {

            } else if (state.equals(SUCCESS)) {

                QuoteChartEntity quoteChartEntity = new Gson().fromJson(response.toString(), QuoteChartEntity.class);
                if (quoteChartEntity.getS().equals("ok")) {
                    Log.d("print", "quote:88 "+"QuoteHistory 60Min:"+getCount++);

                    postQuote(quoteChartEntity);
                    cancelTimer();

                }

            } else if (state.equals(FAILURE)) {
            }
        });


    }

    public void postQuote(QuoteChartEntity data) {
        setChanged();
        notifyObservers(data);

    }

    /**
     * ??????????????????
     */
    public void clear() {
        deleteObservers();
        quoteHistoryManger = null;
    }


}
