package com.example.gabriel.friendslist;

public class Users {

    public String userEmail, image, userName;

    public Users(){



    }

    public String getEmail() {
        return userEmail;
    }

    public void setEmail(String email) {
        this.userEmail = email;
    }

    public String getImage() {
        return image;
    }

    /*public void setImage(String image) {
        this.image = image;
    }*/

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public Users(String email, /*String image,*/ String username) {
        this.userEmail = email;
        //this.image = image;
        this.userName = username;
    }
}