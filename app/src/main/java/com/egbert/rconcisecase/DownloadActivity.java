package com.egbert.rconcisecase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.egbert.rconcise.download.DownloadItem;
import com.egbert.rconcise.download.ErrorCode;
import com.egbert.rconcise.download.RDownload;
import com.egbert.rconcise.download.RDownloadManager;
import com.egbert.rconcise.download.enums.DownloadStatus;
import com.egbert.rconcise.download.interfaces.IDownloadObserver;

/**
 * Created by Egbert on 4/136/2019.
 */
public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {
    private Button download1;
    private Button download2;
    private Button download3;
    private Button download4;
    private Integer id1 = -1;
    private Integer id2 = -1;
    private Integer id3 = -1;
    private Integer id4 = -1;
    private String url1 = "http://gdown.baidu.com/data/wisegame/8be18d2c0dc8a9c9/WPSOffice_177.apk";
    private String url2 = "http://p.gdown.baidu.com/dedecc0aa26733ddce51b9d54f280ca2860db9abdc9a2cf8e11e09e5fb9bac3efc0d562cecb851d7f33309943e93a0f05339e654b0027543526de7113f007a159802aba6d6f9a805141cf0cf4368df61489caf81af92839daffa2b5a44bc7665fa311acf5330e662e206cf28f6cc6500a70c533a43178fec0186ded2149005ba88ddbdf2e50c4dd0f42f9b7ba67250324fd0ec612ac9a5f5";
    private String url3 = "http://appdl.hicloud.com/dl/appdl/application/apk/8c/8cac505994d24872ae1b36a6cf3a4a01/com.smile.gifmaker.1904101502.apk?sign=portal@portal1555383344149&source=portalsite";
    private String url4 = "https://www.fiw.uni-bonn.de/demokratieforschung/personen/stichweh/pdfs/97_stw_inklusion-und-exklusion-in-der-weltgesellschaft-schule-und-erziehungssystem.pdf";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        download1 = findViewById(R.id.downlaod_1);
        download2 = findViewById(R.id.downlaod_2);
        download3 = findViewById(R.id.downlaod_3);
        download4 = findViewById(R.id.downlaod_4);
        download1.setOnClickListener(this);
        download2.setOnClickListener(this);
        download3.setOnClickListener(this);
        download4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.downlaod_1:
                handleStatus(1, download1, url1, "", "");
                break;
            case R.id.downlaod_2:
                handleStatus(2, download2, url2, "", "baidu.apk");
                break;
            case R.id.downlaod_3:
                handleStatus(3, download3, url3, "rdownlaodtest/files", "");
                break;
            case R.id.downlaod_4:
                handleStatus(4, download4, url4, "/", "");
                break;
            default: break;
        }
    }

    private void handleStatus(int flag, Button download, String url, String filePath, String fileName) {
        int tmpId;
        if (flag == 1) {
            tmpId = id1;
        } else if (flag == 2) {
            tmpId = id2;
        } else if (flag == 3) {
            tmpId = id3;
        } else {
            tmpId = id4;
        }
        if (tmpId == -1) {
            tmpId = downloadFile(download, url, filePath, fileName);
            if (flag == 1) {
                id1 = tmpId;
            } else if (flag == 2) {
                id2 = tmpId;
            } else if (flag == 3) {
                id3 = tmpId;
            } else {
                id4 = tmpId;
            }
            if (tmpId != -1) {
                DownloadItem item = RDownloadManager.inst().queryById(tmpId);
                if (item.status == DownloadStatus.waiting.getValue()) {
                    download.setText("waiting");
                }
            }
        } else {
            DownloadItem item = RDownloadManager.inst().queryById(tmpId);
            if (item.status == DownloadStatus.waiting.getValue()
                    || item.status == DownloadStatus.downloading.getValue()) {
                item.reqTask.pause();
                //RDownloadManager.inst().pause(tmpId); 此方法也可以暂停
            } else if (item.status == DownloadStatus.pause.getValue()) {
                download.setText("waiting");
                downloadFile(download, url, filePath, fileName);
            }
        }
    }

    private int downloadFile(final Button downlaod, String url, String filePath, String fileName) {
        return RDownload.Builder.create(url)
                .directory(filePath)
                .fileName(fileName)
                .downloadObserver(new IDownloadObserver() {
                    @Override
                    public void onPause(int downloadId) {
                        downlaod.setText("pause");
                    }

                    @Override
                    public void onTotalLength(int downloadId, long totalLength) {
                        downlaod.setText("downloading");
                    }

                    @Override
                    public void onProgress(int downloadId, int downloadPercent, String speed, long bytes) {
                        downlaod.setText(downloadPercent + "%--" + speed);
                    }

                    @Override
                    public void onSuccess(int downloadId, String filePath) {
//                        downlaod.setText("success");
                    }

                    @Override
                    public void onError(int downloadId, ErrorCode code, String msg) {
                        downlaod.setText(msg);
                    }

                    @Override
                    public void onFailure(int downloadId, ErrorCode code, int httpCode, String msg) {
                        downlaod.setText(httpCode + "--" + msg);
                    }
                })
                .download();
    }
}
