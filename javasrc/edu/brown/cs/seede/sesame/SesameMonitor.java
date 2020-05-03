/********************************************************************************/
/*										*/
/*		SesameMonitor.java						*/
/*										*/
/*	Message interface for SEEDE						*/
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



package edu.brown.cs.seede.sesame;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.mint.MintConstants.MintSyncMode;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cumin.CuminRunner;



class SesameMonitor implements SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SesameMain		sesame_control;
private MintControl		mint_control;
private boolean 		is_done;
private Map<String,SesameSession> session_map;
private Map<String,EvalData>	eval_handlers;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameMonitor(SesameMain sm)
{
   sesame_control = sm;
   is_done = false;
   eval_handlers = new HashMap<String,EvalData>();
   session_map = new HashMap<String,SesameSession>();

   mint_control = MintControl.create(sm.getMintId(),MintSyncMode.ONLY_REPLIES);
}



/********************************************************************************/
/*										*/
/*	Server methods								*/
/*										*/
/********************************************************************************/

void startServer()
{
   mint_control.register("<BEDROCK SOURCE='ECLIPSE' TYPE='_VAR_0' />",new EclipseHandler());
   mint_control.register("<BUBBLES DO='_VAR_0' />",new BubblesHandler());
   mint_control.register("<SEEDE DO='_VAR_0' SID='_VAR_1' />",new CommandHandler());

   new WaitForExit().start();
}



private synchronized void serverDone()
{
   is_done = true;
   notifyAll();
}



private boolean checkEclipse()
{
   MintDefaultReply rply = new MintDefaultReply();
   String msg = "<BUBBLES DO='PING' />";
   mint_control.send(msg,rply,MintConstants.MINT_MSG_FIRST_NON_NULL);
   String r = rply.waitForString(300000);
   AcornLog.logD("BUBBLES PING " + r);
   if (r == null) return false;
   return true;
}



private class WaitForExit extends Thread {

   WaitForExit() {
      super("WaitForExit");
    }

   @Override public void run() {
      SesameMonitor mon = SesameMonitor.this;
      synchronized (mon) {
	 for ( ; ; ) {
	    if (checkEclipse()) break;
	    try {
	       mon.wait(30000l);
	     }
	    catch (InterruptedException e) { }
	  }

	 while (!is_done) {
	    if (!checkEclipse()) is_done = true;
	    else {
	       try {
		  mon.wait(30000l);
		}
	       catch (InterruptedException e) { }
	     }
	  }
       }

      System.exit(0);
    }

}	// end of inner class WaitForExit



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

void noteFileChanged(SesameFile sf)
{
   AcornLog.logD("MONITOR: begin note changed");
   for (SesameSession ss : session_map.values()) {
      ss.noteFileChanged(sf);
    }
   AcornLog.logD("MONITOR: end note changed");
}




/********************************************************************************/
/*										*/
/*	Sending methods 							*/
/*										*/
/********************************************************************************/

void sendMessage(String xml,MintReply rply,int fgs)
{
   mint_control.send(xml,rply,fgs);
}



void sendCommand(String cmd,CommandArgs args,String cnts,MintReply rply)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SEEDEXEC");
   xw.field("TYPE",cmd);
   if (args != null) {
      for (Map.Entry<String,Object> ent : args.entrySet()) {
	 xw.field(ent.getKey(),ent.getValue());
       }
    }
   if (cnts != null) xw.xmlText(cnts);
   xw.end("SEEDEXEC");

   String msg = xw.toString();
   xw.close();

   AcornLog.logD("Send to Bubbles: " + msg);
   if (rply != null) {
      sendMessage(msg,rply,MintConstants.MINT_MSG_FIRST_REPLY);
    }
   else {
      sendMessage(msg,null,MintConstants.MINT_MSG_NO_REPLY);
    }
}




/********************************************************************************/
/*										*/
/*	File-related message handlers						*/
/*										*/
/********************************************************************************/

private void handleErrors(String proj,File file,Element messages)
{
   boolean chng = sesame_control.getFileManager().handleErrors(file,messages);

   if (!chng) return;

   for (SesameSession ss : session_map.values()) {
      SesameProject sp = ss.getProject();
      SesameFile fnd = null;
      for (SesameFile sf : sp.getActiveFiles()) {
	 if (sf.getFile().equals(file)) {
	    fnd = sf;
	    break;
	  }
       }
      AcornLog.logD("EDIT RESTART " + fnd);
      if (fnd != null) {
	 ss.restartRunners();
       }
    }
}


