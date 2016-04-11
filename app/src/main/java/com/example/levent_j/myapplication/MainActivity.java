package com.example.levent_j.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_ENABLE_BT = 1;
    @Bind(R.id.btn_blue_open) Button mOpen;
    @Bind(R.id.btn_blue_close) Button mCheckOpen;
    @Bind(R.id.btn_search_pair) Button mSearchPair;
    @Bind(R.id.btn_show_pair) Button mShowPair;
    @Bind(R.id.btn_find) Button mFindDevice;
    @Bind(R.id.btn_start_find) Button mStartFind;
    @Bind(R.id.rv_devices) RecyclerView mDevRecycler;

    public BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairDevices;
    List<BluetoothDevice> deviceList;
    BroadcastReceiver mReceiver;
    DevicesAdapter devicesAdapter;

    private final String SPP_UUID = "0000110a-0000-1000-8000-00805f9b34fb";
    int deviceSize;
    private UUID uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mOpen.setOnClickListener(this);
        mCheckOpen.setOnClickListener(this);
        mSearchPair.setOnClickListener(this);
        deviceList = new ArrayList<>();
        mShowPair.setOnClickListener(this);
        mFindDevice.setOnClickListener(this);
        mStartFind.setOnClickListener(this);
        mDevRecycler.setLayoutManager(new LinearLayoutManager(this));
        mReceiver = new BluetoothReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        deviceSize = 1000;
        devicesAdapter = new DevicesAdapter(this);
        uuid = UUID.fromString(SPP_UUID);

        AcceptThread acceptThread = new AcceptThread();
        acceptThread.run();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_blue_open:
                bluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null){
                    T("不支持蓝牙设备");
                }else {
                    if (!bluetoothAdapter.isEnabled()){
                        //静默打开
                        bluetoothAdapter.enable();
                        //询问打开
//                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                    }else {
                        T("已开启蓝牙");
                    }
                    T("蓝牙设备名字是："+bluetoothAdapter.getName()+"  已打开");
                }
                break;
            case R.id.btn_blue_close:
                bluetoothAdapter.disable();
                break;
            case R.id.btn_search_pair:
                pairDevices = bluetoothAdapter.getBondedDevices();
                if (pairDevices.size()>0&&deviceSize!=pairDevices.size()){
                    deviceList.clear();
                    for (BluetoothDevice device : pairDevices){
                        deviceList.add(device);
                    }
                    T("配对设备查询结束");
                }else {
                    T("未找到配对设备");
                }
                break;
            case R.id.btn_show_pair:
                L("->","deviceList.size():"+deviceList.size());
                L("->", "deviceSize:" + deviceSize);
                if (deviceSize != deviceList.size()){
                    devicesAdapter.updateDevicesAdapter(deviceList);
                    mDevRecycler.setAdapter(devicesAdapter);
                }
                break;
            case R.id.btn_find:
                if (bluetoothAdapter.startDiscovery()){
                    L("->", "搜索已开启");
                }else {
                    L("->", "开启搜索失败");
                }
                break;
            case R.id.btn_start_find:
                L("->", "本设备已可见");
                Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
                startActivity(discoveryIntent);
                break;
        }
    }


    public void T(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
    public void L(String key,String message){
        Log.d(key, message);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    class BluetoothReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            L("->", "广播收到");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.add(device);
                bluetoothAdapter.cancelDiscovery();
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                L("->", "设备搜索中");
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                L("->","设备搜索完毕");
            }else {
                L("->","Fuck the bluetooth");
            }
        }
    }

    private class AcceptThread extends Thread {
        private  BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("myServerSocket", UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb"));
            } catch (IOException e) {

            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    //一个虚构的方法，用来初始化数据传输的线程
                    L("socket","在AcceptThread中对socket处理数据");
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

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }




}
