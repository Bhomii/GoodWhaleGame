package com.pawras.selfi.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.pawras.selfi.R;
import com.pawras.selfi.authuntication.SignUp;
import com.pawras.selfi.constants.Constant;
import com.pawras.selfi.utilities.VideoRecording;

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
import java.util.ArrayList;
import java.util.Date;
import android.media.ThumbnailUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_FILE_REQUEST = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String selectedFilePath;
    ImageView bimage, bvideo;
    ArrayList imageList;
    TextView bimage_gallery, bvideo_gallery;
    //ProgressDialog dialog;
    PowerManager.WakeLock wakeLock;
    String flag_value, thumbnail, email;
    private static final String EXTRA_FILENAME = "com.commonsware.android.camcon.EXTRA_FILENAME";
    private static final String FILENAME = "CameraContentDemo.jpeg";
    private static final int CONTENT_REQUEST = 1337;
    private File output = null;
    private Uri outputUri = null;
    public static final int MEDIA_TYPE_IMAGE = 1;
    Uri fileUri;
    int dialog_nbr;
    final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=1,PERMISSIONS_REQUEST_CAMERA=2,IMAGE_REQUEST=10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFrstAttemp();
        occupyFullScreen();
        setContentView(R.layout.home_screen);
        getRefereces();
    }

    // is this first attemp of user, please first register yourself
    public void isFrstAttemp() {
        SharedPreferences mSharedPreferences = getSharedPreferences("filter", MODE_PRIVATE);
        email = mSharedPreferences.getString("isRegistered", "no");
        if (email.equals("no")) {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
            finish();
        }
    }

    //set full screen
    public void occupyFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //get references
    public void getRefereces() {
        bimage = (ImageView) findViewById(R.id.camera);
        bvideo = (ImageView) findViewById(R.id.videos);
        bimage_gallery = (TextView) findViewById(R.id.img_gallery);
        bvideo_gallery = (TextView) findViewById(R.id.videos_gallery);
        bimage.setOnClickListener(this);
        bimage_gallery.setOnClickListener(this);
        bvideo_gallery.setOnClickListener(this);
        bvideo.setOnClickListener(this);
        imageList = new ArrayList<>();
    }

    // Create a file Uri for saving an image or video
    private Uri getOutputMediaFileUri(int type) {
        if (getOutputMediaFile(type)!=null){
            Uri path= getOutputMediaFile(type);
            return path;
        }
        else {
            dialog_nbr=6;
            storageProblem();
            return null;
        }
    }

		private Uri getOutputMediaFile(int type) {
			// To be safe, you should check that the SDCard is mounted
			// using Environment.getExternalStorageState() before doing this.
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_DOWNLOADS), "Selfie_APP");
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						Log.e("Selfie", "failed to create directory");
						return null;
					}
				}
				else {
					Log.e("Selfie", "directory exists");
				}
				// Create a media file name
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				File mediaFile;
				if (type == MEDIA_TYPE_IMAGE) {
					mediaFile = new File(mediaStorageDir.getPath() + File.separator +
							"IMG_" + timeStamp + ".jpg");
				} else {
					return null;
				}
				Uri path= Uri.fromFile(mediaFile);
				return path;
			}
			else {
				dialog_nbr=6;
				storageProblem();
				return null;
			}
		}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera:
                flag_value="1";
                getPermssion();
//                imageCapture();
                break;
            case R.id.videos:
                flag_value="2";
                videocapture();
