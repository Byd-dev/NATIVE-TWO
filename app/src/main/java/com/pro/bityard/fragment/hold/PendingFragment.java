package com.pro.bityard.fragment.hold;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.pro.bityard.R;
import com.pro.bityard.adapter.PendingAdapter;
import com.pro.bityard.adapter.PositionAdapter;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.api.OnNetResult;
import com.pro.bityard.api.OnNetTwoResult;
import com.pro.bityard.base.BaseFragment;
import com.pro.bityard.entity.OpenPositionEntity;
import com.pro.bityard.entity.PendingEntity;
import com.pro.bityard.entity.TipCloseEntity;
import com.pro.bityard.view.HeaderRecyclerView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class PendingFragment extends BaseFragment {
    @BindView(R.id.headerRecyclerView)
    HeaderRecyclerView headerRecyclerView;

    private PendingAdapter pendingAdapter;


    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    private String tradeType;


    public PendingFragment newInstance(String type) {
        PendingFragment fragment = new PendingFragment();
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



        pendingAdapter = new PendingAdapter(getContext());

        headerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        headerRecyclerView.setAdapter(pendingAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });

        pendingAdapter.setOnItemClick(new PendingAdapter.OnItemClick() {
            @Override
            public void onClickListener(PendingEntity.DataBean data) {

            }

            @Override
            public void onCancelListener(String id) {
                /*平仓*/
                NetManger.getInstance().cancel(id, tradeType, new OnNetResult() {
                    @Override
                    public void onNetResult(String state, Object response) {
                        if (state.equals(BUSY)) {
                            showProgressDialog();
                        } else if (state.equals(SUCCESS)) {
                            dismissProgressDialog();
                            TipCloseEntity tipCloseEntity = (TipCloseEntity) response;
                            Toast.makeText(getContext(), tipCloseEntity.getMessage(), Toast.LENGTH_SHORT).show();
                            initData();

                        } else if (state.equals(FAILURE)) {
                            dismissProgressDialog();
                        }
                    }
                });
            }

            @Override
            public void onProfitLossListener() {

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
        NetManger.getInstance().getPending(tradeType, new OnNetTwoResult() {
            @Override
            public void setResult(String state, Object response1, Object response2) {
                if (state.equals(BUSY)) {
                    swipeRefreshLayout.setRefreshing(true);
                } else if (state.equals(SUCCESS)) {
                    swipeRefreshLayout.setRefreshing(false);
                    PendingEntity pendingEntity= (PendingEntity) response1;
                    Log.d("print", "setResult:106:  "+pendingEntity);
                    List<String> quoteList= (List<String>) response2;
                    pendingAdapter.setDatas(pendingEntity.getData(),quoteList);


                } else if (state.equals(FAILURE)) {
                    swipeRefreshLayout.setRefreshing(false);

                }
            }


        });
    }
}