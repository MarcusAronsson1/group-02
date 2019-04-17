package org.miniproject.safesweeper;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btn1;
    TextView textView1;
    SeekBar throttleBar;
    SeekBar steeringBar;
    TextView throttleText;
    TextView steeringText;
    TextView connectionTextView;
    ToggleButton steeringToggle;

    public static final int THROTTLE_MIN = -30;
    public static final int THROTTLE_DEFAULT = 30;

    public static final int STEERING_MIN = -50;
    public static final int STEERING_DEFAULT = 50;

    private String macAddress;
    BluetoothAdapter mBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        macAddress = getIntent().getStringExtra("MAC");

        btn1 = (Button) findViewById(R.id.btn1);
        textView1 = (TextView) findViewById(R.id.textView1);
        throttleBar = (SeekBar) findViewById(R.id.throttleBar);
        steeringBar = (SeekBar) findViewById(R.id.steeringBar);
        throttleText = (TextView) findViewById(R.id.throttleText);
        steeringText = (TextView) findViewById(R.id.steeringText);
        connectionTextView = (TextView) findViewById(R.id.connectionTextView);
        steeringToggle = (ToggleButton) findViewById(R.id.steeringToggle);

        new ConnectBT().execute();
        //toggle switch to go between manual steering and automatic steering, starts with manual
        steeringToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(steeringToggle.isChecked()){
                    textView1.setText("Automatic");
                    auto();
                    //implement auto sweeping here
                    //just use the auto we have for now
                    //send a single character via serial which will kick start the auto? a for auto?
                }else
                    textView1.setText("Manual");
                //implement manual steering here uhhhhhh fuck i'm assuming also single characters
                //but we gotta do all this shit man
                //with the drag bar
                //fuck me
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView1.setText("It works");
            }
        });

    }

    private void auto(){
        if (btSocket!=null)
        {
            try
            {
                Toast.makeText(MainActivity.this, "It just works", Toast.LENGTH_SHORT).show();
                btSocket.getOutputStream().write("0".toString().getBytes());
            }
            catch (IOException e)
            {
                textView1.setText("Error");
            }
        }

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            connectionTextView.setText("Connecting.. Please wait!");
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    mBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice mBluetoothDevice = mBluetooth.getRemoteDevice(macAddress);//connects to the device's address and checks if it's available
                    btSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            connectionTextView.setText("");

            if (!ConnectSuccess)
            {
                Toast.makeText(MainActivity.this, "Could not connect..", Toast.LENGTH_LONG).show();
              //  Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
               // startActivity(intent);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                isBtConnected = true;

                throttleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        throttleText.setText((THROTTLE_MIN + progress) + "");
                        steeringBar.getProgress();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        throttleBar.setProgress(THROTTLE_DEFAULT);
                    }
                });

                steeringBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        steeringText.setText((STEERING_MIN + progress) + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        steeringBar.setProgress(STEERING_DEFAULT);
                    }
                });

            }
        }
    }
}