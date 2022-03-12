package com.shadan.mcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import android.Manifest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import com.shadan.mcode.mypack.AsyncFragment;
import com.shadan.mcode.mypack.Steganography;
import com.shadan.mcode.mypack.AES;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EncodeActivity extends AppCompatActivity implements AsyncFragment.MyTaskHandler {

    /*Global Variables*/


    //XML Components
    public ImageView mImageView;
    public EditText mEdittext;
    public FloatingActionButton mEncodebtn,mUploadbtn;
    public ConstraintLayout mConstraint_Layout_Encode;
    public ProgressBar mProgressBar;
    public ProgressBar mecpbar;
    public BottomAppBar mbottomAppBar ;


    /*boolean variables*/

    //image loaded on image view?
    private boolean hasImage=false;
    //used for check the sizeofimage,data,and max capacity true if every thing is fine
    private boolean isConvertable=false;
    //Set true if Encryption is on
    private boolean Encrypt=false;
    //key with default value KEY
    private String KEY="KEY";

    //constant
    public final int Max=16;//1000 0000 0000 0000

    /*request code*/
    //Requesting for camaera permission
    private static final int PERMISSION_REQUEST_CODE=100;
    //Requesting image from file chooser
    private static final int SELECT_PICTURE = 101;
    //Requesting image from camera
    private static final int REQUEST_IMAGE_CAPTURE = 102; //request code
    //Fragment TAg
    private static final String FRAGMENT_TAG="FRAGMENT_TAG";


    //other objects ##
    public Bitmap mbitmap;
    //saving current uri of image
    public Uri targetUri=null;

    //store path of image form camera
    String currentPhotoPath=null;
    //accessing setting parameters
    SharedPreferences sharedPreferences;
    //fragment for running activity
    AsyncFragment fragment;

    //functions
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getting xml objects reference
        initView();

        //attaching a fragment on activity
        FragmentManager manager=getSupportFragmentManager();
        fragment = (AsyncFragment) manager.findFragmentByTag(FRAGMENT_TAG);

        //Creating a fragment
        if(fragment==null){
            fragment = new AsyncFragment();
            manager.beginTransaction().add(fragment,FRAGMENT_TAG).commit();
        }

        //declaring work for toolbar menu
        mbottomAppBar =findViewById(R.id.e_bar);
        mbottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_send :
                        shareImage(targetUri);
                        break;
                    case R.id.menu_back :
                        onBackPressed();
                        break;
                    case R.id.menu_clear :
                        onReset();
                        break;
                    case R.id.menu_camera:
                        dispatchTakePictureIntent();
                        break;
                    case R.id.menu_settings:
                        onSettings();
                        break;
                    case R.id.menu_key:
                        onKey();
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

    //initializingView
    private void initView(){
        mImageView = findViewById(R.id.image_view);
        mEncodebtn =findViewById(R.id.btn_encode);
        mUploadbtn =findViewById(R.id.btn_upload);
        mEdittext = findViewById(R.id.mEditText);
        mProgressBar=findViewById(R.id.encode_progressBar);
        mConstraint_Layout_Encode=findViewById(R.id.constraint_layout_encode);
        mecpbar=findViewById(R.id.ecpBar);
    }

    //refreshing shared preference objects values for this activity
    private void setSharedPreferences(){
        KEY=sharedPreferences.getString("Key","KEY");
        Encrypt=sharedPreferences.getBoolean("Encrption_enable",false);
    }

    //On menu<-key press
    private void onKey() {
        //attcahing a snackBar with yes action
        Snackbar.make(mConstraint_Layout_Encode,"Current Key is '"+KEY+"'",Snackbar.LENGTH_LONG).setAction("Change Key", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //calling setting activity
                onSettings();
            }
        }).show();
    }

    //On menu<-setting press
    private void onSettings() {
        //EncodeActivity to SettingActivity
        Intent encodingActivity = new Intent(EncodeActivity.this, SettingsActivity.class);
        startActivity(encodingActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refreshing shared preference objects values for this activity
        setSharedPreferences();
    }

    @Override
    public void onBackPressed() {
        //if thread is running -> info
        if(fragment.status()==AsyncTask.Status.RUNNING){
            Snackbar.make(mConstraint_Layout_Encode,"Task is running in background still want to exit?", Snackbar.LENGTH_LONG).setAction("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EncodeActivity.super.onBackPressed();
                }
            }).show();
        }else{
            super.onBackPressed();
        }
    }

    //save data in bundle on activity pause
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        try{
            outState.putBoolean("hasImage",hasImage);
            outState.putBoolean("isConvertable",isConvertable);
            outState.putString("My_Message",mEdittext.getText().toString());
            outState.putParcelable("Uri",targetUri);
        }
        catch (Exception e){
            Log.d("EncodeActivityException","onSaveInstantState");
        }
    }

    //Restore data from bundle on activity resume
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try{
            if(savedInstanceState!=null){
                isConvertable=savedInstanceState.getBoolean("isConvertable");
                hasImage=savedInstanceState.getBoolean("hasImage");
                mEdittext.setText(savedInstanceState.getString("My_Message"));
                targetUri =savedInstanceState.getParcelable("Uri");
                mImageView.setImageURI(targetUri);
            }
        }
        catch (Exception e){ Log.d("EncodeActivityException","onRestoreInstantState"); }
    }

    //clear all data and inBackgound running class
    protected void onReset() {
       //flags
       hasImage=false;
       isConvertable=false;
       targetUri=null;
       //Data
       mImageView.setImageBitmap(null);
       mImageView.setImageDrawable(null);
       mImageView.setImageResource(R.drawable.ic_image_black_24dp);
       mEdittext.setText(null);
       mProgressBar.setVisibility(View.GONE);
       mecpbar.setVisibility(View.GONE);
       fragment.taskCancel();
    }


    //camera
    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile=null;
                try {
                    photoFile=createImageFile();
                } catch (IOException e) {
                    Log.e("dispatchPictureIntent_1",e.toString());
                }
                if (photoFile != null) {
                   try{
                       Uri photoURI = FileProvider.getUriForFile(this, "com.shadan.mcode.filProvider", photoFile);
                       takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                       startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                   }catch (Exception e){
                       Log.e("dispatchPictureIntent_2",e.toString());
                   }
                }
            }
        }
        else{//Requesting for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},  PERMISSION_REQUEST_CODE);
        }
    }

    //create a file on internal storage for camera picture
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_REQUEST_CODE){
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    dispatchTakePictureIntent();  //got camera permission
                }
        }
    }



    //call from mycard.xml
    //used for upload an image on ImageView
    public void onUpload(View view){ mEncodebtn.setEnabled(true);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    //upload  a image from photos
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode){
                case SELECT_PICTURE:

                    if(resultCode!=RESULT_OK) return;
                    final Uri uri= data.getData();
                    targetUri=uri;
                    mImageView.post(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageURI(uri);
                        }
                    });
                    hasImage=true;
                    Log.d("Image",""+uri);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    try{
                        if(resultCode!=RESULT_OK) break;
                        if(currentPhotoPath!=null)
                        {
                            File f = new File(currentPhotoPath);
                            final Uri contentUri = Uri.fromFile(f);
                            mImageView.post(new Runnable() {
                               @Override
                               public void run() {
                                   mImageView.setImageURI(contentUri);
                               }
                            });
                            targetUri=contentUri;
                            hasImage=true;
                            galleryAddPic(contentUri);
                            Log.d("Image",""+contentUri);
                        }
                        break;
                    }catch (Exception e){
                        Log.e("IMAGECAPTURE",e.toString());
                    }
                default:
                    break;
            }
    }


    //Encode buttun press
    public void encodefun(View view){
        if(hasImage){
            Toast.makeText(this,"encoding start", Toast.LENGTH_SHORT).show();
            BitmapDrawable abmp = (BitmapDrawable) mImageView.getDrawable();
            mbitmap = abmp.getBitmap();
            if(mEdittext.getText().length()>0){
                int height,width,pixel,max_size,data_length;
                data_length=mEdittext.getText().length();
                max_size=8191; //{(2^16)/8}-1
                height=mbitmap.getHeight();
                width=mbitmap.getWidth();
                pixel=height*width;

                if(pixel>data_length*8){
                    if(data_length<=max_size) isConvertable=true;
                    else toastMe("Massage size exceed...!");
                }
                else toastMe("Image Size is to short..!");

                if(isConvertable){
                    task(mEdittext.getText().toString());// yha bitmsg dalna hai
                }
            }
            else{
                toastMe("Input some message");
            }
        }
        else{
            toastMe("Please Select an Image.");
        }
    }

    public void task(String str){
        Log.d("Steganography.encode", "Encode start");
        if(Encrypt){///if Encryption on
            AES advanced_encryption_standard = new AES();
            str=advanced_encryption_standard.encrypt(str,KEY);
        }
        Log.d("Steganography.TAsk", "Encryption done");
        String bitstring=taskHead(str);
        Steganography steganography= new Steganography(bitstring,mbitmap);
        fragment.runTask(steganography);
    }

    public String taskHead(String str){
         /*First convert String str="ABC"; into binary fom
        01000001 01000010 01000011 then extracting the
        length of msg string now
        bitstring = length of msg (fixed 16 bits ) + msg to be send
        extracting lengthof string*/
        String bits_of_string=strToBinary(str);
        String bin_length=Integer.toBinaryString(str.length());
        bin_length=offset(bin_length);
        bits_of_string=bin_length+bits_of_string;
        Log.d("my_msg",bits_of_string);
        return bits_of_string;
    }

    //calling for save image on External storage  store <- thread
    public void storeImage(Bitmap bitmap) throws FileNotFoundException {

        File filepath = Environment.getExternalStorageDirectory();
        File dir = new File(filepath.getAbsolutePath()+"/Stegnography/");
        if(!dir.exists()) dir.mkdir();
        File file = new File(dir,System.currentTimeMillis()+".png"); //file_nameisCurrenntTimeinmillisec.jpg
        OutputStream outputStream =new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        try {
            outputStream.flush();
        }
        catch (IOException e) { e.printStackTrace(); }
        try {
            outputStream.close();
        }
        catch (IOException e) { e.printStackTrace(); }

        /*Refresh external list!!! and immediately available to the user.*/
        MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                        targetUri=uri;
                    }
                });
        Log.d("File","save");
        //inform at UI
        Snackbar.make(mConstraint_Layout_Encode,"Image saved in gallery 'file//Stegnography'.", Snackbar.LENGTH_LONG).show();
    }


    /*Refresh internal list!!! and immediately available to the user.*/
    private void galleryAddPic(Uri contentUri ) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //offset function for padding length
    public String offset(String str) {
        int l=str.length();
        while (l<Max){
            str="0"+str;
            l++;
        }
        return str;
    }

    //convertString to binary
    public String strToBinary(String s)
    {
        String par="";
        int n = s.length();
        for (int i = 0; i < n; i++)
        {
            int val = (int) s.charAt(i);
            String bin = "";
            while (val > 0)
            {
                if (val % 2 == 1) { bin += '1'; }
                else
                    bin += '0';
                val /= 2;
            }

            //padding in string
            while(bin.length()<8){
                bin=bin+"0";
            }
            // bin=bin+" ";
            bin=reverse(bin);
            par=par+bin;
        }
        return par;
    }


    public String reverse(String input)
    {
        char[] a = input.toCharArray();
        int l, r = a.length - 1;

        for (l = 0; l < r; l++, r--)
        {
            // Swap values of l and r
            char temp = a[l];
            a[l] = a[r];
            a[r] = temp;
        }
        return String.valueOf(a);
    }


    // Share image
    private void shareImage(Uri imagePath) {
        if(fragment.status()== AsyncTask.Status.RUNNING) {
            snackBar("Wait until process is running...");
        }else if(imagePath!=null  && fragment.status()!= AsyncTask.Status.RUNNING){
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, imagePath);
            startActivity(Intent.createChooser(sharingIntent, "Share Image Using"));
        }
        else {
            toastMe("Image is not available at Storage..!");
        }
    }

    //Toast method call from inner class
    public void toastMe(String msg) { Toast.makeText(this,msg, Toast.LENGTH_SHORT).show(); }

    //Create A Long snackBar for this activity
    public void snackBar(String msg) { Snackbar.make(mConstraint_Layout_Encode, msg, Snackbar.LENGTH_LONG).show(); }

    /*
    *******************************Fragment functions override****************************************************
    */

    @Override
    public void handleTaskUpdate(int value,int max) {
        if(mProgressBar.getVisibility()==View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mecpbar.setVisibility(View.VISIBLE);
        }
        mProgressBar.setMax(max);
        mProgressBar.setProgress(value);
    }

    @Override
    public void handleTaskPre() {
        mProgressBar.setVisibility(View.VISIBLE);
        mecpbar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
    }

    @Override
    public void handleTaskPost(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        mProgressBar.setVisibility(View.INVISIBLE);
        mecpbar.setVisibility(View.INVISIBLE);
        try {
            storeImage(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        toastMe("Done..!");
    }
}


