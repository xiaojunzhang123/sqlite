package com.zxj.sqllite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zxj.sqllite.dao.impl.BaseDao;

import java.io.File;

public class DataBaseFactory {

    private static volatile DataBaseFactory dataBaseFactory;
    private String dataBasePath;
    private Context context;

    private DataBaseFactory(Context context) {
        //data/data/com.zxj.database/database/database.db
        this.context = context;
    }

    public static DataBaseFactory getInstance(Context context) {
        if (dataBaseFactory == null) {
            synchronized (DataBaseFactory.class) {
                if (dataBaseFactory == null) {
                    dataBaseFactory = new DataBaseFactory(context);
                }
            }
        }
        return dataBaseFactory;
    }

    public DataBaseFactory setDataBasePath(String dataPath) {
        dataBasePath = context.getDatabasePath(dataPath) + "/data.db";
        File dataFile = new File(dataBasePath);
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdir();
        }
        return dataBaseFactory;
    }

    public <T> BaseDao<T> getBaseDao(Class clazz) {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dataBasePath, null);
        return new BaseDao<T>(sqLiteDatabase, clazz);
    }
}
