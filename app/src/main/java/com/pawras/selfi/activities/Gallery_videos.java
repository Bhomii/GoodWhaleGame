package com.pawras.selfi.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.pawras.selfi.R;
import com.pawras.selfi.constants.Constant;
import com.pawras.selfi.listners.DataCallListener;
import com.pawras.selfi.networks.Network;
import com.pawras.selfi.utilities.VideoListAdapter;
import com.pawras.selfi.utilities.Videoplayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Gallery_videos extends AppCompatActivity implements DataCallListener {

    Network mNetwork;
    JSONArray arr;
    public static ProgressDialog loading;
    String email;
    Map<String, String> params;
    ArrayList<String> video_url=new ArrayList();
    ArrayList<String> images = new ArrayList();
    GridView gridview;
    AlertDialog findMeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disappearNotificationBar();
        setContentView(R.layout.activity_gallery_videos);
        playVideo();
        networkCall();
    }

    //disappearNotificationBar
    public void disappearNotificationBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //play video
    public void playVideo(){
        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(Gallery_videos.this, "" + position, Toast.LENGTH_SHORT).show();
                String current_url=video_url.get(position);
                Intent i=new Intent(Gallery_videos.this, Videoplayer.class);
                i.putExtra("current_url",current_url);
                startActivity(i);
            }
        });
    }

    //netword call
    public void networkCall(){
        mNetwork = new Network(this, this);
        loading = ProgressDialog.show(this, "Loading Videos", "Please wait..", false, false);
        //Adding parameters
        params = new HashMap<>();
        SharedPreferences mSharedPreferences=getSharedPreferences("filter",MODE_PRIVATE);
        email= mSharedPreferences.getString("isRegistered","no");
        params.put("email", email);
        mNetwork.getUserData(Constant.VIDEO_URL, params);
    }

    @Override
    public void onRequestCompleted(int REQUEST_TYPE, String response) {
        try {
            arr = new JSONArray(response);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String thumb= obj.optString("file_type");
                String video = obj.optString("file_path");
                images.add(thumb);
                video_url.add(video);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loading.dismiss();
        gridview.setAdapter(new VideoListAdapter(this,images));
    }

    @Override
    public void onRequestError(VolleyError volleyError) {
        appearNetworkErrorDialog();
    }

    //appear network error dialog
    public void appearNetworkErrorDialog(){
        View dialogView=null;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_network_problem, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog findMeDialog = dialogBuilder.create();
        findMeDialog.show();
        TextView cancel_btn = (TextView) dialogView.findViewById(R.id.cancel);
        TextView try_again = (TextView) dialogView.findViewById(R.id.try_again);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
                finish();
            }
        });

        try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
                networkCall();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}