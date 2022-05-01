package com.soulter.esndroid.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.soulter.esndroid.bean.MsgBean;
import com.soulter.esndroid.R;

import java.util.List;

/*
Author : Soulter
2021© Copyright reserved
 */


public class MsgListAdapter extends ArrayAdapter<MsgBean> {
    private int resourceId;
    private List<MsgBean> msgBeans;
    public MsgListAdapter(Context context, int itemResId, List<MsgBean> msgBeans){
        super(context,itemResId,msgBeans);
        resourceId = itemResId;
        this.msgBeans = msgBeans;
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
        viewHolder.title=view.findViewById(R.id.msg_title_mem);
        viewHolder.title.setTypeface(viewHolder.title.getTypeface(), Typeface.BOLD);
        viewHolder.user=view.findViewById(R.id.msg_user_mem);
//        viewHolder.user.setTypeface(viewHolder.user.getTypeface(), Typeface.BOLD);
        viewHolder.content=view.findViewById(R.id.msg_content_mem);
//        viewHolder.content.setTypeface(viewHolder.content.getTypeface(), Typeface.BOLD);
        viewHolder.time = view.findViewById(R.id.msg_time_mem);



        // 将ViewHolder存储在View中（即将控件的实例存储在其中）
        view.setTag(viewHolder);
        view.setTag(R.string.id_bean_tag,msgBeans.get(position));


        // 获取控件实例，并调用set...方法使其显示出来
        int standardIndex = msgBeans.size()-position-1;
        if (msgBeans.get(standardIndex).getMsgId()!=-1){
            viewHolder.user.setText("发:"+msgBeans.get(standardIndex).getFromUser()+"\n收:"+msgBeans.get(standardIndex).getUsername());
        }
        viewHolder.title.setText(msgBeans.get(standardIndex).getTitle());
        viewHolder.content.setText(msgBeans.get(standardIndex).getContent());
        String timeStr = msgBeans.get(standardIndex).getTime();
        try{
            timeStr = timeStr.substring(5,timeStr.length()-3).replace("-","月").replace(",","日 ");
        }catch (Exception e){
        }
        viewHolder.time.setText(timeStr);
        return view;
    }

    class ViewHolder{
        TextView title;
        TextView user;
        TextView content;
        TextView time;
    }
}
