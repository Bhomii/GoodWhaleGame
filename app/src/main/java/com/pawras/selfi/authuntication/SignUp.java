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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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

public class SignUp extends AppCompatActivity {

    EditText mEmail, mPassword, mConfirmPass, mcell_no;
    private String KEY_EMAIL = "email";
    private String KEY_PASSWORD = "password";
    Button signUp_btn;
    String email, password, confirm_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sign_up);
        getReferences();
    }

    //Get References
    public void getReferences() {
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mConfirmPass = (EditText) findViewById(R.id.confirm_password);
        mcell_no = (EditText) findViewById(R.id.email);
        signUp_btn = (Button) findViewById(R.id.sign_up);
    }

    //Check user iput data
    public void checkUserData(View view) {
        if (haveNetworkConnection()) {
            password = mPassword.getText().toString();
            confirm_pass = mConfirmPass.getText().toString();
            email = mEmail.getText().toString();
            if (email.isEmpty()) {
                mEmail.requestFocus();
                mEmail.setHint("Enter Email");
                mEmail.setHintTextColor(getResources().getColor(R.color.red));
            } else if (!email.contains("@")) {
                mEmail.requestFocus();
                mEmail.setText("");
                mEmail.setHint("Invalid Email");
                mEmail.setTextColor(getResources().getColor(R.color.red));
            } else if (email.length() < 4) {
                mEmail.requestFocus();
                mEmail.setText("");
                mEmail.setHint("Invalid Email");
                mEmail.setHintTextColor(getResources().getColor(R.color.red));
            } else if (!password.equals(confirm_pass)) {
                mPassword.requestFocus();
                Toast.makeText(SignUp.this, "Password Mismatch", Toast.LENGTH_SHORT).show();
            } else {
                networkCall();
            }
        } else {
            openDialog();
        }
    }

    private void networkCall() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Sign Up...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SIGNUP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response.equals("inserted")) {
                            Log.d("response", response);
                            Toast.makeText(SignUp.this, "Sign Up successfully", Toast.LENGTH_LONG).show();
                            SharedPreferences sPref = getSharedPreferences("filter", MODE_PRIVATE);
                            SharedPreferences.Editor mEditor = sPref.edit();
                            mEditor.putString("isRegistered", email);
                            mEditor.commit();
                            Intent i = new Intent(SignUp.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else
                            Toast.makeText(SignUp.this, "User Already Exist", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(SignUp.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                //Adding parameters
                params.put(KEY_EMAIL, email);
                params.put(KEY_PASSWORD, password);
                return params;
            }
        };
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    //swap to login screen
    public void swapTOLogin(View v) {
        Intent intent = new Intent(SignUp.this, Sign_in.class);
        startActivity(intent);
        finish();
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
