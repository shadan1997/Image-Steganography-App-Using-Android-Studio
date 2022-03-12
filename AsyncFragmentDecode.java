package com.shadan.mcode.mypack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;
import java.util.Iterator;

public class AsyncFragmentDecode extends Fragment {

    //Data
    public final int MAX=16;//1000 0000 0000 0000
    private TaskDecode taskDecode;
    private MyTaskHandler myTaskHandler;
    private Boolean Decrypt;
    private String KEY;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //interface
    public interface MyTaskHandler{
        void handleTaskUpdate(int value,int max);
        void handleTaskPre();
        void handleTaskPost(String string);
    }

    //Return the status of subclass taskDecode
    public AsyncTask.Status status(){
        if(taskDecode!=null){
            return taskDecode.getStatus();
        }
        return null;
    }

    //start my background task
    public void runTask(Bitmap bitmap,Boolean isDecryptionOn,String key){
        Decrypt=isDecryptionOn;
        KEY=key;
        taskDecode = new TaskDecode();
        taskDecode.execute(bitmap);

    }

    //background task cancel
    public void taskCancel(){
        if(taskDecode!=null && !taskDecode.isCancelled()){
            taskDecode.cancel(true);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MyTaskHandler){
            myTaskHandler=(MyTaskHandler) context;
        }
    }


    //retrieving text from a arr List
    private String  msg_retrieve(ArrayList<String> arrList){
        Log.d("Message retrieve","start");
        String msg="";
        Iterator iterator = arrList.iterator();

        try{
            if(arrList.isEmpty()){ Log.d("array", "empty"); }
            while(iterator.hasNext()){ msg+=(char) Integer.parseInt(iterator.next().toString(), 2); }
        }
        catch(Exception e){ Log.d("arrayException", e.toString()); }
        Log.d("msg_retrieve",msg);
        return msg;
    }


    /*
    *****************************************subclass***********************************
    */
    public class TaskDecode extends AsyncTask<Bitmap,Integer,String>{

        Bitmap mbitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(myTaskHandler!=null){
                myTaskHandler.handleTaskPre();
            }
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            Log.d("Decoding","start");

            ArrayList<String> bitList =new  ArrayList<>();
            String x="",s,bitmsg="";
            int w=0,TL,progress=0;
            mbitmap = bitmaps[0];

            //checking if task cancelled then stop
            if(taskDecode.isCancelled()){ return "Task Cancelled...!"; }

            try{
                for(int z=0;z<MAX;z++){
                    int y=mbitmap.getPixel(0,z);
                    int r = Color.red(y);
                    s= (r % 2 == 0) ? "0" : "1";
                    x+=s;
                }
                w=Integer.parseInt(x,2);
                Log.d("LEngth",""+w);

            }
            catch (Exception e){ return "MessageNotFound263"; }

            //checking if task cancelled then stop
            if(taskDecode.isCancelled()){ return "Task Cancelled...!"; }

            String rdata="";
            int read=0;
            TL=16+8*w;
            try{
                for(int i=0; i<mbitmap.getWidth(); i++){
                    for(int j=0; j<mbitmap.getHeight(); j++) {
                        if(read<TL){
                            int p = mbitmap.getPixel(i, j);
                            int r = Color.red(p);
                            //Log.d("rvalue", "" + r);
                            rdata = (r % 2 == 0) ? "0" : "1";
                            bitmsg += rdata;
                        }
                        read++;
                        progress++;
                        //checking if task cancelled then stop
                        if(taskDecode.isCancelled()){ return "Task Cancelled...!"; }
                    }
                    publishProgress(progress);
                }
            }
            catch (Exception e){return "MessageNotFound283"; }

            //checking if task cancelled then stop
            if(taskDecode.isCancelled()){ return "Task Cancelled...!"; }
            //Extracting image Length
            try{
                int Length_of_data=Integer.parseInt(bitmsg.substring(0,16),2);
                Log.d("length of data",""+Length_of_data);
                Log.d("Bit",bitmsg);
                bitmsg=bitmsg.substring(16,16+(Length_of_data*8));
            }
            catch (Exception io){ return "MessageNotFound293"; }

            //checking if task cancelled then stop
            if(taskDecode.isCancelled()){ return "Task Cancelled...!"; }
            Log.d("length","");

            int length=bitmsg.length();
            int l=0;
            try{
                while(l<length){
                    bitList.add(bitmsg.substring(l,l+8));
                    l=l+8;
                }
            }
            catch(Exception e){ return "MessageNotFound308"; }
            //checking if task cancelled then stop
            if(taskDecode.isCancelled()){ return "Task Cancelled...!"; }
            String msg="";
            try{
                msg=msg_retrieve(bitList);
                Log.d("Decodefun","decoded:->"+msg);
                //checking if task cancelled then stop
                if(taskDecode.isCancelled()){ return "Task Cancelled...!"; }

                if(Decrypt){///if Decryption on
                    AES advanced_encryption_standard = new AES();
                    String decryptMessage=advanced_encryption_standard.decrypt(msg,KEY);
                    if(decryptMessage==null){
                        return "Key Not MAtched";
                    }
                    return decryptMessage;
                }
            }
            catch (Exception e){ return "MessageNotFound319"; }
            return msg;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int max=mbitmap.getHeight()*mbitmap.getWidth();
            if(myTaskHandler!=null){
                myTaskHandler.handleTaskUpdate(values[0],max);
            }
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            try{
                if(myTaskHandler!=null){
                    myTaskHandler.handleTaskPost(string);
                }
            }catch(Exception e){
                Log.e("OnPostExecute",e.toString());
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }
}
