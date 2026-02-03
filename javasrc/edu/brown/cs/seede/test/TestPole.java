/********************************************************************************/
/*                                                                              */
/*              TestPole.java                                                   */
/*                                                                              */
/*      Test seede using POLE package                                           */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 * This program and the accompanying materials are made available under the      *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at                                                           *
 *      http://www.eclipse.org/legal/epl-v10.html                                *
 *                                                                               *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.seede.test;

import org.junit.Test;

import edu.brown.cs.seede.acorn.AcornLog;

public class TestPole extends TestBase 
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String             TESTPOLE_SID = "SEED_22348";

private static final String             TEST_PROJECT = "Pole";
private static final String             LAUNCHPOLE_NAME = "Pole Test2";



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TestPole()
{
   super("POLE","hump",TEST_PROJECT);
}



/********************************************************************************/
/*                                                                              */
/*      Test method                                                             */
/*                                                                              */
/********************************************************************************/

@Test public void testPole()
{
   AcornLog.logI("TEST: Start TEST POLE");
   LaunchData ld = startLaunch(LAUNCHPOLE_NAME,1);
   
   setupSeedeSession(TESTPOLE_SID,ld,0);
   runSeede(TESTPOLE_SID);
}



}       // end of class TestPole




/* end of TestPole.java */

