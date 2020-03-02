package com.viking.shareimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

public class MainActivity extends AppCompatActivity {

    private ImageView pickedImageView;

    /**
     * REQUEST CODE for taking permission and picking/capturing image
     */
    private static final int GALLERY_REQUEST_CODE = 332;
    private static final int CAMERA_REQUEST_CODE = 333;
    private static final int SHARE_PERMISSION_CODE = 223;

    //URI of picked/captured image
    private Uri cameraFileURI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickedImageView = findViewById(R.id.picked_image_view);
    }


    /**
     * method called when user want to share image and text using Twitter Composer
     * NOTE : If the Twitter app is not installed, the intent will launch twitter.com in a browser,
     * but the specified image will be ignored.
     *
     * @param view of the calling element
     */
    public void shareUsingTwitterComposer(View view) {
        //check if user has picked/captured image or not
        if (cameraFileURI != null) {
            TweetComposer.Builder builder = new TweetComposer.Builder(this)
                    .text("This is a testing tweet!!")//pass any tweet message here
                    .image(cameraFileURI);//pass captured/picked image URI
            builder.show();
        } else {
            //if not then show dialog to pick/capture image
            Toast.makeText(this, "Please select image first to share.", Toast.LENGTH_SHORT).show();
            checkStorageAndCameraPermission();
        }
    }

    /**
     * check if the app has the CAMERA and STORAGE permission to perform the operation
     * This method will automatically ask permission to user if permission is not granted.
     */
    private void checkStorageAndCameraPermission() {
        if (MarshMallowPermission.checkMashMallowPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, SHARE_PERMISSION_CODE)) {
            onPermissionGranted();
        }
    }

    /**
     * when permission is granted for CAMERA and STORAGE show alert dialog with two options : CAMERA and GALLERY
     */
    private void onPermissionGranted() {
        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(new String[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                //Gallery
                                selectImageFromGallery();
                                break;
                            case 1:
                                //Camera
                                captureImageFormCamera();
                                break;
                        }
                    }
                })
                .setCancelable(true)
                .create()
                .show();
    }
    /**
     * start activity to pick image from gallery
     */
    private void selectImageFromGallery() {
        Intent in = new Intent(Intent.ACTION_PICK);
        in.setType("image/*");
        startActivityForResult(in, GALLERY_REQUEST_CODE);
    }

    /**
     * start activity to capture image
     */
    private void captureImageFormCamera() {
        //check if device support camera or not if not then don't do anything
        if (!CameraUtils.isDeviceSupportCamera(this)) {
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Get the file URI using the below code
        //here in place of AUTHORITY you have to pass <package_name>.file_provider
        //NOTE :For more details check this link  :https://developer.android.com/reference/android/support/v4/content/FileProvider.html
        cameraFileURI = FileProvider.getUriForFile(this, "com.viking.shareimage.file_provider", FileNameCreation.createImageFile(this));

        //after getting image URI pass it via Intent
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileURI);

        //grant URI permission to access the create image URI
        for (ResolveInfo resolveInfo : getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
            grantUriPermission(resolveInfo.activityInfo.packageName, cameraFileURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        //here check if there ia any app available to perform camera task or not if not then show toast
        //NOTE : This condition is not required because every device has Camera app but in rare cases some device don't have camera
        //so to avoid that thing we have to add this condition
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No apps to capture image.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //check if all multiple permissions are granted or not
            case SHARE_PERMISSION_CODE:
                if (permissions.length > 0 && grantResults.length > 0) {
                    int counter = 0;
                    for (int result : grantResults) {
                        if (result != 0) {
                            onPermissionDenied();
                            return;
                        }
                        counter++;

                    }
                    if (counter == permissions.length) {
                        //All permission granted
                        onPermissionGranted();
                    }
                }
                break;
        }
    }

    /**
     * if any one of the permission is denied show a dialog to user to grant permission again if they want
     */
    private void onPermissionDenied() {
        new AlertDialog.Builder(this)
                .setMessage("Both permission are required to pick/capture image. Do you want to try again.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //if user click ok then again ask for permission
                        checkStorageAndCameraPermission();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //if user click on cancel show toast
                        Toast.makeText(MainActivity.this, "You cannot share the images without giving these permissions.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GALLERY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    //get the picked image URI
                    Uri imageUri = data.getData();
                    //set the picked image URI to created variable
                    this.cameraFileURI = imageUri;

                    //now display picked image over ImageView
                    displayImage(imageUri);
                } else {

                    //if user cancelled or failed to pick image show toast
                    Toast.makeText(this, "Failed to pick up image from gallery.", Toast.LENGTH_SHORT).show();
                }
                break;
            case CAMERA_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    //check if Camera URI is null or not
                    if (cameraFileURI != null) {
                        //if not null then show URI over image view
                        displayImage(cameraFileURI);
                    } else {
                        //if URI is null show toast
                        Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //if user cancelled capture image then show toast
                    Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * method to show URI over image uri
     * NOTE : I am using picasso to load images as picasso will automatically scale large size images
     * and display very efficiently over ImageView
     *
     * @param imageUri of the picked/captured image
     */
    private void displayImage(Uri imageUri) {
           Picasso.with(this).load(imageUri).into(pickedImageView);
    }

    private void shareInstagram(){

        /// Method 1 : Optimize
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*"); // set mime type
        shareIntent.putExtra(Intent.EXTRA_STREAM,cameraFileURI); // set uri
        shareIntent.setPackage("com.instagram.android");
        startActivity(shareIntent);
    }

    public void shareUsingTwitterNativeComposer(View view) {
        shareInstagram();
    }

}
