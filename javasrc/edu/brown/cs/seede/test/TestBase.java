/********************************************************************************/
/*                                                                              */
/*              TestBase.java                                                   */
/*                                                                              */
/*      Support code for various SEEDE tests                                    */
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

import org.junit.Assert;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
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

abstract class TestBase implements MintConstants, SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected MintControl   mint_control;
private String          project_name;
protected File          project_directory;    
private Element         seede_result;
private String          bedrock_id;
private String          stopped_thread;

private static final String BROWN_ECLIPSE = "java-2024-098/eclipse/eclipse";
private static final String BROWN_WS = "Eclipse/";
private static final String HOME_ECLIPSE =
   "/vol/Developer/java-2023-12/Eclipse.app/Contents/MacOS/eclipse";
private static final String HOME_WS = "Eclipse/";
private static final String BROWN_SEEDE_LIB = "/research/people/spr/seede/lib";
private static final String HOME_SEEDE_LIB = "/pro/seede/lib";

private static final String [] OPENS;

static {
   OPENS = new String [] { "java.desktop/sun.font", "java.desktop/sun.awt", "java.desktop/sun.swing",
         "java.desktop/javax.swing", "java.base/jdk.internal.math", "java.base/sun.nio.cs", 
         "java.base/java.nio",
         "java.base/sun.util.locale.provider",
         "java.base/jdk.internal.math",
         "java.base/jdk.internal.misc",
         "java.base/java.util","java.base/java.lang",
         "java.base/java.util.concurrent",
         "java.base/sun.util.locale",
         "java.desktop/sun.java2d",
         "java.desktop/sun.java2d.loops",
         "java.desktop/sun.java2d.metal",
         "java.desktop/sun.java2d.pipe",
         "java.desktop/java.awt.geom",
         "java.base/sun.util.calendar",
    };
}

      


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected TestBase(String id,String workspace,String project)
{
   mint_control = MintControl.create("SEEDE_TEST_" + id,MintSyncMode.ONLY_REPLIES);
   
   mint_control.register("<BEDROCK SOURCE='ECLIPSE' TYPE='_VAR_0' />",new BedrockHandler());
   mint_control.register("<SEEDEXEC TYPE='_VAR_0' />",new SeedeHandler()); 
   
   project_name = project;
   project_directory = null;
   seede_result = null;
   bedrock_id = null;
   stopped_thread = null; 
   
   if (workspace != null) {
      setupBedrock(workspace,project);
      startSeede();
    }
   
   Runtime.getRuntime().addShutdownHook(new ShutdownAll());
}



protected void setup(String workspace,String project)
{
   project_name = project;
   setupBedrock(workspace,project);
   startSeede();
}




/********************************************************************************/
/*                                                                              */
/*      Test methods                                                            */
/*                                                                              */
/********************************************************************************/

protected LaunchData startLaunch(String id,int continuecount)
{
   LaunchData ld = doStartLaunch(id);
   
   for (int i = 0; i < continuecount; ++i) {
      doContinueLaunch(ld);
    }
   
   return ld;
}


protected void continueLaunch(LaunchData ld)
{
   doContinueLaunch(ld);
}



protected void setupSeedeSession(String id,LaunchData ld,int framecount)
{
   String frameid = null;
   
   if (framecount != 0) {
      MintDefaultReply stkrply = new MintDefaultReply();
      CommandArgs sargs = new CommandArgs("ARRAY",-1,
            "COUNT",framecount+1,
            "THREAD",ld.getThreadId(),
            "LAUNCH",ld.getLaunchId());
      sendBubblesMessage("GETSTACKFRAMES",project_name,sargs,null,stkrply);
      Element rslt = stkrply.waitForXml();
      Element r1 = IvyXml.getChild(rslt,"STACKFRAMES");
      for (Element t1 : IvyXml.children(r1,"THREAD")) {
         String tid = IvyXml.getAttrString(t1,"ID");
         if (!tid.equals(ld.getThreadId())) continue;
         int ct = 0;
         for (Element f1 : IvyXml.children(t1,"STACKFRAME")) {
            if (ct++ == framecount) {
               frameid = IvyXml.getAttrString(f1,"ID");
               break;
             }
          }
       }
    }
   
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SESSION");
   xw.field("TYPE","LAUNCH");
   xw.field("PROJECT",project_name);
   xw.field("LAUNCHID",ld.getLaunchId());
   xw.field("THREADID",ld.getThreadId());
   if (frameid != null) xw.field("FRAMEID",frameid);
   xw.end("SESSION");
   String cnts = xw.toString();
   xw.close();
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("BEGIN",id,null,cnts,rply);
   Element status = rply.waitForXml();
   Assert.assertTrue(IvyXml.isElement(status,"RESULT"));
}



