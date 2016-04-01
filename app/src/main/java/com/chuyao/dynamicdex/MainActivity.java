package com.chuyao.dynamicdex;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import dalvik.system.DexClassLoader;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private static final String REMOTE_JAR_PATH = "http://192.168.1.211/AndroidDynamic/class.jar";


    private Button btPlus;
    private Button btDownload;
    private EditText etLog;
    private StringBuilder stringBuilder = new StringBuilder();

    private Handler logHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                String message = (String) msg.obj;
                stringBuilder.append(message).append("\n");
                etLog.setText(stringBuilder);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btPlus = (Button) findViewById(R.id.btPlus);
        btDownload = (Button) findViewById(R.id.btDownload);
        etLog = (EditText) findViewById(R.id.tvLog);
        btDownload.setOnClickListener(this);
        btPlus.setOnClickListener(this);
    }

    private void download() {
        new Thread(new DownloadTask()).start();
    }

    class DownloadTask implements Runnable {

        public DownloadTask(){

        }

        @Override
        public void run() {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                URL mUrl = new URL(REMOTE_JAR_PATH);
                HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
                mConnection.setConnectTimeout(5000);
                mConnection.setReadTimeout(5000);
                mConnection.connect();
                if (200 == mConnection.getResponseCode()) {
                    inputStream = mConnection.getInputStream();
                    outputStream = new FileOutputStream(new File(MainActivity.this.getFilesDir(), "plus.jar"));
                    int len = 0;
                    byte[] buff = new byte[4096];
                    while ((len = inputStream.read(buff)) != -1) {
                        outputStream.write(buff, 0, len);
                    }
                    outputStream.flush();
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = "download complete";
                    logHandler.sendMessage(msg);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isPlusJarReady(){
        File file = new File(getFilesDir(), "plus.jar");
        return file.exists();
    }

    private void loadPlus(){
        DexClassLoader dexClassLoader = new DexClassLoader(new File(getFilesDir(), "plus.jar").getPath(), "/data/data/com.chuyao.dynamicdex", null, this.getClass().getClassLoader());
        try {
            Class<?> clazz = dexClassLoader.loadClass("com.imfclub.remote.Plus");
            Object obj = clazz.newInstance();
            Method method = clazz.getMethod("getPlusString", new Class<?>[]{});
            String plusString = (String) method.invoke(obj, new Object[]{});
            Log.d(TAG, plusString);
            stringBuilder.append(plusString).append("\n");
            etLog.setText(stringBuilder);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            stringBuilder.append("ClassNotFoundException").append("\n");
            etLog.setText(stringBuilder);
        } catch (InstantiationException e) {
            e.printStackTrace();
            stringBuilder.append("InstantiationException").append("\n");
            etLog.setText(stringBuilder);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            stringBuilder.append("IllegalAccessException").append("\n");
            etLog.setText(stringBuilder);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            stringBuilder.append("NoSuchMethodException").append("\n");
            etLog.setText(stringBuilder);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            stringBuilder.append("InvocationTargetException").append("\n");
            etLog.setText(stringBuilder);
        }
    }

    private void clearPlusJarFile(){
        File file = new File(getFilesDir(), "plus.jar");
        if(file.exists()){
            file.delete();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btDownload){
            if(!isPlusJarReady()){
                download();
            }
        }else if(id == R.id.btPlus){
            loadPlus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearPlusJarFile();
    }
}