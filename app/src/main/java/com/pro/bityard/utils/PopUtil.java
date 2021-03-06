package com.pro.bityard.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.pro.bityard.BuildConfig;
import com.pro.bityard.R;
import com.pro.bityard.adapter.OptionalSelectAdapter;
import com.pro.bityard.adapter.QuoteAdapter;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.api.OnNetResult;
import com.pro.bityard.api.OnResult;
import com.pro.bityard.api.PopQuotesResult;
import com.pro.bityard.api.PopResult;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.entity.HistoryEntity;
import com.pro.bityard.entity.PositionEntity;
import com.pro.bityard.entity.UserDetailEntity;
import com.pro.bityard.manger.SocketQuoteManger;
import com.pro.switchlibrary.SPUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.pro.bityard.api.NetManger.BUSY;
import static com.pro.bityard.api.NetManger.FAILURE;
import static com.pro.bityard.api.NetManger.SUCCESS;

public class PopUtil {
    private static PopUtil popUtil;


    public static PopUtil getInstance() {
        if (popUtil == null) {
            synchronized (SocketQuoteManger.class) {
                if (popUtil == null) {
                    popUtil = new PopUtil();
                }
            }

        }
        return popUtil;

    }

    public void showTip(Activity activity, View layout_view, boolean showCancel, String content, OnPopResult onPopResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_tip_pop_layout, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView text_tip = view.findViewById(R.id.text_content);
        text_tip.setLineSpacing(1.1f, 1.5f);

        text_tip.setText(content);


        view.findViewById(R.id.text_sure).setOnClickListener(v -> {
            onPopResult.setPopResult(true);
            popupWindow.dismiss();
        });

        TextView text_cancel = view.findViewById(R.id.text_cancel);
        if (showCancel) {
            text_cancel.setVisibility(View.VISIBLE);
        } else {
            text_cancel.setVisibility(View.GONE);

        }
        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            onPopResult.setPopResult(false);
            popupWindow.dismiss();
        });


        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // popupWindow.setAnimationStyle(R.style.pop_anim_quote);
        popupWindow.setContentView(view);
        popupWindow.showAtLocation(layout_view, Gravity.CENTER, 0, 0);

    }

    public void showSuccessTip(Activity activity, View layout_view, OnPopResult onPopResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_tip_pop_register_layout, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            onPopResult.setPopResult(false);
            popupWindow.dismiss();
        });

        view.findViewById(R.id.text_sure).setOnClickListener(v -> {
            onPopResult.setPopResult(true);
            popupWindow.dismiss();
        });

        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // popupWindow.setAnimationStyle(R.style.pop_anim_quote);
        popupWindow.setContentView(view);
        popupWindow.showAtLocation(layout_view, Gravity.CENTER, 0, 0);

    }

    public void showLongTip(Activity activity, View layout_view, boolean showCancel, String title, String title2, String title3,
                            String value, String value2, OnPopResult onPopResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_tip_long_pop_layout, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView text_one = view.findViewById(R.id.text_one);
        text_one.setLineSpacing(1.1f, 1.5f);
        text_one.setText(title);
        TextView text_two = view.findViewById(R.id.text_two);
        text_two.setLineSpacing(1.1f, 1.5f);
        text_two.setText(title2);
        TextView text_three = view.findViewById(R.id.text_three);
        text_three.setLineSpacing(1.1f, 1.5f);
        text_three.setText(title3);

        TextView text_value = view.findViewById(R.id.text_value);
        text_value.setLineSpacing(1.1f, 1.5f);
        text_value.setText(value + " " + activity.getResources().getString(R.string.text_usdt));

        TextView text_value2 = view.findViewById(R.id.text_value2);
        text_value2.setLineSpacing(1.1f, 1.5f);
        text_value2.setText(value2 + " " + activity.getResources().getString(R.string.text_usdt));


        view.findViewById(R.id.text_sure).setOnClickListener(v -> {
            onPopResult.setPopResult(true);
            popupWindow.dismiss();
        });

        TextView text_cancel = view.findViewById(R.id.text_cancel);
        if (showCancel) {
            text_cancel.setVisibility(View.VISIBLE);
        } else {
            text_cancel.setVisibility(View.GONE);

        }
        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            onPopResult.setPopResult(false);
            popupWindow.dismiss();
        });


        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // popupWindow.setAnimationStyle(R.style.pop_anim_quote);
        popupWindow.setContentView(view);
        popupWindow.showAtLocation(layout_view, Gravity.CENTER, 0, 0);

    }

    public void showEdit(Activity activity, View layout_view, boolean showCancel, OnResult onResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_edit_pop_layout, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        EditText edit_username = view.findViewById(R.id.edit_username);


        if (edit_username.getText().toString().equals("")) {

        }


        view.findViewById(R.id.text_sure).setOnClickListener(v -> {
            NetManger.getInstance().updateNickName(edit_username.getText().toString(), (state, response) -> {
                if (state.equals(SUCCESS)) {
                    onResult.setResult(edit_username.getText().toString());
                    popupWindow.dismiss();
                } else if (state.equals(FAILURE)) {
                    Toast.makeText(activity, response.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        TextView text_cancel = view.findViewById(R.id.text_cancel);
        if (showCancel) {
            text_cancel.setVisibility(View.VISIBLE);
        } else {
            text_cancel.setVisibility(View.GONE);

        }
        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            popupWindow.dismiss();
        });


        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setContentView(view);
        popupWindow.showAtLocation(layout_view, Gravity.CENTER, 0, 0);

    }


    private String hash = null;

    /*???????????????*/
    public void showVerification(Activity activity, View layout_view, OnResult onResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_verification_layout, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        ImageView img_code = view.findViewById(R.id.img_code);

        EditText edit_code = view.findViewById(R.id.edit_code);


        getCode(activity, img_code, response -> {
            hash = response.toString();
        });

        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        view.findViewById(R.id.img_refresh).setOnClickListener(v -> getCode(activity, img_code, response -> {

        }));

        view.findViewById(R.id.text_sure).setOnClickListener(v -> {
            String s = edit_code.getText().toString();
            if (s.equals("")) {
                Toast.makeText(activity, "????????????????????????", Toast.LENGTH_SHORT).show();
            } else {
                onResult.setResult(hash + "," + s);
                popupWindow.dismiss();
            }
        });

        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // popupWindow.setAnimationStyle(R.style.pop_anim_quote);
        popupWindow.setContentView(view);
        popupWindow.showAtLocation(layout_view, Gravity.CENTER, 0, 0);

    }

    public void getCode(Context context, ImageView img_code, OnResult onResult) {
        String hash = Util.Random32();
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("vHash", hash);
        Log.d("print", "getCode:290:  " + hash);
        NetManger.getInstance().getBitmapRequest("/api/code/image.jpg", map, (state, response) -> {
            if (state.equals(SUCCESS)) {
                Bitmap bitmap = (Bitmap) response;
                img_code.post(() -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] bytes = baos.toByteArray();
                    Glide.with(context.getApplicationContext()).load(bytes)
                            .centerCrop().into(img_code);
                    onResult.setResult(hash);
                });
            }
        });


    }


    private List<String> topList = new ArrayList<>();
    private List<String> midList = new ArrayList<>();
    private List<String> lowList = new ArrayList<>();


    public void dialogShare() {

    }


    /*??????????????????*/
    public void showShare(Activity activity, View layout_view, HistoryEntity.DataBean dataBean, PopResult popResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_share_pop, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout layout_share = view.findViewById(R.id.layout_share);


        double opPrice = dataBean.getOpPrice();
        double cpPrice = dataBean.getCpPrice();
        //?????????
        double lever = dataBean.getLever();
        double margin = dataBean.getMargin();
        double income = dataBean.getIncome();


        TextView text_name = view.findViewById(R.id.text_name);
        String[] split = Util.quoteList(dataBean.getContractCode()).split(",");
        text_name.setText(split[0]);
        TextView text_currency = view.findViewById(R.id.text_currency);
        text_currency.setText("/" + dataBean.getCurrency());

        TextView text_save = view.findViewById(R.id.text_save);

        TextView text_recommend_code = view.findViewById(R.id.text_recommend_code);

        UserDetailEntity userDetailEntity = SPUtils.getData(AppConfig.DETAIL, UserDetailEntity.class);
        if (userDetailEntity != null) {
            text_recommend_code.setText(userDetailEntity.getUser().getRefer());
        }

        text_save.setOnClickListener(v -> {
            if (PermissionUtil.readAndWrite(activity)) {
                ImageUtil.SaveBitmapFromView(activity, layout_share);
                Toast.makeText(activity, activity.getResources().getString(R.string.text_success), Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            } else {
                String[] PERMISSIONS = {
                        "android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"};
                ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
            }
        });
        //?????????
        TextView text_open_price = view.findViewById(R.id.text_open_price);
        text_open_price.setText(String.valueOf(opPrice));
        //?????????
        TextView text_close_price = view.findViewById(R.id.text_close_price);
        text_close_price.setText(String.valueOf(cpPrice));
        TextView text_lever = view.findViewById(R.id.text_lever);
        if (dataBean.isIsBuy()) {
            text_lever.setText(activity.getString(R.string.text_much) + lever + "??");
        } else {
            text_lever.setText(activity.getString(R.string.text_empty) + lever + "??");

        }
        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        TextView text_rate = view.findViewById(R.id.text_rate);
        text_rate.setText(TradeUtil.ratio(income, margin));

        if (income > 0) {
            text_rate.setTextColor(activity.getResources().getColor(R.color.text_quote_green));

        } else {
            text_rate.setTextColor(activity.getResources().getColor(R.color.text_quote_red));

        }

        topList.add(activity.getString(R.string.text_wonderful_unrivalled));
        topList.add(activity.getString(R.string.text_king));
        topList.add(activity.getString(R.string.text_awesome));

        midList.add(activity.getString(R.string.text_set_goal));
        midList.add(activity.getString(R.string.text_one_step));
        midList.add(activity.getString(R.string.text_get_less));

        lowList.add(activity.getString(R.string.text_stop_loss));
        lowList.add(activity.getString(R.string.text_do_hesitate));
        lowList.add(activity.getString(R.string.text_just_case));


        Random random = new Random();
        int top = random.nextInt(topList.size());
        int mid = random.nextInt(midList.size());
        int low = random.nextInt(lowList.size());


        TextView text_title = view.findViewById(R.id.text_title);

        ImageView img_person = view.findViewById(R.id.img_person);
        double rate = TradeUtil.ratioDouble(income, margin);
        if (rate >= 0 && rate < 100) {
            text_title.setText(midList.get(mid));
            img_person.setImageDrawable(activity.getResources().getDrawable(R.mipmap.icon_win));
        } else if (rate >= 100) {
            text_title.setText(topList.get(top));
            img_person.setImageDrawable(activity.getResources().getDrawable(R.mipmap.icon_win));

        } else if (rate < 0) {
            text_title.setText(lowList.get(low));
            img_person.setImageDrawable(activity.getResources().getDrawable(R.mipmap.icon_loss));

        }
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(300);

        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);

        popResult.setResult(layout_share, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(layout_view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        view.startAnimation(animation);


    }

    /*??????????????????*/
    public void showPositionShare(Activity activity, View layout_view, PositionEntity.DataBean dataBean, PopResult popResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_share_pop, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout layout_share = view.findViewById(R.id.layout_share);


        double opPrice = dataBean.getOpPrice();
        double cpPrice = dataBean.getCpPrice();
        //?????????
        double lever = dataBean.getLever();
        double margin = dataBean.getMargin();
        double income = dataBean.getIncome();


        TextView text_name = view.findViewById(R.id.text_name);
        String[] split = Util.quoteList(dataBean.getContractCode()).split(",");
        text_name.setText(split[0]);
        TextView text_currency = view.findViewById(R.id.text_currency);
        text_currency.setText("/" + dataBean.getCurrency());

        TextView text_save = view.findViewById(R.id.text_save);

        TextView text_recommend_code = view.findViewById(R.id.text_recommend_code);

        UserDetailEntity userDetailEntity = SPUtils.getData(AppConfig.DETAIL, UserDetailEntity.class);
        if (userDetailEntity != null) {
            text_recommend_code.setText(userDetailEntity.getUser().getRefer());
        }

        text_save.setOnClickListener(v -> {
            if (PermissionUtil.readAndWrite(activity)) {
                ImageUtil.SaveBitmapFromView(activity, layout_share);
                Toast.makeText(activity, activity.getResources().getString(R.string.text_success), Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            } else {
                String[] PERMISSIONS = {
                        "android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"};
                ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
            }
        });
        //?????????
        TextView text_open_price = view.findViewById(R.id.text_open_price);
        text_open_price.setText(String.valueOf(opPrice));
        //?????????
        TextView text_close_price = view.findViewById(R.id.text_close_price);
        text_close_price.setText(String.valueOf(cpPrice));
        TextView text_lever = view.findViewById(R.id.text_lever);
        if (dataBean.isIsBuy()) {
            text_lever.setText(activity.getString(R.string.text_much) + lever + "??");
        } else {
            text_lever.setText(activity.getString(R.string.text_empty) + lever + "??");

        }
        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        TextView text_rate = view.findViewById(R.id.text_rate);
        text_rate.setText(TradeUtil.ratio(income, margin));

        if (income > 0) {
            text_rate.setTextColor(activity.getResources().getColor(R.color.text_quote_green));

        } else {
            text_rate.setTextColor(activity.getResources().getColor(R.color.text_quote_red));

        }

        topList.add(activity.getString(R.string.text_wonderful_unrivalled));
        topList.add(activity.getString(R.string.text_king));
        topList.add(activity.getString(R.string.text_awesome));

        midList.add(activity.getString(R.string.text_set_goal));
        midList.add(activity.getString(R.string.text_one_step));
        midList.add(activity.getString(R.string.text_get_less));

        lowList.add(activity.getString(R.string.text_stop_loss));
        lowList.add(activity.getString(R.string.text_do_hesitate));
        lowList.add(activity.getString(R.string.text_just_case));


        Random random = new Random();
        int top = random.nextInt(topList.size());
        int mid = random.nextInt(midList.size());
        int low = random.nextInt(lowList.size());


        TextView text_title = view.findViewById(R.id.text_title);

        ImageView img_person = view.findViewById(R.id.img_person);
        double rate = TradeUtil.ratioDouble(income, margin);
        if (rate >= 0 && rate < 100) {
            text_title.setText(midList.get(mid));
            img_person.setImageDrawable(activity.getResources().getDrawable(R.mipmap.icon_win));
        } else if (rate >= 100) {
            text_title.setText(topList.get(top));
            img_person.setImageDrawable(activity.getResources().getDrawable(R.mipmap.icon_win));

        } else if (rate < 0) {
            text_title.setText(lowList.get(low));
            img_person.setImageDrawable(activity.getResources().getDrawable(R.mipmap.icon_loss));

        }
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(300);

        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);

        popResult.setResult(layout_share, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(layout_view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        view.startAnimation(animation);


    }

    /*????????????*/
    /*public void showSharePlatform(Activity activity, View layout_view, HistoryEntity.DataBean dataBean) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.layout_share_pop, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        *//*??????????????????*//*
        showShare(activity, layout_view, dataBean, (response1, repose2) -> {
            PopupWindow popupWindow1 = (PopupWindow) repose2;


            view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
                popupWindow.dismiss();
                popupWindow1.dismiss();
            });

            //????????????
            view.findViewById(R.id.text_save).setOnClickListener(v -> {

                if (PermissionUtil.readAndWrite(activity)) {
                    View view1 = (View) response1;
                    ImageUtil.SaveBitmapFromView(activity, view1);
                    Toast.makeText(activity, activity.getResources().getString(R.string.text_save), Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                    popupWindow1.dismiss();
                } else {
                    String[] PERMISSIONS = {
                            "android.permission.READ_EXTERNAL_STORAGE",
                            "android.permission.WRITE_EXTERNAL_STORAGE"};
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
                }


            });

            //??????????????????
            view.findViewById(R.id.text_friend);
            view.setOnClickListener(v -> {
                ShareUtil.shareImg(ImageUtil.getBitmap((View) response1), Wechat.Name, response ->
                        Log.d("jiguang", "setResult:292:  " + response));
            });
        });


        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(300);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.setFocusable(true);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(layout_view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        view.startAnimation(animation);
    }*/

    public void dialogUp(Activity activity, View layout_view, String versionMessage, String url) {
        Util.lightOff(activity);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.item_tip_pop_update_layout, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        TextView text_content = view.findViewById(R.id.text_content);

        text_content.setText(versionMessage);

        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        view.findViewById(R.id.text_sure).setOnClickListener(v -> {
            //??????????????????
            ProgressDialog progressDialog;
            progressDialog = new ProgressDialog(activity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
            getFile(activity, url, (state, response) -> {
                if (state.equals(BUSY)) {
                    progressDialog.setMessage(activity.getString(R.string.text_progressing));
                } else if (state.equals(SUCCESS)) {
                    progressDialog.setMessage(activity.getResources().getText(R.string.text_success));
                    progressDialog.dismiss();
                } else if (state.equals(FAILURE)) {
                    progressDialog.setMessage(activity.getResources().getText(R.string.text_failure));
                    progressDialog.dismiss();
                }
            }); //??????apk

            popupWindow.dismiss();
        });

        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // popupWindow.setAnimationStyle(R.style.pop_anim_quote);
        popupWindow.setContentView(view);
        popupWindow.showAtLocation(layout_view, Gravity.CENTER, 0, 0);






      /*  AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
//		alertDialogBuilder.setIcon(R.drawable.)
        alertDialogBuilder.setTitle("????????????");
        alertDialogBuilder.setMessage(versionMessage);
        alertDialogBuilder.setPositiveButton(activity.getResources().getText(R.string.text_sure), (dialog, which) -> {

            //??????????????????
            ProgressDialog progressDialog;
            progressDialog = new ProgressDialog(activity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
            getFile(activity, url, (state, response) -> {
                if (state.equals(BUSY)) {
                    progressDialog.setMessage("???????????????~");
                } else if (state.equals(SUCCESS)) {
                    progressDialog.setMessage("????????????~");
                    progressDialog.dismiss();

                } else if (state.equals(FAILURE)) {
                    progressDialog.setMessage("????????????~");
                    progressDialog.dismiss();
                }
            }); //??????apk
        });
        alertDialogBuilder.setNegativeButton(activity.getResources().getText(R.string.text_cancel_position), (dialog, which) -> {

        });

        alertDialogBuilder.setCancelable(false);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();//???dialog????????????*/
    }


    public void getFile(Activity activity, String versionUrl, OnNetResult onNetResult) {

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download";
        final String fileName = "bityard.apk";
        final File file = new File(filePath + "/" + fileName);
        if (file.exists()) {
            file.delete();
        }
        OkGo.<File>get(versionUrl)
                .tag(this)
                .execute(new FileCallback(filePath, fileName) {
                    @Override
                    public void onStart(Request<File, ? extends Request> request) {
                        super.onStart(request);
                        onNetResult.onNetResult(BUSY, "start");
                        Log.e("MainActivity", "????????????");
                    }

                    @Override
                    public void onSuccess(Response<File> response) {
                        onNetResult.onNetResult(SUCCESS, "success");

                        Log.e("MainActivity", "file--" + file);
                        openFile(activity, file);  //??????apk
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        onNetResult.onNetResult(FAILURE, "failure");
                        Log.e("MainActivity", "onError: " + response.message());
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        Log.e("MainActivity", "progress" + progress.fraction * 100);
                    }
                });
    }

    private void openFile(Activity activity, File file) {

        //Android 7.0?????????
        if (Build.VERSION.SDK_INT >= 24) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                boolean hasInstallPermission = activity.getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    //???????????????????????????????????????
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 6666);
                }
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            activity.startActivity(intent);
        } else {
            Log.e("MainActivity", "??????24");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            activity.startActivity(intent);
        }
    }


    public static void showQuotePopWindow(Activity activity, View layout_view, ArrayMap<String, List<String>> arrayMap, String type,
                                          PopQuotesResult popQuotesResult) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.quote_market, null);
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popQuotesResult.setPopView(view);

        List<String> titleList = new ArrayList<>();
        titleList.add(activity.getString(R.string.text_optional));
        titleList.add(activity.getString(R.string.text_contract));
        titleList.add(activity.getString(R.string.text_spot));


        LinearLayout layout_optional_select_pop = view.findViewById(R.id.layout_optional_select_pop);

        RecyclerView recyclerView_optional_select_pop = view.findViewById(R.id.recyclerView_optional_pop);

        LinearLayout layout_null_pop = view.findViewById(R.id.layout_null);

        OptionalSelectAdapter optionalSelectAdapter = new OptionalSelectAdapter(activity);
        recyclerView_optional_select_pop.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        recyclerView_optional_select_pop.setAdapter(optionalSelectAdapter);
        List<String> optionalTitleList = new ArrayList<>();
        optionalTitleList.add(activity.getString(R.string.text_contract));
        optionalTitleList.add(activity.getString(R.string.text_spot));
        optionalSelectAdapter.setDatas(optionalTitleList);
        optionalSelectAdapter.select(activity.getString(R.string.text_contract));
        optionalSelectAdapter.setEnable(true);


        TabLayout tabLayout_market_search = view.findViewById(R.id.tabLayout_market_search);

        LinearLayout layout_null = view.findViewById(R.id.layout_null);

        RecyclerView recyclerView_market = view.findViewById(R.id.recyclerView_market);

        QuoteAdapter quoteAdapter_market_pop = new QuoteAdapter(activity);
        recyclerView_market.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView_market.setAdapter(quoteAdapter_market_pop);
        List<String> quoteList = arrayMap.get(type);
        quoteAdapter_market_pop.setDatas(quoteList);
        quoteAdapter_market_pop.isShowIcon(false);
        ImageView img_price_triangle = view.findViewById(R.id.img_price_triangle);

        ImageView img_rate_triangle = view.findViewById(R.id.img_rate_triangle);

        ImageView img_name_triangle = view.findViewById(R.id.img_name_triangle);

        /*???????????????*/
        optionalSelectAdapter.setOnItemClick((position, data) -> {
            optionalSelectAdapter.select(data);
            switch (position) {
                case 0:

                    popQuotesResult.setOptionalResult(0);
                    List<String> quoteList1 = arrayMap.get(type);
                    if (quoteList1 == null) {
                        layout_null_pop.setVisibility(View.VISIBLE);
                        recyclerView_optional_select_pop.setVisibility(View.GONE);
                    } else {
                        layout_null_pop.setVisibility(View.GONE);
                        recyclerView_optional_select_pop.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList1);
                    }
                    img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    break;
                case 1:
                    popQuotesResult.setOptionalResult(1);
                    List<String> quoteList2 = arrayMap.get(type);
                    if (quoteList2 == null) {
                        layout_null_pop.setVisibility(View.VISIBLE);
                        recyclerView_optional_select_pop.setVisibility(View.GONE);
                    } else {
                        layout_null_pop.setVisibility(View.GONE);
                        recyclerView_optional_select_pop.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList2);
                    }
                    img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    break;
                case 2:
                    popQuotesResult.setOptionalResult(2);

                   List<String> quoteList3 = arrayMap.get(type);
                    if (quoteList3 == null) {
                        layout_null_pop.setVisibility(View.VISIBLE);
                        recyclerView_optional_select_pop.setVisibility(View.GONE);
                    } else {
                        layout_null_pop.setVisibility(View.GONE);
                        recyclerView_optional_select_pop.setVisibility(View.VISIBLE);
                        quoteAdapter_market_pop.setDatas(quoteList3);
                    }

                    img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
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
            popQuotesResult.setClickListenerResult("price");
            img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
            img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
        });
        view.findViewById(R.id.layout_up_down).setOnClickListener(v -> {
            if (arrayMap == null) {
                return;
            }
            popQuotesResult.setClickListenerResult("range");
            img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
            img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
        });
        view.findViewById(R.id.layout_name).setOnClickListener(v -> {
            if (arrayMap == null) {
                return;
            }
            popQuotesResult.setClickListenerResult("name");
            img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
            img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
        });

        tabLayout_market_search.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //????????????????????????
                popQuotesResult.setClickListenerResult(null);
                if (arrayMap == null) {
                    return;
                }
                //??????
                if (tab.getPosition() == 0) {
                    popQuotesResult.setTabSelectResult(0);

                    layout_optional_select_pop.setVisibility(View.VISIBLE);
                    img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                }//??????
                else if (tab.getPosition() == 1) {
                    layout_optional_select_pop.setVisibility(View.GONE);
                    popQuotesResult.setTabSelectResult(1);

                    img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                } else if (tab.getPosition() == 2) {
                    layout_optional_select_pop.setVisibility(View.GONE);
                    popQuotesResult.setTabSelectResult(2);


                    img_rate_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_name_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
                    img_price_triangle.setImageDrawable(activity.getResources().getDrawable(R.mipmap.market_up_down));
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
        swipeRefreshLayout_market.setColorSchemeColors(activity.getResources().getColor(R.color.maincolor));
        /*????????????*/
        swipeRefreshLayout_market.setOnRefreshListener(() -> {
            popQuotesResult.setRefreshResult();
            swipeRefreshLayout_market.setRefreshing(false);

        });

        quoteAdapter_market_pop.setOnItemClick(data -> {
            popQuotesResult.setPopClickResult(data);

            //????????????
            popupWindow.dismiss();


        });


        view.findViewById(R.id.text_cancel).setOnClickListener(v -> {
            popQuotesResult.setCancelResult(type);
            tabLayout_market_search.getTabAt(AppConfig.selectPosition).select();
            popupWindow.dismiss();
        });

        RelativeLayout layout_bar = view.findViewById(R.id.layout_bar);

        EditText edit_search = view.findViewById(R.id.edit_search);

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
                    List<String> strings = arrayMap.get(type);
                    List<String> searchQuoteList = TradeUtil.searchQuoteList(edit_search.getText().toString(), strings);
                    quoteAdapter_market_pop.setDatas(searchQuoteList);
                } else {
                    layout_bar.setVisibility(View.VISIBLE);
                    tabLayout_market_search.setVisibility(View.VISIBLE);
                    List<String> quoteList = arrayMap.get(type);
                    quoteAdapter_market_pop.setDatas(quoteList);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Util.dismiss(activity, popupWindow);
        Util.isShowing(activity, popupWindow);


        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
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
