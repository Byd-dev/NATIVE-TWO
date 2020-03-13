package com.pro.bityard.api;


import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.pro.bityard.R;
import com.pro.bityard.activity.MainActivity;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.entity.InitEntity;
import com.pro.bityard.entity.TradeListEntity;
import com.pro.bityard.utils.Util;
import com.pro.switchlibrary.AES;
import com.pro.switchlibrary.PhotoUtils;
import com.pro.switchlibrary.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.core.app.NavUtils;

public class NetManger {

    public static NetManger instance;

    public static String BUSY = "busy";
    public static String SUCCESS = "success";
    public static String FAILURE = "failure";

     public static String BASE_URL = "http://test.bityard.com";   //测试

   // public static String BASE_URL = "https://www.bityard.com";    //正式

    public static NetManger getInstance() {
        if (instance == null) {
            instance = new NetManger();
        }

        return instance;
    }

    //get 请求
    public void getRequest(String url, ArrayMap map, OnNetResult onNetResult) {
        Log.d("print ", "getRequest:get请求地址:  " + getURL(url, map));
        OkGo.<String>get(getURL(url, map))
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        onNetResult.onNetResult(BUSY, null);

                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        if (!TextUtils.isEmpty(response.body())) {
                            onNetResult.onNetResult(SUCCESS, response.body());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        onNetResult.onNetResult(FAILURE, response.body());
                    }
                });

    }


    //post 请求
    public void postRequest(String url, ArrayMap map, OnNetResult onNetResult) {

        OkGo.<String>post(getURL(url, map))
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        onNetResult.onNetResult(BUSY, null);

                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        if (!TextUtils.isEmpty(response.body())) {
                            onNetResult.onNetResult(SUCCESS, response.body());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        onNetResult.onNetResult(FAILURE, response.body());
                    }
                });

    }


    //动态host get 请求
    public void getHostRequest(String host, String url, ArrayMap map, OnNetResult onNetResult) {
        Log.d("print", "getHostRequest:动态:  " + getHostURL(host, url, map));
        OkGo.<String>get(getHostURL(host, url, map))
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        onNetResult.onNetResult(BUSY, null);

                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        if (!TextUtils.isEmpty(response.body())) {
                            onNetResult.onNetResult(SUCCESS, response.body());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        onNetResult.onNetResult(FAILURE, response.body());
                    }
                });

    }


    //URL拼接参数
    public String getURL(String url, ArrayMap map) {
        Log.d("print", "getURL:参数:  " + map);

        String substring_url = null;
        if (map == null) {
            return BASE_URL + url;
        } else {
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                StringBuilder append = stringBuilder.append(key).append("=").append(value).append("&");
                substring_url = append.toString().substring(0, append.toString().length() - 1);
            }
            String url_result = BASE_URL + url + "?" + substring_url;
            return url_result;
        }


    }

    //动态URL拼接参数
    public String getHostURL(String host, String url, ArrayMap map) {
        Log.d("print", "getURL:HOST参数:  " + map);

        String substring_url = null;
        if (map == null) {
            return host + url;
        } else {
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                StringBuilder append = stringBuilder.append(key).append("=").append(value).append("&");
                substring_url = append.toString().substring(0, append.toString().length() - 1);
            }
            String url_result = host + url + "?" + substring_url;
            return url_result;
        }


    }

    /*行情地址host 合约号 合约号详情 的方法  关键*/
    public void getHostCodeTradeList(OnNetThreeResult onNetThreeResult) {

        initURL(new OnNetHostResult() {
            @Override
            public void setResult(String state, Object response1, Object response2) {
                if (state.equals(BUSY)) {
                    onNetThreeResult.setResult(BUSY, null, null, null);

                } else if (state.equals(SUCCESS)) {

                    List<TradeListEntity> tradeListEntityList = (List<TradeListEntity>) response2;
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < tradeListEntityList.size(); i++) {
                        stringBuilder.append(tradeListEntityList.get(i).getContractCode() + ",");
                    }
                    onNetThreeResult.setResult(SUCCESS, response1, stringBuilder.toString(), tradeListEntityList);

                } else if (state.equals(FAILURE)) {
                    onNetThreeResult.setResult(FAILURE, null, null, null);

                }
            }
        });
    }

    //行情init初始化
    public void initURL(OnNetHostResult onNetResult) {
        /*获取行情的host*/
        getRequest("/api/trade/commodity/initial", null, new OnNetResult() {
            @Override
            public void onNetResult(String state, Object response) {
                if (state.equals(BUSY)) {
                } else if (state.equals(SUCCESS)) {
                    InitEntity initEntity = new Gson().fromJson(response.toString(), InitEntity.class);
                    List<InitEntity.GroupBean> group = initEntity.getGroup();
                    // TODO: 2020/3/13 暂时这里只固定是数字货币的遍历 
                    for (InitEntity.GroupBean data : group) {
                        if (data.getName().equals("数字货币")) {
                            String list = data.getList();
                            getTradeList(list, new OnNetResult() {
                                @Override
                                public void onNetResult(String state, Object response) {
                                    if (state.equals(BUSY)) {

                                    } else if (state.equals(SUCCESS)) {
                                        String quoteDomain = initEntity.getQuoteDomain();//获取域名
                                        onNetResult.setResult(SUCCESS, quoteDomain, response);

                                    } else if (state.equals(FAILURE)) {
                                        onNetResult.setResult(FAILURE, null, response);
                                    }
                                }
                            });//获取合约号
                        }
                    }
                } else if (state.equals(FAILURE)) {
                    onNetResult.setResult(FAILURE, null, null);
                }
            }
        });
    }


    private List<TradeListEntity> tradeListEntityList;

    /*获取合约号*/
    private void getTradeList(String codeList, OnNetResult onNetResult) {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("code", codeList);
        String[] codeSplitList = codeList.split(";");
        getRequest("/api/trade/commodity/tradeList", map, new OnNetResult() {

            private TradeListEntity tradeListEntity;

            @Override
            public void onNetResult(String state, Object response) {
                if (state.equals(BUSY)) {
                } else if (state.equals(SUCCESS)) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());
                        JSONObject jsonObject1 = (JSONObject) jsonObject.get("data");
                        Log.d("print", "onNetResult:250:  " + jsonObject1.length());

                        Log.d("print", "onNetResult:252: " + codeSplitList.length);

                        tradeListEntityList = new ArrayList<>();
                        for (int i = 0; i < jsonObject1.length(); i++) {
                            for (int j = codeSplitList.length - 1; j > 0; j--) {
                                JSONObject trxusdt = (JSONObject) jsonObject1.get(codeSplitList[i]);  //trxusdt.length() =46
                                // Log.d("print", "onNetResult: 258:"+trxusdt.length());
                                tradeListEntity = new Gson().fromJson(trxusdt.toString(), TradeListEntity.class);
                                //  Log.d("print", "onNetResult:260: "+tradeListEntity);
                            }
                            tradeListEntityList.add(tradeListEntity);
                            // Log.d("print", "onNetResult:263:  "+tradeListEntityList.size());


                        }
                        onNetResult.onNetResult(SUCCESS, tradeListEntityList);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else if (state.equals(FAILURE)) {
                    onNetResult.onNetResult(FAILURE, "获取合约号失败");
                }
            }
        });
    }

    int count = 0;

    /*获取行情*/
    public void getQuote(String quoteDomain, String url, String codeList, OnNetResult onNetResult) {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("callback", "?");
        map.put("code", codeList);
        map.put("_", String.valueOf(new Date().getTime()));
        map.put("simple", "true");

        try {
            String urlList = AES.HexDecrypt(quoteDomain.getBytes(), AppConfig.S_KEY);
            Log.d("print", "setResult:294:  " + urlList);

            String[] split = urlList.split(";");
            int length = split.length;
            if (count < length) {
                getHostRequest(split[count], url, map, new OnNetResult() {
                    @Override
                    public void onNetResult(String state, Object response) {
                        if (state.equals(BUSY)) {

                        } else if (state.equals(SUCCESS)) {
                            onNetResult.onNetResult(SUCCESS, response.toString());
                        } else if (state.equals(FAILURE)) {
                            if (length == 0) {

                            } else {
                                count++;
                            }
                        }
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void initQuote() {
        /*初始化获取行情 合约号 行情地址*/
        getHostCodeTradeList(new OnNetThreeResult() {
            @Override
            public void setResult(String state, Object response1, Object response2, Object response3) {
                if (state.equals(SUCCESS)) {
                    SPUtils.putString(AppConfig.QUOTE_HOST, response1.toString());
                    SPUtils.putString(AppConfig.QUOTE_CODE, response2.toString());
                    SPUtils.putString(AppConfig.QUOTE_DETAIL, response3.toString());
                }
            }
        });
    }


}
