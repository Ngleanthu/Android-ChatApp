package com.example.chatapp.utils;

import android.Manifest;
import android.app.Activity;

import android.app.DownloadManager;
import android.content.ContentResolver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHelper {

    private static final Log log = LogFactory.getLog(FileHelper.class);
    private final Activity activity;
    private final ActivityResultLauncher<Intent> resultLauncher;
    private final ActivityResultLauncher<String[]> permissionLauncher;
    private final FileHelperCallback callback;

    public interface FileHelperCallback {
        void onFileSelected(Uri fileUri);
        void onPermissionDenied(String type);
    }

    public FileHelper(Activity activity,
                      ActivityResultLauncher<Intent> resultLauncher,
                      ActivityResultLauncher<String[]> permissionLauncher,
                      FileHelperCallback callback) {
        this.activity = activity;
        this.resultLauncher = resultLauncher;
        this.permissionLauncher = permissionLauncher;
        this.callback = callback;
    }

    public void selectFile(String type) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasStoragePermission(type)) {
            openFilePicker(type);
        } else {
            requestStoragePermission(type);
        }
    }

    private boolean hasStoragePermission(String type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            switch (type) {
                case "image":
                    return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                            == PackageManager.PERMISSION_GRANTED;
                case "video":
                    return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)
                            == PackageManager.PERMISSION_GRANTED;
                case "audio":
                    return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO)
                            == PackageManager.PERMISSION_GRANTED;
                default:
                    // For general files (documents, etc.), fallback to older permissions
                    return true;
            }
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            // For Android versions below 13
            return true;
        } else{
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission(String type) {
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            switch (type) {
                case "image":
                    permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
                    break;
                case "video":
                    permissions = new String[]{Manifest.permission.READ_MEDIA_VIDEO};
                    break;
                case "audio":
                    permissions = new String[]{Manifest.permission.READ_MEDIA_AUDIO};
                    break;
                default:
                    permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                    break;
            }
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        // Request permissions using the launcher
        permissionLauncher.launch(permissions);
    }

    private void openFilePicker(String type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        switch (type) {
            case "file":
                intent.setType("*/*");
                break;
            case "image":
                intent.setType("image/*");
                break;
            case "video":
                intent.setType("video/*");
                break;
            case "audio":
                intent.setType("audio/*");
                break;
            default:
                intent.setType("*/*");
        }

        resultLauncher.launch(Intent.createChooser(intent, "Select " + capitalize(type)));
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String getFileName(Context context, Uri uri) {
        String fileName = null;

        // Check if the URI scheme is "content"
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }

        return fileName;
    }

    public static String determineFileType(String mimeType) {
        if (mimeType == null) return "file";

        if (mimeType.startsWith("image/")) {
            return "image";
        } else if (mimeType.startsWith("video/")) {
            return "video";
        } else if (mimeType.startsWith("audio/")) {
            return "audio";
        } else {
            return "file";
        }
    }

    public static void downloadFile(String fileUrl, String fileName, Context context) {
        if (fileUrl == null) return;

        try {
            Uri fileUri = Uri.parse(fileUrl); // Parse the file URL into a URI

            // Use DownloadManager to download the file
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager == null) {
                Toast.makeText(context, "Download Manager not available", Toast.LENGTH_SHORT).show();
                return;
            }

            DownloadManager.Request request = new DownloadManager.Request(fileUri);
            request.setTitle((fileName != null ? fileName : "file"));
            request.setDescription("File is being downloaded...");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName != null ? fileName : "unknown_file"); // Save to Downloads folder
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Enqueue the download
            downloadManager.enqueue(request);
            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.d("Upload", "Image URI: " + fileUrl);
            android.util.Log.d("DOWNLOADFILE", "Error downloading file: " + e.getMessage());
            Toast.makeText(context, "Error downloading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}
