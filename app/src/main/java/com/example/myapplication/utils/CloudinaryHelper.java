package com.example.myapplication.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    private static final String TAG = "CloudinaryHelper";
    private static boolean isInitialized = false;

    // ⚠️ REMPLACEZ CES VALEURS PAR LES VÔTRES
    private static final String CLOUD_NAME = "dn1j31oxy";
    private static final String API_KEY = "493167378342941";
    private static final String API_SECRET = "HIOUoyphRSDiEPUoH9I9ee6Wljs";

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
        void onProgress(int progress);
    }

    public static void initialize(Context context) {
        if (!isInitialized) {
            try {
                Map<String, Object> config = new HashMap<>();
                config.put("cloud_name", CLOUD_NAME);
                config.put("api_key", API_KEY);
                config.put("api_secret", API_SECRET);
                config.put("secure", true);

                MediaManager.init(context, config);
                isInitialized = true;
                Log.d(TAG, "Cloudinary initialisé avec succès");
            } catch (Exception e) {
                Log.e(TAG, "Erreur initialisation Cloudinary: " + e.getMessage());
            }
        }
    }

    public static void uploadImage(Context context, Uri imageUri, String folder,
                                   final UploadListener listener) {
        if (!isInitialized) {
            initialize(context);
        }

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", folder);
            options.put("resource_type", "auto");
            options.put("upload_preset", "ml_default"); // Preset par défaut

            String requestId = MediaManager.get().upload(imageUri)
                    .option("folder", folder)
                    .option("resource_type", "auto")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Upload démarré: " + requestId);
                            if (listener != null) {
                                listener.onProgress(0);
                            }
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            Log.d(TAG, "Upload: " + progress + "%");
                            if (listener != null) {
                                listener.onProgress(progress);
                            }
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            Log.d(TAG, "Upload réussi: " + imageUrl);
                            if (listener != null) {
                                listener.onSuccess(imageUrl);
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Erreur upload: " + error.getDescription());
                            if (listener != null) {
                                listener.onError(error.getDescription());
                            }
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.d(TAG, "Upload reprogrammé: " + requestId);
                        }
                    })
                    .dispatch();

            Log.d(TAG, "Request ID: " + requestId);

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'upload: " + e.getMessage());
            if (listener != null) {
                listener.onError(e.getMessage());
            }
        }
    }
}