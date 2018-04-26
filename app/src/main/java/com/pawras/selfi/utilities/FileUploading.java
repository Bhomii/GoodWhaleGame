package com.pawras.selfi.utilities;

import android.content.Context;
import android.widget.Toast;
import com.pawras.selfi.constants.Constant;
import java.io.BufferedReader;
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

/**
 * Created by Bhoomi on 1/21/2017.
 */
public class FileUploading {

    Context mcContext;

    FileUploading(Context mcContext){
        this.mcContext=mcContext;
    }

}