//                getPermssion();
                break;
            case R.id.img_gallery:
                if (haveNetworkConnection()) {
                    Intent intent = new Intent(MainActivity.this, Gallery_images.class);
                    startActivity(intent);
                } else {
                    dialog_nbr=1;
                    openDialog();
                }
                break;
            case R.id.videos_gallery:
                if (haveNetworkConnection()) {
                    Intent intent2 = new Intent(MainActivity.this, Gallery_videos.class);
                    startActivity(intent2);
                } else {
                    dialog_nbr=1;
                    openDialog();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_FILENAME, output);
    }

    public void imageCapture() {
        flag_value = "1";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (fileUri!=null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, CONTENT_REQUEST);
        }
    }

    private void videocapture() {
//        flag_value = "2";
//        Intent intent=new Intent(MainActivity.this, VideoRecording.class);
//        intent.putExtra("email",email);
//        startActivity(intent);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        fileUri = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (fileUri!=null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
          startActivityForResult(intent, PICK_FILE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Toast.makeText(MainActivity.this, "requested permission", Toast.LENGTH_SHORT).show();
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (haveNetworkConnection()) {
                if (requestCode == PICK_FILE_REQUEST) {
                    if (data == null) {
                        return;
                    }
                    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
                    wakeLock.acquire();
                    Uri selectedFileUri = data.getData();
                    selectedFilePath = FilePath.getPath(this, selectedFileUri);
                    if (flag_value == "2") {
                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(selectedFilePath,
                                MediaStore.Images.Thumbnails.MICRO_KIND);
                        flag_value = getStringImage(thumb);
                    }
                    Log.i(TAG, "Selected File Path:" + selectedFilePath);
                    if (selectedFilePath != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    uploadFile(selectedFilePath);
                                } catch (OutOfMemoryError e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    // dialog.dismiss();
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(MainActivity.this, "Please choose a File First", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == CONTENT_REQUEST) {
                    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
                    wakeLock.acquire();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //creating new thread to handle Http Operations
                                uploadFile(fileUri.getPath());
                            } catch (OutOfMemoryError e) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog_nbr=2;
                                        openDialog();
                                    }
                                });
                            }
                        }
                    }).start();

                } else if (requestCode==10) {
                    Toast.makeText(MainActivity.this, "result called ", Toast.LENGTH_SHORT).show();
                   Bundle  mBundle=data.getExtras();
                    if (mBundle!=null){
                        String image_path=  mBundle.getString("image_path",null);
                        Toast.makeText(MainActivity.this, "image path is "+image_path, Toast.LENGTH_SHORT).show();
                        uploadFile(image_path);
                    }
                }
                  else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                dialog_nbr=1;
                openDialog();
            }
        }
    }

    public void getPermssion() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        }
        else {
            if (flag_value.equals("1")) {
//            flag_value = "1";
                Intent intent = new Intent(MainActivity.this, CustomCamera.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
            else if (flag_value.equals("2")){
                Intent intent=new Intent(MainActivity.this, VideoRecording.class);
                intent.putExtra("email",email);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    flag_value = "1";

                } else {
                    dialog_nbr=6;
                    openDialog();
                }

            }
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (flag_value.equals("1")){
                        Intent intent=new Intent(MainActivity.this, CustomCamera.class);
                        intent.putExtra("email",email);
                        startActivity(intent);
                    }
                    else if (flag_value.equals("2")){
                        Intent intent=new Intent(MainActivity.this, VideoRecording.class);
                        intent.putExtra("email",email);
                        startActivity(intent);
                    }




//                    flag_value = "1";
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    fileUri = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//                    if (fileUri != null) {
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//                        startActivityForResult(intent, CONTENT_REQUEST);
//                    }




                } else {
                    dialog_nbr = 6;
                    openDialog();
                }
                return;
            }
        }
    }

    public int uploadFile(final String selectedFilePath) {
        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);
        String[] parts = selectedFilePath.split("/");
        if (!selectedFile.isFile()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "suurce file does not exist", Toast.LENGTH_SHORT).show();
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
                connection.setRequestProperty("uploaded_file", selectedFilePath);
                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + selectedFilePath + "\"" + lineEnd);
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
                        dialog_nbr=2;
                        openDialog();
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
                    dialog_nbr=2;
                    openDialog();
                }
                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    // imageList.add(selectedFilePath);
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
                        dialog_nbr=4;
                        openDialog();
                    }
                });
                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "URL Error!", Toast.LENGTH_SHORT).show();
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

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void openDialog() {
        View dialogView=null;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        if (dialog_nbr==1)
            dialogView = inflater.inflate(R.layout.dialog_connection, null);
        else if (dialog_nbr==2){
            dialogView = inflater.inflate(R.layout.dialog_insufficeint_memory, null);
        }
        else if (dialog_nbr==3){
            dialogView = inflater.inflate(R.layout.dialog_server_error, null);
        }
        else if (dialog_nbr==4){
            dialogView = inflater.inflate(R.layout.dialog_file_uploaded, null);
        }
        else if (dialog_nbr==5){
            dialogView = inflater.inflate(R.layout.dialog_exit, null);
        }
        else if (dialog_nbr==6){
            dialogView = inflater.inflate(R.layout.dilog_permission_denied, null);
        }
        dialogBuilder.setView(dialogView);
        final AlertDialog findMeDialog = dialogBuilder.create();
        findMeDialog.show();
        LinearLayout reset_btn = (LinearLayout) dialogView.findViewById(R.id.ok);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
            }
        });
    }

    public void storageProblem(){
        View dialogView=null;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_external_storage_error, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog findMeDialog = dialogBuilder.create();
        findMeDialog.show();
        LinearLayout reset_btn = (LinearLayout) dialogView.findViewById(R.id.ok);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
                System.exit(0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        dialog_nbr=3;
        View dialogView=null;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_exit, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog findMeDialog = dialogBuilder.create();
        findMeDialog.show();
        TextView cancel_btn = (TextView) dialogView.findViewById(R.id.cancel);
        TextView exit_btn = (TextView) dialogView.findViewById(R.id.exit);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
            }
        });

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
                finish();
            }
        });
    }
}