# RConcise 
网络请求库，基于httpUrlConnection完成，包括post，get网络请求，批量上传，批量下载，断点下载，链式调用等，后期将支持https，使用简单方便。

使用方式： implementation 'com.egbert.rconcise:rconcise:1.0.4' // 持续更新

详细使用看demo 请将demo中的请求地址更换为自己的服务端地址

---
**主要api调用事例：**
#### 创建RClient，及post，get请求
```
//创建RClient
RClient rClient = RConcise.inst().createRClient("client_key");
//设置baseUrl, 此处跟okhttp类似
rClient.setBaseUrl("base_url");
// 设置log拦截器，用于打印请求响应log日志
HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
rClient.addInterceptor(interceptor);

Request.Builder builder = Request.Builder.create(URL)
                .addHeader("token1", "test_token")
                .addParam("param1", "value1")
                .addParam("param2", "value2")
                .addParam("param3", "value3")
                .client(RConcise.inst().rClient("client_key"))
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
                })
                .get(); //或者post(); 即可发送get或post请求
```

#### 下载文件
```
//初始化下载管理类(全局只初始化一次，建议在主activity或application中调用)
//需要先获取读写设备存储权限(Manifest.permission.WRITE_EXTERNAL_STORAGE)
RDownloadManager.inst().init(this);
```
```
// 下载操作，返回下载任务id，方便后续状态的追踪更新，url必须是完整的，此处不支持baseUrl
// 通过IDownloadObserver回调下载各状态
private int downloadFile(String url, String filePath, String fileName, IDownloadObserver observer) {
            return RDownload.Builder.create(url)
                    .directory(filePath)
                    .fileName(fileName)
                    .downloadObserver(observer)
                    .download();
        }
```
```
//取消下载任务
RDownloadManager.inst().cancel(downloadId, true);
//暂停下载任务
RDownloadManager.inst().pause(downloadId);
```
```
//查询下载任务信息，通过DownloadItem封装
DownloadItem downloadItem = RDownloadManager.inst().queryById(item.id);
```

#### 上传文件
```
// 初始化上传管理器，全局只需要初始化一次(建议在application中调用)
RUploadManager.inst().init(this);
// 开启独立的上传线程池，默认(false)与下载共用同一个线程池
DownloadUploadThreadPoolManager.getInst().setAloneUpload(true);
```
```
// 通过MultiPartBody封装上传的body，MultiPartBody.Part对应每个上传子部分
// part可为文件或字符串，一个body可添加多个part
MultiPartBody body = new MultiPartBody();
MultiPartBody.Part part = MultiPartBody.createPart(new File(item.filePath), "file");
part.dispositionFilename(item.fileName);
// 向body添加part
body.addPart(part);
// 非文件part可以使用实体类或Map等封装数据，最终转为json字符串上传，也可以直接使用String类，原样输出
body.addPart(MultiPartBody.createPart(new User("张三", "30",
        "123456", "男"), "param"));
// 执行上传，返回上传任务id，通过IUploadObserver监听上传状态
// IUploadObserver回调的进度和网速是写入输出流的进度和网速，并非真实网络传输的进度和网速
// rClientKey是之前创建的RClient使用的key，可用RClient封装baseUrl
int id = RUpload.Builder.create(url)
        .rClientKey("client_key")
        .multiPartBody(body)
        .uploadObserver(iUploadObserver)
        .upload();
// 通过id查询上传任务信息，如不需要则不必获取
UploadItem uploadItem = RUploadManager.inst().queryById(id);
// 取消上传任务
RUploadManager.inst().cancel(id);
// 暂停上传任务
RUploadManager.inst().pause(id);
```
#### DownloadUploadThreadPoolManager 下载和上传线程池管理类
```
// 终止下载线程池工作
DownloadUploadThreadPoolManager.getInst().terminateDownload();
// 启动下载线程池
DownloadUploadThreadPoolManager.getInst().launchDownload();
// 终止上传线程池工作
DownloadUploadThreadPoolManager.getInst().terminateUpload();
// 启动上传线程池
DownloadUploadThreadPoolManager.getInst().launchUpload();
```




