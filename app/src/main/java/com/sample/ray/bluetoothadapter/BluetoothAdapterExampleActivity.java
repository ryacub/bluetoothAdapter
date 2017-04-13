package com.sample.ray.bluetoothadapter;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothAdapterExampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    Button onButton;
    Button offButton;
    Button listButton;
    Button findButton;
    Button discoverButton;
    TextView statusText;
    android.bluetooth.BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> bluetoothDevices;
    ListView devicesList;
    ArrayAdapter<String> BTAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_adapter_example);
        viewInit();
        //making sure bluetooth adapter exists
        if (bluetoothAdapter == null) {
            onButton.setEnabled(false);
            offButton.setEnabled(false);
            listButton.setEnabled(false);
            findButton.setEnabled(false);
            discoverButton.setEnabled(false);
            statusText.setText(String.format(getString(R.string.Text_Status), "not supported"));

            Toast.makeText(getApplicationContext(), R.string.bluetooth_lack_support, Toast.LENGTH_LONG).show();
        } else {
            onButton.setOnClickListener(this);
            offButton.setOnClickListener(this);
            listButton.setOnClickListener(this);
            findButton.setOnClickListener(this);
            discoverButton.setOnClickListener(this);

            // adapter for devices in the list
            BTAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            devicesList.setAdapter(BTAdapter);
        }
    }
    //initializing the views
    private void viewInit() {
        statusText = (TextView) findViewById(R.id.status_text);
        onButton = (Button) findViewById(R.id.turn_on);
        offButton = (Button) findViewById(R.id.turn_off);
        listButton = (Button) findViewById(R.id.paired);
        findButton = (Button) findViewById(R.id.search);
        discoverButton = (Button) findViewById(R.id.discover);
        devicesList = (ListView) findViewById(R.id.devices_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
    }
    //handling on clicks event listener
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.turn_on) {
            on();

        } else if (i == R.id.turn_off) {
            off();

        } else if (i == R.id.paired) {
            pairedList();

        } else if (i == R.id.discover) {
            discovered();

        } else if (i == R.id.search) {
            find();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (bluetoothAdapter.isEnabled()) {
                toolbar.setTitle(String.format(getString(R.string.Text_Status), getString(R.string.enabled)));
            } else {
                toolbar.setTitle(String.format(getString(R.string.Text_Status), getString(R.string.disabled)));
            }
        }
    }
    //turn on bluetooth
    public void on() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(), R.string.bluetooth_turned_on, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.bluetooth_already_on, Toast.LENGTH_LONG).show();
        }
    }
    //turn off bluetooth
    public void off() {
        bluetoothAdapter.disable();
        toolbar.setTitle(String.format(getString(R.string.Text_Status), getString(R.string.Text_disconnected)));
        Toast.makeText(getApplicationContext(), R.string.bluetooth_turned_off,
                Toast.LENGTH_LONG).show();
    }
    //previous paired devices
    public void pairedList() {
        // get paired devices
        bluetoothDevices = bluetoothAdapter.getBondedDevices();
        // put to the adapter
        for (BluetoothDevice device : bluetoothDevices)
            BTAdapter.add(device.getName() + "\n" + device.getAddress());
        Toast.makeText(getApplicationContext(), R.string.show_paired_devices, Toast.LENGTH_SHORT).show();
    }
    //find other devices
    public void find() {
        if (bluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            bluetoothAdapter.cancelDiscovery();
        } else {
            BTAdapter.clear();
            bluetoothAdapter.startDiscovery();
            registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }
    //let this device be discovered
    public void discovered() {
        Intent discoverableIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(android.bluetooth.BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }
    //broadcast receiver to find other bluetooth devices
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTAdapter.add(device.getName() + "\n" + device.getAddress());
                BTAdapter.notifyDataSetChanged();
            }
        }
    };
    //removing receiver
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
