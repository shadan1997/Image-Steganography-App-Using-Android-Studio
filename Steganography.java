package com.shadan.mcode.mypack;

import android.graphics.Bitmap;

public class Steganography {
    public String bit_msg;
    public Bitmap bitmap;

    public Steganography(String str,Bitmap b){
        this.bit_msg=str;
        this.bitmap=b;
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getMsg() {
        return bit_msg;
    }


    ///LSB modification algorithm
    public int valueofRGB(char ch,int r){
        switch(ch){
            case '0':
                //if(r==255) return 254;
                if( r%2 != 0) return 1-r;
                else return  r;
            case '1':
                if(r%2==0) return 1+r;
                else return r;
        }
        return r;
    }
}
