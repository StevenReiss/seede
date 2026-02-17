/********************************************************************************/
/*                                                                              */
/*              TestTc1.java                                                    */
/*                                                                              */
/*      description of class                                                    */
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

public class TestTc1 extends TestBase
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/


private static final String             TESTTC1_SID = "SEED_32548";
private static final String             TEST_PROJECT = "tc1";
static final String             LAUNCHTC1_NAME = "EventRoutingTest";
static final String             LAUNCHTC2_NAME = "InventoryServiceTest";
static final String             LAUNCHTC3_NAME = "OrderLifecysleTest";
static final String             LAUNCHTC4_NAME = "PaymentProcessingTest";




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/


public TestTc1()
{
   super("TC1","tc1",TEST_PROJECT);
}



/********************************************************************************/
/*                                                                              */
/*      Test method                                                             */
/*                                                                              */
/********************************************************************************/

@Test public void testTc1()
{
   AcornLog.logI("TEST: Start TEST TC1");
   LaunchData ld = startLaunch(LAUNCHTC4_NAME,1);
   setupSeedeSession(TESTTC1_SID,ld,-1);
   addAllFiles(TESTTC1_SID);
   runSeede(TESTTC1_SID);
   removeSeede(TESTTC1_SID);
}


}       // end of class TestTc1




/* end of TestTc1.java */

