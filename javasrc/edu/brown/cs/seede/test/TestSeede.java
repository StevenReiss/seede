/********************************************************************************/
/*                                                                              */
/*              TestSeede.java                                                  */
/*                                                                              */
/*      General test program for seede execution engine                         */
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

import java.io.File;

import org.junit.Test;

import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.ivy.xml.IvyXmlWriter;


public class TestSeede extends TestBase
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String             TEST1_SID = "SEED_12346";
private static final String             TEST2_SID = "SEED_12347";
private static final String             TEST3_SID = "SEED_12348";
private static final String             TEST4_SID = "SEED_12349";
private static final String             TEST5_SID = "SEED_12950";
private static final String             TEST6_SID = "SEED_12951";
private static final String             TEST7_SID = "SEED_12952";
private static final String             TEST8_SID = "SEED_12953";

private static final String             TEST_PROJECT = "sample1";
private static final String             LAUNCH1_NAME = "test1";
private static final String             LAUNCH3_NAME = "test2";
private static final String             LAUNCH4_NAME = "test3";
private static final String             LAUNCH5_NAME = "testLambda";
private static final String             LAUNCH6_NAME = "testList";
private static final String             LAUNCH7_NAME = "testNative";
private static final String             LAUNCH8_NAME = "testReflection";
private static final String             REL_PATH1 = "src/edu/brown/cs/seede/sample/Tester.java";



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TestSeede()
{
   super("SEEDTEST","seede-test",TEST_PROJECT);
}




/********************************************************************************/
/*                                                                              */
/*      Basic Tests                                                             */
/*                                                                              */
/********************************************************************************/

@Test public void test1()
{
   AcornLog.logD("TEST: START TEST1");
   LaunchData ld = startLaunch(LAUNCH1_NAME,0);
   setupSeedeSession(TEST1_SID,ld,0);
   runSeede(TEST1_SID);
}



@Test public void test2()
{
   AcornLog.logD("TEST: START TEST2");

   File srcf = new File(project_directory,REL_PATH1);
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SESSION");
   xw.field("TYPE","TEST");
   xw.field("PROJECT",TEST_PROJECT);
   xw.begin("LOCATION");
   xw.field("ID","L1");
   xw.field("FILE",srcf);
   xw.field("LINE",46);
   xw.field("CLASS","edu.brown.cs.seede.sample.Tester");
   xw.field("METHOD","gcd");
   xw.field("THREAD","THREAD_1");
   xw.field("SIGNATURE","(II)I");
   xw.field("ACTIVE",true);
   xw.end("LOCATION");
   xw.begin("TEST");
   xw.field("METHOD","edu.brown.cs.seede.sample.Tester.gcd");
   xw.begin("ARG");
   xw.field("TYPE","int");
   xw.field("JTYPE","I");
   xw.field("VALUE",100);
   xw.end("ARG");
   xw.begin("ARG");
   xw.field("TYPE","int");
   xw.field("JTYPE","I");
   xw.field("VALUE",64);
   xw.end("ARG");
   xw.end("TEST");
   xw.end("SESSION");
   String cnts = xw.toString();
   xw.close();
   
   setupSeedeTestSession(TEST2_SID,cnts);
   runSeede(TEST2_SID);
}





@Test public void test3()
{
   AcornLog.logD("TEST: START TEST3");
   LaunchData ld = startLaunch(LAUNCH3_NAME,1);
   setupSeedeSession(TEST3_SID,ld,0);
   runSeede(TEST3_SID);
}



@Test public void test4()
{
   AcornLog.logD("TEST: START TEST4");
   LaunchData ld = startLaunch(LAUNCH4_NAME,1);
   setupSeedeSession(TEST4_SID,ld,0);
   runSeede(TEST4_SID);
}




@Test public void test5()
{
   AcornLog.logD("TEST: START TEST5");
   LaunchData ld = startLaunch(LAUNCH5_NAME,1);
   setupSeedeSession(TEST5_SID,ld,0);
   runSeede(TEST5_SID);
}




@Test public void test6()
{
   AcornLog.logD("TEST: START TEST6");
   LaunchData ld = startLaunch(LAUNCH6_NAME,1);
   setupSeedeSession(TEST6_SID,ld,0);
   runSeede(TEST6_SID);
}



@Test public void test7()
{
   AcornLog.logD("TEST: START TEST7");
   LaunchData ld = startLaunch(LAUNCH7_NAME,1);
   setupSeedeSession(TEST7_SID,ld,0);
   runSeede(TEST7_SID);
}

@Test public void test8()
{
   AcornLog.logD("TEST: START TEST8");
   LaunchData ld = startLaunch(LAUNCH8_NAME,0);
   setupSeedeSession(TEST8_SID,ld,0);
   runSeede(TEST8_SID);
}



}       // end of class TestSeede




/* end of TestSeede.java */
