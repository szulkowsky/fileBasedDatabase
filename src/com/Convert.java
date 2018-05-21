package com;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class Convert {

    public static byte [] float2ByteArray (float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static byte [] int2ByteArray (int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte [] long2ByteArray (long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static byte boolean2ByteArray (boolean value) {
        byte bool;
        if(value)
            bool = 1;
        else
            bool = 0;

        return bool;
    }

    public static float byteArray2Float (byte[] bytes){
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    public static int byteArray2Int (byte[] bytes){
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public static long byteArray2Long(byte[] bytes){
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    public static boolean byteArray2Boolean (byte bytes){
        if(bytes == 1)
            return true;
        else return false;

    }

    public static  long[] toLongListArray(List<Long> list){
        long[] ret = new long[list.size()];
        for(int i = 0;i < ret.length;i++)
            ret[i] = list.get(i);
        return ret;
    }
}
