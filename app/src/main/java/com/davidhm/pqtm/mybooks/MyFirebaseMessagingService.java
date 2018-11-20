package com.davidhm.pqtm.mybooks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    private static final String TAG = "MyFirebaseMsgService";
    private static final String TEXT_DELETE ="ELIMINAR LIBRO";
    private static final String TEXT_SHOW_DETAIL = "MOSTRAR DETALLE";
    private static final String DEFAULT_NOTIFICATION_TITLE = "Notificación catálogo de libros";
    private static final String DEFAULT_NOTIFICATION_BODY = "Acción requerida";

    // Constantes de acceso público
    public static final String NOTIFICACTION_CHANNEL_NAME = "Actualizaciones base de datos";
    public static final String ACTION_DELETE ="Eliminar_libro";
    public static final String ACTION_SHOW_DETAIL = "Mostrar_detalle_libro";
    public static final int EXPANDED_NOTIFICATION_ID = 10;
    public static final String BOOK_POSITION = "book_position";

    /**
     * Llamado la primera vez que se instala la aplicación, y cuando se genera
     * un nuevo token para el envío de mensajes desde Firebase Cloud Messaging.
     *
     * @param s El nuevo token generado
     */
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        // Muestra el nuevo token en la pantalla de log
        Log.d(TAG, "onNewToken(): token = " + s);
    }

    /**
     * Llamado cuando se recibe un mensaje remoto.
     *
     * @param remoteMessage Mensaje recibido de Firebase Cloud Messaging
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived(): mensaje recibido de " + remoteMessage.getFrom());

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String > data = remoteMessage.getData();
        // Selecciona el tipo de notificación a crear
        if (!data.isEmpty()) {
            if (notification == null) {
                // Crea notificación para mensaje de datos
                sendNotification("", "", data);
            } else {
                // Crea notificación para mensaje de notificación con carga de datos
                sendNotification(notification.getTitle(), notification.getBody(), data);
            }
        }
        else if (notification != null) {
            // Crea notificación básica (sin datos)
            sendBasicNotification(notification.getTitle(), notification.getBody());
        }
    }

    /**
     * Crea y muestra una notificación básica (sin carga de datos) al recibir
     * un mensaje de notificación remoto.
     *
     * @param messageTitle  Título a mostrar en la notificación
     * @param messageBody   Texto a mostrar en la notificación
     */
    private void sendBasicNotification(String messageTitle, String messageBody) {
        Log.d(TAG, "sendBasicNotification(): notificación básica");

        // Establece valores por defecto de título y cuerpo, si no se facilitan
        if (messageTitle == null || messageTitle.trim().isEmpty() ) {
            messageTitle = DEFAULT_NOTIFICATION_TITLE;
        }
        if (messageBody == null || messageBody.trim().isEmpty() ) {
            messageBody = DEFAULT_NOTIFICATION_BODY;
        }

        Intent intent = new Intent(this, BookListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Construye notificación básica
        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Muesta la notificación
        notifyNotification(0, channelId, notificationBuilder.build());
    }

    /**
     * Crea y muestra una notificación expandida al recibir un mensaje de
     * notificación remoto con carga de datos.
     * Si en los datos no existe un par clave-valor donde la clave sea igual
     * a la constante BOOK_POSITION, se considera que los datos son erróneos
     * y por tanto no se genera la notificación.
     *
     * @param messageTitle  Título a mostrar en la notificación
     * @param messageBody   Texto a mostrar en la notificación
     * @param data  Carga de datos del mensaje
     */
    private void sendNotification(String messageTitle, String messageBody, Map<String, String> data) {
        Log.d(TAG, "sendNotification(): notificación extendida; data = " + data);

        // Busca la clave book_position en los datos
        String bookPosition = data.get(BOOK_POSITION);
        if (bookPosition == null) {
            Log.d(TAG, "sendNotification(): clave " + BOOK_POSITION +
                            " no encontrada; no se genera la notificación");
            // Sale sin generar la notificación
            return;
        }

        // Establece valores por defecto de título y cuerpo, si no se facilitan
        if (messageTitle == null || messageTitle.trim().isEmpty() ) {
            messageTitle = DEFAULT_NOTIFICATION_TITLE;
        }
        if (messageBody == null || messageBody.trim().isEmpty() ) {
            messageBody = DEFAULT_NOTIFICATION_BODY;
        }

        // Acción eliminar libro
        Intent intentDelete = new Intent(this, BookListActivity.class);
        intentDelete.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentDelete.setAction(ACTION_DELETE);
        intentDelete.putExtra(BOOK_POSITION, bookPosition);
        PendingIntent pendingIntentDelete = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), intentDelete, 0);

        // Acción mostrar detalle del libro
        Intent intentShowDetail = new Intent(this, BookListActivity.class);
        intentShowDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentShowDetail.setAction(ACTION_SHOW_DETAIL);
        intentShowDetail.putExtra(BOOK_POSITION, bookPosition);
        PendingIntent pendingIntentShowDetail = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), intentShowDetail, 0);

        // Construye notificación expandida
        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(DEFAULT_NOTIFICATION_TITLE)
                        .bigText("Elimina el libro " + bookPosition + " de la lista, o muestra el detalle del libro"))
                .addAction(new NotificationCompat.Action(R.drawable.delete, TEXT_DELETE, pendingIntentDelete))
                .addAction(new NotificationCompat.Action(R.drawable.description, TEXT_SHOW_DETAIL, pendingIntentShowDetail));

        // Muesta la notificación
        notifyNotification(EXPANDED_NOTIFICATION_ID, channelId, notificationBuilder.build());
    }

    /**
     * Muestra la notificación en el dispositivo.
     *
     * @param id    Identificador de la notificación
     * @param channelId Canal de la notificación
     * @param notification  Notificación a mostrar
     */
    private void notifyNotification(int id, String channelId, Notification notification) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // A partir de android Oreo es necesario un canal de notificaciones.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        NOTIFICACTION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            // Muestra la notificación
            notificationManager.notify(id, notification);
        }
    }
}
