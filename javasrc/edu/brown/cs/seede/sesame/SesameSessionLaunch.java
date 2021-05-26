/********************************************************************************/
/*										*/
/*		SesameSessionLaunch.java					*/
/*										*/
/*	Session based on a debugger launch					*/
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewValue;

class SesameSessionLaunch extends SesameSession
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		launch_id;
private Set<String>	thread_ids;
private Set<String>     frame_ids;
private String		method_name;
private SesameFile	source_file;
private int		line_number;
private Map<String,Map<String,SesameValueData>> thread_values;
private Map<String,SesameValueData> unique_values;
private Map<String,String> thread_frame;
private Set<String>	accessible_types;
private SesameSessionCache value_cache;
private boolean 	session_ready;

private static AtomicInteger eval_counter = new AtomicInteger();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameSessionLaunch(SesameMain sm,String sid,Element xml)
{
   super(sm,sid,xml);

   launch_id = IvyXml.getAttrString(xml,"LAUNCHID");
   thread_ids = new HashSet<>();
   thread_frame = new HashMap<>();
   String s = IvyXml.getAttrString(xml,"THREADID");
   for (StringTokenizer tok = new StringTokenizer(s," ,;\t\n"); tok.hasMoreTokens(); ) {
      thread_ids.add(tok.nextToken());
    }
   String s1 = IvyXml.getAttrString(xml,"FRAMEID");
   frame_ids = null;
   if (s1 != null) {
      frame_ids = new HashSet<>();
      for (StringTokenizer tok = new StringTokenizer(s1," ,;\t\n"); tok.hasMoreTokens(); ) {
         frame_ids.add(tok.nextToken());
       }
    }
   
   thread_values = new HashMap<>();
   unique_values = new HashMap<>();
   accessible_types = new HashSet<>();
   value_cache = new SesameSessionCache();

   session_ready = false;
}

protected SesameSessionLaunch(SesameSessionLaunch ssl) 
{
   super(ssl);
   launch_id = ssl.launch_id;
   thread_ids = ssl.thread_ids;
   thread_frame = ssl.thread_frame;
   frame_ids = ssl.frame_ids;
   thread_values = new HashMap<>(ssl.thread_values);
   unique_values = ssl.unique_values;
   accessible_types = ssl.accessible_types;
   value_cache = new SesameSessionCache();      // Want to copy from original cache to new
                                                // at least for initial values.
   
   session_ready = true;
}



@Override void setupSession()
{
   loadInitialValues();
   synchronized (this) {
      session_ready = true;
      notifyAll();
    }
   AcornLog.logD("SESSION " + getSessionId() + " SETUP");
}



@Override protected synchronized void waitForReady()
{
   while (!session_ready) {
      try {
	 wait(10000);
       }
      catch (InterruptedException e) { }
    }
}




@Override void removeSession()
{
   super.removeSession();
   
   thread_values = null;
   unique_values = null;
   thread_frame = null;
   value_cache = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getFrameId(String thread)	{ return thread_frame.get(thread); }

String getAnyThread()
{
   waitForReady();

   for (String s : thread_ids) {
      return s;
    }
   return null;
}


@Override public List<CashewValue> getCallArgs(SesameLocation loc)
{
   waitForReady();

   MethodDeclaration md = getCallMethod(loc);
   if (md == null) return null;
   
   List<CashewValue> args = new ArrayList<>();
   JcompSymbol msym = JcompAst.getDefinition(md.getName());
   if (msym == null) {
      getProject().getTyper();
      msym = JcompAst.getDefinition(md.getName());
      if (msym == null) {
         AcornLog.logE("Can't find method symbol for args " + md + " " + loc);
         return null;
       }
    }

   Map<String,SesameValueData> valmap = thread_values.get(loc.getThread());
   if (!msym.isStatic()) {
      SesameValueData svd = valmap.get("this");
      svd = getUniqueValue(svd);
      CashewValue cv = svd.getCashewValue();
      args.add(cv);
    }
   for (Object o : md.parameters()) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
      JcompSymbol psym = JcompAst.getDefinition(svd.getName());
      SesameValueData val = value_cache.lookup(null,psym.getName());
      if (val == null) val = valmap.get(psym.getName());
      val = getUniqueValue(val);
      if (val != null) {
	 CashewValue argval = val.getCashewValue();
         JcompTyper typer = getProject().getTyper();
	 JcompType jtyp = argval.getDataType(null,typer);
	 // need to check if 'this' is  compatible with COMPONENT
	 JcompType g2dtype = typer.findSystemType("java.awt.Graphics2D");
	 if (jtyp.isCompatibleWith(g2dtype) && !argval.isNull(null)) {
	    if (!jtyp.getName().contains("PoppyGraphics")) {
	       String gname = "MAIN_" + loc.getThreadName();
	       getProject().getJcodeFactory().findClass("edu.brown.cs.seede.poppy.PoppyGraphics");
	       String expr = "edu.brown.cs.seede.poppy.PoppyGraphics.computeGraphics1(";
	       expr += "this,";
	       expr += psym.getName() +  ",\"" + gname + "\")";
	       SesameValueData nval = evaluateData(expr,loc.getThread(),true);
	       if (nval != null) {
		  nval = getUniqueValue(nval);
		  argval = nval.getCashewValue();
		}
	     }
	  }

	 args.add(argval);
       }
    }

   return args;
}



