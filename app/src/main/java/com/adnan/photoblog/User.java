package com.adnan.photoblog;

public class User {
public String image,name,token;

    public User() {
    }

    public User(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public User(String image, String name, String token) {
        this.image = image;
        this.name = name;
        this.token = token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
