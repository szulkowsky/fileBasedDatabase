package com;

public class Triangle {
    private float a;
    private float h;

    public Triangle(float aValue, float hValue){
        this.a = aValue;
        this.h = hValue;
    }

    public float getField(){
        return a*h/2;
    }

    public float getA(){
        return this.a;
    }

    public float getH(){
        return this.h;
    }

    public void setTriangle(float aVal, float hVal){
        this.a = aVal;
        this.h = hVal;
    }

    public byte[] getTriangleInBytes(){
        byte[] byteTab = new byte[8];

        int k = 0;
        byte[] aBytes = Convert.float2ByteArray(a);
        byte[] hBytes = Convert.float2ByteArray(h);

        for(int i = 0; i<4; i++){
            byteTab[k] = aBytes[i];
            k++;
        }
        for(int i = 0; i<4; i++){
            byteTab[k] = hBytes[i];
            k++;
        }

        // put some code here

        return byteTab;
    }

    public String toString(){
        return "( " + this.getA() + " , " + this.getH() + " ) { " + this.getField() + " }";
    }
}