private void handleEdit(MintMessage msg,String bid,File file,int len,int offset,
      boolean complete,boolean remove,String txt)
{
   if (!bid.equals(SOURCE_ID)) {
      msg.replyTo();
      return;
    }

   AcornLog.logD("MONITOR: Begin edit");

   SesameFile sf = sesame_control.getFileManager().handleEdit(file,len,offset,complete,txt);

   msg.replyTo("<OK/>");

   AcornLog.logD("MONITOR: EDIT ACCEPTED");

   if (sf != null) sesame_control.noteFileChanged(sf);

   AcornLog.logD("MONITOR: End edit");
}



private void handleResourceChange(Element res)
{
   // detect file saved.  This will come from the edit
}



/********************************************************************************/
/*										*/
/*	Run time message handlers						*/
/*										*/
/********************************************************************************/

private void handleRunEvent(Element event,long when)
{
   String kind = IvyXml.getAttrString(event,"KIND");
   String detail = IvyXml.getAttrString(event,"DETAIL");
   if (detail != null && detail.equals("EVALUATION")) return;

   switch (kind) {
      case "RESUME" :
	 Element thrd = IvyXml.getChild(event,"THREAD");
	 String tid = null;
	 String lid = null;
	 if (thrd != null) {
	    tid = IvyXml.getAttrString(thrd,"ID");
	    lid = IvyXml.getAttrString(IvyXml.getChild(thrd,"LAUNCH"),"ID");
	  }
	 else {
	    Element tgt = IvyXml.getChild(event,"TARGET");
	    lid = IvyXml.getAttrString(IvyXml.getChild(tgt,"LAUNCH"),"ID");
	  }
	 for (SesameSession ss : session_map.values()) {
	    ss.noteContinue(lid,tid);
	  }

	 break;
      case "SUSPEND" :
	 break;
      default :
	 return;
    }
}


private void handleConsoleEvent(Element e)
{

}




/********************************************************************************/
/*										*/
/*	Methods to handle commands						*/
/*										*/
/********************************************************************************/

private void handleBegin(String sid,Element xml,IvyXmlWriter xw) throws SesameException
{
   SesameSession ss = SesameSession.createSession(sesame_control,sid,xml);
   AcornLog.logD("BEGIN " + sid + " " + ss);
   xw.begin("SESSION");
   xw.field("ID",ss.getSessionId());
   xw.end();
   session_map.put(sid,ss);
   
   BeginHandler bh = new BeginHandler(ss);
   bh.start();
}



private class BeginHandler extends Thread {
   
   private SesameSession for_session;
   
   BeginHandler(SesameSession ss) {
      super("BEGIN_" + ss.getSessionId());
      for_session = ss;
    }
   
   @Override public void run() {
      for_session.setupSession();
    }
   
}       // end of inner class BeginHandler


private void handleExec(String sid,Element xml,IvyXmlWriter xw)
	throws SesameException
{
   String xid = IvyXml.getAttrString(xml,"EXECID","TEST_XID");
   boolean iscont = IvyXml.getAttrBool(xml,"CONTINUOUS");
   long maxtime = IvyXml.getAttrLong(xml,"MAXTIME",10000000);
   int maxdepth = IvyXml.getAttrInt(xml,"MAXDEPTH",750);

   SesameSession ss = session_map.get(sid);
   if (ss == null) throw new SesameException("Session " + sid + " not found");
   AcornLog.logD("WAIT FOR SESSION READY");
   ss.waitForReady();
   SesameContext gblctx = new SesameContext(ss);
   
   AcornLog.logD("COMPILE PROJECT");
   ss.getProject().compileProject();
   SesameExecRunner execer = null;
   int nr = 0;
   for (SesameLocation loc : ss.getActiveLocations()) {
      CuminRunner cr = ss.createRunner(loc,gblctx);
      if (cr == null) continue;
      ++nr;
      if (execer == null) {
	 execer = new SesameExecRunner(ss,xid,gblctx,iscont,maxtime,maxdepth,cr);
	 ss.addRunner(execer);
       }
      else {
	 execer.addRunner(cr);
       }
    }
   AcornLog.logD("START RUNNER " + nr + " " + execer);
   if (execer != null) {
      execer.startExecution();
    }
   else throw new SesameException("Session " + sid + " has no starting points");
}



private void handleRemove(String sid) throws SesameException
{
   SesameSession ss = session_map.remove(sid);
   if (ss == null) AcornLog.logE("Session " + sid + " not found");
   else {
      ss.stopRunners();
      SesameProject sp = ss.getProject();
      boolean inuse = false;
      for (SesameSession ns : session_map.values()) {
	 SesameProject np = ns.getProject();
	 if (np == sp) inuse = true;
       }
      if (!inuse) sesame_control.removeProject(sp);
    }
}



