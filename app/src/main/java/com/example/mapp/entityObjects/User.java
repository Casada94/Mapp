package com.example.mapp.entityObjects;


import java.util.ArrayList;

public class User {
	private String name;
	private String email;
	private String password;
	private Schedule schedule;
	private boolean isAdmin;
	
	public User(String name, String email, String password, boolean isAdmin)
	{
		this.name = name;
		this.email = email;
		this.password = password;
		this.schedule = new Schedule();
		this.isAdmin = isAdmin;
	}
	
	public void addClass(Course C)
	{
		this.schedule.addCourse(C);
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getEmail()
	{
		return email;
	}
	public void setEmail(String email)
	{
		this.email = email;
	}
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public Schedule getSchedule()
	{
		return schedule;
	}
	public boolean isAdmin()
	{
		return isAdmin;
	}
}
