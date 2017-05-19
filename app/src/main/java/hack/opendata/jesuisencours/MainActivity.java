package hack.opendata.jesuisencours;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import hack.opendata.jesuisencours.Model.Course;
import hack.opendata.jesuisencours.Service.CourseService;

public class MainActivity extends AppCompatActivity implements CourseService.CurrentCourseCallback, CourseService.ValidatedCourseCallback {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1000;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Todo ?
    private TextView courseNameTxt;
    private Button acceptButton;

    private Course course;
    private String date;
    private Button discoverButton;
    private ConnectedThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        courseNameTxt = (TextView) findViewById(R.id.courseName);
        acceptButton = (Button) findViewById(R.id.acceptButton);
        discoverButton = (Button) findViewById(R.id.discover);

        final CourseService courseService = new CourseService();
        courseService.getCurrentCourse(this);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * BLUETOOTH DEJA
                 */
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address

                        Log.e(TAG, "DEVICE : " + deviceName + " " + deviceHardwareAddress);


                        if(deviceName != null && deviceName.equals("yo it is me")){ // Todo harcoded
                            Log.e(TAG, "Try to connect...");

                            new ConnectThread(device).start();
                        }

                    }
                }
                /**
                 *
                 */


                // Register for broadcasts when a device is discovered.
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter);

                mBluetoothAdapter.cancelDiscovery();
                mBluetoothAdapter.startDiscovery();

            }
        });

        if (mBluetoothAdapter != null) {
            // Device does not support


            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            // Establish connection to the proxy.
            //mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET);


        } else {
            Log.e(TAG, "Bluetooth not available !");


        }

        // Close proxy connection after use.
        //mBluetoothAdapter.closeProfileProxy(mBluetoothHeadset);


    }

    BluetoothHeadset mBluetoothHeadset;

    // Get the default adapter
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
                Log.e(TAG, "onServiceConnected: ");
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
                Log.e(TAG, "onServiceDisconnected:");
            }
        }
    };

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

        if (requestCode == this.REQUEST_ENABLE_BT) {
            Log.e(TAG, "Bluethooth enabled");


        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                //new ConnectThread(device);

                Log.e(TAG, "onReceive: DEVICE DISCOVERED " + deviceName + " " + deviceHardwareAddress);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);

    }

    @Override
    public void getCurrentCourse(Course course, String date) {

        this.course = course;
        this.date = date;

        if (course != null && !course.getName().equals(""))
            courseNameTxt.setText(course.getName());

        acceptButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void courseValidated() {
        acceptButton.setText("Bon cours !");
        acceptButton.setBackgroundColor(getColor(R.color.colorGreen));
        acceptButton.setEnabled(false);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.

                Log.e(TAG, "Attempt connecting");
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }


    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        Log.e(TAG, "COnnected !");

        String message = JSECApp.currentUserId;
        byte[] send = message.getBytes();
        thread = new ConnectedThread(mmSocket);
        thread.write(send);
    }



    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: " + msg.toString());
        }
    };

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            MainActivity.MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        MainActivity.MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MainActivity.MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


}