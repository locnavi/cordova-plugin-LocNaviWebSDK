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

module.exports = {
    init,
    showMap,
    naviTo,
    showMapWithParmas
};
