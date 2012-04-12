package com.todotxt.todotxttouch.remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Environment;

import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskPersistException;
import com.todotxt.todotxttouch.util.TaskIo;

public abstract class RemoteFileClient implements RemoteClient{

	public abstract File pullTodoFile();
	
	protected final SharedPreferences sharedPreferences;

	public RemoteFileClient(SharedPreferences sharedPreferences) {
		this.sharedPreferences = sharedPreferences;
	}
	
	@Override
	public ArrayList<Task> pullTodo(){
		File remoteFile = pullTodoFile();
		if (remoteFile != null && remoteFile.exists()){
			try {
				return TaskIo.loadTasksFromFile(remoteFile);
			} catch (IOException e) {
				throw new TaskPersistException("Error loading tasks from remote file", e);
			}
		}
		return null;
	}

	public abstract void pushTodoFile(File file);
	
	@Override
	public void pushTodo(ArrayList<Task> tasks) {
		File tmpFile;
		try {
			tmpFile = File.createTempFile("tmp_", ".txt", new File(Environment.getExternalStorageDirectory(), "data/com.todotxt.todotxttouch/tmp/"));
			TaskIo.writeToFile(tasks, tmpFile, sharedPreferences.getBoolean("linebreakspref", false));
			pushTodoFile(tmpFile);
		} catch (IOException e) {
			throw new TaskPersistException("Error storing tasks in remote file", e);
		}
	}

	
	
}
