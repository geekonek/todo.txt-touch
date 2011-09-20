/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/LocalTaskRepository.java
 *
 * Copyright (c) 2011 Tim Barlotta, Tomasz Roszko
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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @author Tomasz Roszko <geekonek[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta, Tomasz Roszko
 */

package com.todotxt.todotxttouch.task;

import java.util.List;

/**
 * A repository for tasks working at the local data store level
 * 
 * @author Tim Barlotta
 * @author Tomasz Roszko
 */
interface LocalTaskRepository {
	void init();

	void purge();

	List<Task> load();

	void store(List<Task> tasks);
}
