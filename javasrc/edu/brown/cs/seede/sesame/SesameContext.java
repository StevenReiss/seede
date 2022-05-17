/********************************************************************************/
/*										*/
/*		SesameContext.java						*/
/*										*/
/*	description of class							*/
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

import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewInputOutputModel;
import edu.brown.cs.seede.cashew.CashewSynchronizationModel;
import edu.brown.cs.seede.cashew.CashewValue;

public class SesameContext extends CashewContext implements SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SesameSession	for_session;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameContext(SesameSession ss)
{
   super("GLOBAL_CONTEXT",null,null);

   for_session = ss;
}




/********************************************************************************/
/*										*/
/*	Overridden methods							*/
/*										*/
/********************************************************************************/

@Override public CashewValue findStaticFieldReference(JcompTyper typer,String name,String type)
{
   CashewValue cv = super.findStaticFieldReference(typer,name,type);
   if (cv != null) return cv;

   if (name.endsWith(".$assertionsDisabled")) {
      cv = CashewValue.booleanValue(typer,false);
      return cv;
    }
   if (name.endsWith(".$assertionsEnabled")) {
      cv = CashewValue.booleanValue(typer,true);
      return cv;
    }
   if (name.endsWith("ENUM$VALUES")) {
      String expr = name.replace("ENUM$VALUES","values()");
      cv = for_session.lookupValue(expr,type);
      if (cv != null) return cv;
    }
   
   int idx = name.lastIndexOf(".");
   if (idx >= 0) {
//    String cnm = name.substring(0,idx);
//    enableAccess(cnm);
    }

   cv = for_session.lookupValue(name,type);
   if (cv == null) {
      cv = getKnownStaticField(typer,name,type);
    }
   if (cv == null) {
      String suffix = "";
      if (type == null) type = "java.lang.Object";
      switch (type) {
	 case "boolean" :
	    suffix = "Boolean";
	    break;
	 case "int" :
	    suffix = "Int";
	    break;
	 case "long" :
	    suffix = "Long";
	    break;
	 case "short" :
	    suffix = "Short";
	    break;
	 case "double" :
	    suffix = "Double";
	    break;
	 case "float" :
	    suffix = "Float";
	    break;
       }
      String expr = "edu.brown.cs.seede.poppy.PoppyValue.getStaticFieldValue";
      expr += suffix + "(\"" + name + "\")";
      cv = for_session.lookupValue(expr,type);
      // System.err.println("HANDLE PROBLEM FIELDS " + cv);
      AcornLog.logI("Handle problem fields: " + suffix + "('" + name + "') => " + cv);
    }
   if (cv != null) {
      cv = CashewValue.createReference(cv,true);
      define(name,cv);
    }

   return cv;
}



private CashewValue getKnownStaticField(JcompTyper typer,String name,String type) 
{
   CashewValue rslt = null;
   
   switch (name) {
      case "jdk.internal.logger.SurrogateLogger.JUL_DEFAULT_LEVEL" :
         rslt = findStaticFieldReference(typer,"sun.util.logging.PlatformLogger.Level.INFO",type);
         break;
    }
   
   return rslt;
}


@Override public CashewValue evaluate(String expr)
{
   return for_session.evaluate(expr,null,true);
}


@Override public CashewValue evaluate(String expr,String tid)
{
   return for_session.evaluate(expr,tid,true);
}


@Override public CashewValue evaluateVoid(String expr) throws CashewException 
{
   for_session.evaluateVoid(expr,true);

   return null;
}


@Override public CashewSynchronizationModel getSynchronizationModel()
{
   return for_session.getSynchronizationModel();
}



@Override public void enableAccess(String type)
{
   for_session.enableAccess(type);
}


@Override public CashewInputOutputModel getIOModel()
{
   return for_session.getIOModel();
}

@Override public String findNameForValue(CashewValue cv,String thread)
{
   return for_session.getValueName(cv,thread);
}


@Override public String getNextInputLine(String file)
{
   String inp = for_session.requestInput(file);
   return inp;
}

@Override public Object getSessionKey()
{
   if (for_session == null) return null;
   return for_session.getSessionKey();
}



}	// end of class SesameContext




/* end of SesameContext.java */

