package com.pro.bityard.fragment.my;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pro.bityard.R;
import com.pro.bityard.activity.UserActivity;
import com.pro.bityard.base.BaseFragment;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.config.IntentConfig;
import com.pro.bityard.entity.LoginEntity;
import com.pro.switchlibrary.SPUtils;

import butterknife.BindView;

public class PersonFragment extends BaseFragment implements View.OnClickListener {




    @BindView(R.id.text_mobile_tip)
    TextView text_mobile_tip;

    @BindView(R.id.text_email_tip)
    TextView text_email_tip;



    private int lever=0;
    @Override
    protected int setLayoutResourceID() {
        return R.layout.fragment_person;
    }
    @Override
    public void onResume() {
        super.onResume();
        LoginEntity data = SPUtils.getData(AppConfig.LOGIN, LoginEntity.class);
        Log.d("print", "onResume:27:用户:  " + data);


    }

    @Override
    protected void onLazyLoad() {

    }

    @Override
    protected void initView(View view) {

        view.findViewById(R.id.img_back).setOnClickListener(this);


        view.findViewById(R.id.layout_one).setOnClickListener(this);
        view.findViewById(R.id.layout_two).setOnClickListener(this);
        view.findViewById(R.id.layout_three).setOnClickListener(this);
        view.findViewById(R.id.layout_four).setOnClickListener(this);


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

                UserActivity.enter(getActivity(), IntentConfig.Keys.KEY_SAFE_CENTER_LOGIN_PASS);

                break;
            case R.id.layout_two:
                UserActivity.enter(getActivity(), IntentConfig.Keys.KEY_SAFE_CENTER_FUNDS_PASS);

                break;
            case R.id.layout_three:
                UserActivity.enter(getActivity(), IntentConfig.Keys.KEY_SAFE_CENTER_BIND_CHANGE_MOBILE);

                break;
            case R.id.layout_four:
                UserActivity.enter(getActivity(), IntentConfig.Keys.KEY_SAFE_CENTER_BIND_CHANGE_EMAIL);

                break;


        }
    }
}
