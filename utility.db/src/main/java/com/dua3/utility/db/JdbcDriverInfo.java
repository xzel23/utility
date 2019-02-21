package com.dua3.utility.db;

import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionSet;

public class JdbcDriverInfo {

	public final String name;
	public final String className;
	public final String urlPrefix;
	public final String urlScheme;
	public final String link;	
	public final OptionSet options;

	public JdbcDriverInfo(String name, String className, String urlPrefix, String urlScheme, String link) {
		this.name = name;
		this.className = className;
		this.urlPrefix = urlPrefix;
		this.urlScheme = urlScheme;
		this.link = link;
		
		this.options = new OptionSet(Option.parseScheme(urlScheme));
	}
	
}
