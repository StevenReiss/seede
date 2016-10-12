/********************************************************************************/
/*                                                                              */
/*              SesameMonitor.java                                              */
/*                                                                              */
/*      Message interface for SEEDE                                             */
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

import org.w3c.dom.Element;

import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintConstants.MintSyncMode;
import edu.brown.cs.ivy.mint.MintConstants;



class SesameMonitor implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private MintControl         mint_control;
private boolean             is_done;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameMonitor(String mid)
{
   is_done = false;
   
   mint_control = MintControl.create(mid,MintSyncMode.ONLY_REPLIES);
}



/********************************************************************************/
/*                                                                              */
/*      Server methods                                                          */
/*                                                                              */
/********************************************************************************/

void server()
{
   mint_control.register("<BEDROCK SOURCE='ECLIPSE' TYPE='_VAR_0' />",new EclipseHandler());
   mint_control.register("<BUBBLES DO='_VAR_0' />",new BubblesHandler());
   mint_control.register("<SEEDE DO='_VAR_0' />",new CommandHandler());
         
   synchronized (this) {
      while (!is_done) {
         checkEclipse();
         try {
            wait(30000l);
          }
         catch (InterruptedException e) { }
       }
    }
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
 



/********************************************************************************/
/*                                                                              */
/*      Handle Messages from Eclipse                                            */
/*                                                                              */
/********************************************************************************/


private class EclipseHandler implements MintHandler {
   
   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      Element e = msg.getXml();
      switch (cmd) {
         case "PING" :
            msg.replyTo("PONG");
            break;
       }
    }
   
}       // end of inner class EclipseHandler




/********************************************************************************/
/*                                                                              */
/*      Handle messages from Bubbles                                            */
/*                                                                              */
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
   
}       // end of inner class BubblesHandler



/********************************************************************************/
/*                                                                              */
/*      Command handler                                                         */
/*                                                                              */
/********************************************************************************/

private class CommandHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      Element e = msg.getXml();
    }
   
}       // end of inner class CommandHandler




}       // end of class SesameMonitor




/* end of SesameMonitor.java */

