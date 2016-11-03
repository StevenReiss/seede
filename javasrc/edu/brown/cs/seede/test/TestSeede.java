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
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import edu.brown.cs.seede.sesame.SesameMain;
import edu.brown.cs.seede.sesame.SesameConstants;


public class TestSeede implements MintConstants, SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final String		MINT_NAME = "SEEDE_TEST_spr";
private static final String		SOURCE_ID = "SEED_12345";
private static final String             TEST_SID = "SEED_12346";

private static final String             TEST_PROJECT = "sample1";      
private static final String             LAUNCH_NAME = "test1";
private static final String             REL_PATH1 = "src/edu/brown/cs/seede/sample/Tester.java";


private static MintControl mint_control;
private static File        project_directory;

private String          stopped_thread;


static {
   mint_control = MintControl.create(MINT_NAME,MintSyncMode.ONLY_REPLIES);
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TestSeede()
{
   mint_control.register("<BEDROCK SOURCE='ECLIPSE' TYPE='_VAR_0' />",new IDEHandler());
}


@BeforeClass public static void setupBedrock()
{
   System.err.println("SETTING UP BEDROCK");
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
      for (int i = 0; i < 250; ++i) {
         try { Thread.sleep(1000); } catch (InterruptedException e) { }
	 if (pingEclipse()) {
            CommandArgs args = new CommandArgs("LEVEL","DEBUG");
            sendBubblesMessage("LOGLEVEL",null,args,null);
            sendBubblesMessage("ENTER");
            MintDefaultReply rply = new MintDefaultReply();
            sendBubblesMessage("OPENPROJECT",TEST_PROJECT,null,null,rply);
            Element pxml = rply.waitForXml();
            if (!IvyXml.isElement(pxml,"PROJECT")) pxml = IvyXml.getChild(pxml,"PROJECT");
            String dirs = IvyXml.getAttrString(pxml,"PATH");
            if (dirs != null) project_directory = new File(dirs);
            return;
          }
         if (i == 0) new IvyExec(cmd);
       }
    }
   catch (IOException e) { }

   throw new Error("Problem running Eclipse: " + cmd);
}


@BeforeClass public static void startSeede()
{ 
   System.err.println("Setting Up Sesame");
   
   SeedeThread st = new SeedeThread();
   for (int i = 0; i < 100; ++i) {
      if (pingSeede()) return;
      if (i == 0) st.start();
      try { Thread.sleep(1000); } catch (InterruptedException e) { }
    }
   throw new Error("Problem starting sesame server");
}



@AfterClass public static void shutdownBedrock()
{
   System.err.println("Shut down bedrock");
   sendBubblesMessage("EXIT");
}



@AfterClass public static void shutdownSeede()
{
   System.err.println("Shut down seede");
   // sendSeedeMessage("EXIT");
}


private static boolean pingEclipse()
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendBubblesMessage("PING",null,null,null,mdr);
   String r = mdr.waitForString(500);
   return r != null;
}

private static boolean pingSeede()
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendSeedeMessage("PING",null,null,null,mdr);
   String r = mdr.waitForString(500);
   return r != null;
}


private static class SeedeThread extends Thread {

   SeedeThread() {
      super("SeedeMain");
    }
   
   @Override public void run() {
      SesameMain.main(new String [] { "-m", MINT_NAME });
    }
   
}       // end of inner class SeedeThread




/********************************************************************************/
/*                                                                              */
/*      Basic Tests                                                             */
/*                                                                              */
/********************************************************************************/

@Test public void test1()
{
   System.err.println("Start TEST1");
   stopped_thread = null;
   CommandArgs args = new CommandArgs("NAME",LAUNCH_NAME,"MODE","debug","BUILD","TRUE",
         "REGISTER","TRUE");
   MintDefaultReply rply = new MintDefaultReply();
   sendBubblesMessage("START",TEST_PROJECT,args,null,rply);
   Element xml = rply.waitForXml();
   Element ldata = IvyXml.getChild(xml,"LAUNCH");
   Assert.assertNotNull(ldata);
   String launchid = IvyXml.getAttrString(ldata,"ID");
   Assert.assertNotNull(launchid);
   String targetid = IvyXml.getAttrString(ldata,"TARGET");
   Assert.assertNotNull(targetid);
   String processid = IvyXml.getAttrString(ldata,"PROCESS");
   Assert.assertNotNull(processid);
   waitForStop();
   stopped_thread = null;
   
   args = new CommandArgs("LAUNCH",launchid,"TARGET",targetid,"PROCESS",processid,"ACTION","RESUME");
   rply = new MintDefaultReply();
   sendBubblesMessage("DEBUGACTION",TEST_PROJECT,args,null,rply);
   String x = rply.waitForString();
   String threadid = waitForStop();
   Assert.assertNotNull(threadid);
   // do something here
}



