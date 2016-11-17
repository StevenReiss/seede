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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.mint.MintConstants.MintSyncMode;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cumin.CuminRunner;
import edu.brown.cs.seede.cumin.CuminConstants;
import edu.brown.cs.seede.cumin.CuminRunError;



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

   mint_control = MintControl.create(sm.getMintId(),MintSyncMode.ONLY_REPLIES
   );
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
   String r = rply.waitForString(30000);
   if (r == null) return false;
   return true;
}



private class WaitForExit extends Thread {

   WaitForExit() {
      super("WaitForExit");
      setDaemon(true);
    }

   @Override public void run() {
      synchronized (this) {
	 for ( ; ; ) {
	    if (checkEclipse()) break;
	    try {
	       wait(30000l);
	     }
	    catch (InterruptedException e) { }
	  }

	 while (!is_done) {
	    if (!checkEclipse()) is_done = true;
	    else {
	       try {
		  wait(30000l);
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
/*	Sending methods 							*/
/*										*/
/********************************************************************************/

void sendMessage(String xml,MintReply rply,int fgs)
{
   mint_control.send(xml,rply,fgs);
}




/********************************************************************************/
/*										*/
/*	File-related message handlers						*/
/*										*/
/********************************************************************************/

private void handleErrors(String proj,File file,int id,Element messages)
{
   // nothing to do here
}


private void handleEdit(String bid,File file,int len,int offset,boolean complete,
      boolean remove,String txt)
{
   sesame_control.getFileManager().handleEdit(file,len,offset,complete,txt);
   
   for (SesameSession ss : session_map.values()) {
      SesameProject sp = ss.getProject();
      boolean fnd = false;
      for (SesameFile sf : sp.getActiveFiles()) {
         if (sf.getFile().equals(file)) {
            fnd = true;
            break;
          }
       }
      if (fnd) ss.stopRunners();
    }
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
   System.err.println("BEGIN " + sid + " " + ss);
   xw.begin("SESSION");
   xw.field("ID",ss.getSessionId());
   xw.end();
   session_map.put(sid,ss);
}


private void handleExec(String sid,Element xml,IvyXmlWriter xw) 
        throws SesameException
{
   String xid = IvyXml.getAttrString(xml,"EXECID");
   
   SesameSession ss = session_map.get(sid);
   if (ss == null) throw new SesameException("Session " + sid + " not found");
   MethodDeclaration mthd = ss.getCallMethod();
   List<CashewValue> args = ss.getCallArgs();
   SesameContext gblctx = new SesameContext(ss);
   CuminRunner cr = CuminRunner.createRunner(ss.getProject(),
         gblctx,mthd,args);
   
   if (xid != null) {
      ExecRunner execer = new ExecRunner(ss,xid,cr);
      ss.addRunner(execer);
      execer.start();
      return;
    }
   
   CuminRunError sts = null;
   try {
      cr.interpret(CuminConstants.EvalType.RUN);
    }
   catch (CuminRunError r) {
      sts = r;
    }
   
   if (sts == null) xw.textElement("STATUS","OK");
   else {
      if (sts.getValue() != null) {
         xw.begin("RETURN");
         sts.getValue().outputXml(xw);
         xw.end("RETURN");
       }
      CashewContext ctx = cr.getLookupContext();
      ctx.outputXml(xw);
    }
}



private class ExecRunner extends Thread {
   
   private SesameSession for_session;
   private CuminRunner cumin_runner;
   private String      reply_id;
   
   ExecRunner(SesameSession ss,String rid,CuminRunner runner) {
      super("SeedeExec_" + rid);
      for_session = ss;
      cumin_runner = runner;
      reply_id = rid;
    }
   
   @Override public void run() {
      CuminRunError sts = null;
      try {
         cumin_runner.interpret(CuminConstants.EvalType.RUN);
       }
      catch (CuminRunError r) {
         sts = r;
       }
      catch (Throwable t) {
         sts = new CuminRunError(t);
       }
      
      IvyXmlWriter xw = new IvyXmlWriter();
      xw.begin("SEEDEEXEC");
      xw.field("TYPE","EXEC");
      if (reply_id != null) xw.field("ID",reply_id);
      if (isInterrupted() || sts == null) xw.field("EMPTY",true);
      else {
         if (sts.getValue() != null) {
            xw.begin("RETURN");
            sts.getValue().outputXml(xw);
            xw.end("RETURN");
          }
         CashewContext ctx = cumin_runner.getLookupContext();
         ctx.outputXml(xw);
       } 
      xw.end("SEEDEEXEC");
      if (reply_id != null) {
         MintDefaultReply hdlr = new MintDefaultReply();
         sendMessage(xw.toString(),hdlr,MintConstants.MINT_MSG_FIRST_REPLY);
         hdlr.waitFor();
       }
      xw.close();
    }
   
}       // end of inner class ExecRunner





private void handleRemove(String sid) throws SesameException
{
   SesameSession ss = session_map.remove(sid);
   if (ss == null) throw new SesameException("Session " + sid + " not found");
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
         case "PING" :
            msg.replyTo("<PONG/>");
            break;
         case "EDITERROR" :
         case "FIEERROR" :
            handleErrors(IvyXml.getAttrString(e,"PROJECT"),
        	  new File(IvyXml.getAttrString(e,"FILE")),
        	  IvyXml.getAttrInt(e,"ID",-1),
        	  IvyXml.getChild(e,"MESSAGES"));
            break;
         case "EDIT" :
            String txt = IvyXml.getText(e);
            boolean complete = IvyXml.getAttrBool(e,"COMPLETE");
            boolean remove = IvyXml.getAttrBool(e,"REMOVE");
            if (complete) {
               byte [] data = IvyXml.getBytesElement(e,"CONTENTS");
               if (data != null) txt = new String(data);
               else remove = true;
             }
            handleEdit(IvyXml.getAttrString(e,"BID"),
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
            String bid = IvyXml.getAttrString(e,"BID");
            String id = IvyXml.getAttrString(e,"ID");
            if ((bid == null || bid.equals(SOURCE_ID)) && id != null) {
               EvalData ed = eval_handlers.remove(id);
               if (ed != null) {
        	  ed.handleResult(e);
        	}
             }
            msg.replyTo("<OK/>");
            break;
         case "STOP" :
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
         handleBegin(sid,IvyXml.getChild(e,"SESSION"),xw);
         break;
      case "EXEC" :
         handleExec(sid,e,xw);
         break;
      case "REMOVE" :
         handleRemove(sid);
         break;
      case "ADDFILE" :
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
      String cmd = args.getArgument(0);
      String sid = args.getArgument(1);
      Element e = msg.getXml();
      String rslt = null;
      try {
         rslt = processCommand(cmd,sid,e);
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

private class EvalData {

   EvalData() {
    }

   void handleResult(Element xml) {
    }

}	// end of inner class EvalData




}	// end of class SesameMonitor




/* end of SesameMonitor.java */














































































































































































































































































































































































































































































































































































