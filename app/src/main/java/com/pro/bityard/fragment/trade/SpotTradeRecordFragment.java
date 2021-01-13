package com.pro.bityard.fragment.trade;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.pro.bityard.R;
import com.pro.bityard.adapter.MyPagerAdapter;
import com.pro.bityard.adapter.RadioDateAdapter;
import com.pro.bityard.base.BaseFragment;
import com.pro.bityard.manger.ControlManger;

import java.util.ArrayList;
import java.util.List;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;

public class SpotTradeRecordFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.text_title)
    TextView text_title;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.img_spot_filter)
    ImageView img_spot_filter;

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.layout_right)
    LinearLayout layout_right;

    @BindView(R.id.recyclerView_date)
    RecyclerView recyclerView_date;

    @BindView(R.id.recyclerView_type)
    RecyclerView recyclerView_type;

    @BindView(R.id.btn_sure)
    Button btn_sure;
    @BindView(R.id.btn_return)
    Button btn_return;

    @BindView(R.id.edit_search)
    EditText edit_search;

    private RadioDateAdapter radioDateAdapter, radioTypeAdapter;//杠杆适配器
    private List<String> dataList, typeList;


    @Override
    protected void onLazyLoad() {

    }

    @Override
    protected void initView(View view) {
        text_title.setText(getResources().getString(R.string.text_spot));
        view.findViewById(R.id.img_back).setOnClickListener(this);
        btn_return.setOnClickListener(this);
        btn_sure.setOnClickListener(this);

        Handler handler = new Handler();
        handler.postDelayed(() -> initContent(), 50);
    }

    private String value_date = null;
    private String value_type = null;

    private void initContent() {
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        initViewPager(viewPager);
        img_spot_filter.setOnClickListener(this);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    img_spot_filter.setVisibility(View.VISIBLE);
                } else {
                    img_spot_filter.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        value_date = getActivity().getString(R.string.text_near_one_day);
        value_type = getActivity().getString(R.string.text_buy_and_sell);

        radioDateAdapter = new RadioDateAdapter(getActivity());
        recyclerView_date.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView_date.setAdapter(radioDateAdapter);

        dataList = new ArrayList<>();
        dataList.add(getString(R.string.text_near_one_day));
        dataList.add(getString(R.string.text_near_one_week));
        dataList.add(getString(R.string.text_near_one_month));
        dataList.add(getString(R.string.text_near_three_month));

        radioDateAdapter.setDatas(dataList);
        radioDateAdapter.select(getString(R.string.text_near_one_day));
        radioDateAdapter.setOnItemClick((position, data) -> {
            radioDateAdapter.select(data);
            value_date = data;
        });


        radioTypeAdapter = new RadioDateAdapter(getActivity());
        recyclerView_type.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView_type.setAdapter(radioTypeAdapter);

        typeList = new ArrayList<>();
        typeList.add(getString(R.string.text_buy_and_sell));
        typeList.add(getString(R.string.text_buy));
        typeList.add(getString(R.string.text_sell));
        radioTypeAdapter.select(getString(R.string.text_buy_and_sell));

        radioTypeAdapter.setDatas(typeList);
        radioTypeAdapter.setOnItemClick((position, data) -> {
            radioTypeAdapter.select(data);
            value_type = data;
        });

    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.fragment_spot_trade_record;
    }

    @Override
    protected void intPresenter() {

    }

    @Override
    protected void initData() {

    }

    private void initViewPager(ViewPager viewPager) {
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        myPagerAdapter.addFragment(new SpotRecordItemFragment(), getString(R.string.text_spot_position));
        myPagerAdapter.addFragment(new SpotCommitHistoryFragment(), getString(R.string.text_history_spot_position));
        myPagerAdapter.addFragment(new SpotTradeHistoryFragment(), getString(R.string.text_history_trade));

        viewPager.setAdapter(myPagerAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                getActivity().finish();
                break;
            case R.id.img_spot_filter:
                drawerLayout.openDrawer(layout_right);
                break;
            case R.id.btn_sure:
                String value_search = edit_search.getText().toString();
                String value = value_date + "," + value_search + "," + value_type;
                ControlManger.getInstance().postTag(value);
                drawerLayout.closeDrawer(layout_right);
                break;
            case R.id.btn_return:
                radioDateAdapter.select(getString(R.string.text_near_one_day));
                radioTypeAdapter.select(getString(R.string.text_buy_and_sell));
                edit_search.setText("");
                value_date = getActivity().getString(R.string.text_near_one_day);
                value_type = getActivity().getString(R.string.text_buy_and_sell);
                break;
        }
    }
}