package com.wtm.cashbon.firebase;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wtm.cashbon.HomeFragment;
import com.wtm.cashbon.MainActivity;
import com.wtm.cashbon.R;
import com.wtm.cashbon.utils.SessionManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by @alimasudd on 3/27/2019.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String TAG = "FIREBASEMESSAGE";
    Bitmap bitmap;
    SessionManager sessionManager;

    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        sessionManager = new SessionManager(this);

        // condition to start activity when receive lock key
        String myCustomKey = data.get("pelunas_key");


        if (myCustomKey != null) {
             if (myCustomKey.toLowerCase().equals("dialog_cair")) {

                Log.d(TAG, "DIALOG " + myCustomKey);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                    Intent intent = new Intent(this, DialogKomisiCairActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    Log.d(TAG, "DIALOGOPEN1 ");
//                } else {
//
//                    Intent intent = new Intent(this, DialogKomisiCairActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    Log.d(TAG, "DIALOGOPEN2 ");
//                }

                sendText(remoteMessage.getData().get("body"), remoteMessage.getData().get("title"), "indi");
                Log.d("NotifData1", "Data Notifikasi : " + remoteMessage.getData());
            }
        }
        Log.d(TAG, "LOCATIONTEST " + remoteMessage.getData().get("image"));
        Log.d(TAG, "Notifikasi Dari : " + remoteMessage.getFrom());
//        Log.d(TAG, "Notifikasi Body : " + remoteMessage.getNotification().getBody());

        //Memeriksa apakah pesan berisi data
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Notifikasi : " + remoteMessage.getData());
        }


        String url = remoteMessage.getData().get("url");
        String imageUri = remoteMessage.getData().get("image");
        if (imageUri != null && !imageUri.equals("") && url != null && !url.equals("")) {
            //dengan gambar
            bitmap = getBitmapfromUrl(imageUri);
            sendImageURL(bitmap, remoteMessage.getData().get("body"), url, remoteMessage.getData().get("title"));
            Log.d("NotifData2", "Data Notifikasi : " + remoteMessage.getData());
        } else if (imageUri != null && !imageUri.equals("")) {
            //dengan gambar
            bitmap = getBitmapfromUrl(imageUri);
            sendImage(bitmap, remoteMessage.getData().get("body"), remoteMessage.getData().get("title"));
            Log.d("NotifData2", "Data Notifikasi : " + remoteMessage.getData());
        } else if (url != null && !url.equals("")) {
            //dengan link
            sendURL(remoteMessage.getData().get("body"), url, remoteMessage.getData().get("title"));
            Log.d("NotifData3", "Data Notifikasi : " + remoteMessage.getData());
        } else if (remoteMessage.getData().get("body") != null) {
            // notifikasi tanpa gambar
            sendText(remoteMessage.getData().get("body"), remoteMessage.getData().get("title"), remoteMessage.getData().get("tipenotif"));
            Log.d("NotifData1", "Data Notifikasi : " + remoteMessage.getData());
        }

        //To get a Bitmap image from the URL received

    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Token baru: " + token);
        // If you want to send messages to this application instance or
    }

    private void sendImageURL(Bitmap image, String body, String link, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))/*Notification icon image*/
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(image))/*Notification with Image*/
                .setContentText(body).setAutoCancel(true).setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    private void sendImage(Bitmap image, String body, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(getString(R.string.fragment), HomeFragment.class.getSimpleName());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))/*Notification icon image*/
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(image))/*Notification with Image*/
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentText(body).setAutoCancel(true).setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    private void sendURL(String body, String link, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentText(body).setAutoCancel(true).setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }



    private void sendText(String body, String title, String indi) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (indi != null) {

            if (indi.trim().equals("quote")) {
                Log.d(TAG, "yessssssss");
                intent.putExtra("open", HomeFragment.class.getSimpleName());
            }
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentText(body).setAutoCancel(true).setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }
    /*
     *To get a Bitmap image from the URL received
     * */
    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }
}
