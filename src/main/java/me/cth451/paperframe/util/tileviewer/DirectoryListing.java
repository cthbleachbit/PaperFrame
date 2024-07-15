package me.cth451.paperframe.util.tileviewer;

import com.google.gson.Gson;

import java.time.Instant;
import java.util.LinkedList;

public class DirectoryListing {
	public LinkedList<String> directories;
	public LinkedList<String> tilesets;

	transient Instant created = null;

	public DirectoryListing() {
		this.directories = new LinkedList<>();
		this.tilesets = new LinkedList<>();
		this.created = Instant.now();
	}

	static DirectoryListing fromJson(String json) {
		return new Gson().fromJson(json, DirectoryListing.class);
	}
}
