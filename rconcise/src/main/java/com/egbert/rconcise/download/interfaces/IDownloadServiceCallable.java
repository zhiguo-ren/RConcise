package com.egbert.rconcise.download.interfaces;

import com.egbert.rconcise.download.DownloadItem;

/**
 * Created by Egbert on 3/18/2019.
 */
public interface IDownloadServiceCallable {

    void onDownloadStatusChanged(DownloadItem downloadItem);

    void onTotalLengthReceived(DownloadItem downloadItem);

    void onCurrProgress(DownloadItem downloadItem, double downLen, long speed);

    void onDownloadSuccess(DownloadItem downloadItem);

    void onDownloadPause(DownloadItem downloadItem);

    void onDownloadError(DownloadItem downloadItem, int var2, String var3);
}
