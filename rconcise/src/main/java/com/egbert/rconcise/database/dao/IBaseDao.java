package com.egbert.rconcise.database.dao;

import java.util.List;

/**
 * Created by Egbert on 3/11/2019.
 */
public interface IBaseDao<T> {

    long insert(T entity);

    int delete(T where);

    int update(T entity, T where);

    List<T> query(T where);

    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);

    List<T> query(String sql);



}
