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

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminRunnerAstVisitor extends ASTVisitor implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private CuminStack      execution_stack;
private CashewClock     execution_clock;
private CashewContext   execution_context;


private static Map<Object,CuminOperator> op_map;

static {
   op_map.put(InfixExpression.Operator.AND,CuminOperator.AND);
   op_map.put(InfixExpression.Operator.DIVIDE,CuminOperator.DIV); 
   op_map.put(InfixExpression.Operator.EQUALS,CuminOperator.EQL); 
   op_map.put(InfixExpression.Operator.GREATER,CuminOperator.GTR);  
   op_map.put(InfixExpression.Operator.GREATER_EQUALS,CuminOperator.GEQ);  
   op_map.put(InfixExpression.Operator.LEFT_SHIFT,CuminOperator.LSH);  
   op_map.put(InfixExpression.Operator.LESS,CuminOperator.LSS);  
   op_map.put(InfixExpression.Operator.LESS_EQUALS,CuminOperator.LEQ);  
   op_map.put(InfixExpression.Operator.MINUS,CuminOperator.SUB);   
   op_map.put(InfixExpression.Operator.NOT_EQUALS,CuminOperator.NEQ);  
   op_map.put(InfixExpression.Operator.OR,CuminOperator.OR);  
   op_map.put(InfixExpression.Operator.PLUS,CuminOperator.ADD);   
   op_map.put(InfixExpression.Operator.REMAINDER,CuminOperator.MOD);   
   op_map.put(InfixExpression.Operator.RIGHT_SHIFT_SIGNED,CuminOperator.RSH);  
   op_map.put(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED,CuminOperator.RSHU);  
   op_map.put(InfixExpression.Operator.TIMES,CuminOperator.MUL);  
   op_map.put(InfixExpression.Operator.XOR,CuminOperator.XOR); 
   op_map.put(PostfixExpression.Operator.INCREMENT,CuminOperator.POSTINCR);
   op_map.put(PostfixExpression.Operator.DECREMENT,CuminOperator.POSTDECR);
   op_map.put(PrefixExpression.Operator.COMPLEMENT,CuminOperator.COMP);
   op_map.put(PrefixExpression.Operator.DECREMENT,CuminOperator.DECR);
   op_map.put(PrefixExpression.Operator.INCREMENT,CuminOperator.INCR);
   op_map.put(PrefixExpression.Operator.MINUS,CuminOperator.NEG);
   op_map.put(PrefixExpression.Operator.PLUS,CuminOperator.NOP);
   op_map.put(Assignment.Operator.ASSIGN,CuminOperator.ASG);
   op_map.put(Assignment.Operator.BIT_AND_ASSIGN,CuminOperator.ASG_AND);
   op_map.put(Assignment.Operator.BIT_OR_ASSIGN,CuminOperator.ASG_OR);
   op_map.put(Assignment.Operator.BIT_XOR_ASSIGN,CuminOperator.ASG_XOR);
   op_map.put(Assignment.Operator.DIVIDE_ASSIGN,CuminOperator.ASG_DIV);
   op_map.put(Assignment.Operator.LEFT_SHIFT_ASSIGN,CuminOperator.ASG_LSH);
   op_map.put(Assignment.Operator.MINUS_ASSIGN,CuminOperator.ASG_SUB);
   op_map.put(Assignment.Operator.PLUS_ASSIGN,CuminOperator.ASG_ADD);
   op_map.put(Assignment.Operator.REMAINDER_ASSIGN,CuminOperator.ASG_MOD);
   op_map.put(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN,CuminOperator.ASG_RSH);
   op_map.put(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN,CuminOperator.ASG_RSHU);
   op_map.put(Assignment.Operator.TIMES_ASSIGN,CuminOperator.ASG_MUL);
}




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminRunnerAstVisitor(CuminStack stack,CashewClock clock,CashewContext ctx)
{
   execution_stack = stack;
   execution_clock = clock;
   execution_context = ctx;
}


/********************************************************************************/
/*                                                                              */
/*      Constant handling                                                       */
/*                                                                              */
/********************************************************************************/

@Override public boolean visit(BooleanLiteral v)
{
   execution_stack.push(CashewValue.booleanValue(v.booleanValue()));
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
   execution_stack.push(CashewValue.nullValue());
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
   execution_stack.push(CashewValue.stringValue(v.getLiteralValue()));
   return false;
}



@Override public boolean visit(TypeLiteral v)
{
   JcompType jt = JcompAst.getExprType(v);
   JcompType acttyp = JcompAst.getJavaType(v.getType());
   execution_stack.push(CashewValue.classValue(acttyp));
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
   CashewValue v2 = execution_stack.pop();
   CashewValue v1 = execution_stack.pop();
   CuminOperator op = op_map.get(v.getOperator());
   JcompType tgt = JcompAst.getExprType(v.getLeftHandSide());
   CashewValue v0 = CuminEvaluator.evaluateAssign(execution_clock,op,v1,v2,tgt);
   execution_stack.push(v0);
}



