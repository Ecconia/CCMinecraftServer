package de.ecconia.mcserver;

public class Logger
{
	public static void debug(String message)
	{
		System.out.println("[Debug]" + message);
	}
	
	public static void info(String message)
	{
		System.out.println("[Info]" + message);
	}
	
	public static void warning(String message)
	{
		System.out.println("[Warning]" + message);
	}
	
	public static void warning(String message, Throwable e)
	{
		System.out.println("[Warning-TODO!]" + message);
		e.printStackTrace(System.out);
	}
	
	public static void error(String message)
	{
		System.out.println("[Error]" + message);
	}
	
	public static void errorShort(String message, Throwable e)
	{
		System.out.println("[Error-Short-TODO!]" + message);
		e.printStackTrace(System.out);
	}
	
	public static void error(String message, Throwable e)
	{
		System.out.println("[Error-TODO!]" + message);
		e.printStackTrace(System.out);
	}
}
