# cordova 引入 LocNaviWebSDK 簡介

cordova-plugin-LocNaviWebSDK 是一套面向 cordova 開發者使用LocNaviWebSDK的插件，開發者可自行引用該插件輕鬆實現地圖展示、導航至具體poi等功能。
1.*的版本开始支持Android Support。0.*和2.*版本支持Android X库。iOS部分没有什么影响

## 獲取AppKey

**appKey mapId targetId 請向<richard.chin@locnavi.com> 申請**

## 插件引用

### npm引用
直接引入线上npm包

```bash
// android support 使用1.*的版本
cordova plugin add cordova-plugin-locnavi-websdk@1.0.13
// android x 使用0.*和2.*的版本
cordova plugin add cordova-plugin-locnavi-websdk@2.0.11
```

如果已經添加cordova-plugin-locnavi-websdk,請先移除,在進行添加

```bash
cordova plugin remove cordova-plugin-locnavi-websdk
cordova plugin add cordova-plugin-locnavi-websdk
```

### 本地引用
拷貝插件文件夾cordova-plugin-LocNaviWebSDK 至項目目錄 (相對路徑)

```bash
cordova plugin add ./cordova-plugin-LocNaviWebSDK
```

如果已經添加cordova-plugin-LocNaviWebSDK,請先移除,在進行添加

```bash
//先查看安装了什么插件
cordova plugin list
cordova plugin remove cordova-plugin-LocNaviWebSDK
cordova plugin add ./cordova-plugin-LocNaviWebSDK
```

### 衝突處理

若是加入插件遇到衝突，請加入--force指令，如下：

```bash
cordova plugin add ./cordova-plugin-LocNaviWebSDK --force
```

## 安裝注意事項

* iOS

  * 要安裝Cordova，需要先安裝Node.js在Node.js官網上下載並安裝
  * 添加該插件之後，cd到iOS項目目錄，使用pod install
  * 打開項目工程後會發現有些文件沒有，比如Plugins文件可以在 cordova-plugin-LocNaviWebSDK 拷貝
  * 权限设置请参考iOS原生demo  [iOS README](https://github.com/locnavi/ios-sdk/blob/master/README.md)

### SDK初始化

```js
        LocNaviWebSDKPlugin.init(appkey, {
            userId: "123",
            serverUrl: "https://xxx.com",
            uuids: ["uuid1", "uuid2"],
            uploadApi: "https://xxx.com", //需要https
            uploadInterval: 1000
        });
```

### 顯示室內地圖

```js
  LocNaviWebSDKPlugin.showMap(mapId);
```

### 導航至具體地址

```js
  LocNaviWebSDKPlugin.showMap(mapId, poi);
```

## 進入 IOS 和 Android 平台查看相關的 README 進行設置

[iOS文檔](https://github.com/locnavi/locnavi-websdk-ios/blob/master/README.md)

[Android文檔](https://github.com/locnavi/IndoorNavigationAndroidWebSDK/blob/main/README.md)
