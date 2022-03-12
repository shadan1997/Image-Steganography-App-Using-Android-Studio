package com.shadan.mcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.shadan.mcode.mypack.AsyncFragmentDecode;

public class DecodeActivity extends AppCompatActivity implements AsyncFragmentDecode.MyTaskHandler {

    /*Global Variables*/

    /*Constant*/
    //Requesting image from file chooser
    private static final int SELECT_PICTURE = 101; //request
    //Fragment TAg
    private static final String FRAGMENT_TAG="FRAGMENT_TAG";

    /*Boolean */
    //image loaded on image view?
    private boolean hasImage=false;
    //Set true if Decryption is on
    private boolean Decrypt=false;
    //key with default value KEY
    public String KEY="KEY";

    //XML Components
    public ImageView mImageView2 ;
    public TextView mTextView;
    public FloatingActionButton mdecodebtn,mloadbtn;
    public BottomAppBar mbottomAppBarfordecode;
    public ProgressBar mProgressBar_decode;
    public ProgressBar mdcpbar;
    public ConstraintLayout mConstraint_Layout_Decode;


    //Other Objects
    public Bitmap bitmap;
    //saving current uri of image
    public Uri targetUri=null;


    //accessing setting parameters
    SharedPreferences sharedPreferences;
    //fragment for running activity
    AsyncFragmentDecode fragment;


    //functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //getting xml objects reference
        initView();

        //attaching a fragment on activity
        FragmentManager manager=getSupportFragmentManager();
        fragment = (AsyncFragmentDecode) manager.findFragmentByTag(FRAGMENT_TAG);

        //Creating a fragment
        if(fragment==null){
            fragment = new AsyncFragmentDecode();
            manager.beginTransaction().add(fragment,FRAGMENT_TAG).commit();
        }

