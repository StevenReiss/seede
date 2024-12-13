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
import java.util.HashSet;
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
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;


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
private boolean is_output;
 
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
   if (!jt.isMethodType()) jt = jt.getFunctionalType();
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
   start_time = 0;
   end_time = 0;
   context_id = id_counter.incrementAndGet();
   is_output = f != null;
}



public void reset()
{
   nested_contexts.clear();
   context_map.clear();
   start_time = 0;
   end_time = 0;
}



/********************************************************************************/
/*										*/
/*	High-level access methods						*/
/*										*/
/********************************************************************************/

public String getName()                         { return context_owner; }


public int getId()                              { return context_id; }

public CashewContext getParentContext()         { return parent_context; }



public List<CashewContext> getChildContexts()   { return nested_contexts; }

public long getStartTime()                      { return start_time; }

public long getEndTime()                        { return end_time; }

public Object getSessionKey()
{
   if (parent_context == null) return null;
   return parent_context.getSessionKey();
}


public CashewValue findReference(JcompTyper typer,JcompSymbol js)
{
   CashewValue cv = null;

   if (js.isFieldSymbol() && js.isStatic()) {
      String nm = js.getFullName();
      return findStaticFieldReference(typer,nm,js.getType().getName());
    }
   else if (js.isEnumSymbol()) {
      String tnm = js.getType().getName();
      String nm = tnm + "." + js.getName();
      return findStaticFieldReference(typer,nm,tnm);
    }

   cv = findActualReference((Object) js);
   if (cv != null) return cv;

   return cv;
}


public CashewValue findReference(JcompTyper typer,JcodeField jf)
{
   CashewValue cv = null;
   String cnm = jf.getDeclaringClass().getName();
   cnm = cnm.replace("$",".");
   String nm = cnm + "." + jf.getName();
   if (jf.isStatic()) {
      return findStaticFieldReference(typer,nm,jf.getType().getName());
    }

   cv = findActualReference((Object) jf);

   return cv;
}



public CashewValue findStaticFieldReference(JcompTyper typer,String name,String type)
{
   CashewValue cv = findActualReference(name);

   if (cv == null && parent_context != null) {
      cv = parent_context.findStaticFieldReference(typer,name,type);
    }

   return cv;
}



public CashewValue findReference(Integer lv)
{
   CashewValue cv = findActualReference(((Object) lv));
   if (cv != null) return cv;

   return null;
}


public CashewValue findReference(String s)
{
   return findActualReference(s);
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



public CashewValue findReferencedVariableValue(CashewValueSession sess,
      JcompTyper typer,String name,long when)
        throws CashewException
{
   int idx = name.indexOf("?");
   if (idx > 0) {
      String outname = name.substring(0,idx);
      name = name.substring(idx+1);
      int idx1 = outname.indexOf("#");
      if (idx1 > 0) {
         String ctxname = outname.substring(0,idx1);
         if (getName().equals(ctxname)) {
            return lookupVariableValue(sess,typer,name,when);
          }
         for (CashewContext ictx : nested_contexts) {
            if (ictx.getName().equals(ctxname)) {
               return ictx.findReferencedVariableValue(sess,typer,name,when);
             }
          }
         return null;
       }
      else {
         return lookupVariableValue(sess,typer,outname,when);
       }
    }
   else {
      return lookupVariableValue(sess,typer,name,when);
    }
}


private CashewValue lookupVariableValue(CashewValueSession sess,
      JcompTyper typer,String name,long when) throws CashewException
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
      lookup = lookup.substring(0,idx1);   
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
      bestvalue = bestvalue.lookupVariableName(sess,this,typer,next,when);
    }
   
   return bestvalue;
}





/********************************************************************************/
/*										*/
/*	Context Operators							*/
/*										*/
/********************************************************************************/

public CashewValue findActualReference(Object var)
{
   // for byte-code based lookup
   CashewValue cv = context_map.get(var);
   if (cv != null) return cv;
   if (parent_context != null) {
      cv = parent_context.findActualReference(var);
    }

   return cv;
}



public void define(JcompSymbol sym,CashewValue addr)
{
   define((Object) sym,addr);
}




