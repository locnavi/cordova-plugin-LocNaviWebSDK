package com.locnavi.cordova.plugin.websdk;

import com.locnavi.websdk.LocNaviWebSDK;
import com.orhanobut.logger.Logger;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class echoes a string called from JavaScript.
 */
public class LocNaviWebSDKPlugin extends CordovaPlugin {

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
}