package com.pro.bityard.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pro.bityard.R;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.api.OnNetResult;
import com.pro.bityard.base.BaseActivity;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.config.IntentConfig;
import com.pro.bityard.entity.CountryCodeEntity;
import com.pro.bityard.fragment.tab.HoldFragment;
import com.pro.bityard.fragment.tab.HomeRecyclerFragment;
import com.pro.bityard.fragment.tab.MarketFragment;
import com.pro.bityard.fragment.tab.MyFragment;
import com.pro.bityard.manger.ChargeUnitManger;
import com.pro.bityard.manger.QuoteListManger;
import com.pro.bityard.manger.TradeListManger;
import com.pro.bityard.viewutil.StatusBarUtil;
import com.pro.switchlibrary.SPUtils;

import androidx.annotation.Nullable;
import butterknife.BindView;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;
import static com.pro.bityard.config.AppConfig.QUOTE_SECOND;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    /**
     * 首页Tab索引
     */
    public static final class TAB_TYPE {
        public static final int COUNT = 4;
        public static final int TAB_HOME = 0;
        public static final int TAB_HALL = TAB_HOME + 1;
        public static final int TAB_POSITION = TAB_HALL + 1;
        public static final int TAB_INFORMATION = TAB_POSITION + 1;


    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;

    public static void enter(Context context, int tabIndex) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(IntentConfig.Keys.POSITION, tabIndex);
        context.startActivity(intent);
    }

    @Override
    protected int setContentLayout() {
        return R.layout.activity_main;

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView(View view) {
        //主题是深色的标题
        StatusBarUtil.setStatusBarDarkTheme(this, false);
        //打开沉浸式状态栏
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);


        radioGroup.setOnCheckedChangeListener(this);
        radioGroup.getChildAt(0).performClick();
        //行情初始化
        QuoteListManger.getInstance().startScheduleJob(QUOTE_SECOND, QUOTE_SECOND);


    }


    @Override
    protected void initData() {


        //获取国家code
        NetManger.getInstance().getRequest("/api/home/country/list", null, new OnNetResult() {
            @Override
            public void onNetResult(String state, Object response) {
                if (state.equals(BUSY)) {

                } else if (state.equals(SUCCESS)) {
                    CountryCodeEntity countryCodeEntity = new Gson().fromJson(response.toString(), CountryCodeEntity.class);
                    SPUtils.putData(AppConfig.COUNTRY_CODE, countryCodeEntity);

                } else if (state.equals(FAILURE)) {

                }
            }
        });
        //合约号初始化
        TradeListManger.getInstance().tradeList(new OnNetResult() {
            @Override
            public void onNetResult(String state, Object response) {
                if (state.equals(SUCCESS)) {
                    Toast.makeText(MainActivity.this, "合约号获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*手续费*/
        ChargeUnitManger.getInstance().chargeUnit(new OnNetResult() {
            @Override
            public void onNetResult(String state, Object response) {
                if (state.equals(SUCCESS)){
                    Toast.makeText(MainActivity.this, "手续费获取成功", Toast.LENGTH_SHORT).show();

                }
            }
        });
        //行情
        String quote_host = SPUtils.getString(AppConfig.QUOTE_HOST);
        String quote_code = SPUtils.getString(AppConfig.QUOTE_CODE);
        if (quote_host.equals("") && quote_code.equals("")) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.text_err_init), Toast.LENGTH_SHORT).show();
            NetManger.getInstance().initQuote();
            return;
        } else {
            QuoteListManger.getInstance().quote(quote_host, quote_code);
        }


    }


    @Override
    protected void initEvent() {

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_0:
                showFragment(R.id.layout_fragment_containter, new HomeRecyclerFragment(), null, null);

                break;
            case R.id.radio_1:
                showFragment(R.id.layout_fragment_containter, new MarketFragment(), null, null);


                break;


            case R.id.radio_2:


                break;

            case R.id.radio_3:
                showFragment(R.id.layout_fragment_containter, new HoldFragment(), null, null);


                break;
            case R.id.radio_4:
                showFragment(R.id.layout_fragment_containter, new MyFragment(), null, null);

                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        QuoteListManger.getInstance().cancelTimer();
        QuoteListManger.getInstance().clear();
    }
}
