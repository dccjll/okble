package com.bluetoothle.util.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.bluetoothle.R;
import com.bluetoothle.util.log.LogUtil;
import com.yolanda.nohttp.FileBinary;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.download.DownloadListener;
import com.yolanda.nohttp.download.DownloadQueue;
import com.yolanda.nohttp.download.DownloadRequest;
import com.yolanda.nohttp.error.NetworkError;
import com.yolanda.nohttp.error.NotFoundCacheError;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.error.URLError;
import com.yolanda.nohttp.error.UnKnownHostError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class NoHttpUtil {

    private static final String TAG = NoHttpUtil.class.getSimpleName();
    private static RequestQueue mRequestQueue;
    private static DownloadQueue mDownloadQueue;
    private static NoHttpUtil mNoHttpInstance;
    private static final int HTTP_REQUEST_SUCCESS = 0x0001;
    private static final int HTTP_REQUEST_FAIL = 0x0000;
    private static Context context;

    private static final String USER_STATUS = "status";
    private static final String WHAT_VALUE = "what";
    private static final ArrayList<Map<String, Object>> mSignList = new ArrayList<>();
    private static final int maxRequestNumbers = 30;

    static {
        for (int i = 0; i < maxRequestNumbers; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put(USER_STATUS, false);
            map.put(WHAT_VALUE, 1000 + i);
            mSignList.add(map);
        }
    }

    private static synchronized int assignWhatValue() {
        int what = -1;
        for (int i = 0; i < mSignList.size(); i++) {
            Map<String, Object> map = mSignList.get(i);
            if (!((boolean) map.get(USER_STATUS))) {
                map.put(USER_STATUS, true);
                mSignList.set(i, map);
                what = (int) map.get(WHAT_VALUE);
                break;
            }
        }
        return what;
    }

    private static synchronized void recoverWhatValue(int what) {
        for (int i = 0; i < mSignList.size(); i++) {
            Map<String, Object> map = mSignList.get(i);
            if (what == (int) map.get(WHAT_VALUE)) {
                map.put(USER_STATUS, false);
                mSignList.set(i, map);
                break;
            }
        }
    }

    private NoHttpUtil() {
        getNoHttpRequestQueue();
        getNoHttpDownloadQueue();
    }

    public static NoHttpUtil getInstance(Context application) {
        if (mNoHttpInstance == null) {
            synchronized (NoHttpUtil.class) {
                if (mNoHttpInstance == null) {
                    context = application.getApplicationContext();
                    mNoHttpInstance = new NoHttpUtil();
                }
            }
        }
        return mNoHttpInstance;
    }

    private RequestQueue getNoHttpRequestQueue() {
        if (mRequestQueue == null) {
            synchronized (NoHttpUtil.class) {
                if (mRequestQueue == null) {
                    NoHttp.initialize(context);
                    mRequestQueue = NoHttp.newRequestQueue();
                }
            }
        }
        return mRequestQueue;
    }

    private DownloadQueue getNoHttpDownloadQueue() {
        if (mDownloadQueue == null) {
            synchronized (NoHttpUtil.class) {
                if (mDownloadQueue == null) {
                    NoHttp.initialize(context);
                    mDownloadQueue = NoHttp.newDownloadQueue();
                }
            }
        }
        return mDownloadQueue;
    }

    /**
     * 下载图片
     * 返回一个Bitmap对象
     */
    public void sendImageRequest(String url, final CommonResponseListener listener) {
        if (!checkNetworkAvailable(context)) {
            listener.onFinish(false, null, Log.INFO, context.getString(R.string.network_error));
            return;
        }
        Request<Bitmap> request = NoHttp.createImageRequest(url, RequestMethod.GET);
        if (TextUtils.isEmpty(url)) {
            LogUtil.e(TAG, "url is empty");
            return;
        }
        if (url.startsWith("https")) {
            //SSLContextUtil.doHttps(request);
        }
        LogUtil.i(TAG,"sendImageRequest url:"+url);
        mRequestQueue.add(assignWhatValue(), request, new OnResponseListener<Bitmap>() {
            @Override
            public void onStart(int what) {

            }

            @Override
            public void onSucceed(int what, Response<Bitmap> response) {
                List data = new ArrayList();
                data.add(response.get());
                listener.onFinish(true, data, Log.INFO, "success");
            }

            @Override
            public void onFailed(int what, Response<Bitmap> response) {
                LogUtil.e(TAG, "url=" + response.request().url() + "\nerror=" + response.get());
                listener.onFinish(false, null, Log.WARN, context.getString(R.string.network_error));
            }

            @Override
            public void onFinish(int what) {
                recoverWhatValue(what);
            }
        });
    }

    /**
     * 上传文件
     */
    public void uploadFile(final String url, Map<String, String> postMap, String fileKey, File postFile, final CommonResponseListener listener) {
        if (!checkNetworkAvailable(context)) {
            listener.onFinish(false, null, Log.INFO, context.getString(R.string.network_error));
            return;
        }
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.POST);
        request.add(postMap);
        request.add(fileKey, new FileBinary(postFile));
        if (url.startsWith("https")) {
            SSLContextUtil.doHttps(request, context);
        }
        mRequestQueue.add(assignWhatValue(), request, new OnResponseListener<String>() {
            @Override
            public void onStart(int what) {

            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                List data = new ArrayList();
                try {
                    JSONObject jsonObject = new JSONObject(response.get());
                    if (jsonObject.getInt("status") == 1) {
                        listener.onFinish(true, data, Log.INFO, jsonObject.getString("msg"));
                    } else {
                        LogUtil.e(TAG, "url=" + response.request().url() + "\n协议状态验证失败");
                        listener.onFinish(false, null, Log.WARN, jsonObject.getString("msg"));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "url=" + response.request().url() + "\nerror=" + e.getMessage());
                    listener.onFinish(false, null, Log.WARN, context.getString(R.string.network_error));
                }

            }

            @Override
            public void onFailed(int what, Response<String> response) {
                LogUtil.e(TAG, "url=" + response.request().url() + "\nerror=" + response.get());
                listener.onFinish(false, null, Log.WARN, context.getString(R.string.network_error));
            }

            @Override
            public void onFinish(int what) {

            }
        });
    }


    /**
     * 下载文件
     * 返回文件在本地的存储路径
     */
    public void download(String url, String fileFolder, String filename, final DownloadResponseListener listener) {
        if (!checkNetworkAvailable(context)) {
            listener.onFailure(context.getString(R.string.network_error), Log.INFO);
            return;
        }
        DownloadRequest request = NoHttp.createDownloadRequest(url, RequestMethod.GET, fileFolder, filename, true, true);
        if (url.startsWith("https")) {
            SSLContextUtil.doHttps(request, context);
        }
        mDownloadQueue.add(assignWhatValue(), request, new DownloadListener() {
            @Override
            public void onDownloadError(int what, final Exception exception) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailure(exception.getMessage(), Log.ERROR);
                    }
                });
            }

            @Override
            public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {

            }

            @Override
            public void onProgress(int what, final int progress, long fileCount) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onProgress(progress);
                    }
                });
            }

            @Override
            public void onFinish(final int what, final String filePath) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        recoverWhatValue(what);
                        listener.onSuccess(filePath);
                    }
                });
            }

            @Override
            public void onCancel(int what) {

            }
        });
    }

    public interface DownloadResponseListener {
        void onProgress(int progress);

        void onSuccess(String filePath);

        void onFailure(String msg, int loglever);
    }

    /**
     * 异步加密请求
     */
    public void asyncPostStringEncryptRequest(String url,Map<String, String> header, Map<String, String> map, CommonResponseListener listener) {
        asyncPostStringRequest(url, assignWhatValue(),header, map, listener, true);
    }

    /**
     * 异步不加密请求
     */
    public void asyncPostStringNoEncryptRequest(String url,Map<String, String> header, Map<String, String> map, CommonResponseListener listener) {
        asyncPostStringRequest(url, assignWhatValue(),header, map, listener, false);
    }

    /**
     * 异步请求总入口
     */
    private void asyncPostStringRequest(String url, int what,Map<String, String> header, Map<String, String> map, CommonResponseListener listener, boolean encryptFlag) {
        if (!checkNetworkAvailable(context)) {
            listener.onFinish(false, null, Log.INFO, context.getString(R.string.network_error));
            return;
        }
        // 取消队列中已开始的请求
        mRequestQueue.cancelBySign(what);

        // 创建请求对象
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.POST);
        // 设置请求失败后，重新尝试请求的次数
        request.setRetryCount(2);
