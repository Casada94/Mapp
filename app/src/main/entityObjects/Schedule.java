package entityObjects;

import java.util.ArrayList;

public class Schedule {
	private ArrayList<Course> schedule;
	public Schedule()
	{
		this.schedule = new ArrayList<Course>();
	}
	public void addCourse(Course C)
	{
		schedule.add(C);
	}
	public void removeCourse(Course C)
	{
		schedule.remove(C);
	}
	public ArrayList<Course> getSchedule()
	{
		return schedule;
	}
}
