/********************************************************************************/
/*										*/
/*		SesameConstants.java						*/
/*										*/
/*	SEEDE Management Environment constant definitions			*/
/*										*/
/********************************************************************************/
/*	Copyright 2016 Brown University -- Steven P. Reiss		      */
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



package edu.brown.cs.seede.sesame;

import java.util.HashMap;
import java.util.Random;



public interface SesameConstants {


String	  SOURCE_ID = "SEEDE_" + (new Random().nextInt(1000000));



/********************************************************************************/
/*                                                                              */
/*      Value definitions                                                       */
/*                                                                              */
/********************************************************************************/

enum ValueKind {
   UNKNOWN, PRIMITIVE, STRING, CLASS, OBJECT, ARRAY
}



/********************************************************************************/
/*										*/
/*	Command arguments							*/
/*										*/
/********************************************************************************/

class CommandArgs extends HashMap<String,Object> {

   private static final long serialVersionUID = 1;

   public CommandArgs() { }
   public CommandArgs(String key,Object... args) {
      this();
      if (args.length == 0) return;
      put(key,args[0]);
      for (int i = 2; i < args.length; i += 2) {
         put(args[i-1].toString(),args[i]);
       }
    }

   public void put(String key,Object... args) {
      if (args.length == 0) return;
      put(key,args[0]);
      for (int i = 2; i < args.length; i += 2) {
	 put(args[i-1].toString(),args[i]);
       }
    }

}	// end of inr class CommandArgs




}	// end of interface SesameConstants




/* end of SesameConstants.java */
