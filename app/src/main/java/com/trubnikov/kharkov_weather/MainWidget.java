package com.trubnikov.kharkov_weather;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MainWidget extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //������������� ��� ����������� ���������� ���������
        ComponentName thisWidget = new ComponentName(context, MainWidget.class);
        //���� � ��� �� ������ ����� ���� �������� ��������� ���, ������� ������
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        //MainWidget �����������
        //������� ������ ��� ������ �������
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        //���������� ������, ������� �������� ��������� allWidgetIds
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        //��������� ������
       //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
           Log.d ("UpdateWidgetService", "MW startForegroundService");
           //context.startForegroundService(intent);
           UpdateWidgetService.enqueueWork(context, intent);
       /* } else {
           Log.d ("UpdateWidgetService", "MW startService");
            context.startService(intent);
       }*/

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
