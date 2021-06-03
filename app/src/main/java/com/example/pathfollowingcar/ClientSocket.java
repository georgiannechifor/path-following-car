package com.example.pathfollowingcar;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket extends AppCompatActivity {
    private static ClientSocket connection = null;


    private static final int SERVER_PORT = 6000;
    private static final String SERVER_ADDRESS = "192.168.43.253";
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Context appContext;

    public ClientSocket() {
    }

    public void showMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    static class ClientThread implements Runnable {
        @Override
        public void run() {
                try {
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    connection.showMessage("Android has connected to server socket");

                } catch (Exception e) {
                    connection.showMessage("Android has not connected to server socket");
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException interruptedException) {
//                        interruptedException.printStackTrace();
//                    }
                    //System.exit(1);
                    e.printStackTrace();
                }
        }
    }


    public static ClientSocket getInstance(Context context) {
        if(connection == null) {
            appContext = context;
            connection = new ClientSocket();
            new Thread(new ClientThread()).start();
        }
        return connection;
    }

    public void sendMessage(String str) {
        try {
            out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
