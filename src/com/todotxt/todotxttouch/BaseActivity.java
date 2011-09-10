/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/BaseActivity.java
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
package com.todotxt.todotxttouch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends Activity {

	protected TodoApplication m_app;
	private static String TAG = BaseActivity.class.getName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		m_app = (TodoApplication) getApplication();
		
		String theme = m_app.m_prefs.getString(Constants.PREF_CURRENT_THEME, "WHITE");
		Log.d(TAG, "\n\nSetting theme: "+theme+"\n\n");
		int themeId = getResources().getIdentifier("@style/TodoTheme."+theme, null, getPackageName());
		m_app.setTheme(themeId);
		this.setTheme(themeId);
		
	}

	
	
}
