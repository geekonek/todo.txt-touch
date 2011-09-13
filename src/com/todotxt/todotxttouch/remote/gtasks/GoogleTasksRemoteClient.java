/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/gtasks/GoogleTasksRemoteClient.java
 *
 * Copyright (c) 2011 Tomasz Roszko
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
 * @author Tomasz Roszko <geekonek[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tomasz Roszko
 */
package com.todotxt.todotxttouch.remote.gtasks;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.Tasks.TasksOperations.List;
import com.google.api.services.tasks.model.Task;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.Client;
import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.remote.RemoteLoginTask;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.util.TaskMetadata;
import com.todotxt.todotxttouch.util.Util;

public class GoogleTasksRemoteClient implements RemoteClient {
	
	private static final String RESOURCE_ALIAS = "Manage your tasks";
	private static final String GOOGLE_TASKS_TOKEN = "com.todotxt.provider.gtasks.token";
	private static final String GOOGLE_TASKS_ACCOUNT = "com.todotxt.provider.gtasks.account";
	private static final String GOOGLE_TASKS_LAST_SYNC = "com.todotxt.provider.gtasks.lastSync";
	private static final String GOOGLE_TASKS_LAST_PUSH = "com.todotxt.provider.gtasks.lastPush";
	
	private static final String TAG = GoogleTasksRemoteClient.class.getName();
	
	private TodoApplication todoApplication;
	private SharedPreferences sharedPreferences;
	
	private GoogleAccountManager accountManager;
	private HttpTransport transport;
	private GoogleAccessProtectedResource accessProtectedResource;
	private Tasks tasksService;
	
	private String token;
	private Account account;
	
	private SimpleDateFormat dateFormat;
	private long lastSync =-1;
	private long lastPush = -1;
	private TaskBag taskBag;
	
	public GoogleTasksRemoteClient(TodoApplication todoApplication,
			SharedPreferences sharedPreferences, TaskBag taskBag) {
		
		this.dateFormat = new SimpleDateFormat(com.todotxt.todotxttouch.task.Task.DATE_FORMAT);
		
		this.todoApplication = todoApplication;
		this.sharedPreferences = sharedPreferences;
		this.taskBag = taskBag;
		this.accountManager = new GoogleAccountManager(todoApplication);
		this.transport = AndroidHttp.newCompatibleTransport();
		this.accessProtectedResource = new GoogleAccessProtectedResource(null);
		this.tasksService = new Tasks(transport, accessProtectedResource, new JacksonFactory());
		tasksService.setKey(todoApplication.getResources().getString(R.string.google_tasks_api_key));
		tasksService.setApplicationName("todo.txt.test/1.0");
		
		//get token and account setting from preferences
		this.token = sharedPreferences.getString(GOOGLE_TASKS_TOKEN, null);
		this.lastSync = sharedPreferences.getLong(GOOGLE_TASKS_LAST_SYNC, -1);
		this.lastPush = sharedPreferences.getLong(GOOGLE_TASKS_LAST_PUSH, -1);
		String accountName = sharedPreferences.getString(GOOGLE_TASKS_ACCOUNT, null);
		
		if(accountName != null){
			this.account = accountManager.getAccountByName(accountName); //TODO: handle account not found exception
		}
		
	}

	@Override
	public boolean isAvailable() {
		return true; //TODO: only true if user have at least one google account
	}
	
	void sendBroadcast(Intent intent) {
		todoApplication.sendBroadcast(intent);
	}
	
	private void setLastSyncTimestamp(){
		lastSync = System.currentTimeMillis();
		Editor editor = sharedPreferences.edit();
		editor.putLong(GOOGLE_TASKS_LAST_SYNC, lastSync);
		editor.commit();//TODO: check if succesfull?
	}
	
	private void setLastPushTimestamp(){
		lastPush = new Date().getTime();
		Editor editor = sharedPreferences.edit();
		editor.putLong(GOOGLE_TASKS_LAST_PUSH, lastPush);
		editor.commit();//TODO: check if succesfull?
	}
	
	
	@Override
	public Client getClient() {
		return Client.GTASKS;
	}

	@Override
	public boolean authenticate() {
		return token != null; //TODO: rethink this, maybe we need to check if token still valid
	}

	@Override
	public void deauthenticate() {
		if (token != null){
			accountManager.invalidateAuthToken(token);
		}
		accessProtectedResource.setAccessToken(null);
		
		Editor editor = sharedPreferences.edit();
		editor.remove(GOOGLE_TASKS_TOKEN);
		editor.remove(GOOGLE_TASKS_ACCOUNT);
		editor.commit();
		//we should be deauthenticated now
	}

	@Override
	public boolean isAuthenticated() {
		Log.d(TAG, "AUTH TOKEN: "+token+"");//FIXME: DO NOT LOG SECURITY TOKENS :)!!!
		
		if (account == null){
			return false;
		}
		
		if (!checkIfTokenValid(null)){
			return reAuthenticateWithGoogle();
		}
		return true;
	}

	//This method runs authentication synchronously
	private boolean reAuthenticateWithGoogle() {
		Log.d(TAG, "Starting authentication...");
		if (account == null){
			return false;
		}
		AccountManagerFuture<Bundle> ret = accountManager.manager.getAuthToken(account, RESOURCE_ALIAS, true, null, null); 
		try {
			Bundle bundle = ret.getResult();
		    if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)){
		    	token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
		    	accessProtectedResource.setAccessToken(token);
		    	return checkIfTokenValid(null);
		    }
	    } catch (OperationCanceledException e) {
	    	//noop - user denied access
	    	Log.e(TAG, "Operation cancelled", e);
	    } catch (Exception e) {
	    	Util.showToastLong(todoApplication, 
	    		"Something wrong happened while authenticating with google tasks. Please report this issue");
	    	Log.e(TAG, "Exception while authenticating with google service", e);
	    }
		return false;
	}

	@Override
	public boolean isLoggedIn() {
		//deprecated
		return token != null;
	}

	@Override
	public RemoteLoginTask getLoginTask() {
		
		if (token != null){
			//just to be sure
			deauthenticate();
		}
		
		return new RemoteLoginTask() {
			@Override
			public void showLoginDialog(Activity act) {
				
				//get available google accounts
				Account[] accounts = accountManager.getAccounts();
				Log.d(TAG, "Found "+accounts.length+" google accounts available.");
				
				if (accounts.length > 0){
					//if more than one account, allow user to pick one
					showAccountSelectDialog(accounts, act);
				} else if (accounts.length == 1){
					//only one account present, no need to select
					Editor editor = sharedPreferences.edit();
					editor.putString(GOOGLE_TASKS_ACCOUNT, accounts[0].name);
					editor.commit();
					authenticateWithGoogle(act);
				} else {
					//we don't have any google accounts, show message and do nothing
					Util.showToastLong(act, "You do not have any google account connected!");
				}
			}
		};
	}

	private void authenticateWithGoogle(final Activity act) {
		
		Log.d(TAG, "Showing authenticating dialog...");
		final AlertDialog dialog = act != null ? new AlertDialog.Builder(act).setTitle("Connecting do google tasks")
				.setMessage("Please wait while Todo.txt authenticates with your google account").show() : //TODO: externalize strings
				null; 
					
		Log.d(TAG, "Starting authentication...");
		accountManager.manager.getAuthToken(account, RESOURCE_ALIAS, true, 
			new AccountManagerCallback<Bundle>() {
	    		public void run(AccountManagerFuture<Bundle> future) {
	    			try {
	    				Log.d(TAG, "Got response from google...");
	    				Bundle bundle = future.getResult();
	    				/*if (bundle.containsKey(AccountManager.KEY_INTENT)){
	    					Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
	    					intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
	    	                act.startActivityForResult(intent, 0);
	    				} else*/ 
	    				if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)){
	    					token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
	    					accessProtectedResource.setAccessToken(token);
	    					if (checkIfTokenValid(act)){
	    						sendBroadcast(new Intent("com.todotxt.todotxttouch.ACTION_LOGIN"));
	    					}
	    				}
	    			} catch (OperationCanceledException e) {
	    				//noop - user denied access
	    				Log.e(TAG, "Operation cancelled", e);
	    			} catch (Exception e) {
	    				Toast.makeText(todoApplication, 
	    						"Something wrong happened while authenticating with google tasks. Please report this issue", 
	    						Toast.LENGTH_LONG).show();
	    				Log.e(TAG, "Exception while authenticating with google service", e);
	    			} finally {
	    				if (dialog != null) {
	    					dialog.dismiss();
	    				}
	    			}
	    		}
		}, null);
	}
	
	
	private void showAccountSelectDialog(final Account[] accounts, final Activity act) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setTitle("Select a Google account");
		final int size = accounts.length;
		String[] names = new String[size];
		for (int i = 0; i < size; i++) {
			names[i] = accounts[i].name;
		}
		builder.setItems(names, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
				account = accounts[which];
				Editor editor = sharedPreferences.edit();
				editor.putString(GOOGLE_TASKS_ACCOUNT, account.name);
				editor.commit();
				authenticateWithGoogle(act);
			}
		});
		builder.create().show();
	}
	

	@Override
	public void pushTodo(java.util.List<com.todotxt.todotxttouch.task.Task> tasks, boolean anotherBooleanThatShouldNotBePassedThisWay) {
		Log.d(TAG,"\n\n\n************** PUSH TODO ************\n\n\n");
		try {
		
			for (com.todotxt.todotxttouch.task.Task task : tasks) {
				Log.d(TAG, "Checking task: "+task);
				
				if (task.getExternalId() == null){
					//this is new (unsynchronized task)
					Log.d(TAG, "Pushing new task");
					pushNewGTask(task);
				} else {
					//this is existing task, need to find a way to detect if it was changed without last modified date
					Log.d(TAG, "Comparing modDate: "+task.getModDate()+" with "+ lastPush);
					if (task.getModDate() == null || task.getModDate() > lastPush){
						Log.d(TAG, "Found modified task, updating google version");
						Task gTask = tasksService.tasks.get("@default", task.getExternalId()).execute();
						if (task == null){
							Log.d(TAG, "Task not found on google, pushing");
							pushNewGTask(task);
						} else {
							fillGTask(gTask, task);
							tasksService.tasks.update("@default", task.getExternalId(), gTask).execute();
							task.setModDate(System.currentTimeMillis());
							taskBag.update(task);
						}
					}
					
				}
				
				//TODO: find deleted tasks as well
				
				
			}
			setLastPushTimestamp();
		} catch (IOException e) {
			Log.e(TAG, "Exception while pushing task to google.", e);
		} 
	}

	private String pushNewGTask(com.todotxt.todotxttouch.task.Task task)
			throws IOException {
		Task gTask = new Task();
		fillGTask(gTask, task);
		
		String id = tasksService.tasks.insert("@default", gTask).execute().getId();
		task.setExternalId(id);
		task.setModDate(System.currentTimeMillis());
		taskBag.update(task);
		return id;
	}

	

	

	private void fillGTask(Task gTask, com.todotxt.todotxttouch.task.Task task) {
		
		TaskMetadata meta = new TaskMetadata(task.inFileFormat());
		
		gTask.setTitle(meta.getStrippedText()); //TODO: maybe in screen format?
		//gTask.setNotes(meta.getMetaInFileFormat()); //We do not need to store this as string on google
		String prependedDate = task.getPrependedDate();
		if (prependedDate != null && prependedDate.length() > 0){
			try {
				gTask.setDue(new DateTime(dateFormat.parse(prependedDate), TimeZone.getDefault()));
				Log.d(TAG, "SET dueDate to "+gTask.getDue());
			} catch (ParseException e) {
				Log.e(TAG, "Exception while trying to parse task prepended date.", e);
			}
		}
		
		String completionDate = task.getCompletionDate();
		if (task.isCompleted() && completionDate != null && completionDate.length() > 0){
			try {
				gTask.setCompleted(new DateTime(dateFormat.parse(completionDate), TimeZone.getDefault()));
				gTask.setStatus("completed");
				Log.d(TAG, "SET completed to "+gTask.getCompleted());
			} catch (ParseException e) {
				Log.e(TAG, "Exception while trying to parse task completed date.", e);
			}
		} else {
			gTask.setStatus("needsAction");
		}
		//Log.d(TAG, "Filled GTask with: "+gTask); - crashes - bug in gTask
	}

	@Override
	public java.util.List<com.todotxt.todotxttouch.task.Task> pullTodo() {
		Log.d(TAG,"\n\n\n************** PULL TODO ************\n\n\n");
		
		try { 
			java.util.List<com.todotxt.todotxttouch.task.Task> todoTasks = taskBag.getTasks();
			java.util.List<Task> tasks = tasksService.tasks
					.list("@default")  //TODO: add support for different lists, or create dedicated one
					.setUpdatedMin(new DateTime(new Date(lastSync), TimeZone.getDefault()).toStringRfc3339())
					.setShowDeleted(true)  //get recently deleted tasks as well
					.execute().getItems();
			
			if (tasks != null){
				for (final Task gtask : tasks) {

					Log.d(TAG, "Processing task: "+gtask);
					if (gtask == null || gtask.getTitle() == null || gtask.getTitle().length() <=0){
						Log.d(TAG, "Task was null, continuing");
						continue;
					}
					
					if (gtask.getDeleted() != null && gtask.getDeleted()){
						Log.d(TAG,"Removing deleted task: "+gtask);
						com.todotxt.todotxttouch.task.Task currentTask = findTask(todoTasks, gtask.getId());
						if (currentTask != null){
							todoTasks.remove(currentTask);
						}
						continue;
					}
					
					//task is modified/added
					
					//find task
					com.todotxt.todotxttouch.task.Task existingTask = findTask(todoTasks, gtask.getId()); //FIXME: fill and use map, we need at least one iteration
					if (existingTask != null){
						Log.d(TAG, "Found existing local task with this externalId, updating");
						//update existing task
						com.todotxt.todotxttouch.task.Task newTask = cloneAndFillTask(existingTask, gtask);
						int idx = todoTasks.indexOf(existingTask);
						todoTasks.remove(idx);
						todoTasks.add(idx, newTask);
						
					} else {
						//insert new task
						Log.d(TAG, "Creating new local task");
					
						com.todotxt.todotxttouch.task.Task todoTask = new com.todotxt.todotxttouch.task.Task(todoTasks.size()+1, gtask.getTitle());
						todoTasks.add(cloneAndFillTask(todoTask, gtask));
					}
				}
			}
			setLastSyncTimestamp();
			return todoTasks;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	

	private com.todotxt.todotxttouch.task.Task cloneAndFillTask(final com.todotxt.todotxttouch.task.Task srcTask,
			Task gtask) {
		
		com.todotxt.todotxttouch.task.Task newTask = new com.todotxt.todotxttouch.task.Task(srcTask.getId(), 
				gtask.getTitle()+(gtask.getNotes() == null ? "" : " " + gtask.getNotes()));
		
		if (gtask.getDue() != null){
			Log.d(TAG, "Setting due date to: "+gtask.getDue());
			newTask.setPrependedDate(dateFormat.format(new Date(gtask.getDue().getValue())));
		}
		if (gtask.getCompleted() != null){
			Log.d(TAG, "Setting completed date to: "+gtask.getCompleted());
			newTask.setCompleted(true);
			newTask.setCompletionDate(dateFormat.format(new Date(gtask.getCompleted().getValue())));
		}
		if (gtask.getId() != null){
			Log.d(TAG, "Setting external id to: "+gtask.getId());
			newTask.setExternalId(gtask.getId());
		}
		
		return newTask;
	}

	private com.todotxt.todotxttouch.task.Task findTask(
			java.util.List<com.todotxt.todotxttouch.task.Task> todoTasks,
			String id) {
		Log.d(TAG, "Searching for existing task with externalID = "+id);
		if (id != null){
			for (com.todotxt.todotxttouch.task.Task task : todoTasks) {
				//Log.d(TAG, "id="+id+" ->  externalId="+task.getExternalId());
				if (task.getExternalId() != null && id.equals(task.getExternalId())){
					return task;
				}
			}
		}
		return null;
	}

	public boolean checkIfTokenValid(Activity act) {

		Log.d(TAG, "Checking if token valid: "+token); //FIXME: DO NOT LOG SECURITY STUFF !!!
		
		List list = tasksService.tasks.list("@default");
		list.setUpdatedMin(new DateTime( 				//to limit traffic, 
				new Date(System.currentTimeMillis()),   //we query only for tasks 
				TimeZone.getDefault())					//edited in the future, there should be none :)
			.toStringRfc3339());
		
		try {
			//just check if query succesfull
			list.execute().getItems();
			
			//save token in preferences
			Editor editor = sharedPreferences.edit();
			editor.putString(GOOGLE_TASKS_TOKEN, token);
			editor.commit();
			return true;
			
		} catch (IOException e) {
			Log.e(TAG, "Exception while getting tasks list", e);
			if (e instanceof HttpResponseException) {
				HttpResponse response = ((HttpResponseException) e).getResponse();
				int statusCode = response.getStatusCode();
				try {
					response.ignore();
				} catch (IOException e1) {
			       	Log.e(TAG, "Exception while ignoring google error response", e);
				}
			      
				if (statusCode == 401) {
					//token expired, reauthenticate
					Log.d(TAG, "We got 401 code from google api, token expired, invalidating it.");
		    		accountManager.invalidateAuthToken(token);
			    	accessProtectedResource.setAccessToken(null);
				}
			}
			return false;
		}  
	}
}