private void handleAddFile(String sid,Element xml)
{
   SesameSession ss = session_map.get(sid);
   if (ss == null) return;
   SesameProject sp = ss.getProject();

   for (Element e : IvyXml.children(xml,"FILE")) {
      String file = IvyXml.getAttrString(e,"NAME");
      SesameFile sf = sesame_control.getFileManager().openFile(new File(file));
      if (sf != null) sp.addFile(sf);
    }

   ss.resetRunners();
}



private void handleSetValue(String sid,Element xml) throws SesameException
{
   SesameSession ss = session_map.get(sid);
   if (ss == null) return;

   String var = IvyXml.getAttrString(xml,"VAR");
   Element val = IvyXml.getChild(xml,"VALUE");
   if (val != null) {
      ss.setInitialValue(var,val);
    }
   else {
      String thread = IvyXml.getTextElement(xml,"THREAD");
      String expr = IvyXml.getTextElement(xml,"EXPR");
      ss.setInitialValue(var,thread,expr);
    }
}


private void handleDefField(String sid,Element xml) throws SesameException
{
   SesameSession ss = session_map.get(sid);
   if (ss == null) return;

   String cls = IvyXml.getAttrString(xml,"CLASS");
   String fld = IvyXml.getAttrString(xml,"FIELD");
   Element val = IvyXml.getChild(xml,"VALUE");
   if (val != null) {
      // handle simple value given for field
    }
   else {
      // handle expression given for field
    }
   System.err.println("HANDLE DEFINE FIELD" + cls + " " + fld);
}



private void handleSwingComponent(String sid,Element xml) throws SesameException
{
   SesameSession ss = session_map.get(sid);
   if (ss == null) return;

   String name = IvyXml.getAttrString(xml,"VARIABLE");
   ss.addSwingComponent(name);
   ss.restartRunners();
}




private void handleVarHistory(String sid,Element xml,IvyXmlWriter xw) throws SesameException
{
   SesameSession ss = session_map.get(sid);
   if (ss == null) return;

   SesameVarHistory vh = new SesameVarHistory(ss,xml);
   vh.process(xw);
}



private void handleToString(String sid,Element xml,IvyXmlWriter xw) throws SesameException
{
   SesameSession ss = session_map.get(sid);
   if (ss == null) return;
   boolean fg = IvyXml.getAttrBool(xml,"VALUE");
   int ct = 0;

   // handle requests for individual variables
   // for (Element vxml : IvyXml.children(xml,"VARIABLE")) {
      // ++ct;
    // }

   if (ct == 0) {
      ss.setComputeToString(fg);
      ss.resetRunners();
    }
}


private void handleExpand(String sid,Element xml,IvyXmlWriter xw) throws SesameException
{
   SesameSession ss = session_map.get(sid);
   if (ss == null) return;
   String var = IvyXml.getAttrString(xml,"VARIABLE");
   if (var == null) return;
   ss.addExpandName(var);
   ss.restartRunners();
}




/********************************************************************************/
/*										*/
/*	Handle Messages from Eclipse						*/
/*										*/
/********************************************************************************/

private class EclipseHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      Element e = msg.getXml();
   
      switch (cmd) {
         case "ELISION" :
            return;
       }
   
      AcornLog.logD("Message from eclipse: " + cmd + " " + msg.getText());
   
      switch (cmd) {
         case "PING" :
         case "PING1" :
         case "PING2" :
         case "PING3" :
            msg.replyTo("<PONG/>");
            break;
         case "EDITERROR" :
         case "FILEERROR" :
            handleErrors(IvyXml.getAttrString(e,"PROJECT"),
        	  new File(IvyXml.getAttrString(e,"FILE")),
        	  IvyXml.getChild(e,"MESSAGES"));
            break;
         case "EDIT" :
            String bid = IvyXml.getAttrString(e,"BID");
            if (!bid.equals(SOURCE_ID)) {
               msg.replyTo();
               break;
             }
            String txt = IvyXml.getText(e);
            boolean complete = IvyXml.getAttrBool(e,"COMPLETE");
            boolean remove = IvyXml.getAttrBool(e,"REMOVE");
            if (complete) {
               byte [] data = IvyXml.getBytesElement(e,"CONTENTS");
               if (data != null) txt = new String(data);
               else remove = true;
             }
            handleEdit(msg,bid,
        	  new File(IvyXml.getAttrString(e,"FILE")),
        	  IvyXml.getAttrInt(e,"LENGTH"),
        	  IvyXml.getAttrInt(e,"OFFSET"),
        	  complete,remove,txt);
            break;
         case "RUNEVENT" :
            long when = IvyXml.getAttrLong(e,"TIME");
            for (Element re : IvyXml.children(e,"RUNEVENT")) {
               handleRunEvent(re,when);
             }
            break;
         case "RESOURCE" :
            for (Element re : IvyXml.children(e,"DELTA")) {
               handleResourceChange(re);
             }
            break;
         case "CONSOLE" :
            handleConsoleEvent(e);
            break;
         case "EVALUATION" :
            AcornLog.logD("Eclipse Message: " + msg.getText());
            bid = IvyXml.getAttrString(e,"BID");
            String id = IvyXml.getAttrString(e,"ID");
            if ((bid == null || bid.equals(SOURCE_ID)) && id != null) {
               EvalData ed = new EvalData(e);
               synchronized (eval_handlers) {
        	  eval_handlers.put(id,ed);
        	  eval_handlers.notifyAll();
        	}
             }
            msg.replyTo("<OK/>");
            break;
         case "STOP" :
            AcornLog.logD("Eclipse Message: " + msg.getText());
            serverDone();
            break;
       }
    }

}	// end of inner class EclipseHandler



