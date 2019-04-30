package com.egbert.rconcise.upload;

import com.egbert.rconcise.internal.http.IRequest;
import com.egbert.rconcise.upload.listener.IUploadObserver;

import java.util.HashMap;

/**
 * 发起上传请求任务，配置上传请求的参数及回调监听等；<br><br>
 * Created by Egbert on 4/24/2019.
 */
public class RUpload implements IRequest {
    /**
     * 上传使用的url地址
     */
    private final String url;

    /**
     * 请求头
     */
    private final HashMap<String, String> headers;

    /**
     * 本次请求使用的RClient的唯一标识，用于获取RClient实例<br>
     * <p>参见 {@link com.egbert.rconcise.RClient RClient}
     */
    private final String rClientKey;

    /**
     * 上传状态监听回调
     */
    private final IUploadObserver uploadObserver;

    /**
     * 上传请求的body数据，通过<code>MultiPartBody<code/>封装multipart/form-data协议中各part的数据,不能为null
     * @see MultiPartBody MultiPartBody
     */
    private final MultiPartBody multiPartBody;

    public RUpload(Builder builder) {
        this.url = builder.url;
        this.headers = builder.headers;
        this.rClientKey = builder.rClientKey;
        this.uploadObserver = builder.uploadObserver;
        this.multiPartBody = builder.multiPartBody;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public String url() {
        return url;
    }

    public HashMap<String, String> headers() {
        return headers;
    }

    public String rClientKey() {
        return rClientKey;
    }

    public IUploadObserver observer() {
        return uploadObserver;
    }

    public MultiPartBody multiPartBody() {
        return multiPartBody;
    }

    public static class Builder {
        /**
         * 上传的url地址
         */
        private String url;

        /**
         * 请求头 <br>
         *     已下请求头会默认添加，无需再手动添加<p>
         *      Connection: keep-alive   <p>
         *      Content-Type: multipart/form-data; boundary=xxx
         *  <p> Content-Length: xxx
         */
        private HashMap<String, String> headers;

        /**
         * 本次请求使用的RClient的唯一标识，用于获取RClient实例<br>
         * <p>参见 {@link com.egbert.rconcise.RClient RClient}
         */
        private String rClientKey;

        /**
         * 上传状态监听回调
         */
        private IUploadObserver uploadObserver;

        /**
         * {@link RUpload#multiPartBody 见RUpload.multiPartBody}
         */
        private MultiPartBody multiPartBody;

        public Builder(String url) {
            this.url = url;
        }

        /**
         * 创建RUpload的Builder构建器
         * @param url {@link RUpload#url  见RUpload.url}
         * @return Builder
         */
        public static Builder create(String url) {
            return new Builder(url);
        }

        public Builder(RUpload rUpload) {
            this.url = rUpload.url();
            this.headers = rUpload.headers();
            this.rClientKey = rUpload.rClientKey();
            this.uploadObserver = rUpload.observer();
            this.multiPartBody = rUpload.multiPartBody();
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         *
         * @param rClientKey {@link RUpload#rClientKey 见RUpload.rClientKey}
         * @return Builder
         */
        public Builder rClientKey(String rClientKey) {
            this.rClientKey = rClientKey;
            return this;
        }

        /**
         * 单个添加请求头 如：<br>
         * <code>headerMap.put("Content-Type", "application/x-www-form-urlencoded");<code/>
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
        public Builder uploadObserver(IUploadObserver uploadObserver) {
            this.uploadObserver = uploadObserver;
            return this;
        }

        /**
         * @param multiPartBody {@link RUpload#multiPartBody 见RUpload.multiPartBody}
         * @return Builder
         */
        public Builder multiPartBody(MultiPartBody multiPartBody) {
            this.multiPartBody = multiPartBody;
            return this;
        }

        public RUpload build() {
            if (this.multiPartBody == null || this.multiPartBody.getBodyParts().isEmpty()) {
                throw new IllegalArgumentException("The MultiPartBody is null, or length of the bodyParts is 0");
            }
            return new RUpload(this);
        }

        /**
         * 添加下载请求任务到线程池
         * @return 返回用于标识下载记录的唯一Id, 为-1则添加下载请求任务失败
         */
        public int upload() {
            return RUploadManager.inst().upload(build());
        }

    }
}
