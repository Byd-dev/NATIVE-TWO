package com.pro.bityard.fragment.trade;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.pro.bityard.R;
import com.pro.bityard.activity.LoginActivity;
import com.pro.bityard.activity.SpotTradeActivity;
import com.pro.bityard.activity.UserActivity;
import com.pro.bityard.adapter.OptionalSelectAdapter;
import com.pro.bityard.adapter.ProportionSelectAdapter;
import com.pro.bityard.adapter.QuoteAdapter;
import com.pro.bityard.adapter.SellListAdapter;
import com.pro.bityard.adapter.SpotPositionAdapter;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.base.AppContext;
import com.pro.bityard.base.BaseFragment;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.config.IntentConfig;
import com.pro.bityard.entity.BalanceEntity;
import com.pro.bityard.entity.BuySellEntity;
import com.pro.bityard.entity.LoginEntity;
import com.pro.bityard.entity.QuoteMinEntity;
import com.pro.bityard.entity.SpotPositionEntity;
import com.pro.bityard.entity.TipEntity;
import com.pro.bityard.entity.TradeListEntity;
import com.pro.bityard.manger.BalanceManger;
import com.pro.bityard.manger.QuoteCodeManger;
import com.pro.bityard.manger.QuoteSpotCurrentManger;
import com.pro.bityard.manger.QuoteSpotManger;
import com.pro.bityard.manger.SocketQuoteManger;
import com.pro.bityard.manger.SpotCodeManger;
import com.pro.bityard.manger.WebSocketManager;
import com.pro.bityard.utils.SocketUtil;
import com.pro.bityard.utils.TradeUtil;
import com.pro.bityard.utils.Util;
import com.pro.bityard.view.DecimalEditText;
import com.pro.bityard.view.HeaderRecyclerView;
import com.pro.switchlibrary.SPUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;
import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;
import static com.pro.bityard.utils.TradeUtil.itemQuoteContCode;

public class SpotTradeFragment extends BaseFragment implements View.OnClickListener, Observer {
    private static final String TYPE = "tradeType";
    private static final String VALUE = "value";
    @BindView(R.id.layout_spot)
    LinearLayout layout_view;
    @BindView(R.id.text_name)
    TextView text_name;
    @BindView(R.id.text_currency)
    TextView text_currency;
    @BindView(R.id.layout_null)
    LinearLayout layout_null;
    private String tradeType = "1";//??????=1 ??????=2
    private String itemData;
    private String quote_code = null, quote_code_old = null;
    @BindView(R.id.img_star_spot)
    ImageView img_star_spot;
    private Set<String> optionalList;//????????????

    @BindView(R.id.recyclerView_spot)
    HeaderRecyclerView recyclerView_spot;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;


    /*????????????id*/
    RelativeLayout layout_cancel;
    View view_line_two;
    private SpotPositionAdapter spotPositionAdapter;
    private ProportionSelectAdapter proportionLimitAdapter, proportionMarketAdapter;
    private List<Integer> proportionList;


    private SellListAdapter sellAdapter, buyAdapter;
    private RecyclerView recyclerView_sell;
    private RecyclerView recyclerView_buy;
    private List<BuySellEntity> buyList;
    private List<BuySellEntity> sellList;
    private String quote;
    private TextView text_price;
    private TextView text_currency_price;
    private String value_rate;
    private TextView text_scale;
    private RelativeLayout layout_switch_limit_price;
    private TextView text_limit_market;
    private TextView text_currency_head;
    private DecimalEditText edit_amount_limit;

    private String isBuy = "true";


    private LinearLayout layout_spot_limit;
    private LinearLayout layout_spot_market;
    private TextView text_balance;
    private DecimalEditText edit_price_limit;
    private double price;
    private TradeListEntity tradeDetail;
    private TextView text_add_price_limit, text_sub_price_limit, text_add_amount_limit, text_sub_amount_limit;
    private RadioButton radioButton_buy;
    private RadioButton radioButton_sell;
    private RadioGroup radioGroup;
    private TextView text_buy_what;
    private RelativeLayout layout_buy_what;

