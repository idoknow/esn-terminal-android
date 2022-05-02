package com.soulter.esndroid.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.soulter.esndroid.service.ConnService;
import com.soulter.esndroid.bean.MsgBean;
import com.soulter.esndroid.R;
import com.soulter.esndroid.Utils;
import com.soulter.esndroid.adapter.MsgListAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.soulter.esndroid.service.ConnService.getStoredID;

/*
Author : Soulter
2021© Copyright reserved
 */


public class MainActivity extends AppCompatActivity {

    ImageView addIDEntry;
    String focusedUser;
    String userListDsp = "";
    TextView idLinkedStats;
    TextView idFocused;
    ListView msgListView ;
    ProgressBar loadingLink;

    FloatingActionButton newAccountBtn;
    FloatingActionButton removeAccountBtn;
    FloatingActionButton pushMsgBtn;
    FloatingActionButton pullMsgBtn;
    FloatingActionsMenu openFeaturesFab;

    CardView mainCvM;
    CardView mainAcAddServer;
    CardView mainAcAddID;
    TextView mainAcAddServerTv;
    List<MsgBean> msgBeans = new ArrayList<>();

    public static final String feature_type_tag = "feature_type_tag";
    public static final String onClickNewAcBtn = "onClickNewAcBtn";
    public static final String NewAcUser = "NewAcUser";
    public static final String NewAcPass = "NewAcPass";
    public static final String NewAcType = "NewAcType";

    public static final String onClickRemoveAcBtn = "onClickRemoveAcBtn";
    public static final String RemoveAcUser = "RemoveAcUser";

    public static final String onClickPushMsgBtn = "onClickPushMsgBtn";
    public static final String PushTitle = "PushTitle";
    public static final String PushContent = "PushContent";
    public static final String PushTarget = "PushTarget";

    public static final String focus_user="focus_user";
    public static final String ACTION2="MainActivityAction";
    private int linkedCount = 0;

    public static final String onPullForRestart = "onPullForRestart";
    public static final String UpdateFocusUser = "UpdateFocusUser";

    public static final String onAppRunInBG = "onAppRunInBG";
    public static final String onAppRunInFG = "onAppRunInFG";

