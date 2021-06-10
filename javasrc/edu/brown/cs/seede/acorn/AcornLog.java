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


import edu.brown.cs.ivy.file.IvyLog;



public class AcornLog extends IvyLog
{


/********************************************************************************/
/*										*/
/*	Control methods 							*/
/*										*/
/********************************************************************************/

public static void setup()
{
   setupLogging("SEEDE",true);
}



/********************************************************************************/
/*										*/
/*	Execution trace entries 						*/
/*										*/
/********************************************************************************/

public static void logT(String msg)
{
   logT("EXEC",msg);
}





}	// end of class AcornLog




/* end of AcornLog.java */

