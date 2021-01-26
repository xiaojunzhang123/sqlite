package com.zxj.sqllite.dao;

import java.util.List;

public interface IBaseDao<T> {

    long insert(T t);

    List<T> queryAll();

    List<T> query(T t);

    long delete(T t);

    long update(T t);
}
