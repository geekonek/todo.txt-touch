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
package com.todotxt.todotxttouch.remote;

import android.content.SharedPreferences;
import android.util.Log;

import com.todotxt.todotxttouch.TodoApplication;

/**
 * Manager for obtaining, switching, etc. remote clients
 * 
 * @author Tim Barlotta
 */
public class RemoteClientManager implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private final static String TAG = RemoteClientManager.class.getSimpleName();
	
	private Client currentClientToken;
	private RemoteClient currentClient;
	private TodoApplication todoApplication;
	private SharedPreferences sharedPreferences;

	public RemoteClientManager(TodoApplication todoApplication,
			SharedPreferences sharedPreferences) {
		this.todoApplication = todoApplication;
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		this.sharedPreferences = sharedPreferences;
		calculateRemoteClient();
		if (currentClient != null){
			currentClient.authenticate();
		}
	}

	public RemoteClient getRemoteClient() {
		return currentClient;
	}

	/**
	 * Returns the client associated with the passed in token does not switch
	 * the client
	 * 
	 * @param clientToken
	 * @return
	 */
	private RemoteClient getRemoteClient(Client clientToken) {
		switch(clientToken){
			case DROPBOX: return new DropboxRemoteClient(todoApplication, sharedPreferences);
			case LOCAL: return null;
			case SYNCTXT: return null;
		}
		
		return null;
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO later
	}

	public void calculateRemoteClient() {
		String provider = sharedPreferences.getString("remove_provider_type", null);
		if (provider != null){
			currentClientToken = Client.valueOf(provider);
			currentClient = getRemoteClient(currentClientToken);
		}
	}

	public void setRemoteType(String selectedItem) {
		String provider = sharedPreferences.getString("remove_provider_type", null);
		String selectedProvider = selectedItem.equalsIgnoreCase("Dropbox") ? Client.DROPBOX.name() : 
			(selectedItem.equalsIgnoreCase("Local") ? Client.LOCAL.name() : Client.SYNCTXT.name());
		if (provider == null || provider != selectedProvider){
			Log.d(TAG, "Saving provider selection to: "+selectedProvider);
			sharedPreferences.edit().putString("remove_provider_type", selectedProvider).apply();
		}
		Log.d(TAG, "Setting client type to "+selectedProvider);
		
		calculateRemoteClient();
		if (currentClient != null){
			currentClient.authenticate();
		}
	}
}