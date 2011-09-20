/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/FileBasedRemoteClient.java
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
 * @copyright 2011 Tomasz Roszko
 */

package com.todotxt.todotxttouch.remote;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Environment;

import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.util.TaskIo;

public abstract class FileBasedRemoteClient implements RemoteClient{

	private SharedPreferences preferences;

	public abstract File pullTodoFile();
	public abstract void pushTodoFile(File file);
	
	public FileBasedRemoteClient(SharedPreferences preferences){
		this.preferences = preferences;
	}
	
	@Override
	public final List<Task> pullTodo() throws IOException {
		
		File file = pullTodoFile();
		if (file != null && file.exists()){
			return TaskIo.loadTasksFromFile(file);
		}
		
		return null;
	}

	@Override
	public final void pushTodo(List<Task> tasks) {
		File tmpFile = new File( Environment.getExternalStorageDirectory(), 
				"data/com.todotxt.todotxttouch/tmp/file_provider_todo.txt");
		TaskIo.writeToFile(tasks, tmpFile, isUseWindowsLineBreaks());
		pushTodoFile(tmpFile);
	}
	
	
	private boolean isUseWindowsLineBreaks() {
		return preferences.getBoolean("linebreakspref", false);
	}

}
