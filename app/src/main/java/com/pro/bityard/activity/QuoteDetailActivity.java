package com.pro.bityard.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pro.bityard.R;
import com.pro.bityard.adapter.QuotePopAdapter;
import com.pro.bityard.adapter.RadioGroupAdapter;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.api.OnNetResult;
import com.pro.bityard.base.BaseActivity;
import com.pro.bityard.entity.ChargeUnitEntity;
import com.pro.bityard.entity.TradeListEntity;
import com.pro.bityard.manger.BalanceManger;
import com.pro.bityard.manger.ChargeUnitManger;
import com.pro.bityard.manger.QuoteItemManger;
import com.pro.bityard.manger.QuoteListManger;
import com.pro.bityard.manger.TradeListManger;
import com.pro.bityard.utils.TradeUtil;
import com.pro.bityard.utils.Util;
import com.pro.bityard.viewutil.StatusBarUtil;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

import static com.pro.bityard.api.NetManger.SUCCESS;
import static com.pro.bityard.config.AppConfig.ITEM_QUOTE_SECOND;
import static com.pro.bityard.utils.TradeUtil.ProfitAmount;
import static com.pro.bityard.utils.TradeUtil.itemQuoteCode;
import static com.pro.bityard.utils.TradeUtil.itemQuoteContCode;
import static com.pro.bityard.utils.TradeUtil.itemQuoteIsRange;
import static com.pro.bityard.utils.TradeUtil.itemQuoteMaxPrice;
import static com.pro.bityard.utils.TradeUtil.itemQuoteMinPrice;
import static com.pro.bityard.utils.TradeUtil.itemQuotePrice;
import static com.pro.bityard.utils.TradeUtil.itemQuoteTodayPrice;
import static com.pro.bityard.utils.TradeUtil.itemQuoteVolume;
import static com.pro.bityard.utils.TradeUtil.listQuoteIsRange;
import static com.pro.bityard.utils.TradeUtil.listQuoteName;
import static com.pro.bityard.utils.TradeUtil.listQuotePrice;
import static com.pro.bityard.utils.TradeUtil.listQuoteTodayPrice;
import static com.pro.bityard.utils.TradeUtil.listQuoteUSD;
import static com.pro.bityard.utils.TradeUtil.lossAmount;
import static com.pro.bityard.utils.TradeUtil.lossRate;
import static com.pro.bityard.utils.TradeUtil.profitRate;

public class QuoteDetailActivity extends BaseActivity implements View.OnClickListener, Observer, RadioGroup.OnCheckedChangeListener {
    private static final String TYPE = "tradeType";
    private static final String VALUE = "value";
    private static final String quoteType = "1";
    private int lever;

    @BindView(R.id.layout_bar)
    RelativeLayout layout_bar;
    private String tradeType = "1";
    private String orderType = "0";

    private String itemData;

    @BindView(R.id.text_name)
    TextView text_name;

    @BindView(R.id.text_usdt)
    TextView text_name_usdt;

    @BindView(R.id.text_switch)
    TextView text_switch;

    @BindView(R.id.img_right)
    ImageView img_right;

    @BindView(R.id.text_lastPrice)
    TextView text_lastPrice;

    @BindView(R.id.img_up_down)
    ImageView img_up_down;

    @BindView(R.id.text_change)
    TextView text_change;

    @BindView(R.id.text_range)
    TextView text_range;

