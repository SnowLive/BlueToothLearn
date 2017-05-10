package com.test.shengyunt.blelearn11;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @Author : Snowlive
 * 蓝牙客户端连接进程
 * 通过以传入参数的方式获取找到的蓝牙设备，然后，通过
 *      调用BluetoothDevice的`creatRfcommSocketToServiceRecord(UUID MY_UUID);`方法，进而创造出一个
 *      BluetoothSocket用于通信，
 *      即：通过获取找到的蓝牙设备对象，然后，调用‘创建与远程服务通信的交流的套接字根据UUID号’。
 *
 *
 *
 */
//客户端线程
public class ConnectThread extends Thread {
    private static final int MESSAGE_READ = 10001;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Context context;

    private Handler mHandler ;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public ConnectThread(Context context,BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        this.context = context;
        mmDevice = device;
        // 获取蓝牙BluetoothSocket（套接字）与传入的蓝牙设备链接
        try {
            // MY_UUID是该app的UUID字符串，也可用作服务的信息代码
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
        mHandler = ((MainActivity)context).myHandler;



    }
    public void run() {
        // 结束会带慢链接的蓝牙搜索。
        bluetoothAdapter.cancelDiscovery();
        try {
            //通过套接字与设备链接，只有当连接成功或者抛出异常时，程序会停止。
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                Toast.makeText(context, "无法链接", Toast.LENGTH_SHORT).show();
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // 在分线程中管理链接。Do work to manage the connection (in a separate thread)
//            manageConnectedSocket(mmSocket);
    }
    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
            if (!mmSocket.isConnected())
                Toast.makeText(context, "关闭链接完成", Toast.LENGTH_SHORT).show();
        } catch (IOException e) { }
    }

    //数据传输线程
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // 数据流缓冲 buffer store for the stream
            int bytes; // 存储读取的数据。bytes returned from read()

            // 持续监听输入流，直到异常出现。 Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // 从输入数据中读取数据。 Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // 将保留的数据发送到ui
                    // Activity Send the obtained bytes to the UI activity
                    String str = new String(buffer);
                    Log.e("BLEMsg",str);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("data",str);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);


//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }



}
