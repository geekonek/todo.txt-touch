/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
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
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.remote.local;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.Client;
import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.task.Task;

public class DummyLocalClient implements RemoteClient {

	private static final String LOCAL_PROVIDER_AUTHENTICATED = "local_provider_authenticated";
	private TodoApplication todoApplication;
	private SharedPreferences sharedPreferences;

	public DummyLocalClient(TodoApplication todoApplication,
			SharedPreferences sharedPreferences) {
		this.todoApplication = todoApplication;
		this.sharedPreferences = sharedPreferences;
	}

	@Override
	public Client getClient() {
		return Client.LOCAL;
	}

	@Override
	public boolean authenticate() {
		sharedPreferences.edit().putBoolean(LOCAL_PROVIDER_AUTHENTICATED, true).commit();
		Intent broadcastLoginIntent = new Intent("com.todotxt.todotxttouch.ACTION_LOGIN");
		todoApplication.sendBroadcast(broadcastLoginIntent);
		return true;
	}

	@Override
	public void deauthenticate() {
		sharedPreferences.edit().putBoolean(LOCAL_PROVIDER_AUTHENTICATED, false).commit();
	}

	@Override
	public boolean isAuthenticated() {
		return sharedPreferences.getBoolean(LOCAL_PROVIDER_AUTHENTICATED, false);
	}

	@Override
	public boolean isLoggedIn() {
		return true;
	}

	@Override
	public boolean startLogin() {
		return true;
	}

	@Override
	public boolean finishLogin() {
		return true;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public List<Task> pullTodo() {
		return todoApplication.getTaskBag().getTasks();
	}

	@Override
	public void pushTodo(List<Task> file) {
		return;
	}

}
