/********************************************************************************/
/*										*/
/*		SesameMain.java 						*/
/*										*/
/*	Main program for SEEDE assistance					*/
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
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;

public final class SesameMain implements SesameConstants, MintConstants
{




/********************************************************************************/
/*										*/
/*	Main Program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   SesameMain sm = new SesameMain(args);
   sm.process();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String			message_id;
private SesameFileManager	file_manager;
private SesameMonitor		message_monitor;
private Map<String,SesameProject> project_map;
private boolean                 compute_tostring;
private boolean                 compute_toarray;
private long                     timeout_error;

private static JcompControl	jcomp_base;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private SesameMain(String [] args)
{
   AcornLog.setup();
   
   message_id = null;
   project_map = new HashMap<String,SesameProject>();
   jcomp_base = new JcompControl();
   compute_tostring = false;
   compute_toarray = false;
   timeout_error = -1;
   
   scanArgs(args);

   file_manager = new SesameFileManager(this);
   message_monitor = new SesameMonitor(this);
}



/********************************************************************************/
/*										*/
/*	Argument scanning methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-m") && i+1 < args.length) {           // -m <MINTID>
	    message_id = args[++i];
	  }
	 else if (args[i].startsWith("-T")) {                           // -Trace
	    AcornLog.setTracing(true);
	  }
	 else if (args[i].startsWith("-D")) {                           // -Debug
	    AcornLog.setLogLevel(AcornLog.LogLevel.DEBUG);
	  }
	 else if (args[i].startsWith("-E")) {                           // -Error
	    AcornLog.useStdErr(true);
	  }
	 else if (args[i].startsWith("-L") && i+1 < args.length) {      // -L logfile
	    AcornLog.setLogFile(new File(args[++i]));
	  }
         else if (args[i].startsWith("-s")) {
            compute_tostring = !compute_tostring;
          }
         else if (args[i].startsWith("-a")) {
            compute_toarray = !compute_toarray;
          }
         else if (args[i].startsWith("-time") && i+1 < args.length) {   // -timeout <val>
            timeout_error = Long.parseLong(args[++i]);
          }
	 else badArgs();
       }
      else badArgs();
    }

  if (message_id == null) {
     message_id = System.getProperty("edu.brown.cs.bubbles.MINT");
     if (message_id == null) message_id = System.getProperty("edu.brown.cs.bubbles.mint");
     if (message_id == null) message_id = BOARD_MINT_NAME;
   }
}



private void badArgs()
{
   System.err.println("Sesame: SesameMain -m <message_id>");
   System.exit(1);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

static JcompControl getJcompBase()		{ return jcomp_base; }

SesameFileManager getFileManager()		{ return file_manager; }

boolean getComputeToString()                    { return compute_tostring; }

boolean getComputeToArray()                     { return compute_toarray; }

long getTimeoutError()                       { return timeout_error; }



void noteFileChanged(SesameFile sf)
{
   message_monitor.noteFileChanged(sf);
}

SesameMonitor getMonitor()			{ return message_monitor; }

String getMintId()				{ return message_id; }




/********************************************************************************/
/*										*/
/*	Project managment							*/
/*										*/
/********************************************************************************/

SesameProject getProject(String name)
{
   if (name == null) return null;

   synchronized (project_map) {
      SesameProject sp = project_map.get(name);
      if (sp == null) {
	 sp = new SesameProject(this,name);
	 project_map.put(name,sp);
       }
      return sp;
    }
}



void removeProject(SesameProject sp)
{
   if (sp == null) return;
   if (!sp.isLocal()) {
      SesameProject np = project_map.remove(sp.getName());
      if (np == null) return;
      else if (np != sp) {
         project_map.put(np.getName(),np);
         return;
       }
    }
   sp.removeProject();
}



/********************************************************************************/
/*										*/
/*	Messaging methods							*/
/*										*/
/********************************************************************************/

void sendMessage(String cmd,String proj,Map<String,Object> flds,String cnts)
{
   sendMessage(cmd,proj,flds,cnts,null,MINT_MSG_NO_REPLY);
}

String getStringReply(String cmd,String proj,Map<String,Object> flds,String cnts,long delay)
{
   MintDefaultReply rply = new MintDefaultReply();
   sendMessage(cmd,proj,flds,cnts,rply,MINT_MSG_FIRST_REPLY);
   String rslt = rply.waitForString(delay);

   AcornLog.logD("Reply: " + rslt);

   return rslt;
}


Element getXmlReply(String cmd,SesameProject sproj,Map<String,Object> flds,String cnts,long delay)
{
   String proj = null;
   if (sproj != null) proj = sproj.getName();

   MintDefaultReply rply = new MintDefaultReply();
   sendMessage(cmd,proj,flds,cnts,rply,MINT_MSG_FIRST_NON_NULL);
   Element rslt = rply.waitForXml(delay);

   AcornLog.logD("Reply: " + IvyXml.convertXmlToString(rslt));

   return rslt;
}


Element waitForEvaluation(String id)
{
   return message_monitor.waitForEvaluation(id);
}


private void sendMessage(String cmd,String proj,Map<String,Object> flds,String cnts,
      MintReply rply,int fgs)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("BUBBLES");
   xw.field("DO",cmd);
   xw.field("BID",SOURCE_ID);
   if (proj != null && proj.length() > 0) xw.field("PROJECT",proj);
   xw.field("LANG","Eclipse");
   if (flds != null) {
      for (Map.Entry<String,Object> ent : flds.entrySet()) {
	 xw.field(ent.getKey(),ent.getValue());
       }
    }
   if (cnts != null) {
      xw.xmlText(cnts);
    }
   xw.end("BUBBLES");
   String msg = xw.toString();
   xw.close();

   AcornLog.logD("SEND: " + msg);

   message_monitor.sendMessage(msg,rply,fgs);
}


/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void process()
{
   message_monitor.startServer();
}




}	// end of class SesameMain




/* end of SesameMain.java */

