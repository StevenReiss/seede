/********************************************************************************/
/*                                                                              */
/*              CuminRunner.java                                                */
/*                                                                              */
/*      Generic code runner (interpreter)                                       */
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



package edu.brown.cs.seede.cumin;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.brown.cs.ivy.jcode.JcodeClass;
import edu.brown.cs.ivy.jcode.JcodeFactory;
import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSearcher;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cumin.CuminMethodRunner.CallType;
import edu.brown.cs.seede.sesame.SesameProject;


public abstract class CuminRunner implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameProject   base_project;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected CuminRunner(SesameProject sp)
{
   base_project = sp;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
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

JcompTyper getTyper()           { return base_project.getTyper(); }

JcodeFactory getCodeFactory()   { return base_project.getJcodeFactory(); }

JcompProject getCompProjoect()  { return base_project.getJcompProject(); }




/********************************************************************************/
/*                                                                              */
/*      Method lookup                                                           */
/*                                                                              */
/********************************************************************************/

protected CashewValue handleCall(CashewClock cc,JcompSymbol method,List<CashewValue> args,
      CallType ctyp)
{
   CashewValue thisarg = null;
   if (args != null && args.size() > 0) thisarg = args.get(1);
   
   method = findTargetMethod(cc,method,thisarg,ctyp); 
   
   JcompType type = method.getClassType();
   if (type.isKnownType()) {
      MethodDeclaration md = findAstForMethod(method.getFullName());
      if (md != null) return doCall(cc,md,args);
    }
   
   JcodeClass mcls = getCodeFactory().findClass(type.getName());
   JcodeMethod mthd = mcls.findMethod(method.getName(),method.getType().getName());
   return doCall(cc,mthd,args);
}



protected CashewValue handleCall(CashewClock cc,JcodeMethod method,List<CashewValue> args,
      CallType ctyp)
{
   CashewValue thisarg = null;
   if (args != null && args.size() > 0) thisarg = args.get(1);
   
   method = findTargetMethod(cc,method,thisarg,ctyp);
   
   JcompType type = getTyper().findType(method.getDeclaringClass().getName());
   if (type.isKnownType()) {
      MethodDeclaration md = findAstForMethod(method.getFullName());
      if (md != null) return doCall(cc,md,args);
    }
   
   return doCall(cc,method,args);
}


private CashewValue doCall(CashewClock cc,MethodDeclaration ast,List<CashewValue> args)
{
   return null;
}


private CashewValue doCall(CashewClock cc,JcodeMethod mthd,List<CashewValue> args)
{
   return null;
}

private JcompSymbol findTargetMethod(CashewClock cc,JcompSymbol method,
      CashewValue arg0,CallType ctyp)
{
   JcompType base = null;
   if (arg0 != null) base = arg0.getDataType(cc);
   if (method.isStatic() || ctyp == CallType.STATIC || ctyp == CallType.SPECIAL) {
      return method;
    }
   method = base.lookupMethod(getTyper(),method.getName(),method.getType());
   
   return method;
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
   method = cls.findMethod(method.getName(),method.getDescription());
   
   return method;
}



private MethodDeclaration findAstForMethod(String nm)
{
   JcompProject jp = base_project.getJcompProject();
   JcompSearcher js = jp.findSymbols(nm,"METHOD");
   for (JcompSearcher.SearchResult sr : js.getMatches()) {
      JcompSource src = sr.getFile();
      if (src == null) continue;
      for (JcompSemantics jsem : jp.getSources()) {
         if (jsem.getFile() == src) {
            ASTNode n = jsem.getAstNode();
            MethodFinder mf = new MethodFinder(sr.getSymbol());
            n.accept(mf);
            MethodDeclaration md = mf.getMethodAst();
            if (md != null) return md;
          }
       }
    }
   
   return null;
}




private static class MethodFinder extends ASTVisitor {
   
   private JcompSymbol look_for;
   private MethodDeclaration found_method;
   
   MethodFinder(JcompSymbol js) {
      look_for = js;
      found_method = null;
    }
   
   MethodDeclaration getMethodAst()             { return found_method; }
   
   @Override public boolean visit(MethodDeclaration n) {
      JcompSymbol js = JcompAst.getDefinition(n);
      if (js == null) js = JcompAst.getDefinition(n.getName());
      if (js == look_for) {
         found_method = n;
       }
      return false;
    }
   
}       // end of inner class MethodFinder





/********************************************************************************/
/*                                                                              */
/*      Evaluation methods                                                      */
/*                                                                              */
/********************************************************************************/

abstract void interpret(EvalType et);





}       // end of class CuminRunner




/* end of CuminRunner.java */

