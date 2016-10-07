/********************************************************************************/
/*										*/
/*		TestSeede.java							*/
/*										*/
/*	General test program for seede execution engine 			*/
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
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.mint.MintConstants;



public class TestSeede implements MintConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final String		MINT_NAME = "SEEDE_TEST_spr";
private static final String		SOURCE_ID = "SEED_12345";

private MintControl	mint_control;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TestSeede()
{
   mint_control = MintControl.create(MINT_NAME,MintSyncMode.ONLY_REPLIES);
}


@Before public void setupBedrock()
{
   File ec1 = new File("/u/spr/eclipse-neonx/eclipse/eclipse");
   File ec2 = new File("/u/spr/Eclipse/solar");
   if (!ec1.exists()) {
      ec1 = new File("/Developer/eclipse42/Eclipse.app");
      ec2 = new File("/Users/spr/Documents/workspacesolar");
    }
   if (!ec1.exists()) {
      System.err.println("Can't find bubbles version of eclipse to run");
      System.exit(1);
    }

   String cmd = ec1.getAbsolutePath();
   cmd += " -application edu.brown.cs.bubbles.bedrock.application";
   cmd += " -data " + ec2.getAbsolutePath();
   cmd += " -vmargs -Dedu.brown.cs.bubbles.MINT=" + MINT_NAME;

   try {
      IvyExec ex = new IvyExec(cmd);
      for (int i = 0; i < 250; ++i) {
	 synchronized(this) {
	    try { wait(1000); } catch (InterruptedException e) { }
	  }
	 if (tryPing()) return;
       }
    }
   catch (IOException e) { }

   throw new Error("Problem running Eclipse");
}



@After public void shutdownBedrock()
{
   sendMessage("EXIT",null,null,null);
}



private boolean tryPing()
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendMessage("PING",null,null,null,null,MINT_MSG_FIRST_NON_NULL);
   String r = mdr.waitForString(50000);
   return r != null;
}




/********************************************************************************/
/*										*/
/*	Messaging methods							*/
/*										*/
/********************************************************************************/

private void sendMessage(String cmd)
{
   sendMessage(cmd,null,null,null,null,MINT_MSG_NO_REPLY);
}


private void sendMessage(String cmd,String proj,String flds,String cnts)
{
   sendMessage(cmd,proj,flds,cnts,null,MINT_MSG_NO_REPLY);
}


private void sendMessage(String cmd,String proj,String flds,String cnts,MintReply rply,int fgs)
{
   String xml = "<BUBBLES DO='" + cmd + "'";
   xml += " BID='" + SOURCE_ID + "'";
   if (proj != null && proj.length() > 0) xml += " PROJECT='" + proj + "'";
   if (flds != null) xml += " " + flds;
   xml += " LANG='Java' >";
   if (cnts != null) xml += cnts;
   xml += "</BUBBLES>";
   mint_control.send(xml,rply,fgs);
}




}	// end of class TestSeede




/* end of TestSeede.java */






































