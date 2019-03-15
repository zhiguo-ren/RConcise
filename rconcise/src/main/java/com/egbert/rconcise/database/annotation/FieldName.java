package com.egbert.rconcise.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表字段注解，注解的值为表的字段名，不设置默认为类属性名称
 * Created by Egbert on 3/11/2019.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldName {
    String value();
}
