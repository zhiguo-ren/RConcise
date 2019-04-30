package com.egbert.rconcise.download;

import com.egbert.rconcise.database.dao.BaseDao;
import com.egbert.rconcise.enums.Priority;
import com.egbert.rconcise.enums.TaskStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 下载任务记录的数据库操作类<br><br>
 * Created by Egbert on 3/15/2019.
 */
public class DownloadDao extends BaseDao<DownloadItem> {

    private static List<DownloadItem> sDownloadList = Collections.synchronizedList(new ArrayList<DownloadItem>());

    private DownloadItemComparator comparator = new DownloadItemComparator();

    private static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    @Override
    protected String createTable() {
        return "create table if not exists " + tableName + "(id INTEGER NOT NULL PRIMARY KEY,"
                + "url TEXT NOT NULL," + "file_path TEXT NOT NULL," + "file_name TEXT," + "status INTEGER,"
                + "curr_len INTEGER," + "total_len INTEGER," + "start_time VARCHAR(20)," + "end_time VARCHAR(20),"
                + "user_id TEXT," + "task_type VARCHAR(10)," + "priority INTEGER," + "stop_mode INTEGER,"
                + "unique(file_path)" + ")";
    }

    /**
     * 根据下载地址和下载文件路径查找下载记录
     */
    /*public DownloadItem findRecord(String url, String filePath) {
        synchronized (DownloadDao.class) {
            for (DownloadItem item : sDownloadList) {
                if (item.url.equals(url) && item.filePath.equals(filePath)) {
                    return item;
                }
            }
            DownloadItem where = new DownloadItem();
            where.url = url;
            where.filePath = filePath;
            List<DownloadItem> results = query(where);
            if (!results.isEmpty()) {
                return results.get(0);
            }
            return null;
        }
    }*/

    /**
     * 查找全部下载记录
     */
    public List<DownloadItem> selectAllRecord() {
        synchronized (DownloadDao.class) {
            DownloadItem where = new DownloadItem();
            return query(where);
        }
    }

    /**
     * 根据下载文件路径查找下载记录
     */
    public DownloadItem findRecord(String filePath) {
        synchronized (DownloadDao.class) {
            for (DownloadItem item : sDownloadList) {
                if (item.filePath.equals(filePath)) {
                    return item;
                }
            }
            DownloadItem where = new DownloadItem();
            where.filePath = filePath;
            List<DownloadItem> results = query(where);
            if (results == null || results.isEmpty()) {
                return null;
            }
            return results.get(0);
        }
    }

    /**
     * 根据id查找下载记录对象
     */
    public DownloadItem findRecordById(int recordId) {
        synchronized (DownloadDao.class) {
            for (DownloadItem item : sDownloadList) {
                if (item.id == recordId) {
                    return item;
                }
            }
            DownloadItem where = new DownloadItem();
            where.id = recordId;
            List<DownloadItem> result = query(where);
            if (result == null || result.isEmpty()) {
                return null;
            }
            return result.get(0);
        }
    }

    /**
     * 根据id查找下载记录对象（只从缓存中查询）
     */
    public DownloadItem findRecordByIdFromCached(int recordId) {
        synchronized (DownloadDao.class) {
            for (DownloadItem item : sDownloadList) {
                if (item.id == recordId) {
                    return item;
                }
            }
            return null;
        }
    }

    /**
     * 根据id从缓存中移除下载记录
     */
    public boolean delRecordFromCached(int id) {
        synchronized (DownloadItem.class) {
            for (int i = 0; i < sDownloadList.size(); i++) {
                if (sDownloadList.get(i).id == id) {
                    sDownloadList.remove(i);
                    return true;
                }
            }
            return false;
        }
    }

    public int delRecord(int id) {
        synchronized (DownloadItem.class) {
            delRecordFromCached(id);
            DownloadItem where = new DownloadItem();
            where.id = id;
            return delete(where);
        }
    }

    public int addRecord(DownloadItem item) {
        synchronized (DownloadDao.class) {
            DownloadItem existed = findRecord(item.filePath);
            if (existed == null) {
                item.id = generateId(true);
                item.priority = Priority.HIGH.getValue();
                item.currLen = 0L;
                item.totalLen = 0L;
                item.status = TaskStatus.waiting.getValue();
                item.startTime = sFormat.format(new Date());
                item.endTime = "0";
                long effect = insert(item);
                if (effect == -1) {
                    return -1;
                }
                sDownloadList.add(item);
                return item.id;
            }
            return -1;
        }
    }

    /**
     * 更新数据库下载记录
     */
    public int updateRecord(DownloadItem item) {
        DownloadItem where = new DownloadItem();
        where.id = item.id;
        int result;
        synchronized (DownloadDao.class) {
            result = update(item, where);
            if (result > 0) {
                if (item.status == TaskStatus.finish.getValue()) {
                    for (DownloadItem downloadItem : sDownloadList) {
                        if (downloadItem.id.intValue() == item.id) {
                            sDownloadList.remove(downloadItem);
                            break;
                        }
                    }
                } else {
                    int len = sDownloadList.size();
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            if (sDownloadList.get(i).id.intValue() == item.id) {
                                sDownloadList.set(i, item);
                                break;
                            }
                            if (i == sDownloadList.size() - 1) {
                                sDownloadList.add(item);
                            }
                        }
                    } else {
                        sDownloadList.add(item);
                    }
                }
            }
        }
        return result;
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
