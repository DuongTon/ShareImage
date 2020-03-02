package com.viking.shareimage;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileNameCreation {

    /**
     * method to return File of created image
     * @param context of calling class
     * @return File of the created image
     */
    public static File createImageFile(Context context) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //create image file name under Pictures directory
        File storageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "TweetImages");

        //if directory doesn't exist create directory
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File image = null;
        try {
            //now create jpg image under created directory
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }


}
