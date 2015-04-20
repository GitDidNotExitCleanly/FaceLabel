package com.facelabel.data_model;

import java.util.ArrayList;

public class GroupInfo {
	
	private long groupId;
	private String groupName;
	private String groupPhoto;
	private ArrayList<MemberInfo> groupMembers;
	
	public GroupInfo(long groupId, String groupName, String groupPhoto, ArrayList<MemberInfo> groupMembers) {
		this.groupId = groupId;
		this.groupName = groupName;
		this.groupPhoto = groupPhoto;
		if (groupMembers == null) {
			this.groupMembers = new ArrayList<MemberInfo>();
		}
		else {
			this.groupMembers = groupMembers;
		}
	}
	
	public long getGroupId() {
		return groupId;
	}

	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupName(String newName) {
		this.groupName = newName;
	}

	public String getGroupPhoto() {
		return groupPhoto;
	}

	public ArrayList<MemberInfo> getGroupMembers() {
		return groupMembers;
	}
}
