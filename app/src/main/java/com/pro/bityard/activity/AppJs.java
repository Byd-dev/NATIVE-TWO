package com.pro.bityard.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.pro.bityard.R;
import com.pro.bityard.api.NetManger;
import com.pro.bityard.config.AppConfig;
import com.pro.bityard.config.IntentConfig;
import com.pro.bityard.entity.LoginEntity;
import com.pro.bityard.utils.DeviceUtil;
import com.pro.switchlibrary.SPUtils;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import static com.pro.bityard.base.BaseActivity.isLogin;

/**
 * 客服端和h5交互代码
 */
public class AppJs {

    private static final String TAG = "AppJs";
    private static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_BANKCARD = 111;

    private Activity activity;
    private WebView webView;

    public AppJs(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }


    /**
     * 打开Android手机应用市场，让用户对app进行下载
     *
     * @author
     * @version 1
     */
    @JavascriptInterface
    public void openAppMarket() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + activity.getPackageName()));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "未找到任何应用市场", Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void postMessage(String key) {

    }

    @JavascriptInterface
    public void openWeChat() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            this.activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.activity, "检测到您手机没有安装微信,请安装后使用该功能", Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void toast(String message) {
        Toast.makeText(this.activity, message, Toast.LENGTH_SHORT).show();
    }

    /*跳转携带参数*/
    @JavascriptInterface
    public void openPageParam(String pageName, String data) {
        switch (pageName) {
            case IntentConfig.Keys.KEY_TRADE_LIVE://实盘交易
                TradeTabActivity.enter(activity, "1", data);
                break;
            case IntentConfig.Keys.KEY_TRADE_DEMO://模拟交易
                TradeTabActivity.enter(activity, "2", data);
                break;
        }
    }

    /*跳转页面不携带参数*/
    @JavascriptInterface
    public void openPage(String pageName) {
        LoginEntity loginEntity = SPUtils.getData(AppConfig.LOGIN, LoginEntity.class);
        String language = SPUtils.getString(AppConfig.KEY_LANGUAGE, AppConfig.ZH_SIMPLE);

        switch (pageName) {
            case IntentConfig.Keys.KEY_TRADE_LIVE://实盘交易
                TradeTabActivity.enter(activity, "1", "BTCUSDT1808,-1,11786.41,11803.59,11786.26,0.0678,11786.52,0.0678,11827.04,11658.68");
                break;
            case IntentConfig.Keys.KEY_TRADE_DEMO://模拟交易
                TradeTabActivity.enter(activity, "2", "BTCUSDT1808,-1,11786.41,11803.59,11786.26,0.0678,11786.52,0.0678,11827.04,11658.68");
                break;
            case IntentConfig.Keys.KEY_LOGIN://登录
                LoginActivity.enter(activity, IntentConfig.Keys.KEY_LOGIN);
                break;
            case IntentConfig.Keys.KEY_SET_UP://系统设置
                UserActivity.enter(activity, IntentConfig.Keys.KEY_SET_UP);
                break;
            case IntentConfig.Keys.KEY_LANGUAGE://语言设置
                UserActivity.enter(activity, IntentConfig.Keys.KEY_LANGUAGE);

                break;
            case IntentConfig.Keys.KEY_EXCHANGE_RATE://汇率设置
                UserActivity.enter(activity, IntentConfig.Keys.KEY_EXCHANGE_RATE);
                break;
            case IntentConfig.Keys.KEY_FUND_STATEMENT://资金记录
                UserActivity.enter(activity, IntentConfig.Keys.KEY_FUND_STATEMENT);
                break;
            case IntentConfig.Keys.KEY_TRADE_HISTORY://交易记录
                UserActivity.enter(activity, IntentConfig.Keys.KEY_TRADE_HISTORY);
                break;
            case IntentConfig.Keys.KEY_INVITE_HISTORY://邀请记录
                UserActivity.enter(activity, IntentConfig.Keys.KEY_INVITE_HISTORY);
                break;
            case IntentConfig.Keys.KEY_TRADE_SETTINGS://交易设置
                UserActivity.enter(activity, IntentConfig.Keys.KEY_TRADE_SETTINGS);
                break;
            case IntentConfig.Keys.KEY_WITHDRAWAL_ADDRESS://提币地址管理
                UserActivity.enter(activity, IntentConfig.Keys.KEY_WITHDRAWAL_ADDRESS);
                break;
            case IntentConfig.Keys.KEY_ADD_ADDRESS://安全中心
                UserActivity.enter(activity, IntentConfig.Keys.KEY_ADD_ADDRESS);
                break;
            case IntentConfig.Keys.KEY_ANNOUNCEMENT://最新公告
                UserActivity.enter(activity, IntentConfig.Keys.KEY_ANNOUNCEMENT);
                break;
            case IntentConfig.Keys.KEY_ACCOUNT://资金账户
                UserActivity.enter(activity, IntentConfig.Keys.KEY_ACCOUNT);
                break;
            case IntentConfig.Keys.KEY_WITHDRAWAL://提币
                UserActivity.enter(activity, IntentConfig.Keys.KEY_WITHDRAWAL);
                break;
            case IntentConfig.Keys.KEY_QUICK_EXCHANGE://币币闪兑
                UserActivity.enter(activity, IntentConfig.Keys.KEY_QUICK_EXCHANGE);
                break;
            case IntentConfig.Keys.KEY_SAFE_CENTER://安全中心
                UserActivity.enter(activity, IntentConfig.Keys.KEY_SAFE_CENTER);
                break;
            case IntentConfig.Keys.KEY_DEPOSIT://充币
                if (isLogin()) {
                    WebActivity.getInstance().openUrl(activity, NetManger.getInstance().h5Url(loginEntity.getAccess_token(), null, "/deposit"),
                            activity.getResources().getString(R.string.text_recharge));
                } else {
                    LoginActivity.enter(activity, IntentConfig.Keys.KEY_LOGIN);
                }
                break;
            case IntentConfig.Keys.KEY_HOLD://持仓
                UserActivity.enter(activity, IntentConfig.Keys.KEY_HOLD);
                break;
            case IntentConfig.Keys.KEY_FIAT://法币充值
                String url_api = null;
                switch (language) {
                    case AppConfig.ZH_SIMPLE:
                        url_api = "/cnRecharge";
                        break;
                    case AppConfig.VI_VN:
                        url_api = "/viRecharge";
                        break;
                    case AppConfig.IN_ID:
                        url_api = "/idRecharge";
                        break;
                }
                if (isLogin()) {
                    WebActivity.getInstance().openUrl(activity, NetManger.getInstance().h5Url(loginEntity.getAccess_token(), null, url_api),
                            activity.getResources().getString(R.string.text_fabi_trade));
                } else {
                    LoginActivity.enter(activity, IntentConfig.Keys.KEY_LOGIN);
                }
                break;
            case IntentConfig.Keys.KEY_SAFE_CENTER_LOGIN_PASS://修改登录密码
                UserActivity.enter(activity, IntentConfig.Keys.KEY_SAFE_CENTER_LOGIN_PASS);
                break;
            case IntentConfig.Keys.KEY_SAFE_CENTER_FUNDS_PASS://设置资金密码和修改资金密码
                UserActivity.enter(activity, IntentConfig.Keys.KEY_SAFE_CENTER_FUNDS_PASS);
                break;
            case IntentConfig.Keys.KEY_SAFE_CENTER_BIND_CHANGE_MOBILE://绑定手机号和修改手机号
                UserActivity.enter(activity, IntentConfig.Keys.KEY_SAFE_CENTER_BIND_CHANGE_MOBILE);
                break;
            case IntentConfig.Keys.KEY_SAFE_CENTER_BIND_CHANGE_EMAIL://绑定邮箱和修改邮箱
                UserActivity.enter(activity, IntentConfig.Keys.KEY_SAFE_CENTER_BIND_CHANGE_EMAIL);
                break;
            case IntentConfig.Keys.KEY_THEME://主题设置
                UserActivity.enter(activity, IntentConfig.Keys.KEY_THEME);
                break;
            case IntentConfig.Keys.KEY_REGISTER:
                RegisterActivity.enter(activity, IntentConfig.Keys.KEY_REGISTER);
                break;
            case IntentConfig.Keys.KEY_FORGET:
                UserActivity.enter(activity, IntentConfig.Keys.KEY_FORGET);
                break;
            case IntentConfig.Keys.RULE:
                UserActivity.enter(activity, IntentConfig.Keys.RULE, "");
                break;
            case IntentConfig.Keys.KEY_SERVICE:
                String url;
                if (isLogin()) {
                    url = String.format(NetManger.SERVICE_URL, language, loginEntity.getUser().getUserId(), loginEntity.getUser().getAccount());
                } else {
                    url = String.format(NetManger.SERVICE_URL, language, "", "游客");
                }
                WebActivity.getInstance().openUrl(activity, url, activity.getResources().getString(R.string.text_my_service));
                break;

        }
    }


    @JavascriptInterface
    public void hello(String msg) {
        Toast.makeText(activity, "调用了方法 hello", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String GetDeviceInfo() {
        String phoneInfo = DeviceUtil.getPhoneInfo(activity);
        Log.d(TAG, "GetDeviceInfo:68:  " + phoneInfo);
        return phoneInfo;
    }


    //前端可以凭此属性判断内容是否是使用移动端打开的
    @JavascriptInterface
    public String isSuperman() {
        return "true";
    }

    //判断当前手机性能
    @JavascriptInterface
    public String isPerformance() {
        return DeviceUtil.isPerformance(activity);
    }

    @JavascriptInterface
    public void LinkTo(String url) {
        Uri content_url = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, content_url);
        activity.startActivity(intent);
    }


    //指纹识别
    @JavascriptInterface
    public boolean TouchIDSupport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final FingerprintManagerCompat fingerprint = FingerprintManagerCompat.from(activity);   //v4包下的API，包装内部已经判断Android系统版本是否大于6.0，这也是官方推荐的方式
            return fingerprint.hasEnrolledFingerprints();
        } else {
            return false;
        }
    }


    @JavascriptInterface
    public void TouchIDAuthenticate() {
        DeviceUtil.TouchIDAuthenticate(activity, webView);
    }


    /**
     * 结束当前页面
     *
     * @author
     */
    @JavascriptInterface
    public void finishActivity() {
        activity.finish();
    }

    /**
     * 提供h5直接拨打电话
     *
     * @param number
     */
    @JavascriptInterface
    public void call(String number) {
        Uri uri = Uri.parse("tel:" + number);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        activity.startActivity(intent);
    }


}
