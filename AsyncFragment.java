package com.shadan.mcode.mypack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AsyncFragment extends Fragment {

    //Data
    private TaskEncode taskEncode;
    private MyTaskHandler myTaskHandler;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    //interface
    public interface MyTaskHandler{
        void handleTaskUpdate(int value,int max);
        void handleTaskPre();
        void handleTaskPost(Bitmap bitmap);
    }

    //Return the status of subclass taskEncode
    public AsyncTask.Status status(){
       if(taskEncode!=null){
           return taskEncode.getStatus();
       }
        return null;
    }

    //start my background task
    public void runTask(Steganography steganography){
        taskEncode = new TaskEncode();
        taskEncode.execute(steganography);
    }

    //background task cancel
    public void taskCancel(){
        if(taskEncode!=null && !taskEncode.isCancelled()){
            taskEncode.cancel(true);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MyTaskHandler){
            myTaskHandler=(MyTaskHandler) context;
        }
    }


    /*
     *****************************************subclass***********************************
     */

    //subclass --------------obj pass on excute ,progress ,result in
    class TaskEncode extends AsyncTask<Steganography,Integer, Bitmap> {
        Bitmap mbitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(myTaskHandler!=null){
                myTaskHandler.handleTaskPre();
            }
        }
        @Override
        protected Bitmap doInBackground(Steganography... steganographies) {

            if(taskEncode.isCancelled()){ return null; }
            Steganography steganography = steganographies[0];
            mbitmap=steganography.getBitmap();
            String bitstring=steganography.getMsg();

            Bitmap operation = Bitmap.createBitmap(mbitmap.getWidth(),mbitmap.getHeight(), mbitmap.getConfig());

            int length=bitstring.length();//24 + 16
            int pointer=0;
            int progress=0;

            for(int i=0; i<mbitmap.getWidth(); i++){
                for(int j=0; j<mbitmap.getHeight(); j++){
                    int p = mbitmap.getPixel(i, j);
                    int r = Color.red(p);
                    int valueofR;
                    //////for red color
                    if(pointer<length){
                        char ch = bitstring.charAt(pointer);
                        valueofR=steganography.valueofRGB(ch,r);
                        if(taskEncode.isCancelled()){ return null; }
                        pointer++;
                        //Log.d("value","bit"+ch+"old-r"+r+"valueOFR"+valueofR);
                    }else {
                        valueofR=r;
                    }
                    operation.setPixel(i, j, Color.argb(Color.alpha(p), valueofR, Color.green(p), Color.blue(p)));
                    progress++;
                }
                publishProgress(progress);
            }
            if(taskEncode.isCancelled()){ return null; }
            Log.d("Operation Done!.","in Encode");
            return operation;
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
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            try{
               if(myTaskHandler!=null){
                   myTaskHandler.handleTaskPost(bitmap);
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
