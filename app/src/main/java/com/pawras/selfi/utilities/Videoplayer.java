package com.pawras.selfi.utilities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.pawras.selfi.R;

public class Videoplayer extends AppCompatActivity {
    ProgressDialog progressDialog;
    VideoView videoView;
    String videopath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplayer);
        videoView=(VideoView)findViewById(R.id.videoView);
        Intent i=getIntent();
        videopath=i.getStringExtra("current_url");


        progressDialog = ProgressDialog.show(Videoplayer.this, "", "Buffering video...", true);
        progressDialog.setCancelable(true);
        PlayVideo();

    }
    private void PlayVideo()
    {
        try
        {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(Videoplayer.this);
            mediaController.setAnchorView(videoView);

            Uri video = Uri.parse(videopath);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {

                public void onPrepared(MediaPlayer mp)
                {
                    progressDialog.dismiss();
                    videoView.start();
                }
            });


        }
        catch(Exception e)
        {
            progressDialog.dismiss();
            System.out.println("Video Play Error :"+e.toString());
            finish();
        }

//        Intent i=getIntent();
//        String str=i.getStringExtra("current_url");
//        String sss="http://tornadofoods.com/extras/video/afzal.mp4";
//        MediaController mediaController = new MediaController(this);
//        //mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);
//        videoView.setVideoURI(Uri.parse(str));
//        videoView.requestFocus();
//
//        loading = ProgressDialog.show(this, "Please wait ...", "Buffering video ...", true);
//
//
//            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//
//                public void onPrepared(MediaPlayer mp) {
//                    // TODO Auto-generated method stub
//                    loading.dismiss();
//                    videoView.start();
//                }
//            });


    }
}