@Override public void endVisit(CastExpression v)
{
   JcompType tgt = JcompAst.getJavaType(v.getType());
   CashewValue cv = execution_stack.pop();
   cv = CuminEvaluator.castValue(execution_clock,cv,tgt);
   execution_stack.push(cv);
}


@Override public void endVisit(ClassInstanceCreation v)
{
   
}



@Override public boolean visit(ConditionalExpression v)
{
   v.getExpression().accept(this);
   CashewValue cv = execution_stack.pop();
   if (cv.getBoolean(execution_clock)) {
      v.getThenExpression().accept(this);
    }
   else {
      v.getElseExpression().accept(this);
    }
   return false;
}


@Override public boolean visit(FieldAccess v)
{
   v.getExpression().accept(this);
   CashewValue obj = execution_stack.pop();
   JcompSymbol sym = JcompAst.getReference(v.getName());
   CashewValue rslt = obj.getFieldValue(execution_clock,sym.getFullName());
   execution_stack.push(rslt);
   return false;
}


@Override public boolean visit(InfixExpression v) 
{
   if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
      v.getLeftOperand().accept(this);
      CashewValue v1 = execution_stack.pop();
      if (v1.getBoolean(execution_clock)) {
         v.getRightOperand().accept(this);
       }
      else {
         execution_stack.push(v1);
       }
      return false;
    }
   else if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
      v.getLeftOperand().accept(this);
      CashewValue v1 = execution_stack.pop();
      if (v1.getBoolean(execution_clock)) {
         execution_stack.push(v1);
       }
      else {
         v.getRightOperand().accept(this);
       }
    }
   return true;
}


@Override public void endVisit(InfixExpression v)
{
   CashewValue v2 = execution_stack.pop();
   CashewValue v1 = execution_stack.pop();
   CuminOperator op = op_map.get(v.getOperator());
   CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1,v2);
   execution_stack.push(v0);
}


@Override public void endVisit(InstanceofExpression v)
{
   
}


@Override public void endVisit(MethodInvocation v)
{
   
}



@Override public void endVisit(SimpleName v)
{
   JcompSymbol js = JcompAst.getReference(v);
   if (js == null) {
      // throw error of some sort
    }
   if (js.isFieldSymbol()) {
      // handle this.field reference
    }
   // this should look things up on the stack
   CashewValue cv = execution_context.findReference(js);
   execution_stack.push(cv);
}



@Override public boolean visit(QualifiedName v)
{
   // could be field access -- need to handle
   
   v.getName().accept(this);
   
   return false;
}


@Override public void endVisit(ParenthesizedExpression v)
{
   // nothing needed here
}


@Override public void endVisit(PostfixExpression v)
{
   CashewValue v1 = execution_stack.pop();
   CuminOperator op = op_map.get(v.getOperator());
   CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1);
   // need to handle LVALUES
   execution_stack.push(v0);
}


@Override public void endVisit(PrefixExpression v)
{
   CashewValue v1 = execution_stack.pop();
   CuminOperator op = op_map.get(v.getOperator());
   CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1);
   // need to handle LVALUES
   execution_stack.push(v0);
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
/*      Statement visitors                                                      */
/*                                                                              */
/********************************************************************************/

@Override public boolean visit(AssertStatement as)
{
   return true;
}



@Override public boolean visit(Block as)
{
   return true;
}


@Override public boolean visit(BreakStatement as)
{
   return true;
}


@Override public boolean visit(ConstructorInvocation as)
{
   return true;
}


@Override public boolean visit(ContinueStatement as)
{
   return true;
}


@Override public boolean visit(DoStatement as)
{
   return true;
}


@Override public boolean visit(EmptyStatement as)
{
   return true;
}


@Override public boolean visit(ExpressionStatement as)
{
   return true;
}


@Override public boolean visit(ForStatement as)
{
   return true;
}


@Override public boolean visit(IfStatement as)
{
   return true;
}


@Override public boolean visit(LabeledStatement as)
{
   return true;
}


@Override public boolean visit(ReturnStatement as)
{
   return true;
}


@Override public boolean visit(SuperConstructorInvocation as)
{
   return true;
}


@Override public boolean visit(SwitchStatement as)
{
   return true;
}


@Override public boolean visit(SynchronizedStatement as)
{
   return true;
}


@Override public boolean visit(ThrowStatement as)
{
   return true;
}


@Override public boolean visit(TryStatement as)
{
   return true;
}


@Override public boolean visit(TypeDeclarationStatement as)
{
   return true;
}


@Override public boolean visit(VariableDeclarationStatement as)
{
   return true;
}


@Override public boolean visit(WhileStatement as)
{
   return true;
}



}       // end of class CuminRunnerAstVisitor





/* end of CuminRunnerAstVisitor.java */

