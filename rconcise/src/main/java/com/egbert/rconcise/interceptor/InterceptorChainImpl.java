package com.egbert.rconcise.interceptor;

import com.egbert.rconcise.internal.http.Request;
import com.egbert.rconcise.internal.http.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * 拦截器链实现类
 * Created by Egbert on 3/4/2019.
 */
public class InterceptorChainImpl implements Interceptor.Chain {
    private List<Interceptor> interceptors;
    private Request request;
    private int index;
    private int calls;

    public InterceptorChainImpl(List<Interceptor> interceptors, Request request, int index) {
        this.interceptors = interceptors;
        this.request = request;
        this.index = index;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) throws IOException{
        return proceed(request, null);
    }

    public Response proceed(Request request, HttpURLConnection connection) throws IOException {
        if (index >= interceptors.size()) {
            throw new ArrayIndexOutOfBoundsException("The index greater than size of the interceptors ");
        }
        //确保是对proceed的唯一调用 (Confirm that this is the only call to chain.proceed().)
        calls++;
        if (calls > 1) {
            throw new IllegalStateException("interceptor " + interceptors.get(index - 1)
                    + " must call proceed() exactly once");
        }

        // 将连接器链的索引值向后移一位 （Call the next interceptor in the chain.）
        InterceptorChainImpl nextChain = new InterceptorChainImpl(interceptors, request,
                index + 1);
        Interceptor interceptor = interceptors.get(index);
        Response response = interceptor.intercept(nextChain);

        // Confirm that the next interceptor made its required call to chain.proceed().
        if (index + 1 < interceptors.size() && nextChain.calls != 1) {
            throw new IllegalStateException("interceptor " + interceptor
                    + " must call proceed() exactly once");
        }

        if (response == null) {
            throw new NullPointerException("Interceptor " + interceptor + "return null");
        }

        return response;
    }

}
