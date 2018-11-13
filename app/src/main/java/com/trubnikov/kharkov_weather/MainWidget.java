package com.trubnikov.kharkov_weather;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MainWidget extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //Идентификатор для конкретного компонента программы
        ComponentName thisWidget = new ComponentName(context, MainWidget.class);
        //Один и тот же виджет может быть размещен несколько раз, поэтому массив
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        //MainWidget установлено
        //Готовим Интент для вызова Сервиса
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        //записываем ссылку, которая содержит несколько allWidgetIds
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        //запускаем сервис
        context.startService(intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