@Override void setInitialValue(String what,Element val) throws SesameException
{
   JcompTyper typer = getProject().getTyper();
   try {
      CashewValue cv = CashewValue.createValue(typer,val);
      value_cache.setInitialValue(what,cv);
    }
   catch (CashewException e) {
      throw new SesameException("Illegal value",e);
    }
}



@Override void setInitialValue(String what,String thread,String expr)
	throws SesameException
{
   CashewValue cv = evaluate(expr,thread,true);
   if (cv == null) throw new SesameException("Evaluation failed");
   if (what != null) value_cache.setInitialValue(what,cv);
}



/********************************************************************************/
/*										*/
/*	Evaluation methods							*/
/*										*/
/********************************************************************************/

@Override void noteContinue(String launch,String thread)
{
   if (!launch.equals(launch_id)) return;
   if (thread != null) {
      if (!thread_ids.contains(thread)) return;
    }

   value_cache.clearCache();
}



@Override CashewValue lookupValue(String name,String type)
{
   CashewValue cv = super.lookupValue(name,type);
   if (cv != null) return null;

   cv = evaluate(name,null,true);
   if (cv != null) return cv;

   return cv;
}


@Override SesameValueData evaluateData(String expr,String thread0,boolean allframes)
{
   String eid = "E_" + eval_counter.incrementAndGet();
   // expr = "edu.brown.cs.seede.poppy.PoppyValue.register(" + expr + ")";

   SesameValueData svd0 = value_cache.lookup(thread0,expr);
   if (svd0 != null) return svd0;

   String thread = thread0;
   if (thread0 == null) thread0 = getAnyThread();
   String frame = thread_frame.get(thread);
   CommandArgs args = new CommandArgs("THREAD",thread,
	 "FRAME",frame,"BREAK",false,"EXPR",expr,"IMPLICIT",true,
	 "LEVEL",3,"ARRAY",-1,"REPLYID",eid,"ALLFRAMES",allframes);
   args.put("SAVEID",eid);
   Element xml = getControl().getXmlReply("EVALUATE",getProject(),args,null,0);
   if (IvyXml.isElement(xml,"RESULT")) {
      Element root = getControl().waitForEvaluation(eid);
      Element v = IvyXml.getChild(root,"EVAL");
      Element v1 = IvyXml.getChild(v,"VALUE");
      String assoc = expr;
      if (args.get("SAVEID") != null) {
	 assoc = "*" + args.get("SAVEID").toString();
       }
      SesameValueData svd = new SesameValueData(this,thread,v1,assoc);
      svd = getUniqueValue(svd);
      value_cache.cacheValue(thread0,expr,svd);
      return svd;
    }
   return null;
}



@Override void evaluateVoid(String expr,boolean allframes) throws CashewException
{
   String eid = "E_" + eval_counter.incrementAndGet();
   String thread = getAnyThread();
   String frame = thread_frame.get(thread);
   CommandArgs args = new CommandArgs("THREAD",getAnyThread(),
	 "FRAME",frame,"BREAK",false,"EXPR",expr,"IMPLICIT",true,
	 "LEVEL",4,"REPLYID",eid,"ALLFRAMES",allframes);
   Element xml = getControl().getXmlReply("EVALUATE",getProject(),args,null,0);
   if (IvyXml.isElement(xml,"RESULT")) {
      Element rslt = getControl().waitForEvaluation(eid);
      Element v = IvyXml.getChild(rslt,"EVAL");
      String sts = IvyXml.getAttrString(v,"STATUS");
      if (sts.equals("EXCEPTION")) {
	 String exc = IvyXml.getTextElement(v,"EXCEPTION");
	 if (exc != null && exc.contains("thread not suspended")) {
	    throw new CashewException("Process continued");
	  }
       }
      return;
    }
}


