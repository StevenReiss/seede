/********************************************************************************/
/*                                                                              */
/*              TestTc2.java                                                    */
/*                                                                              */
/*      Tests for TC2                                                           */
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



package edu.brown.cs.seede.test;

import org.junit.Test;

import edu.brown.cs.seede.acorn.AcornLog;

public class TestTc2 extends TestBase
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String             TESTTC2_SID = "SEED_32578";
private static final String             TEST_PROJECT = "tc2";
private static final String             LAUNCH_NAME = "t13CorrelationTest";


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TestTc2()
{
   super("TC2","tc2",TEST_PROJECT);
}



/********************************************************************************/
/*                                                                              */
/*      Test methods                                                            */
/*                                                                              */
/********************************************************************************/


@Test public void testTc2()
{
   AcornLog.logI("TEST","Start TEST TC2");
   LaunchData ld = startLaunch(LAUNCH_NAME,0);
   setupSeedeSession(TESTTC2_SID,ld,-1);
   addAllFiles(TESTTC2_SID);
   runSeede(TESTTC2_SID);
   removeSeede(TESTTC2_SID);
}




}       // end of class TestTc2




/* end of TestTc2.java */

