package com.egbert.rconcise.download;

import com.egbert.rconcise.database.annotation.Entity;
import com.egbert.rconcise.database.dao.BaseDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Egbert on 3/15/2019.
 */
public class DownloadDao extends BaseDao<DownloadItem> {

    private List<DownloadItem> downlaodList = Collections.synchronizedList(new ArrayList<DownloadItem>());

    private DownloadItemComparator comparator = new DownloadItemComparator();

    @Override
    protected String createTable() {
        String tableName = DownloadItem.class.getAnnotation(Entity.class).value();
        return "create table if not exists " + tableName + "(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                + "url TEXT NOT NULL" + ")";
    }

    @Override
    public List<DownloadItem> query(String sql) {
        return null;
    }

    class DownloadItemComparator implements Comparator<DownloadItem> {

        @Override
        public int compare(DownloadItem o1, DownloadItem o2) {
            return o1.id - o2.id;
        }
    }

}
