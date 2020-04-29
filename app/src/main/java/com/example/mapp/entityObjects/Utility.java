package com.example.mapp.entityObjects;

import java.util.ArrayList;

public class Utility extends point{
	enum Type
	{
		VENDING_MACHINE, BATHROOM, WATER_FOUNTAIN;
	}
	private Type type;
	private ArrayList<String> contents;
	private Building building;

	public Utility()
	{
		super();
		contents = new ArrayList<String>();
	}

	public Utility(Type type)
	{
		super();
		this.type = type;
		contents = new ArrayList<String>();
	}
	public String getName(){ return "water fountain"; }
	public void addContent(String content)
	{
		contents.add(content);
	}
	public ArrayList<String> getContents()
	{
		return contents;
	}
	public Type getType()
	{
		return type;
	}
}
