package com.soulter.esndroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;

/*
Author : Soulter
2021© Copyright reserved
 */


public class ServerLoginActivity extends AppCompatActivity {

    EditText serverIpInp;
    EditText serverPortInp;
    Button addServerButton;
    ProgressBar progressBar;
    public static final int RESULT_CODE_ADDED_SERVER_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_login);

        serverIpInp = findViewById(R.id.add_server_ip);
        serverPortInp = findViewById(R.id.add_server_port);
        addServerButton = findViewById(R.id.add_server_btn);
        progressBar = findViewById(R.id.loading_server);
        final SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();

        addServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String serverIp = serverIpInp.getText().toString();
                String serverPort = serverPortInp.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if (!serverIp.equals("") && !serverPort.equals("")){
                                addServerButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        addServerButton.setText("检验中...");
                                    }
                                });
                                ESNSession esnSession = new ESNSession(serverIp + ":" + serverPort, "111", "111", 3000, new ISessionListener() {
                                    @Override
                                    public void notificationReceived(PackRespNotification packRespNotification) {

                                    }

                                    @Override
                                    public void sessionLogout(PackResult packResult) {

                                    }
                                });
                                esnSession.dispose();

                                addServerButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        addServerButton.setText("服务器可用！");
                                        try {
                                            Thread.sleep(300);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                });
                                progressBar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });

                                editor.putBoolean("is_server_added",true);
                                editor.apply();
                                editor.putString("server_url",serverIp+":"+serverPort);
                                editor.apply();

                                setResult(RESULT_CODE_ADDED_SERVER_ID);
                                finish();

                            }

                        }catch (Exception e){
                            progressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });

                            if (e.getMessage().equals("Login failed:Auth Failed")){

                                addServerButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        addServerButton.setText("服务器可用！");
                                        try {
                                            Thread.sleep(500);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                });


                                editor.putBoolean("is_server_added",true);
                                editor.apply();
                                editor.putString("server_url",serverIp+":"+serverPort);
                                editor.apply();

                                setResult(RESULT_CODE_ADDED_SERVER_ID);
                                finish();
                            }else{
                                serverPortInp.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        serverPortInp.setError("网络错误");
                                    }
                                });
                                addServerButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        addServerButton.setText("添加");
                                        addServerButton.setEnabled(true);
                                    }
                                });

                                e.printStackTrace();
                                Log.v("lwl",e.getMessage());
                            }

                        }

                    }
                }).start();
            }
        });
    }
}