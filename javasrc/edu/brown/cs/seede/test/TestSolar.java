/********************************************************************************/
/*										*/
/*		TestSolar.java							*/
/*										*/
/*	Test seede using SOLAR package						*/
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



package edu.brown.cs.seede.test;

import java.io.File;

import org.junit.Test;

import edu.brown.cs.seede.acorn.AcornLog;

public class TestSolar extends TestBase 
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final String		TESTSOLAR_SID = "SEED_22548";
private static final String		TESTSOLAR_SID1 = "SEED_22548_1";

private static final String		TEST_PROJECT = "solar";
private static final String		LAUNCHSOLAR_NAME = "solar";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/


public TestSolar()
{
   super("SOLAR","solar",TEST_PROJECT);
}



/********************************************************************************/
/*										*/
/*	Test method								*/
/*										*/
/********************************************************************************/

@Test public void testSolar()
{
   AcornLog.logI("TEST: Start TEST3 (solar)");
   LaunchData ld = startLaunch(LAUNCHSOLAR_NAME,1);
   setupSeedeSession(TESTSOLAR_SID,ld,0);
   runSeede(TESTSOLAR_SID);
   removeSeede(TESTSOLAR_SID);
   
   setupSeedeSession(TESTSOLAR_SID1,ld,0);
   runSeede(TESTSOLAR_SID1);
      
   File f1 = new File("/gpfs/main/home/spr/solar/javasrc/edu/brown/cs/cs032/solar/SolarGravity.java");
   if (!f1.exists()) {
      f1 = getFile("solar/javasrc/edu/brown/cs/cs032/solar/SolarGravity.java");
    }
   editBedrock(f1.getPath(),0,4298,"x");
}



}	// end of class TestSolar




/* end of TestSolar.java */