        //declaring work for toolbar menu
        mbottomAppBarfordecode =findViewById(R.id.d_bar);
        mbottomAppBarfordecode.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.menu_back :
                        onBackPressed();
                        break;
                    case R.id.menu_clear :
                        onReset();
                        break;
                    case R.id.menu_settings:
                        onSettings();
                        break;
                    case R.id.menu_key:
                        onKey();
                        break;
                    case R.id.menu_info:
                       onInfo();
                        break;
                    case R.id.menu_cancle:
                        onCancel();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }

                return false;
            }
        });
        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setSharedPreferences();
    }

    //initialization
    private void initView() {
        mTextView =findViewById(R.id.mTextview);
        mImageView2 =findViewById(R.id.image_view2);
        mdecodebtn =findViewById(R.id.decode_btn);
        mloadbtn =findViewById(R.id.select_btn);
        mProgressBar_decode =findViewById(R.id.decode_progressBar);
        mConstraint_Layout_Decode=findViewById(R.id.constraint_layout_decode);
        mdcpbar=findViewById(R.id.dcpBar);
    }


    //refreshing shared preference objects values for this activity
    private void setSharedPreferences(){
        KEY=sharedPreferences.getString("Key","KEY");
        Decrypt=sharedPreferences.getBoolean("Encrption_enable",false);
    }


    //On menu<-key press
    private void onKey() {
        Snackbar.make(mConstraint_Layout_Decode,"Current Key is '"+KEY+"'",Snackbar.LENGTH_LONG).setAction("Change Key", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettings();
            }
        }).show();
    }

    //On menu<-setting press
    private void onSettings() {
        //DecodeActivity to SettingActivity
        Intent encodingActivity = new Intent(DecodeActivity.this, SettingsActivity.class);
        startActivity(encodingActivity);
    }



    //back operation
    @Override
    public void onBackPressed() { if(fragment.status()==AsyncTask.Status.RUNNING){
        Snackbar.make(mConstraint_Layout_Decode,"Are you sure want to exit?", Snackbar.LENGTH_LONG).setAction("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DecodeActivity.super.onBackPressed();
            }
        }).show();
    }else
        super.onBackPressed();
    }


    //information about us
    public void onInfo(){
        new MaterialAlertDialogBuilder(this).setTitle("Steganography").setMessage("This application is created for educational purpose by shadan and shaheen.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //doing nothing.
                    }
                })
                .show();
    }

    //save data
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("call","OnSaveInstanceState");
        try{
            outState.putBoolean("hasImage",hasImage);
            outState.putString("My_Message",mTextView.getText().toString());
            outState.putParcelable("Uri",targetUri);
        }
        catch (Exception e){Log.d("DecodeActivity","onSaveInstantState"); }
    }

    //get save data
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("call","OnInstanceState");
       try{
          if(savedInstanceState!=null){
              hasImage=savedInstanceState.getBoolean("hasImage");
              mTextView.setText(savedInstanceState.getString("My_Message"));
              targetUri =savedInstanceState.getParcelable("Uri");
              mImageView2.setImageURI(targetUri);
          }
       }
       catch (Exception e){ Log.d("DecodeActivity","onRestoreInstantState"); }
    }

    //Reset all UI elements and cancle backgroundTask
    protected void onReset() {
        //flags
        hasImage=false;
        //Data
        targetUri=null;
        mImageView2.setImageBitmap(null);
        mImageView2.setImageDrawable(null);
        mImageView2.setImageResource(R.drawable.ic_image_black_24dp);
        mTextView.setText(null);
        mProgressBar_decode.setVisibility(View.GONE);
        mdcpbar.setVisibility(View.GONE);
        fragment.taskCancel();
    }


    //for select an image from gallery
    public void loadfun(View view) {
        //creating a chooser
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/Stegnography/");
        intent.setDataAndType(uri,"image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    //upload  a image from photos --- here creating thread
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SELECT_PICTURE:
                if(resultCode!=RESULT_OK) return;
                final Uri uri= data.getData();
                targetUri=uri;
                mImageView2.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageView2.setImageURI(uri);
                    }
                });
                hasImage=true;
                Log.d("Image",uri.toString());
                break;
            default:
                break;
        }
    }


    //initial step for decode
    public void decodefun(View view) {
       if(hasImage){
           BitmapDrawable bitmapDrawable;
           bitmapDrawable=(BitmapDrawable) mImageView2.getDrawable();
           bitmap = bitmapDrawable.getBitmap();
          try{
             fragment.runTask(bitmap,Decrypt,KEY);
            }
          catch (Exception e){Log.d("DecodeException", e.toString()); }
       }
       else{ toastMe("Select an Image"); }
    }

    //used to set the UI text View
    public void show_msg(String msg){
        Log.d("show Message","start");
        mTextView.setText(msg);
    }

    //toast generator
    public void toastMe(String msg){ Toast.makeText(this,msg, Toast.LENGTH_SHORT).show(); }

    //snackbar generator
    public void snackBar(String msg) {   Snackbar.make(mConstraint_Layout_Decode, msg, Snackbar.LENGTH_LONG).show(); }


    @Override
    protected void onResume() {
        super.onResume();
        setSharedPreferences();
    }


    //  for cancel Background Task...
    public void onCancel(){
        fragment.taskCancel();
        if(mProgressBar_decode.getVisibility()==View.VISIBLE){
            mProgressBar_decode.setVisibility(View.INVISIBLE);
            mdcpbar.setVisibility(View.GONE);
        }
    }

    /*
     *******************************Fragment functions override****************************************************
     */

    @Override
    public void handleTaskUpdate(int value, int max) {
        if(mProgressBar_decode.getVisibility()==View.INVISIBLE) {
            mProgressBar_decode.setVisibility(View.VISIBLE);
            mdcpbar.setVisibility(View.VISIBLE);
        }
        mProgressBar_decode.setMax(max);
        mProgressBar_decode.setProgress(value);
    }

    @Override
    public void handleTaskPre() {
        mProgressBar_decode.setVisibility(View.VISIBLE);
        mdcpbar.setVisibility(View.VISIBLE);
        mProgressBar_decode.setProgress(0);
    }

    @Override
    public void handleTaskPost(String string) {
        mProgressBar_decode.setVisibility(View.INVISIBLE);
        mdcpbar.setVisibility(View.INVISIBLE);
        show_msg(string);
        toastMe("Done..!");
    }
}
