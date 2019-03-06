package com.egbert.rconcisecase;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.egbert.rconcise.RClient;
import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.interceptor.Interceptor;
import com.egbert.rconcise.internal.http.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RClient rClient = RConcise.inst().createRClient("test");
        rClient.setBaseUrl("");
        rClient.setInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request());
            }
        });

    }
}
