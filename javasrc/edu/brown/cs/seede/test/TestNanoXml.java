/********************************************************************************/
/*                                                                              */
/*              TestNanoXml.java                                                */
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

/* SVN: $Id$ */



package edu.brown.cs.seede.test;

import org.junit.Test;

import edu.brown.cs.seede.acorn.AcornLog;

public class TestNanoXml extends TestBase
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/


private static final String             TESTNANOXML_SID = "SEED_22736";

private static final String             TEST_PROJECT = "nanoxml";
private static final String             LAUNCH_NAME = "testParsing16";



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/


public TestNanoXml()
{
   super("NANOXML","nanoxml",TEST_PROJECT);
}





/********************************************************************************/
/*                                                                              */
/*      Test method                                                             */
/*                                                                              */
/********************************************************************************/

@Test
public void testNanoXml()
{
   AcornLog.logI("TEST: Start TEST NanoXML");
   LaunchData ld = startLaunch(LAUNCH_NAME,0);
   
   setupSeedeSession(TESTNANOXML_SID,ld,2);
   addSeedeFiles(TESTNANOXML_SID,"/pro/nanoxml/src/net/n3/nanoxml/NonValidator.java",
         "/pro/nanoxml/src/net/n3/nanoxml/XMLParserFactory.java",
         "/pro/nanoxml/src/net/n3/nanoxml/XMLElement.java",
         "/pro/nanoxml/src/net/n3/nanoxml/StdXMLBuilder.java",
         "/pro/nanoxml/src/net/n3/nanoxml/XMLUtil.java",
         "/pro/nanoxml/test/net/n3/nanoxml/ParserTest1.java",
         "/pro/nanoxml/src/net/n3/nanoxml/StdXMLParser.java",
         "/pro/nanoxml/src/net/n3/nanoxml/XMLEntityResolver.java",
         "/pro/nanoxml/src/net/n3/nanoxml/StdXMLReader.java"
   );

   runSeede(TESTNANOXML_SID);
}



}       // end of class TestNanoXml




/* end of TestNanoXml.java */

