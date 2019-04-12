package com.egbert.rconcise.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.egbert.rconcise.database.annotation.Entity;
import com.egbert.rconcise.database.annotation.FieldName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Egbert on 3/11/2019.
 */
public abstract class BaseDao<T> implements IBaseDao<T> {

    protected SQLiteDatabase database;

    private volatile boolean isInit;

    private Class<T> entityClass;

    private HashMap<String, Field> cacheMap;

    public String tableName;

    protected synchronized boolean init(Class<T> entity, SQLiteDatabase database) {
        if (!isInit) {
            entityClass = entity;
            this.database = database;
            if (entity.getAnnotation(Entity.class) == null) {
                tableName = entity.getSimpleName();
            } else {
                tableName = entity.getAnnotation(Entity.class).value();
            }

            if (!database.isOpen()) {
                return false;
            }
            if (!TextUtils.isEmpty(createTable())) {
                database.execSQL(createTable());
            }
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;
        }
        return isInit;
    }

    /**
     * 维护映射关系
     */
    private void initCacheMap() {
        String sql = "select * from " + this.tableName + " limit 1, 0";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
            String[] columnNames = cursor.getColumnNames();
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }
            for (String columnName : columnNames) {
                Field colField = null;
                for (Field field : fields) {
                    String tmpName;
                    field.setAccessible(true);
                    if (field.getAnnotation(FieldName.class) != null) {
                        tmpName = field.getAnnotation(FieldName.class).value();
                    } else {
                        tmpName = field.getName();
                    }
                    if (columnName.equals(tmpName)) {
                        colField = field;
                        break;
                    }
                }
                if (colField != null) {
                    cacheMap.put(columnName, colField);
                }

            }
        } catch (Exception e) {
            Log.e(BaseDao.class.getSimpleName(), Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private ContentValues getContentValues(T entity) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Field> entry : cacheMap.entrySet()) {
            if (entry.getValue() != null) {
                String value = null;
                try {
                    Object obj = entry.getValue().get(entity);
                    if (obj != null) {
                        value = obj.toString();
                    }
                } catch (IllegalAccessException e) {
                    Log.e(BaseDao.class.getSimpleName(), Log.getStackTraceString(e));
                }
                if (value != null) {
                    values.put(entry.getKey(), value);
                }
            }
        }
        return values;
    }

    private HashMap<String, String> getValues(T entity) {
        HashMap<String, String> values = new HashMap<>();
        for (Map.Entry<String, Field> entry : cacheMap.entrySet()) {
            if (entry.getValue() != null) {
                String value = null;
                try {
                    Object obj = entry.getValue().get(entity);
                    if (obj != null) {
                        value = obj.toString();
                    }
                } catch (IllegalAccessException e) {
                    Log.e(BaseDao.class.getSimpleName(), Log.getStackTraceString(e));
                }
                if (value != null) {
                    values.put(entry.getKey(), value);
                }
            }
        }
        return values;
    }

    private ArrayList<T> getResult(Cursor cursor, T entity) {
        ArrayList<T> results = new ArrayList<>();
        T item;
        try {
            while (cursor.moveToNext()) {
                item = (T) entity.getClass().newInstance();
                for (Map.Entry<String, Field> entry : cacheMap.entrySet()) {
                    int index = cursor.getColumnIndex(entry.getKey());
                    Field field = entry.getValue();
                    Class<?> type = field.getType();
                    if (type == String.class) {
                        field.set(item, cursor.getString(index));
                    } else if (type == Integer.class) {
                        field.set(item, cursor.getInt(index));
                    } else if (type == Long.class) {
                        field.set(item, cursor.getLong(index));
                    } else if (type == Double.class) {
                        field.set(item, cursor.getDouble(index));
                    } else if (type == byte[].class) {
                        field.set(item, cursor.getBlob(index));
                    }
                }
                results.add(item);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return results;
    }

    @Override
    public long insert(T entity) {
        return database.insert(tableName, null, getContentValues(entity));
    }

    @Override
    public int delete(T where) {
        Map<String, String> map = getValues(where);
        Condition condition = new Condition(map);
        return database.delete(tableName, condition.getWhereClause(), condition.getWhereArgs());
    }

    @Override
    public int update(T entity, T where) {
        Condition condition = new Condition(getValues(where));
        return database.update(tableName, getContentValues(entity),
                condition.getWhereClause(), condition.getWhereArgs());
    }

    @Override
    public List<T> query(T where) {
        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        String limitClause = null;
        if (startIndex != null && limit != null) {
            limitClause = startIndex + "," + limit;
        }
        Condition condi = new Condition(getValues(where));
        Cursor cursor = database.query(tableName, null, condi.getWhereClause(), condi.getWhereArgs(),
                null, null, orderBy, limitClause);
        if (cursor != null) {
            return getResult(cursor, where);
        }
        return null;
    }

    /**
     * 创建表
     */
    protected abstract String createTable();

    class Condition {
        /**
         * 查询条件
         */
        private String whereClause;
        /**
         * 条件的值
         */
        private String[] whereArgs;

        Condition(Map<String, String> map) {
            if (map != null && map.size() > 0) {
                ArrayList<String> args = new ArrayList<>();
                StringBuilder builder = new StringBuilder();
                builder.append(" 1=1");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (entry.getValue() != null) {
                        builder.append(" and ").append(entry.getKey()).append("=?");
                        args.add(entry.getValue());
                    }
                }
                whereClause = builder.toString();
                args.toArray(whereArgs = new String[args.size()]);
            }
        }

        public String getWhereClause() {
            return whereClause;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }
    }
}
