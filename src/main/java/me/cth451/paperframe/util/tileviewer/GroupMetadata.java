package me.cth451.paperframe.util.tileviewer;

import com.google.gson.Gson;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;

public class GroupMetadata {
	public String name;
	public LinkedList<LinkedList<Integer>> geometry;
	public String description;
	public HashMap<String, String> links;
	public UsageHints usageHints;

	transient Instant created = null;

	GroupMetadata() {
		created = Instant.now();
	}

	static GroupMetadata fromJson(String json) {
		return new Gson().fromJson(json, GroupMetadata.class);
	}
}
