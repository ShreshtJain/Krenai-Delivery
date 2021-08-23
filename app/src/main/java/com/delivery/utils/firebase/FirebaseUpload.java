package com.delivery.utils.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.delivery.utils.jkeys.Keys;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;

import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;

public class FirebaseUpload {
    private static final String TAG = FirebaseUpload.class.getSimpleName();
    FirebaseStorage storage;
    StorageReference storageReference;
    Context context;
    FirebaseUser user;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;

    public FirebaseUpload(Context context) {
        sharedPreferenceStatus = context.getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        this.context = context;
    }

    public boolean uploadImage(Uri siteShotBitmap, Long timeStamp, String imageType) {
        final boolean[] status = {false};
        if (imageType.contains("Profile") || imageType.contains("Licence")|| imageType.contains("Adhar")) {
            if (siteShotBitmap != null) {
                StorageReference ref = storageReference.child(imageType + "/" + timeStamp);
                ref.putFile(siteShotBitmap)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                status[0] = true;
                                System.out.println("FIREBASE TASK SHOT : "+taskSnapshot.getMetadata().getGeneration());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                                status[0] = false;
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                            }
                        });
                return true;
            }
        }
        return false;
    }
}
