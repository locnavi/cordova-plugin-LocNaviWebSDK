//
//  LocNaviWebSDKPlugin.m
//  cordovaDemo
//
//  Created by zhangty on 2018/12/3.
//

#import "LocNaviWebSDKPlugin.h"
#import <LocNaviWebSDK/LocNaviWebSDK.h>
#import <Cordova/CDVAppDelegate.h>

@interface LocNaviWebSDKPlugin()

// 专门持有「定位结果推送」的 callbackId
@property (nonatomic, copy) NSString *locationCallbackId;

@end

@implementation LocNaviWebSDKPlugin


- (void)init:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *appKey = command.arguments[0];
        NSDictionary *options = command.arguments.count > 1 ? command.arguments[1] : @{};
        NSString *userId = options[@"userId"];
        NSString *serverUrl = options[@"serverUrl"];
        NSArray *uuids = options[@"uuids"];
        NSString *uploadApi = options[@"uploadApi"];
        int uploadInterval = options[@"uploadInterval"] != NULL ? [options[@"uploadInterval"] intValue] : 0;
        
        //初始化SDK
        [LocNaviMapService setAppKey:appKey];
        if (userId) {
            [LocNaviMapService setUserId:userId];
        }
        if (serverUrl) {
            [LocNaviMapService setServerUrl:serverUrl];
        }
        if (uuids && uuids.count > 0) {
            [LocNaviMapService setUUIDs:uuids];
        }
        if (uploadApi) {
            [LocNaviMapService setUploadLocationApi:uploadApi timeInterval:uploadInterval];
        }

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"initSDK ok"];
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showMap:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    @try {
        //显示地图
        NSString *mapId = command.arguments[0];

        LocNaviWebViewController *vc = [[LocNaviWebViewController alloc] initWithMapId:mapId];
        vc.modalPresentationStyle = UIModalPresentationFullScreen;
        //可以使用其他方式弹出界面，如navigationController
        CDVAppDelegate* delegate = [UIApplication sharedApplication].delegate;
        [delegate.window.rootViewController presentViewController:vc animated:true completion:nil];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"showMap ok"];
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
    
- (void)naviTo:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    @try {
        //导航到具体地点
        NSString *mapId = @"";
        NSString *targetId = @"";
        if (command.arguments.count >= 2) {
            mapId = command.arguments[0];
            targetId = command.arguments[1];
        }
        LocNaviWebViewController *vc = [[LocNaviWebViewController alloc] initWithMapId:mapId poi:targetId];
        vc.modalPresentationStyle = UIModalPresentationFullScreen;
        
        //可以使用其他方式弹出界面，如navigationController
        CDVAppDelegate* delegate = [UIApplication sharedApplication].delegate;
        [delegate.window.rootViewController presentViewController:vc animated:true completion:nil];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"naviTo ok"];
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showMapWithParmas:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    @try {
        //显示地图
        NSString *mapId = command.arguments[0];
        NSString *params = command.arguments.count > 1 ? command.arguments[1] : @"";

        LocNaviWebViewController *vc = [[LocNaviWebViewController alloc] initWithMapId:mapId params:params];
        vc.modalPresentationStyle = UIModalPresentationFullScreen;
        //可以使用其他方式弹出界面，如navigationController
        CDVAppDelegate* delegate = [UIApplication sharedApplication].delegate;
        [delegate.window.rootViewController presentViewController:vc animated:true completion:nil];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"showMapWithParmas ok"];
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)initLocationService:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *mapId = command.arguments[0];
        NSDictionary *options = command.arguments.count > 1 ? command.arguments[1] : @{};
        NSString *serverUrl = options[@"serverUrl"];
        
        //初始化SDK
        LocNaviLocationService *service = [LocNaviLocationService sharedInstance];
        [service setMapId:mapId];
        if (serverUrl && serverUrl.length > 0) {
            [service setServerUrl:serverUrl];
        }

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"init locationService ok"];
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)startLocation:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    @try {
        NSDictionary *options = command.arguments.count > 0 ? command.arguments[0] : @{};
        int mode = options[@"mode"] != NULL ? [options[@"mode"] intValue] : 0;
        
        LocNaviLocationService *service = [LocNaviLocationService sharedInstance];
        // 開始定位
        // 可指定只開啟藍牙定位，暫時未使用 GPS 定位，預設使用 LocNaviConstants.LOCATION_MODE_AUTO
        // [service start:LocNaviLocationModeAuto];
        // 獲取更詳細的定位資訊
        [service start:mode detail:YES];

        // 預設每秒返回定位，若調用下面方法，請確保 ScanPeriods 大於 1000。
        // [service updateScanPeriods:1500 betweenScanPeriod:1000];

        // 新增廣播監聽
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateLocation:) name:LOCNAVI_NOTI_LOCATION object:nil];

        self.locationCallbackId = command.callbackId;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"startLocation ok"];
        pluginResult.keepCallback = @YES;
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)stopLocation:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    @try {
        LocNaviLocationService *service = [LocNaviLocationService sharedInstance];
        // 停止定位
        [service stop:LocNaviLocationModeAuto];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:LOCNAVI_NOTI_LOCATION object:nil];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"stop location ok"];
        pluginResult.keepCallback = @NO;
        self.locationCallbackId = nil;
    } @catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)updateLocation:(NSNotification *)noti {
    // noti.object 傳遞 LocNaviLocation 物件
    if (!self.locationCallbackId || !noti.object) {
        return;
    }
    NSString *locationStr = nil;
    if ([noti.object isKindOfClass:[LocNaviLocation class]]) {
        LocNaviLocation *location = noti.object;
        locationStr = [NSString stringWithFormat:@"{\"longitude\":\"%f\",\"latitude\":\"%f\",\"floor\":%@}", location.coordinate.longitude, location.coordinate.latitude, location.floor && ![location.floor isKindOfClass:[NSNull class]] ? location.floor : @"null"];
    } else if ([noti.object isKindOfClass:[NSString class]]) {
        locationStr = [noti.object copy];
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:locationStr];
    pluginResult.keepCallback = @YES;
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.locationCallbackId];
}

@end
