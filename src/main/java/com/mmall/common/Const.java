package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by lyz on 6/7/18.
 */
public class Const {
    //为什么不放入用户名，考虑到多个用户登录，从session中取出用户冲突问题？
    //每个用户使用的是不同的session吗？登录用户都会有自己的session，并不是共用session
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface ProductListOrderBy{
        Set<String> PRICE_DESC_ASC = Sets.newHashSet("price_desc","price_asc");
    }
    //不用枚举而实现分组的功能
    public interface Role{
        int ROLE_CUSTOMER = 0;//普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    public enum ProductStatusEnum {
        //枚举类规定只有的实例变量
        ON_SALE("在线",1);

        private String value;
        private int code;

        ProductStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
