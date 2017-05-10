package org.snowlive.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_PERMISSION_BT = 252611;//蓝牙权限请求
    private static final int REQUEST_PERMISSION_LOCATION = 252612;//定位权限获取
    private static final String NAME = "BLELean";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private Button btcloseBLE, btopenBLE, btsearchBLE,btBondBLE;
    private BluetoothAdapter bluetoothAdapter;//蓝牙适配器
    private boolean suppBLE = true;//是否支持
    //listview属性参数
    private ListView lv_device;
    private HashMap<String, Object> deviceMap;
    private List<HashMap<String, Object>> deviceList;
    private SimpleAdapter BLEAdapter;
    private String[] from;
    private int[] to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        //判断设备是否支持蓝牙设备
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            suppBLE = false;
        }
        if (suppBLE) {//设备支持蓝牙后，进行以下操作
            Log.e("Main:", "设备支持蓝牙");
            //注册广播
            registerBroadcast();
            btEvent();
        }
    }

    //初始化组件
    private void initView() {
        setContentView(R.layout.activity_main);
        mayRequestLocation();//根据sdk版本获取定位控制权限
        btopenBLE = (Button) findViewById(R.id.btopenBLE);
        btcloseBLE = (Button) findViewById(R.id.btcloseBLE);
        btsearchBLE = (Button) findViewById(R.id.btsearchBLE);
        btBondBLE = (Button) findViewById(R.id.btbondBLE);
        lv_device = (ListView) findViewById(R.id.lv_device);
        //获取蓝牙迭代器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        from = new String[]{"deviceName", "deviceAddr"};
        to = new int[]{R.id.deviceName, R.id.deviceMac};
        deviceList = new ArrayList<HashMap<String, Object>>();
    }

    //事件处理
    private void btEvent() {
        btopenBLE.setOnClickListener(this);
        btcloseBLE.setOnClickListener(this);
        btsearchBLE.setOnClickListener(this);

    }

    // 敲击事件处理
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btopenBLE:
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                    openBLEDiscoverrable();
                    Toast.makeText(this,"开启蓝牙可检测",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btcloseBLE:
                if (bluetoothAdapter.isEnabled())
                    bluetoothAdapter.disable();
                break;
            case R.id.btsearchBLE:
                lv_device.setAdapter(null);
                deviceList.clear();
                findpairedDevice();
                if (bluetoothAdapter == null)
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.startDiscovery();
                break;
            case R.id.btbondBLE:
                new AcceptThread().start();
                break;
            default:
                Toast.makeText(this, "按键事件", Toast.LENGTH_SHORT).show();

        }
    }

    //查询配对的蓝牙设备
    public void findpairedDevice() {
        Toast.makeText(this, "查找配对的设备", Toast.LENGTH_SHORT).show();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //数据放入ListView
                deviceMap = new HashMap<String, Object>();
                deviceMap.put("deviceName", device.getName());
                deviceMap.put("deviceMac", device.getAddress());
                deviceList.add(deviceMap);
                //找到之后用土司框显示。
                Toast.makeText(this, "设备：" + device.getName(), Toast.LENGTH_SHORT).show();
            }
            showDevicesInListView();
        }
    }
    //注册广播
    public void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, intentFilter);
        Log.e("BluetoothUtil", "注册广播");
    }

    //定义广播接收器
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 开始搜索广播
            if (action.equals("android.bluetooth.device.action.FOUND")) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                deviceMap = new HashMap<String, Object>();
                deviceMap.put(from[0], device.getName());
                deviceMap.put(from[1], device.getAddress());
                deviceList.add(deviceMap);
                new ConnectThread(device).start();
                showDevicesInListView();
            }
            // 停止搜索广播
            if (action.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
                Toast.makeText(MainActivity.this, "查询停止", Toast.LENGTH_SHORT).show();
                Log.e("Broadcast", "搜索结束");
            }
        }
    };
    //显示结果
    public void showDevicesInListView() {
        BLEAdapter = new SimpleAdapter(this, deviceList, R.layout.list_layout, from, to);
        lv_device.setAdapter(BLEAdapter);
    }
    // Activity销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

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

    //开本机设备可检测
    private void openBLEDiscoverrable(){
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
        startActivity(discoverableIntent);
    }
    //链接服务线程
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // 保持监听知道连接完成（返回一个socket）或出现异常
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // 如果链接成功
                if (socket != null) {
                    //管理连接（在一个分线程） Do work to manage the connection (in a separate thread)
//                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /*Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    //客户端线程
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
//            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


}
