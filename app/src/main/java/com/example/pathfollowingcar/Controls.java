package com.example.pathfollowingcar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static com.example.pathfollowingcar.Controls.MotorState.BACKWARD;
import static com.example.pathfollowingcar.Controls.MotorState.FORWARD;
import static com.example.pathfollowingcar.Controls.MotorState.STOP;

@SuppressLint("ClickableViewAccessibility")
public class Controls extends AppCompatActivity {

    private Button topBtn, bottomBtn, connectBtn, rightBtn, leftBtn;
    private MotorState leftMotorState = STOP;
    private MotorState rightMotorState = STOP;
    private BluetoothDevice MiDevice;
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;
    private boolean isConnected = false;

    protected enum MotorState {
        FORWARD(2),
        STOP(0),
        BACKWARD(1);
        private int intValue;

        MotorState(int value) {
            intValue = value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Buttons Control");
        setContentView(R.layout.activity_controls);

        topBtn = (Button) findViewById(R.id.up);
        bottomBtn = findViewById(R.id.down);
        leftBtn = findViewById(R.id.left);
        rightBtn = findViewById(R.id.right);
        connectBtn = (Button) findViewById(R.id.connectButton);

        topBtn.setEnabled(false);
        bottomBtn.setEnabled(false);
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);

        topBtn.setOnTouchListener((view, motionEvent) -> {
            switch(motionEvent.getAction()) {
                case 0:
                    upButtonStartTouch();
                    break;
                case 1:
                    upButtonEndTouch();
                    break;
            }
            return false;
        });

        bottomBtn.setOnTouchListener((view, motionEvent) -> {
            switch(motionEvent.getAction()) {
                case 0:
                    downButtonStartTouch();
                    break;
                case 1:
                    downButtonEndTouch();
                    break;
            }
            return false;
        });

        leftBtn.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case 0:
                    moveRightMotor(FORWARD);
                    moveLeftMotor(BACKWARD);
                    break;
                case 1:
                    moveRightMotor(rightMotorState);
                    moveLeftMotor(leftMotorState);
                    break;
            }
            return false;
        });

        rightBtn.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case 0:
                    moveRightMotor(BACKWARD);
                    moveLeftMotor(FORWARD);
                    break;
                case 1:
                    moveRightMotor(rightMotorState);
                    moveLeftMotor(leftMotorState);
                    break;
            }
            return false;
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // BLUETOOTH CONNECT TO CAR
                adapter = BluetoothAdapter.getDefaultAdapter();

                if(!adapter.isEnabled()) {
                    Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enable, 0);
                }

                Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                if(pairedDevices.size() > 0) {
                    for(BluetoothDevice device : pairedDevices) {
                        if(device.getName().equals("HC-06")) {
                            MiDevice = device;
                            Toast.makeText(getApplicationContext(), "Device paired", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                try {
                    if(MiDevice == null || MiDevice.createRfcommSocketToServiceRecord(uuid) == null) {
                        Toast.makeText(getApplicationContext(), "Device is not paired to the car",  Toast.LENGTH_SHORT).show();
                    } else {
                        socket = MiDevice.createRfcommSocketToServiceRecord(uuid);

                        socket.connect();
                        out = socket.getOutputStream();
                        in = socket.getInputStream();

                        Toast.makeText(getApplicationContext(), "Connection established", Toast.LENGTH_SHORT).show();
                        isConnected = true;

                        topBtn.setEnabled(true);
                        bottomBtn.setEnabled(true);
                        leftBtn.setEnabled(true);
                        rightBtn.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.draw:
                intent = new Intent(getApplicationContext(), Drawing.class);
                startActivity(intent);
                return true;
            case R.id.control:
                return true;
            case R.id.voice:
                intent = new Intent(getApplicationContext(), Voice.class);
                startActivity(intent);
                return true;
            case R.id.maps:
                intent = new Intent(getApplicationContext(), Maps.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void upButtonStartTouch() {
        moveRightMotor(FORWARD);
        moveLeftMotor(FORWARD);
    }

    void upButtonEndTouch() {
        moveRightMotor(STOP);
        moveLeftMotor(STOP);
    }

    void downButtonStartTouch() {
        moveRightMotor(BACKWARD);
        moveLeftMotor(BACKWARD);
    }

    void downButtonEndTouch() {
        moveRightMotor(STOP);
        moveLeftMotor(STOP);
    }

    void moveRightMotor(MotorState state) {
        rightMotorState = state;
        if(out != null) {
            try {
                out.write("rightMotor".getBytes());
                out.write(state.intValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void moveLeftMotor(MotorState state) {
        leftMotorState = state;

        if(out != null) {
            try {
                out.write("leftMotor".getBytes());
                out.write(state.intValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}