    @BindView(R.id.text_max)
    TextView text_max;
    @BindView(R.id.text_min)
    TextView text_min;
    @BindView(R.id.text_volume)
    TextView text_volume;

    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.radio_0)
    RadioButton radio_btn0;
    @BindView(R.id.radio_1)
    RadioButton radio_btn1;

    @BindView(R.id.text_buy_much)
    TextView text_buy_much;
    @BindView(R.id.text_buy_empty)
    TextView text_buy_empty;

    @BindView(R.id.layout_market_price)
    LinearLayout layout_market_price;
    @BindView(R.id.layout_limit_price)
    LinearLayout layout_limit_price;

    @BindView(R.id.text_market_price)
    TextView text_market_price;
    @BindView(R.id.text_market_balance)
    TextView text_market_balance;
    @BindView(R.id.text_limit_balance)
    TextView text_limit_balance;

    @BindView(R.id.edit_market_margin)
    EditText edit_market_margin;

    @BindView(R.id.edit_limit_margin)
    EditText edit_limit_margin;


    @BindView(R.id.recyclerView_market)
    RecyclerView recyclerView_market;

    @BindView(R.id.recyclerView_limit)
    RecyclerView recyclerView_limit;

    @BindView(R.id.text_market_volume)
    TextView text_market_volume;

    @BindView(R.id.text_limit_volume)
    TextView text_limit_volume;

    @BindView(R.id.edit_limit_price)
    EditText edit_limit_price;

    private RadioGroupAdapter radioGroupAdapter;

    private List<String> quoteList;

    private QuotePopAdapter quotePopAdapter;
    private PopupWindow popupWindow;
    private String quote;
    private List<TradeListEntity> tradeListEntityList;
    private List<ChargeUnitEntity> chargeUnitEntityList;


    public static void enter(Context context, String tradeType, String data) {
        Intent intent = new Intent(context, QuoteDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VALUE, data);
        bundle.putString(TYPE, tradeType);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBarDarkTheme(this, false);

    }

    @Override
    protected int setContentLayout() {
        return R.layout.activity_quote_detail;
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView(View view) {

        QuoteListManger.getInstance().addObserver(this);

        QuoteItemManger.getInstance().addObserver(this);


        BalanceManger.getInstance().getBalance("USDT");

        TradeListManger.getInstance().addObserver(this);

        ChargeUnitManger.getInstance().addObserver(this);


        findViewById(R.id.img_back).setOnClickListener(this);
        findViewById(R.id.img_setting).setOnClickListener(this);

        findViewById(R.id.layout_more).setOnClickListener(this);

        radioGroup.setOnCheckedChangeListener(this);
        findViewById(R.id.layout_much).setOnClickListener(this);
        findViewById(R.id.layout_empty).setOnClickListener(this);

        radioGroupAdapter = new RadioGroupAdapter(this);
        recyclerView_market.setAdapter(radioGroupAdapter);
        recyclerView_limit.setAdapter(radioGroupAdapter);


    }

    @Override
    protected void initData() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        tradeType = bundle.getString(TYPE);
        itemData = bundle.getString(VALUE);

        //开启单个刷新
        QuoteItemManger.getInstance().startScheduleJob(ITEM_QUOTE_SECOND, ITEM_QUOTE_SECOND, TradeUtil.itemQuoteContCode(itemData));
        //合约号
        tradeListEntityList = TradeListManger.getInstance().getTradeListEntityList();
        //手续费
        chargeUnitEntityList = ChargeUnitManger.getInstance().getChargeUnitEntityList();
        if (tradeListEntityList == null) {
            startHandler(handler, 0, ITEM_QUOTE_SECOND, ITEM_QUOTE_SECOND);
        } else {
            TradeListEntity tradeListEntity = (TradeListEntity) TradeUtil.tradeDetail(itemQuoteContCode(itemData), tradeListEntityList);
            Log.d("print", "initData:258:合约号:  " + tradeListEntity);
            setContent(tradeListEntity);
        }

        if (chargeUnitEntityList == null) {
            startHandler(handler, 1, ITEM_QUOTE_SECOND, ITEM_QUOTE_SECOND);
        } else {
            ChargeUnitEntity chargeUnitEntity = (ChargeUnitEntity) TradeUtil.chargeDetail(itemQuoteCode(itemData), chargeUnitEntityList);
            Log.d("print", "initData:259:手续费: " + chargeUnitEntity);
        }


        String[] split1 = Util.quoteList(itemQuoteContCode(itemData)).split(",");
        text_name.setText(split1[0]);
        text_name_usdt.setText(split1[1]);
        text_lastPrice.setText(listQuotePrice(itemData));
        text_market_price.setText(listQuotePrice(itemData));
        text_change.setText(TradeUtil.quoteChange(listQuotePrice(itemData), listQuoteTodayPrice(itemData)));
        text_range.setText(TradeUtil.quoteRange(listQuotePrice(itemData), listQuoteTodayPrice(itemData)));


        if (listQuoteIsRange(itemData).equals("-1")) {
            text_lastPrice.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_red));
            text_change.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_red));
            text_range.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_red));

            img_up_down.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_down));

        } else if (listQuoteIsRange(itemData).equals("1")) {
            text_lastPrice.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_green));
            text_change.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_green));
            text_range.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_green));

            img_up_down.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_up));

        } else if (listQuoteIsRange(itemData).equals("0")) {

            text_lastPrice.setTextColor(getApplicationContext().getResources().getColor(R.color.text_maincolor));
            text_change.setTextColor(getApplicationContext().getResources().getColor(R.color.text_maincolor));
            text_range.setTextColor(getApplicationContext().getResources().getColor(R.color.text_maincolor));

        }


        //可用余额
        if (tradeType.equals("1")) {
            text_market_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceReal(), 2));
            text_limit_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceReal(), 2));
        } else {
            text_market_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceSim(), 2));
            text_limit_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceSim(), 2));
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    TradeListManger.getInstance().tradeList(new OnNetResult() {
                        @Override
                        public void onNetResult(String state, Object response) {
                            if (state.equals(SUCCESS)) {
                                cancelTimer();
                            }
                        }
                    });
                    break;
                case 1:
                    ChargeUnitManger.getInstance().chargeUnit(new OnNetResult() {
                        @Override
                        public void onNetResult(String state, Object response) {
                            if (state.equals(SUCCESS)) {
                                cancelTimer();
                            }
                        }
                    });
                    break;
            }


        }
    };


    /*设置 保证金和杠杆*/
    public void setContent(TradeListEntity tradeListEntity) {
        if (tradeListEntity != null) {
            edit_market_margin.setHint(TradeUtil.deposit(tradeListEntity.getDepositList()));
            edit_limit_margin.setHint(TradeUtil.deposit(tradeListEntity.getDepositList()));
            List<Integer> leverShowList = tradeListEntity.getLeverShowList();
            recyclerView_market.setLayoutManager(new GridLayoutManager(this, leverShowList.size()));
            recyclerView_limit.setLayoutManager(new GridLayoutManager(this, leverShowList.size()));

            radioGroupAdapter.setDatas(leverShowList);
            radioGroupAdapter.select(0);
            lever = leverShowList.get(0);

            radioGroupAdapter.setOnItemClick(new RadioGroupAdapter.OnItemClick() {
                @Override
                public void onSuccessListener(Integer position, Integer data) {
                    lever = data;
                    radioGroupAdapter.select(position);
                    recyclerView_market.setAdapter(radioGroupAdapter);
                    recyclerView_limit.setAdapter(radioGroupAdapter);
                    radioGroupAdapter.notifyDataSetChanged();


                }
            });
        }
    }

    @Override
    protected void initEvent() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.layout_more:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    img_right.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_right));
                    backgroundAlpha(1f);
                } else {
                    showMoreWindow(quoteList);
                    img_right.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_open));
                }
                break;

            case R.id.img_setting:

                break;

            case R.id.layout_much:
                Log.d("print", "onCheckedChanged:543:  " + lever);

                // TODO: 2020/4/3   开仓
                TradeListEntity tradeListEntity = (TradeListEntity) TradeUtil.tradeDetail(itemQuoteContCode(quote), tradeListEntityList);
                double margin = Double.parseDouble(edit_market_margin.getText().toString());
                String s = text_buy_much.getText().toString();
                double price = Double.parseDouble(s);
                int priceDigit = tradeListEntity.getPriceDigit();
                String stopProfitPrice = TradeUtil.StopProfitPrice(true, price, priceDigit, lever, margin, TradeUtil.mul(margin, tradeListEntity.getStopProfitList().get(1)));
                String profitAmount = ProfitAmount(true, price, priceDigit, lever, margin, Double.parseDouble(stopProfitPrice));
                String profitRate = profitRate(Double.parseDouble(profitAmount), margin);

                String stopLossPrice = TradeUtil.StopLossPrice(true, price, priceDigit, lever, margin, TradeUtil.mul(margin, tradeListEntity.getStopProfitList().get(0)));
                String lossAmount = lossAmount(true, price, priceDigit, lever, margin, Double.parseDouble(stopLossPrice));
                String lossRate = lossRate(Double.parseDouble(lossAmount), margin);

                String priceOrder;
                if (orderType.equals("0")) {
                    priceOrder = String.valueOf(price);
                } else {
                    priceOrder = edit_limit_price.getText().toString();
                }

               /* NetManger.getInstance().order(tradeType, "2", tradeListEntity.getContractCode(),
                        tradeListEntity.getCode(), "true", edit_market_margin.getText().toString(), String.valueOf(lever),
                        priceOrder, String.valueOf(tradeListEntity.isDefer()),
                        String.valueOf(tradeListEntity.getDeferFee()), profitRate, lossRate,

                        );*/
                break;
            case R.id.layout_empty:

                break;

        }
    }


    private void showMoreWindow(List<String> quoteList) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_more_layout, null);
        popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_pop);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        quotePopAdapter = new QuotePopAdapter(this);
        if (quoteList != null) {
            quotePopAdapter.setDatas(quoteList);
        }
        if (quote != null) {
            quotePopAdapter.select(itemQuoteContCode(quote));

        }


        recyclerView.setAdapter(quotePopAdapter);
        quotePopAdapter.setOnItemClick(new QuotePopAdapter.OnItemClick() {
            @Override
            public void onSuccessListener(String data) {
                text_name.setText(listQuoteName(data));
                text_name_usdt.setText(listQuoteUSD(data));
                QuoteItemManger.getInstance().startScheduleJob(ITEM_QUOTE_SECOND, ITEM_QUOTE_SECOND, itemQuoteContCode(data));
                //相应选择
                quotePopAdapter.select(itemQuoteContCode(data));
                backgroundAlpha(1f);
                img_right.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_right));
                popupWindow.dismiss();


                TradeListEntity tradeListEntity = (TradeListEntity) TradeUtil.tradeDetail(itemQuoteContCode(data), tradeListEntityList);

                setContent(tradeListEntity);


            }
        });


        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.6f;
        getWindow().setAttributes(params);

        Button btn_switch = view.findViewById(R.id.btn_switch);

        if (tradeType.equals("1")) {
            btn_switch.setText(getResources().getText(R.string.text_simulation_btn));

        } else if (tradeType.equals("2")) {
            btn_switch.setText(getResources().getText(R.string.text_real_btn));

        }

        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tradeType.equals("1")) {

                    tradeType = "2";
                    text_switch.setText(getResources().getText(R.string.text_simulation_trade));
                    radio_btn1.setVisibility(View.GONE);


                } else if (tradeType.equals("2")) {

                    tradeType = "1";
                    text_switch.setText(getResources().getText(R.string.text_real_trade));
                    radio_btn1.setVisibility(View.VISIBLE);


                }
                radio_btn0.setChecked(true);
                if (radio_btn0.isChecked()) {
                    layout_market_price.setVisibility(View.VISIBLE);
                    layout_limit_price.setVisibility(View.GONE);


                }
                //可用余额
                if (tradeType.equals("1")) {
                    text_market_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceReal(), 2));
                    text_limit_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceReal(), 2));
                } else {
                    text_market_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceSim(), 2));
                    text_limit_balance.setText(TradeUtil.getNumberFormat(BalanceManger.getInstance().getBalanceSim(), 2));
                }

                backgroundAlpha(1f);
                popupWindow.dismiss();
                img_right.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_right));


            }
        });

        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.pop_anim_quote);
        popupWindow.setContentView(view);
        popupWindow.showAsDropDown(layout_bar);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == QuoteListManger.getInstance()) {
            ArrayMap<String, List<String>> arrayMap = (ArrayMap<String, List<String>>) arg;
            quoteList = arrayMap.get(quoteType);
            if (quotePopAdapter != null && quoteList != null) {
                quotePopAdapter.setDatas(quoteList);
            }

        } else if (o == QuoteItemManger.getInstance()) {
            quote = (String) arg;


            if (quote != null) {
                //仓位实时更新
                if (edit_market_margin.getText().length() != 0) {
                    text_market_volume.setText(TradeUtil.volume(lever, Double.parseDouble(edit_market_margin.getText().toString()), Double.parseDouble(itemQuotePrice(quote))));
                }
                if (edit_limit_margin.getText().length() != 0) {
                    text_limit_volume.setText(TradeUtil.volume(lever, Double.parseDouble(edit_limit_margin.getText().toString()), Double.parseDouble(itemQuotePrice(quote))));
                }


                if (quotePopAdapter != null) {
                    quotePopAdapter.select(itemQuoteContCode(quote));
                }


                text_lastPrice.setText(itemQuotePrice(quote));
                text_market_price.setText(itemQuotePrice(quote));
                text_change.setText(TradeUtil.quoteChange(itemQuotePrice(quote), itemQuoteTodayPrice(quote)));
                text_range.setText(TradeUtil.quoteRange(itemQuotePrice(quote), itemQuoteTodayPrice(quote)));

                if (itemQuoteIsRange(quote).equals("-1")) {
                    text_lastPrice.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_red));
                    text_change.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_red));
                    text_range.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_red));

                    img_up_down.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_down));

                } else if (itemQuoteIsRange(quote).equals("1")) {
                    text_lastPrice.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_green));
                    text_change.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_green));
                    text_range.setTextColor(getApplicationContext().getResources().getColor(R.color.text_quote_green));
                    img_up_down.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.icon_market_up));

                } else if (itemQuoteIsRange(quote).equals("0")) {

                    text_lastPrice.setTextColor(getApplicationContext().getResources().getColor(R.color.text_maincolor));
                    text_change.setTextColor(getApplicationContext().getResources().getColor(R.color.text_maincolor));
                    text_range.setTextColor(getApplicationContext().getResources().getColor(R.color.text_maincolor));

                }

                text_max.setText(itemQuoteMaxPrice(quote));
                text_min.setText(itemQuoteMinPrice(quote));
                text_volume.setText(itemQuoteVolume(quote));


                String spread = TradeUtil.spread(itemQuoteContCode(quote), tradeListEntityList);

                if (spread != null) {
                    text_buy_much.setText(TradeUtil.itemQuoteBuyMuchPrice(quote, Integer.valueOf(spread)));
                    text_buy_empty.setText(TradeUtil.itemQuoteBuyEmptyPrice(quote, Integer.valueOf(spread)));
                }

            }

        } else if (o == TradeListManger.getInstance()) {
            tradeListEntityList = (List<TradeListEntity>) arg;
            TradeListEntity tradeListEntity = (TradeListEntity) TradeUtil.tradeDetail(itemQuoteContCode(itemData), tradeListEntityList);
            Log.d("print", "initData:510:  " + tradeListEntity);
            setContent(tradeListEntity);
        } else if (o == ChargeUnitManger.getInstance()) {
            chargeUnitEntityList = (List<ChargeUnitEntity>) arg;
            ChargeUnitEntity chargeUnitEntity = (ChargeUnitEntity) TradeUtil.chargeDetail(itemQuoteCode(itemData), chargeUnitEntityList);
            Log.d("print", "update:596: " + chargeUnitEntity);
        }
    }

    public void backgroundAlpha(float bgalpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgalpha;
        getWindow().setAttributes(lp);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        QuoteItemManger.getInstance().clear();
        QuoteItemManger.getInstance().cancelTimer();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_0:
                layout_market_price.setVisibility(View.VISIBLE);
                layout_limit_price.setVisibility(View.GONE);
                text_buy_much.setVisibility(View.VISIBLE);
                text_buy_empty.setVisibility(View.VISIBLE);
                orderType = "0";

                break;
            case R.id.radio_1:
                layout_market_price.setVisibility(View.GONE);
                layout_limit_price.setVisibility(View.VISIBLE);
                text_buy_much.setVisibility(View.GONE);
                text_buy_empty.setVisibility(View.GONE);
                orderType = "1";
                break;


        }
    }
}
