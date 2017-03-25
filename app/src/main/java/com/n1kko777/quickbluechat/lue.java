package com.n1kko777.quickbluechat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class lue extends Activity {

    private TextView tvDeviceListPairedDeviceTitle, tvDeviceListNewDeviceTitle;
    private ListView lvDeviceListPairedDevice, lvDeviceListNewDevice;
    private Button btnDeviceListScan;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    private static final int REQUEST_PERMISSIONS = 1;



    public static String DEVICE_ADDRESS = "deviceAddress";
    private Button skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_lue);

        requestPermission();

        Toast.makeText(lue.this, getResources().getString(R.string.tost), Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_CANCELED);

        getWidgetReferences();
        bindEventHandler();
        initializeValues();

    }

    private void requestPermission() {
        String cameraPermission = Manifest.permission.CAMERA;
        String bluetoothPermission = Manifest.permission.BLUETOOTH;
        String bluetoothAdminPermission = Manifest.permission.BLUETOOTH_ADMIN;
        String readPhoneStatePermition = Manifest.permission.READ_PHONE_STATE;
        String readPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String internetPermission = Manifest.permission.INTERNET;
        String vibratePermission = Manifest.permission.VIBRATE;

        int hasbluetoothPermission = 0;
        int hasreadPhoneStatePermition = 0;
        int hasbluetoothAdminPermission = 0;
        int hascameraPermission = 0;
        int hasreadPermission  = 0;
        int haswritePermission = 0;
        int hasinternetPermission = 0;
        int hasvibratePermission = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasreadPhoneStatePermition = checkSelfPermission(readPhoneStatePermition);
            hascameraPermission = checkSelfPermission(cameraPermission);
            hasbluetoothPermission = checkSelfPermission(bluetoothPermission);
            hasbluetoothAdminPermission = checkSelfPermission(bluetoothAdminPermission);
            hasreadPermission = checkSelfPermission(readPermission);
            haswritePermission = checkSelfPermission(writePermission);
            hasinternetPermission = checkSelfPermission(internetPermission);
            hasvibratePermission = checkSelfPermission(vibratePermission);

        }
        List<String> permissions = new ArrayList<String>();

        if (hasbluetoothPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(bluetoothPermission);
        }
        if (hasreadPhoneStatePermition != PackageManager.PERMISSION_GRANTED) {
            permissions.add(readPhoneStatePermition);
        }
        if (hasbluetoothAdminPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(bluetoothAdminPermission);
        }

        if (hascameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(cameraPermission);
        }
        if (hasreadPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(readPermission);
        }
        if (hasinternetPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(internetPermission);
        }
        if (haswritePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(writePermission);
        }
        if (hasvibratePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(vibratePermission);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(params, REQUEST_PERMISSIONS);
            }
        } else {
            // We already have permission, so handle as normal

        }
    }

    private void getWidgetReferences() {
        tvDeviceListPairedDeviceTitle = (TextView) findViewById(R.id.tvDeviceListPairedDeviceTitle);
        tvDeviceListNewDeviceTitle = (TextView) findViewById(R.id.tvDeviceListNewDeviceTitle);

        lvDeviceListPairedDevice = (ListView) findViewById(R.id.lvDeviceListPairedDevice);
        lvDeviceListNewDevice = (ListView) findViewById(R.id.lvDeviceListNewDevice);

        btnDeviceListScan = (Button) findViewById(R.id.btnDeviceListScan);
        skip = (Button) findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void bindEventHandler() {
        lvDeviceListPairedDevice.setOnItemClickListener(mDeviceClickListener);
        lvDeviceListNewDevice.setOnItemClickListener(mDeviceClickListener);

        btnDeviceListScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDiscovery();
                btnDeviceListScan.setVisibility(View.GONE);
            }
        });
    }

    private void initializeValues() {
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);

        lvDeviceListPairedDevice.setAdapter(pairedDevicesArrayAdapter);
        lvDeviceListNewDevice.setAdapter(newDevicesArrayAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            tvDeviceListPairedDeviceTitle.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress());

            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired)
                    .toString();
            pairedDevicesArrayAdapter.add(noDevices);

        }
    }

    private void startDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        tvDeviceListNewDeviceTitle.setVisibility(View.VISIBLE);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            bluetoothAdapter.cancelDiscovery();

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra(DEVICE_ADDRESS, address);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesArrayAdapter.add(device.getName() + "\n"
                            + device.getAddress());

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (newDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(
                            R.string.none_found).toString();
                    newDevicesArrayAdapter.add(noDevices);
                    finish();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(discoveryFinishReceiver);
    }

}

