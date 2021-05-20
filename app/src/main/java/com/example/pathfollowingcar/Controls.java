package com.example.pathfollowingcar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("ClickableViewAccessibility")
public class Controls extends AppCompatActivity {

    private Button topBtn, bottomBtn, rightBtn, leftBtn;
    private ClientSocket connection;

    final class workerThread implements Runnable {
        private final String btMsg;
        public workerThread(String msg) { btMsg = msg; }
        public void run() {
            connection.sendMessage("[BUTTONS]" + btMsg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Buttons Control");
        setContentView(R.layout.activity_controls);

        topBtn = findViewById(R.id.up);
        bottomBtn = findViewById(R.id.down);
        leftBtn = findViewById(R.id.left);
        rightBtn = findViewById(R.id.right);
        connection = ClientSocket.getInstance(getApplicationContext());

        
        topBtn.setOnTouchListener((view, motionEvent) -> {
            try {
                switch (motionEvent.getAction()) {
                    case 0:
                    case 2:
                        Thread.sleep(60);
                        (new Thread(new workerThread("forward*START*"))).start();
                        break;
                    case 1:
                    case 3:
                        Thread.sleep(60);
                        (new Thread(new workerThread("forward*STOP*"))).start();
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        });

        bottomBtn.setOnTouchListener((view, motionEvent) -> {
            try {
                switch (motionEvent.getAction()) {
                    case 0:
                    case 2:
                        Thread.sleep(60);
                        (new Thread(new workerThread("backwards*START*"))).start();
                        break;
                    case 1:
                    case 3:
                        Thread.sleep(60);
                        (new Thread(new workerThread("backwards*STOP*"))).start();
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        });

        leftBtn.setOnTouchListener((v, event) -> {
            try {
                switch (event.getAction()) {
                    case 0:
                    case 2:
                        Thread.sleep(60);
                        (new Thread(new workerThread("left*START*"))).start();
                        break;
                    case 1:
                    case 3:
                        Thread.sleep(60);
                        (new Thread(new workerThread("left*STOP*"))).start();
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        });

        rightBtn.setOnTouchListener((v, event) -> {
            try {
                switch (event.getAction()) {
                    case 0:
                    case 2:
                        Thread.sleep(60);
                        (new Thread(new workerThread("right*START*"))).start();
                        break;
                    case 1:
                    case 3:
                        Thread.sleep(60);
                        (new Thread(new workerThread("right*STOP*"))).start();
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
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
        switch (item.getItemId()) {
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

}