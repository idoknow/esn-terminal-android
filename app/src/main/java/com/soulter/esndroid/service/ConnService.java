package com.soulter.esndroid.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soulter.esndroid.R;
import com.soulter.esndroid.activity.MainActivity;
import com.soulter.esndroid.bean.ESNBean;
import com.soulter.esndroid.bean.IDBean;
import com.soulter.esndroid.bean.MsgBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;

/*
Author : Soulter
2021© Copyright reserved
 */


public class ConnService extends Service {

    public static boolean isRestartService = false;
    private int linkedCount = 0;
    private int notifyId = 1;
    private String focusedUser = "";
    public static final String CONN_SERVICE_NAME = "com.soulter.esndroid.service.ConnService";
    public static final String BC_TAG_NEW_PULLED_MSG = "NEW_PULLED_MSG";
    public static final String BC_TAG_LINKED_COUNT = "LINKED_COUNT";
    public static final String BC_TAG_LINKED_USER = "LINKED_USER";
    public static final String BC_TAG_LINKED_USER_TYPES = "LINKED_USER_TYPES";
    public static final String BC_TAG_FOCUSED_USER = "FOCUSED_USER";
    public static final String ACTION = "ConnService";
    public static final String IDENTIFY_CODE = "IDENTIFY_CODE";
    public static final int NEW_PULLED_MSG_CODE = 1;
    public static final int LINKED_COUNT_CODE = 2;
    public static final int LINKED_USER_CODE = 3;
    public static final int SEND_BASIC_INFO_CODE = 4;
    private boolean isAppRunInBG = false;
    HashMap<String, ESNBean> linkedAccountMap = new HashMap<>();

