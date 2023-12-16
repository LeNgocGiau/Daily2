package com.example.dailyselfie.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.example.dailyselfie.MainActivity;
import com.example.dailyselfie.R;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    //lấy timestamp (dấu thời gian) của cuối ngày hiện tại
    public static long getEndOfDayMs() {
        final Calendar date = Calendar.getInstance(Locale.getDefault());
        //Thiết lập giờ của đối tượng Calendar thành giá trị tối đa (23) của ngày hiện tại.
        date.set(Calendar.HOUR_OF_DAY,
                date.getActualMaximum(Calendar.HOUR_OF_DAY));

        ////Thiết lập phút của đối tượng Calendar thành giá trị tối đa (59) của ngày hiện tại.
        date.set(Calendar.MINUTE, date.getActualMaximum(Calendar.MINUTE));

        ////Thiết lập giaay của đối tượng Calendar thành giá trị tối đa (59) của ngày hiện tại.
        date.set(Calendar.SECOND, date.getActualMaximum(Calendar.SECOND));
        //Thiết lập mili giây của đối tượng Calendar thành giá trị tối đa (999) của ngày hiện tại
        date.set(Calendar.MILLISECOND,
                date.getActualMaximum(Calendar.MILLISECOND));
        //trả về timestamp của thời điểm cuối ngày hiện tại
        return date.getTimeInMillis();
    }

    //đặt một báo thức nhắc nhở sau một khoảng thời gian nhất định
    @SuppressLint("ScheduleExactAlarm")
    public static void remindAfterHour(Context context, double hour) {
//        WorkManager.getInstance(this).cancelAllWork();
//        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(RemindWorker.class,hour, TimeUnit.HOURS)
//                .setInitialDelay(hour, TimeUnit.HOURS)
//                .build();
//        WorkManager.getInstance(this).enqueue(periodicWorkRequest);

        // tạo một đối tượng AlarmManager để quản lý các báo thức
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //Tạo một đối tượng Intent để khởi động một BroadcastReceiver khi báo thức được kích hoạt.
        Intent i = new Intent(context, AlarmReceiver.class);
       //đóng gói Intent và gửi nó đến BroadcastReceiver khi báo thức được kích hoạt
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2206, i, PendingIntent.FLAG_IMMUTABLE);

        //Tính thời gian trì hoãn của báo thức dựa trên tham số hour được truyền vào.
        final long ALARM_DELAY_IN = Math.round(hour * 3600 * 1000);
        //Tính thời gian kích hoạt của báo thức bằng cách cộng thời gian hiện tại với thời gian trì hoãn
        long alarmTimeAtUTC = System.currentTimeMillis() + ALARM_DELAY_IN;
        // Toast.makeText(context,ALARM_DELAY_IN + "",Toast.LENGTH_SHORT).show();
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent);
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void remindAfterTime(Context context, long time) {
        //Khởi tạo một đối tượng AlarmManager để quản lý các báo thức
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //Tạo một đối tượng Intent để khởi động một BroadcastReceiver khi báo thức được kích hoạt
        Intent i = new Intent(context, AlarmReceiver.class);
        //Tạo một đối tượng PendingIntent để đóng gói Intent và gửi nó đến BroadcastReceiver khi báo thức được kích hoạt
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2206, i, PendingIntent.FLAG_IMMUTABLE);

        // gán tham số time cho biến ALARM_DELAY_IN, đại diện cho thời gian trì hoãn của báo thức.
        final long ALARM_DELAY_IN = time;

        //Tính toán thời gian kích hoạt của báo thức bằng cách cộng thời gian hiện tại với thời gian trì hoãn.
        long alarmTimeAtUTC = System.currentTimeMillis() + ALARM_DELAY_IN;
        // Toast.makeText(context,ALARM_DELAY_IN + "",Toast.LENGTH_SHORT).show();
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent);
    }

    // hủy một báo thức đã được đặt trước đó
    public static void cancel(Context context) {
        //tạo một đối tượng AlarmManager để quản lý các báo thức
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //Tạo một đối tượng Intent tương ứng với BroadcastReceiver được sử dụng cho báo thức cần hủy.
        Intent i = new Intent(context, AlarmReceiver.class);
        //Tạo một đối tượng PendingIntent dựa trên Intent và mã nhận dạng báo thức (2206)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2206, i, PendingIntent.FLAG_IMMUTABLE);
        //Hủy báo thức được xác định bởi pendingIntent
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intentThis) {

        //Tạo một Intent mới để khởi động MainActivity
        Intent intent = new Intent(context, MainActivity.class);

        //Thiết lập các flag cho Intent để mở Activity mới và xóa bộ nhớ cache
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //Sử dụng một id ngẫu nhiên để phân biệt các PendingIntent khác nhau
        int id = new Random().nextInt();

        //Tạo một PendingIntent để khởi động MainActivity khi nhấn vào thông báo.
        PendingIntent pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String channelId = "123";
        //Lấy URI mặc định cho âm thanh thông báo
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle("Thông báo")
                .setContentText("Đã đến lúc selfie rồi bạn ơi..")
                .setAutoCancel(true)  // tự động hủy thông báo khi nhấn vào
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel(channelId,
                "Thông báo nhắc nhở",
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        //NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManager.notify(id, notificationBuilder.build());
        //đặt lại báo thức sau 24h
        remindAfterHour(context, 24D );
    }
}
