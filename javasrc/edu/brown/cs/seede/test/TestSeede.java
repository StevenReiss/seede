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
import org.junit.Test;
import org.junit.Assert;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.xml.IvyXml;



public class TestSeede implements MintConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final String		MINT_NAME = "SEEDE_TEST_spr";
private static final String		SOURCE_ID = "SEED_12345";
private static final String             TEST_PROJECT = "Sample1";      
private static final String             LAUNCH_NAME = "test1";


private MintControl	mint_control;
private String          stopped_thread;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TestSeede()
{
   mint_control = MintControl.create(MINT_NAME,MintSyncMode.ONLY_REPLIES);
   mint_control.register("<BEDROCK SOURCE='ECLIPSE' TYPE='_VAR_0' />",new IDEHandler());
}


@Before public void setupBedrock()
{
   File ec1 = new File("/u/spr/eclipse-neonx/eclipse/eclipse");
   File ec2 = new File("/u/spr/Eclipse/seede-test");
   if (!ec1.exists()) {
      ec1 = new File("/Developer/eclipse42/eclipse");
      ec2 = new File("/Users/spr/Documents/seede-test");
    }
   if (!ec1.exists()) {
      System.err.println("Can't find bubbles version of eclipse to run");
      System.exit(1);
    }

   String cmd = ec1.getAbsolutePath();
   cmd += " -application edu.brown.cs.bubbles.bedrock.application";
   cmd += " -data " + ec2.getAbsolutePath();
   cmd += " -nosplash";
   cmd += " -vmargs -Dedu.brown.cs.bubbles.MINT=" + MINT_NAME;

   try {
      IvyExec ex = new IvyExec(cmd);
      for (int i = 0; i < 250; ++i) {
	 synchronized(this) {
	    try { wait(1000); } catch (InterruptedException e) { }
	  }
	 if (tryPing()) {
            sendMessage("LOGLEVEL",null,"LEVEL='DEBUG'",null);
            sendMessage("ENTER");
            return;
          }
       }
    }
   catch (IOException e) { }

   throw new Error("Problem running Eclipse: " + cmd);
}



@After public void shutdownBedrock()
{
   sendMessage("EXIT",null,null,null);
}



private boolean tryPing()
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendMessage("PING",null,null,null,mdr,MINT_MSG_FIRST_NON_NULL);
   String r = mdr.waitForString(500);
   return r != null;
}




/********************************************************************************/
/*                                                                              */
/*      Basic Tests                                                             */
/*                                                                              */
/********************************************************************************/

@Test public void test1()
{
   stopped_thread = null;
   String args = "NAME='" + LAUNCH_NAME + "'";
   args += " MODE='DEBUG='debug'";
   args += " BUILD='true'";
   args += " REGISTER='true'";
   MintDefaultReply rply = new MintDefaultReply();
   sendMessage("START",TEST_PROJECT,args,null,rply,MINT_MSG_FIRST_NON_NULL);
   Element xml = rply.waitForXml();
   Element ldata = IvyXml.getChild(xml,"LANUCH");
   Assert.assertNotNull(ldata);
   String launchid = IvyXml.getAttrString(xml,"ID");
   Assert.assertNotNull(launchid);
   String targetid = IvyXml.getAttrString(xml,"TARGET");
   Assert.assertNotNull(targetid);
   String processid = IvyXml.getAttrString(xml,"PROCESS");
   Assert.assertNotNull(processid);
   waitForStop();
   stopped_thread = null;
   
   
   args = "LANUCH='" + launchid + "'";
   args += " TARGET='" + targetid + "'";
   args += " PROCESS='" + processid + "'";
   args += " ACTION='RESUME'";
   rply = new MintDefaultReply();
   sendMessage("DEBUGACTION",TEST_PROJECT,args,null,rply,MINT_MSG_FIRST_NON_NULL);
   String threadid = waitForStop();

   // do something here
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




/********************************************************************************/
/*                                                                              */
/*      Handle messages from Eclipse                                            */
/*                                                                              */
/********************************************************************************/

private class IDEHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      Element e = msg.getXml();
      if (cmd == null) return;
      
      switch (cmd) {
         case "ELISIION" :
            break;
         case "EDITERROR" :
            break;
         case "FILEERROR" :
            break;
         case "PRIVATEERROR" :
            break;
         case "EDIT" :
            break;
         case "BREAKEVENT" :
            break;
         case "LAUNCHCONFIGEVENT" :
            break;
         case "RUNEVENT" :
            long when = IvyXml.getAttrLong(e,"TIME");
            for (Element re : IvyXml.children(e,"RUNEVENT")) {
               handleRunEvent(re,when);
             }
            msg.replyTo("<OK/>");
            break;
         case "NAMES" :
         case "ENDNAMES" :
            break;
         case "PING" :
            msg.replyTo("<PONG/>");
            break;
         case "PROGRESS" :
            msg.replyTo("<OK/>");
            break;
         case "RESOURCE" :
            break;
         case "CONSOLE" :
            msg.replyTo("<OK/>");
            break;
         case "OPENEDITOR" :
            break;
         case "EVALUATION" :
            msg.replyTo("<OK/>");
            break;
         case "BUILDDONE" :
         case "FILECHANGE" :
         case "PROJECTDATA" :
         case "PROJECTOPEN" :
            break;
         case "STOP" :
            break;
         default :
            break;
       }
    }
   
}       // end of innerclass IDEHandler



private void handleRunEvent(Element xml,long when)
{
   String type = IvyXml.getAttrString(xml,"TYPE");
   if (type == null) return;
   switch (type) {
      case "PROCESS" :
         break;
      case "THREAD" :
         handleThreadEvent(xml,when);
         break;
      case "TARGET" :
         break;
      default :
         break;
    }
}


private void handleThreadEvent(Element xml,long when)
{
   String kind = IvyXml.getAttrString(xml,"KIND");
   String detail = IvyXml.getAttrString(xml,"DETAIL");
   Element thread = IvyXml.getChild(xml,"THREAD");
   if (thread == null) return;
   switch (kind) {
      case "SUSPEND" :
         synchronized (this) {
            stopped_thread = IvyXml.getAttrString(thread,"ID");
            notifyAll();
          }
         break;   
    }
}



private String waitForStop()
{
   synchronized (this) {
      while (stopped_thread == null) {
         try {
            wait();
          }
         catch (InterruptedException e) { }
       }
      return stopped_thread;
    }
}


}	// end of class TestSeede




/* end of TestSeede.java */






































