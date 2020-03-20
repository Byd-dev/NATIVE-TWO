package com.pro.bityard.fragment.hold;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pro.bityard.R;
import com.pro.bityard.adapter.HistoryAdapter;
import com.pro.bityard.adapter.PendingAdapter;
import com.pro.bityard.adapter.PositionAdapter;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.api.OnNetResult;
import com.pro.bityard.api.OnNetTwoResult;
import com.pro.bityard.base.BaseFragment;
import com.pro.bityard.entity.HistoryEntity;
import com.pro.bityard.entity.OpenPositionEntity;
import com.pro.bityard.entity.PendingEntity;
import com.pro.bityard.view.HeaderRecyclerView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class HistoryFragment extends BaseFragment {
    @BindView(R.id.headerRecyclerView)
    HeaderRecyclerView headerRecyclerView;


    private HistoryAdapter historyAdapter;


    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    private String tradeType;


    public HistoryFragment newInstance(String type) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString("tradeType", type);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null){
            tradeType = getArguments().getString("tradeType");
        }
    }


    @Override
    protected void onLazyLoad() {

    }

    @Override
    protected void initView(View view) {

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.maincolor));

        historyAdapter = new HistoryAdapter(getContext());

        headerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        headerRecyclerView.setAdapter(historyAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });


    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.fragment_open;
    }

    @Override
    protected void intPresenter() {

    }

    @Override
    protected void initData() {

        NetManger.getInstance().getHistory(tradeType, new OnNetTwoResult() {
            @Override
            public void setResult(String state, Object response1, Object response2) {
                if (state.equals(BUSY)) {
                    swipeRefreshLayout.setRefreshing(true);
                } else if (state.equals(SUCCESS)) {
                    swipeRefreshLayout.setRefreshing(false);
                    HistoryEntity historyEntity= (HistoryEntity) response1;
                    List<String> quoteList= (List<String>) response2;
                    historyAdapter.setDatas(historyEntity.getData(),quoteList);


                } else if (state.equals(FAILURE)) {
                    swipeRefreshLayout.setRefreshing(false);

                }
            }


        });

    }
}