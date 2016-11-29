/********************************************************************************/
/*										*/
/*		AcornLog.java							*/
/*										*/
/*	Logging methods 							*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.seede.acorn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;



public class AcornLog implements AcornConstants
{



/********************************************************************************/
/*										*/
/*	Internal classes							*/
/*										*/
/********************************************************************************/

enum LogLevel {
   ERROR, WARNING, INFO, DEBUG
}


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static LogLevel log_level;
private static boolean	use_stderr;
private static PrintWriter log_writer;

static {
   use_stderr = true;
   log_level = LogLevel.DEBUG;
   log_writer = null;
}


/********************************************************************************/
/*										*/
/*	Logging entries 							*/
/*										*/
/********************************************************************************/

public static void logE(String msg,Throwable t)
{
   log(LogLevel.ERROR,msg,t);
}


public static void logE(String msg)
{
   log(LogLevel.ERROR,msg,null);
}


public static void logW(String msg)
{
   log(LogLevel.WARNING,msg,null);
}


public static void logI(String msg)
{
   log(LogLevel.INFO,msg,null);
}


public static void logD(String msg,Throwable t)
{
   log(LogLevel.DEBUG,msg,t);
}


public static void logD(String msg)
{
   log(LogLevel.DEBUG,msg,null);
}



/********************************************************************************/
/*										*/
/*	Control methods 							*/
/*										*/
/********************************************************************************/

public static void setLogLevel(LogLevel lvl)
{
   log_level = lvl;
}


public static void setLogFile(File f)
{
   try {
      log_writer = new PrintWriter(new FileWriter(f));
    }
   catch (IOException e) {

    }
}


public static void useStdErr(boolean fg)
{
   use_stderr = fg;
}



/********************************************************************************/
/*										*/
/*	Actual logging routines 						*/
/*										*/
/********************************************************************************/

private static void log(LogLevel lvl,String msg,Throwable t)
{
   if (lvl.ordinal() > log_level.ordinal()) return;

   String s = lvl.toString().substring(0,1);
   String pfx = "SEEDE:" + s + ": ";
   if (log_writer != null) {
      log_writer.println(pfx + msg);
      if (t != null) t.printStackTrace(log_writer);
      log_writer.flush();
    }
   if (use_stderr || log_writer == null) {
      System.err.println(pfx + msg);
      if (t != null) t.printStackTrace();
      System.err.flush();
    }
}




}	// end of class AcornLog




/* end of AcornLog.java */

