package com.facelabel.data_model;

public class MemberInfo {
	
	private long groupId;
	private long id;
	private String name;
	private String photo;
	private String phone;
	private String email;
	
	public MemberInfo(long groupId, long id, String name, String photo, String phone, String email) {
		this.groupId = groupId;
		this.id = id;
		this.name = name;
		this.photo = photo;
		this.phone = phone;
		this.email = email;
	}
	
	public long getGroupId() {
		return groupId;
	}
	
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhoto() {
		return photo;
	}
	
	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}
	
	public void updateInfo(String... newInfo) {
		this.name = newInfo[0];
		this.phone = newInfo[1];
		this.email = newInfo[2];
	}
}
