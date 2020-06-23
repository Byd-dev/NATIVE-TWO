package com.pro.bityard.guide;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pro.bityard.R;
import com.pro.bityard.activity.MainOneActivity;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.base.BaseActivity;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.entity.GuideEntity;
import com.pro.switchlibrary.SPUtils;
import com.stx.xhb.xbanner.XBanner;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;

import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class GuideActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.banner)
    XBanner banner;

    @BindView(R.id.btn_sure)
    Button btn_sure;

    @BindView(R.id.text_jump)
    TextView text_jump;

    @BindView(R.id.text_err)
    TextView text_err;

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


    }

    @Override
    protected void initData() {
        NetManger.getInstance().getInit((state, response) -> {
            if (state.equals(SUCCESS)) {
                text_jump.setOnClickListener(this);
                btn_sure.setOnClickListener(this);
                text_jump.setVisibility(View.VISIBLE);
                text_err.setVisibility(View.GONE);
                String string = SPUtils.getString(AppConfig.FIRST_OPEN, null);
                if (string != null) {
                    MainOneActivity.enter(GuideActivity.this, MainOneActivity.TAB_TYPE.TAB_HOME);
                    GuideActivity.this.finish();

                } else {
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
    protected void initEvent() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_jump:
            case R.id.btn_sure:
                MainOneActivity.enter(GuideActivity.this, MainOneActivity.TAB_TYPE.TAB_HOME);
                GuideActivity.this.finish();
                break;
        }
    }


}
