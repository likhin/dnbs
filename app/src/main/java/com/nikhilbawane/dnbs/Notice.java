package com.nikhilbawane.dnbs;

public class Notice {
    int id;
    String user;
    String title;
    String description;
    String tag;
    int priority;
    String date;

    Notice(int id, String user, String title, String description, String tag, int priority, String date) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.priority = priority;
        this.date = date;
    }

}
