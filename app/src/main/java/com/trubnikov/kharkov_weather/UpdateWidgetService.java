package com.trubnikov.kharkov_weather;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.JobIntentService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Example implementation of a JobIntentService.
 */
public class UpdateWidgetService extends JobIntentService {
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    private Elements text; //�����
    public Elements media; //��������
    //�� � ��� ����� ������� ������ ���� �� ��������� �� �����
    public final ArrayList<String> titleList = new ArrayList<>();
    RemoteViews remoteViews;
    AppWidgetManager appWidgetManager;
    int[] allWidgetIds;
    int widthView, picSize;
    float scalePics;
    private Bitmap bitmap, scaledPic;


    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, UpdateWidgetService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        //Log.i("UpdateWidgetService", "Executing work: " + intent);
        //��������� ������� ������� � ������������ ���������������
        DisplayMetrics display = this.getResources().getDisplayMetrics();
        widthView = display.widthPixels;
        //����������� ��������������� ��������
        scalePics = (float) (widthView / 720.0f);
        picSize = Math.round(128 * scalePics);


        //AppWidgetManager �������� ���������� �� ������������� ��������
        appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        try {
            allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new requestThread().execute();

        if (allWidgetIds != null) {
            for (int widgetId : allWidgetIds) {
                //�������� �������� ������� � XML
                remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_main);
                //� ���� ������ ������������ ������� �������
                Intent clickIntent = new Intent(this.getApplicationContext(), MainWidget.class);
                //��������� ��� ������ (������ ����� �������� AppWidget)
                clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.Widget, pendingIntent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        }

        //Log.i("UpdateWidgetService", "Completed service @ ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //����� ������� ������ �������
    class requestThread extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... arg) {
            // ������ ��������
            Document doc;
            bitmap = null;

            try {
                //data sorce
//                doc = Jsoup.connect("https://tvoj.kharkov.ua/help/weather/").get();

                doc = Jsoup.connect("https://meteopost.com/weather/kharkov/")
                        .sslSocketFactory(socketFactory())
                        .get();
                //������ � ������ �����. �������� � ���������� ������
                text = doc.select(".dat");
                for (Element txt:text){
                    Log.d ("UpdateWidgetService", "txt: "+txt);}
                //������ ArrayList ����� �����������
                titleList.clear();
                //���������� � ArrayList
                titleList.add(text.get(0).text());
                titleList.add(text.get(3).text());
                titleList.add(text.get(4).text());
                titleList.add(text.get(6).text());
                titleList.add(text.get(7).text());

                media = doc.select(".cw");
                String imageURL = "https:" + media.get(1).attr("src");
                Log.d ("UpdateWidgetService", "imageURL: "+imageURL);
                // Download Image from URL
                URL url = new URL(imageURL);

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setSSLSocketFactory(socketFactory());
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
                scaledPic = Bitmap.createScaledBitmap(bitmap, picSize, picSize, true);

            } catch (IOException e) {
                Log.d ("UpdateWidgetService", "error: "+e);
            }
            return scaledPic;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            for (int widgetId : allWidgetIds) {
                //������� ���������� ���������� �� �����
                // Set the bitmap into ImageView
                try {//���� �� ��������� ��������
                    remoteViews.setImageViewBitmap(R.id.imageView1, result);
                    remoteViews.setTextViewText(R.id.temp, titleList.get(0));
                    remoteViews.setTextViewText(R.id.humid, titleList.get(1));
                    remoteViews.setTextViewText(R.id.pressure, titleList.get(2));
                    remoteViews.setTextViewText(R.id.wind, titleList.get(3));
                    remoteViews.setTextViewText(R.id.wind_speed, titleList.get(4));
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                } catch (Exception e) {
                }
            }
        }
    }

    @SuppressLint("TrustAllX509TrustManager")
    private SSLSocketFactory socketFactory() {

        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType) {
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }};

        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }


}