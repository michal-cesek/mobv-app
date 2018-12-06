package boo.foo.org.mobvapp.services;


import android.app.Activity;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import boo.foo.org.mobvapp.Utils;


public class MediaUploaderService {
    private final String TAG = "MediaUploaderService:";

    private final String servicePath;

    public MediaUploaderService() {
        this.servicePath = "http://mobv.mcomputing.eu/upload/index.php";
    }

    public void upload(
            Activity activity,
            String parameterName,
            File file,
            Function<String, Object> onResolved,
            Function<String, Void> onFail
    ) {
        String mime = Utils.getMimeType(file.getAbsolutePath());
        if (mime == null) {
            Log.d(TAG, "upload fail - corrupted file");
            onFail.apply("Corrupted file");
            return;
        }

        RequestBody rb = RequestBody.create(MediaType.parse(mime), file);
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart(parameterName, file.getName(), rb)
                    .build();

            Request request = new Request.Builder()
                    .url(servicePath)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Request request, IOException e) {
                    activity.runOnUiThread(() -> {
                        Log.d(TAG, "upload fail httpok onFailure", e);
                        onFail.apply("OkHTTP client error");
                    });
                }

                @Override
                public void onResponse(Response response) {
                    activity.runOnUiThread(() -> {
                        try {
                            String jsonStr = response.body().string();
                            JsonObject jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();

                            if (response.isSuccessful() && jsonObj.get("status").getAsString().equals("ok")) {
                                String url = Utils.getMediaFileUrl(jsonObj.get("message").getAsString());
                                Log.d(TAG, "upload success httpok onResponse " + url + " " + jsonStr);
                                onResolved.apply(url);
                            } else {
                                Log.d(TAG, "upload fail httpok onResponse - not successful " + jsonStr);
                                onFail.apply("OkHTTP client error");
                            }
                        } catch (IOException ioe) {
                            Log.d(TAG, "upload fail httpok onResponse - IOException");
                            onFail.apply("OkHTTP client error");

                        }
                    });

                }
            });

        } catch (Exception ex) {
            Log.d(TAG, "upload fail httpok Exception catched");
            onFail.apply("OkHTTP client error");
        }
    }


}