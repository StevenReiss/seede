/********************************************************************************/
/*                                                                              */
/*              CuminMethdRunner.java                                           */
/*                                                                              */
/*      Evaluate a method                                                       */
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.brown.cs.ivy.jcode.JcodeClass;
import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminMethodRunner implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

enum CallType { STATIC, SPECIAL, INTERFACE, DYNAMIC, VIRTUAL };

private List<CashewValue> initial_parameters;
private CuminRunner     current_runner;
private MethodDeclaration ast_method;
private JcodeMethod       byte_method;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminMethodRunner(JcompTyper typer,JcompSymbol sym,List<CashewValue> params)
{
   this(params);
}



CuminMethodRunner(JcompTyper typer,JcodeMethod sym,List<CashewValue> params)
{
   this(params);
}


private CuminMethodRunner(List<CashewValue> params)
{
   initial_parameters = params;
   current_runner = null;
   ast_method = null;
   byte_method = null;
}




/********************************************************************************/
/*                                                                              */
/*      Evaluation methods                                                      */
/*                                                                              */
/********************************************************************************/

void evaluate(EvalType et)
{
   setupRunner();
   
}



private void setupRunner()
{
   if (current_runner != null) return;
   
   // first split method into class/method/signature
   // then determine if class is known or not
   // if known, find its AST and use AST evaluation
   // else find the JcodeMethod and use byte code evaluation
   // set up context
   // set up initial stack
}


/********************************************************************************/
/*                                                                              */
/*      Find local variables for a method                                       */
/*                                                                              */
/********************************************************************************/

private static class LocalFinder extends ASTVisitor {

   private Set<JcompSymbol> local_vars;
   
   LocalFinder() {
      local_vars = new HashSet<JcompSymbol>();
    }
   
   Set<JcompSymbol> getLocalVars()              { return local_vars; }
   
   @Override public void endVisit(SingleVariableDeclaration n) {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      local_vars.add(js);
    }
   
   @Override public void endVisit(VariableDeclarationFragment n) {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      local_vars.add(js);
    }
   
}       // end of inner class LocalFinder

   

}       // end of class CuminMethdRunner




/* end of CuminMethdRunner.java */

