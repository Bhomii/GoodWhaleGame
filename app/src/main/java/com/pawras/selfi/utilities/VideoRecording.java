package com.pawras.selfi.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.pawras.selfi.R;
import com.pawras.selfi.constants.Constant;

public class VideoRecording extends Activity {

    private Camera myCamera;
    private CameraPreview myCameraSurfaceView;
    private MediaRecorder mediaRecorder;
    ImageView myButton;
    boolean recording = false;
    TextView video_timer;
    boolean isStop = false;
    String email, flag_value;
    CountDownTimer timer;
    Dialog dialogView;
    String video_path = null;
    int dialog_nbr,flag=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen();
        getIntentExtras();
        getAndCheckCamera();
        initilization();
        getCameraInstace();
    }

    //occupy full window's space
    public void fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_recording);
    }

    //intialization
    public void initilization() {
        myButton = (ImageView) findViewById(R.id.mybutton);
        myButton.setOnClickListener(myButtonOnClickListener);
        video_timer = (TextView) findViewById(R.id.video_timer);
    }

    public void getAndCheckCamera() {
        myCamera = getCameraInstance();
        if (myCamera == null) {
            Toast.makeText(VideoRecording.this,
                    "Fail to get Camera",
                    Toast.LENGTH_LONG).show();
        }
    }

    //get camera instance
    public void getCameraInstace() {
        if (checkCameraHardware(this)) {
            // Create our Preview view and set it as the content of our activity.
            myCameraSurfaceView = new CameraPreview(this, myCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.videoview);
            preview.addView(myCameraSurfaceView);
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    //get Intent
    public void getIntentExtras() {
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            email = mBundle.getString("email", null);
        }
    }

    Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (recording) {
                // stop recording and release camera
                timer.cancel();
                mediaRecorder.stop();  // stop the recording
                myCamera.stopPreview();
                releaseMediaRecorder(); // release the MediaRecorder object
                //Exit after saved
                isStop = true;
                //after stopping the video, video preview will stay for 1 second then a dialog will open
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openTimer();
                    }
                }, 2000);
            } else {
                flag++;
                if (flag==2) {
                    if (!prepareMediaRecorder()) {
                        Toast.makeText(VideoRecording.this, "vieo is not prepared", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(VideoRecording.this,
//                            "Fail in prepareMediaRecorder()!\n - Ended -",
//                            Toast.LENGTH_LONG).show();
//                    finish();
                    }
                    startTimer();
                }
            }
        }
    };

    private Camera getCameraInstance() {
        // TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareMediaRecorder() {
        // myCamera = getCameraInstance();
        mediaRecorder = new MediaRecorder();
        myCamera.unlock();
        mediaRecorder.setCamera(myCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if (CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_HIGH)) {
            mediaRecorder.setProfile(CamcorderProfile.get(1, CamcorderProfile.QUALITY_HIGH));
        } else {
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        }
        
        mediaRecorder.setOutputFile(getOutputMediaFile());
        mediaRecorder.setMaxDuration(10000); // Set max duration 10 sec.
        mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            myCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (myCamera != null) {
            myCamera.release();        // release the camera for other applications
            myCamera = null;
        }
    }

    public void openTimer() {
        dialogView = new Dialog(this);
        dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogView.setContentView(R.layout.dialog_timer);
        final TextView mTextField = (TextView) dialogView.findViewById(R.id.timer);
        final CountDownTimer mCountDownTimer = new CountDownTimer(7000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextField.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            upload(video_path);
                        } catch (OutOfMemoryError e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VideoRecording.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            // dialog.dismiss();
                        }
                    }
                }).start();
                Toast.makeText(VideoRecording.this, "Uploading Video..", Toast.LENGTH_SHORT).show();
                dialogView.dismiss();
                finish();
            }
        }.start();

        final TextView upload = (TextView) dialogView.findViewById(R.id.upload);
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCountDownTimer.cancel();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            upload(video_path);
                        } catch (OutOfMemoryError e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VideoRecording.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            // dialog.dismiss();
                        }
                    }
                }).start();
                Toast.makeText(VideoRecording.this, "Uploading..", Toast.LENGTH_SHORT).show();
                dialogView.dismiss();
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.dismiss();
                finish();
            }
        });
        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogView.show();
    }


    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private String getOutputMediaFile() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Selfies");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".mp4");
        Uri path = Uri.fromFile(mediaFile);
        video_path = path.getPath();
        Log.d("path", video_path);
        return video_path;
    }

    public int upload(String path) {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(video_path,
                MediaStore.Images.Thumbnails.MICRO_KIND);
        flag_value = getStringImage(thumb);
        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(path);
        String[] parts = path.split("/");
        if (!selectedFile.isFile()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoRecording.this, "suurce file does not exist", Toast.LENGTH_SHORT).show();
                }
            });
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(Constant.SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", path);
                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + path + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];
                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    try {
                        dataOutputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        dialog_nbr = 2;
                        Toast.makeText(VideoRecording.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"email\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(email);
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"flag\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(flag_value);
                dataOutputStream.writeBytes(lineEnd);
                // get server ok responce
                try {
                    serverResponseCode = connection.getResponseCode();
                } catch (OutOfMemoryError e) {
                    dialog_nbr = 2;
                    Toast.makeText(VideoRecording.this, "Error", Toast.LENGTH_SHORT).show();
                }
                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    // imageList.add(path);
                }
                //reading server echo responce
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog_nbr = 4;
                        Toast.makeText(VideoRecording.this, "Finish", Toast.LENGTH_SHORT).show();
                    }
                });
                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VideoRecording.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VideoRecording.this, "URL Error!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
            return serverResponseCode;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        myCamera.release();
        finish();
    }

    public void startTimer() {
        openSound1();
        final TextView mTextField = (TextView) findViewById(R.id.timer);
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                mTextField.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                flag=1;
                mTextField.setVisibility(View.GONE);
                mediaRecorder.start();
                recording = true;
                timer = new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        video_timer.setText("" + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        if (!isStop) {
                            isStop = true;
                            mediaRecorder.stop();  // stop the recording
                            releaseMediaRecorder();
                            myCamera.stopPreview();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    openTimer();
                                }
                            }, 1000);
                        }
                    }
                }.start();
            }
        }.start();
    }

    public void openSound1() {
        MediaPlayer mp = MediaPlayer.create(VideoRecording.this, R.raw.btn_click);
        mp.start();
    }
}