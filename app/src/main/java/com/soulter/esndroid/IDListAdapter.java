package com.soulter.esndroid;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;

/*
Author : Soulter
2021© Copyright reserved
 */


public class IDListAdapter extends ArrayAdapter<IDBean> {
    private List<IDBean> idBeanList = new ArrayList<>();
    private int resourceId;
    public IDListAdapter(Context context,int itemResId,List<IDBean> idBeanList){
        super(context,itemResId,idBeanList);
        this.idBeanList = idBeanList;
        resourceId = itemResId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
        view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

        // 避免每次调用getView()时都要重新获取控件实例
        viewHolder=new ViewHolder();
        viewHolder.userTv=view.findViewById(R.id.add_id_username);
        viewHolder.userTv.setTypeface(viewHolder.userTv.getTypeface(), Typeface.BOLD);
        viewHolder.typeTv=view.findViewById(R.id.add_id_type);
        viewHolder.typeTv.setTypeface(viewHolder.typeTv.getTypeface(), Typeface.BOLD);


        // 将ViewHolder存储在View中（即将控件的实例存储在其中）
        view.setTag(viewHolder);
        view.setTag(R.string.id_bean_tag,idBeanList.get(position));


        // 获取控件实例，并调用set...方法使其显示出来
        viewHolder.userTv.setText(idBeanList.get(position).getUserName());
        viewHolder.typeTv.setText(idBeanList.get(position).getTypes().toString());
        return view;
    }

    class ViewHolder{
        TextView userTv;
        TextView typeTv;
    }
}
