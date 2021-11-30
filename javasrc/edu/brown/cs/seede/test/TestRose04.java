/********************************************************************************/
/*                                                                              */
/*              TestRose04.java                                                 */
/*                                                                              */
/*      Another rose test -- use test04 launch                                  */
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

public class TestRose04 extends TestBase
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String             TEST4_SID = "SEED_23344";
private static final String             ROSETEST_WORKSPACE = "rosetest";
private static final String             ROSETEST_PROJECT = "rosetest";
private static final String             LAUNCH4_NAME = "test04";




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TestRose04()
{
   super("ROSETEST",ROSETEST_WORKSPACE,ROSETEST_PROJECT);
}




/********************************************************************************/
/*                                                                              */
/*      Actual test                                                             */
/*                                                                              */
/********************************************************************************/

@Test
public void testRose04()
{
   AcornLog.logI("TEST: Start ROSETEST");
   LaunchData ld = startLaunch(LAUNCH4_NAME,0);
   setupSeedeSession(TEST4_SID,ld,1);
   addSeedeFiles(TEST4_SID,"src/edu/brown/cs/rosetest/RoseTestExamples.java",
         "src/edu/brown/cs/rosetest/RoseTestTests.java");
   runSeede(TEST4_SID);
   
   String ssid = startSeedeSubsession(TEST4_SID);
   editSeede(ssid,"src/edu/brown/cs/rosetest/RoseTestExamples.java",13,2928,"getBabyName");
   runSeede(ssid);
   removeSeede(ssid);
}


}       // end of class TestRose04




/* end of TestRose04.java */

