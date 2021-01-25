package com.zxj.database;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.zxj.sqllite.DataBaseFactory;
import com.zxj.sqllite.dao.impl.BaseDao;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseDao<User> userBaseDao = DataBaseFactory.getInstance(this).setDataBasePath("zhangxiaojun").getBaseDao(User.class);
        userBaseDao.insert(new User("zhangsan",20));
        List<User> list = userBaseDao.queryAll();
        Log.d("========>",list.toString()) ;
    }
}