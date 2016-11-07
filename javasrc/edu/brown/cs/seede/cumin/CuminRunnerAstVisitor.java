/********************************************************************************/
/*                                                                              */
/*              CuminRunnerAstVisitor.java                                      */
/*                                                                              */
/*      AST visitor to do execution                                             */
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.jcomp.JcompAst;

import edu.brown.cs.seede.cashew.CashewValue;

class CuminRunnerAstVisitor extends ASTVisitor implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private CuminStack      execution_stack;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminRunnerAstVisitor(CuminStack stack)
{
   execution_stack = stack;
}


/********************************************************************************/
/*                                                                              */
/*      Constant handling                                                       */
/*                                                                              */
/********************************************************************************/

@Override public boolean visit(BooleanLiteral v)
{
   JcompType bool = JcompAst.getExprType(v);
   execution_stack.push(CashewValue.numericValue(bool,v.booleanValue() ? 1 : 0));
   return false; 
}



@Override public boolean visit(CharacterLiteral v)
{
   JcompType ctype = JcompAst.getExprType(v);
   execution_stack.push(CashewValue.characterValue(ctype,v.charValue()));
   return false; 
}


@Override public boolean visit(NullLiteral v)
{
   execution_stack.push(CashewValue.nullValue(JcompAst.getTyper(v)));
   return false;
}



@Override public boolean visit(NumberLiteral v)
{
   JcompType jt = JcompAst.getExprType(v);
   switch (jt.getName()) {
      case "float" :
      case "double" :
         double dv = Double.parseDouble(v.getToken());
         execution_stack.push(CashewValue.numericValue(jt,dv));
         break;
      default :
         long lv = Long.parseLong(v.getToken());
         execution_stack.push(CashewValue.numericValue(jt,lv));
         break;
    }
   return false;
}



@Override public boolean visit(StringLiteral v)
{
   JcompType jt = JcompAst.getExprType(v);
   execution_stack.push(CashewValue.stringValue(jt,v.getLiteralValue()));
   return false;
}



@Override public boolean visit(TypeLiteral v)
{
   JcompType jt = JcompAst.getExprType(v);
   JcompType acttyp = JcompAst.getJavaType(v.getType());
   execution_stack.push(CashewValue.classValue(jt,acttyp));
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Expression computation                                                  */
/*                                                                              */
/********************************************************************************/

@Override public void endVisit(ArrayAccess v)
{
   
}



@Override public void endVisit(ArrayCreation v)
{
   
}



@Override public void endVisit(Assignment v)
{
   
}



@Override public void endVisit(CastExpression v)
{
   
}


@Override public void endVisit(ClassInstanceCreation v)
{
   
}



@Override public void endVisit(ConditionalExpression v)
{
   
}


@Override public void endVisit(FieldAccess v)
{
   
}


@Override public void endVisit(InfixExpression v)
{
   
}


@Override public void endVisit(InstanceofExpression v)
{
   
}


@Override public void endVisit(MethodInvocation v)
{
   
}



@Override public void endVisit(SimpleName v)
{
   
}



@Override public void endVisit(QualifiedName v)
{
   
}


@Override public void endVisit(ParenthesizedExpression v)
{
   
}


@Override public void endVisit(PostfixExpression v)
{
   
}


@Override public void endVisit(PrefixExpression v)
{
   
}


@Override public void endVisit(SuperFieldAccess v)
{
   
}



@Override public void endVisit(SuperMethodInvocation v)
{
   
}


@Override public void endVisit(ThisExpression v)
{
   
}


@Override public void endVisit(VariableDeclarationExpression v)
{
   
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

JcompType getSystemType(ASTNode n,String s)
{
   JcompTyper typer = JcompAst.getTyper(n); 
   return typer.findSystemType(s);
}



}       // end of class CuminRunnerAstVisitor





/* end of CuminRunnerAstVisitor.java */

