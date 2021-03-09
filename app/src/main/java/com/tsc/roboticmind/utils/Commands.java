package com.tsc.roboticmind.utils;

public class Commands {

    /*
     * 0 - up
     * 1 - right
     * 2 - down
     * 3 - left
     * 4 - halt
     * 5 - set motor speed (150 - 255)
     * "L"+ any integer = rotate left by specified integer (in degrees) (eg, L90)
     * "R" + any integer = rotate right by specified integer (in degrees) (eg, R180)
     */

    public static final String MOVE_UP = "0";
    public static final String MOVE_RIGHT = "1";
    public static final String MOVE_DOWN = "2";
    public static final String MOVE_LEFT = "3";
    public static final String HALT = "4";

    public static final String SET_MSPEED = "5";
    public static final String MODE_MANUAL = "8";
    public static final String MODE_AUTO = "9";

}
