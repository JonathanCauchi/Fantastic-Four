package com.example.neil.googlemapsdemo;

/**
 * Created by Neil on 3/4/2018.
 */

public class User {

    public String username;
    public String email;
    public String name;
    public String password;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String username, String email,String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
    }

}
