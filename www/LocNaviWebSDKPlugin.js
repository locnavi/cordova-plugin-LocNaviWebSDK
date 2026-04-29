var exec = require('cordova/exec');

function init (appkey, options, success, error) {
    exec(success, error, 'LocNaviWebSDKPlugin', 'init', [appkey, options]);
};

function showMap (mapId, success, error) {
    exec(success, error, 'LocNaviWebSDKPlugin', 'showMap', [mapId]);
};

function naviTo(mapId, targetId, success, error) {
    exec(success, error, 'LocNaviWebSDKPlugin', 'naviTo', [mapId, targetId]);
};

function showMapWithParmas (mapId, params, success, error) {
    exec(success, error, 'LocNaviWebSDKPlugin', 'showMapWithParmas', [mapId, params]);
};

function initLocationService (mapId, options, success, error) {
    exec(success, error, 'LocNaviWebSDKPlugin', 'initLocationService', [mapId, options]);
}

function startLocation (options, success, error) {
    exec(success, error, 'LocNaviWebSDKPlugin', 'startLocation', [options]);
}

function stopLocation (options, success, error) {
    exec(success, error, 'LocNaviWebSDKPlugin', 'stopLocation', [options]);
}

module.exports = {
    init,
    showMap,
    naviTo,
    showMapWithParmas,
    initLocationService,
    startLocation,
    stopLocation
};
