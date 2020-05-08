package com.example.mapp;

/* Simple class used for Classes in the schedule of a non admin user */
public class Classes {

    private String className;
    private String location;
    private String days;
    private String time;
    private boolean isPm;

    public Classes(String className, String location, String days, String time, boolean isPm){
        this.className = className;
        this.location = location;
        this.days = days;
        this.time = time;
        this.isPm = isPm;
    }

    /* GETTERS */
    public String getClassName() { return className; }
    public String getLocation() { return location; }
    public String getDays() { return days; }
    public String getTime() { return time; }
    public boolean isPm() { return isPm; }
}