protected void setupSeedeTestSession(String id,String cnts)
{
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("BEGIN",id,null,cnts,rply);
   Element status = rply.waitForXml();
   Assert.assertTrue(IvyXml.isElement(status,"RESULT"));   
}



protected Element runSeede(String id)
{
   return runSeede(id,1000000);
}



protected Element runSeede(String id,int ct)
{
   MintDefaultReply rply = new MintDefaultReply();
   seede_result = null;
   CommandArgs args = new CommandArgs("EXECID",id,"CONTINUOUS",true,
        "MAXTIME",ct,"MAXDEPTH",100);
   sendSeedeMessage("EXEC",id,args,null,rply);
   String sstatus = rply.waitForString();
   AcornLog.logD("TEST: RESULT IS " + sstatus);
   Element xml = waitForSeedeResult();
   Assert.assertNotNull(xml);
   AcornLog.logI("TEST: SEEDE RESULT IS " + IvyXml.convertXmlToString(xml));
   
   return xml;
}


protected void removeSeede(String id)
{
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("REMOVE",id,null,null,rply);
   String sstatus = rply.waitForString();
   AcornLog.logI("TEST: SEEDE REMOVE STATUS: " + sstatus);
}



protected Element editBedrock(String file,int len,int off,String txt)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("BEDROCK");
   xw.field("BID",bedrock_id);
   xw.field("FILE",file);
   xw.field("LENGTH",len);
   xw.field("OFFSET",off);
   xw.field("SOURCE","ECLIPSE");
   xw.field("TYPE","EDIT");
   if (txt != null) xw.cdata(txt);
   xw.end("BEDROCK");
   mint_control.send(xw.toString());
   xw.close();
   
   Element xml = waitForSeedeResult();
   AcornLog.logI("TEST: SEEDE result after editing Is " + IvyXml.convertXmlToString(xml));
   
   return xml;
}



protected void editSeede(String id,String file,int len,int off,String txt)
{
   File f1 = getProjectFile(file);
   
   int ilen = 0;
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("REPAIREDIT");
   xw.field("FILE",f1.getPath());
   xw.begin("EDIT");
   xw.field("COUNTER",2);
   xw.field("EXCEND",off+len);
   xw.field("ID",1);
   xw.field("INCEND",off+len-1);
   xw.field("OFFSET",off);
   xw.field("TYPE","MULTI");
   if (txt != null) {
      ilen = txt.length();
      xw.begin("EDIT");
      xw.field("COUNTER",3);
      xw.field("EXCEND",off);
      xw.field("ID",2);
      xw.field("INCEND",off-1);
      xw.field("LENGTH",0);
      xw.field("OFFSET",off);
      xw.field("TYPE","INSERT");
      xw.cdataElement("TEXT",txt);
      xw.end("EDIT");
    }
   xw.begin("EDIT");
   xw.field("COUNTER",4);
   xw.field("EXCEND",off+ilen);
   xw.field("ID",3);
   xw.field("INCEND",off+ilen-1);
   xw.field("LENGTH",len);
   xw.field("OFFSET",off);
   xw.field("TYPE","DELETE");
   xw.end("EDIT");
   xw.end("EDIT");
   xw.end("REPAIREDIT");
   
   String cnts = xw.toString();
   xw.close();
   
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("EDITFILE",id,null,cnts,rply);
   String sstatus = rply.waitForString();
   AcornLog.logI("TEST: SEEDE EDITFILE STATUS: " + sstatus); 
}



protected void addSeedeFiles(String id,String... files) 
{
   IvyXmlWriter xw = new IvyXmlWriter();
   for (String s : files) {
      File f1 = getProjectFile(s);
      xw.begin("FILE");
      xw.field("NAME",f1.getPath());
      xw.end("FILE");
    }
   String cnts = xw.toString();
   xw.close();
   
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("ADDFILE",id,null,cnts,rply);
   String sstatus = rply.waitForString();
   AcornLog.logI("TEST: SEEDE ADDFILE STATUS: " + sstatus);
}


protected void addSeedeFiles(String id,File f)
{
   String cnts = null;
   try {
      cnts = IvyFile.loadFile(f);
    }
   catch (IOException e) {
      throw new Error("File " + f + " not found");
    }
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("ADDFILE",id,null,cnts,rply);
   String sstatus = rply.waitForString();
   AcornLog.logI("TEST: SEEDE ADDFILE STATUS: " + sstatus);
}



protected String startSeedeSubsession(String id)
{
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("SUBSESSION",id,null,null,rply);
   Element xml = rply.waitForXml();
   Element sxml = IvyXml.getChild(xml,"SESSION");
   String ssid = IvyXml.getAttrString(sxml,"ID");
   return ssid;
}



