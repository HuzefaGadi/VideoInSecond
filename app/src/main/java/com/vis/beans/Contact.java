package com.vis.beans;

import android.graphics.Bitmap;

public class Contact {
	
	private String name;
	private String number;
	private String email;
	private Bitmap image;
	
	
	public Contact() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Contact(String name, String number, String email, Bitmap image) {
		super();
		this.name = name;
		this.number = number;
		this.email = email;
		this.image = image;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	

}
