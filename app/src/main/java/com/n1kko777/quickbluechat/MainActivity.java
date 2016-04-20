package com.n1kko777.quickbluechat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int PICKFILE_RESULT_CODE = 123;

    // The path to the "images" subdirectory
    private File mImagesDir;


    private ListView lvMainChat;
    private EditText etMain;
    private Button btnSend;

    private Intent serverIntent = null;
    private Intent PhotoSend = null;

    private String connectedDeviceName = null;
    private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private com.n1kko777.quickbluechat.chatService chatService = null;









    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case com.n1kko777.quickbluechat.chatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to,
                                    connectedDeviceName));
                            chatArrayAdapter.clear();

                            break;
                        case com.n1kko777.quickbluechat.chatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case com.n1kko777.quickbluechat.chatService.STATE_LISTEN:
                        case com.n1kko777.quickbluechat.chatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatArrayAdapter.add("Me: " + writeMessage);

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chatArrayAdapter.add(connectedDeviceName + ":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:

                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + connectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });
    private int gr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        getWidgetReferences();
        bindEventHandler();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        serverIntent = new Intent(this, lue.class);
        startActivityForResult(serverIntent,
                REQUEST_CONNECT_DEVICE_INSECURE);
        ensureDiscoverable();
    }


    private void getWidgetReferences() {
        lvMainChat = (ListView) findViewById(R.id.lvMainChat);
        etMain = (EditText) findViewById(R.id.etMain);
        btnSend = (Button) findViewById(R.id.btnSend);


    }

    private void bindEventHandler() {
        etMain.setOnEditorActionListener(mWriteListener);

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = etMain.getText().toString();
                sendMessage(message);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case PICKFILE_RESULT_CODE:
                if (gr == 1)
                {
                    String photo = data.getData().toString();
                    Log.i("photo", photo);
                    Intent iu = new Intent(Intent.ACTION_SEND);
                    iu.setType("image/*");
                    Uri uri = data.getData();
                    iu.putExtra(Intent.EXTRA_STREAM, uri);
                    iu.setPackage("com.android.bluetooth");

                    startActivity(iu);
                }
                else
                if(gr == 2)
                {
                    String photo = data.getData().toString();
                    Log.i("file", photo);
                    Intent iu = new Intent(Intent.ACTION_SEND);
                    iu.setType("file/*");
                    Uri uri = data.getData();
                    iu.putExtra(Intent.EXTRA_STREAM, uri);
                    iu.setPackage("com.android.bluetooth");

                    startActivity(iu);
                }




                break;

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(
                lue.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        chatService.connect(device, secure);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//            case R.id.secure_connect_scan:
//                serverIntent = new Intent(this, lue.class);
//                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
//                return true;
            case R.id.insecure_connect_scan:
                serverIntent = new Intent(this, lue.class);
                startActivityForResult(serverIntent,
                        REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
            case R.id.inеfo:
                Intent in = new Intent(this, About.class);
                startActivity(in);

                return true;
            case R.id.photo:
                isExternalStorageWritable();

                if(connectedDeviceName!= null)
                {
                    isExternalStorageWritable();
                    gr = 1;

                    Intent sharingIntent = new Intent(Intent.ACTION_PICK);
                    sharingIntent.setType("image/*");
                    startActivityForResult(sharingIntent,PICKFILE_RESULT_CODE);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "No one paired devices",Toast.LENGTH_SHORT).show();
                }

    return true;
            case R.id.file:
                isExternalStorageWritable();
                if(connectedDeviceName!= null)
                {
                    gr = 2;
                    isExternalStorageWritable();

                    Intent sharingIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    sharingIntent.setType("file/*");
                    startActivityForResult(sharingIntent,PICKFILE_RESULT_CODE);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "No one paired devices",Toast.LENGTH_SHORT).show();
                }

                return true;
        }


    return false;
    }




    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.i("Check","True");
            int i = 0;
            return true;
        }
        Log.i("Check","True");
        return false;
    }




    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        if (chatService.getState() != com.n1kko777.quickbluechat.chatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);

            outStringBuffer.setLength(0);
            etMain.setText(outStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private final void setStatus(int resId) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);

    }

    private void setupChat() {
        chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        lvMainChat.setAdapter(chatArrayAdapter);

        chatService = new chatService(this, handler);

        outStringBuffer = new StringBuffer("");
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (chatService == null)
                setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (chatService != null) {
            if (chatService.getState() == com.n1kko777.quickbluechat.chatService.STATE_NONE) {
                chatService.start();
            }
        }
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }

}
