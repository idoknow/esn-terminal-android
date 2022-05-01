package com.soulter.esndroid.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soulter.esndroid.service.ConnService;
import com.soulter.esndroid.bean.IDBean;
import com.soulter.esndroid.R;
import com.soulter.esndroid.adapter.IDListAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/*
Author : Soulter
2021© Copyright reserved
 */

public class AddIDActivity extends AppCompatActivity {
    CardView addServerBtn;
    CardView addIDBtn;
    ListView IDListView;
    TextView addServerEntryTv;
    boolean isOperatedData = false;

    public static final int RESULT_CODE_SELECT_ID = 1;
    public static final int REQUEST_CODE_ADDED_ID = 2;
    public static final int REQUEST_CODE_ADDED_SERVER_ID = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_i_d);


        addServerEntryTv = findViewById(R.id.add_server_entry_tv);
        final SharedPreferences spfs = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE);
        if (spfs.getBoolean("is_server_added",false)){
            addServerEntryTv.setText("服务器:"+spfs.getString("server_url","null"));
        }
        IDListView = findViewById(R.id.id_list_view);
        List<IDBean> idBeans = getStoredID(spfs);
        IDListAdapter idListAdapter = new IDListAdapter(AddIDActivity.this,R.layout.id_list_member,idBeans);
        IDListView.setAdapter(idListAdapter);
        IDListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                IDBean idBean = (IDBean)view.getTag(R.string.id_bean_tag);
                Intent intent = new Intent(AddIDActivity.this,MainActivity.class);
                intent.putExtra("ID_user",idBean.getUserName());
                intent.putExtra("ID_pass",idBean.getPass());
                intent.putExtra("ID_types",idBean.getTypes().toString());
                setResult(RESULT_CODE_SELECT_ID,intent);
                finish();

            }
        });
        addIDBtn = findViewById(R.id.add_id_btn);
        addServerBtn = findViewById(R.id.add_server_btn);
        addServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddIDActivity.this,ServerLoginActivity.class);
                startActivityForResult(intent,REQUEST_CODE_ADDED_SERVER_ID);
            }
        });
        addIDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddIDActivity.this,IDLoginActivity.class);
                startActivityForResult(intent,REQUEST_CODE_ADDED_ID);
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isOperatedData){
            Toast.makeText(this,"检测到数据有更改，因此即将重启服务...",Toast.LENGTH_LONG).show();
            ConnService.isRestartService = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADDED_ID && resultCode == IDLoginActivity.RESULT_CODE_ADDED_ID){
            final SharedPreferences spfs = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE);
            IDListAdapter adapter = new IDListAdapter(AddIDActivity.this,R.layout.id_list_member,getStoredID(spfs));
            IDListView.setAdapter(adapter);
            isOperatedData = true;
        }
        if (requestCode == REQUEST_CODE_ADDED_SERVER_ID && resultCode == ServerLoginActivity.RESULT_CODE_ADDED_SERVER_ID){
            final SharedPreferences spfs = getApplicationContext().getSharedPreferences("spfs", Context.MODE_PRIVATE);
            if (spfs.getBoolean("is_server_added",false)){
                addServerEntryTv.setText("服务器:"+spfs.getString("server_url","null"));
                isOperatedData = true;
            }
        }
    }

    public List<IDBean> getStoredID(SharedPreferences sp){
        Gson gson = new Gson();
        List<IDBean> arrayList;
        String json = sp.getString("id_beans","");
        Log.v("lwl","stored_idbeans:"+json);
        if (!json.equals("")){
            Type type = new TypeToken<List<IDBean>>() {
            }.getType();
            arrayList = gson.fromJson(json, type);
        }else {
            arrayList = new ArrayList<>();
        }
        return arrayList;
    }
}