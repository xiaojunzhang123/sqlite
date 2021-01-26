package com.zxj.sqllite.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zxj.sqllite.annotation.DBField;
import com.zxj.sqllite.annotation.DBTable;
import com.zxj.sqllite.dao.IBaseDao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseDao<T> implements IBaseDao<T> {

    private SQLiteDatabase sqLiteDatabase;
    private Map<String, Field> cacheMap;
    private String tableName;
    private Class clazz;

    public BaseDao(SQLiteDatabase sqLiteDatabase, Class<T> clazz) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.clazz = clazz;
        //创建表
        createTable(clazz);
        //缓存表中和clazz中都有的字段
        initCacheMap();
    }

    private void initCacheMap() {
        cacheMap = new HashMap<>();
        String sql = "select * from " + tableName + " limit 0";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        String[] columnNames = cursor.getColumnNames();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (String columnName : columnNames) {
            for (Field field : declaredFields) {
                DBField dbField = field.getAnnotation(DBField.class);
                if (dbField == null) {
                    continue;
                }
                String dbFieldName = dbField.value();
                if (columnName.equals(dbFieldName)) {
                    cacheMap.put(columnName, field);
                    break;
                }
            }
        }
    }

    @Override
    public long insert(T t) {
        Map<String, String> values = getValues(t);
        ContentValues contentValues = getContentValues(values);
        return sqLiteDatabase.insert(tableName, null, contentValues);
    }

    @Override
    public List<T> queryAll() {
        List<T> list = new ArrayList<>();
        String queryStr = "select * from " + tableName;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(queryStr, null);
            while (cursor.moveToNext()) {
                Iterator<String> iterator = cacheMap.keySet().iterator();
                T t = (T) clazz.newInstance();
                while (iterator.hasNext()) {
                    Object fieldDBValue = null;
                    String columnName = iterator.next();
                    int columnIndex = cursor.getColumnIndex(columnName);
                    int columnType = cursor.getType(columnIndex);
                    if (columnType == Cursor.FIELD_TYPE_NULL) {
                    } else if (columnType == Cursor.FIELD_TYPE_INTEGER) {
                        fieldDBValue = cursor.getInt(columnIndex);
                    } else if (columnType == Cursor.FIELD_TYPE_FLOAT) {
                        fieldDBValue = cursor.getFloat(columnIndex);
                    } else if (columnType == Cursor.FIELD_TYPE_STRING) {
                        fieldDBValue = cursor.getString(columnIndex);
                    } else if (columnType == Cursor.FIELD_TYPE_BLOB) {
                        fieldDBValue = cursor.getBlob(columnIndex);
                    }
                    Field field = cacheMap.get(columnName);
                    field.setAccessible(true);
                    field.set(t, fieldDBValue);
                }
                list.add(t);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<T> query(T t) {
        return null;
    }

    @Override
    public long delete(T t) {
        return 0;
    }

    @Override
    public long update(T t) {
        return 0;
    }

    /**
     * 创建表
     */
    private void createTable(Class clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create table if not exists ");
        DBTable dbTable = (DBTable) clazz.getAnnotation(DBTable.class);
        if (dbTable != null) {
            tableName = dbTable.value();
        }
        stringBuilder.append(tableName + "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,");
        //获取所有的成员变量属性
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            DBField dbField = field.getAnnotation(DBField.class);
            if (dbField == null) {
                continue;
            }
            stringBuilder.append(dbField.value());
            Class<?> fieldType = field.getType();
            if (fieldType == int.class) {
                stringBuilder.append(" INTEGER,");
            } else if (fieldType == String.class) {
                stringBuilder.append(" TEXT,");
            } else if (fieldType == boolean.class) {
                stringBuilder.append(" BOOLEAN,");
            } else if (fieldType == float.class) {
                stringBuilder.append(" FLOAT,");
            } else {
                throw new RuntimeException("sqlLite 不支持的数据类型");
            }
        }

        if (stringBuilder.toString().endsWith(",")) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append(")");
        String sqlString = stringBuilder.toString();
        try {
            sqLiteDatabase.execSQL(sqlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<String, String> getValues(T t) {
        Map<String, String> map = new HashMap<>();
        //获取所有的成员变量属性
        Iterator<Field> iterator = cacheMap.values().iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            field.setAccessible(true);
            Object object = null;
            try {
                object = field.get(t);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (object == null) {
                continue;
            }
            String fieldValue = object.toString();
            String dbFieldName = field.getAnnotation(DBField.class).value();
            if (!TextUtils.isEmpty(fieldValue) && !TextUtils.isEmpty(dbFieldName)) {
                map.put(dbFieldName, fieldValue);
            }
        }
        return map;
    }

    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            String value = map.get(key);
            if (value != null) {
                contentValues.put(key, value);
            }
        }
        return contentValues;
    }

}
