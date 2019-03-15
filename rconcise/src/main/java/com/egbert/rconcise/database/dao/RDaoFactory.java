package com.egbert.rconcise.database.dao;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * dao工程类，通过来给创建dao，该类为单例
 * Created by Egbert on 3/11/2019.
 */
public class RDaoFactory {

    private String databasePath;
    private SQLiteDatabase database;
    /**
     * 存放数据库的相对路径和对应的数据库操作类SQLiteDatabase实例
     */
    private HashMap<String, SQLiteDatabase> dbMap;

    private static RDaoFactory sDaoFactory;

    private RDaoFactory() {
        dbMap = new HashMap<>();
    }

    /**
     * @param relativePath 数据库相对路劲
     * @return 是否成功开发或创建
     */
    public boolean openOrCreateDb(String relativePath) {
        if (dbMap.get(relativePath) != null) {
            return true;
        }
        String databasePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + relativePath;
        File file = new File(databasePath.substring(0, databasePath.lastIndexOf('/')));
        if (!file.exists()) {
            file.mkdirs();
        }
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
        if (database != null) {
            dbMap.put(relativePath, database);
            return true;
        }
        return false;
    }

    public static RDaoFactory getInst() {
        if (sDaoFactory == null) {
            sDaoFactory = new RDaoFactory();
        }
        return sDaoFactory;
    }

    /**
     * 获取数据库操作类dao
     * @param dao 要返回的dao的Class的实例
     * @param entity 对应的实体类的class对象
     * @param relativePath 数据库相对路劲（相对于设备存储卡根路径）
     * @param <T> 要产生的dao的类型
     * @param <K> 对应实体类的类型
     * @return dao实例
     */
    public synchronized <T extends BaseDao<K>, K> T getDao(Class<T> dao, Class<K> entity, String relativePath) {
        T daoInst = null;
        SQLiteDatabase database = dbMap.get(relativePath);
        if (database == null) {
            throw new NullPointerException("SQLiteDatabase is null, going to call openOrCreateDb() first");
        }
        try {
            daoInst = dao.newInstance();
            daoInst.init(entity, database);
        } catch (IllegalAccessException e) {
            Log.e(RDaoFactory.class.getSimpleName(), Log.getStackTraceString(e));
        } catch (InstantiationException e) {
            Log.e(RDaoFactory.class.getSimpleName(), Log.getStackTraceString(e));
        }
        return daoInst;
    }

    /**
     * 关闭指定的数据库连接
     * @param relativePath 相对路劲为数据库链接在map中的key，通过key找到对应的数据库连接
     */
    public void closeDb(String relativePath) {
        SQLiteDatabase database = dbMap.get(relativePath);
        if (database != null) {
            database.close();
            dbMap.remove(relativePath);
        }
    }

    /**
     * 关闭所有数据库连接
     */
    public void closeAllDb() {
        for (Map.Entry<String, SQLiteDatabase> entry : dbMap.entrySet()) {
            SQLiteDatabase database = entry.getValue();
            if (database != null) {
                database.close();
            }
        }
        dbMap.clear();
    }

}
