package com.n1kko777.quickbluechat;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.OpenableColumns;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
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




public class MainActivity extends AppCompatActivity {

    private static String readMessage = null;

    public static final String ACTION_DEMAND = "com.androidweardocs.ACTION_DEMAND";
    public static final String EXTRA_MESSAGE = "com.androidweardocs.EXTRA_MESSAGE";
    public static final String EXTRA_VOICE_REPLY = "com.androidweardocs.EXTRA_VOICE_REPLY";

    private Intent sharingIntent;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private MenuItem item;

    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int PICKFILE_RESULT_CODE = 123;

    private ListView lvMainChat;
    private EditText etMain;
    private Button btnSend;

    private Intent serverIntent = null;

    String connectedDeviceName = null;

    private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private com.n1kko777.quickbluechat.chatService chatService = null;

    private int notificationId = 1 ;
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
                    chatArrayAdapter.add(getResources().getString(R.string.me) + writeMessage);

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    readMessage = new String(readBuf, 0, msg.arg1);
                    chatArrayAdapter.add(connectedDeviceName + ": " + readMessage);




                    long[] a;
                    if(item.isChecked())
                    {
                        a = new long[]{1000, 1000};
                    }
                    else
                    {
                        a = new long[]{0, 0};
                    }

                    Intent demandIntent = new Intent(MainActivity.this, DemandIntentReceiver.class)
                            .putExtra(EXTRA_MESSAGE, "Reply selected.")
                            .setAction(ACTION_DEMAND);

                    PendingIntent demandPendingIntent =
                            PendingIntent.getBroadcast(MainActivity.this, 0, demandIntent, 0);

                    String replyLabel = getResources().getString(R.string.reply_label);
                    String[] replyChoices = getResources().getStringArray(R.array.reply_choices);

                    RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                            .setLabel(replyLabel)
                            .setChoices(replyChoices)
                            .build();

                    NotificationCompat.Action replyAction =
                            new NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                                    getString(R.string.reply_label), demandPendingIntent)
                                    .addRemoteInput(remoteInput)
                                    .build();

                    NotificationCompat.WearableExtender wearableExtender =
                            new NotificationCompat.WearableExtender()
                                    .addAction(replyAction);

                    Notification notification =
                            new NotificationCompat.Builder(MainActivity.this)
                                    .setContentTitle(connectedDeviceName)
                                    .setContentText(readMessage)
                                    .setSmallIcon(R.drawable.logo)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.sad))
                                    .setVibrate(a)
                                    .extend(wearableExtender)
                                    .build();

                    NotificationManagerCompat notificationManager =
                            NotificationManagerCompat.from(MainActivity.this);

                    notificationManager.notify(notificationId, notification);

                    // Register the local broadcast receiver for the users demand.
                    IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
                    MessageReceiver messageReceiver = new MessageReceiver();
                    LocalBroadcastManager.getInstance(MainActivity.this).

                            registerReceiver(messageReceiver, messageFilter);
                    break;
                case MESSAGE_DEVICE_NAME:

                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.ct) + " " + connectedDeviceName,
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        getWidgetReferences();
        bindEventHandler();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.bina),
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
                try {
                    Intent iu = new Intent(Intent.ACTION_SEND);
                    iu.setType("*/*");
                    Uri uri = data.getData();

                    Cursor returnCursor =
                            getContentResolver().query(uri, null, null, null, null);

                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    iu.putExtra(Intent.EXTRA_STREAM, uri);
                    iu.setPackage("com.android.bluetooth");
                    startActivity(iu);
                    sendMessage(returnCursor.getString(nameIndex));


                } catch (Exception e) {
                    onResume();
                    Toast.makeText(this, "Please select a file.",
                            Toast.LENGTH_SHORT).show();
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
        SharedPreferences settings = getSharedPreferences("settings", 0);
        boolean isChecked = settings.getBoolean("checkbox", true);
        item = menu.findItem(R.id.action_check);
        item.setChecked(isChecked);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_check:
                item.setChecked(!item.isChecked());
                SharedPreferences settings = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("checkbox", item.isChecked());
                editor.commit();

                return true;

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

            case R.id.file:
                isExternalStorageWritable();
                if (connectedDeviceName != null) {
                    isExternalStorageWritable();

                    sharingIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    sharingIntent.setType("*/*");
                    startActivityForResult(sharingIntent, PICKFILE_RESULT_CODE);
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.nopair), Toast.LENGTH_SHORT).show();
                }

                return true;
        }


        return false;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.i("Check", "True");
            int i = 0;
            return true;
        }
        Log.i("Check", "True");
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

    public void sendMessage(String message) {
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


    // Class to receive demand text from the wearable demand receiver
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Display the received demand

            String demand = intent.getStringExtra("reply");
            sendMessage(demand);
        }
    }
}