    private LocalBroadcastManager mLocalBroadcastManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showNotification();
    }

    private void showNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("connService", "connService", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, getAntiKillNotification());
    }

    private Notification getAntiKillNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.abc_vector_test)
                .setContentTitle("ESN正在后台运行")
                .setFullScreenIntent( null, true)
                .setPriority(Notification.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("connService");
        }
        Notification notification = builder.build();
        return notification;
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final SharedPreferences spfs = context.getSharedPreferences("spfs", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = context.getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();
            if (intent.getStringExtra(MainActivity.focus_user).equals("") && intent.getStringExtra(MainActivity.feature_type_tag).equals(MainActivity.onPullForRestart)){
                    new Thread(() -> {
                        for(String key:linkedAccountMap.keySet()){
                            try{
                                linkedAccountMap.get(key).getEsnSession().requestNotifications(0,100);
                            }catch (Exception e){
                                e.printStackTrace();
                                Looper.prepare();
                                Toast.makeText(ConnService.this,"后台时网络出现过异常，自动修复异常的代码暂时未编写，建议重启应用",Toast.LENGTH_SHORT).show();
                                Looper.loop();

                            }
                        }
                        receiveLinkedCount(linkedCount);
                        if (!focusedUser.equals("")){
                            receivedLinkedUserTypes(focusedUser,linkedAccountMap.get(focusedUser).getTypes().toString(),focusedUser);

                        }
                        Thread.interrupted();
                    }).start();
            }else{
                if (!focusedUser.equals("")){
                    Log.v("lwl","focusedUser has changed from "+focusedUser+" to "+intent.getStringExtra(MainActivity.focus_user));
                    focusedUser = intent.getStringExtra(MainActivity.focus_user);
                }

            }
            if (intent.getStringExtra(MainActivity.feature_type_tag).equals(MainActivity.UpdateFocusUser)){
                Log.v("lwl","focusedUser has changed from "+focusedUser+" to "+intent.getStringExtra(MainActivity.focus_user));
                focusedUser = intent.getStringExtra(MainActivity.focus_user);

            }
            if (intent.getStringExtra(MainActivity.feature_type_tag).equals(MainActivity.onClickNewAcBtn)){
                if (linkedAccountMap.get(intent.getStringExtra(MainActivity.focus_user)).getTypes().contains("account")) {
                ESNBean esnBean = linkedAccountMap.get(focusedUser);
                String type = intent.getStringExtra(MainActivity.NewAcType);
                String username = intent.getStringExtra(MainActivity.NewAcUser);
                String passw = intent.getStringExtra(MainActivity.NewAcPass);
                try {
                    esnBean.getEsnSession().addAccount(username,passw,type);
                    Gson gson = new Gson();
                    List<IDBean> idBeanList = getStoredID(spfs);
                    List<String> types = new ArrayList<>();
                    if (type.contains("pull"))
                        types.add("pull");
                    if (type.contains("push"))
                        types.add("push");
                    if (type.contains("account"))
                        types.add("account");
                    IDBean idBean = new IDBean(username,passw,types);
                    idBeanList.add(idBean);
                    String idJson = gson.toJson(idBeanList);
                    Log.v("lwl","logactivity  id:"+idJson);
                    editor.putString("id_beans",idJson);
                    editor.apply();
                    Toast.makeText(ConnService.this,"新建成功",Toast.LENGTH_LONG).show();

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(ConnService.this,"新建失败",Toast.LENGTH_LONG).show();
                }
                } else
                    Toast.makeText(ConnService.this, "当前未聚焦一个Account类型的账户", Toast.LENGTH_LONG).show();

            }
            if (intent.getStringExtra(MainActivity.feature_type_tag).equals(MainActivity.onClickRemoveAcBtn)){
                if (linkedAccountMap.get(intent.getStringExtra(MainActivity.focus_user)).getTypes().contains("account")){
                    ESNBean esnBean = linkedAccountMap.get(intent.getStringExtra(MainActivity.focus_user));
                    try {
                        esnBean.getEsnSession().removeAccount(intent.getStringExtra(MainActivity.RemoveAcUser),true);
                    } catch (Exception e) {
                        Toast.makeText(ConnService.this,"删除失败",Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }else
                    Toast.makeText(ConnService.this, "当前未聚焦一个Account类型的账户", Toast.LENGTH_LONG).show();


            }
            if (intent.getStringExtra(MainActivity.feature_type_tag).equals(MainActivity.onClickPushMsgBtn)){
                if (linkedAccountMap.get(intent.getStringExtra(MainActivity.focus_user)).getTypes().contains("push")) {

                    ESNBean esnBean = linkedAccountMap.get(intent.getStringExtra(MainActivity.focus_user));
                    try {
                        esnBean.getEsnSession().pushNotification(intent.getStringExtra(MainActivity.PushTarget), intent.getStringExtra(MainActivity.PushTitle), intent.getStringExtra(MainActivity.PushContent));
                    } catch (Exception e) {
                        Toast.makeText(ConnService.this, "推送失败", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }else
                    Toast.makeText(ConnService.this, "当前未聚焦一个Push类型的账户", Toast.LENGTH_LONG).show();

            }
            if (intent.getStringExtra(MainActivity.feature_type_tag).equals(MainActivity.onAppRunInBG)){
                isAppRunInBG = true;
            }
            if (intent.getStringExtra(MainActivity.feature_type_tag).equals(MainActivity.onAppRunInFG)){
                isAppRunInBG = false;
                Log.v("lwl","当前在前台");
                NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mLocalBroadcastManager = LocalBroadcastManager.getInstance(ConnService.this);
                mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,new IntentFilter(MainActivity.ACTION2));
                final SharedPreferences spfs = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE);
                List<IDBean> idBeans = getStoredID(spfs);
                if (idBeans.size()==0){

                    stopSelf();
                }
                String serverUrl = spfs.getString("server_url","null");
                sendBasicInfo("loading_linking");
                for (int i=0;i<idBeans.size();i++){
                    final int position = i;
                    try {

                        ESNSession esnSession = new ESNSession(serverUrl, idBeans.get(position).userName, idBeans.get(position).getPass(), 3000, new ISessionListener() {
                            @Override
                            public void notificationReceived(PackRespNotification packRespNotification) {
                                Log.v("lwl","id"+position);
                                receivedMsg(idBeans.get(position).userName,packRespNotification.Title,packRespNotification.Content,packRespNotification.Time,packRespNotification.Source,packRespNotification.Id);
                                Log.v("lwl","rsaKey:"+packRespNotification.Id+" title:"+packRespNotification.Title);
                            }
                            @Override
                            public void sessionLogout(PackResult packResult) {
                                if (!isRestartService){
                                    receivedMsg(idBeans.get(position).userName,"账户登出, 正在重连...","错误:"+packResult.Error,"","",-1);
                                        try{
                                            linkedAccountMap.get(idBeans.get(position).userName).getEsnSession().reConnect("39.100.5.139:3003", idBeans.get(position).userName, idBeans.get(position).getPass());
                                                receivedMsg(idBeans.get(position).userName,"账户重连成功"," ","","",-1);
                                                sendBasicInfo("loading_linked");
                                                linkedCount+=1;
                                                linkedAccountMap.get(idBeans.get(position).userName).getEsnSession().requestNotifications(0,500);
                                        }catch (Exception e){
                                            sendBasicInfo("service_restart");
                                            linkedAccountMap.clear();
                                            linkedAccountMap = null;
                                            stopSelf();
                                            e.printStackTrace();
                                        }
                                    linkedCount-=1;
                                    receiveLinkedCount(linkedCount);
                                }

                            }

                        });
                        linkedCount+=1;
                        receiveLinkedCount(linkedCount);
                        List<String> types = new ArrayList<>();
                        if (esnSession.can("pull")){
                            types.add("pull");
                        }
                        if (esnSession.can("push")){
                            types.add("push");
                        }
                        if (esnSession.can("account")){
                            types.add("account");
                        }
                        receivedLinkedUserTypes(idBeans.get(position).userName,types.toString(),focusedUser);
                        ESNBean esnBean = new ESNBean(idBeans.get(position).userName,esnSession,types);

                        linkedAccountMap.put(esnBean.getUsername(),esnBean);
                        esnSession.requestNotifications(0,500);


                    }catch (Exception e){
                        sendBasicInfo("loading_linked");
                        e.printStackTrace();
                    }
                }
                sendBasicInfo("loading_linked");

            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    public void sendBasicInfo(String msg){
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putExtra(IDENTIFY_CODE,SEND_BASIC_INFO_CODE);
        intent.putExtra("basic_info",msg);
        LocalBroadcastManager.getInstance(ConnService.this).sendBroadcast(intent);
    }

    public static List<IDBean> getStoredID(SharedPreferences sp){
        Gson gson = new Gson();
        List<IDBean> arrayList;
        String json = sp.getString("id_beans","");
        if (!json.equals("")){
            Type type = new TypeToken<List<IDBean>>() {
            }.getType();
            arrayList = gson.fromJson(json, type);
        }else {
            arrayList = new ArrayList<>();
        }
        return arrayList;
    }

    public void receivedMsg(String username,String title,String content,String time,String fromUser,int msgId){
        MsgBean msgBean= new MsgBean(username,title,content,time,fromUser,msgId);
        Bundle bundle = new Bundle();
        Log.v("lwl","接收到Msg:"+msgBean.getContent());
        bundle.putSerializable(BC_TAG_NEW_PULLED_MSG,msgBean);
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putExtra(BC_TAG_NEW_PULLED_MSG,bundle);
        intent.putExtra(IDENTIFY_CODE,NEW_PULLED_MSG_CODE);
        LocalBroadcastManager.getInstance(ConnService.this).sendBroadcast(intent);
        if (isAppRunInBG){
            Log.v("lwl","发送了通知");
            createNotification(username, title, content, fromUser);
        }

    }

    public void receiveLinkedCount(int linkedCount){
        Log.v("lwl","接收到count:"+linkedCount);
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putExtra(BC_TAG_LINKED_COUNT,linkedCount);
        intent.putExtra(IDENTIFY_CODE,LINKED_COUNT_CODE);
        LocalBroadcastManager.getInstance(ConnService.this).sendBroadcast(intent);
    }

    public void receivedLinkedUserTypes(String username,String types,String focusedUser){
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putExtra(BC_TAG_LINKED_USER,username);
        intent.putExtra(BC_TAG_LINKED_USER_TYPES,types);
        intent.putExtra(BC_TAG_FOCUSED_USER,focusedUser);
        intent.putExtra(IDENTIFY_CODE,LINKED_USER_CODE);
        LocalBroadcastManager.getInstance(ConnService.this).sendBroadcast(intent);
    }

    public void createNotification(String user, String title, String content, String fromUser){
        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("EsnMsgChannel", getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, "EsnMsgChannel");
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setNumber(2)
                .setTicker("ESN新通知")
                .setShowWhen(true)
                .setAutoCancel(true)
                .setFullScreenIntent( null, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentText(content);
        if (fromUser.equals("")&&user.equals("") && isAppRunInBG){
            builder.setContentTitle(title);
            builder.setOngoing(true);
        }
        else builder.setContentTitle(fromUser + "->" +user);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);
        //点击跳转的intent
        builder.setContentIntent(pIntent);
        Notification notification = builder.build();
        notificationManager.notify(0, notification);

    }
    @Override
    public void onDestroy() {
        Log.v("lwl","service destroy");
        try{
            if (linkedAccountMap!=null) {
                for (String key : linkedAccountMap.keySet()) {
                    linkedAccountMap.get(key).getEsnSession().dispose();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
