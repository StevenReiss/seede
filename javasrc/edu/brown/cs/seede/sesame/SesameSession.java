/********************************************************************************/
/*										*/
/*		SesameSession.java						*/
/*										*/
/*	Abstarct representation of a evaluation session 			*/
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewInputOutputModel;
import edu.brown.cs.seede.cashew.CashewSynchronizationModel;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewConstants.CashewValueSession;
import edu.brown.cs.seede.cumin.CuminRunner;

public abstract class SesameSession implements SesameConstants, CashewValueSession
{


/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

static SesameSession createSession(SesameMain sm,String sid,Element xml) throws SesameException
{
   SesameSession ss = null;

   Element celt = IvyXml.getChild(xml,"SESSION");
   if (celt != null) xml = celt;

   String typ = IvyXml.getAttrString(xml,"TYPE");

   if (typ.equals("LAUNCH")) {
      ss = new SesameSessionLaunch(sm,sid,xml);
    }
   else if (typ.equals("TEST")) {
      ss = new SesameSessionTest(sm,sid,xml);
    }

   return ss;
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected SesameMain	sesame_control;
private String		session_id;
private SesameProject	for_project;
private Map<String,SesameLocation> location_map;
private Map<CuminRunner,SesameLocation> runner_location;
private Set<SesameExecRunner> exec_runners;
private CashewInputOutputModel	cashew_iomodel;
private CashewSynchronizationModel cashew_syncmodel;
private Set<String>	expand_names;
private SesameSession	parent_session;
private boolean 	compute_tostring;
private boolean 	compute_toarray;
private boolean 	show_all;
private Set<CashewValue> poppy_graphics;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SesameSession(SesameMain sm,String sid,Element xml)
{
   sesame_control = sm;

   String proj = IvyXml.getAttrString(xml,"PROJECT");
   for_project = sm.getProject(proj);
   initialize(sid,xml);

   for (Element locxml : IvyXml.children(xml,"LOCATION")) {
      SesameLocation sloc = new SesameLocation(sm,locxml);
      addLocation(sloc);
    }
}


SesameSession(SesameSession parent)
{
   sesame_control = parent.sesame_control;
   for_project = parent.for_project;

   initialize(null,null);

   parent_session = parent;

   location_map.putAll(parent.location_map);
   if (parent.expand_names != null) {
      expand_names = new HashSet<>(parent.expand_names);
    }
   compute_tostring = parent.compute_tostring;
   compute_toarray = parent.compute_toarray;
   show_all = parent.show_all;
}


private void initialize(String sid,Element xml)
{
   if (sid == null || sid.equals("*")) {
      Random r = new Random();
      sid = "SESAME_" + r.nextInt(10000000);
    }
   session_id = sid;

   location_map = new HashMap<>();

   exec_runners = new HashSet<>();
   runner_location = new WeakHashMap<>();
   cashew_iomodel = new CashewInputOutputModel();
   cashew_syncmodel = new CashewSynchronizationModel();
   expand_names = null;
   compute_tostring = sesame_control.getComputeToString();
   compute_toarray = sesame_control.getComputeToArray();
   show_all = IvyXml.getAttrBool(xml,"SHOWALL");
   compute_tostring = IvyXml.getAttrBool(xml,"TOSTRING",compute_tostring);
   compute_toarray = IvyXml.getAttrBool(xml,"TOARRAY",compute_toarray);
   parent_session = null;
   poppy_graphics = null;
}



/********************************************************************************/
/*										*/
/*	Cleanup methods 							*/
/*										*/
/********************************************************************************/

void removeSession()
{
   sesame_control.removeProject(getProject());
   for_project = null;
   location_map = null;
   runner_location = null;
   exec_runners = null;
   cashew_iomodel = null;
   cashew_syncmodel = null;
   expand_names = null;
   parent_session = null;
   poppy_graphics = null;
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

void setupSession()				{ }
protected void waitForReady()			{ }
SesameSubsession getSubsession()		{ return null; }




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getSessionId()			{ return session_id; }

@Override public CashewValueSession getParent() { return parent_session; }

public SesameProject getProject()
{
   return for_project;
}

public MethodDeclaration getCallMethod(SesameLocation loc)
{
   if (!loc.isActive() || loc.getStartPosition() == null) {
      AcornLog.logE("Bad call method " + loc.getMethodName() + " " +
	    loc.isActive() + " " + loc.getStartPosition());
      return null;
    }

   SesameFile sf = loc.getFile();
   ASTNode root = sf.getResolvedAst(getProject());
   ASTNode mnode = JcompAst.findNodeAtOffset(root,loc.getStartPosition().getOffset());
   while (mnode != null && !(mnode instanceof MethodDeclaration)) {
      mnode = mnode.getParent();
    }
   return (MethodDeclaration) mnode;
}



public List<SesameLocation> getActiveLocations()
{
   List<SesameLocation> rslt = new ArrayList<SesameLocation>();

   for (SesameLocation loc : location_map.values()) {
      if (loc.isActive()) rslt.add(loc);
    }

   return rslt;
}

public List<CashewValue> getCallArgs(SesameLocation loc)	{ return null; }

public SesameLocation getLocation(CuminRunner cr)
{
   return runner_location.get(cr);
}

SesameMain getControl() 			{ return sesame_control; }

protected final void addLocation(SesameLocation sl)
{
   location_map.put(sl.getId(),sl);
   getProject().addFile(sl.getFile());
}

void noteFileChanged(SesameFile sf)
{
   SesameProject sp = getProject();
   if (sp.noteFileChanged(sf,false)) {
      restartRunners();
    }
}



void noteContinue(String launch,String thread)			{ }



void setInitialValue(String what,Element value) throws SesameException	   { }


void setInitialValue(String what,String thread,String expr) throws SesameException
{
   throw new SesameException("Initial expression value not implemented");
}


String getValueName(CashewValue cv,String thread)
{
   return null;
}


boolean getComputeToString()
{
   return compute_tostring;
}


boolean getComputeToArray()
{
   return compute_toarray;
}


void setComputeToString(boolean fg)
{
   compute_tostring = fg;
}


void setComputeToArray(boolean fg)
{
   compute_toarray = fg;
}

boolean getShowAll()
{
   return show_all;
}



void setShowAll(boolean fg)
{
   show_all = fg;
}


Object getSessionKey()
{
   return this;
}


@Override public void addPoppyGraphics(CashewValue cv)
{
   if (parent_session != null) {
      parent_session.addPoppyGraphics(cv);
    }
   else {
      if (poppy_graphics == null) poppy_graphics = new HashSet<>();
      poppy_graphics.add(cv);
    }
}


Collection<CashewValue> getPoppyGraphics()
{
   if (poppy_graphics == null) return Collections.emptyList();

   return poppy_graphics;
}



/********************************************************************************/
/*										*/
/*	Request methods 							*/
/*										*/
/********************************************************************************/

void requestInitialValue(String what)
{
   CommandArgs args = new CommandArgs("WHAT",what,"SID",session_id);

   SesameMonitor mon = sesame_control.getMonitor();
   MintDefaultReply rply = new MintDefaultReply();
   mon.sendCommand("INITIALVALUE",args,null,rply);
   Element rslt = rply.waitForXml();
   if (rslt == null) return;
   Element val = IvyXml.getChild(rslt,"VALUE");
   try {
      setInitialValue(what,val);
    }
   catch (SesameException _ex) { }
}



String requestInput(String file)
{
   CommandArgs args = new CommandArgs("FILE",file,"SID",session_id);
   SesameMonitor mon = sesame_control.getMonitor();
   MintDefaultReply rply = new MintDefaultReply();
   mon.sendCommand("INPUT",args,null,rply);
   String rslt = rply.waitForString();
   return rslt;
}

synchronized void addExpandName(String name)
{
   if (expand_names == null) expand_names = new HashSet<>();
   expand_names.add(name);
}

Set<String> getExpandNames()		{ return expand_names; }



/********************************************************************************/
/*										*/
/*	Methods to help in variable history					*/
/*										*/
/********************************************************************************/

CashewContext findContext(int idn)
{
   if (idn <= 0) return null;

   for (CuminRunner cr : runner_location.keySet()) {
      CashewContext ctx = cr.getLookupContext();
      CashewContext nctx = findContext(idn,ctx);
      if (nctx != null) return nctx;
    }

   return null;
}


private CashewContext findContext(int id,CashewContext ctx)
{
   if (ctx.getId() == id) return ctx;

   CashewContext pctx = null;
   for (CashewContext cctx : ctx.getChildContexts()) {
      if (cctx.getId() > id) break;
      pctx = cctx;
    }
   if (pctx == null) return null;

   return findContext(id,pctx);
}




/********************************************************************************/
/*										*/
/*	Runner management methods						*/
/*										*/
/********************************************************************************/

CuminRunner createRunner(SesameLocation loc,SesameContext gblctx)
{
   MethodDeclaration mthd = getCallMethod(loc);
   List<CashewValue> args = getCallArgs(loc);
   if (args == null || mthd == null) return null;

   SesameThreadContext tctx = new SesameThreadContext(loc.getThread(),
	 loc.getThreadName(),this,gblctx);
   CuminRunner cr = CuminRunner.createRunner(this,getProject(),tctx,mthd,args,true);
   runner_location.put(cr,loc);

   return cr;
}





MethodDeclaration getRunnerMethod(CuminRunner cr)
{
   SesameLocation loc = getLocation(cr);
   MethodDeclaration mthd = getCallMethod(loc);

   return mthd;
}



synchronized void addRunner(SesameExecRunner er)
{
   exec_runners.add(er);
}



synchronized void stopRunners()
{
   for (SesameExecRunner run : exec_runners) {
      run.stopExecution();
    }
}


synchronized void restartRunners()
{
   for (SesameExecRunner run : exec_runners) {
      run.restartExecution();
    }
}


synchronized void startRunners()
{
   for (SesameExecRunner run : exec_runners) {
      run.startExecution();
    }
}



/********************************************************************************/
/*										*/
/*	Handle project changes							*/
/*										*/
/********************************************************************************/

void resetRunners()
{
   for (SesameExecRunner run : exec_runners) {
      resetRunners(run);
    }
}



void resetRunners(SesameExecRunner run)
{
   run.removeAllRunners();
   SesameContext gblctx = run.getBaseContext();
   for (SesameLocation loc : getActiveLocations()) {
      CuminRunner cr = createRunner(loc,gblctx);
      run.addRunner(cr);
    }
   if (run.isContinuous()) {
      run.startExecution();
    }
}


void addSwingComponent(String name)
{
   for (SesameExecRunner ser : exec_runners) {
      ser.addSwingComponent(name);
    }
}



void resetCache()				{ }



/********************************************************************************/
/*										*/
/*	Methods to get global values from underlying execution			*/
/*										*/
/********************************************************************************/

CashewValue lookupValue(String name,String type)
{
   return null;
}


CashewValue evaluate(String expr,String thread,boolean allframes)
{
   return null;
}



SesameValueData evaluateData(String expr,String tid,boolean allframes)
{
   return null;
}

void evaluateVoid(String expr,boolean allframes) throws CashewException
{
}


void enableAccess(String type)		{ }



/********************************************************************************/
/*										*/
/*	Model management methods						*/
/*										*/
/********************************************************************************/

CashewInputOutputModel getIOModel()
{
   return cashew_iomodel;
}


CashewSynchronizationModel getSynchronizationModel()
{
   return cashew_syncmodel;
}



}	// end of class SesameSession




/* end of SesameSession.java */

