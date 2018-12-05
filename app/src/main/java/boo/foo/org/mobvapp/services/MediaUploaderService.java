package boo.foo.org.mobvapp.services;


import android.app.Activity;

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

    private final String servicePath;

    public MediaUploaderService(String servicePath) {
        this.servicePath = servicePath;
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
                    activity.runOnUiThread(() -> onFail.apply("OkHTTP client error"));
                }

                @Override
                public void onResponse(Response response) {
                    activity.runOnUiThread(() -> {
                        try {
                            if (response.isSuccessful() && response.message().equals("OK")) {
                                String jsonStr = response.body().string();
                                JsonObject jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();
                                String url = Utils.getMediaFileUrl(jsonObj.get("message").getAsString());
                                onResolved.apply(url);
                            } else {
                                onFail.apply("OkHTTP client error");
                            }
                        } catch (IOException ioe) {
                            onFail.apply("OkHTTP client error");

                        }
                    });

                }
            });

        } catch (Exception ex) {
            onFail.apply("OkHTTP client error");
        }
    }


}