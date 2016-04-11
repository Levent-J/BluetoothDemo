package com.example.levent_j.myapplication;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.LogRecord;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by levent_j on 16-4-11.
 */
public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.mViewHolder>{
    private Context context;
    private final LayoutInflater inflater;
    private List<BluetoothDevice> bluetoothDevices;
    BluetoothDevice client;

    public DevicesAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
        bluetoothDevices = new ArrayList<>();
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.device_item, null);
        return new mViewHolder(view);
    }

    @Override
    public void onBindViewHolder(mViewHolder holder, int position) {
        BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
        holder.mDeviceName.setText(bluetoothDevice.getName());
        holder.mDeviceMac.setText(bluetoothDevice.getAddress());
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public void updateDevicesAdapter(List<BluetoothDevice> list){
        this.bluetoothDevices.clear();
        this.bluetoothDevices.addAll(list);
        notifyDataSetChanged();
    }

    class mViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.tv_device_name) TextView mDeviceName;
        @Bind(R.id.tv_device_mac) TextView mDeviceMac;
        public mViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

            mDeviceName.setOnClickListener(this);
            mDeviceMac.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_device_name:
                case R.id.tv_device_mac:
                    BluetoothDevice bluetoothDevice = bluetoothDevices.get(getPosition());
                    Log.d("UUID",""+bluetoothDevice.getUuids()[0]);
                    try {
                        connect(bluetoothDevice);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }



    }
    private void connect(BluetoothDevice device) throws IOException {
        //固定的UUID
        final String SPP_UUID = "0000110a-0000-1000-8000-00805f9b34fb";
        UUID uuid = UUID.fromString(SPP_UUID);
        ConnectThread connectThread = new ConnectThread(device,uuid);
        connectThread.run();
//            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
//            socket.connect();
    }



    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device,UUID uuid) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {

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
            // 一个虚构的方法，用来初始化数据传输的线程
//            manageConnectedSocket(mmSocket);
            Log.d("socket", "在AcceptThread中对socket处理数据");

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


}
