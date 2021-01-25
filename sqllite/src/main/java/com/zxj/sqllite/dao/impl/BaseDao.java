package com.zxj.sqllite.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zxj.sqllite.annotation.DBField;
import com.zxj.sqllite.annotation.DBTable;
import com.zxj.sqllite.dao.IBaseDao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseDao<T> implements IBaseDao<T> {

    private SQLiteDatabase sqLiteDatabase;
    private Map<String, String> cacheMap;
    private String tableName;
    private Class clazz;

    public BaseDao(SQLiteDatabase sqLiteDatabase, Class clazz) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.clazz = clazz;
        //创建表
        createTable(clazz);
        //获取表中的所有字段
        getFields(clazz);
    }

    @Override
    public long insert(T t) {
        ContentValues contentValues = getContentValues(t);
        return sqLiteDatabase.insert(tableName, null, contentValues);
    }

    @Override
    public List<T> queryAll() {
        List<T> list = new ArrayList<>();
        String queryStr = "select * from " + tableName;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(queryStr,null);
            Set<String> keySet = cacheMap.keySet();
            while (cursor.moveToNext()) {
                T t = (T) clazz.newInstance();
                for (String fieldName : keySet) {
                    String fieldDBName = cacheMap.get(fieldName);
                    int fieldType =  cursor.getType(cursor.getColumnIndex(fieldDBName));
                    Object fieldDBValue = null;
                    if (fieldType == Cursor.FIELD_TYPE_NULL){
                    }else if(fieldType == Cursor.FIELD_TYPE_INTEGER){
                        fieldDBValue = cursor.getInt(cursor.getColumnIndex(fieldDBName));
                    }else if(fieldType == Cursor.FIELD_TYPE_FLOAT){
                        fieldDBValue = cursor.getFloat(cursor.getColumnIndex(fieldDBName));
                    }else if(fieldType == Cursor.FIELD_TYPE_STRING){
                       fieldDBValue = cursor.getString(cursor.getColumnIndex(fieldDBName));
                    }else if(fieldType == Cursor.FIELD_TYPE_BLOB){
                        fieldDBValue = cursor.getBlob(cursor.getColumnIndex(fieldDBName));
                    }
                    Field field = t.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(t, fieldDBValue);
                }
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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
        stringBuilder.append(tableName + "(");
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
        sqLiteDatabase.execSQL(sqlString);
    }

    /**
     * 获取存储类中所有带注解的成员变量
     *
     * @return
     */
    private Map<String, String> getFields(Class clazz) {
        cacheMap = new HashMap<>();
        //获取所有的成员变量属性
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            DBField dbField = field.getAnnotation(DBField.class);
            if (dbField == null) {
                continue;
            }
            String fieldDBName = dbField.value();
            cacheMap.put(field.getName(), fieldDBName);
        }
        return cacheMap;
    }

    private ContentValues getContentValues(T t) {
        ContentValues contentValues = new ContentValues();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (cacheMap.containsKey(field.getName())) {
                String fieldDBName = cacheMap.get(field.getName());
                field.setAccessible(true);
                try {
                    String fieldValue = field.get(t).toString();
                    contentValues.put(fieldDBName, fieldValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return contentValues;
    }
}
