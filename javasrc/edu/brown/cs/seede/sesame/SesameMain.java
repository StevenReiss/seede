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

import edu.brown.cs.bubbles.board.BoardConstants;

public class SesameMain implements SesameConstants
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
private String                  launch_id;
private SesameFileManager       file_manager;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private SesameMain(String [] args)
{
   message_id = null;
   launch_id = null;
   
   scanArgs(args);
   
   file_manager = new SesameFileManager();
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
         else if (args[i].startsWith("-l") && i+1 < args.length) {
   
            launch_id = args[++i];
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
   System.err.println("Sesame: SesameMain -m <message_id> -l <launch id>");
   System.exit(1);
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

SesameFileManager getFileManager()              { return file_manager; }



/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

private void process()
{
   
}




}       // end of class SesameMain




/* end of SesameMain.java */

