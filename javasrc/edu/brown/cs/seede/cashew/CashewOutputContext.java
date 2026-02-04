/********************************************************************************/
/*										*/
/*		CashewOutputContext.java					*/
/*										*/
/*	Information for providing concise outputs				*/
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



package edu.brown.cs.seede.cashew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;



public class CashewOutputContext implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IvyXmlWriter xml_writer;
private Map<CashewValue,Integer> values_output;
private List<ExpandName> expand_names;
private CashewContext current_context;
private CashewRunner for_runner;
private JcompTyper type_context;
private boolean show_all;

private static AtomicInteger id_counter = new AtomicInteger();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public CashewOutputContext(CashewRunner cr,IvyXmlWriter xw,Set<String> exp,boolean showall)
{
   values_output = new HashMap<CashewValue,Integer>();
   xml_writer = xw;
   expand_names = null;
   if (exp != null) {
      expand_names = new ArrayList<ExpandName>();
      for (String s : exp) expand_names.add(new ExpandName(s));
    }
   current_context = null;
   for_runner = cr;
   type_context = cr.getTyper();
   show_all = showall;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public IvyXmlWriter getXmlWriter()
{
   return xml_writer;
}


public String getContents()
{
   return xml_writer.toString();
}

public JcompTyper getTyper()			{ return type_context; }
public CashewClock getClock()			{ return for_runner.getClock(); }
public CashewValueSession getSession()          { return for_runner.getSession(); }

void setContext(CashewContext ctx)
{
   current_context = ctx;
}
CashewContext getContext()                      { return current_context; }

public boolean getShowAll()                     { return show_all; }



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

public int noteValue(CashewValue cv)
{
   // returns -id if new, id if old

   Integer v = values_output.get(cv);
   if (v != null && v != 0) return v;
   v = id_counter.incrementAndGet();
   values_output.put(cv,v);
   return -v;
}


public boolean noteChecked(CashewValue cv)
{
   Integer v = values_output.get(cv);
   if (v != null) return true;
   values_output.put(cv,0);
   return false;
}


public void resetValues()
{
   values_output.clear();
}



/********************************************************************************/
/*										*/
/*	Handle to String							*/
/*										*/
/********************************************************************************/

public String getToString(CashewValueSession sess,CashewValue cv)
{
   JcompType typ = cv.getDataType(getTyper());
   if (typ.isPrimitiveType()) return null;
   JcompType atyp = type_context.createMethodType(null,new ArrayList<>(),false,null);
   JcompSymbol tostr = typ.lookupMethod(type_context,"toString",atyp);
   if (tostr == null) return null;
   switch (tostr.getClassType().getName()) {
      case "java.lang.Object" :
      case "java.lang.String" :
      case "java.awt.Graphics" :
      case "java.awt.Component" :
	 return null;
    }

   for_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
   String rtn = "edu.brown.cs.seede.poppy.PoppyValue.getToString";
   CashewValue rslt = for_runner.executeCall(rtn,cv);
   if (rslt == null || rslt.isNull(sess,for_runner.getClock())) return null;

   try {
      CashewClock clk = for_runner.getClock();
      return rslt.getString(sess,type_context,clk);
    }
   catch (CashewException e) {
      AcornLog.logE("Problem getting toString",e);
    }

   return null;
}


/********************************************************************************/
/*                                                                              */
/*      Handle to array                                                         */
/*                                                                              */
/********************************************************************************/

public CashewValue getToArray(CashewValueSession sess,CashewValue cv)
{
   JcompType typ = cv.getDataType(getTyper());
   if (typ.isPrimitiveType()) return null;
   if (typ.isArrayType()) return null;
// JcompType atyp = type_context.createMethodType(null,new ArrayList<>(),false,null);
// JcompSymbol toarr = typ.lookupMethod(type_context,"toArray",atyp);
// AcornLog.logD("CASHEW","TOARRAY " + typ + " " + toarr);
// if (toarr == null) return null;
   
   for_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
   String rtn = "edu.brown.cs.seede.poppy.PoppyValue.getToArray";
   CashewValue rslt = for_runner.executeCall(rtn,cv);
   AcornLog.logD("CASHEW","TOARRAY RESULT " + rslt);
   if (rslt == null || rslt.isNull(sess,for_runner.getClock())) return null;
   
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Handle name expansion							*/
/*										*/
/********************************************************************************/

public boolean expand(String name)
{
   if (expand_names == null) return false;
   if (name == null) return false;
   if (current_context == null) return false;
   for (ExpandName en : expand_names) {
      if (en.match(current_context,name)) return true;
    }
   return false;
}


public boolean expandChild(String name)
{
   if (expand_names == null) return false;
   if (name == null) return false;
   if (current_context == null) return false;
   name = name + "?";
   for (ExpandName en : expand_names) {
      if (en.matchChild(current_context,name)) return true;
    }
   return false;
}



private static class ExpandName {

   private String context_name;
   private String variable_name;

   ExpandName(String nm) {
      int idx = nm.indexOf("?");
      if (idx >= 0) {
	 context_name = nm.substring(0,idx);
	 variable_name = nm.substring(idx+1);
	 int idx1 = context_name.indexOf("#");
	 if (idx1 > 0) context_name = context_name.substring(0,idx1);
       }
      else {
	 context_name = null;
	 variable_name = nm;
       }

      int idx1 = variable_name.indexOf("@");
      if (idx1 > 0) {
	 int idx2 = variable_name.indexOf("?",idx1);
	 if (idx2 < 0) variable_name = variable_name.substring(0,idx1);
	 else {
	    variable_name = variable_name.substring(0,idx1) + variable_name.substring(idx2);
	  }
       }

    }

   boolean match(CashewContext ctx,String name) {
      String ctxnm = ctx.getName();
      if (context_name != null && !context_name.equals(ctxnm)) return false;
      if (variable_name == null) return true;
      if (name.startsWith(variable_name)) {
	 int ln = variable_name.length();
	 if (ln == name.length()) return true;
	 if (ln == name.lastIndexOf("?")) return true;
       }
      return false;
    }

   boolean matchChild(CashewContext ctx,String name) {
      String ctxnm = ctx.getName();
      if (context_name != null && !context_name.equals(ctxnm)) return false;
      if (variable_name != null && !variable_name.startsWith(name)) return false;
      return true;
   }

}


}	// end of class CashewOutputContext




/* end of CashewOutputContext.java */

