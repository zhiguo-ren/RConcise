package com.egbert.rconcise.upload;

import android.content.Context;
import android.text.TextUtils;

import com.egbert.rconcise.RConcise;
import com.egbert.rconcise.database.dao.RDaoFactory;
import com.egbert.rconcise.enums.TaskStatus;
import com.egbert.rconcise.internal.Utils;
import com.egbert.rconcise.task.ReqTask;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.egbert.rconcise.database.dao.RDaoFactory.DB_NAME;

/**
 * 上传管理类，创建上传所需资源并发起上传任务<br><br>
 * Created by Egbert on 4/24/2019.
 */
public class RUploadManager {
    private static RUploadManager sManager = new RUploadManager();
    /**
     * 上传文件操作使用的全局请求头参数，可将每次都使用的请求头属性参数添加到该map中，避免每次发起上传请求都要添加请求头
     * <p>
     *     该sHeaders中默认已包含如下请求头，无需再添加：<br>
 *         Connection: keep-alive   <p>
*          Content-Type: multipart/form-data; boundary=xxx
     * <p> Content-Length: xxx
     *
     */
    private static HashMap<String, String> sHeaders = new HashMap<>();
    private UploadDao uploadDao;
    private AtomicBoolean isInit = new AtomicBoolean(false);

    private RUploadManager() {
    }

    public static RUploadManager inst() {
        return sManager;
    }

    /**
     * 在主activity或者application 等入口出调用，且应用只需调用一次
     * 初始化方法
     */
    public synchronized void init(Context context) {
        if (isInit.compareAndSet(false, true)) {
            RDaoFactory.getInst().openOrCreateDb(DB_NAME, context);
            uploadDao = RDaoFactory.getInst().getDao(UploadDao.class, UploadItem.class, DB_NAME);
        }
    }

    public synchronized int upload(RUpload rUpload) {
        // 获取part路劲，如有多个文件，路劲拼接以“|”分割
        MultiPartBody body = rUpload.multiPartBody();
        StringBuilder path = new StringBuilder();
        for (MultiPartBody.Part part : body.getBodyParts()) {
            if (part.isFile()) {
                if (path.length() != 0) {
                    path.append("|");
                }
                path.append(((File)part.getContent()).getAbsolutePath());
            }
        }
        if (path.length() == 0) {
            throw new IllegalArgumentException("FilePath is null, Include at least one file part");
        }
        // 处理url，拼接baseUrl
        String absoluteUrl;
        String relativeUrl = rUpload.url();
        if (Utils.verifyUrl(relativeUrl, false)) {
            absoluteUrl = relativeUrl;
        } else {
            if (!TextUtils.isEmpty(rUpload.rClientKey())) {
                String baseUrl = RConcise.inst().rClient(rUpload.rClientKey()).getBaseUrl();
                if (Utils.verifyUrl(baseUrl, true)) {
                    absoluteUrl = baseUrl + relativeUrl;
                } else {
                    throw new IllegalArgumentException("The BaseUrl is illegal.");
                }
            } else {
                throw new IllegalArgumentException("The BaseUrl is not existed.");
            }
        }
        rUpload = rUpload.newBuilder().url(absoluteUrl).build();
        UploadItem uploadItem = uploadDao.findRecord(absoluteUrl, path.toString());
        if (uploadItem == null) {
            uploadItem = new UploadItem();
            uploadItem.filePath = path.toString();
            uploadItem.url = absoluteUrl;
            uploadItem.fileName = new File(uploadItem.filePath).getName();
            uploadItem.status = TaskStatus.waiting.getValue();
            uploadItem.id = uploadDao.addRecord(uploadItem);
        } else {
            uploadItem.status = TaskStatus.waiting.getValue();
            uploadDao.updateRecord(uploadItem);
        }
        if (uploadItem.reqTask == null) {
            uploadItem.reqTask = new ReqTask(rUpload);
            uploadItem.reqTask.setUploadItem(uploadItem);
        }
        uploadItem.reqTask.start();
        return uploadItem.id;
    }

    /**
     * 通过id暂停该id对应的上传任务
     * @param uploadId 上传任务id
     */
    public void pause(int uploadId) {
        UploadItem item = uploadDao.findRecordByIdFromCached(uploadId);
        if (item != null && item.reqTask != null) {
            item.reqTask.pause();
        }
    }

    /**
     * @param uploadId 上传任务id
     */
    public void cancel(int uploadId) {
        UploadItem item = uploadDao.findRecordById(uploadId);
        if (item != null) {
            if (item.reqTask != null) {
                item.reqTask.cancel(false);
            } else {
                uploadDao.delRecord(uploadId);
            }
        }
    }

    /**
     * @return 返回upload 数据dao 操作类 可进行增删改查操作
     */
    public UploadDao getUploadDao() {
        return uploadDao;
    }

    /**
     * 根据id查询上传实体bean
     * @param uploadId 上传任务id
     * @return 返回上传实体对象
     */
    public UploadItem queryById(int uploadId) {
        return uploadDao.findRecordById(uploadId);
    }

    /**
     * 获取上传操作的全局请求头 {@link RUploadManager#sHeaders 见sHeaders};
     * @return 上传全局请求头map
     */
    public static HashMap<String, String> getsHeaders() {
        return sHeaders;
    }

    /**
     * 添加上传操作的全局请求头header
     * @param headers 请求头以key-value形式添加到map中
     * @see RUploadManager#sHeaders
     */
    public static void addHeaders(HashMap<String, String> headers) {
        if (headers != null) {
            RUploadManager.sHeaders.putAll(headers);
        }
    }

    /**
     * 添加上传操作的全局请求头header
     * @param headerKey   请求头key
     * @param headerValue 请求头value
     *  <p>例如：{@code headerMap.put("Content-Type", "application/x-www-form-urlencoded");}
     * @see RUploadManager#sHeaders
     */
    public static void addHeader(String headerKey, String headerValue) {
        RUploadManager.sHeaders.put(headerKey, headerValue);
    }
}
