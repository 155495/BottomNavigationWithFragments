package com.example.bottomnavigationwithfragments;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.bottomnavigationwithfragments.VideoCompression.VideoCompress;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    View view;
    TextView txtName;

    private StorageReference mStorageRef;



    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        File myDirectory = new File(Environment.getExternalStorageDirectory(), "Bivin");

        if(!myDirectory.exists()) {
            myDirectory.mkdirs();
        }

        txtName=view.findViewById(R.id.textViewName);

        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");
                startActivityForResult(intent,1);


            }
        });

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            //videoView.setVideoURI(videoUri);
            Toast.makeText(getActivity(),videoUri+ "", Toast.LENGTH_LONG).show();

            Log.d("VideoUri:",videoUri+"");


            String  VideoPath=getRealPathFromURI(getContext(),videoUri);
            Log.d("VideoPathLoc:",VideoPath+"");
            Log.d("VideoDestLoc:",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"");


            final String dest=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Bivin"+  File.separator + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date()) + ".mp4";
            Log.d("VideoDestName:",dest+"");
             VideoCompress.compressVideoLow(VideoPath, dest, new VideoCompress.CompressListener() {
                 ProgressDialog  dialog = new ProgressDialog(getActivity());
                @Override
                public void onStart() {
                    //Start Compress
                    dialog.setMessage("Compressing video..");
                    dialog.show();
                    Log.d("VideoCompression:","Start");
                }

                @Override
                public void onSuccess() {
                    //Finish successfully
                    if (dialog.isShowing()) {
                        dialog.dismiss();


                    }
                    Log.d("VideoCompression:","Success");
                }

                @Override
                public void onFail() {
                    //Failed
                    Log.d("VideoCompression:","Fail");
                }

                @Override
                public void onProgress(float percent) {
                    Log.d("VideoCompression:","Progress");
                    dialog.setMessage("Compressing video..");
                    dialog.show();
                    //Progress
                }
            });

            upload(dest);
            //task.execute();

           /*// "file:///mnt/sdcard/FileName.mp3"

            try {

                String filePath = SiliCompressor.with(getActivity()).compressVideo(VideoPath, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
*/
        }
    }

    private void upload(String dest) {
        FirebaseApp.initializeApp(getContext());
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Uri file = Uri.fromFile(new File(dest));
        StorageReference riversRef = mStorageRef.child("Videos/");

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Log.d("FirebaseUpload:","Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("FirebaseUpload:","Failure");
                    }
                });
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Locale getLocale() {
        Configuration config = getResources().getConfiguration();
        Locale sysLocale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sysLocale = getSystemLocale(config);
        } else {
            sysLocale = getSystemLocaleLegacy(config);
        }

        return sysLocale;
    }

    @SuppressWarnings("deprecation")
    public static Locale getSystemLocaleLegacy(Configuration config){
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemLocale(Configuration config){
        return config.getLocales().get(0);
    }
}
