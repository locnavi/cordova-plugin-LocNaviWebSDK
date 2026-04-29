package com.locnavi.cordova.plugin.websdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.locnavi.websdk.LocNaviLocationService;
import com.locnavi.websdk.LocNaviWebSDK;
import com.locnavi.websdk.data.LocNaviConstants;
import com.orhanobut.logger.Logger;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class echoes a string called from JavaScript.
 */
public class LocNaviWebSDKPlugin extends CordovaPlugin {

    // 持久回调（用于持续推送定位结果）
    private CallbackContext locationCallbackContext;

    // 广播接收器
    private BroadcastReceiver locationReceiver;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("init")) {
            String appkey = args.getString(0);
            JSONObject options = args.length() > 1 ? args.getJSONObject(1) : new JSONObject();
            init(appkey, options, callbackContext);
            return true;
        }
        if (action.equals("showMap")) {
            String mapId = args.getString(0);
            showMap(mapId, callbackContext);
            return true;
        }
        if (action.equals("showMapWithParmas")) {
            String mapId = args.getString(0);
            String params = args.optString(1);
            showMapWithParmas(mapId, params, callbackContext);
            return true;
        }
        if (action.equals("naviTo")) {
            String mapId = args.getString(0);
            String targetId = args.optString(1);
            naviTo(mapId, targetId, callbackContext);
            return true;

        }
        if (action.equals("initLocationService")) {
            String mapId = args.getString(0);
            JSONObject options = args.getJSONObject(1);
            initLocationService(mapId, options, callbackContext);
            return true;
        }

        if (action.equals("startLocation")) {
            JSONObject options = args.getJSONObject(0);
            startLocation(options, callbackContext);
            return true;
        }

        if (action.equals("stopLocation")) {
            JSONObject options = args.getJSONObject(0);
            stopLocation(options, callbackContext);
            return true;
        }

        return false;
    }

    private void init(String appKey, JSONObject options, CallbackContext callbackContext) throws JSONException {
        LocNaviWebSDK.init(this.cordova.getActivity(), appKey);

        String userId = options.optString("userId");
        String serverUrl = options.optString("serverUrl");
        JSONArray uuids = options.optJSONArray("uuids");
        String uploadApi = options.optString("uploadApi");
        int uploadInterval = options.optInt("uploadInterval");
        if (userId != null) {
            LocNaviWebSDK.setUserId(userId);
        }
        if (serverUrl != null) {
            LocNaviWebSDK.setServerUrl(serverUrl);
        }
        if (uuids != null && uuids.length() > 0 ) {
            ArrayList list = new ArrayList();
            for (int i = 0; i < uuids.length(); i++) {
                list.add(uuids.getString(i));
            }
            String[] uuidList = (String[]) list.toArray(new String[list.size()]);
            LocNaviWebSDK.setUUIDs(uuidList);
        }
        if (uploadApi != null) {
            LocNaviWebSDK.setUploadLocationApi(uploadApi, uploadInterval);
        }
        callbackContext.success("init");
    }

    private void showMap(String mapId, CallbackContext callbackContext) {
        LocNaviWebSDK.openMap(this.cordova.getActivity(), mapId);
        callbackContext.success("showMap:" + mapId);
    }

    private void naviTo(String mapId, String targetId, CallbackContext callbackContext) {
        LocNaviWebSDK.openMap(this.cordova.getActivity(), mapId, targetId);
        callbackContext.success("naviTo:"+targetId);
    }

    private void showMapWithParmas(String mapId, String params, CallbackContext callbackContext) {
        LocNaviWebSDK.openMapWithParmas(this.cordova.getActivity(), mapId, params);
        callbackContext.success("showMapWithParmas:" + mapId + ", params:" + params);
    }

    private void initLocationService(String mapId, JSONObject options, CallbackContext callbackContext) {
        try {
            String serverUrl = options.optString("serverUrl");

            // 提前設定相關參數
            LocNaviWebSDK.setMapId(mapId);
            // 定位相關的 URL，一般情況下可不用設定
            LocNaviLocationService service = LocNaviLocationService.getInstanceForApplication(cordova.getActivity().getApplication());
            // 严格检查字符串有效性
            if (serverUrl != null && !serverUrl.trim().isEmpty() && !"null".equalsIgnoreCase(serverUrl)) {
                service.setServerURL(serverUrl);
                Logger.d("Set Server URL: " + serverUrl);
            }
            callbackContext.success("init locationService ok");
        } catch (Exception e) {
            callbackContext.error("init locationService error:" + e.getMessage());
        }
    }

    private void startLocation(JSONObject options, CallbackContext callbackContext) {
        try {
            int mode = options.optInt("mode", 0);
            boolean enableDetail = options.optBoolean("enableDetail", false);

            boolean isBackground = options.optBoolean("isBackground", false);
            String foregroundTitle = options.optString("foregroundTitle", "背景定位中");
            String foregroundContent = options.optString("foregroundContent", "詳細資訊");
            long foregroundScanPeriod = options.optLong("foregroundScanPeriod", -1);
            long foregroundBetweenScanPeriod = options.optLong("foregroundBetweenScanPeriod", -1);
            long backgroundScanPeriod = options.optLong("backgroundScanPeriod", -1);
            long backgroundBetweenScanPeriod = options.optLong("backgroundBetweenScanPeriod", -1);
            long sleepBackgroundScanPeriod = options.optLong("sleepBackgroundScanPeriod", -1);
            long sleepBackgroundBetweenScanPeriod = options.optLong("sleepBackgroundBetweenScanPeriod", -1);

            LocNaviLocationService service = LocNaviLocationService.getInstanceForApplication(cordova.getActivity().getApplication());
            // LocNaviLocationModeAuto = 0,
            // LocNaviLocationModeOnlyBeacon = 1,
            // LocNaviLocationModeOnlyGPS = 2,
            // LocNaviLocationModeGPSAndBeacon = 3
            // 開始定位
            String strMode = LocNaviConstants.LOCATION_MODE_AUTO;
            if (mode == 1) {
                strMode = LocNaviConstants.LOCATION_MODE_ONLY_BEACON;
            } else if (mode == 2) {
                strMode = LocNaviConstants.LOCATION_MODE_ONLY_GPS;
            } else if (mode == 3) {
                strMode = LocNaviConstants.LOCATION_MODE_GPS_AND_BEACON;
            }
            // 支援獲取更詳細的定位資訊
            service.start(strMode, enableDetail);
            // 可指定只開啟藍牙定位，暫未使用 GPS 定位，預設使用 LocNaviConstants.LOCATION_MODE_AUTO
            //service.start(LocNaviConstants.LOCATION_MODE_ONLY_BEACON);

            // 設定掃描時長及掃描間隔 (毫秒)，顯示 WebView 前建議改回正常的掃描間隔以免影響體驗。
            service.updateScanPeriods(foregroundScanPeriod, foregroundBetweenScanPeriod, backgroundScanPeriod, backgroundBetweenScanPeriod, sleepBackgroundScanPeriod, sleepBackgroundBetweenScanPeriod);


            // 背景定位功能
            if (isBackground) {
                service.setupForegroundService(cordova.getActivity().getApplicationInfo().icon, foregroundTitle, foregroundContent);
            }

            locationCallbackContext = callbackContext;
            // 注册广播监听
            registerLocationReceiver(cordova.getActivity().getApplicationContext());

            // 先回一个 keepCallback=true 的结果，告知 JS 启动成功
            // 后续每次定位结果会继续触发这个 callback
            PluginResult startResult = new PluginResult(
                    PluginResult.Status.OK, "started"
            );
            startResult.setKeepCallback(true); // ← 关键：保持回调不被销毁
            callbackContext.sendPluginResult(startResult);

            // 添加廣播監聽
            IntentFilter filter = new IntentFilter();
            filter.addAction("location");
            LocalBroadcastManager.getInstance(cordova.getActivity().getApplicationContext()).registerReceiver(locationReceiver, filter);

//            callbackContext.success("start location ok");
        } catch (Exception e) {
            callbackContext.error("start location error:" + e.getMessage());
        }
    }

    private void stopLocation(JSONObject options, CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            try {
                LocNaviLocationService service = LocNaviLocationService.getInstanceForApplication(cordova.getActivity().getApplication());
                if (service != null) {
                    // 停止定位
                    service.stop();
                    // 停止背景定位
                    service.stopForegroundService();
                }
                //取消廣播監聽
                unregisterLocationReceiver();

                // 关闭持久回调
                if (locationCallbackContext != null) {
                    PluginResult endResult = new PluginResult(
                            PluginResult.Status.OK, "stopped"
                    );
                    endResult.setKeepCallback(false); // 释放回调
                    locationCallbackContext.sendPluginResult(endResult);
                    locationCallbackContext = null;
                }

                callbackContext.success("stop location ok");
            } catch (Exception e) {
                callbackContext.error("stop location error:" + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────
    // 广播接收器：收到定位结果 → 推给 JS
    // ─────────────────────────────────────────
    private void registerLocationReceiver(Context ctx) {
        if (locationReceiver != null) return; // 防止重复注册

        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (locationCallbackContext == null) return;

                try {
                    // 从 Intent 中取出定位数据（字段名按实际 SDK 文档调整）
                    String data = intent.getStringExtra("data");

                    // keepCallback=true：持续触发，不销毁回调
                    PluginResult result = new PluginResult(
                            PluginResult.Status.OK, data
                    );
                    result.setKeepCallback(true);
                    locationCallbackContext.sendPluginResult(result);

                } catch (Exception e) {
                    PluginResult errResult = new PluginResult(
                            PluginResult.Status.ERROR, e.getMessage()
                    );
                    errResult.setKeepCallback(true);
                    locationCallbackContext.sendPluginResult(errResult);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("location");
        LocalBroadcastManager.getInstance(ctx)
                .registerReceiver(locationReceiver, filter);
    }

    private void unregisterLocationReceiver() {
        if (locationReceiver != null) {
            LocalBroadcastManager.getInstance(
                    cordova.getActivity().getApplicationContext()
            ).unregisterReceiver(locationReceiver);
            locationReceiver = null;
        }
    }
}