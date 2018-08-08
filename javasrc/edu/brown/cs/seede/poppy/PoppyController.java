/********************************************************************************/
/*                                                                              */
/*              PoppyController.java                                            */
/*                                                                              */
/*      Java Agent class for poppy                                              */
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



package edu.brown.cs.seede.poppy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class PoppyController implements PoppyConstants
{



/********************************************************************************/
/*                                                                              */
/*      Agent entry points                                                      */
/*                                                                              */
/********************************************************************************/

public static void premain(String args,Instrumentation inst)
{
   the_control = new PoppyController(args,inst);
}


public static void agentmain(String args,Instrumentation inst)
{
   if (the_control == null) the_control = new PoppyController(args,inst);
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static PoppyController  the_control = null;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private PoppyController(String args,Instrumentation inst)
{
   RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
   
   // System.err.println("POPPY: Start");
   // System.err.println("POPPY: CP: " + System.getProperty("java.class.path"));
   // System.err.println("POPPY: BCP: " + rmx.getBootClassPath());
   // System.err.println("POPPY: CP: " + rmx.getClassPath());
   // System.err.println("POPPY: BBPS: " + rmx.isBootClassPathSupported());
   
   try {
      File f1 = new File("/u/spr/poppy.out");
      PrintWriter p1 = new PrintWriter(new FileWriter(f1));
      p1.println("POPPY: Start");
      p1.println("POPPY: CP: " + System.getProperty("java.class.path"));
      if (rmx.isBootClassPathSupported()) {
         p1.println("POPPY: BCP: " + rmx.getBootClassPath());
       }
      p1.println("POPPY: CP: " + rmx.getClassPath());
      p1.close();
    }
   catch (IOException e) { }
}



}       // end of class PoppyController




/* end of PoppyController.java */

