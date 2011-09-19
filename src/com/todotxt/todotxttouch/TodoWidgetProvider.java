/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/TodoWidgetProvider.java
 *
 * Copyright (c) 2011 Scott Anderson, Tomasz Roszko
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Scott Anderson <scotta[at]gmail[dot]com>
 * @author Tomasz Roszko <geekonek[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Scott Anderson, Tomasz Roszko
 */
package com.todotxt.todotxttouch;

import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.util.Util;

public class TodoWidgetProvider extends AppWidgetProvider {

	private static final String TAG = TodoWidgetProvider.class.getName();
	private static final int TASKS_TO_DISPLAY = 4;
	
	private static final int TASK_ID = 0;
	private static final int TASK_PRIO = 1;
	private static final int TASK_TEXT = 2;

	private final int[][] id = {
			{R.id.todoWidget_IdTask1, R.id.todoWidget_PrioTask1, R.id.todoWidget_TextTask1},
			{R.id.todoWidget_IdTask2, R.id.todoWidget_PrioTask2, R.id.todoWidget_TextTask2},
			{R.id.todoWidget_IdTask3, R.id.todoWidget_PrioTask3, R.id.todoWidget_TextTask3},
			{R.id.todoWidget_IdTask4, R.id.todoWidget_PrioTask4, R.id.todoWidget_TextTask4}
	};
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		//receive intent and update widget content
		if (Constants.INTENT_WIDGET_UPDATE.equals(intent.getAction())){
			Log.d(TAG, "Update widget intent received ");
			updateWidgetContent(context, AppWidgetManager.getInstance(context), null, null);
		}
	}
	
	private void updateWidgetContent(Context context, AppWidgetManager appWidgetManager, int[] widgetIds, RemoteViews remoteViews) {
		
		Log.d(TAG, "Updating TodoWidgetProvider content.");
		
		//get widget ID's if not provided
		if (widgetIds == null){
			widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TodoWidgetProvider.class.getName()));
		}
		
		//get remoteViews if not provided
		if (remoteViews == null){
			remoteViews = new RemoteViews( context.getPackageName(), R.layout.widget );
		}
		
		//get taskBag from application
		TaskBag taskBag = ((TodoApplication)((ContextWrapper)context).getBaseContext()).getTaskBag();
		
		List<Task> tasks = taskBag.getTasks();
		int taskCount = tasks.size();
		Resources resources = context.getResources();
		
		
		for (int i = 0; i < TASKS_TO_DISPLAY; i++) {
			
			//get task to display
			if (i >= tasks.size()){
				// no more tasks to display
				remoteViews.setViewVisibility(id[i][TASK_ID], View.GONE);
				remoteViews.setViewVisibility(id[i][TASK_PRIO], View.GONE);
				remoteViews.setViewVisibility(id[i][TASK_TEXT], View.GONE);
				//remoteViews.setViewVisibility(id[i][TASK_ID], View.GONE); //add task age later
				continue;
			}
			Task task = tasks.get(i);

			
			
			//id
			remoteViews.setTextViewText(id[i][TASK_ID], String.format("%02d", task.getId() + 1));
			remoteViews.setTextColor(id[i][TASK_ID], resources.getColor(R.color.black));
			remoteViews.setViewVisibility(id[i][TASK_ID], View.VISIBLE);
			
			//text
			//TODO: use styles from widget theme, like in main task list display
			SpannableString ss = new SpannableString(task.inScreenFormat());
			Util.setColor(ss, task.getProjects(), Color.GRAY);
			Util.setColor(ss, task.getContexts(), Color.GRAY);
			remoteViews.setTextViewText(id[i][TASK_TEXT], ss);
			remoteViews.setViewVisibility(id[i][TASK_TEXT], View.VISIBLE);
			
			//priority
			//TODO: use styles from widget theme, like in main task list display
			int color = R.color.white;			
			switch (task.getPriority()) {
				case A: color = R.color.green; break;
				case B:	color = R.color.blue; break;
				case C: color = R.color.orange;	break;
				case D:	color = R.color.gold;
			}
					
			remoteViews.setTextViewText(id[i][TASK_PRIO], task.getPriority().inListFormat());
			remoteViews.setTextColor(id[i][TASK_PRIO], resources.getColor(color));
			remoteViews.setViewVisibility(id[i][TASK_PRIO], View.VISIBLE);
			
			
			//TODO: do wee need to display completed tasks on widget? IMO not
			if (task.isCompleted()) {
				Log.v(TAG, "Striking through " + task.getText());
				ss.setSpan(new StrikethroughSpan(), 0, ss.length(), SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
				//holder.tasktext.setPaintFlags(holder.tasktext.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}

			// hide line numbers unless show preference is checked
			/*if (!app.m_prefs.getBoolean("showlinenumberspref", false)) {
				remoteViews.setViewVisibility(taskid, View.GONE);
			} else {
				remoteViews.setViewVisibility(taskid, View.VISIBLE);
			}*/

			/*if (app.m_prefs.getBoolean("show_task_age_pref", false)) {
				if (!task.isCompleted()
						&& !Strings.isEmptyOrNull(task.getRelativeAge())) {
					remoteViews.setTextViewText(taskage, task.getRelativeAge());
					remoteViews.setViewVisibility(taskage, View.VISIBLE);
				} else {
					remoteViews.setTextViewText(taskage, "");
					remoteViews.setViewVisibility(taskage, View.GONE);
					holder.tasktext.setPadding(
							holder.tasktext.getPaddingLeft(),
							holder.tasktext.getPaddingTop(),
							holder.tasktext.getPaddingRight(), 4);
				}
			} else {
				holder.tasktext.setPadding(
						holder.tasktext.getPaddingLeft(),
						holder.tasktext.getPaddingTop(),
						holder.tasktext.getPaddingRight(), 4);
			}*/
		}
		
		remoteViews.setViewVisibility(R.id.empty, taskCount == 0 ? View.VISIBLE : View.GONE);
		appWidgetManager.updateAppWidget(widgetIds, remoteViews);
		
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		RemoteViews remoteViews = new RemoteViews( context.getPackageName(), R.layout.widget );
		
		Intent intent = new Intent(context, LoginScreen.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent( R.id.widget_launchbutton, pendingIntent);
		
		intent = new Intent(context, AddTask.class);
		pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_addbutton, pendingIntent);
		
		updateWidgetContent(context, appWidgetManager, appWidgetIds, remoteViews);

	}
}