@Test public void test2()
{
   System.err.println("START TEST2");
   System .err.flush();
   
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("PING",null,null,null,rply);
   String srply = rply.waitForString();
   Assert.assertTrue(srply.contains("PONG"));
   
   File srcf = new File(project_directory,REL_PATH1);
   
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SESSION");
   xw.field("TYPE","TEST");
   xw.field("PROJECT",TEST_PROJECT);
   xw.begin("LOCATION");
   xw.field("ID","L1");
   xw.field("FILE",srcf);
   xw.field("LINE",33);
   xw.field("CLASS","edu.brown.cs.seede.sample.Tester");
   xw.field("METHOD","gcd");
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
   
   rply = new MintDefaultReply();
   sendSeedeMessage("BEGIN",TEST_SID,null,cnts,rply);
   Element status = rply.waitForXml();
   Assert.assertTrue(IvyXml.isElement(status,"RESULT")); 
}




/********************************************************************************/
/*										*/
/*	Bubbles Messaging methods						*/
/*										*/
/********************************************************************************/

private static void sendBubblesMessage(String cmd)
{
   sendBubblesMessage(cmd,null,null,null,null);
}


private static void sendBubblesMessage(String cmd,String proj,Map<String,Object> flds,String cnts)
{
   sendBubblesMessage(cmd,proj,flds,cnts,null);
}


private static void sendBubblesMessage(String cmd,String proj,Map<String,Object> flds,String cnts,
      MintReply rply)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("BUBBLES");
   xw.field("DO",cmd);
   xw.field("BID",SOURCE_ID);
   if (proj != null && proj.length() > 0) xw.field("PROJECT",proj);
   if (flds != null) {
      for (Map.Entry<String,Object> ent : flds.entrySet()) {
         xw.field(ent.getKey(),ent.getValue());
       }
    }
   xw.field("LANG","eclipse");
   if (cnts != null) xw.xmlText(cnts);
   xw.end("BUBBLES");
   
   String xml = xw.toString();
   xw.close();
   
   int fgs = MINT_MSG_NO_REPLY;
   if (rply != null) fgs = MINT_MSG_FIRST_NON_NULL;
   mint_control.send(xml,rply,fgs);
}



/********************************************************************************/
/*										*/
/*	Sesame Messaging methods						*/
/*										*/
/********************************************************************************/

private static void sendSeedeMessage(String cmd)
{
   sendSeedeMessage(cmd,null,null,null);
}


private static void sendSeedeMessage(String cmd,String sess,Map<String,Object> flds,String cnts)
{
   sendSeedeMessage(cmd,sess,flds,cnts,null);
}


private static void sendSeedeMessage(String cmd,String sess,Map<String,Object> flds,String cnts,
      MintReply rply)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SEEDE");
   xw.field("DO",cmd);
   if (sess == null) sess = "*";
   xw.field("SID",sess);
   if (flds != null) {
      for (Map.Entry<String,Object> ent : flds.entrySet()) {
         xw.field(ent.getKey(),ent.getValue());
       }
    }
   if (cnts != null) xw.xmlText(cnts);
   xw.end("SEEDE");
   
   String xml = xw.toString();
   xw.close();
   
   int fgs = MINT_MSG_NO_REPLY;
   if (rply != null) fgs = MINT_MSG_FIRST_NON_NULL;
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
            wait(3000);
          }
         catch (InterruptedException e) { }
       }
      return stopped_thread;
    }
}








}	// end of class TestSeede




/* end of TestSeede.java */






































