/********************************************************************************/
/*                                                                              */
/*              CuminRunnerAst.java                                             */
/*                                                                              */
/*      AST-based code interpreter                                              */
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.sesame.SesameProject;

class CuminRunnerAst extends CuminRunner
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ASTNode         method_node;
private ASTNode         current_node;
private List<CashewValue> call_args;
private CuminRunnerAstVisitor runner_visitor;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminRunnerAst(SesameProject sp,CashewClock cc,ASTNode method,List<CashewValue> args)
{
   super(sp,cc,args);
   
   method_node = method;
   current_node = method_node;
   runner_visitor = new CuminRunnerAstVisitor(this,method);
   
   setupContext();
}





@Override protected void interpretRun(EvalType et)
{
   runner_visitor.setEvalType(et);
   
   try {
      method_node.accept(runner_visitor);
      throw new CuminRunError(CuminRunError.Reason.RETURN);
    }
   catch (CuminRunError r) {
      if (r.getReason() == CuminRunError.Reason.RETURN) current_node = null;
      else current_node = runner_visitor.getCurrentNode();
      throw r;
    }
}




/********************************************************************************/
/*                                                                              */
/*      Context methods                                                         */
/*                                                                              */
/********************************************************************************/

private void setupContext()
{
   CashewContext ctx = new CashewContext();
   LocalFinder lf = new LocalFinder();
   method_node.accept(lf);
   for (JcompSymbol lcl : lf.getLocalVars()) {
      JcompType lty = lcl.getType();
      CashewValue nv = CashewValue.createDefaultValue(lty);
      ctx.define(lcl,nv);
    }
    
   JcompSymbol js = JcompAst.getDefinition(method_node);
   if (!js.isStatic()) {
      ctx.define("this",CashewValue.nullValue());
    }
   JcompType cty = js.getClassType();
   for (ASTNode n = method_node; n != null; n = n.getParent()) {
      if (n instanceof TypeDeclaration) {
         TypeDeclaration td = (TypeDeclaration) n;
         JcompSymbol sty = JcompAst.getDefinition(td.getName());
         JcompType ty = JcompAst.getJavaType(td);
         if (ty != cty && !sty.isStatic()) {
            String nm = sty.getFullName() + ".this";
            ctx.define(nm,CashewValue.nullValue());
          }
       }
    } 
   
   setLoockupContext(ctx);
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



}       // end of class CuminRunnerAst




/* end of CuminRunnerAst.java */

