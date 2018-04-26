package com.pawras.selfi.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.pawras.selfi.R;
import com.pawras.selfi.constants.Constant;
import com.pawras.selfi.listners.DataCallListener;
import com.pawras.selfi.networks.Network;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gallery_images extends FragmentActivity implements DataCallListener {
    Network mNetwork;
    JSONArray arr;
    public static ProgressDialog loading;
    String email;
    Map<String, String> params;
    ArrayList<String> images = new ArrayList<>(Arrays.asList(
            "http://qutehay.com/OldOnes/volleyuploads/upload/2.jpg"
    ));
    private ScrollGalleryView scrollGalleryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disappearNotificationBar();
        setContentView(R.layout.activity_gallery_images);
        networkCall();
    }

    //disappearNotificationBar
    public void disappearNotificationBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //network call
    public void networkCall(){
        mNetwork = new Network(this, this);
        loading = ProgressDialog.show(this, "Loading images..", "Please wait", false, false);
        //Adding parameters
        params = new HashMap<>();
        SharedPreferences mSharedPreferences = getSharedPreferences("filter", MODE_PRIVATE);
        email = mSharedPreferences.getString("isRegistered", "no");
        params.put("type", "image");
        params.put("email", email);
        // call for getting data from server (network call)
        mNetwork.getUserData(Constant.IMAGE_URL, params);
    }

    private Bitmap toBitmap(int image) {
        return ((BitmapDrawable) getResources().getDrawable(image)).getBitmap();
    }

    @Override
    public void onRequestCompleted(int REQUEST_TYPE, String response) {
        try {
            arr = new JSONArray(response);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                // String name = obj.optString("company_name");
                String photo = obj.optString("file_path");
                images.add(photo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loading.dismiss();
        setgallery();
        //Toast.makeText(Gallery_images.this, "path is=" + response, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestError(VolleyError volleyError) {
        appearNetworkErrorDialog();
    }

    public void setgallery() {
        List<MediaInfo> infos = new ArrayList<>(images.size());
        for (String url : images) infos.add(MediaInfo.mediaLoader(new PicassoImageLoader(url)));
        scrollGalleryView = (ScrollGalleryView) findViewById(R.id.scroll_gallery_view);
        scrollGalleryView
                .setThumbnailSize(100)
                .setZoom(true)
                .setFragmentManager(getSupportFragmentManager())
                .addMedia(infos);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        loading.dismiss();
        finish();
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
}

