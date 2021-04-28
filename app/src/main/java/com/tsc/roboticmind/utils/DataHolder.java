package com.tsc.roboticmind.utils;

import com.tsc.roboticmind.core.Task;

public class DataHolder {

    private static DataHolder dh;
    private Task t;

    private DataHolder() {}

    public static DataHolder getInstance() {
        if(dh == null)
            dh = new DataHolder();
        return dh;
    }

    public Task getTask() {
        return t;
    }

    public void setTask(Task t) {
        this.t = t;
    }
}
