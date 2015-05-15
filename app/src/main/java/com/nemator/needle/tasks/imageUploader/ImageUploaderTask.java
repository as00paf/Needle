package com.nemator.needle.tasks.imageUploader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;

import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageUploaderTask extends AsyncTask<ImageUploadParams, Void, ImageUploadResult> {

    private static final String UPLOAD_HAYSTACK_IMAGE_URL = AppConstants.PROJECT_URL +"uploadHaystackImage.php";

    private static final String TAG = "FetchHaystacksTask";

    private JSONParser jsonParser = new JSONParser();

    private ImageUploadResponseHandler delegate;
    private ImageUploadParams params;

    public interface ImageUploadResponseHandler {
        void onImageUploaded(ImageUploadResult result);
    }

    public ImageUploaderTask(ImageUploadParams params, ImageUploadResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(ImageUploadResult result) {
        if(this.params.progressBar != null) this.params.progressBar.setVisibility(View.GONE);
        delegate.onImageUploaded(result);
    }

	@Override
	// Actual download method, run in the task thread
	protected ImageUploadResult doInBackground(ImageUploadParams... params) {
        ImageUploadResult result = new ImageUploadResult();

        if(this.params.bitmap != null){
            try{
                //Encode Image
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                this.params.bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                String encodedString = Base64.encodeToString(byte_arr, 0);

                //Params
                List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
                requestParams.add(new BasicNameValuePair("data", encodedString));
                requestParams.add(new BasicNameValuePair("fileName", this.params.fileName));

                //Request
                JSONObject json = jsonParser.makeHttpRequest(UPLOAD_HAYSTACK_IMAGE_URL, "POST", requestParams);

                //Results
                result.successCode = json.getInt(AppConstants.TAG_SUCCESS);
                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
                result.imageURL = json.getString(AppConstants.TAG_PICTURE_URL);

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                result.successMessage = "An error occured. \n" + e.toString();
            }
        }else{
            result.successMessage = "Bitmap is null";
        }

        result.successCode = 0;
        return result;
	}
}