public void define(Object var,CashewValue addr)
{
   if (addr == null) {
      AcornLog.logE("Attempt to put null address in context for " + var);
      return;
    }
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


public CashewValue evaluateVoid(String expr) throws CashewException
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


public CashewSynchronizationModel getSynchronizationModel()
{
   if (parent_context != null) return parent_context.getSynchronizationModel();
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

public void resetValues(CashewValueSession sess,Set<CashewValue> done) 
{
   for (CashewValue cv : context_map.values()) {
      if (cv != null) cv.resetValues(sess,done);
    }
   if (nested_contexts != null) {
      for (CashewContext nctx : nested_contexts) {
         nctx.resetValues(sess,done);
       }
    }
}



public void addNestedContext(CashewContext ctx)
{
   nested_contexts.add(ctx);
}



public boolean isOutput()                       { return is_output; }



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public void checkChanged(CashewOutputContext outctx)
{
   for (CashewValue cv : context_map.values()) {
      if (cv != null) cv.checkChanged(outctx);
    }
   
   for (CashewContext ctx : nested_contexts) {
      ctx.checkChanged(outctx);
    }
}



public void checkToString(CashewValueSession sess,CashewOutputContext outctx)
{
   if (!isOutput()) return;
   
   for (CashewValue cv : context_map.values()) {
      if (cv != null) cv.checkToString(sess,outctx);
    }

   Set<CashewContext> ctxs = new HashSet<>(nested_contexts);
   // internal executions can add new contexts
   for (CashewContext ctx : ctxs) {
      ctx.checkToString(sess,outctx);
    }
   
   for (CashewContext ctx : nested_contexts) {
      if (ctxs.contains(ctx)) continue;
      ctx.is_output = false;
    }
}


public void checkToArray(CashewValueSession sess,CashewOutputContext outctx)
{
   AcornLog.logD("CASHEW","CONTEXT TOARRAY " + getId() + " " + getName() + " " + isOutput());
   
   if (!isOutput()) return;
   
   for (Map.Entry<Object,CashewValue> ent : context_map.entrySet()) {
      String key = ent.getKey().toString();
      if (key.startsWith("*")) continue;
      CashewValue cv = ent.getValue();
      if (cv != null) {
         AcornLog.logD("CONTEXT VALUE " + key + " "  + cv.isEmpty(sess) +
               " " + cv);
         cv.checkToArray(sess,outctx);
       }
    }
   
   Set<CashewContext> ctxs = new HashSet<>(nested_contexts);
   // internal executions can add new contexts
   for (CashewContext ctx : ctxs) {
      ctx.checkToArray(sess,outctx);
    }
   
   for (CashewContext ctx : nested_contexts) {
      if (ctxs.contains(ctx)) continue;
      ctx.is_output = false;    // don't output toArray calls
    }
}



public void outputXml(CashewOutputContext outctx)
{
   outctx.setContext(this);
   if (isOutput() || outctx.getShowAll()) {
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
      
      outputVariables(outctx);
      
      for (CashewContext ctx : nested_contexts) {
         ctx.outputXml(outctx);
       }
      xw.end("CONTEXT");
    }
   else {
      for (CashewContext ctx : nested_contexts) {
         ctx.outputXml(outctx);
       } 
    }
   outctx.setContext(null);
}



private void outputVariables(CashewOutputContext outctx)
{
   IvyXmlWriter xw = outctx.getXmlWriter();
   
   for (Map.Entry<Object,CashewValue> ent : context_map.entrySet()) {
      Object var = ent.getKey();
      String name = var.toString();
      xw.begin("VARIABLE");
      xw.field("NAME",name);
      if (var instanceof JcompSymbol) {
         JcompSymbol js = (JcompSymbol) var;
         ASTNode defn = js.getDefinitionNode();
         CompilationUnit cu = (CompilationUnit) defn.getRoot();
         int lno = cu.getLineNumber(defn.getStartPosition());
         xw.field("LINE",lno);
       }
      CashewValue cv = ent.getValue();
      if (cv != null) cv.outputXml(outctx,name);
      xw.end("VARIABLE");
    }
}



public void outputStatics(CashewOutputContext outctx)
{
   if (parent_context != null) {
      parent_context.outputStatics(outctx);
    }
   else {
      outputVariables(outctx);
    }
}








}	// end of class CashewContext




/* end of CashewContext.java */