//        request.setHeader("appVersion", SystemUtil.getAppCurrentVersion(context));
        if(header!=null){
            for (String key : header.keySet()) {
                request.setHeader(key, header.get(key));
            }
        }
        LogUtil.i(TAG,"header="+(header==null?"null":header.toString()));
        // 添加请求参数
        listener.setEncryptFlag(false);
        if (map == null) {
            map = new HashMap<>();
        }
        LogUtil.i(TAG, "接收到服务器请求\n请求链接:" + url + " \n请求参数:" + map + "\n是否加密:" + encryptFlag);
        if (encryptFlag) {
            listener.setEncryptFlag(true);
            map = EncryptUtil.encryptMap(map);
        }
        request.add(map);

        // 设置请求取消标志
        request.setCancelSign(what);
        //设置超时
        request.setConnectTimeout(10000);
        //客户端请求使用短连接
        request.setHeader("Connection", "close");
        //https加密
        if (url.startsWith("https")) {
            SSLContextUtil.doHttps(request, context);
        }
        // 向请求队列中添加请求
        // what: 当多个请求同时使用同一个OnResponseListener时，用来区分请求，类似Handler中的what
        mRequestQueue.add(what, request, listener);
    }

    /**
     * 异步请求响应结果回调对象
     */
    public abstract static class CommonResponseListener implements OnResponseListener<String> {

        Class clazz;
        boolean encryptFlag;

        public CommonResponseListener() {
        }

        public CommonResponseListener(Class clazz) {
            this.clazz = clazz;
        }

        void setEncryptFlag(boolean encryptFlag) {
            this.encryptFlag = encryptFlag;
        }

        @Override
        public void onSucceed(int what, Response<String> response) {
            int responseCode = response.getHeaders().getResponseCode();
            if (responseCode > 400) {
                LogUtil.e(TAG, "responseCode > 400\nresponseCode=" + responseCode + "\nurl=" + response.request().url());
                onFinish(false, null, Log.WARN, context.getString(R.string.network_error));
                return;
            }
            String resultString;

            if (encryptFlag) {
                resultString = EncryptUtil.deEncrypt(response.get());
            } else {
                resultString = response.get();
            }
            if (!response.request().url().contains("showStandardPageItem.action")) {
                LogUtil.i(TAG, "服务器请求结束，请求链接:" + response.request().url() + "\n服务器响应的数据:" + resultString);
            }
            JSONObject jsonResult;
            try {
                jsonResult = new JSONObject(resultString);
                int status = jsonResult.getInt("status");
                String data = jsonResult.getString("data");
                String msg = jsonResult.getString("msg");
                LogUtil.i(TAG, "status=" + status + "\ndata=" + data + "\nmsg=" + msg);
                if (status != HTTP_REQUEST_SUCCESS) {
                    LogUtil.i(TAG, "status=" + status + "\nurl=" + response.request().url());
                    onFinish(false, null, (-1 == status ? Log.INFO : Log.WARN), msg);
                    return;
                }
                List list = BeanUtil.antiSerializationJsonString(data, clazz);
                onFinish(true, list, Log.INFO, msg);
            } catch (JSONException e) {
                e.printStackTrace();
                LogUtil.e(TAG, "url=" + response.request().url() + " ,error=" + e.getMessage());
                onFinish(false, null, Log.WARN, context.getString(R.string.network_error));
            }
        }

        @Override
        public void onFailed(int what, Response<String> response) {
            Exception exception = response.getException();
            exception.printStackTrace();
            String errorMsg;
            if (exception instanceof NetworkError) {// 网络不好
                errorMsg = context.getString(R.string.network_error);
                LogUtil.e(TAG, "nohttp_network_error");
            } else if (exception instanceof TimeoutError) {// 请求超时
                errorMsg = context.getString(R.string.network_error);
                LogUtil.e(TAG, "nohttp_timeout_error");
            } else if (exception instanceof UnKnownHostError) {// 找不到服务器
                errorMsg = context.getString(R.string.network_error);
                LogUtil.e(TAG, "nohttp_unknownhost_error");
            } else if (exception instanceof URLError) {// URL是错的
                errorMsg = context.getString(R.string.network_error);
                LogUtil.e(TAG, "nohttp_url_error");
            } else if (exception instanceof NotFoundCacheError) {
                // 这个异常只会在仅仅查找缓存时没有找到缓存时返回
                errorMsg = context.getString(R.string.network_error);
                LogUtil.e(TAG, "nohttp_notfoundcache_error");
            } else if (exception instanceof ProtocolException) {
                errorMsg = context.getString(R.string.network_error);
                LogUtil.e(TAG, "nohttp_protocol_error");
            } else {
                errorMsg = context.getString(R.string.network_error);
                LogUtil.e(TAG, "nohttp_unknown_error");
            }
            onFinish(false, null, Log.WARN, errorMsg);
        }

        @Override
        public void onStart(int what) {

        }

        @Override
        public void onFinish(int what) {
            recoverWhatValue(what);
        }

        private void onFinish(final boolean state, final Object data, final int loglevel, final String msg) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (state) {
                        requestSuccess((List) data, msg);
                    } else {
                        requestFailed(msg, loglevel);
                    }
                }
            });
        }

        public abstract void requestSuccess(List data, String msg);

        public abstract void requestFailed(String error, int loglever);
    }

    public static boolean checkNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info != null) {
            for (NetworkInfo anInfo : info) {
                if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                    if (anInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        LogUtil.i(TAG, "当前是wifi网络环境");
                        return true;
                    } else if (anInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        LogUtil.i(TAG, "当前是移动网络环境");
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