    private String tradeName = null;
    //????????????
    private DecimalEditText edit_trade_amount_limit, edit_trade_amount_market;
    private int volumeDigit;
    private TextWatcher watcher_price;
    private TextWatcher watcher_amount;
    private TextWatcher watcher_trade;
    private String value_price;
    private String srcCurrency;
    private String desCurrency;
    private BalanceEntity.DataBean balanceEntity;
    private String value_volume;
    private TextView text_add_amount_market;
    private TextView text_sub_amount_market;
    private int priceDigit;
    private Activity activity;
    private String volumeMin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.layout_spot_tab;
    }

    public SpotTradeFragment newInstance(String type, String value) {
        SpotTradeFragment fragment = new SpotTradeFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, type);
        args.putString(VALUE, value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onLazyLoad() {

    }

    @Override
    protected void initView(View view) {
        // showProgressDialog();
        BalanceManger.getInstance().addObserver(this);
        SocketQuoteManger.getInstance().addObserver(this);
        SpotCodeManger.getInstance().addObserver(this);

        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.head_spot_layout, null);
        //??????
        edit_price_limit = headView.findViewById(R.id.edit_price_limit);
        //????????????
        edit_amount_limit = headView.findViewById(R.id.edit_amount_limit);
        //??????????????????
        edit_trade_amount_limit = headView.findViewById(R.id.edit_trade_amount_limit);


        //??????????????????
        edit_trade_amount_market = headView.findViewById(R.id.edit_trade_amount_market);

        headView.findViewById(R.id.img_record).setOnClickListener(v -> {
            UserActivity.enter(getActivity(), IntentConfig.Keys.KEY_SPOT_RECORD);
        });


        text_add_price_limit = headView.findViewById(R.id.text_add_price_limit);
        text_sub_price_limit = headView.findViewById(R.id.text_sub_price_limit);
        text_add_amount_limit = headView.findViewById(R.id.text_add_amount_limit);
        text_sub_amount_limit = headView.findViewById(R.id.text_sub_amount_limit);
        view.findViewById(R.id.layout_product).setOnClickListener(this);

        radioGroup = headView.findViewById(R.id.radioGroup);

        radioButton_buy = headView.findViewById(R.id.radio_buy);
        radioButton_sell = headView.findViewById(R.id.radio_sell);

        text_balance = headView.findViewById(R.id.text_balance);

        text_buy_what = headView.findViewById(R.id.text_buy_what);
        layout_buy_what = headView.findViewById(R.id.layout_buy_what);

        layout_spot_limit = headView.findViewById(R.id.layout_spot_limit);
        layout_spot_market = headView.findViewById(R.id.layout_spot_market);
        layout_spot_market.setVisibility(View.GONE);


        text_currency_head = headView.findViewById(R.id.text_currency_head);
        /*??????*/
        RecyclerView recyclerView_proportion_limit = headView.findViewById(R.id.recyclerView_proportion_limit);

        proportionList = new ArrayList<>();
        proportionList.add(25);
        proportionList.add(50);
        proportionList.add(75);
        proportionList.add(100);
        proportionLimitAdapter = new ProportionSelectAdapter(getActivity());
        recyclerView_proportion_limit.setLayoutManager(new GridLayoutManager(getActivity(), proportionList.size()));
        recyclerView_proportion_limit.setAdapter(proportionLimitAdapter);






        /*??????*/
        RecyclerView recyclerView_proportion_market = headView.findViewById(R.id.recyclerView_proportion_market);
        proportionMarketAdapter = new ProportionSelectAdapter(getActivity());
        recyclerView_proportion_market.setLayoutManager(new GridLayoutManager(getActivity(), proportionList.size()));
        recyclerView_proportion_market.setAdapter(proportionMarketAdapter);


        view.findViewById(R.id.layout_product).setOnClickListener(this);


        //????????????
        view.findViewById(R.id.layout_optional).setOnClickListener(this);
        BalanceManger.getInstance().addObserver(this);
        QuoteSpotManger.getInstance().addObserver(this);
        QuoteSpotCurrentManger.getInstance().addObserver(this);//1min ??????


        spotPositionAdapter = new SpotPositionAdapter(getActivity());
        recyclerView_spot.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_spot.setAdapter(spotPositionAdapter);

        spotPositionAdapter.setOnDetailClick(data -> NetManger.getInstance().spotClose(data.getId(), (state, response) -> {
            if (state.equals(BUSY)) {
                showProgressDialog();
            } else if (state.equals(SUCCESS)) {
                dismissProgressDialog();
                TipEntity tipEntity = (TipEntity) response;
                if (tipEntity.getCode() == 200) {
                    Toast.makeText(getActivity(), getString(R.string.text_tip_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.text_failure), Toast.LENGTH_SHORT).show();

                }

                getPosition();
            } else if (state.equals(FAILURE)) {
                dismissProgressDialog();

            }
        }));

        recyclerView_spot.addHeaderView(headView);
        layout_cancel = headView.findViewById(R.id.layout_cancel);
        view_line_two = headView.findViewById(R.id.view_line_two);
        layout_switch_limit_price = headView.findViewById(R.id.layout_switch_limit_price);
        text_limit_market = headView.findViewById(R.id.text_limit_market);


        Util.colorSwipe(getActivity(), swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getPosition();
        });

        text_scale = headView.findViewById(R.id.text_scale);
        text_price = headView.findViewById(R.id.text_price);
        text_currency_price = headView.findViewById(R.id.text_currency_price);


        headView.findViewById(R.id.layout_buy_sell_switch).setOnClickListener(this);

        //????????????????????????
        text_add_amount_market = headView.findViewById(R.id.text_add_amount_market);
        text_sub_amount_market = headView.findViewById(R.id.text_sub_amount_market);

        layout_switch_limit_price.setOnClickListener(this);


        sellAdapter = new SellListAdapter(getActivity());
        recyclerView_sell = headView.findViewById(R.id.recyclerView_sell);
        recyclerView_sell.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_sell.setAdapter(sellAdapter);

        buyAdapter = new SellListAdapter(getActivity());
        recyclerView_buy = headView.findViewById(R.id.recyclerView_buy);
        recyclerView_buy.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_buy.setAdapter(buyAdapter);

        view.findViewById(R.id.img_market).setOnClickListener(this);


    }


    @Override
    protected void intPresenter() {

    }


    boolean flag_price = true;
    boolean flag_amount = true;
    boolean flag_trade = true;


    private void setContent(String itemData) {
        if (itemData == null) {
            return;
        }

        Log.d("print", "setContent:313:  " + itemData);
        tradeName = TradeUtil.nameWithoutUsd(itemData);
        quote_code = itemQuoteContCode(itemData);
        srcCurrency = "USDT";
        desCurrency = tradeName;


        String string = SPUtils.getString(AppConfig.QUOTE_DETAIL, null);
        List<TradeListEntity> tradeListEntityList = Util.SPDealEntityResult(string);
        tradeDetail = (TradeListEntity) TradeUtil.tradeDetail(itemQuoteContCode(itemData), tradeListEntityList);
        if (text_name == null) {
            return;
        }
        text_name.setText(TradeUtil.name(itemData));
        text_currency.setText(TradeUtil.currency(itemData));
        text_currency_head.setText("(" + tradeName + ")");
        edit_amount_limit.setHint(getResources().getString(R.string.text_amount_withdrawal) + " (" + tradeName + ")");
        //?????????????????????????????????????????????????????????
        //priceDigit = tradeDetail.getPriceDigit();

        if (isBuy.equals("true")) {
            text_buy_what.setText(getResources().getText(R.string.text_buy) + tradeName);
        } else {
            text_buy_what.setText(getResources().getText(R.string.text_sell) + tradeName);
        }


        getBalance();
    }

   /* private final Timer timer = new Timer();
    private TimerTask task;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getPosition();
            super.handleMessage(msg);
        }

    };*/


    @Override
    protected void initData() {
       /* task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };*/


        // timer.schedule(task, QUOTE_SECOND, QUOTE_SECOND);
        //startScheduleJob(mHandler, QUOTE_SECOND, QUOTE_SECOND);


        tradeType = getArguments().getString(TYPE);
        itemData = getArguments().getString(VALUE);
        Log.d("print", "initData:??????????????????:  " + itemQuoteContCode(itemData));
        setContent(itemData);
        edit_price_limit.setDecimalEndNumber(priceDigit);
        if (tradeDetail==null){
            volumeDigit = 0;
        }else {
            volumeMin = tradeDetail.getVolumeMin();
            if (volumeMin == null) {
                volumeDigit = 0;
            } else {
                volumeDigit = TradeUtil.decimalPoint(volumeMin);
            }
        }

        edit_amount_limit.setDecimalEndNumber(volumeDigit);
        edit_trade_amount_limit.setDecimalEndNumber(priceDigit);




        watcher_price = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edit_trade_amount_limit.removeTextChangedListener(watcher_trade);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    TradeUtil.setTradeAmount(edit_price_limit, edit_amount_limit, edit_trade_amount_limit, priceDigit);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                edit_trade_amount_limit.addTextChangedListener(watcher_trade);

            }
        };

        edit_price_limit.addTextChangedListener(watcher_price);


        watcher_amount = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edit_trade_amount_limit.removeTextChangedListener(watcher_trade);


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    TradeUtil.setTradeAmount(edit_amount_limit, edit_price_limit, edit_trade_amount_limit, priceDigit);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                edit_trade_amount_limit.addTextChangedListener(watcher_trade);

            }
        };
        edit_amount_limit.addTextChangedListener(watcher_amount);


        watcher_trade = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edit_amount_limit.removeTextChangedListener(watcher_amount);

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
                edit_amount_limit.addTextChangedListener(watcher_amount);
            }
        };

        edit_trade_amount_limit.addTextChangedListener(watcher_trade);


        //????????????
        text_add_price_limit.setOnClickListener(v ->
        {
            TradeUtil.addMyself(edit_price_limit, tradeDetail.getPriceChange());
            TradeUtil.setTradeAmount(edit_price_limit, edit_amount_limit, edit_trade_amount_limit, priceDigit);
        });
        //????????????
        text_sub_price_limit.setOnClickListener(v ->
        {
            TradeUtil.subMyself(edit_price_limit, tradeDetail.getPriceChange());
            TradeUtil.setTradeAmount(edit_price_limit, edit_amount_limit, edit_trade_amount_limit, priceDigit);
        });

        //??????????????????
        text_add_amount_limit.setOnClickListener(v ->
        {
            TradeUtil.addMyself(edit_amount_limit, Double.parseDouble(volumeMin));
            TradeUtil.setTradeAmount(edit_amount_limit, edit_price_limit, edit_trade_amount_limit, priceDigit);

        });
        //??????????????????
        text_sub_amount_limit.setOnClickListener(v ->

        {
            TradeUtil.subMyself(edit_amount_limit, Double.parseDouble(volumeMin));
            TradeUtil.setTradeAmount(edit_amount_limit, edit_price_limit, edit_trade_amount_limit, priceDigit);
        });


        optionalList = Util.SPDealResult(SPUtils.getString(AppConfig.KEY_OPTIONAL, null));

        Util.setOptional(
                getActivity(), optionalList, quote_code, img_star_spot, response ->
                {
                    if (!(boolean) response) {
                        optionalList = new HashSet<>();
                    }
                });

        getPosition();
        getBalance();

        value_rate = SPUtils.getString(AppConfig.USD_RATE, null);
        text_buy_what.setText(
                getResources().

                        getText(R.string.text_buy) + tradeName);
        radioGroup.setOnCheckedChangeListener((group, checkedId) ->
        {
            switch (checkedId) {
                case R.id.radio_buy:
                    count = 0;
                    radioButton_buy.setBackground(getActivity().getResources().getDrawable(R.mipmap.bg_spot_buy));
                    radioButton_sell.setBackground(getActivity().getResources().getDrawable(R.drawable.bg_color_left));
                    layout_buy_what.setBackground(getActivity().getResources().getDrawable(R.drawable.bg_shape_green));
                    text_buy_what.setText(getResources().getText(R.string.text_buy) + tradeName);
                    isBuy = "true";
                    text_balance.setText(TradeUtil.justDisplay(BalanceManger.getInstance().getBalanceReal()) + " " + getResources().getString(R.string.text_usdt));
                    srcCurrency = "USDT";
                    desCurrency = tradeName;
                    break;
                case R.id.radio_sell:
                    count = 0;
                    radioButton_buy.setBackground(getActivity().getResources().getDrawable(R.drawable.bg_color_left));
                    radioButton_sell.setBackground(getActivity().getResources().getDrawable(R.mipmap.bg_spot_sell));
                    layout_buy_what.setBackground(getActivity().getResources().getDrawable(R.drawable.bg_shape_red));
                    text_buy_what.setText(getResources().getText(R.string.text_sell) + tradeName);
                    isBuy = "false";
                    srcCurrency = tradeName;
                    desCurrency = "USDT";
                    if (balanceEntity != null) {
                        TradeUtil.getScale(balanceEntity.getCurrency(), response2 -> {
                            double money = balanceEntity.getMoney();
                            int scale = (int) response2;
                            text_balance.setText(TradeUtil.justDisplay(money) + " " + tradeName);
                        });
                    } else {
                        getBalance();
                    }

                    break;
            }
        });
        /*??????*/
        proportionLimitAdapter.setDatas(proportionList);
        proportionLimitAdapter.select(5);
        proportionLimitAdapter.setOnItemClick((position, data) -> {
            proportionLimitAdapter.select(position);
            if (isBuy.equals("true")) {
                double balanceReal = BalanceManger.getInstance().getBalanceReal();
                String s = TradeUtil.mulBig(balanceReal, Double.parseDouble(TradeUtil.divBig(data, 100, 2)));
                String numberFormat = TradeUtil.getNumberFormat(Double.parseDouble(s), priceDigit);
                edit_trade_amount_limit.setText(numberFormat);

            } else {
                if (balanceEntity != null) {
                    double money = balanceEntity.getMoney();
                    String s = TradeUtil.mulBig(money, Double.parseDouble(TradeUtil.divBig(data, 100, 2)));
                    String numberFormat = TradeUtil.numberHalfUp(Double.parseDouble(s), TradeUtil.decimalPoint(String.valueOf(money)));
                    edit_amount_limit.setText(numberFormat);
                } else {
                    getBalance();
                }
            }

        });

        /*??????*/

        //????????????????????????
        text_add_amount_market.setOnClickListener(v -> TradeUtil.addMyself(edit_trade_amount_market, tradeDetail.getPriceChange()));
        //????????????????????????
        text_sub_amount_market.setOnClickListener(v -> TradeUtil.subMyself(edit_trade_amount_market, tradeDetail.getPriceChange()));

        proportionMarketAdapter.setDatas(proportionList);
        proportionMarketAdapter.select(5);
        proportionMarketAdapter.setOnItemClick((position, data) -> {
            proportionMarketAdapter.select(position);
            if (isBuy.equals("true")) {
                double balanceReal = BalanceManger.getInstance().getBalanceReal();
                String s = TradeUtil.mulBig(balanceReal, Double.parseDouble(TradeUtil.divBig(data, 100, 2)));
                String numberFormat = TradeUtil.getNumberFormat(Double.parseDouble(s), priceDigit);
                edit_trade_amount_market.setText(numberFormat);
            } else {
                if (balanceEntity != null) {
                    double money = balanceEntity.getMoney();
                    String s = TradeUtil.mulBig(money, Double.parseDouble(TradeUtil.divBig(data, 100, 2)));
                    String numberFormat = TradeUtil.getNumberFormat(Double.parseDouble(s), priceDigit);
                    edit_trade_amount_market.setText(numberFormat);
                } else {
                    getBalance();
                }
            }

        });
        /*??????*/
        layout_buy_what.setOnClickListener(v -> {
            if (isLogin()) {

                if (limit_market_type.equals("0")) {
                    value_price = edit_price_limit.getText().toString();
                    value_volume = edit_amount_limit.getText().toString();
                    if (value_price.equals("")) {
                        return;
                    }

                } else {
                    value_price = "0";
                    value_volume = edit_trade_amount_market.getText().toString();

                }

                NetManger.getInstance().spotOpen(Util.filterNumber(quote_code), isBuy, limit_market_type, srcCurrency, desCurrency, value_price,
                        value_volume, "0", (state, response) -> {
                            if (state.equals(BUSY)) {
                                showProgressDialog();
                            } else if (state.equals(SUCCESS)) {
                                dismissProgressDialog();
                                Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                                getPosition();
                                getBalance();

                            } else if (state.equals(FAILURE)) {
                                dismissProgressDialog();
                            }
                        });
            } else {
                LoginActivity.enter(getActivity(), IntentConfig.Keys.KEY_LOGIN);

            }


        });

    }


    private void getBalance() {
        if (isLogin()) {
            BalanceManger.getInstance().getBalance(tradeName, response -> {
                balanceEntity = (BalanceEntity.DataBean) response;
                Log.d("print", "getBalance:??????: " + balanceEntity.getMoney());
                TradeUtil.getScale(balanceEntity.getCurrency(), response2 -> {
                    double money = balanceEntity.getMoney();
                    int scale = (int) response2;
                    priceDigit = TradeUtil.decimalPoint(String.valueOf(BalanceManger.getInstance().getBalanceReal()));
                    Log.d("print", "getBalance:694:  " + priceDigit);

                    if (isBuy.equals("true")) {
                        text_balance.setText(BalanceManger.getInstance().getBalanceReal() + " " + activity.getResources().getString(R.string.text_usdt));

                    } else {
                        text_balance.setText(TradeUtil.justDisplay(money) + " " + tradeName);
                    }
                });
            });
        } else {
            if (isBuy.equals("true")) {
                text_balance.setText("0.0" + " " + activity.getResources().getString(R.string.text_usdt));
            } else {
                text_balance.setText("0.0" + " " + tradeName);
            }
        }


    }

    private void getPosition() {
        LoginEntity loginEntity = SPUtils.getData(AppConfig.LOGIN, LoginEntity.class);
        if (loginEntity == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        NetManger.getInstance().userSpotPosition(loginEntity.getUser().getUserId(), (state, response) -> {
            if (state.equals(BUSY)) {
                swipeRefreshLayout.setRefreshing(true);
            } else if (state.equals(SUCCESS)) {
                if (isAdded()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                SpotPositionEntity spotPositionEntity = (SpotPositionEntity) response;
                if (spotPositionEntity.getData() == null) {
                    return;
                }
                if (spotPositionEntity.getData().size() == 0) {
                    layout_null.setVisibility(View.VISIBLE);
                    layout_cancel.setVisibility(View.GONE);
                    view_line_two.setVisibility(View.GONE);
                } else {
                    layout_null.setVisibility(View.GONE);
                    layout_cancel.setVisibility(View.VISIBLE);
                    view_line_two.setVisibility(View.VISIBLE);
                }
                Log.d("print", "getPosition:721: " + spotPositionEntity.getData());
                spotPositionAdapter.setDatas(spotPositionEntity.getData());
            } else if (state.equals(FAILURE)) {
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_product:
                Util.lightOff(getActivity());
                SocketUtil.switchQuotesList("3001");
                showQuotePopWindow();
                break;

            case R.id.layout_optional:
                optionalList = Util.SPDealResult(SPUtils.getString(AppConfig.KEY_OPTIONAL, null));
                Log.d("print", "onClick:????????????: " + optionalList);
                //??????????????????????????????
                if (optionalList.size() != 0) {
                    Util.isOptional(quote_code, optionalList, response -> {
                        boolean isOptional = (boolean) response;
                        if (isOptional) {
                            img_star_spot.setImageDrawable(getResources().getDrawable(R.mipmap.icon_star_normal));
                            optionalList.remove(quote_code);
                        } else {
                            img_star_spot.setImageDrawable(getResources().getDrawable(R.mipmap.icon_star));
                            optionalList.add(quote_code);
                        }
                    });
                } else {
                    img_star_spot.setImageDrawable(getResources().getDrawable(R.mipmap.icon_star));
                    optionalList.add(quote_code);
                }
                SPUtils.putString(AppConfig.KEY_OPTIONAL, Util.SPDeal(optionalList));

                break;
            /*??????????????????*/
            case R.id.layout_buy_sell_switch:
                if (quote != null) {
                    Util.lightOff(getActivity());
                    showBuySellSwitch(getActivity(), layout_view);
                }
                break;
            case R.id.layout_switch_limit_price:
                showLimitPriceWindow();
                break;
            case R.id.img_market:
                if (quote_code_old!=null){
                    WebSocketManager.getInstance().cancelQuotes("4002", quote_code_old);
                }
                WebSocketManager.getInstance().cancelQuotes("4002", quote_code);
                SpotTradeActivity.enter(getActivity(), tradeType, itemData);
                //getActivity().finish();
                break;
        }
    }

    private String old_code = null;

    /* private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(@NotNull Message msg) {
             super.handleMessage(msg);
             //???????????????
             // Log.d("print", "handleMessage:??????:  "+quote_code);
             if (quote_code != null) {
                 old_code = quote_code;
                 Log.d("print", "handleMessage:??????fragment??????: " + quote_code);
             }
         }
     };*/
    private int length = 5;
    private int count = 0;//???????????????????????? ????????????????????????

    @SuppressLint("SetTextI18n")
    @Override
    public void update(Observable o, Object arg) {

        if (o == QuoteSpotManger.getInstance()) {
            if (!isAdded()) {
                return;
            }
            quote = (String) arg;
            Log.d("print", "update:??????fragment??????????????????:  " + quote);
            buyList = Util.getBuyList(quote);


            runOnUiThread(() -> {
                buyAdapter.isSell(false);
                buyAdapter.setDatas(buyList.subList(0, length), Util.buyMax(quote));
                sellList = Util.getSellList(quote);
                sellAdapter.isSell(true);
                Collections.reverse(sellList);
                sellAdapter.setDatas(sellList.subList(0, length), Util.sellMax(quote));
            });
        } else if (o == SpotCodeManger.getInstance()) {
            itemData = (String) arg;
            runOnUiThread(() -> {
                setContent(itemData);
                quote_code = itemQuoteContCode(itemData);

            });


        } else if (o == SocketQuoteManger.getInstance()) {
            if (!isAdded()) {
                return;
            }
            arrayMap = (ArrayMap<String, List<String>>) arg;
            quoteList = arrayMap.get(type);
            Log.d("print", "update:843 " + quoteList);
            runOnUiThread(() -> {
                if (quoteList != null && quoteAdapter_market_pop != null) {
                    //?????????
                    if (edit_search.getText().toString().equals("")) {
                        quoteAdapter_market_pop.setDatas(quoteList);
                    } else {
                        List<String> searchQuoteList = TradeUtil.searchQuoteList(edit_search.getText().toString(), quoteList);
                        quoteAdapter_market_pop.setDatas(searchQuoteList);
                    }

                }
            });


        } else if (o == QuoteSpotCurrentManger.getInstance()) {
            if (!isAdded()) {
                return;
            }
            QuoteMinEntity quoteMinEntity = (QuoteMinEntity) arg;
            // Log.d("print", "onReceive:1549:??????fragment??????:  " + quoteMinEntity);

            if (quoteMinEntity != null) {
                runOnUiThread(() -> {
                    if (quoteMinEntity.getSymbol().equals(quote_code)) {
                        dismissProgressDialog();
                        if (text_price != null) {
                            int isUp = quoteMinEntity.getIsUp();
                            price = quoteMinEntity.getPrice();
                            text_price.setText(String.valueOf(price));

                            if (count == 0) {
                                edit_price_limit.setText(String.valueOf(price));
                                count++;
                            }
                            text_scale.setText(TradeUtil.scaleString(TradeUtil.decimalPoint(String.valueOf(price))));
                            if (value_rate != null) {
                                text_currency_price.setText("$" + TradeUtil.numberHalfUp(TradeUtil.mul(price, Double.parseDouble(value_rate)), 2));
                            } else {
                                text_currency_price.setText("$" + price);
                            }
                            switch (isUp) {
                                case -1:
                                    text_price.setTextColor(AppContext.getAppContext().getResources().getColor(R.color.text_quote_red));
                                    break;
                                case 1:
                                    text_price.setTextColor(AppContext.getAppContext().getResources().getColor(R.color.text_quote_green));
                                    break;
                                case 0:
                                    text_price.setTextColor(AppContext.getAppContext().getResources().getColor(R.color.text_main_color));
                                    break;
                            }

                        }
                    }
                });
            }
        } else if (o == BalanceManger.getInstance()) {
            if (!isAdded()) {
                return;
            }

            BalanceEntity balanceEntity = (BalanceEntity) arg;
            if (balanceEntity == null) {
                return;
            }

            text_balance.setText(TradeUtil.justDisplay(BalanceManger.getInstance().getBalanceReal()) + " " + getResources().getString(R.string.text_usdt));

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("progress", "onStart: "+"Spot onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("progress", "onStop: "+"Spot onStop");

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("progress", "onResume: "+"Spot onResume");

        BalanceManger.getInstance().getBalance("USDT");
        //WebSocketManager.getInstance().sendQuotes("4001", quote_code, "1");

    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d("progress", "onPause: "+"Spot onPause");

        SocketUtil.switchQuotesList("3002");
        WebSocketManager.getInstance().cancelQuotes("4002", quote_code);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("progress", "onDestroy: "+"Spot onDestroy");

        cancelTimer();



    }


    private String limit_market_type = "0";

    /*?????? ???????????????*/
    private void showLimitPriceWindow() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_switch_limit_layout, null);
        PopupWindow popupWindowLimitMarket = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        view.findViewById(R.id.text_limit_order).setOnClickListener(v -> {

            popupWindowLimitMarket.dismiss();
            text_limit_market.setText(getResources().getText(R.string.text_limit_order));
            layout_spot_limit.setVisibility(View.VISIBLE);
            layout_spot_market.setVisibility(View.GONE);
            limit_market_type = "0";

        });


        view.findViewById(R.id.text_market_order).setOnClickListener(v -> {
            popupWindowLimitMarket.dismiss();
            text_limit_market.setText(getResources().getText(R.string.text_market_order));
            layout_spot_limit.setVisibility(View.GONE);
            layout_spot_market.setVisibility(View.VISIBLE);
            limit_market_type = "1";

        });


        Util.dismiss(getActivity(), popupWindowLimitMarket);
        popupWindowLimitMarket.setFocusable(true);
        popupWindowLimitMarket.setOutsideTouchable(true);
        popupWindowLimitMarket.setContentView(view);
        popupWindowLimitMarket.showAsDropDown(layout_switch_limit_price, Gravity.CENTER, 0, 0);
    }

    /*?????????????????????*/
    public void showBuySellSwitch(Activity activity, View layout_view) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_buy_sell_pop_layout, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        view.findViewById(R.id.text_default).setOnClickListener(view1 -> {

            popupWindow.dismiss();
            recyclerView_buy.setVisibility(View.VISIBLE);
            recyclerView_sell.setVisibility(View.VISIBLE);
            length = 5;

            buyAdapter.setDatas(buyList.subList(0, length), Util.buyMax(quote));
            sellAdapter.setDatas(sellList.subList(0, length), Util.sellMax(quote));

        });


        view.findViewById(R.id.text_show_sell).setOnClickListener(view12 -> {

            popupWindow.dismiss();
            recyclerView_buy.setVisibility(View.GONE);
            recyclerView_sell.setVisibility(View.VISIBLE);
            length = 10;
            if (sellList != null) {
                sellAdapter.setDatas(sellList.subList(0, length), Util.sellMax(quote));
            }

        });
        view.findViewById(R.id.text_show_buy).setOnClickListener(view12 -> {

            popupWindow.dismiss();
            recyclerView_buy.setVisibility(View.VISIBLE);
            recyclerView_sell.setVisibility(View.GONE);
            length = 10;
            if (buyList != null) {
                buyAdapter.setDatas(buyList.subList(0, length), Util.buyMax(quote));
            }

        });


        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            popupWindow.dismiss();
        });


        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);


        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(100);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(layout_view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        view.startAnimation(animation);

    }


    // ??????????????????
    private TranslateAnimation animation;
    private EditText edit_search;
    private JSONObject chargeUnitEntityJson;

    private List<String> titleList, titleListContract, optionalTitleList;
    private OptionalSelectAdapter optionalSelectAdapter;
    private boolean flag_new_price = false;
    private boolean flag_up_down = false;
    private boolean flag_name = false;
    private String type = AppConfig.CONTRACT_ALL;
    private String zone_type = AppConfig.VIEW_CONTRACT;//-1????????? 1????????? 0???????????? 2????????????
    private ArrayMap<String, List<String>> arrayMap;

    private QuoteAdapter quoteAdapter_market_pop;
    private List<String> quoteList;

    /*???????????????????????????*/
    private void showQuotePopWindow() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.quote_market, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);


        titleList = new ArrayList<>();
        titleList.add(getString(R.string.text_optional));
        titleList.add(getString(R.string.text_contract));
        //titleList.add(getString(R.string.text_derived));
        titleList.add(getString(R.string.text_spot));
        LinearLayout layout_optional_select_pop = view.findViewById(R.id.layout_optional_select_pop);

        RecyclerView recyclerView_optional_select_pop = view.findViewById(R.id.recyclerView_optional_pop);

        LinearLayout layout_null_pop = view.findViewById(R.id.layout_null);

        optionalSelectAdapter = new OptionalSelectAdapter(getActivity());
        recyclerView_optional_select_pop.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        recyclerView_optional_select_pop.setAdapter(optionalSelectAdapter);
        optionalTitleList = new ArrayList<>();
        optionalTitleList.add(getString(R.string.text_contract));
        optionalTitleList.add(getString(R.string.text_spot));
        //optionalTitleList.add(getString(R.string.text_derived));
        optionalSelectAdapter.setDatas(optionalTitleList);
        optionalSelectAdapter.select(getString(R.string.text_contract));
        optionalSelectAdapter.setEnable(true);


        TabLayout tabLayout_market_search = view.findViewById(R.id.tabLayout_market_search);

        LinearLayout layout_null = view.findViewById(R.id.layout_null);

        RecyclerView recyclerView_market = view.findViewById(R.id.recyclerView_market);

        quoteAdapter_market_pop = new QuoteAdapter(getActivity());
        recyclerView_market.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_market.setAdapter(quoteAdapter_market_pop);
        String quoteJson = SPUtils.getString(AppConfig.QUOTE_LIST, null);
        List<String> quote_list = Util.SPDealStringResult(quoteJson);
        if (quoteList == null) {
            quoteAdapter_market_pop.setDatas(quote_list);
        } else {
            quoteAdapter_market_pop.setDatas(quoteList);
        }
        quoteAdapter_market_pop.isShowIcon(false);
        ImageView img_price_triangle = view.findViewById(R.id.img_price_triangle);

        ImageView img_rate_triangle = view.findViewById(R.id.img_rate_triangle);

        ImageView img_name_triangle = view.findViewById(R.id.img_name_triangle);

        /*???????????????*/
        optionalSelectAdapter.setOnItemClick((position, data) -> {
            optionalSelectAdapter.select(data);
            switch (position) {
                case 0:
                    type = AppConfig.OPTIONAL_CONTRACT_ALL;
                    zone_type = AppConfig.VIEW_OPTIONAL_CONTRACT;

                    quoteList = arrayMap.get(type);
                    if (quoteList == null) {
                        layout_null_pop.setVisibility(View.VISIBLE);
                        recyclerView_optional_select_pop.setVisibility(View.GONE);
                    } else {
                        layout_null_pop.setVisibility(View.GONE);
                        recyclerView_optional_select_pop.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList);
                    }
                    img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    break;
                case 1:
                    type = AppConfig.OPTIONAL_SPOT_ALL;
                    zone_type = AppConfig.VIEW_OPTIONAL_SPOT;

                    quoteList = arrayMap.get(type);
                    if (quoteList == null) {
                        layout_null_pop.setVisibility(View.VISIBLE);
                        recyclerView_optional_select_pop.setVisibility(View.GONE);
                    } else {
                        layout_null_pop.setVisibility(View.GONE);
                        recyclerView_optional_select_pop.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList);
                    }
                    img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    break;
                case 2:
                    type = AppConfig.OPTIONAL_DERIVATIVES_ALL;
                    zone_type = AppConfig.VIEW_OPTIONAL_DERIVATIVES;
                    quoteList = arrayMap.get(type);
                    if (quoteList == null) {
                        layout_null_pop.setVisibility(View.VISIBLE);
                        recyclerView_optional_select_pop.setVisibility(View.GONE);
                    } else {
                        layout_null_pop.setVisibility(View.GONE);
                        recyclerView_optional_select_pop.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList);
                    }
                    img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    break;
            }
        });


        for (String market_name : titleList) {
            tabLayout_market_search.addTab(tabLayout_market_search.newTab().setText(market_name));
        }
        tabLayout_market_search.getTabAt(1).select();
        view.findViewById(R.id.layout_new_price).setOnClickListener(v -> {
            if (arrayMap == null) {
                return;
            }
            if (flag_new_price) {
                img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_down));
                flag_new_price = false;

                Util.priceTypeHigh2Low(zone_type, response -> type = (String) response);
                List<String> quoteList = arrayMap.get(type);
                quoteAdapter_market_pop.setDatas(quoteList);


            } else {

                img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up));
                flag_new_price = true;
                Util.priceTypeLow2High(zone_type, response -> type = (String) response);
                List<String> quoteList = arrayMap.get(type);
                quoteAdapter_market_pop.setDatas(quoteList);

            }
            img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
            img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));

        });
        view.findViewById(R.id.layout_up_down).setOnClickListener(v -> {
            if (arrayMap == null) {
                return;
            }
            if (flag_up_down) {
                img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_down));
                flag_up_down = false;

                Util.rateTypeHigh2Low(zone_type, response -> type = (String) response);
                List<String> quoteList = arrayMap.get(type);
                quoteAdapter_market_pop.setDatas(quoteList);

            } else {
                img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up));
                flag_up_down = true;

                Util.rateTypeLow2High(zone_type, response -> type = (String) response);
                List<String> quoteList = arrayMap.get(type);
                quoteAdapter_market_pop.setDatas(quoteList);

            }
            img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
            img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
        });
        view.findViewById(R.id.layout_name).setOnClickListener(v -> {
            if (arrayMap == null) {
                return;
            }
            if (flag_name) {
                img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_down));
                flag_name = false;

                Util.nameTypeA2Z(zone_type, response -> type = (String) response);
                List<String> quoteList = arrayMap.get(type);
                quoteAdapter_market_pop.setDatas(quoteList);

            } else {
                img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up));
                flag_name = true;

                Util.nameTypeZ2A(zone_type, response -> type = (String) response);
                List<String> quoteList = arrayMap.get(type);
                quoteAdapter_market_pop.setDatas(quoteList);

            }
            img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
            img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
        });

        tabLayout_market_search.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //????????????????????????
                flag_new_price = false;
                flag_up_down = false;
                flag_name = false;
                if (arrayMap == null) {
                    return;
                }
                //??????
                if (tab.getPosition() == 0) {
                    layout_optional_select_pop.setVisibility(View.VISIBLE);
                    type = AppConfig.OPTIONAL_CONTRACT_ALL;
                    zone_type = AppConfig.VIEW_OPTIONAL_CONTRACT;
                    quoteList = arrayMap.get(type);
                    Log.d("print", "onTabSelected:684:  " + quoteList + "  " + type);
                    if (quoteList == null) {
                        layout_null.setVisibility(View.VISIBLE);
                        recyclerView_market.setVisibility(View.GONE);
                    } else {
                        layout_null.setVisibility(View.GONE);
                        recyclerView_market.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList);
                    }
                    img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                }//??????
                else if (tab.getPosition() == 1) {
                    layout_optional_select_pop.setVisibility(View.GONE);

                    type = AppConfig.CONTRACT_ALL;
                    zone_type = AppConfig.VIEW_CONTRACT;

                    quoteList = arrayMap.get(type);
                    if (quoteList == null) {
                        layout_null.setVisibility(View.VISIBLE);
                        recyclerView_market.setVisibility(View.GONE);
                    } else {
                        layout_null.setVisibility(View.GONE);
                        recyclerView_market.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList);
                    }
                    img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                }//?????????
               /* else if (tab.getPosition() == 2) {
                    layout_optional_select_pop.setVisibility(View.GONE);

                    type = AppConfig.DERIVATIVES_ALL;
                    zone_type = AppConfig.VIEW_DERIVATIVES;

                    quoteList = arrayMap.get(type);
                    if (quoteList == null) {
                        layout_null.setVisibility(View.VISIBLE);
                        recyclerView_market.setVisibility(View.GONE);
                    } else {
                        layout_null.setVisibility(View.GONE);
                        recyclerView_market.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList);
                    }
                    img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                }*///??????
                else if (tab.getPosition() == 2) {
                    layout_optional_select_pop.setVisibility(View.GONE);

                    type = AppConfig.SPOT_ALL;
                    zone_type = AppConfig.VIEW_SPOT;

                    quoteList = arrayMap.get(type);
                    if (quoteList == null) {
                        layout_null.setVisibility(View.VISIBLE);
                        recyclerView_market.setVisibility(View.GONE);
                    } else {
                        layout_null.setVisibility(View.GONE);
                        recyclerView_market.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList);
                    }
                    img_rate_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.market_up_down));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        SwipeRefreshLayout swipeRefreshLayout_market = view.findViewById(R.id.swipeRefreshLayout_market);
        swipeRefreshLayout_market.setColorSchemeColors(getResources().getColor(R.color.maincolor));
        /*????????????*/
        swipeRefreshLayout_market.setOnRefreshListener(() -> {
            swipeRefreshLayout_market.setRefreshing(false);
            quoteAdapter_market_pop.setDatas(quoteList);

        });

        quote_code_old = quote_code;
        quoteAdapter_market_pop.setOnItemClick(data -> {
            showProgressDialog();
            SocketUtil.switchQuotesList("3002");
            quote_code = TradeUtil.itemQuoteContCode(data);
            WebSocketManager.getInstance().cancelQuotes("4002", quote_code_old);

            if (TradeUtil.type(data).equals(AppConfig.TYPE_FT)) {
                QuoteCodeManger.getInstance().postTag(data);
            } else if (TradeUtil.type(data).equals(AppConfig.TYPE_CH)) {
                SpotCodeManger.getInstance().postTag(data);
                Log.d("print", "showQuotePopWindow:1231:  " + data + "  " + old_code);
                setContent(data);
                type = AppConfig.CONTRACT_IN_ALL;
                WebSocketManager.getInstance().sendQuotes("4001", quote_code, "1");


                //??????????????????????????????
                Util.isOptional(quote_code, optionalList, response -> {
                    boolean isOptional = (boolean) response;
                    if (isOptional) {
                        img_star_spot.setImageDrawable(getResources().getDrawable(R.mipmap.icon_star));
                    } else {
                        img_star_spot.setImageDrawable(getResources().getDrawable(R.mipmap.icon_star_normal));

                    }
                });


                text_name.setText(TradeUtil.name(data));
                text_currency.setText(TradeUtil.currency(data));


            }
            //????????????
            popupWindow.dismiss();


        });


        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            type = AppConfig.SPOT_ALL;
            tabLayout_market_search.getTabAt(AppConfig.selectPosition).select();
            popupWindow.dismiss();
        });

        RelativeLayout layout_bar = view.findViewById(R.id.layout_bar);

        edit_search = view.findViewById(R.id.edit_search);

        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    layout_bar.setVisibility(View.GONE);
                    tabLayout_market_search.setVisibility(View.GONE);
                    tabLayout_market_search.getTabAt(AppConfig.selectPosition).select();
                    type = AppConfig.SPOT_ALL;
                    List<String> strings = arrayMap.get(type);
                    List<String> searchQuoteList = TradeUtil.searchQuoteList(edit_search.getText().toString(), strings);
                    quoteAdapter_market_pop.setDatas(searchQuoteList);
                } else {
                    layout_bar.setVisibility(View.VISIBLE);
                    tabLayout_market_search.setVisibility(View.VISIBLE);
                    type = AppConfig.SPOT_ALL;
                    List<String> quoteList = arrayMap.get(type);
                    quoteAdapter_market_pop.setDatas(quoteList);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Util.dismiss(getActivity(), popupWindow);
        Util.isShowing(getActivity(), popupWindow);


        animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(300);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(layout_view);
        //   popupWindow.showAtLocation(layout_view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        view.startAnimation(animation);

    }


}