protected void setVariable(String id,String var,String valtype,String val)
{
   CommandArgs args = new CommandArgs("VAR",var);
   
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("VALUE");
   xw.field("TYPE",valtype);
   xw.field("VALUE",val);   
   xw.end("VALUE");
   String cnts = xw.toString();
   xw.close();
   
   MintDefaultReply rply = new MintDefaultReply();
   sendSeedeMessage("SETVALUE",id,args,cnts,rply);
   rply.waitForXml();
}


/********************************************************************************/
/*                                                                              */
/*      Setup Eclipse access                                                    */
/*                                                                              */
/********************************************************************************/

private void setupBedrock(String workspace,String project)
{
   AcornLog.logI("TEST: SETTING UP BEDROCK");
   
   File ec1 = getFile(BROWN_ECLIPSE);
   File ec2 = getFile(BROWN_WS + workspace);
   if (!ec1.exists()) {
      ec1 = getFile(HOME_ECLIPSE);
      ec2 = getFile(HOME_WS + workspace);
    }
   if (!ec1.exists()) {
      System.err.println("Can't find bubbles version of eclipse to run");
      throw new Error("No eclipse");
    }
   
   String cmd = ec1.getAbsolutePath();
   cmd += " -application edu.brown.cs.bubbles.bedrock.application";
   cmd += " -data " + ec2.getAbsolutePath();
   cmd += " -nosplash";
   cmd += " -vmargs -Dedu.brown.cs.bubbles.MINT=" + mint_control.getMintName();
   cmd += " -Xmx16000m";
   
   AcornLog.logI("TEST: RUN: " + cmd);
   
   try {
      for (int i = 0; i < 250; ++i) {
         try {
            Thread.sleep(1000);
          }
         catch (InterruptedException e) { }
         if (pingEclipse()) {
            CommandArgs args = new CommandArgs("LEVEL","DEBUG");
            sendBubblesMessage("LOGLEVEL",null,args,null);
            sendBubblesMessage("ENTER");
            MintDefaultReply rply = new MintDefaultReply();
            sendBubblesMessage("OPENPROJECT",project,null,null,rply);
            Element pxml = rply.waitForXml();
            if (!IvyXml.isElement(pxml,"PROJECT")) {
               pxml = IvyXml.getChild(pxml,"PROJECT");
             }
            String dirs = IvyXml.getAttrString(pxml,"PATH");
            if (dirs != null) project_directory = new File(dirs);
            return;
          }
         if (i == 0) {
            SesameMain.pongEclipse();
            new IvyExec(cmd);
          }
       }
    }
   catch (IOException e) { }
   
   throw new Error("Problem running Eclipse: " + cmd);
}


protected File getFile(String f)
{
   File f1 = new File(f);
   if (!f1.isAbsolute()) {
      String home = System.getProperty("user.home");
      File fhome = new File(home);
      f1 = new File(fhome,f);
    }
   
   return f1;
}



protected File getProjectFile(String f)
{
   File f1 = new File(f);
   if (!f1.isAbsolute()) {
      f1 = new File(project_directory,f);
    }
   
   return f1;
}



private boolean pingEclipse()
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendBubblesMessage("PING",null,null,null,mdr);
   String r = mdr.waitForString(500);
   return r != null;
}




/********************************************************************************/
/*                                                                              */
/*      Run SEEDE in separate thread                                            */
/*                                                                              */
/********************************************************************************/

private void startSeede()
{
   AcornLog.logI("TEST","Setting Up Seede Thread");
   
   SeedeThread st = new SeedeThread();
   for (int i = 0; i < 100; ++i) {
      if (pingSeede()) return;
      if (i == 0) st.start();
      try {
         Thread.sleep(1000);
      }
catch (InterruptedException e) { }
    }
   throw new Error("Problem starting sesame server");
}


private boolean pingSeede()
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendSeedeMessage("PING",null,null,null,mdr);
   String r = mdr.waitForString(500);
   return r != null;
}



private class SeedeThread extends Thread {

   SeedeThread() {
      super("SeedeMain");
    }
   
   @Override public void run() {
      String log = getFile("seede.log").getPath();
      System.err.println("LOG in " + log);
      SesameMain.main(new String [] { "-m", mint_control.getMintName(),
            "-T", "-D", "-L", log });
    }
   
}       // end of inner class SeedeThread



/********************************************************************************/
/*                                                                              */
/*      Handle shutdown                                                         */
/*                                                                              */
/********************************************************************************/

private class ShutdownAll extends Thread {
   
   ShutdownAll() {
      super("SeedeTestShutdown");
    }
   
