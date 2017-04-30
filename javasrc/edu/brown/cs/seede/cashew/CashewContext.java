/********************************************************************************/
/*										*/
/*		CashewContext.java						*/
/*										*/
/*	Context for looking up symbols/names					*/
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.brown.cs.ivy.jcode.JcodeDataType;
import edu.brown.cs.ivy.jcode.JcodeField;
import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class CashewContext implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<Object,CashewValue> context_map;
protected CashewContext parent_context;
private File   context_file;
private String context_owner;
private List<CashewContext> nested_contexts;
private int context_id;
private long start_time;
private long end_time;

private static AtomicInteger id_counter = new AtomicInteger();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public CashewContext(JcompSymbol js,File f,CashewContext par)
{
   this(getNameWithSignature(js),f,par);
}



private static String getNameWithSignature(JcompSymbol js)
{
   StringBuffer buf = new StringBuffer();
   buf.append(js.getFullName());
   
   buf.append("(");
   JcompType jt = js.getType();
   int i = 0;
   for (JcompType aty : jt.getComponents()) {
      if (i++ > 0) buf.append(",");
      buf.append(aty.getName());
    }
   buf.append(")");
   
   return buf.toString();
}


public CashewContext(JcodeMethod jm,CashewContext par)
{
   this(getNameWithSignature(jm),null,par);
}



private static String getNameWithSignature(JcodeMethod jm)
{
   StringBuffer buf = new StringBuffer();
   buf.append(jm.getFullName());
   buf.append("(");
   for (int i = 0; i < jm.getNumArguments(); ++i) {
      JcodeDataType dt = jm.getArgType(i);
      if (i > 0) buf.append(",");
      buf.append(dt.getName());
    }
   buf.append(")");
   
   return buf.toString();
}



public CashewContext(String js,File f,CashewContext par)
{
   context_owner = js;
   context_file = f;
   context_map = new HashMap<>();
   parent_context = par;
   nested_contexts = new ArrayList<CashewContext>();
   start_time = end_time = 0;
   context_id = id_counter.incrementAndGet();
}



public void reset()
{
   nested_contexts.clear();
   context_map.clear();
   start_time = end_time = 0;
}



/********************************************************************************/
/*										*/
/*	High-level access methods						*/
/*										*/
/********************************************************************************/

public String getName()                         { return context_owner; }

public CashewContext getParentContext()         { return parent_context; }


public CashewValue findReference(JcompSymbol js)
{
   CashewValue cv = null;

   if (js.isFieldSymbol() && js.isStatic()) {
      String nm = js.getFullName();
      return findStaticFieldReference(nm,js.getType().getName());
    }

   cv = findReference((Object) js);
   if (cv != null) return cv;

   return cv;
}


public CashewValue findReference(JcodeField jf)
{
   CashewValue cv = null;
   String cnm = jf.getDeclaringClass().getName();
   cnm = cnm.replace("$",".");
   String nm = cnm + "." + jf.getName();
   if (jf.isStatic()) {
      return findStaticFieldReference(nm,jf.getType().getName());
    }

   cv = findReference((Object) jf);

   return cv;
}



public CashewValue findStaticFieldReference(String name,String type)
{
   CashewValue cv = findReference(name);

   if (cv == null && parent_context != null) {
      cv = parent_context.findStaticFieldReference(name,type);
    }

   return cv;
}



public CashewValue findReference(Integer lv)
{
   CashewValue cv = findReference(((Object) lv));
   if (cv != null) return cv;

   return null;
}








/********************************************************************************/
/*                                                                              */
/*      Find operations for user variables                                      */
/*                                                                              */
/********************************************************************************/

public String findReferencedVariableName(String name)
{
   int idx = name.indexOf("?");
   if (idx > 0) {
      String outname = name.substring(0,idx);
      name = name.substring(idx+1);
      int idx1 = outname.indexOf("#");
      if (idx1 > 0) {
         String ctxname = outname.substring(0,idx1);
         for (CashewContext ictx : nested_contexts) {
            if (ictx.getName().equals(ctxname)) {
               return ictx.findReferencedVariableName(name);
             }
          }
         return null;
       }
      else {
         return outname;
       }
    }
   else {
      return name;
    }
}



public CashewValue findReferencedVariableValue(String name)
{
   int idx = name.indexOf("?");
   if (idx > 0) {
      String outname = name.substring(0,idx);
      name = name.substring(idx+1);
      int idx1 = outname.indexOf("#");
      if (idx1 > 0) {
         String ctxname = outname.substring(0,idx1);
         for (CashewContext ictx : nested_contexts) {
            if (ictx.getName().equals(ctxname)) {
               return ictx.findReferencedVariableValue(name);
             }
          }
         return null;
       }
      else {
         return lookupVariableValue(outname);
       }
    }
   else {
      return lookupVariableValue(name);
    }
}


