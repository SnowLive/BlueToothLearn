# Android 学习之蓝牙学习

[TOC]

## 前言

在进行蓝牙开发时，需要注意，涉及到蓝牙设备扫描时，需要根据sdk的版本的不同进行定位权限的申请。
因为，在android sdk23之后，google对权限的有使用有了很大的调整。比如，在使用蓝牙扫描蓝牙设
备时需要验证应用是否获取了定位权限。如果没有要让用户重新确认获取权限。从而进行蓝牙设备扫描操
作的实现。

## 一、获取蓝牙适配器

```java
    
    BluetoothAdapter BLEAdapter = BluetoothAdapter.getDefaultAdapter();

```


## 二、蓝牙的开启和关闭

在AndroidManifast.xml文件中添加如下权限：

```xml

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION"/>

```
在java中使用BluetoothAdapter的enable()开蓝牙，使用BluetoothAdapter的disable()方法关闭
蓝牙。
```java
BLEAdapter.enable();//开蓝牙
BLEAdapter.disable();//关蓝牙
```

## 三、蓝牙的搜索

蓝牙设备的搜索主要包含两步，
 - 定义广播接收器

```java
 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 开始搜索广播
            if (action.equals("android.bluetooth.device.action.FOUND")) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                Toast.makeText(MainActivity.this, "设备找到："+device.getName, Toast.LENGTH_SHORT).show();
                               Log.i("SnowliveBLE", "设备找到："+devicegetName);
            }
            // 停止搜索广播
            if (action.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
                Toast.makeText(MainActivity.this, "查询停止", Toast.LENGTH_SHORT).show();
                Log.e("SnowliveBLE", "搜索结束");
            }
        }
    };
```
 
 - 注册广播
 
 ```java
 
 IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
         intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         registerReceiver(mReceiver, intentFilter);
         Log.e("BluetoothUtil", "注册广播");
 
 ```


同时，进行蓝牙设备搜索功能的实现时，为了适应Android6.0及以上版本，必须要在AndroidManifast.xml
文件中添加如下的权限代码：

```xml
<uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

之后，在首次搜索蓝牙设备时，向用户获取定位权限，获取方式如下，定义并调用如下方法：
```java
 //申请定位权限(首次搜索时调用)
    private void mayRequestLocation() {
        String TAG = "Snowlive";
        Log.e(TAG, "mayRequestLocation: androidSDK--" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 23) {
            //6.0以上设备
            int checkCallPhonePermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "mayRequestLocation: 请求粗略定位的权限");
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                return;
            }
        }
    }
```
通过判断Build.VERSION.SDK_INT(SDK的版本号)，请求定位权限。然后，在首次搜索蓝牙设备时调用该方法。进行权限的获取。





## 注意

在onDestroy()方法中要调用unregister(mReceiver);注销广播。


## 蓝牙的使用过程
 - 开蓝牙
 - 开可检测
 - 搜索配对过的设备
 - 搜索设备






