@Override void enableAccess(String type)
{
   if (accessible_types.contains(type)) return;

   if (type.startsWith("jdk.internal.ref.")) {
      accessible_types.add(type);
      return;
    }

   String type1 = type.replace('$','.');

   String expr = "java.lang.reflect.AccessibleObject.setAccessible(" + type1 + ".class";
   expr += ".getDeclaredFields(),true)";
   try {
      evaluateVoid(expr,false);
    }
   catch (CashewException e) { }

   accessible_types.add(type);
}



@Override String getValueName(CashewValue cv,String thread)
{
   for (Map.Entry<String,Map<String,SesameValueData>> emaps : thread_values.entrySet()) {
      String tid = emaps.getKey();
      Map<String,SesameValueData> maps = emaps.getValue();
      if (thread != null && !thread.equals(tid)) continue;
      for (Map.Entry<String,SesameValueData> ent : maps.entrySet()) {
	 String key = ent.getKey();
	 SesameValueData svd = ent.getValue();
	 String find = svd.findValue(cv,1);
	 if (find != null) {
	    return key + find;
	  }
       }
    }

   return null;
}


@Override void resetCache()
{
   JcompTyper typer = getProject().getTyper();
   value_cache.updateCache(typer);
}




/********************************************************************************/
/*										*/
/*	Launch access methods							*/
/*										*/
/********************************************************************************/

private void loadInitialValues()
{
   int ct = 1;
   if (frame_ids != null) ct = -1;
   CommandArgs cargs = new CommandArgs("LAUNCH",launch_id,"THREAD",null,"COUNT",ct,
					  "ARRAY",-1);

   Element stack = sesame_control.getXmlReply("GETSTACKFRAMES",getProject(),cargs,
	 null,0);
   stack = IvyXml.getChild(stack,"STACKFRAMES");

   for (Element telt : IvyXml.children(stack,"THREAD")) {
      String teid = IvyXml.getAttrString(telt,"ID");
      if (!thread_ids.contains(teid)) continue;
      String thnm = IvyXml.getAttrString(telt,"NAME");
      Element frm = getFrameForThread(telt);
      String feid = IvyXml.getAttrString(frm,"ID");
      thread_frame.put(teid,feid);
      method_name = IvyXml.getAttrString(frm,"METHOD");
      String fnm = IvyXml.getAttrString(frm,"FILE");
      File sf = new File(fnm);
      if (!sf.exists()) continue;
      source_file = sesame_control.getFileManager().openFile(sf);
      line_number = IvyXml.getAttrInt(frm,"LINENO");
      Map<String,SesameValueData> valmap = thread_values.get(teid);
      if (valmap == null) {
	 valmap = new HashMap<String,SesameValueData>();
	 thread_values.put(teid,valmap);
       }
      for (Element var : IvyXml.children(frm,"VALUE")) {
	 String nm = IvyXml.getAttrString(var,"NAME");
	 SesameValueData svd = new SesameValueData(this,teid,var,null);
	 svd = getUniqueValue(svd);
	 valmap.put(nm,svd);
       }
      SesameLocation loc = new SesameLocation(source_file,method_name,line_number,teid,thnm);
      getProject().addFile(source_file);
      addLocation(loc);
    }
}



private Element getFrameForThread(Element telt)
{
   if (frame_ids == null) return IvyXml.getChild(telt,"STACKFRAME");
   
   Element dflt = null;
   for (Element felt : IvyXml.children(telt,"STACKFRAME")) {
      if (dflt == null) dflt = felt;
      String fid = IvyXml.getAttrString(felt,"ID");
      if (frame_ids.contains(fid)) return felt;
    }
   
   return dflt;
}



SesameValueData getUniqueValue(SesameValueData svd)
{
   if (svd == null) return null;
   switch (svd.getKind()) {
      case OBJECT :
      case ARRAY :
	 String dnm = svd.getValue();
	 if (dnm != null && dnm.length() > 0) {
	    SesameValueData nsvd = unique_values.get(dnm);
	    if (nsvd != null) svd = nsvd;
	    else unique_values.put(dnm,svd);
	  }
	 break;
      default :
	 break;
    }
   return svd;
}


}	// end of class SesameSessionLaunch




/* end of SesameSessionLaunch.java */

