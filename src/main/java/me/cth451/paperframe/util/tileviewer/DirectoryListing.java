package me.cth451.paperframe.util.tileviewer;

import com.google.gson.Gson;

import java.time.Instant;
import java.util.LinkedList;

/**
 * Response struct for a /api/list-groups call
 */
public class DirectoryListing {
	public LinkedList<String> directories = new LinkedList<>();
	public LinkedList<String> tilesets = new LinkedList<>();

	transient Instant created = null;

	public DirectoryListing() {
		this.created = Instant.now();
	}

	static DirectoryListing fromJson(String json) {
		return new Gson().fromJson(json, DirectoryListing.class);
	}
}
