   package com.pawras.selfi.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.pawras.selfi.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

   /**
    * Created by Saif on 8/18/2016.
    */
   public class VideoListAdapter extends BaseAdapter {

       Context mContext;
       LayoutInflater mLayoutInflater;
       ArrayList<String> userList;

       public VideoListAdapter(Context context, ArrayList<String> userList) {
           mContext=context;
           this.userList = userList;
           mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       }

       @Override
       public int getCount() {
           return userList.size();
       }

       @Override
       public Object getItem(int position) {
           return userList.get(position);
       }

       @Override
       public long getItemId(int position) {
           return position;
       }

       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
           if (convertView == null){
               convertView = mLayoutInflater.inflate(R.layout.video_list_iteam,parent,false);
           }

           ImageView photo = (ImageView) convertView.findViewById(R.id.img1);
           String current_url=userList.get(position);

          Picasso.with(mContext).load(current_url).resize(100,100).centerCrop().into(photo);


           return convertView;
       }
   }
