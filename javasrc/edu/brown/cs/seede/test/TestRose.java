/********************************************************************************/
/*                                                                              */
/*              TestRose.java                                                   */
/*                                                                              */
/*      Test cases for ROSE                                                     */
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

public class TestRose extends TestBase
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String             TEST1_SID = "SEED_23333";
private static final String             ROSETEST_WORKSPACE = "rosetest";
private static final String             ROSETEST_PROJECT = "rosetest";
private static final String             LAUNCH1_NAME = "test01";



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TestRose()
{
   super("ROSETEST",ROSETEST_WORKSPACE,ROSETEST_PROJECT);
}


/********************************************************************************/
/*                                                                              */
/*      Actual test                                                             */
/*                                                                              */
/********************************************************************************/

@Test
public void testRose01()
{
   AcornLog.logI("TEST: Start ROSETEST");
   LaunchData ld = startLaunch(LAUNCH1_NAME,0);
   setupSeedeSession(TEST1_SID,ld,2);
   addSeedeFiles(TEST1_SID,"src/edu/brown/cs/rosetest/RoseTestExamples.java",
         "src/edu/brown/cs/rosetest/RoseTestTests.java");
   runSeede(TEST1_SID);
   
   String ssid = startSeedeSubsession(TEST1_SID);
   String ssid1 = startSeedeSubsession(TEST1_SID);
   editSeede(ssid,"src/edu/brown/cs/rosetest/RoseTestExamples.java",4,2979,"baby");
   editSeede(ssid1,"src/edu/brown/cs/rosetest/RoseTestExamples.java",3,2993,"baby");
   runSeede(ssid);
   removeSeede(ssid);
   runSeede(ssid1);
   removeSeede(ssid1);
   
   for (int i = 0; i < 100; ++i) {
      AcornLog.logI("TEST: Start subsession " + i);
      System.err.println("TEST: Start subsession " + i);
      String ssid2 = startSeedeSubsession(TEST1_SID);
      editSeede(ssid2,"src/edu/brown/cs/rosetest/RoseTestExamples.java",4,3054,"type");
      runSeede(ssid2);
      removeSeede(ssid2); 
    }
}




}       // end of class TestRose




/* end of TestRose.java */