private CashewValue lookupVariableValue(String name)
{
   int idx = name.indexOf("?");
   String lookup = name;
   String next = null;
   if (idx > 0) {
      lookup = name.substring(0,idx);
      next = name.substring(idx+1);
    }
   int line = 0;
   int bestline = 0;
   CashewValue bestvalue = null;
   int idx1 = lookup.indexOf("@");
   if (idx1 > 0) {
      line = Integer.parseInt(lookup.substring(idx1+1));
      lookup = lookup.substring(0,idx);   
    }
   
   for (Map.Entry<Object,CashewValue> ent : context_map.entrySet()) {
      Object key = ent.getKey();
      if (key.toString().equals(lookup)) {
         if (line == 0) {
            bestvalue = ent.getValue();
            break;
          }
         if (key instanceof JcompSymbol) {
            JcompSymbol js = (JcompSymbol) key;
            ASTNode defn = js.getDefinitionNode();
            CompilationUnit cu = (CompilationUnit) defn.getRoot();
            int lno = cu.getLineNumber(defn.getStartPosition());
            if (bestline == 0 || Math.abs(bestline - line) > Math.abs(lno-line)) {
               bestvalue = ent.getValue();
               bestline = lno;
             }
          }
       }
    }  
   
   if (bestvalue != null && next != null) {
      //TODO: lookup field next in best value
      return null;
    }
   
   return bestvalue;
}





/********************************************************************************/
/*										*/
/*	Context Operators							*/
/*										*/
/********************************************************************************/

public CashewValue findReference(Object var)
{
   // for byte-code based lookup
   CashewValue cv = context_map.get(var);
   if (cv != null) return cv;
   if (parent_context != null) {
      cv = parent_context.findReference(var);
    }

   return cv;
}



public void define(JcompSymbol sym,CashewValue addr)
{
   define((Object) sym,addr);
}




public void define(Object var,CashewValue addr)
{
   context_map.put(var,addr);
}



/********************************************************************************/
/*										*/
/*	Recording methods							*/
/*										*/
/********************************************************************************/

public void setStartTime(CashewClock cc)	{ start_time = cc.getTimeValue(); }


public void setEndTime(CashewClock cc)		{ end_time = cc.getTimeValue(); }



/********************************************************************************/
/*										*/
/*	Evaluation methods							*/
/*										*/
/********************************************************************************/

public CashewValue evaluate(String expr)
{
   return evaluate(expr,null);
}


public CashewValue evaluate(String expr,String tid)
{
   if (parent_context != null) return parent_context.evaluate(expr,tid);
   
   return null;
}


public CashewValue evaluateVoid(String expr)
{
   if (parent_context != null) return parent_context.evaluateVoid(expr);

   return null;
}


public void enableAccess(String type)
{
   if (parent_context != null) {
      parent_context.enableAccess(type);
    }
}


public CashewInputOutputModel getIOModel()
{
   if (parent_context != null) return parent_context.getIOModel();
   return null;
}


public String findNameForValue(CashewValue cv)
{
   if (parent_context != null) return parent_context.findNameForValue(cv);
   return null;
}


public String findNameForValue(CashewValue cv,String thread)
{
   if (parent_context != null) return parent_context.findNameForValue(cv,thread);
   return null;
}


public String getNextInputLine(String file)
{
   if (parent_context != null) return parent_context.getNextInputLine(file);
   return null;
}
      



/********************************************************************************/
/*                                                                              */
/*      Reset methods                                                           */
/*                                                                              */
/********************************************************************************/

public void resetValues(Set<CashewValue> done) 
{
   for (CashewValue cv : context_map.values()) {
      cv.resetValues(done);
    }
   if (nested_contexts != null) {
      for (CashewContext nctx : nested_contexts) {
         nctx.resetValues(done);
       }
    }
}



public void addNestedContext(CashewContext ctx)
{
   nested_contexts.add(ctx);
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public void checkChanged(CashewOutputContext outctx)
{
   for (CashewValue cv : context_map.values()) {
      cv.checkChanged(outctx);
    }
   for (CashewContext ctx : nested_contexts) {
      ctx.checkChanged(outctx);
    }
}



public void outputXml(CashewOutputContext outctx)
{
   IvyXmlWriter xw = outctx.getXmlWriter();
   xw.begin("CONTEXT");
   xw.field("ID",context_id);
   if (context_owner != null) {
      xw.field("METHOD",context_owner);
    }
   if (context_file != null) {
      xw.field("FILE",context_file.getAbsolutePath());
    }
   xw.field("START",start_time);
   xw.field("END",end_time);
   if (parent_context != null) xw.field("PARENT",parent_context.context_id);

   for (Map.Entry<Object,CashewValue> ent : context_map.entrySet()) {
      Object var = ent.getKey();
      xw.begin("VARIABLE");
      xw.field("NAME",var.toString());
      if (var instanceof JcompSymbol) {
	 JcompSymbol js = (JcompSymbol) var;
	 ASTNode defn = js.getDefinitionNode();
	 CompilationUnit cu = (CompilationUnit) defn.getRoot();
	 int lno = cu.getLineNumber(defn.getStartPosition());
	 xw.field("LINE",lno);
       }
      CashewValue cv = ent.getValue();
      cv.outputXml(outctx);
      xw.end("VARIABLE");
    }
   for (CashewContext ctx : nested_contexts) {
      ctx.outputXml(outctx);
    }
   xw.end("CONTEXT");
}








}	// end of class CashewContext




/* end of CashewContext.java */

