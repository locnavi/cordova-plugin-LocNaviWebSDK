//
//  LocNaviWebSDKPlugin.m
//  cordovaDemo
//
//  Created by zhangty on 2018/12/3.
//

#import "LocNaviWebSDKPlugin.h"
#import <LocNaviWebSDK/LocNaviWebSDK.h>
#import <Cordova/CDVAppDelegate.h>

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

@end
