package com.egbert.rconcise.upload;

import com.egbert.rconcise.database.dao.BaseDao;
import com.egbert.rconcise.enums.Priority;
import com.egbert.rconcise.enums.TaskStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 上传任务记录的数据库操作类<br><br>
 * Created by Egbert on 4/24/2019.
 */
public class UploadDao extends BaseDao<UploadItem> {
    private static List<UploadItem> sUploadList = Collections.synchronizedList(new ArrayList<UploadItem>());
    private static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    @Override
    protected String createTable() {
        return "create table if not exists " + tableName + "(id INTEGER NOT NULL PRIMARY KEY,"
                + "url TEXT NOT NULL," + "file_path TEXT NOT NULL," + "file_name TEXT," + "status INTEGER,"
                + "curr_len INTEGER," + "total_len INTEGER," + "start_time VARCHAR(20)," + "end_time VARCHAR(20),"
                + "user_id TEXT," + "task_type VARCHAR(10)," + "priority INTEGER," + "stop_mode INTEGER,"
                + "unique(url, file_path)" + ")";
    }

    /**
     * 查找全部上传记录
     */
    public List<UploadItem> selectAllRecord() {
        synchronized (UploadDao.class) {
            UploadItem where = new UploadItem();
            return query(where);
        }
    }

    /**
     * 根据url和上传文件路径查找上传记录
     */
    public UploadItem findRecord(String url, String filePath) {
        synchronized (UploadDao.class) {
            for (UploadItem item : sUploadList) {
                if (item.url.equals(url) && item.filePath.equals(filePath)) {
                    return item;
                }
            }
            UploadItem where = new UploadItem();
            where.url = url;
            where.filePath = filePath;
            List<UploadItem> results = query(where);
            if (results == null || results.isEmpty()) {
                return null;
            }
            return results.get(0);
        }
    }

    /**
     * 根据id查找上传记录对象
     */
    public UploadItem findRecordById(int recordId) {
        synchronized (UploadDao.class) {
            for (UploadItem item : sUploadList) {
                if (item.id == recordId) {
                    return item;
                }
            }
            UploadItem where = new UploadItem();
            where.id = recordId;
            List<UploadItem> result = query(where);
            if (result == null || result.isEmpty()) {
                return null;
            }
            return result.get(0);
        }
    }

    /**
     * 根据id查找上传记录对象（只从缓存中查询）
     */
    public UploadItem findRecordByIdFromCached(int recordId) {
        synchronized (UploadDao.class) {
            for (UploadItem item : sUploadList) {
                if (item.id == recordId) {
                    return item;
                }
            }
            return null;
        }
    }

    /**
     * 根据id从缓存中移除上传记录
     */
    public boolean delRecordFromCached(int id) {
        synchronized (UploadItem.class) {
            for (int i = 0; i < sUploadList.size(); i++) {
                if (sUploadList.get(i).id == id) {
                    sUploadList.remove(i);
                    return true;
                }
            }
            return false;
        }
    }

    public int delRecord(int id) {
        synchronized (UploadItem.class) {
            delRecordFromCached(id);
            UploadItem where = new UploadItem();
            where.id = id;
            return delete(where);
        }
    }

    public int addRecord(UploadItem item) {
        synchronized (UploadDao.class) {
            UploadItem existed = findRecord(item.url, item.filePath);
            if (existed == null) {
                item.id = generateId(false);
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
                sUploadList.add(item);
                return item.id;
            }
            return -1;
        }
    }

    /**
     * 更新数据库上传记录
     */
    public int updateRecord(UploadItem item) {
        UploadItem where = new UploadItem();
        where.id = item.id;
        int result;
        synchronized (UploadDao.class) {
            result = update(item, where);
            if (result > 0) {
                if (item.status == TaskStatus.finish.getValue()) {
                    for (UploadItem uploadItem : sUploadList) {
                        if (uploadItem.id.intValue() == item.id) {
                            sUploadList.remove(uploadItem);
                            break;
                        }
                    }
                } else {
                    int len = sUploadList.size();
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            if (sUploadList.get(i).id.intValue() == item.id) {
                                sUploadList.set(i, item);
                                break;
                            }
                            if (i == sUploadList.size() - 1) {
                                sUploadList.add(item);
                            }
                        }
                    } else {
                        sUploadList.add(item);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<UploadItem> query(String sql) {
        return null;
    }
}
