package com.egbert.rconcise.download;

import android.text.TextUtils;
import android.util.Log;

import com.egbert.rconcise.download.listener.IDownloadObserver;
import com.egbert.rconcise.internal.ReqMethod;
import com.egbert.rconcise.internal.http.IRequest;

import java.io.IOException;
import java.util.HashMap;

/**
 * 发起下载请求任务，配置下载请求的参数及回调监听<br><br>
 * Created by Egbert on 3/19/2019.
 */
public final class RDownload implements IRequest {

    /**
     * 下载使用的完整url地址
     */
    private final String url;

    /**
     * 请求头
     */
    private final HashMap<String, String> headers;

    /**
     * post 请求参数map
     */
    private final HashMap<String, String> params;

    private final String method;

    /**
     * 下载回调接口，用于下载状态及进度的监听
     */
    private final IDownloadObserver observer;

    private final String directory;

    private final String fileName;

    public RDownload(Builder builder) {
        this.headers = builder.headers;
        this.params = builder.params;
        this.url = builder.url;
        this.method = builder.method;
        this.observer = builder.observer;
        this.fileName = builder.fileName;
        this.directory = builder.directory;
    }

    public String url() {
        return url;
    }

    public HashMap<String, String> headers() {
        return headers;
    }

    public HashMap<String, String> params() {
        return params;
    }

    public String method() {
        return method;
    }

    public IDownloadObserver observer() {
        return observer;
    }

    public String directory() {
        return directory;
    }

    public String fileName() {
        return fileName;
    }

    public static class Builder {
        private final String url;
        private String fileName;
        private String directory;
        private HashMap<String, String> headers;
        private HashMap<String, String> params;
        private String method;
        private IDownloadObserver observer;

        public Builder(String url) {
            this.url = url;
        }


        public static Builder create(String url) {
            return new Builder(url);
        }

        public Builder(RDownload rDownload) {
            this.url = rDownload.url();
            this.fileName = rDownload.fileName();
            this.directory = rDownload.directory();
            this.headers = rDownload.headers();
            this.params = rDownload.params();
            this.method = rDownload.method();
            this.observer = rDownload.observer();
        }

        /**
         * post 请求参数map
         */
        public Builder addParam(String name, String value) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put(name, value);
            return this;
        }

        /**
         * post 请求参数map
         */
        public Builder addParams(HashMap<String, String> params) {
            if (this.params == null) {
                this.params = params;
            } else {
                this.params.putAll(params);
            }
            return this;
        }

        /**
         * 单个添加请求头 如：<br>
         *     {@code headerMap.put("Content-Type", "application/x-www-form-urlencoded");}
         */
        public Builder addHeader(String name, String value) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(name, value);
            return this;
        }

        /**
         * 批量添加请求头
         */
        public Builder addHeaders(HashMap<String, String> headers) {
            if (headers != null) {
                if (this.headers == null) {
                    this.headers = headers;
                } else {
                    this.headers.putAll(headers);
                }
            }
            return this;
        }

        /**
         * 下载回调接口，用于下载状态及进度的监听
         */
        public Builder downloadObserver(IDownloadObserver downloadObserver) {
            this.observer = downloadObserver;
            return this;
        }

        /**
         *  请求方法,下载只支持GET和POST
         */
        public Builder method(String method) {
            this.method = method;
            return this;
        }

        /**
         * 添加文件下载的目标文件目录（下载到哪）,根目录之下的目录路径（不包含sd卡根目录，由框架统一获取系统外置存储根目录），
         * 如：rdownload/imgs/   如果要存到sd卡根目录，只使用 "/" 即可
         *  不设置为默认目录
         */
        public Builder directory(String directory) {
            this.directory = directory;
            return this;
        }

        /**
         * 添加文件下载后保存和显示的文件名（包括扩展名） 不设置为默认名称
         */
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public RDownload build() {
            if (TextUtils.isEmpty(method)) {
                method = ReqMethod.GET.getMethod();
            }
            return new RDownload(this);
        }

        /**
         * 添加下载请求任务到线程池
         * @return 返回用于标识下载记录的唯一Id, 为-1则添加下载请求任务失败
         */
        public int download() {
            try {
                return RDownloadManager.inst().download(build());
            } catch (IOException e) {
                Log.e(RDownload.class.getSimpleName(), Log.getStackTraceString(e));
            }
            return -1;
        }

    }
}
