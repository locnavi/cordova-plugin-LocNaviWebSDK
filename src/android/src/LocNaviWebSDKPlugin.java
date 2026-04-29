package com.locnavi.cordova.plugin.websdk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

    private static final int REQUEST_CODE_BLUETOOTH_OPEN = 1000;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 1001;

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
            if (mode != 2) {
                // check if bluetooth is enabled
                String btStatus = this.verifyBluetooth();
                if (!btStatus.equals("ok")) {
                    callbackContext.error(btStatus);
                    return;
                }
            }
            if (mode != 1 ) {
                // check if location is enabled
                if (!this.isLocationEnabled()) {
                    callbackContext.error("location service is not enabled");
                    return;
                }
            }
            if (!this.checkAndRequestPermissions()) {
                callbackContext.error("permission is not granted");
                return;
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

    // 判斷手機定位功能是否開啟
    private void verifyLocation() {
        if (!this.isLocationEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.cordova.getActivity())
                    .setTitle("定位功能未開啟")
                    .setMessage("室內定位需要開啟定位功能後方可使用")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 跳轉至 GPS 設定介面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            cordova.getActivity().startActivity(intent);
                        }
                    });
            builder.setCancelable(true);
            builder.show();
        }
    }

    // 判斷手機藍牙是否開啟，若未開啟則嘗試開啟。
    public String verifyBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // 設備不支援藍牙
            return "bluetooth not supported";
        } else {
            // 設備支援藍牙
            if (!bluetoothAdapter.isEnabled()) {
                // 藍牙未啟用，請求打開
                Activity ctx = this.cordova.getActivity();
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // 請求藍牙連接權限
                    ActivityCompat.requestPermissions(ctx,
                            new String[]{ Manifest.permission.BLUETOOTH_CONNECT },
                            REQUEST_CODE_BLUETOOTH_OPEN
                    );
                    return "requesting bluetooth permission"; // 等待用戶授權後再嘗試開啟藍牙
                }
                bluetoothAdapter.enable();
            }
        }
        return "ok";
    }

    // 判斷定位功能是否開啟
    private boolean isLocationEnabled() {
        int locationMode = 0;
        Context ctx = this.cordova.getActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            String locationProviders = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private boolean checkAndRequestPermissions() {
        // 檢查並請求藍牙和位置權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Activity ctx = this.cordova.getActivity();
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 請求藍牙掃描和定位權限
                ActivityCompat.requestPermissions(ctx,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                        },
                        REQUEST_CODE_BLUETOOTH_SCAN
                );
                return false; // 等待用戶授權後再嘗試開啟藍牙
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
        int[] grantResults) throws JSONException {
            switch (requestCode) {
                case REQUEST_CODE_BLUETOOTH_SCAN: {
                    if (grantResults.length >= 1) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            // 同意授權
                            // this.startRangeBeacons();
                        } else {
                            // 拒絕授權
                            // String msg;
                            // if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                            //     msg = "請在設定頁面開啟【位置資訊】權限。";
                            // } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                            //     msg = "請在設定頁面開啟【附近的裝置】權限。";
                            // } else {
                            //     msg = "請在設定頁面開啟【附近的裝置】和【位置資訊】權限。";
                            // }
                            // // 提示開啟授權
                            // AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            //         .setTitle("室內定位功能受限")
                            //         .setMessage(msg)
                            //         .setNegativeButton(android.R.string.cancel, null)
                            //         .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            //             @Override
                            //             public void onClick(DialogInterface dialog, int which) {
                            //                 Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            //                 intent.setData(Uri.parse("package:" + getPackageName()));
                            //                 startActivity(intent);
                            //             }
                            //         });
                            // builder.setCancelable(true);
                            // builder.show();
                        }
                    }
                }
                break;
                case REQUEST_CODE_BLUETOOTH_OPEN: {
                    if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        verifyBluetooth();
                    }
                }
                break;
            }
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