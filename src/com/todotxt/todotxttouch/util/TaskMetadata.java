/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/util/TaskMetadata.java
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

package com.todotxt.todotxttouch.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskMetadata {

	private final static Pattern EXTERNAL_ID_PATTERN = Pattern.compile(".*\\sid\\:([a-zA-Z0-9]*).*");
	private final static Pattern MOD_DATE_PATTERN = Pattern.compile(".*\\smod\\:(\\d{4}-\\d{2}-\\d{2}_\\d{2}\\:\\d{2}\\:\\d{2}).*");
	private static final String MOD_DATE_FORMAT = "yyyy-MM-dd_hh:mm:ss";
	
	private String externalId;
	private String modDate;
	private String strippedText;
	
	public TaskMetadata(String externalId, Long modDate){
		if (modDate != null){
			this.modDate = new SimpleDateFormat(MOD_DATE_FORMAT).format(new Date(modDate));
		} else {
			this.modDate = null;
		}
		this.externalId = externalId;		
	}
	
	public TaskMetadata(final String textInFileFormat) {
		//text can contain external ID
		strippedText = textInFileFormat;
		Matcher matcher = EXTERNAL_ID_PATTERN.matcher(strippedText);
		if(matcher.matches()){
			externalId = matcher.group(1);
			strippedText = strippedText.substring(0, matcher.start(1)-4) + strippedText.substring(matcher.end(1));
		}
		
		matcher = MOD_DATE_PATTERN.matcher(strippedText);
		if (matcher.matches()){
			modDate = matcher.group(1);
			strippedText = strippedText.substring(0, matcher.start(1)-5) + strippedText.substring(matcher.end(1));
		}
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getModDate() {
		return modDate;
	}

	public void setModDate(String modDate) {
		this.modDate = modDate;
	}

	public String getStrippedText() {
		return strippedText;
	}

	public void setStrippedText(String strippedText) {
		this.strippedText = strippedText;
	}

	public String getMetaInFileFormat() {
		StringBuilder sb = new StringBuilder().append("");
		//append external id if provided
		if (externalId != null){
			sb.append(" id:").append(externalId);
		}
		
		if (modDate != null){
			sb.append(" mod:").append(modDate);
		}
		return sb.toString();
	}

	public static Long getModDate(String modDateString) throws ParseException {
		return new SimpleDateFormat(MOD_DATE_FORMAT).parse(modDateString).getTime();
	}
	
}