   @Override public void run() {
      AcornLog.logI("TEST: Shutting down");
      sendBubblesMessage("EXIT");
      // sendSeedeMessage("EXIT");       -- seede is running in our process
    }
   
}       // end of inner class ShutdownAll




/********************************************************************************/
/*                                                                              */
/*      Manage run state                                                        */
/*                                                                              */
/********************************************************************************/

private LaunchData doStartLaunch(String name)
{
   stopped_thread = null;
   
   File lib = new File(BROWN_SEEDE_LIB);
   if (!lib.exists()) lib = new File(HOME_SEEDE_LIB);
   String dargs = null;
   
// File f1 = new File(lib,"poppy.jar");
// dargs = "-javaagent:" + f1.getPath();
   for (String s : OPENS) {
      String arg = "--add-opens=" + s + "=ALL-UNNAMED";
      if (dargs == null) dargs = arg;
      else dargs += " " + arg;
    }
   
   stopped_thread = null;
   CommandArgs args = new CommandArgs("NAME",name,"MODE","debug","BUILD","TRUE",
         "REGISTER","TRUE","VMARG",dargs);
   MintDefaultReply rply = new MintDefaultReply();
   sendBubblesMessage("START",project_name,args,null,rply);
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



private void doContinueLaunch(LaunchData ld)
{
   stopped_thread = null;
   
   if (ld == null) return;
   
   CommandArgs args = new CommandArgs("LAUNCH",ld.getLaunchId(),
         "TARGET",ld.getTargetId(),
         "PROCESS",ld.getProcessId(),"ACTION","RESUME");
   MintDefaultReply rply = new MintDefaultReply();
   sendBubblesMessage("DEBUGACTION",project_name,args,null,rply);
   String x = rply.waitForString();
   Assert.assertNotNull(x);
   String threadid = waitForStop();
   Assert.assertNotNull(threadid);
   
   ld.setThreadId(threadid);
}



private String waitForStop()
{
   synchronized (this) {
      for (int i = 0; i < 100; ++i) {
         if (stopped_thread != null) break;
         try {
            wait(3000);
          }
         catch (InterruptedException e) { }
       }
      return stopped_thread;
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


protected static class LaunchData {

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
   
   String getLaunchId()                         { return lanuch_id; }
   String getTargetId()                         { return target_id; }
   String getProcessId()                        { return process_id; }
   String getThreadId()                         { return thread_id; }
   
   void setThreadId(String id)                  { thread_id = id; }
   
}       // end of inner class LaunchData






/********************************************************************************/
/*                                                                              */
/*      Send messages to Bubbles backend                                        */
/*                                                                              */
/********************************************************************************/

protected void sendBubblesMessage(String cmd)
{
   sendBubblesMessage(cmd,null,null,null,null);
}


protected void sendBubblesMessage(String cmd,String proj,CommandArgs flds,String cnts)
{
   sendBubblesMessage(cmd,proj,flds,cnts,null);
}


protected void sendBubblesMessage(String cmd,String proj,CommandArgs flds,String cnts,
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
   
   AcornLog.logD("TEST: SEND to BUBBLES: " + xml);
   
   int fgs = MINT_MSG_NO_REPLY;
   if (rply != null) fgs = MINT_MSG_FIRST_NON_NULL;
   mint_control.send(xml,rply,fgs);
}



/********************************************************************************/
/*                                                                              */
/*      Send messages to SEEDE                                                  */
/*                                                                              */
/********************************************************************************/

protected void sendSeedeMessage(String cmd,String sess,CommandArgs flds,String cnts,
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
/*      Handle Bedrock callbacks                                                */
/*                                                                              */
/********************************************************************************/

private final class BedrockHandler implements MintHandler {

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
            if (bedrock_id == null) {
               bedrock_id = IvyXml.getAttrString(e,"BID");
             }
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




/********************************************************************************/
/*                                                                              */
/*      Handle results from SEEDE                                               */
/*                                                                              */
/********************************************************************************/

protected Element waitForSeedeResult()
{
   synchronized (this) {
      for (int i = 0; i < 5000; ++i) {
         if (seede_result != null) break;
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



private final class SeedeHandler implements MintHandler {
   
   @Override public void receive(MintMessage msg,MintArguments args) {
      AcornLog.logI("TEST: Received from seede: " + msg.getText());
      String what = args.getArgument(0);
      Element xml = msg.getXml();
      switch (what) {
         case "EXEC" :
            synchronized (TestBase.this) {
               seede_result = xml;
               TestBase.this.notifyAll();
             }
            break;
       }
      msg.replyTo();
    }

}       // end of inner class SeedeHandler



}       // end of class TestBase




/* end of TestBase.java */