    public static final int REQUEST_CODE_ADD_ID = 1;
    public static final int REQUEST_CODE_MAIN_ADD_SERVER = 4;
    public static final int REQUEST_CODE_MAIN_ADD_ID = 5;

    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,new IntentFilter(ConnService.ACTION));


        loadingLink = findViewById(R.id.main_loading_link);
        idLinkedStats = findViewById(R.id.id_linked_stats);
        idFocused = findViewById(R.id.id_focused_stats);
        addIDEntry = findViewById(R.id.id_mng_entry);
        newAccountBtn = findViewById(R.id.new_account_feature);
        removeAccountBtn = findViewById(R.id.rm_id_feature);
        openFeaturesFab = findViewById(R.id.open_feature_fab);
        openFeaturesFab.setVisibility(View.GONE);
        pushMsgBtn = findViewById(R.id.push_msg_feature);
        pullMsgBtn = findViewById(R.id.pull_msg_feature);
        newAccountBtn.setVisibility(View.GONE);
        removeAccountBtn.setVisibility(View.GONE);
        pushMsgBtn.setVisibility(View.GONE);
        pullMsgBtn.setVisibility(View.GONE);
        msgListView = findViewById(R.id.msg_list_view);

        mainCvM = findViewById(R.id.main_card_view_main);
        mainAcAddID = findViewById(R.id.mainac_add_id);
        mainAcAddServer = findViewById(R.id.mainac_add_server);
        mainAcAddServerTv = findViewById(R.id.mainac_add_server_tv);

        MsgListAdapter msgListAdapter = new MsgListAdapter(this,R.layout.msg_list_member,msgBeans);
        msgListView.setAdapter(msgListAdapter);
        msgListView.setDivider(null);


        final SharedPreferences spfs = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE);

        if (getStoredID(spfs).size() == 0 || !spfs.getBoolean("is_server_added",false)){
            mainAcAddServer.setVisibility(View.VISIBLE);
            if (spfs.getBoolean("is_server_added",false)){
                mainAcAddServerTv.setText("添加服务器...已添加");
            }
            mainAcAddServer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,ServerLoginActivity.class);
                    startActivityForResult(intent,REQUEST_CODE_MAIN_ADD_SERVER);
                }
            });
            mainAcAddID.setVisibility(View.VISIBLE);
            mainAcAddID.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this,IDLoginActivity.class);
                startActivityForResult(intent,REQUEST_CODE_MAIN_ADD_ID);
            });
        }else{
            mainCvM.setVisibility(View.VISIBLE);
            msgListView.setVisibility(View.VISIBLE);
        }

        if (focusedUser==null){
            if (!Utils.isServiceRunning(this,ConnService.CONN_SERVICE_NAME)){
                if (spfs.getBoolean("is_server_added",false)){
                    if (getStoredID(spfs).size()>0){
                        Intent serviceIntent = new Intent(MainActivity.this,ConnService.class);
                        startService(serviceIntent);
                        Toast.makeText(this,"服务启动成功",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this,"服务暂未启动，因为没有添加用户",Toast.LENGTH_SHORT).show();
                    }
                }else
                    Toast.makeText(this,"服务暂未启动，因为没有添加服务器",Toast.LENGTH_SHORT).show();

            }else{
                Intent intent = new Intent();
                intent.setAction(ACTION2);
                intent.putExtra(focus_user,"");
                intent.putExtra(feature_type_tag,onPullForRestart);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }

        addIDEntry.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,AddIDActivity.class);
            startActivityForResult(intent,REQUEST_CODE_ADD_ID);
        });
        idLinkedStats.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            if (userListDsp == null || userListDsp.toString().equals("")){
                builder.setTitle("状态")
                        .setMessage("暂无信息")
                        .show();
            }else{
                builder.setTitle("状态")
                        .setMessage(userListDsp)
                        .show();
            }

        });

        newAccountBtn.setOnClickListener(view -> {
            if (focusedUser != null){
                AlertDialog.Builder addIDDialog = new AlertDialog.Builder(MainActivity.this);
                final View newAccountView = (LinearLayout)getLayoutInflater().inflate(R.layout.new_a_account_dialog,null);

                addIDDialog
                        .setView(newAccountView)
                        .setNegativeButton("添加", (dialogInterface, i) -> {
                            EditText idUserInput = (EditText)newAccountView.findViewById(R.id.new_ac_dialog_user);
                            EditText idPassInput = (EditText)newAccountView.findViewById(R.id.new_ac_dialog_pass);
                            EditText idTypeInput = (EditText)newAccountView.findViewById(R.id.new_ac_dialog_type);

                            String username = idUserInput.getText().toString();
                            String passw = idPassInput.getText().toString();
                            String type = idTypeInput.getText().toString();

                            if(!username.equals("") && !passw.equals("") && !type.equals("")){
                                if (focusedUser!=null){
                                        Intent intent = new Intent();
                                        intent.setAction(ACTION2);
                                        intent.putExtra(focus_user,focusedUser);
                                        intent.putExtra(feature_type_tag,onClickNewAcBtn);
                                        intent.putExtra(NewAcUser,username);
                                        intent.putExtra(NewAcPass,passw);
                                        intent.putExtra(NewAcType,type);
                                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                                }else Toast.makeText(MainActivity.this,"当前未聚焦账户",Toast.LENGTH_LONG).show();
                            }
                        }).show();
            }

        });
        removeAccountBtn.setOnClickListener(view -> {
            if (focusedUser!=null){
                AlertDialog.Builder rmAccountDialog = new AlertDialog.Builder(MainActivity.this);
                final View rmAccountView = (LinearLayout)getLayoutInflater().inflate(R.layout.remove_a_account_dialog,null);

                rmAccountDialog
                        .setView(rmAccountView)
                        .setNegativeButton("推送", (dialogInterface, i) -> {
                            EditText username = (EditText)rmAccountView.findViewById(R.id.rm_account_username);
                            String usernameStr = username.getText().toString();


                            if(!usernameStr.equals("")) {
                                if (focusedUser != null) {
                                    Intent intent = new Intent();
                                    intent.setAction(ACTION2);
                                    intent.putExtra(focus_user,focusedUser);
                                    intent.putExtra(feature_type_tag,onClickRemoveAcBtn);
                                    intent.putExtra(RemoveAcUser,usernameStr);
                                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                                }
                            }

                        }).show();
            }

        });
        pushMsgBtn.setOnClickListener(view -> {
            if (focusedUser!=null){
                AlertDialog.Builder pushMsgDialog = new AlertDialog.Builder(MainActivity.this);
                final View pushMsgView = (LinearLayout)getLayoutInflater().inflate(R.layout.push_a_msg_dialog,null);

                pushMsgDialog
                        .setView(pushMsgView)
                        .setNegativeButton("推送", (dialogInterface, i) -> {
                            EditText titleInput = (EditText)pushMsgView.findViewById(R.id.push_msg_title);
                            EditText contentInput = (EditText)pushMsgView.findViewById(R.id.push_msg_content);
                            EditText targetInput = (EditText)pushMsgView.findViewById(R.id.push_msg_target);

                            String title = titleInput.getText().toString();
                            String content = contentInput.getText().toString();
                            String target = targetInput.getText().toString();

                            if(!title.equals("") && !content.equals("") && !target.equals("")) {
                                if (focusedUser != null) {
                                    Intent intent = new Intent();
                                    intent.setAction(ACTION2);
                                    intent.putExtra(focus_user,focusedUser);
                                    intent.putExtra(feature_type_tag,onClickPushMsgBtn);
                                    intent.putExtra(PushTitle,title);
                                    intent.putExtra(PushContent,content);
                                    intent.putExtra(PushTarget,target);
                                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                                }
                            }

                        }).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final SharedPreferences spfs = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE);
        if (requestCode == REQUEST_CODE_ADD_ID && resultCode == AddIDActivity.RESULT_CODE_SELECT_ID){
            if (data.getStringExtra("ID_user")!=null && data.getStringExtra("ID_types")!=null){
                focusedUser = data.getStringExtra("ID_user");
                openFeaturesFab.setVisibility(View.VISIBLE);
                displayFeature(data.getStringExtra("ID_types"));

                idFocused.setText("| 聚焦:"+focusedUser+" 类型:"+data.getStringExtra("ID_types"));

                newAccountBtn.setVisibility(View.GONE);
                removeAccountBtn.setVisibility(View.GONE);
                pushMsgBtn.setVisibility(View.GONE);
                if (data.getStringExtra("ID_types").contains("push")){
                    pushMsgBtn.setVisibility(View.VISIBLE);
                }
                if (data.getStringExtra("ID_types").contains("account")){
                    removeAccountBtn.setVisibility(View.VISIBLE);
                    newAccountBtn.setVisibility(View.VISIBLE);
                }
                if (Utils.isServiceRunning(MainActivity.this,ConnService.CONN_SERVICE_NAME)){
                    Toast.makeText(this,"服务正在运行",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(ACTION2);
                    intent.putExtra(focus_user,focusedUser);
                    intent.putExtra(feature_type_tag,UpdateFocusUser);
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                }else{
                    Intent serviceIntent = new Intent(MainActivity.this,ConnService.class);
                    startService(serviceIntent);
                    Toast.makeText(this,"服务启动",Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == REQUEST_CODE_MAIN_ADD_SERVER && resultCode == ServerLoginActivity.RESULT_CODE_ADDED_SERVER_ID){
            if (spfs.getBoolean("is_server_added",false)){
                mainAcAddServerTv.setText("添加服务器...已添加");
            }
        }
        if (requestCode == REQUEST_CODE_MAIN_ADD_ID && resultCode == IDLoginActivity.RESULT_CODE_ADDED_ID){
            if (spfs.getBoolean("is_server_added",false) && getStoredID(spfs).size()>0){
                Toast.makeText(MainActivity.this,"基础设置完成",Toast.LENGTH_LONG).show();
                mainAcAddID.setVisibility(View.GONE);
                mainAcAddServer.setVisibility(View.GONE);
                mainCvM.setVisibility(View.VISIBLE);
                msgListView.setVisibility(View.VISIBLE);
                if (!Utils.isServiceRunning(this,ConnService.CONN_SERVICE_NAME)){
                    Intent intent = new Intent(MainActivity.this,ConnService.class);
                    startService(intent);
                }
            }
        }

    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("lwl","Mreceived:"+intent.getIntExtra(ConnService.IDENTIFY_CODE,-1));
            if (intent.getIntExtra(ConnService.IDENTIFY_CODE,-1) == ConnService.SEND_BASIC_INFO_CODE){
                if (intent.getStringExtra("basic_info").equals("service_restart")){
                    Log.v("lwl","receive service restart");
                    msgBeans.clear();
                    Log.v("lwl","msgbeans"+msgBeans.toString());
                    MsgListAdapter msgListAdapter = new MsgListAdapter(MainActivity.this,R.layout.msg_list_member,msgBeans);
                    msgListView.setAdapter(msgListAdapter);
                    loadingLink.setVisibility(View.GONE);
                    Intent intent1 = new Intent(MainActivity.this,ConnService.class);
                    startService(intent1);
                }
                if (intent.getStringExtra("basic_info").equals("loading_linking")){
                    loadingLink.setVisibility(View.VISIBLE);
                }
                if (intent.getStringExtra("basic_info").equals("loading_linked")){
                    loadingLink.setVisibility(View.GONE);
                }
            }
            if (intent.getIntExtra(ConnService.IDENTIFY_CODE,-1) == ConnService.NEW_PULLED_MSG_CODE){

                Bundle bundle = intent.getBundleExtra(ConnService.BC_TAG_NEW_PULLED_MSG);
                MsgBean msgBean = (MsgBean) bundle.getSerializable(ConnService.BC_TAG_NEW_PULLED_MSG);
                receivedMsg(msgBean);
            }
            if (intent.getIntExtra(ConnService.IDENTIFY_CODE,-1) == ConnService.LINKED_COUNT_CODE){
                idLinkedStats.post(() -> idLinkedStats.setText("已连接数:"+intent.getIntExtra(ConnService.BC_TAG_LINKED_COUNT,0)+" "));
            }
            if (intent.getIntExtra(ConnService.IDENTIFY_CODE,-1) == ConnService.LINKED_USER_CODE){
                String username = intent.getStringExtra(ConnService.BC_TAG_LINKED_USER);
                String types = intent.getStringExtra(ConnService.BC_TAG_LINKED_USER_TYPES);
                if (focusedUser != null) {

                    if (focusedUser.equals(username)) {
                        idFocused.post(() -> idFocused.setText(" | 聚焦:" + focusedUser + " 类型" + types));
                        openFeaturesFab.setVisibility(View.VISIBLE);
                        displayFeature(types);
                    }
                }
            }
        }
    };

    public void receivedMsg(MsgBean msgBean){
        int msgId = msgBean.getMsgId();
        boolean isSameMsg = false;
        if (msgId != -1){
            for (int i=0;i<msgBeans.size();i++){
                if (msgBean.getMsgId() == msgBeans.get(i).getMsgId()){
                    isSameMsg = true;
                    msgBeans.get(i).setTime(msgBean.getTime());
                    msgBeans.get(i).setUsername(msgBeans.get(i).getUsername()+","+msgBean.getUsername());

                }
            }

        }else{
            for (int i=0;i<msgBeans.size();i++){
                if (-1 == msgBeans.get(i).getMsgId() && msgBean.getUsername().equals(msgBeans.get(i).getUsername())){
                    msgBeans.remove(i);
                    break;
                }
            }
        }
        if (!isSameMsg)
          msgBeans.add(msgBean);
        MsgListAdapter msgListAdapter = new MsgListAdapter(this,R.layout.msg_list_member,msgBeans);
        msgListView.post(() -> msgListView.setAdapter(msgListAdapter));
    }

    public void displayFeature(String types){

        if (types.contains("push")){
            pullMsgBtn.post(() -> pullMsgBtn.setVisibility(View.VISIBLE));
            pushMsgBtn.post(() -> pushMsgBtn.setVisibility(View.VISIBLE));
        }
        if (types.contains("pull")){
            pullMsgBtn.post(new Runnable() {
                @Override
                public void run() {
                    pullMsgBtn.setVisibility(View.VISIBLE);
                }
            });
        }
        if (types.contains("account")){
            pullMsgBtn.post(new Runnable() {
                @Override
                public void run() {
                    pullMsgBtn.setVisibility(View.VISIBLE);
                }
            });
            newAccountBtn.post(new Runnable() {
                @Override
                public void run() {
                    newAccountBtn.setVisibility(View.VISIBLE);
                }
            });
            removeAccountBtn.post(new Runnable() {
                @Override
                public void run() {
                    removeAccountBtn.setVisibility(View.VISIBLE);
                }
            });
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.setAction(ACTION2);
        intent.putExtra(feature_type_tag,onAppRunInBG);
        if (focusedUser!=null){
            intent.putExtra(focus_user,focusedUser);
        }else intent.putExtra(focus_user,"");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent();
        intent.setAction(ACTION2);
        intent.putExtra(feature_type_tag,onAppRunInFG);
        if (focusedUser!=null){
            intent.putExtra(focus_user,focusedUser);
        }else intent.putExtra(focus_user,"");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}