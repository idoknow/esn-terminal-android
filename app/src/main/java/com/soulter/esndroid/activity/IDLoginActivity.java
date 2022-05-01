package com.soulter.esndroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.soulter.esndroid.bean.IDBean;
import com.soulter.esndroid.R;

import java.util.ArrayList;
import java.util.List;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;

import static com.soulter.esndroid.service.ConnService.getStoredID;

/*
Author : Soulter
2021© Copyright reserved
 */


public class IDLoginActivity extends AppCompatActivity {

    EditText idUserInput;
    EditText idPassInput;
    Button addIdButton;
    ProgressBar progressBar;
    public static final int RESULT_CODE_ADDED_ID = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_d_login);


        idUserInput = findViewById(R.id.add_id_dialog_user);
        idPassInput = findViewById(R.id.add_id_dialog_pass);
        addIdButton = findViewById(R.id.add_id_btn);
        progressBar = findViewById(R.id.loading_id);
        final SharedPreferences spfs = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();

        if (!spfs.getBoolean("is_server_added",false)){
            idPassInput.setEnabled(false);
            idUserInput.setEnabled(false);
            addIdButton.setEnabled(false);
            addIdButton.setText("请先去添加服务器");
        }else {
            addIdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    String username = idUserInput.getText().toString();
                    String passw = idPassInput.getText().toString();

                    if(!username.equals("") && !passw.equals("")){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    List<String> types = new ArrayList<>();
                                    String serverUrl = spfs.getString("server_url","null");
                                    ESNSession esnSession = new ESNSession(serverUrl, username, passw, 3000, new ISessionListener() {
                                        @Override
                                        public void notificationReceived(PackRespNotification packRespNotification) {

                                        }

                                        @Override
                                        public void sessionLogout(PackResult packResult) {

                                        }
                                    });
                                    if (esnSession.can("pull")){
                                        types.add("pull");
                                    }
                                    if (esnSession.can("push")){
                                        types.add("push");
                                    }
                                    if (esnSession.can("account")){
                                        types.add("account");
                                    }


                                    Gson gson = new Gson();
                                    List<IDBean> idBeanList = getStoredID(spfs);
                                    Log.v("lwl","type:"+types.toString());
                                    IDBean idBean = new IDBean(username,passw,types);
                                    idBeanList.add(idBean);
                                    String idJson = gson.toJson(idBeanList);
                                    Log.v("lwl","idJson:"+idJson);
                                    editor.putString("id_beans",idJson);
                                    editor.apply();
                                    esnSession.dispose();
                                    setResult(RESULT_CODE_ADDED_ID);
                                    finish();


                                }catch (Exception e){
                                    e.printStackTrace();
                                    progressBar.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                    idPassInput.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            idPassInput.setError("添加失败了，账户不存在或账号密码错误。");
                                        }
                                    });

                                }

                            }
                        }).start();

                    }
                }
            });
        }

    }
}