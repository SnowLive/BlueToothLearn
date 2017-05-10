package com.test.shengyunt.blelearn11;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

/**
 * Created by SHENGYUNT on 2017/5/8.
 */
public class MyBLEAdapter extends BaseAdapter implements View.OnClickListener {

    private ViewHolder viewHolder ;
    private LayoutInflater layoutInflater;
    private List<HashMap<String, Object>> bleList;
    private String[] from ;
    private int[] to;
    private ConnectThread connThread;

    public MyBLEAdapter(Context context, List<HashMap<String, Object>> bleList,String[] from, int[] to) {
        this.layoutInflater = LayoutInflater.from(context);
        this.bleList = bleList;
        this.from = from;
        this.to = to;

    }


    @Override
    public int getCount() {
        return bleList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder = null;
        if (convertView==null){
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.list_item,null);
            viewHolder.lvBleName = (TextView) convertView.findViewById(to[0]);
            viewHolder.lvBleAddr = (TextView) convertView.findViewById(to[1]);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.lvBleName.setText((CharSequence) bleList.get(position).get(from[0]));
        viewHolder.lvBleAddr.setText((CharSequence) bleList.get(position).get(from[1]));
        setViewEvent();
        return convertView;
    }

    //为listView中的各个组件添加监听事件
    private void setViewEvent() {

        viewHolder.lvBleName.setOnClickListener(this);
        viewHolder.lvBleAddr.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lvblename:
                    if (connThread!=null){
                        Toast.makeText(layoutInflater.getContext(), "断开连接", Toast.LENGTH_SHORT).show();
                        connThread.cancel();
                        connThread.interrupt();
                    }
                break;
            case R.id.lvbleaddr:
                String str = String.valueOf(viewHolder.lvBleAddr.getText());
                Log.e("device", str);
                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(str);

                str = "链接"+str;
                Toast.makeText(layoutInflater.getContext(), str, Toast.LENGTH_SHORT).show();
                if(device!=null){
                    Toast.makeText(layoutInflater.getContext(),"device 不为空",Toast.LENGTH_SHORT).show();
                    connThread = null;
                    connThread = new ConnectThread(layoutInflater.getContext(),device);
                    connThread.start();
                }
                break;
        }
    }

}
