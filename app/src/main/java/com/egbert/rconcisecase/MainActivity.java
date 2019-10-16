package com.egbert.rconcisecase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.egbert.rconcise.RClient;
import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.download.RDownloadManager;
import com.egbert.rconcise.interceptor.HttpLoggingInterceptor;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.listener.IRespListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String RCLIENT_KEY = "test";
    private static final int WRITE_REQUEST_CODE = 1;
    public static final String BASE_URL = "http://192.168.1.34:8080/tmall/";
    public static final String URL = "app/cs";

    private Button buttonGet;
    private Button buttonPost;
    private Button download;
    private Button upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonGet = findViewById(R.id.get_btn);
        buttonPost = findViewById(R.id.post_btn);
        download = findViewById(R.id.download_btn);
        upload = findViewById(R.id.upload_btn);
        buttonGet.setOnClickListener(this);
        buttonPost.setOnClickListener(this);
        download.setOnClickListener(this);
        upload.setOnClickListener(this);
        RClient rClient = RConcise.inst().createRClient(RCLIENT_KEY);
        rClient.setBaseUrl(BASE_URL);
        /*try {
            rClient.setSelfCert(true, getAssets().open("cacert.pem"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        rClient.addInterceptor(interceptor);
        reqPermission();
        reqIndex();
    }

    private void req(boolean method, String ...args) {
        Request.Builder builder = Request.Builder.create(URL)
                .addHeader("token1", args[0])
                .addParam("param1", args[1])
                .addParam("param2", args[2])
                .addParam("param3", args[3])
                .client(RConcise.inst().rClient(RCLIENT_KEY))
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
            case R.id.upload_btn:
                startActivity(new Intent(this, UploadActivity.class));
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
            //初始化下载管理类 需要有读写存储权限
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

    public void reqIndex() {
        Request.Builder.create("https://api2.xintujing.cn/v1/course/homeJson")
                .client(RConcise.inst().rClient(RCLIENT_KEY))
                .setActivity(this)
                .respStrListener(new IRespListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        buttonGet.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                buttonGet.setVisibility(View.INVISIBLE);
                            }
                        }, 50);
                    }

                    @Override
                    public void onError(Exception e, String desp) {
//                        super.onError(e, desp);
                    }

                    @Override
                    public void onFailure(int respCode, String desp) {
//                        super.onFailure(respCode, desp);
                    }
                })
                .get();
    }

}
