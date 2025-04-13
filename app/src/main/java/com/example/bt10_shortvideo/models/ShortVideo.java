package com.example.bt10_shortvideo.models;

public class ShortVideo {
    private int id;
    private String email;
    private String title;
    private String desc;
    private String url;
    private long favNum;
    private long notFavNum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getFavNum() {
        return favNum;
    }

    public void setFavNum(long favNum) {
        this.favNum = favNum;
    }

    public long getNotFavNum() {
        return notFavNum;
    }

    public void setNotFavNum(long notFavNum) {
        this.notFavNum = notFavNum;
    }

    public ShortVideo() {
    }

    public ShortVideo(int id, String email, String title, String desc, String url, long favNum, long notFavNum) {
        this.id = id;
        this.email = email;
        this.title = title;
        this.desc = desc;
        this.url = url;
        this.favNum = favNum;
        this.notFavNum = notFavNum;
    }

    public void increaseFavNum() {
        this.favNum ++;
    }
    public void increaseNotFavNum() {
        this.notFavNum ++;
    }

    public void decreaseFavNum() {
        this.favNum --;
    }
    public void decreaseNotFavNum() {
        this.notFavNum --;
    }
}
