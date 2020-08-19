package com.pro.bityard.guide;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pro.bityard.R;
import com.pro.bityard.activity.MainFollowActivity;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.base.BaseActivity;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.entity.GuideEntity;
import com.pro.bityard.entity.InitEntity;
import com.pro.bityard.entity.TradeListEntity;
import com.pro.bityard.manger.TradeListManger;
import com.pro.bityard.utils.PermissionUtil;
import com.pro.bityard.utils.Util;
import com.pro.switchlibrary.SPUtils;
import com.stx.xhb.xbanner.XBanner;

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
    protected void initView(View view) {


        PermissionUtil.getInstance().initPermission(this, response -> {
            if (response == SUCCESS) {
                init();
            }
        });


    }


    private void init() {
        NetManger.getInstance().getInit((state, response) -> {
            if (state.equals(BUSY)) {
            } else if (state.equals(SUCCESS)) {
                InitEntity initEntity = (InitEntity) response;
                if (initEntity.getGroup() != null) {
                    SPUtils.putString(AppConfig.SUPPORT_CURRENCY, initEntity.getBrand().getSupportCurrency());//可支持的货币
                    SPUtils.putString(AppConfig.PRIZE_TRADE, initEntity.getBrand().getPrizeTrade());//礼金抵扣比例
                    String quoteDomain = initEntity.getQuoteDomain();//获取域名
                    SPUtils.putString(AppConfig.QUOTE_HOST, quoteDomain);
                    List<InitEntity.GroupBean> group = initEntity.getGroup();
                    Log.d("print", "initQuote:103:  " + group);
                    ArrayMap<String, String> stringStringArrayMap = Util.groupData(group);
                    String allList = Util.groupList(stringStringArrayMap);
                    SPUtils.putString(AppConfig.CONTRACT_ID, allList);
                    TradeListManger.getInstance().getTradeList(allList, (state1, response1) -> {
                        if (state1.equals(BUSY)) {
                        } else if (state1.equals(SUCCESS)) {
                            List<TradeListEntity> tradeListEntityList = (List<TradeListEntity>) response1;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < tradeListEntityList.size(); i++) {
                                stringBuilder.append(tradeListEntityList.get(i).getContractCode() + ",");
                            }
                            SPUtils.putString(AppConfig.QUOTE_CODE, stringBuilder.toString());
                            SPUtils.putString(AppConfig.QUOTE_DETAIL, tradeListEntityList.toString());
                        } else if (state1.equals(FAILURE)) {
                        }
                    });//获取合约号


                }
                run();
            } else if (state.equals(FAILURE)) {
                text_jump.setVisibility(View.GONE);
                text_err.setVisibility(View.VISIBLE);
                if (response != null) {
                    text_err.setText(response.toString());
                }
            }
        });
    }

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
            MainFollowActivity.enter(GuideActivity.this, MainFollowActivity.TAB_TYPE.TAB_HOME);
            GuideActivity.this.finish();
        } else {
            layout_view.setBackgroundColor(getResources().getColor(R.color.background_maincolor));
            text_jump.setOnClickListener(this);
            btn_sure.setOnClickListener(this);
            text_jump.setVisibility(View.VISIBLE);
            SPUtils.putString(AppConfig.FIRST_OPEN, "first");
            data = new ArrayList<>();
            data.add(new GuideEntity("实盘", "模拟", "助您验证理财方案", getResources().getDrawable(R.mipmap.guide_1)));
            data.add(new GuideEntity("闪电下单", "让您快人一步", "同样的操作 不同的收益", getResources().getDrawable(R.mipmap.guide_2)));
            data.add(new GuideEntity("每日签到", "领红包", "红包可用于实盘交易抵扣现金", getResources().getDrawable(R.mipmap.guide_3)));
            data.add(new GuideEntity("五重防护", "资金更安全", "资金 隐私 项目 合法 银行级别风控", getResources().getDrawable(R.mipmap.guide_4)));

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
                MainFollowActivity.enter(GuideActivity.this, MainFollowActivity.TAB_TYPE.TAB_HOME);
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
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                init();
                // 如果所有的权限都授予了, 跳转到主页
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                //只调用一次
                if (count == 1) {
                    init();
                    count++;
                }
            }
        }
    }
}
