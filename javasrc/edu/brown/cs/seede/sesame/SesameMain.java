/********************************************************************************/
/*                                                                              */
/*              SesameMain.java                                                 */
/*                                                                              */
/*      Main program for SEEDE assistance                                       */
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



package edu.brown.cs.seede.sesame;

import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.board.BoardConstants;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class SesameMain implements SesameConstants, MintConstants 
{




/********************************************************************************/
/*                                                                              */
/*      Main Program                                                            */
/*                                                                              */
/********************************************************************************/


public static void main(String [] args) 
{
   SesameMain sm = new SesameMain(args);
   sm.process();
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String                  message_id;
private SesameFileManager       file_manager;
private SesameMonitor           message_monitor;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private SesameMain(String [] args)
{
   message_id = null;
   
   scanArgs(args);
   
   file_manager = new SesameFileManager(this);
   message_monitor = new SesameMonitor(this);
}



/********************************************************************************/
/*                                                                              */
/*      Argument scanning methods                                               */
/*                                                                              */
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
         if (args[i].startsWith("-m") && i+1 < args.length) {
            message_id = args[++i];
          }
         else badArgs();
       }
      else badArgs();
    }
   
  if (message_id == null) {
     message_id = System.getProperty("edu.brown.cs.bubbles.MINT");
     if (message_id == null) message_id = System.getProperty("edu.brown.cs.bubbles.mint");
     if (message_id == null) message_id = BoardConstants.BOARD_MINT_NAME;
   }
}



private void badArgs()
{
   System.err.println("Sesame: SesameMain -m <message_id>");
   System.exit(1);
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

SesameFileManager getFileManager()              { return file_manager; }
SesameMonitor getMonitor()                      { return message_monitor; }

String getMintId()                              { return message_id; }




/********************************************************************************/
/*                                                                              */
/*      Messaging methods                                                       */
/*                                                                              */
/********************************************************************************/
 
void sendMessage(String cmd,String proj,Map<String,Object> flds,String cnts)
{
   sendMessage(cmd,proj,flds,cnts,null,MINT_MSG_NO_REPLY);
}

String getStringReply(String cmd,String proj,Map<String,Object> flds,String cnts,long delay)
{
   MintDefaultReply rply = new MintDefaultReply();
   sendMessage(cmd,proj,flds,cnts,rply,MINT_MSG_FIRST_REPLY);
   return rply.waitForString(delay);
}


Element getXmlReply(String cmd,String proj,Map<String,Object> flds,String cnts,long delay)
{
   MintDefaultReply rply = new MintDefaultReply();
   sendMessage(cmd,proj,flds,cnts,rply,MINT_MSG_FIRST_REPLY);
   return rply.waitForXml(delay);
}


private void sendMessage(String cmd,String proj,Map<String,Object> flds,String cnts,
      MintReply rply,int fgs)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("BUBBLES");
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
   
   SesameLog.logD("SEND: " + msg);
   
   message_monitor.sendMessage(msg,rply,fgs);
}


/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

private void process()
{
   message_monitor.startServer();
}




}       // end of class SesameMain




/* end of SesameMain.java */

