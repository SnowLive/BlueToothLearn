package com.test.shengyunt.blelearn11;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_COARSE_LOCATION = 1;

    private Button btOpenBLE,btSearchBLE,btCloseBLE;
    private ListView deviceListView ,devicePariedListView;
    private TextView findDevice ;

    private BluetoothAdapter bleAdapter;
    private MyBLEAdapter myBleAdapter;


//    private SimpleAdapter dLvSimpeAdapter;//单纯采用系统提供的ListView的SimpleAdapter进行信息的迭代

    private HashMap<String,Object> tempDeviceMap;
    private List<HashMap<String,Object>> deviceList,devicePariedList;
    private String[] from ;
    private int[] to ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registBroadcast();//注册广播
        initView();//初始化组件

    }

    //初始化组件，添加Button监听事件
    private void initView() {
        setContentView(R.layout.activity_main);
        checkLocationPermission();
        devicePariedListView = (ListView) findViewById(R.id.devicelistview1);
        deviceListView = (ListView) findViewById(R.id.devicelistview2);
        btOpenBLE = (Button) findViewById(R.id.btOpenBLE);
        btSearchBLE = (Button) findViewById(R.id.btSearchBLE);
        btCloseBLE = (Button) findViewById(R.id.btCloseBLE);
        findDevice = (TextView) findViewById(R.id.findtitle);


        btOpenBLE.setOnClickListener(this);
        btSearchBLE.setOnClickListener(this);
        btCloseBLE.setOnClickListener(this);




        deviceList = new ArrayList<>();
        devicePariedList = new ArrayList<>();
        from = new String[]{"deviceName","deviceAddr"};
        to = new int[]{R.id.lvblename,R.id.lvbleaddr};
        initBleAdapter();



    }

    //Button监听事件处理
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btOpenBLE:
                initBleAdapter();
                bleAdapter.enable();
                break;
            case R.id.btSearchBLE:
                initBleAdapter();
                deviceList.clear();
                devicePariedList.clear();
                deviceListView.setAdapter(null);
                findPariedDevice();
                bleAdapter.cancelDiscovery();
                bleAdapter.startDiscovery();
                break;
            case R.id.btCloseBLE:
                initBleAdapter();
                bleAdapter.disable();
                break;
        }

    }

    //初始化BLEAdapter
    private void initBleAdapter() {
        bleAdapter = bleAdapter==null? BluetoothAdapter.getDefaultAdapter():bleAdapter;
    }

    //查询已配对的设备
    private void findPariedDevice(){
        initBleAdapter();
        Set<BluetoothDevice> pariedDevicesSet = bleAdapter.getBondedDevices();
        for (BluetoothDevice bonddevice:pariedDevicesSet) {
            tempDeviceMap = new HashMap<>();
            tempDeviceMap.put(from[0],bonddevice.getName());
            tempDeviceMap.put(from[1],bonddevice.getAddress());
            devicePariedList.add(tempDeviceMap);
        }
    }

    //自定义广播接收器,根据广播的不同，做出不同的调整
    private BroadcastReceiver bleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String TAG = "SL_onReceive";
            String str = null;
            boolean isToast = true;
            //获取action
            String action = intent.getAction();
            switch (action) {
                //1.获将找到的BLEDevice存入deviceList中
                case BluetoothDevice.ACTION_FOUND:
                    str = "设备找到了";
                    isToast = false;
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    tempDeviceMap = new HashMap<String,Object>();
                    tempDeviceMap.put(from[0],device.getName());
                    tempDeviceMap.put(from[1],device.getAddress());
                    deviceList.add(tempDeviceMap);
                    showBlelist();
                    break;
                //2.查找完成，提示符提示
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    str = "查询结束";
                    break;
            }
            showLogToast(TAG,str,isToast);
        }
    };

    //注册广播
    private void registBroadcast(){
        IntentFilter bleFilter = new IntentFilter();
        bleFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bleFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bleReceiver,bleFilter);
    }

    //展示搜索的device
    private void showBlelist(){
//        dLvSimpeAdapter = new SimpleAdapter(this,deviceList,R.layout.list_item,from,to);
//        deviceListView.setAdapter(dLvSimpeAdapter);
        myBleAdapter = new MyBLEAdapter(this,devicePariedList,from,to);
        devicePariedListView.setAdapter(myBleAdapter);
        myBleAdapter = null;
        myBleAdapter = new MyBLEAdapter(this,deviceList,from,to);
        deviceListView.setAdapter(myBleAdapter);

    }

    //为LIstView添加

    //打印信息
    private void showLogToast(String TAG, String str, boolean isToast){
        Log.e(TAG,str);
        if(isToast)
            Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    //初略定位权限请求
    public void checkLocationPermission(){
        if (Build.VERSION.SDK_INT>=23){
            showLogToast("reqmiss","SDK版本为"+Build.VERSION.SDK_INT,true);
            int isGrant = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (isGrant != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
            }
        }
    }

    //注销广播
    @Override
    protected void onDestroy() {
        unregisterReceiver(bleReceiver);//注销广播
        super.onDestroy();
    }

    //消息句柄
    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            findDevice.setText(msg.getData().getBundle("data").get("data").toString());

        }
    };




    //服务器端线程
    //客户端线程
}
