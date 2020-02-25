package com.example.mapp.entityObjects;

import java.sql.Time;

public class Course{
	private String courseName;
	private String classroom;
	public String professor;
	private Time startTime;
	private Time endTime;
	public Course(String courseName, String classroom, String Professor, Time startTime, Time endTime)
	{
		this.courseName = courseName;
		this.classroom = classroom;
		this.professor = professor;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	public String getCourseName()
	{
		return courseName;
	}		
	public void setCourseName(String courseName)
	{
		this.courseName = courseName;
	}
	public String getClassroom()
	{
		return classroom;
	}
	public void setClassroom(String classroom)
	{
		this.classroom = classroom;
	}
	public String getProfessor()
	{
		return professor;
	}
	public void setProfessor(String professor)
	{
		this.professor = professor;
	}
	public Time getStartTime()
	{
		return startTime;
	}
	public void setStartTime(Time startTime)
	{
		this.startTime = startTime;
	}
	public Time getEndTime()
	{
		return endTime;
	}
	public void setEndTime(Time endTime)
	{
		this.endTime = endTime;
	}
}