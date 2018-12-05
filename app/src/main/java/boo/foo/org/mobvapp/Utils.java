package boo.foo.org.mobvapp;

import android.webkit.MimeTypeMap;

import java.util.List;

public class Utils {

    public static boolean checkIfIsSupportedFileType(String filePath, List<String> supportedTypes) {
        return supportedTypes.stream()
                .anyMatch(supportedType -> {
                    String type = getMimeType(filePath);
                    return supportedType.equals(type);
                });
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getMediaFileUrl(String filename) {
        return "http://mobv.mcomputing.eu/upload/v/" + filename;
    }


}
