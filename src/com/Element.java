package com;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Element {
    private long key;
    private Triangle data;
    private int overflowPageNo;

    public Element(){
        this.key = -1;
        this.data = new Triangle(0,0);
        this.overflowPageNo = -1;
    }

    public Element(long keyValue){
        this.key = keyValue;
        this.data = new Triangle(0,0);
        this.overflowPageNo = -1;
    }

    public Element(long keyValue, float aValue, float hValue){
        this.key = keyValue;
        this.data = new Triangle(aValue,hValue);
        this.overflowPageNo = -1;
    }

    public Element(long keyValue, float aValue, float hValue, int overflowPageNoValue){
        this.key = keyValue;
        this.data = new Triangle(aValue,hValue);
        this.overflowPageNo = overflowPageNoValue;
    }

    public Element(boolean random){
        if(random){
            float maxA = 1000;
            float maxH = 1000;
            long maxKey = Long.MAX_VALUE;
            long minKey = 1;

            Random r = new Random();
            this.key = ThreadLocalRandom.current().nextLong(minKey, maxKey);
            this.data = new Triangle(r.nextFloat() * (maxA),r.nextFloat() * (maxH));
            this.overflowPageNo = -1;
        }
        else{
            this.key = -1;
            this.data = new Triangle(0,0);
            this.overflowPageNo = -1;
        }
    }

    // setters

    public void setKey(long keyValue){
        this.key = keyValue;
    }
    public void setData(float aValue, float hValue){
        this.data.setTriangle(aValue, hValue);
    }
    public void setOverflowPageNo(int overflowPageNoValue){
        this.overflowPageNo = overflowPageNoValue;
    }

    // getters
    public long getKey(){
        return this.key;
    }

    public Triangle getData(){
        return this.data;
    }

    public int getOverflowPageNo(){
        return this.overflowPageNo;
    }

    public byte[] getElementInBytes(){
        byte[] byteTab = new byte[20];

        byte[] longBytes = Convert.long2ByteArray(key);
        byte[] dataBytes = data.getTriangleInBytes();
        byte[] ofBytes = Convert.int2ByteArray(overflowPageNo);

        int k = 0;

        for(int i = 0; i<8; i++){
            byteTab[k] = longBytes[i];
            k++;
        }
        for(int i = 0; i<8; i++){
            byteTab[k] = dataBytes[i];
            k++;
        }
        for(int i = 0; i<4; i++){
            byteTab[k] = ofBytes[i];
            k++;
        }

        return byteTab;
    }

    public void printElement(){
        System.out.println("[ " + this.getKey() + " ] " + this.getData().toString() + " " + this.getOverflowPageNo());
    }
}
