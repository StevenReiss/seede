/********************************************************************************/
/*                                                                              */
/*              TestPole.java                                                   */
/*                                                                              */
/*      Test seed using POLE package                                            */
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
import java.io.IOException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.sesame.SesameConstants;
import edu.brown.cs.seede.sesame.SesameMain;

public class TestPole implements MintConstants, SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String		MINT_NAME = "SEEDE_TEST_pole";
private static final String		SOURCE_ID = "SEED_22345";
private static final String		TESTPOLE_SID = "SEED_22348";

private static final String		TEST_PROJECT = "Pole";
private static final String		LAUNCHPOLE_NAME = "Pole Test2";


private static MintControl mint_control;

private String		stopped_thread;
private Element 	seede_result;


static {
   mint_control = MintControl.create(MINT_NAME,MintSyncMode.ONLY_REPLIES);
}



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TestPole()
{
   stopped_thread = null;
   seede_result = null;
   
   mint_control.register("<BEDROCK SOURCE='ECLIPSE' TYPE='_VAR_0' />",new IDEHandler());
   mint_control.register("<SEEDE TYPE='_VAR_0' />",new SeedeHandler());
}




@BeforeClass public static void setupBedrock()
{
   System.err.println("SETTING UP BEDROCK");
   File ec1 = new File("/u/spr/eclipse-neonx/eclipse/eclipse");
   File ec2 = new File("/u/spr/Eclipse/hump");
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
   cmd += " -Xmx16000m";
   
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



/********************************************************************************/
/*                                                                              */
/*      Test method                                                             */
/*                                                                              */
/********************************************************************************/

@Test public void testPole()
{
   System.err.println("Start TEST3");
   LaunchData ld = startLaunch(LAUNCHPOLE_NAME);
   continueLaunch(ld); 
   
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SESSION");
   xw.field("TYPE","LAUNCH");
   xw.field("PROJECT",TEST_PROJECT);
   xw.field("LAUNCHID",ld.getLaunchId());
   xw.field("THREADID",ld.getThreadId());
   xw.end("SESSION");
   String cnts = xw.toString();
   xw.close();
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("BEGIN",TESTPOLE_SID,null,cnts,rply);
   Element status = rply.waitForXml();
   Assert.assertTrue(IvyXml.isElement(status,"RESULT"));
   
   rply = new MintDefaultReply();
   seede_result = null;
   CommandArgs args = new CommandArgs("EXECID","testpole");
   sendSeedeMessage("EXEC",TESTPOLE_SID,args,cnts,rply);
   String sstatus = rply.waitForString();
   System.err.println("RESULT IS " + sstatus);
   Element xml = waitForSeedeResult();
   System.err.println("SEED RESULT IS " + IvyXml.convertXmlToString(xml));
}





/********************************************************************************/
/*										*/
/*	Handle launches 							*/
/*										*/
/********************************************************************************/

private LaunchData startLaunch(String name)
{
   stopped_thread = null;
   
   File lib = new File("/pro/seede/lib");
   if (!lib.exists()) lib = new File("/research/people/spr/seede/lib");
   File f1 = new File(lib,"poppy.jar");
   String dargs = "-javaagent:" + f1.getPath();
   
   stopped_thread = null;
   CommandArgs args = new CommandArgs("NAME",name,"MODE","debug","BUILD","TRUE",
	 "REGISTER","TRUE","VMARG",dargs);
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
   String threadid = waitForStop();
   Assert.assertNotNull(threadid);
   
   return new LaunchData(launchid,targetid,processid,threadid);
}


private void continueLaunch(LaunchData ld)
{
   stopped_thread = null;
   
   CommandArgs args = new CommandArgs("LAUNCH",ld.getLaunchId(),
	 "TARGET",ld.getTargetId(),
	 "PROCESS",ld.getProcessId(),"ACTION","RESUME");
   MintDefaultReply rply = new MintDefaultReply();
   sendBubblesMessage("DEBUGACTION",TEST_PROJECT,args,null,rply);
   String x = rply.waitForString();
   Assert.assertNotNull(x);
   String threadid = waitForStop();
   Assert.assertNotNull(threadid);
   
   ld.setThreadId(threadid);
}



private static class LaunchData {
   
   private String lanuch_id;
   private String target_id;
   private String process_id;
   private String thread_id;
   
   LaunchData(String launch,String target,String process,String thread) {
      lanuch_id = launch;
      target_id = target;
      process_id = process;
      thread_id = thread;
    }
   
   String getLaunchId() 			{ return lanuch_id; }
   String getTargetId() 			{ return target_id; }
   String getProcessId()			{ return process_id; }
   String getThreadId() 			{ return thread_id; }
   
   void setThreadId(String id)			{ thread_id = id; }
   
}	// end of inner class LaunchData




/********************************************************************************/
/*										*/
/*	Handle messages coming back						*/
/*										*/
/********************************************************************************/

private static class SeedeThread extends Thread {
   
   SeedeThread() {
      super("SeedeMain");
    }
   
   @Override public void run() {
      SesameMain.main(new String [] { "-m", MINT_NAME });
    }

}	// end of inner class SeedeThread





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
   
   AcornLog.logD("SEND to BUBBLES: " + xml);
   
   int fgs = MINT_MSG_NO_REPLY;
   if (rply != null) fgs = MINT_MSG_FIRST_NON_NULL;
   mint_control.send(xml,rply,fgs);
}



/********************************************************************************/
/*										*/
/*	Sesame Messaging methods						*/
/*										*/
/********************************************************************************/

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
/*										*/
/*	Handle messages from Eclipse						*/
/*										*/
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
   
}	// end of innerclass IDEHandler



/********************************************************************************/
/*										*/
/*	Handle messages from SEEDE						*/
/*										*/
/********************************************************************************/

private class SeedeHandler implements MintHandler {
   
   @Override public void receive(MintMessage msg,MintArguments args) {
      System.err.println("TEST: Received from seede: " + msg.getText());
      String what = args.getArgument(0);
      Element xml = msg.getXml();
      switch (what) {
         case "EXEC" :
            synchronized (TestPole.this) {
               seede_result = xml;
               TestPole.this.notifyAll();
             }
            break;
       }
    }
   
}	// end of inner class SeedeHandler



private Element waitForSeedeResult()
{
   synchronized (this) {
      while (seede_result == null) {
	 try {
	    wait(1000);
	  }
	 catch (InterruptedException e) { }
       }
      Element rslt = seede_result;
      seede_result = null;
      return rslt;
    }
}




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
   // String detail = IvyXml.getAttrString(xml,"DETAIL");
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



}       // end of class TestPole




/* end of TestPole.java */
