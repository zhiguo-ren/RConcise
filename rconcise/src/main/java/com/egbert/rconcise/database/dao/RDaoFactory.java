package com.egbert.rconcise.database.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * dao工程类，通过该类创建dao，该类为单例
 * Created by Egbert on 3/11/2019.
 */
public class RDaoFactory {
    public static final String DB_NAME = "RConcise.db";
    /**
     * 存放数据库的相对路径和对应的数据库操作类SQLiteDatabase实例
     */
    private HashMap<String, SQLiteDatabase> dbMap;

    private static volatile RDaoFactory sDaoFactory;

    private RDaoFactory() {
        dbMap = new HashMap<>();
    }

    /**
     * @param dbName 数据库名称
     * @return 是否成功开发或创建
     */
    public synchronized boolean openOrCreateDb(String dbName, Context context) {
        if (dbMap.get(dbName) != null) {
            return true;
        }
        SQLiteDatabase database = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        if (database != null) {
            dbMap.put(dbName, database);
            return true;
        }
        return false;
    }

    public static RDaoFactory getInst() {
        if (sDaoFactory == null) {
            synchronized (RDaoFactory.class) {
                if (sDaoFactory == null) {
                    sDaoFactory = new RDaoFactory();
                }
            }
        }
        return sDaoFactory;
    }

    /**
     * 获取数据库操作类dao
     * @param dao 要返回的dao的Class的实例
     * @param entity 对应的实体类的class对象
     * @param dbName 数据库名称
     * @param <T> 要产生的dao的类型
     * @param <K> 对应实体类的类型
     * @return dao实例
     */
    public synchronized <T extends BaseDao<K>, K> T getDao(Class<T> dao, Class<K> entity, String dbName) {
        T daoInst = null;
        SQLiteDatabase database = dbMap.get(dbName);
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
     * @param dbName 数据库名称
     */
    public void closeDb(String dbName) {
        SQLiteDatabase database = dbMap.get(dbName);
        if (database != null) {
            database.close();
            dbMap.remove(dbName);
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
