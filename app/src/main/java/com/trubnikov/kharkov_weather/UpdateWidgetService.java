package com.trubnikov.kharkov_weather;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UpdateWidgetService extends Service {

    final String TAG = "myLogs";
    public Elements text; //текст
    public Elements media; //картинки
    //то в чем будем хранить данные пока не передадим на экран
    public ArrayList<String> titleList = new ArrayList<>();
    RemoteViews remoteViews;
    AppWidgetManager appWidgetManager;
    int[] allWidgetIds;
    int widthView, picSize;
    float scalePics;
    private Bitmap bitmap, scaledPic;


    @Override
    public void onStart(Intent intent, int startId) {

        //вычисляем размеры дисплея и коэффициенты масштабирования
        DisplayMetrics display = this.getResources().getDisplayMetrics();
        widthView = display.widthPixels;
        //коэффициент масштабирования картинок
        scalePics = (float) (widthView / 720.0f);
        picSize = Math.round(128 * scalePics);

        //AppWidgetManager получает информацию об установленных виджетах
        appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        new NewThread().execute();

        for (int widgetId : allWidgetIds) {
            //получаем описание виджета в XML
            remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_main);
            //В этой строке регистрируем касание виджета
            Intent clickIntent = new Intent(this.getApplicationContext(), MainWidget.class);
            //Указываем что делать (пришло время обновить AppWidget)
            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.Widget, pendingIntent);
        }
        //stopSelf();
        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //класс который делает запросы
    public class NewThread extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... arg) {
            // захват страницы
            Document doc;
            bitmap = null;

            try {
                //data sorce
                doc = Jsoup.connect("https://tvoj.kharkov.ua/help/weather/").get();
                //задаем с какого места. Например с заголовков статей
                text = doc.select(".dat");
                for (Element txt:text)
                {Log.d ("UpdateWidgetServiceLog", "txt: "+ txt.toString());}
                media = doc.select(".cw");
                String imageURL ="https:" + media.get(0).attr("src");
                {Log.d ("UpdateWidgetServiceLog", "imageURL: "+ imageURL);}
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
                scaledPic = Bitmap.createScaledBitmap(bitmap, picSize, picSize, true);
                //чистим ArrayList перед заполнением
                titleList.clear();
                //записываем в ArrayList
                titleList.add(text.get(0).text());
                titleList.add(text.get(3).text());
                titleList.add(text.get(4).text());
                titleList.add(text.get(6).text());
                titleList.add(text.get(7).text());

            } catch (IOException e) {
                Log.d ("UpdateWidgetService", "error: "+e);
            }
            return scaledPic;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            for (int widgetId : allWidgetIds) {
                //выводим полученную информацию на экран
                // Set the bitmap into ImageView
                try {//если не подключен интернет
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

}
