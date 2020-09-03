package com.pro.bityard.manger;

import com.pro.bityard.entity.QuoteMinEntity;

import java.util.Observable;

public class QuoteCurrentManger extends Observable {


    private static QuoteCurrentManger quoteCurrentManger;



    public static QuoteCurrentManger getInstance() {
        if (quoteCurrentManger == null) {
            synchronized (QuoteCurrentManger.class) {
                if (quoteCurrentManger == null) {
                    quoteCurrentManger = new QuoteCurrentManger();
                }
            }

        }
        return quoteCurrentManger;

    }



    public void postQuote(QuoteMinEntity data) {
        setChanged();
        notifyObservers(data);

    }

    /**
     * 清理消息监听
     */
    public void clear() {
        deleteObservers();
        quoteCurrentManger = null;
    }


}
