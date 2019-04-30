package com.egbert.rconcisecase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.egbert.rconcise.download.RDownload;
import com.egbert.rconcise.download.RDownloadManager;
import com.egbert.rconcise.download.listener.IDownloadObserver;
import com.egbert.rconcise.internal.ErrorCode;
import com.egbert.rconcisecase.model.Download;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cn.bingoogolapple.progressbar.BGAProgressBar;

/**
 * Created by Egbert on 4/136/2019.
 */
public class DownloadActivity extends AppCompatActivity {
    private RecyclerView downloadRv;
    private String url1 = "http://gdown.baidu.com/data/wisegame/8be18d2c0dc8a9c9/WPSOffice_177.apk";
    private String url2 = "http://p.gdown.baidu.com/dedecc0aa26733ddce51b9d54f280ca2860db9abdc9a2cf8e11e09e5fb9bac3efc0d562cecb851d7f33309943e93a0f05339e654b0027543526de7113f007a159802aba6d6f9a805141cf0cf4368df61489caf81af92839daffa2b5a44bc7665fa311acf5330e662e206cf28f6cc6500a70c533a43178fec0186ded2149005ba88ddbdf2e50c4dd0f42f9b7ba67250324fd0ec612ac9a5f5";
    private String url3 = "http://appdl.hicloud.com/dl/appdl/application/apk/8c/8cac505994d24872ae1b36a6cf3a4a01/com.smile.gifmaker.1904101502.apk?sign=portal@portal1555383344149&source=portalsite";
    private String url4 = "https://www.fiw.uni-bonn.de/demokratieforschung/personen/stichweh/pdfs/97_stw_inklusion-und-exklusion-in-der-weltgesellschaft-schule-und-erziehungssystem.pdf";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        downloadRv = findViewById(R.id.download_rv);
        DownloadAdapter adapter = new DownloadAdapter();
        downloadRv.setAdapter(adapter);
        downloadRv.setLayoutManager(new LinearLayoutManager(this));
        adapter.setNewData(createData());
    }

    private ArrayList<Download> createData() {
        ArrayList<Download> task = new ArrayList<>();
        Download download1 = new Download();
        download1.url = url1;

        Download download2 = new Download();
        download2.url = url2;
        download2.fileName = "baidu.apk";

        Download download3 = new Download();
        download3.url = url3;
        download3.filePath = "/rdownload/files"; // 下载到根目录下的rdownload/files

        Download download4 = new Download();
        download4.url = url4;
        download4.filePath = "/"; // 下载到sd卡根目录 只需要用/

        task.add(download1);
        task.add(download2);
        task.add(download3);
        task.add(download4);
        return task;
    }

    private static class DownloadAdapter extends BaseQuickAdapter<Download, BaseViewHolder> {
        private DecimalFormat format;

        public DownloadAdapter() {
            super(R.layout.item_download_rv);
            format = new DecimalFormat("#.##");
        }

        @Override
        protected void convert(final BaseViewHolder helper, final Download item) {
            if (item.downloadItem == null) {
                item.observer = new IDownloadObserver() {
                    @Override
                    public void onPause(int downloadId) {
                        helper.setText(R.id.speed_tv, "暂停下载");
                    }

                    @Override
                    public void onCancel(ErrorCode code) {
                        helper.setText(R.id.speed_tv, code.getMsg());
                        helper.setText(R.id.curr_and_total_tv, "");
                        helper.setImageResource(R.id.action_btn, android.R.drawable.ic_media_play);
                        helper.setVisible(R.id.action_btn, true);
                        BGAProgressBar progressBar = helper.getView(R.id.pb);
                        progressBar.setProgress(0);
                    }

                    @Override
                    public void onStart(int downloadId, long totalLength) {
                        helper.setText(R.id.file_name_tv, item.downloadItem.fileName);
                        item.total = format.format( totalLength / 1024d / 1024) + "MB";
                        long curr = item.downloadItem.currLen;
                        helper.setText(R.id.curr_and_total_tv, format.format(curr / 1024d / 1024) + "MB/" + item.total);
                    }

                    @Override
                    public void onProgress(int downloadId, final int downloadPercent, String speed, long bytes) {
                        helper.setText(R.id.curr_and_total_tv, format.format(bytes / 1024d / 1024) + "MB/"
                                + item.total);
                        helper.setText(R.id.speed_tv, speed);
                        BGAProgressBar progressBar = helper.getView(R.id.pb);
                        progressBar.setProgress(downloadPercent);
                    }

                    @Override
                    public void onSuccess(int downloadId, String filePath) {
                        helper.setText(R.id.speed_tv, "下载完成");
                        helper.setVisible(R.id.action_btn, false);
                    }

                    @Override
                    public void onError(int downloadId, ErrorCode code, String msg) {
                        helper.setText(R.id.speed_tv, msg);
                        item.isStart = false;
                    }

                    @Override
                    public void onFailure(int downloadId, ErrorCode code, int httpCode, String msg) {
                        helper.setText(R.id.speed_tv, msg);
                        helper.setImageResource(R.id.action_btn, android.R.drawable.ic_media_play);
                    }
                };
            }

            helper.getView(R.id.action_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.isStart) {
                        item.isStart = true;
                        item.id = downloadFile(item.url, item.filePath, item.fileName, item.observer);
                        item.downloadItem = RDownloadManager.inst().queryById(item.id);
                        item.isCancel = false;
                    } else {
                        item.isStart = false;
                        item.downloadItem.reqTask.pause();
                    }
                    helper.setImageResource(R.id.action_btn, item.isStart
                            ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                }
            });
            helper.getView(R.id.del_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.isCancel) {
                        item.isCancel = true;
                        item.isStart = false;
                        RDownloadManager.inst().cancel(item.id, true);
                    }
                }
            });
        }

        private int downloadFile(String url, String filePath, String fileName, IDownloadObserver observer) {
            return RDownload.Builder.create(url)
                    .directory(filePath)
                    .fileName(fileName)
                    .downloadObserver(observer)
                    .download();
        }
    }
}
