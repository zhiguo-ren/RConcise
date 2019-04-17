package com.egbert.rconcisecase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.egbert.rconcise.RClient;
import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.download.RDownloadManager;
import com.egbert.rconcise.interceptor.HttpLoggingInterceptor;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.listener.IRespListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int WRITE_REQUEST_CODE = 1;
    public static final String BASE_URL = "http://192.168.1.36:8080/tmall/";
    public static final String URL = "app/cs";

    private Button buttonGet;
    private Button buttonPost;
    private Button download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonGet = findViewById(R.id.get_btn);
        buttonPost = findViewById(R.id.post_btn);
        download = findViewById(R.id.download_btn);
        buttonGet.setOnClickListener(this);
        buttonPost.setOnClickListener(this);
        download.setOnClickListener(this);
        RClient rClient = RConcise.inst().createRClient("test");
        rClient.setBaseUrl(BASE_URL);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        rClient.setInterceptor(interceptor);
        reqPermission();
    }

    private void req(boolean method, String ...args) {
        Request.Builder builder = Request.Builder.create(URL)
                .addHeader("token1", args[0])
                .addParam("param1", args[1])
                .addParam("param2", args[2])
                .addParam("param3", args[3])
                .client(RConcise.inst().rClient("test"))
                .respStrListener(new IRespListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e, String desp) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int respCode, String desp) {
                        Toast.makeText(MainActivity.this, respCode + "  " + desp, Toast.LENGTH_SHORT).show();
                    }
                });
        if (method) {
            builder.get();
        } else {
            builder.post();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_btn:
                req(true, "1234567890", "param1Value", "这是个get测试参数", "参数3");
                break;
            case R.id.post_btn:
                req(false, "0987654321", "postParam1Value", "这是个post测试参数", "参数3");
                break;
            case R.id.download_btn:
                startActivity(new Intent(this, DownloadActivity.class));
                break;
            default: break;
        }
    }

    private void reqPermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        int grant = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, WRITE_REQUEST_CODE);
        } else {
            RDownloadManager.inst().init(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    RDownloadManager.inst().init(this);
                } else {
                    Toast.makeText(this, "没有读写sd权限，无法操作数据库", Toast.LENGTH_SHORT).show();
                }
                break;
            default: break;
        }
    }

}
