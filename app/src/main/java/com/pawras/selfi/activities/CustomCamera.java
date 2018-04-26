package com.pawras.selfi.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.pawras.selfi.R;
import com.pawras.selfi.constants.Constant;
import com.pawras.selfi.utilities.CameraPreview;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomCamera extends AppCompatActivity implements Camera.PictureCallback {

    Camera mCamera;
    CameraPreview mPreview;
    File capturedImage;
    Uri path = null;
    boolean isCancel = false, isUpload = false;
    Dialog dialogView;
    String email, flag_value;
    int dialog_nbr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen();
        getCameraInstace();
        getIntentExtras();
        setListner();
    }

    //occupy full window's screen
    public void fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.custom_camera);
    }

    //get camera instance
    public void getCameraInstace() {
        if (checkCameraHardware(this)) {
            mCamera = getCameraInstance();
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }

    //set listner
    public void setListner() {
        final ImageView captureButton = (ImageView) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openSound1();
                        startTimer();
                    }
                }
        );
    }

    //get Intent
    public void getIntentExtras() {
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            email = mBundle.getString("email", null);
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

    //get camera hardware
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(1);
        } catch (Exception e) {

        }
        return c;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                capturedImage = getOutputMediaFile();
                FileOutputStream fos = new FileOutputStream(capturedImage);
                fos.write(data);
                fos.close();
                path = Uri.fromFile(capturedImage);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openTimer();
                    }
                }, 2000);
            } catch (FileNotFoundException e) {
                Log.d("file not found", "File not found: " + e.getMessage());
            } catch (IOException e) {
            }
        }
    };

    //make picture and save to a folder
    private static File getOutputMediaFile() {
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
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        Uri path = Uri.fromFile(mediaFile);
        return mediaFile;
    }

    public int upload(String path) {
        flag_value = "1";
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
                    Toast.makeText(CustomCamera.this, "suurce file does not exist", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CustomCamera.this, "Error", Toast.LENGTH_SHORT).show();
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
                //get server ok responce

                try {
                    serverResponseCode = connection.getResponseCode();
                } catch (OutOfMemoryError e) {
                    dialog_nbr = 2;
                    Toast.makeText(CustomCamera.this, "Error", Toast.LENGTH_SHORT).show();
                }
                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    // imageList.add(path);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
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
                        Toast.makeText(CustomCamera.this, "Finish", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CustomCamera.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CustomCamera.this, "URL Error!", Toast.LENGTH_SHORT).show();
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
                            upload(path.getPath());
                        } catch (OutOfMemoryError e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CustomCamera.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            // dialog.dismiss();
                        }
                    }
                }).start();
                Toast.makeText(CustomCamera.this, "Uploading Video..", Toast.LENGTH_SHORT).show();
                dialogView.dismiss();
                finish();
            }
        }.start();

        TextView upload = (TextView) dialogView.findViewById(R.id.upload);
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        upload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCountDownTimer.cancel();
                dialogView.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            isUpload = true;
                            upload(path.getPath());
                        } catch (OutOfMemoryError e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CustomCamera.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            // dialog.dismiss();
                        }
                    }
                }).start();
                Toast.makeText(CustomCamera.this, "Uploading..", Toast.LENGTH_SHORT).show();
                dialogView.dismiss();
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCancel = true;
                dialogView.dismiss();
                finish();
            }
        });
        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogView.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialogView != null) {
            dialogView.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCamera.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mCamera.release();
        finish();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }

    public void openSound() {
        MediaPlayer mp = MediaPlayer.create(CustomCamera.this, R.raw.selfie);
        mp.start();
    }

    public void openSound1() {
        MediaPlayer mp = MediaPlayer.create(CustomCamera.this, R.raw.btn_click);
        mp.start();
    }

    public void startTimer() {
        final TextView mTextField = (TextView) findViewById(R.id.timer);
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                mTextField.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mTextField.setVisibility(View.GONE);
                openSound();
                mCamera.takePicture(null, null, mPicture);
            }
        }.start();
    }
}