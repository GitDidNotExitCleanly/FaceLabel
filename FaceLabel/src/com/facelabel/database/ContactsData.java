package com.facelabel.database;

import java.util.ArrayList;

import com.facelabel.data_model.GroupInfo;

public class ContactsData {
	
	private static ArrayList<GroupInfo> contacts = new ArrayList<GroupInfo>();

	public static ArrayList<GroupInfo> getContacts() {
		return contacts;
	}
}
