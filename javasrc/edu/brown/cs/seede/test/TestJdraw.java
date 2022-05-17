/********************************************************************************/
/*                                                                              */
/*              TestJdraw.java                                                  */
/*                                                                              */
/*      Tests based on jdraw                                                    */
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

public class TestJdraw extends TestBase
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String             TESTJDRAW_SID = "SEED_29529";




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TestJdraw()
{
   super("ROSETEST_JDRAW",null,null);
}



/********************************************************************************/
/*                                                                              */
/*      Actual tests                                                            */
/*                                                                              */
/********************************************************************************/

@Test
public void testJdraw01()
{
   setup("jdraw","jdraw");
   
   AcornLog.logI("TEST: Start ROSETEST");
   LaunchData ld = startLaunch("jdraw",1);
   
   setupSeedeSession(TESTJDRAW_SID,ld,0);
   addSeedeFiles(TESTJDRAW_SID,new File("/Users/spr/Eclipse/jdraw/filesall.xml"));
   
   runSeede(TESTJDRAW_SID);
}




}       // end of class TestJdraw




/* end of TestJdraw.java */

