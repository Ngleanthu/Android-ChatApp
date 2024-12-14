package com.example.chatapp.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import java.io.File;

public class FileHelper {

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
                default:
                    // For general files (documents, etc.), fallback to older permissions
                    return true;
            }
        } else {
            // For Android versions below 13
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
            // For "file" scheme, use the File class
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
        } else {
            return "file";
        }
    }
}
