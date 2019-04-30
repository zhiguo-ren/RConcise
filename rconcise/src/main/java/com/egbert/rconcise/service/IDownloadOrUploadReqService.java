package com.egbert.rconcise.service;

/**
 * Created by Egbert on 4/25/2019.
 */
public interface IDownloadOrUploadReqService {

    void resume();

    void pause();

    void cancel(boolean flag);
}
