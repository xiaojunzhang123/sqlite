package com.zxj.database;


import com.zxj.sqllite.annotation.DBField;
import com.zxj.sqllite.annotation.DBTable;

@DBTable("user")
public class User {

    @DBField("name")
    private String userName;
    @DBField("age")
    private int age;

    private String Sex;

    public User(){
    }

    public User(String userName, int age) {
        this.userName = userName;
        this.age = age;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", age=" + age +
                ", Sex='" + Sex + '\'' +
                '}';
    }
}
