package com.pro.bityard.guide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pro.bityard.R;
import com.pro.bityard.activity.MainActivity;
import com.pro.bityard.activity.MainActivity;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.base.BaseActivity;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.entity.GuideEntity;
import com.pro.bityard.entity.InitEntity;
import com.pro.bityard.entity.QuoteCodeEntity;
import com.pro.bityard.entity.TradeListEntity;
import com.pro.bityard.manger.SocketQuoteManger;
import com.pro.bityard.manger.TradeListManger;
import com.pro.bityard.manger.WebSocketManager;
import com.pro.bityard.utils.NetworkUtils;
import com.pro.bityard.utils.PermissionUtil;
import com.pro.bityard.utils.PopUtil;
import com.pro.bityard.utils.Util;
import com.pro.switchlibrary.AES;
import com.pro.switchlibrary.SPUtils;
import com.stx.xhb.xbanner.XBanner;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class GuideActivity extends BaseActivity implements View.OnClickListener {
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;


    @BindView(R.id.banner)
    XBanner banner;

    @BindView(R.id.btn_sure)
    Button btn_sure;

    @BindView(R.id.text_jump)
    TextView text_jump;

    @BindView(R.id.text_err)
    TextView text_err;

    @BindView(R.id.layout_view)
    RelativeLayout layout_view;

    private List<GuideEntity> data;
    private List<TradeListEntity> tradeListEntityList;
    private String domain;

    @Override
    protected int setContentLayout() {
        return R.layout.activity_guide;
    }

    public static void enter(Context context) {
        Intent intent = new Intent(context, GuideActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void initPresenter() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionUtil.getInstance().initPermission(this, response -> {
            if (response == SUCCESS) {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    init();
                } else {
                    Util.lightOff(this);
                    layout_view.post(() -> PopUtil.getInstance().showTip(GuideActivity.this, layout_view, true, "WIFI SETTINGS?", state -> {
                        if (state) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //????????????????????????wifi??????????????????
                        } else {
                            GuideActivity.this.finish();
                        }
                    }));
                }
            }
        });
    }

    @Override

    protected void initView(View view) {
    }


    private void init() {
        //?????????webSocket ??????
        // SocketQuoteManger.getInstance().initSocket(NetManger.QUOTE_SOCKET);
        startScheduleJob(mHandler, 2000, 2000);

        String json = Util.getJson("layout.json", this);
        SPUtils.putString(AppConfig.QUOTE_CODE_JSON, json);


        NetManger.getInstance().getInit((state, response) -> {
            if (state.equals(BUSY)) {
            } else if (state.equals(SUCCESS)) {
                InitEntity initEntity = (InitEntity) response;
                if (initEntity.getGroup() != null) {
                    SPUtils.putString(AppConfig.SUPPORT_CURRENCY, initEntity.getBrand().getSupportCurrency());//??????????????????
                    SPUtils.putString(AppConfig.PRIZE_TRADE, initEntity.getBrand().getPrizeTrade());//??????????????????
                    SPUtils.putString(AppConfig.LUCKY_TRADE, initEntity.getBrand().getLuckyTrade());//??????????????????


                    String quoteDomain = initEntity.getQuoteDomain();//????????????
                    Log.d("print", "init:137:  " + quoteDomain);
                    try {
                        String quoteDomainUrl = AES.HexDecrypt(quoteDomain.getBytes(), AppConfig.S_KEY);

                        SPUtils.putString(AppConfig.QUOTE_HOST, quoteDomainUrl);
                        if (quoteDomainUrl.startsWith("http")) {
                            domain = quoteDomainUrl.replaceAll("http", "ws");
                        } else if (quoteDomainUrl.startsWith("https")) {
                            domain = quoteDomainUrl.replaceAll("https", "wss");
                        }
                        String url = domain + "/wsquote";
                        Log.d("print", "init:150:  " + quoteDomainUrl + "    " + url);
                        SPUtils.putString(AppConfig.QUOTE_SOCKET,url);
                        SocketQuoteManger.getInstance().initSocket(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SPUtils.putData(AppConfig.KEY_COMMODITY, initEntity);
                    String allList2 = Util.initContractList(initEntity.getData());
                    Log.d("print", "init:159: "+allList2);
                    SPUtils.putString(AppConfig.CONTRACT_ID, allList2);

                    TradeListManger.getInstance().getTradeList(allList2, (state1, response1) -> {
                        if (state1.equals(BUSY)) {
                        } else if (state1.equals(SUCCESS)) {
                            tradeListEntityList = (List<TradeListEntity>) response1;
                            if (tradeListEntityList.size() == 0) {
                                text_jump.setVisibility(View.GONE);
                                text_err.setVisibility(View.VISIBLE);
                                Toast.makeText(this, "The contract number is empty!", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < tradeListEntityList.size(); i++) {
                                    stringBuilder.append(tradeListEntityList.get(i).getContractCode() + ",");
                                }
                                Log.d("print", "init:177: "+stringBuilder.toString());
                                //SPUtils.putString(AppConfig.QUOTE_CODE, stringBuilder.toString().replaceAll("null,",""));
                                SPUtils.putString(AppConfig.QUOTE_CODE, stringBuilder.toString());
                                SPUtils.putString(AppConfig.QUOTE_DETAIL, Util.SPDealContract(tradeListEntityList));
                                run();
                            }

                        } else if (state1.equals(FAILURE)) {
                        }
                    });//???????????????

                }
            } else if (state.equals(FAILURE)) {
                text_jump.setVisibility(View.GONE);
                text_err.setVisibility(View.VISIBLE);
                reconnect();
                if (response != null) {
                    text_err.setText(response.toString());
                }
            }
        });
    }

    private int connectNum = 0;
    private final static int MAX_NUM = 5;       // ???????????????
    private final static int MILLIS = 3000;     // ???????????????????????????

    public void reconnect() {
        if (connectNum <= MAX_NUM) {
            try {
                Thread.sleep(MILLIS);
                connect();
                connectNum++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Util.lightOff(this);
            PopUtil.getInstance().showTip(this, layout_view, true, "INIT FAILED ! EXIT OR RETRY ?", state -> {
                if (state) {
                    init();
                } else {
                    GuideActivity.this.finish();
                }
            });
        }
    }

    public void connect() {
        if (isConnect()) {
            return;
        } else {
            init();
        }
    }

    public boolean isConnect() {
        return tradeListEntityList != null && tradeListEntityList.size() != 0;
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            String quote_code = SPUtils.getString(AppConfig.QUOTE_CODE, null);
            if (quote_code == null) {
                NetManger.getInstance().initQuote();
                return;
            } else {
                WebSocketManager.getInstance().send("3000");
            }

        }
    };

    @Override
    protected void initData() {


        String language_local = Locale.getDefault().toString();
        String language = SPUtils.getString(AppConfig.KEY_LANGUAGE, null);
        if (language.equals(AppConfig.KEY_LANGUAGE)) {
            SPUtils.putString(AppConfig.KEY_LANGUAGE, language_local);
        }


    }

    private void run() {
        text_err.setVisibility(View.GONE);
        String string = SPUtils.getString(AppConfig.FIRST_OPEN, null);
        if (string != null) {
            MainActivity.enter(GuideActivity.this, MainActivity.TAB_TYPE.TAB_HOME);
            GuideActivity.this.finish();
        } else {
            layout_view.setBackgroundColor(getResources().getColor(R.color.background_main_color));
            text_jump.setOnClickListener(this);
            btn_sure.setOnClickListener(this);
            text_jump.setVisibility(View.VISIBLE);
            SPUtils.putString(AppConfig.FIRST_OPEN, "first");
            data = new ArrayList<>();
            data.add(new GuideEntity("??????", "??????", "????????????????????????", getResources().getDrawable(R.mipmap.guide_1)));
            data.add(new GuideEntity("????????????", "??????????????????", "??????????????? ???????????????", getResources().getDrawable(R.mipmap.guide_2)));
            data.add(new GuideEntity("????????????", "?????????", "???????????????????????????????????????", getResources().getDrawable(R.mipmap.guide_3)));
            data.add(new GuideEntity("????????????", "???????????????", "?????? ?????? ?????? ?????? ??????????????????", getResources().getDrawable(R.mipmap.guide_4)));

            banner.setBannerData(R.layout.banner_guide_layout, data);
            banner.loadImage((banner, model, view1, position) -> {
                ImageView imageView = view1.findViewById(R.id.img_banner);

                imageView.setImageDrawable(data.get(position).getDrawable());

                TextView text_left = view1.findViewById(R.id.text_left);
                TextView text_right = view1.findViewById(R.id.text_right);
                TextView text_bottom = view1.findViewById(R.id.text_bottom);

                text_left.setText(data.get(position).getTextLeft());
                text_right.setText(data.get(position).getTextRight());
                text_bottom.setText(data.get(position).getTextBottom());
            });

            banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    if (i == 3) {
                        btn_sure.setVisibility(View.VISIBLE);
                    } else {
                        btn_sure.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {

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
            case R.id.text_jump:
            case R.id.btn_sure:
                MainActivity.enter(GuideActivity.this, MainActivity.TAB_TYPE.TAB_HOME);
                GuideActivity.this.finish();
                break;
        }


    }

    int count = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            // ?????????????????????????????????????????????
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                init();
                // ?????????????????????????????????, ???????????????
            } else {
                // ????????????????????????????????????????????????, ???????????????????????????????????????????????????????????????
                //???????????????
                if (count == 1) {
                    init();
                    count++;
                }
            }
        }
    }
}
