package com.egbert.rconcisecase;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.egbert.rconcise.internal.ErrorCode;
import com.egbert.rconcise.task.DownloadUploadThreadPoolManager;
import com.egbert.rconcise.upload.MultiPartBody;
import com.egbert.rconcise.upload.RUpload;
import com.egbert.rconcise.upload.RUploadManager;
import com.egbert.rconcise.upload.UploadItem;
import com.egbert.rconcise.upload.listener.IUploadObserver;
import com.egbert.rconcisecase.model.Upload;
import com.egbert.rconcisecase.model.User;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bingoogolapple.progressbar.BGAProgressBar;

import static com.egbert.rconcisecase.MainActivity.RCLIENT_KEY;

/**
 * Created by Egbert on 4/136/2019.
 */
public class UploadActivity extends AppCompatActivity {
    private static final int FILE_SELECT_CODE = 0;
    private RecyclerView uploadRv;
    private String url1 = "app/upload1";
    private String url2 = "app/upload2";
    private UploadAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        TextView title = findViewById(R.id.title);
        title.setText("RUpload 文件上传使用demo");
        uploadRv = findViewById(R.id.download_rv);
        Button button = findViewById(R.id.add_file);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加上传文件
                addFile();
            }
        });
        RUploadManager.inst().init(this);
        DownloadUploadThreadPoolManager.getInst().setAloneUpload(true);
        adapter = new UploadAdapter();
        uploadRv.setAdapter(adapter);
        uploadRv.setLayoutManager(new LinearLayoutManager(this));
        createData();
    }

    private void addFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void createData() {
        List<UploadItem> uploadItems = RUploadManager.inst().getUploadDao().query(new UploadItem());
        ArrayList<Upload> tasks = new ArrayList<>();
        if (uploadItems != null && !uploadItems.isEmpty()) {
            for (UploadItem uploadItem : uploadItems) {
                Upload upload = new Upload();
                upload.fileName = uploadItem.fileName;
                upload.filePath = uploadItem.filePath;
                upload.id = uploadItem.id;
                upload.url = uploadItem.url;
                upload.total = String.valueOf(uploadItem.totalLen);
                upload.uploadItem = uploadItem;
                tasks.add(upload);
            }
            adapter.setNewData(tasks);
        }
    }

    private void createNewTask(File file, String url) {
        ArrayList<Upload> tasks = new ArrayList<>();
        Upload upload = new Upload();
        upload.filePath = file.getAbsolutePath();
        upload.fileName = file.getName();
        upload.isNewTask = true;
        upload.url = url;
        tasks.add(upload);
        adapter.addData(tasks);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                // Get the path
                String path = getPath(this, uri);
                createNewTask(new File(path), url1);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static class UploadAdapter extends BaseQuickAdapter<Upload, BaseViewHolder> {
        private DecimalFormat format;

        public UploadAdapter() {
            super(R.layout.item_download_rv);
            format = new DecimalFormat("#.##");
        }

        @Override
        protected void convert(final BaseViewHolder helper, final Upload item) {
            item.observer = new IUploadObserver() {
                @Override
                public void onPause(int uploadId) {
                    helper.setText(R.id.speed_tv, "暂停上传");
                    helper.setImageResource(R.id.action_btn, android.R.drawable.ic_media_play);
                }

                @Override
                public void onCancel(ErrorCode code) {
                    helper.setText(R.id.speed_tv, code.getMsg());
                    helper.setText(R.id.curr_and_total_tv, "");
                    helper.setImageResource(R.id.action_btn, android.R.drawable.ic_media_play);
                    helper.setVisible(R.id.action_btn, true);
                    BGAProgressBar progressBar = helper.getView(R.id.pb);
                    progressBar.setProgress(0);
                    getData().remove(item);
                    notifyDataSetChanged();
                }

                @Override
                public void onStart(int uploadId, long totalLength) {
                    Log.e(TAG, "onStart: " + System.currentTimeMillis() / 1000);
                    helper.setText(R.id.file_name_tv, item.uploadItem.fileName);
                    item.total = format.format(totalLength / 1024d / 1024) + "MB";
                    long curr = item.uploadItem.currLen;
                    helper.setText(R.id.curr_and_total_tv, format.format(curr / 1024d / 1024) + "MB/" + item.total);
                }

                @Override
                public void onProgress(int uploadId, final int uploadPercent, String speed, long bytes) {
                    helper.setText(R.id.curr_and_total_tv, format.format(bytes / 1024d / 1024) + "MB/"
                            + item.total);
                    helper.setText(R.id.speed_tv, speed)
                            .setText(R.id.percent_tv, uploadPercent + "%");
                    BGAProgressBar progressBar = helper.getView(R.id.pb);
                    progressBar.setProgress(uploadPercent);
                }

                @Override
                public void onSuccess(int uploadId) {
                    Log.e(TAG, "onSuccess: " + System.currentTimeMillis() / 1000);
                    helper.setText(R.id.speed_tv, "上传完成");
                    helper.setVisible(R.id.action_btn, false);
                }

                @Override
                public void onError(int uploadId, ErrorCode code, String msg) {
                    helper.setText(R.id.speed_tv, msg);
                    item.isStart = false;
                }

                @Override
                public void onFailure(int uploadId, ErrorCode code, int httpCode, String msg) {
                    helper.setText(R.id.speed_tv, msg);
                    helper.setImageResource(R.id.action_btn, android.R.drawable.ic_media_play);
                }
            };
            if (item.uploadItem == null) {
                if (item.isNewTask) {
                    item.isNewTask = false;
                    item.isStart = true;
                    MultiPartBody body = new MultiPartBody();
                    MultiPartBody.Part part = MultiPartBody.createPart(new File(item.filePath), "file");
                    part.dispositionFilename(item.fileName);
                    body.addPart(part);
                    body.addPart(MultiPartBody.createPart(new User("rzg", "30",
                            "123456", "男"), "param"));
                    int id = RUpload.Builder.create(item.url)
                            .rClientKey(RCLIENT_KEY)
                            .multiPartBody(body)
                            .uploadObserver(item.observer)
                            .upload();
                    item.uploadItem = RUploadManager.inst().queryById(id);
                }
            }

            helper.getView(R.id.action_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.isStart) {
                        item.isStart = true;
                        MultiPartBody body = new MultiPartBody();
                        MultiPartBody.Part part = MultiPartBody.createPart(new File(item.filePath), "file");
                        part.dispositionFilename(item.fileName);
                        body.addPart(part);
                        HashMap<String, String> param = new HashMap<>();
                        param.put("param1", "参数1");
                        param.put("param2", "参数2");
                        param.put("param3", "参数3");
                        body.addPart(MultiPartBody.createPart(param, "param"));
                        item.id = uploadFile(item.url, item.observer, body);
                        item.uploadItem = RUploadManager.inst().queryById(item.id);
                        item.isCancel = false;
                    } else {
                        item.isStart = false;
                        item.uploadItem.reqTask.pause();
                    }
                    helper.setImageResource(R.id.action_btn, item.isStart
                            ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                }
            });
            helper.setText(R.id.file_name_tv, item.fileName);
            helper.setText(R.id.speed_tv, item.uploadItem.status == 5 ? "上传完成" : "继续上传");
            helper.getView(R.id.del_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.isCancel) {
                        item.isCancel = true;
                        item.isStart = false;
                        RUploadManager.inst().cancel(item.id);
                        getData().remove(item);
                        notifyDataSetChanged();
                    }
                }
            });
        }

        private int uploadFile(String url, IUploadObserver observer, MultiPartBody body) {
            return RUpload.Builder.create(url)
                    .uploadObserver(observer)
                    .rClientKey(RCLIENT_KEY)
                    .multiPartBody(body)
                    .upload();
        }
    }
}
