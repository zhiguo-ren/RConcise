package com.egbert.rconcisecase;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.egbert.rconcise.RClient;
import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.interceptor.HttpLoggingInterceptor;
import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.listener.IRespListener;

public class MainActivity extends AppCompatActivity {
    public static final String BASE_URL = "http://192.168.1.36:8080/tmall/";
    public static final String URL = "app/cs";

    private Button buttonGet;
    private Button buttonPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonGet = findViewById(R.id.get_btn);
        buttonPost = findViewById(R.id.post_btn);
        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req(true, "1234567890", "param1Value", "这是个get测试参数", "参数3");
            }
        });
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req(false, "0987654321", "postParam1Value", "这是个post测试参数", "参数3");
            }
        });
        RClient rClient = RConcise.inst().createRClient("test");
        rClient.setBaseUrl(BASE_URL);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        rClient.setInterceptor(interceptor);
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
}
