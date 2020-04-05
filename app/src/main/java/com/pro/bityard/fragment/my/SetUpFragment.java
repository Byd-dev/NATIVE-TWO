package com.pro.bityard.fragment.my;

import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pro.bityard.R;
import com.pro.bityard.activity.UserActivity;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.api.OnNetResult;
import com.pro.bityard.base.BaseFragment;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.config.IntentConfig;
import com.pro.bityard.entity.LogoutTipEntity;
import com.pro.bityard.manger.BalanceManger;
import com.pro.bityard.manger.PositionRealManger;
import com.pro.bityard.manger.PositionSimulationManger;
import com.pro.bityard.manger.TagManger;
import com.pro.switchlibrary.SPUtils;

import butterknife.BindView;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class SetUpFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.text_theme)
    TextView text_theme;

    @BindView(R.id.text_language)
    TextView text_language;

    @BindView(R.id.text_select)
    TextView text_select;

    @BindView(R.id.text_rate)
    TextView text_rate;


    @Override
    public void onResume() {
        super.onResume();
        boolean theme = SPUtils.getBoolean(AppConfig.KEY_THEME, true);
        if (theme == true) {
            text_theme.setText(getResources().getText(R.string.text_night));
        } else {
            text_theme.setText(getResources().getText(R.string.text_day));
        }

        String language = SPUtils.getString(AppConfig.KEY_LANGUAGE,null);
        if (language==null) {

        } else {
            if (language.equals("en")) {
                text_language.setText(getResources().getText(R.string.text_english));
            } else if (language.equals("zh_simple")) {
                text_language.setText(getResources().getText(R.string.text_chinese));

            } else if (language.equals("zh_traditional")) {
                text_language.setText(getResources().getText(R.string.text_traditional));

            } else if (language.equals("ja")) {
                text_language.setText(getResources().getText(R.string.text_japan));

            } else if (language.equals("ko")) {
                text_language.setText(getResources().getText(R.string.text_korean));

            }
        }


    }

    @Override
    protected void onLazyLoad() {

    }

    @Override
    protected void initView(View view) {

        view.findViewById(R.id.img_back).setOnClickListener(this);

        view.findViewById(R.id.btn_logout).setOnClickListener(this);

        view.findViewById(R.id.layout_one).setOnClickListener(this);
        view.findViewById(R.id.layout_two).setOnClickListener(this);
        view.findViewById(R.id.layout_three).setOnClickListener(this);


    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.fragment_setup;
    }

    @Override
    protected void intPresenter() {

    }

    @Override
    protected void initData() {


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                getActivity().finish();
                break;
            case R.id.layout_one:
                UserActivity.enter(getContext(), IntentConfig.Keys.KEY_LANGUAGE);

                break;
            case R.id.layout_two:
                UserActivity.enter(getContext(), IntentConfig.Keys.KEY_THEME);

                break;
            case R.id.layout_three:
                UserActivity.enter(getContext(), IntentConfig.Keys.KEY_RATE);

                break;


            case R.id.btn_logout:

                NetManger.getInstance().postRequest("/api/sso/user_logout", null, new OnNetResult() {
                    @Override
                    public void onNetResult(String state, Object response) {
                        if (state.equals(BUSY)) {
                            showProgressDialog();
                        } else if (state.equals(SUCCESS)) {
                            dismissProgressDialog();
                            LogoutTipEntity tipEntity = new Gson().fromJson(response.toString(), LogoutTipEntity.class);
                            if (tipEntity.getCode() == 200) {
                                SPUtils.remove(AppConfig.LOGIN);
                                showToast(tipEntity.getMessage());
                                //退出成功 初始化
                                //余额初始化
                                TagManger.getInstance().clear();
                                getActivity().finish();


                            }
                        } else if (state.equals(FAILURE)) {
                            dismissProgressDialog();
                        }
                    }
                });
                break;


        }
    }
}
