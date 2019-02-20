package com.dua3.utility.db;

public class JdbcDriverInfo {

	public final String name;
	public final String className;
	public final String urlPrefix;
	public final String urlScheme;
	public final String link;

	public JdbcDriverInfo(String name, String className, String urlPrefix, String urlScheme, String link) {
		this.name = name;
		this.className = className;
		this.urlPrefix = urlPrefix;
		this.urlScheme = urlScheme;
		this.link = link;
	}

}
