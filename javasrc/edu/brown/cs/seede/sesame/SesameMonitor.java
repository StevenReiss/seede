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
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;



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



private void checkEclipse()
{
   MintDefaultReply rply = new MintDefaultReply();
   String msg = "<BUBBLES DO='PING' />";
   mint_control.send(msg,rply,MintConstants.MINT_MSG_FIRST_NON_NULL);
   String r = rply.waitForString(30000);
   if (r == null) is_done = true;
}



private class WaitForExit extends Thread {
   
   WaitForExit() {
      super("WaitForExit");
      setDaemon(true);
    }
   
   @Override public void run() {
      synchronized (this) {
         while (!is_done) {
            checkEclipse();
            try {
               wait(30000l);
             }
            catch (InterruptedException e) { }
          }
       }
      
      System.exit(0);
    }
   
}       // end of inner class WaitForExit




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
/*                                                                              */
/*      Methods to handle commands                                              */
/*                                                                              */
/********************************************************************************/

private void handleBegin(String sid,Element xml,IvyXmlWriter xw)
{
   
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
	    msg.replyTo("PONG");
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
      Element e = msg.getXml();
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

private class CommandHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      String sid = args.getArgument(1);
      Element e = msg.getXml();
      IvyXmlWriter xw = new IvyXmlWriter();
      switch (cmd) {
         case "PING" :
            xw.begin("PONG");
            xw.end();
            break;
         case "EXIT" :
            System.exit(0);
            break;
         case "BEGIN" :
            handleBegin(sid,IvyXml.getChild(e,"SESSION"),xw);
            break;
         case "ADDFILE" :
            break;
         default :
            SesameLog.logE("Unknown command " + cmd);
            break;
       }
      String rslt = xw.toString();
      xw.close();
      if (rslt != null &&rslt.trim().length() > 0) {
         msg.replyTo(rslt);
       }
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

