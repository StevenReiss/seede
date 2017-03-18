/********************************************************************************/
/*										*/
/*		CuminRunner.java						*/
/*										*/
/*	Generic code runner (interpreter)					*/
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



package edu.brown.cs.seede.cumin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.brown.cs.ivy.jcode.JcodeClass;
import edu.brown.cs.ivy.jcode.JcodeDataType;
import edu.brown.cs.ivy.jcode.JcodeFactory;
import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSearcher;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;


public abstract class CuminRunner implements CuminConstants, CashewConstants 
{



/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

public static CuminRunner createRunner(CuminProject cp,CashewContext glblctx,
      MethodDeclaration method,List<CashewValue> args)
{
   return new CuminRunnerAst(cp,glblctx,null,method,args);
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private CuminProject	base_project;
private CuminRunner	nested_call;

protected CuminStack	execution_stack;
protected CashewClock	execution_clock;
protected CashewContext lookup_context;
protected CashewContext global_context;
protected List<CashewValue> call_args;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected CuminRunner(CuminProject cp,CashewContext gblctx,CashewClock cc,List<CashewValue> args)
{
   base_project = cp;
   nested_call = null;
   execution_stack = new CuminStack();
   if (cc == null) {
      execution_clock = new CashewClock();
      execution_clock.tick();
    }
   else execution_clock = cc;
   call_args = args;
   global_context = gblctx;
   lookup_context = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean isAtError()
{
   return false;
}


boolean isAtException()
{
   return false;
}


boolean isComplete()
{
   return false;
}

JcompTyper getTyper()			{ return base_project.getTyper(); }

JcodeFactory getCodeFactory()		{ return base_project.getJcodeFactory(); }

JcompProject getCompProjoect()		{ return base_project.getJcompProject(); }

CashewClock getClock()			{ return execution_clock; }

CuminStack getStack()			{ return execution_stack; }

public CashewContext getLookupContext() 	{ return lookup_context; }
List<CashewValue> getCallArgs() 	{ return call_args; }

protected JcompType convertType(JcodeDataType cty)   
{
   JcompTyper typer = getTyper();
   String tnm = cty.getName();
   JcompType rslt = typer.findType(tnm);
   if (rslt != null) return rslt;
   rslt = typer.findSystemType(tnm);
   return rslt;
}


protected void setLookupContext(CashewContext ctx)
{
   lookup_context = ctx;
   ctx.setStartTime(execution_clock);
}



/********************************************************************************/
/*										*/
/*	Evaluation methods							*/
/*										*/
/********************************************************************************/

public void reset(MethodDeclaration md)
{
   reset();
}


protected void reset()
{
   nested_call = null;
   execution_stack = new CuminStack();
   execution_clock = new CashewClock();
   lookup_context = null;
}



public void interpret(EvalType et) throws CuminRunError
{
   CuminRunError ret = null;

   for ( ; ; ) {
      if (nested_call != null) {
	 try {
	    nested_call.interpret(et);
	  }
	 catch (CuminRunError r) {
	    if (r.getReason() == CuminRunError.Reason.RETURN ||
		  r.getReason() == CuminRunError.Reason.EXCEPTION) {
	       ret = r;
	     }
	    else {
	       lookup_context.setEndTime(execution_clock);
	       throw r;
	     }
	  }
       }

      try {
	 interpretRun(ret);
	 throw new CuminRunError(CuminRunError.Reason.RETURN);
       }
      catch (CuminRunError r) {
	 if (r.getReason() == CuminRunError.Reason.CALL) {
	    nested_call = r.getCallRunner();
	    continue;
	  }
	 lookup_context.setEndTime(execution_clock);
	 throw r;
       }
    }
}


abstract protected void interpretRun(CuminRunError ret) throws CuminRunError;


protected void saveReturn(CashewValue cv)
{

}



/********************************************************************************/
/*										*/
/*	Method lookup								*/
/*										*/
/********************************************************************************/

protected CuminRunner handleCall(CashewClock cc,JcompSymbol method,List<CashewValue> args,
      CallType ctyp)
{
   CashewValue thisarg = null;
   if (args != null && args.size() > 0) {
      thisarg = args.get(0);
      // AcornLog.logD("Call " + method + " on " + thisarg.getDebugString(execution_clock));
    }

   JcompSymbol cmethod = findTargetMethod(cc,method,thisarg,ctyp);
   if (cmethod == null) {
      AcornLog.logE("Couldn't find method to call " + method);
      for (CashewValue cv : args) {
	 AcornLog.logE("ARG: " + cv.getDebugString(cc));
       }
      throw new CuminRunError(CuminRunError.Reason.ERROR,"Missing method " + method);
    }

   JcompType type = cmethod.getClassType();
   if (!type.isKnownType()) {
      MethodDeclaration md = (MethodDeclaration) cmethod.getDefinitionNode();
      if (md != null) return doCall(cc,md,args);
      AcornLog.logE("Missing AST for method declaration " + md);
    }

   JcodeClass mcls = getCodeFactory().findClass(type.getName());
   String jtyp = cmethod.getType().getJavaTypeName();
   JcodeMethod mthd = mcls.findMethod(cmethod.getName(),jtyp);
   return doCall(cc,mthd,args);
}



CuminRunner handleCall(CashewClock cc,JcodeMethod method,List<CashewValue> args,
      CallType ctyp)
{
   CashewValue thisarg = null;
   if (args != null && args.size() > 0 && !method.isStatic()) {
      thisarg = args.get(0);
      // AcornLog.logD("Call " + method + " on " + thisarg.getDebugString(execution_clock));
    }

   JcodeMethod cmethod = findTargetMethod(cc,method,thisarg,ctyp);

   if (cmethod == null) {
      AcornLog.logE("Couldn't find bc method to call " + method);
      for (CashewValue cv : args) {
	 AcornLog.logE("ARG: " + cv.getDebugString(cc));
       }
    }

   JcompType type = getTyper().findType(cmethod.getDeclaringClass().getName());
   if (type != null && !type.isKnownType()) {
      int narg = cmethod.getNumArguments();
      List<JcompType> argtyps = new ArrayList<JcompType>();
      for (int i = 0; i < narg; ++i) {
         JcodeDataType cotyp = cmethod.getArgType(i);
         JcompType cmtyp = convertType(cotyp);
         if (cmtyp != null && argtyps != null) argtyps.add(cmtyp);
         else argtyps = null;
       }
      JcompType rtyp = convertType(cmethod.getReturnType());
      JcompType mtyp = null;
      if (argtyps != null) {
         mtyp = JcompType.createMethodType(rtyp,argtyps,false);
       }
      MethodDeclaration md = findAstForMethod(cmethod.getFullName(),mtyp);
      if (md != null) return doCall(cc,md,args);
    }

   return doCall(cc,cmethod,args);
}


private CuminRunner doCall(CashewClock cc,MethodDeclaration ast,List<CashewValue> args)
{
   CuminRunnerAst rast = new CuminRunnerAst(base_project,global_context,cc,ast,args);
   lookup_context.addNestedContext(rast.getLookupContext());

   AcornLog.logD("Start ast call to " + ast.getName());

   return rast;
}


CuminRunner doCall(CashewClock cc,JcodeMethod mthd,List<CashewValue> args)
{
   CuminRunnerByteCode rbyt = new CuminRunnerByteCode(base_project,global_context,cc,mthd,args);

   AcornLog.logD("Start binary call to " + mthd + " with " + args);

   return rbyt;
}


private JcompSymbol findTargetMethod(CashewClock cc,JcompSymbol method,
      CashewValue arg0,CallType ctyp)
{
   JcompType base = null;
   if (arg0 != null) base = arg0.getDataType(cc);
   if (method.isStatic() || ctyp == CallType.STATIC || ctyp == CallType.SPECIAL) {
      return method;
    }
   JcompSymbol nmethod = base.lookupMethod(getTyper(),method.getName(),method.getType());
   if (nmethod == null) {
      base.defineAll(getTyper());
      nmethod = base.lookupMethod(getTyper(),method.getName(),method.getType());
    }
   if (nmethod == null) {
      AcornLog.logD("Can't find method " + method.getName() + " " + method.getType() +
		       " " + base);
      nmethod = method;
    }

   return nmethod;
}



private JcodeMethod findTargetMethod(CashewClock cc,JcodeMethod method,
      CashewValue arg0,CallType ctyp)
{
   JcompType base = null;
   if (arg0 != null) base = arg0.getDataType(cc);
   if (method.isStatic() || ctyp == CallType.STATIC || ctyp == CallType.SPECIAL) {
      return method;
    }

   JcodeClass cls = getCodeFactory().findClass(base.getName());
   if (cls == null) cls = getCodeFactory().findClass("java.lang.Object");
   JcodeMethod cmethod = cls.findInheritedMethod(method.getName(),method.getDescription());

   return cmethod;
}



private MethodDeclaration findAstForMethod(String nm,JcompType mtyp)
{
   JcompProject jp = base_project.getJcompProject();
   String typ = "METHOD";
   if (nm.endsWith(".<init>")) {
      int idx = nm.lastIndexOf(".<init>");
      nm = nm.substring(0,idx);
      typ = "CONSTRUCTOR";
    }
   JcompSearcher js = jp.findSymbols(nm,typ);
   for (JcompSymbol sr : js.getSymbols()) {
      ASTNode an = sr.getDefinitionNode();
      if (mtyp !=  null && !sr.getType().isCompatibleWith(mtyp)) continue;
      if (an != null && an instanceof MethodDeclaration) {
	 return ((MethodDeclaration) an);
       }
    }

   return null;
}


/********************************************************************************/
/*                                                                              */
/*      Handle special cases of NEW                                             */
/*                                                                              */
/********************************************************************************/

protected CashewValue handleNew(JcompType nty)
{
   CashewValue rslt = null;
   
   if (nty == STRING_TYPE) {
      rslt = CashewValue.stringValue("");
    }
   else if (nty == FILE_TYPE) {
      rslt = CashewValue.fileValue();
    }
   else {
      nty.defineAll(getTyper());
      rslt = CashewValue.objectValue(nty);
    }
   
   return rslt;
}
}	// end of class CuminRunner




/* end of CuminRunner.java */