/********************************************************************************/
/*										*/
/*	Handle messages from Bubbles						*/
/*										*/
/********************************************************************************/

private class BubblesHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      // Element e = msg.getXml();
      switch (cmd) {
	 case "EXIT" :
	    serverDone();
	    break;
       }
      msg.replyTo();
    }

}	// end of inner class BubblesHandler



/********************************************************************************/
/*										*/
/*	Command handler 							*/
/*										*/
/********************************************************************************/

private String processCommand(String cmd,String sid,Element e) throws SesameException
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("RESULT");
   switch (cmd) {
      case "PING" :
	 xw.text("PONG");
	 break;
      case "EXIT" :
	 System.exit(0);
	 break;
      case "BEGIN" :
	 handleBegin(sid,e,xw);
	 break;
      case "EXEC" :
	 handleExec(sid,e,xw);
	 break;
      case "REMOVE" :
	 handleRemove(sid);
	 break;
      case "ADDFILE" :
	 handleAddFile(sid,e);
	 break;
      case "SETVALUE" :
	 handleSetValue(sid,e);
	 break;
      case "DEFFIELD" :
	 handleDefField(sid,e);
	 break;
      case "SWING" :
	 handleSwingComponent(sid,e);
	 break;
      case "VARHISTORY" :
	 handleVarHistory(sid,e,xw);
	 break;
      case "TOSTRING" :
	 handleToString(sid,e,xw);
	 break;
      case "EXPAND" :
	 handleExpand(sid,e,xw);
	 break;
      default :
	 AcornLog.logE("Unknown command " + cmd);
	 break;
    }
   xw.end("RESULT");
   String rslt = xw.toString();
   xw.close();

   return rslt;
}


private class CommandHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      AcornLog.logD("PROCESS COMMAND: " + msg.getText());
      String cmd = args.getArgument(0);
      String sid = args.getArgument(1);
      Element e = msg.getXml();
      String rslt = null;
      try {
         rslt = processCommand(cmd,sid,e);
         AcornLog.logD("COMMAND RESULT: " + rslt);
       }
      catch (SesameException t) {
         String xmsg = "BEDROCK: error in command " + cmd + ": " + t;
         AcornLog.logE(xmsg,t);
         IvyXmlWriter xw = new IvyXmlWriter();
         xw.cdataElement("ERROR",xmsg);
         rslt = xw.toString();
         xw.close();
       }
      catch (Throwable t) {
         String xmsg = "Problem processing command " + cmd + ": " + t;
         AcornLog.logE(xmsg,t);
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         t.printStackTrace(pw);
         Throwable xt = t;
         for ( ; xt.getCause() != null; xt = xt.getCause());
         if (xt != null && xt != t) {
            pw.println();
            xt.printStackTrace(pw);
          }
         AcornLog.logE("TRACE: " + sw.toString());
         IvyXmlWriter xw = new IvyXmlWriter();
         xw.begin("ERROR");
         xw.textElement("MESSAGE",xmsg);
         xw.cdataElement("EXCEPTION",t.toString());
         xw.cdataElement("STACK",sw.toString());
         xw.end("ERROR");
         rslt = xw.toString();
         xw.close();
         pw.close();
       }
      msg.replyTo(rslt);
    }

}	// end of inner class CommandHandler





/********************************************************************************/
/*										*/
/*	Evaluation handler							*/
/*										*/
/********************************************************************************/

Element waitForEvaluation(String id)
{
   synchronized (eval_handlers) {
      for ( ; ; ) {
	 EvalData ed = eval_handlers.remove(id);
	 if (ed != null) {
	    return ed.getResult();
	  }
	 try {
	    eval_handlers.wait(5000);
	  }
	 catch (InterruptedException e) { }
       }
    }
}



private static class EvalData {

   private Element eval_result;

   EvalData(Element rslt) {
      eval_result = rslt;
    }

   Element getResult() {
      return eval_result;
    }

}	// end of inner class EvalData




}	// end of class SesameMonitor




/* end of SesameMonitor.java */
