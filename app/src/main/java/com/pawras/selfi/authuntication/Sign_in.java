package com.pawras.selfi.authuntication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pawras.selfi.activities.MainActivity;
import com.pawras.selfi.R;
import com.pawras.selfi.constants.Constant;

import java.util.Hashtable;
import java.util.Map;

public class Sign_in extends AppCompatActivity {

    //    Typeface nexalight,nexabold;
    TextView forgot_pass;
    EditText mUsername, mPassword;
    String email, password, EMAIL = "email", PASSWORD = "password";
    final String CODE = "code";
    String pin_code;
    int randomPIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sign_in);
        getReferences();
    }

    //getting & setting
    public void getReferences() {
        mUsername = (EditText) findViewById(R.id.login_email);
        mPassword = (EditText) findViewById(R.id.login_password);
        forgot_pass = (TextView) findViewById(R.id.forgot_pass);
    }

    //login
    public void isDataCorrect(View view) {
        if (haveNetworkConnection()) {
            email = mUsername.getText().toString();
            password = mPassword.getText().toString();
            if (email.isEmpty()) {
                mUsername.requestFocus();
                mUsername.setHint("Enter Email");
                mUsername.setHintTextColor(getResources().getColor(R.color.red));
            } else if (password.isEmpty()) {
                mPassword.requestFocus();
                mPassword.setHint("Enter Password ");
                mPassword.setHintTextColor(getResources().getColor(R.color.red));
            } else if (password.length() < 3) {
                Toast.makeText(Sign_in.this, "Password too much Small", Toast.LENGTH_SHORT).show();
            } else {
                networkCall();
            }
        } else {
            openNetworkConnectionDialog();
        }
    }

    //User Authentication
    public void networkCall() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Logging", "Please wait..", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response.equals("notexists")) {
                            Toast.makeText(Sign_in.this, "Incorrect Email/Password", Toast.LENGTH_SHORT).show();
                        } else if (response.equals("exist")) {
                            Toast.makeText(Sign_in.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            SharedPreferences sPref = getSharedPreferences("filter", MODE_PRIVATE);
                            SharedPreferences.Editor mEditor = sPref.edit();
                            mEditor.putString("isRegistered", email);
                            mEditor.commit();
                            Intent intent = new Intent(Sign_in.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(Sign_in.this, "Server Error", Toast.LENGTH_SHORT).show();
                        // Toast.makeText(Sign_in.this,"server error"+volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();reg
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                //Adding parameters
                params.put(EMAIL, email);
                params.put(PASSWORD, password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //call sign up screen
    public void swapToSignup(View v) {
        Intent intent = new Intent(Sign_in.this, SignUp.class);
        startActivity(intent);
        finish();
    }

    //reset password
    public void resetPassword(View v) {
        if (haveNetworkConnection()) {
            email = mUsername.getText().toString();
            if (email.isEmpty()) {
                mUsername.requestFocus();
                mUsername.setHint("Enter Email");
                mUsername.setHintTextColor(getResources().getColor(R.color.red));
            } else if (!email.contains("@")) {
                mUsername.requestFocus();
                mUsername.setText("");
                mUsername.setHint("Invalid Email");
                mUsername.setTextColor(getResources().getColor(R.color.red));
            } else if (email.length() < 4) {
                mUsername.requestFocus();
                mUsername.setText("");
                mUsername.setHint("Invalid Email");
                mUsername.setHintTextColor(getResources().getColor(R.color.red));
            } else {
                sendEmail();
            }
        }
        else{
            openNetworkConnectionDialog();
        }
    }

    public void openDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_connection_loss, null);
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

    //forgot password
    final public void sendEmail() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Logging", "Please wait..", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SEND_EMAIL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("sent")) {
                            openDialog();
                            loading.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(Sign_in.this, "Server Error", Toast.LENGTH_SHORT).show();
                        // Toast.makeText(Sign_in.this,"server error"+volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();reg
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //generating 4 digit number
                Map<String, String> params = new Hashtable<String, String>();
                //Adding parameters
                params.put(EMAIL, email);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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

    public void openNetworkConnectionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_connection, null);
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
}