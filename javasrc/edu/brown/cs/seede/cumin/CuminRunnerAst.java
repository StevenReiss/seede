/********************************************************************************/
/*										*/
/*		CuminRunnerAst.java						*/
/*										*/
/*	AST-based code interpreter						*/
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.brown.cs.seede.acorn.AcornLog;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminRunnerAst extends CuminRunner
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ASTNode 	method_node;
private ASTNode 	current_node;
private ASTNode 	next_node;
private int             last_line;

private static Map<Object,CuminOperator> op_map;

static {
   op_map = new HashMap<>();
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
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminRunnerAst(CuminProject sp,CashewClock cc,ASTNode method,List<CashewValue> args)
{
   super(sp,cc,args);

   method_node = method;
   current_node = method_node;
   last_line = 0;

   setupContext();
}





@Override protected void interpretRun(CuminRunError err) throws CuminRunError
{
   ASTNode afternode = null;
   CuminRunError passerror = err;

   for ( ; ; ) {
      try {
	 if (passerror != null) {
	    evalThrow(current_node,passerror);
	    passerror = null;
	  }
	 else {
	    evalNode(current_node,afternode);
	  }
	 if (next_node == null) {
	    afternode = current_node;
	    current_node = current_node.getParent();
	  }
	 else {
	    current_node = next_node;
	    afternode = null;
	  }
       }
      catch (CuminRunError r) {
	 if (current_node == method_node) throw r;
	 passerror = r;
	 current_node = current_node.getParent();
	 afternode = null;
       }
    }
}





/********************************************************************************/
/*										*/
/*	Context methods 							*/
/*										*/
/********************************************************************************/

private void setupContext()
{
   CashewContext ctx = new CashewContext();
   LocalFinder lf = new LocalFinder();
   method_node.accept(lf);
   for (JcompSymbol lcl : lf.getLocalVars()) {
      JcompType lty = lcl.getType();
      CashewValue nv = CashewValue.createDefaultValue(lty);
      nv = CashewValue.createReference(nv);
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
            CashewValue nv = CashewValue.nullValue();
            nv = CashewValue.createReference(nv);
	    ctx.define(nm,nv);
	  }
       }
    }

   CashewValue zv = CashewValue.numericValue(CashewConstants.INT_TYPE,0);
   ctx.define("*LINE*",CashewValue.createReference(zv));
   setLoockupContext(ctx);
}




/********************************************************************************/
/*										*/
/*	Find local variables for a method					*/
/*										*/
/********************************************************************************/

private static class LocalFinder extends ASTVisitor {

   private Set<JcompSymbol> local_vars;

   LocalFinder() {
      local_vars = new HashSet<JcompSymbol>();
    }

   Set<JcompSymbol> getLocalVars()		{ return local_vars; }

   @Override public void endVisit(SingleVariableDeclaration n) {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      if (js != null) local_vars.add(js);
    }

   @Override public void endVisit(VariableDeclarationFragment n) {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      local_vars.add(js);
    }

}	// end of inner class LocalFinder




/********************************************************************************/
/*										*/
/*	Evauation dispatch methods						*/
/*										*/
/********************************************************************************/

private void evalNode(ASTNode node,ASTNode afterchild) throws CuminRunError
{
   if (node instanceof Statement && afterchild == null) {
      CompilationUnit cu = (CompilationUnit) node.getRoot();
      int lno = cu.getLineNumber(node.getStartPosition());
      if (lno != last_line) {
         last_line = lno;
         CashewValue lvl = CashewValue.numericValue(CashewConstants.INT_TYPE,lno);
         lookup_context.findReference("*LINE*").setValueAt(execution_clock,lvl);
       }
      
      // System.err.println("EVAL: " + node);
      // check for breakpoint
      // check for step
      // check for timeout
    }

   next_node = null;

   switch (node.getNodeType()) {
      // nodes that can be ignored for interpretation
      case ASTNode.ANNOTATION_TYPE_DECLARATION :
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
      case ASTNode.ANONYMOUS_CLASS_DECLARATION :
      case ASTNode.ARRAY_TYPE :
      case ASTNode.BLOCK_COMMENT :
      case ASTNode.COMPILATION_UNIT :
      case ASTNode.EMPTY_STATEMENT :
      case ASTNode.ENUM_CONSTANT_DECLARATION :
      case ASTNode.ENUM_DECLARATION :
      case ASTNode.IMPORT_DECLARATION :
      case ASTNode.JAVADOC :
      case ASTNode.LINE_COMMENT :
      case ASTNode.MARKER_ANNOTATION :
      case ASTNode.MEMBER_REF :
      case ASTNode.MEMBER_VALUE_PAIR :
      case ASTNode.METHOD_REF :
      case ASTNode.METHOD_REF_PARAMETER :
      case ASTNode.MODIFIER :
      case ASTNode.NORMAL_ANNOTATION :
      case ASTNode.PACKAGE_DECLARATION :
      case ASTNode.PARAMETERIZED_TYPE :
      case ASTNode.PRIMITIVE_TYPE :
      case ASTNode.QUALIFIED_TYPE :
      case ASTNode.SIMPLE_TYPE :
      case ASTNode.SINGLE_MEMBER_ANNOTATION :
      case ASTNode.TAG_ELEMENT :
      case ASTNode.TEXT_ELEMENT :
      case ASTNode.TYPE_DECLARATION :
      case ASTNode.TYPE_DECLARATION_STATEMENT :
      case ASTNode.TYPE_PARAMETER :
      case ASTNode.UNION_TYPE :
      case ASTNode.WILDCARD_TYPE :
	 break;
      case ASTNode.ARRAY_ACCESS :
	 visit((ArrayAccess) node,afterchild);
	 break;
      case ASTNode.ARRAY_CREATION :
	 break;
      case ASTNode.ARRAY_INITIALIZER :
	 visit((ArrayInitializer) node,afterchild);
	 break;
      case ASTNode.ASSERT_STATEMENT :
	 visit((AssertStatement) node,afterchild);
	 break;
      case ASTNode.ASSIGNMENT :
	 visit((Assignment) node,afterchild);
	 break;
      case ASTNode.BLOCK :
	 visit((Block) node,afterchild);
	 break;
      case ASTNode.BOOLEAN_LITERAL :
	 visit((BooleanLiteral) node);
	 break;
      case ASTNode.BREAK_STATEMENT :
	 visit((BreakStatement) node);
	 break;
      case ASTNode.CAST_EXPRESSION :
	 visit((CastExpression) node,afterchild);
	 break;
      case ASTNode.CATCH_CLAUSE :
	 break;
      case ASTNode.CHARACTER_LITERAL :
	 visit((CharacterLiteral) node);
	 break;
      case ASTNode.CLASS_INSTANCE_CREATION :
	 break;
      case ASTNode.CONDITIONAL_EXPRESSION :
	 visit((ConditionalExpression) node,afterchild);
	 break;
      case ASTNode.CONSTRUCTOR_INVOCATION :
	 break;
      case ASTNode.CONTINUE_STATEMENT :
	 visit((ContinueStatement) node);
	 break;
      case ASTNode.DO_STATEMENT :
	 visit((DoStatement) node,afterchild);
	 break;
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 break;
      case ASTNode.EXPRESSION_STATEMENT :
	 visit((ExpressionStatement) node,afterchild);
	 break;
      case ASTNode.FIELD_ACCESS :
	 visit((FieldAccess) node,afterchild);
	 break;
      case ASTNode.FIELD_DECLARATION :
	 visit((FieldDeclaration) node,afterchild);
	 break;
      case ASTNode.FOR_STATEMENT :
	 visit((ForStatement) node,afterchild);
	 break;
      case ASTNode.IF_STATEMENT :
	 visit((IfStatement) node,afterchild);
	 break;
      case ASTNode.INFIX_EXPRESSION :
	 visit((InfixExpression) node,afterchild);
	 break;
      case ASTNode.INITIALIZER :
	 break;
      case ASTNode.INSTANCEOF_EXPRESSION :
	 visit((InstanceofExpression) node,afterchild);
	 break;
      case ASTNode.LABELED_STATEMENT :
	 visit((LabeledStatement) node,afterchild);
	 break;
      case ASTNode.METHOD_DECLARATION :
         visit((MethodDeclaration) node,afterchild);
	 break;
      case ASTNode.METHOD_INVOCATION :
         visit((MethodInvocation) node,afterchild);
	 break;
      case ASTNode.NULL_LITERAL :
	 visit((NullLiteral) node);
	 break;
      case ASTNode.NUMBER_LITERAL :
	 visit((NumberLiteral) node);
	 break;
      case ASTNode.PARENTHESIZED_EXPRESSION :
	 visit((ParenthesizedExpression) node,afterchild);
	 break;
      case ASTNode.POSTFIX_EXPRESSION :
	 visit((PostfixExpression) node,afterchild);
	 break;
      case ASTNode.PREFIX_EXPRESSION :
	 visit((PrefixExpression) node,afterchild);
	 break;
      case ASTNode.QUALIFIED_NAME :
	 visit((QualifiedName) node,afterchild);
	 break;
      case ASTNode.RETURN_STATEMENT :
         visit((ReturnStatement) node,afterchild);
	 break;
      case ASTNode.SIMPLE_NAME :
	 visit((SimpleName) node);
	 break;
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
	 visit((SingleVariableDeclaration) node,afterchild);
	 break;
      case ASTNode.STRING_LITERAL :
	 visit((StringLiteral) node);
	 break;
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
	 break;
      case ASTNode.SUPER_FIELD_ACCESS :
	 break;
      case ASTNode.SUPER_METHOD_INVOCATION :
	 break;
      case ASTNode.SWITCH_CASE :
	 visit((SwitchCase) node,afterchild);
	 break;
      case ASTNode.SWITCH_STATEMENT :
	 visit((SwitchStatement) node,afterchild);
	 break;
      case ASTNode.SYNCHRONIZED_STATEMENT :
	 break;
      case ASTNode.THIS_EXPRESSION :
	 visit((ThisExpression) node);
	 break;
      case ASTNode.THROW_STATEMENT :
	 visit((ThrowStatement) node,afterchild);
	 break;
      case ASTNode.TRY_STATEMENT :
	 visit((TryStatement) node,afterchild);
	 break;
      case ASTNode.TYPE_LITERAL :
	 visit((TypeLiteral) node);
	 break;
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
	 visit((VariableDeclarationExpression) node,afterchild);
	 break;
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
	 visit((VariableDeclarationFragment) node,afterchild);
	 break;
      case ASTNode.VARIABLE_DECLARATION_STATEMENT :
         visit((VariableDeclarationStatement) node,afterchild);
	 break;
      case ASTNode.WHILE_STATEMENT :
	 visit((WhileStatement) node,afterchild);
	 break;
      default :
	 AcornLog.logE("Unknown AST node " + current_node);
	 break;

    }
}




private void evalThrow(ASTNode node,CuminRunError cause) throws CuminRunError
{
   next_node = null;

   // need to restore stack in each of these

   switch (node.getNodeType()) {
      // nodes that can be ignored for catching exceptions
      case ASTNode.ANNOTATION_TYPE_DECLARATION :
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
      case ASTNode.ANONYMOUS_CLASS_DECLARATION :
      case ASTNode.ARRAY_TYPE :
      case ASTNode.BLOCK_COMMENT :
      case ASTNode.COMPILATION_UNIT :
      case ASTNode.EMPTY_STATEMENT :
      case ASTNode.ENUM_CONSTANT_DECLARATION :
      case ASTNode.ENUM_DECLARATION :
      case ASTNode.IMPORT_DECLARATION :
      case ASTNode.JAVADOC :
      case ASTNode.LINE_COMMENT :
      case ASTNode.MARKER_ANNOTATION :
      case ASTNode.MEMBER_REF :
      case ASTNode.MEMBER_VALUE_PAIR :
      case ASTNode.METHOD_REF :
      case ASTNode.METHOD_REF_PARAMETER :
      case ASTNode.MODIFIER :
      case ASTNode.NORMAL_ANNOTATION :
      case ASTNode.PACKAGE_DECLARATION :
      case ASTNode.PARAMETERIZED_TYPE :
      case ASTNode.PRIMITIVE_TYPE :
      case ASTNode.QUALIFIED_TYPE :
      case ASTNode.SIMPLE_TYPE :
      case ASTNode.SINGLE_MEMBER_ANNOTATION :
      case ASTNode.TAG_ELEMENT :
      case ASTNode.TEXT_ELEMENT :
      case ASTNode.TYPE_DECLARATION :
      case ASTNode.TYPE_DECLARATION_STATEMENT :
      case ASTNode.TYPE_PARAMETER :
      case ASTNode.UNION_TYPE :
      case ASTNode.WILDCARD_TYPE :
      case ASTNode.ARRAY_ACCESS :
      case ASTNode.ARRAY_CREATION :
      case ASTNode.ARRAY_INITIALIZER :
      case ASTNode.ASSERT_STATEMENT :
      case ASTNode.ASSIGNMENT :
      case ASTNode.BLOCK :
      case ASTNode.BOOLEAN_LITERAL :
      case ASTNode.BREAK_STATEMENT :
      case ASTNode.CAST_EXPRESSION :
      case ASTNode.CATCH_CLAUSE :
      case ASTNode.CHARACTER_LITERAL :
      case ASTNode.CLASS_INSTANCE_CREATION :
      case ASTNode.CONDITIONAL_EXPRESSION :
      case ASTNode.CONSTRUCTOR_INVOCATION :
      case ASTNode.CONTINUE_STATEMENT :
      case ASTNode.EXPRESSION_STATEMENT :
      case ASTNode.FIELD_ACCESS :
      case ASTNode.FIELD_DECLARATION :
      case ASTNode.IF_STATEMENT :
      case ASTNode.INFIX_EXPRESSION :
      case ASTNode.INITIALIZER :
      case ASTNode.INSTANCEOF_EXPRESSION :
      case ASTNode.METHOD_DECLARATION :
      case ASTNode.NULL_LITERAL :
      case ASTNode.NUMBER_LITERAL :
      case ASTNode.PARENTHESIZED_EXPRESSION :
      case ASTNode.POSTFIX_EXPRESSION :
      case ASTNode.PREFIX_EXPRESSION :
      case ASTNode.QUALIFIED_NAME :
      case ASTNode.RETURN_STATEMENT :
      case ASTNode.SIMPLE_NAME :
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
      case ASTNode.STRING_LITERAL :
      case ASTNode.SUPER_FIELD_ACCESS :
      case ASTNode.SWITCH_CASE :
      case ASTNode.THIS_EXPRESSION :
      case ASTNode.THROW_STATEMENT :
      case ASTNode.TYPE_LITERAL :
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
      case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	 throw cause;

      case ASTNode.DO_STATEMENT :
	 visitThrow((DoStatement) node,cause);
	 break;
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 break;
      case ASTNode.FOR_STATEMENT :
	 visitThrow((ForStatement) node,cause);
	 break;
      case ASTNode.METHOD_INVOCATION :
	 break;
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
	 break;
      case ASTNode.SUPER_METHOD_INVOCATION :
	 break;
      case ASTNode.SWITCH_STATEMENT :
	 visitThrow((SwitchStatement) node,cause);
	 break;
      case ASTNode.SYNCHRONIZED_STATEMENT :
	 break;
      case ASTNode.TRY_STATEMENT :
	 visitThrow((TryStatement) node,cause);
	 break;
      case ASTNode.WHILE_STATEMENT :
	 visitThrow((WhileStatement) node,cause);
	 break;

      default :
	 AcornLog.logE("Unknown AST node " + current_node);
	 break;

    }
}




/********************************************************************************/
/*                                                                              */
/*      Top level methods                                                       */
/*                                                                              */
/********************************************************************************/

private void visit(MethodDeclaration md,ASTNode after)
{
   if (after == null) {
      List<CashewValue> argvals = getCallArgs();
      List<?> args = md.parameters();
      int off = 0;
      int idx = 0;
      JcompSymbol vsym = JcompAst.getDefinition(md.getName());
      if (!vsym.isStatic()) {
         off = 1;
         lookup_context.define("this",argvals.get(0));
       } 
      // need to handle nested this values as well
      int nparm = args.size();
      for (Object o : args) {
         SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
         JcompSymbol psym = JcompAst.getDefinition(svd.getName());
         if (idx == nparm-1 && md.isVarargs()) {
            // handle var args
          }
         else {
            CashewValue pv = lookup_context.findReference(psym);
            pv.setValueAt(execution_clock,argvals.get(idx+off));
          }
         ++idx;
       }
      next_node = md.getBody();
    }
   else {
      throw new CuminRunError(CuminRunError.Reason.RETURN);
    }
}




/********************************************************************************/
/*										*/
/*	Constant Handling							*/
/*										*/
/********************************************************************************/

private void visit(BooleanLiteral v)
{
   execution_stack.push(CashewValue.booleanValue(v.booleanValue()));
}


private void visit(CharacterLiteral v)
{
   JcompType ctype = JcompAst.getExprType(v);
   execution_stack.push(CashewValue.characterValue(ctype,v.charValue()));
}


private void visit(NullLiteral v)
{
   execution_stack.push(CashewValue.nullValue());
}



private void visit(NumberLiteral v)
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
}



private void visit(StringLiteral v)
{
   execution_stack.push(CashewValue.stringValue(v.getLiteralValue()));
}



private void visit(TypeLiteral v)
{
   JcompType acttyp = JcompAst.getJavaType(v.getType());
   execution_stack.push(CashewValue.classValue(acttyp));
}



/********************************************************************************/
/*										*/
/*	Expression handling							*/
/*										*/
/********************************************************************************/

private void visit(ArrayAccess v,ASTNode after)
{
   if (after == null) next_node = v.getArray();
   else if (after == v.getArray()) next_node = v.getIndex();
   else {
      CashewValue cv = execution_stack.pop();
      CashewValue av = execution_stack.pop();
      int idx = cv.getNumber(execution_clock).intValue();
      CashewValue rv = av.getIndexValue(execution_clock,idx);
      execution_stack.push(rv);
    }
}



private void visit(Assignment v,ASTNode after)
{
   if (after == null) next_node = v.getLeftHandSide();
   else if (after == v.getLeftHandSide()) next_node = v.getRightHandSide();
   else {
      CashewValue v2 = execution_stack.pop();
      CashewValue v1 = execution_stack.pop();
      CuminOperator op = op_map.get(v.getOperator());
      JcompType tgt = JcompAst.getExprType(v.getLeftHandSide());
      CashewValue v0 = CuminEvaluator.evaluateAssign(execution_clock,op,v1,v2,tgt);
      execution_stack.push(v0);
    }
}


private void visit(CastExpression v,ASTNode after)
{
   if (after == null) next_node = v.getExpression();
   else {
      JcompType tgt = JcompAst.getJavaType(v.getType());
      CashewValue cv = execution_stack.pop();
      cv = CuminEvaluator.castValue(execution_clock,cv,tgt);
      execution_stack.push(cv);
    }
}


private void visit(ConditionalExpression v,ASTNode after)
{
   if (after == null) next_node = v.getExpression();
   else if (after == v.getExpression()) {
      CashewValue cv = execution_stack.pop();
      if (cv.getBoolean(execution_clock)) {
	 next_node = v.getThenExpression();
       }
      else {
	 next_node = v.getElseExpression();
       }
    }
}


private void visit(FieldAccess v,ASTNode after)
{
   if (after == null) next_node = v.getExpression();
   else {
      CashewValue obj = execution_stack.pop();
      JcompSymbol sym = JcompAst.getReference(v.getName());
      CashewValue rslt = obj.getFieldValue(execution_clock,sym.getFullName());
      execution_stack.push(rslt);
    }
}


private void visit(InfixExpression v,ASTNode after)
{
   if (after == null) next_node = v.getLeftOperand();
   else if (after == v.getLeftOperand()) {
      if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
	 CashewValue v1 = execution_stack.pop();
	 if (v1.getBoolean(execution_clock)) {
	    next_node = v.getRightOperand();
	  }
	 else {
	    execution_stack.push(v1);
	  }
       }
      else if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
	 CashewValue v1 = execution_stack.pop();
	 if (v1.getBoolean(execution_clock)) {
	    execution_stack.push(v1);
	  }
	 else {
	    next_node = v.getRightOperand();
	  }
       }
      else next_node = v.getRightOperand();
    }
   else {
      if (v.getOperator() != InfixExpression.Operator.CONDITIONAL_AND &&
	    v.getOperator() != InfixExpression.Operator.CONDITIONAL_OR) {
	 CashewValue v2 = execution_stack.pop();
	 CashewValue v1 = execution_stack.pop();
	 CuminOperator op = op_map.get(v.getOperator());
	 CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1,v2);
	 execution_stack.push(v0);
       }
    }
}



private void visit(InstanceofExpression v,ASTNode after)
{
   if (after == null) next_node = v.getLeftOperand();
   else {
      JcompType rt = JcompAst.getJavaType(v.getRightOperand());
      CashewValue nv = CuminEvaluator.castValue(execution_clock,execution_stack.pop(),rt);
      execution_stack.push(nv);
    }
}


private void visit(MethodInvocation v,ASTNode after)
{
   if (after == null && v.getExpression() != null) {
      next_node = v.getExpression();
      return;
    }
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after)+1;      // will be 0 for v.getExpression()
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return;
    }
   
   // need to handle varargs
   
   List<CashewValue> argv = new ArrayList<CashewValue>();
   JcompSymbol js = JcompAst.getReference(v.getName());
   CallType cty = CallType.VIRTUAL;
   if (!js.isStatic()) {
      CashewValue thisv = lookup_context.findReference("this");
      thisv = thisv.getActualValue(execution_clock);
      argv.add(thisv);
      cty = CallType.STATIC;
    }
   for (int i = 0; i < args.size(); ++i) argv.add(null);
   int off = argv.size();
   for (int i = 0; i < args.size(); ++i) {
      CashewValue cv = execution_stack.pop().getActualValue(execution_clock);
      argv.set(args.size()-i-1+off,cv);
    }
   CuminRunner crun = handleCall(execution_clock,js,argv,cty);
   throw new CuminRunError(crun);
}



private void visit(SimpleName v)
{
   JcompSymbol js = JcompAst.getReference(v);
   if (js == null) {
      // throw error of some sort
    }
   if (js.isFieldSymbol()) {
      // handle this.field reference
    }
   // this should look things up on the stack
   CashewValue cv = lookup_context.findReference(js);
   execution_stack.push(cv);
}


private void visit(QualifiedName v,ASTNode after)
{
   if (after == null) next_node = v.getName();
   // could be field access -- need to handle
}



private void visit(ParenthesizedExpression v,ASTNode after)
{
   if (after == null) next_node = v.getExpression();
}



private void visit(PostfixExpression v,ASTNode after)
{
   if (after == null) next_node = v.getOperand();
   else {
      CashewValue v1 = execution_stack.pop();
      CuminOperator op = op_map.get(v.getOperator());
      CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1);
      execution_stack.push(v0);
    }
}


private void visit(PrefixExpression v,ASTNode after)
{
   if (after == null) next_node = v.getOperand();
   else {
      CashewValue v1 = execution_stack.pop();
      CuminOperator op = op_map.get(v.getOperator());
      CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1);
      execution_stack.push(v0);
    }
}



private void visit(ThisExpression v)
{
   String name = "this";
   if (v.getQualifier() != null) {
      name = v.getQualifier().getFullyQualifiedName() + "." + name;
    }
   CashewValue cv = lookup_context.findReference(name);
   execution_stack.push(cv);
}


private void visit(VariableDeclarationExpression v,ASTNode after)
{
   List<?> frags = v.fragments();
   int idx = 0;
   if (after != null) idx = frags.indexOf(after)+1;
   if (idx < frags.size()) next_node = (ASTNode) frags.get(idx);
}


private void visit(VariableDeclarationStatement v,ASTNode after)
{
   List<?> frags = v.fragments();
   int idx = 0;
   if (after != null) idx = frags.indexOf(after)+1;
   if (idx < frags.size()) next_node = (ASTNode) frags.get(idx);
}


/********************************************************************************/
/*										*/
/*	Statement methods							*/
/*										*/
/********************************************************************************/

private void visit(AssertStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else {
      if (!execution_stack.pop().getBoolean(execution_clock)) {
	 // throw assertion exception
       }
    }
}


private void visit(Block s,ASTNode after)
{
   int idx = 0;
   List<?> stmts = s.statements();
   if (after != null) {
      idx = stmts.indexOf(after)+1;
    }
   if (idx < stmts.size()) {
      next_node = (ASTNode) stmts.get(idx);
    }
}


private void visit(BreakStatement s)
{
   throw new CuminRunError(CuminRunError.Reason.BREAK,s.getLabel().getIdentifier());
}


private void visit(ContinueStatement s)
{
   throw new CuminRunError(CuminRunError.Reason.CONTINUE,s.getLabel().getIdentifier());
}




private void visit(DoStatement s,ASTNode after)
{
   if (after == null) next_node = s.getBody();
   else if (after == s.getBody()) next_node = s.getExpression();
   else {
      if (execution_stack.pop().getBoolean(execution_clock)) next_node = s.getBody();
    }
}


private void visitThrow(DoStatement s,CuminRunError r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) break;
	 else throw r;
      case CONTINUE :
	 if (checkLabel(s,lbl)) next_node = s.getExpression();
	 else throw r;
	 break;
      default :
	 throw r;
    }
}


private void visit(ExpressionStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else {
      execution_stack.pop();
    }
}



private void visit(ForStatement s,ASTNode after)
{
   StructuralPropertyDescriptor spd = null;
   if (after != null) spd = after.getLocationInParent();

   if (after != null && after == s.getExpression()) {
      if (execution_stack.pop().getBoolean(execution_clock)) {
	 next_node = s.getBody();
       }
      else return;
    }

   if (after == null || spd == ForStatement.INITIALIZERS_PROPERTY) {
      int idx = 0;
      List<?> inits = s.initializers();
      if (after != null) {
	 idx = inits.indexOf(after)+1;
	 if (idx == 0) idx = inits.size();
       }
      if (idx < inits.size()) {
	 next_node = (ASTNode) inits.get(idx);
	 return;
       }
    }
   if (after == s.getBody() || spd == ForStatement.UPDATERS_PROPERTY) {
      List<?> updts = s.updaters();
      int idx = 0;
      if (after != s.getBody()) idx = updts.indexOf(after)+1;
      if (idx < updts.size()) {
	 next_node = (ASTNode) updts.get(idx);
	 return;
       }
    }
   if (s.getExpression() == null) next_node = s.getBody();
   else next_node = s.getExpression();
}



private void visitThrow(ForStatement s,CuminRunError r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) break;
	 else throw r;
      case CONTINUE :
	 if (checkLabel(s,lbl)) {
	    if (s.getExpression() != null) next_node = s.getExpression();
	    else next_node = s.getBody();
	  }
	 else throw r;
	 break;
      default :
	 throw r;
    }
}



private void visit(IfStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else if (after == s.getExpression()) {
      if (execution_stack.pop().getBoolean(execution_clock)) {
	 next_node = s.getThenStatement();
       }
      else if (s.getElseStatement() != null) {
	 next_node = s.getElseStatement();
       }
    }
}

private void visit(LabeledStatement s,ASTNode after)
{
   if (after == null) next_node = s.getBody();
}


private void visit(ReturnStatement s,ASTNode after)
{
   if (after == null && s.getExpression() != null) {
      next_node = s.getExpression();
    }
   else {
      CashewValue rval = null;
      if (s.getExpression() != null) {
         rval = execution_stack.pop();
         rval = rval.getActualValue(execution_clock);
       }
      throw new CuminRunError(CuminRunError.Reason.RETURN,rval);
    }
}



private void visit(SwitchStatement s,ASTNode after)
{
   if (after == null) {
      next_node = s.getExpression();
      return;
    }

   List<?> stmts = s.statements();

   if (after != null && after instanceof SwitchCase) {
      CashewValue cv = execution_stack.pop();
      CashewValue sv = execution_stack.pop();
      // compare cv and sv correctly here
      int idx = stmts.indexOf(after) + 1;
      if (cv == sv) {
	 while (idx < stmts.size()) {
	    Statement stmt = (Statement) stmts.get(idx);
	    if (stmt instanceof SwitchCase) continue;
	    next_node = stmt;
	    break;
	  }
	 return;
       }
      execution_stack.push(sv);
    }
   if (after == s.getExpression() || after instanceof SwitchCase) {
      int idx = 0;
      if (after != s.getExpression()) idx = stmts.indexOf(after)+1;
      while (idx < stmts.size()) {
	 Statement stmt = (Statement) stmts.get(idx);
	 if (stmt instanceof SwitchCase) {
	    SwitchCase sc = (SwitchCase) stmt;
	    if (!sc.isDefault()) {
	       next_node = stmt;
	       return;
	     }
	  }
       }
      execution_stack.pop();
      idx = 0;
      while (idx < stmts.size()) {
	 Statement stmt = (Statement) stmts.get(idx);
	 if (stmt instanceof SwitchCase) {
	    SwitchCase sc = (SwitchCase) stmt;
	    if (sc.isDefault()) {
	       ++idx;
	       while (idx < stmts.size()) {
		  stmt = (Statement) stmts.get(idx);
		  if (stmt instanceof SwitchCase) continue;
		  next_node = stmt;
		  return;
		}
	     }
	  }
       }
      return;
    }

   int next = stmts.indexOf(after)+1;
   while (next < stmts.size()) {
      Statement ns = (Statement) stmts.get(next);
      if (ns instanceof SwitchCase) continue;
      next_node = ns;
      return;
    }
}



private void visitThrow(SwitchStatement s,CuminRunError r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) return;
	 else throw r;
      default :
	 throw r;
    }
}





private void visit(SwitchCase s,ASTNode after)
{
   if (after == null && s.getExpression() != null) next_node = s.getExpression();
}




private void visit(ThrowStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else {
      throw new CuminRunError(CuminRunError.Reason.EXCEPTION,execution_stack.pop());
    }
}



private void visit(TryStatement s,ASTNode after)
{
   if (after == null) next_node = s.getBody();
}


private void visitThrow(TryStatement s,CuminRunError r)
{
   if (r.getReason() != CuminRunError.Reason.EXCEPTION) throw r;
   JcompType etyp = r.getValue().getDataType(execution_clock);
   for (Object o : s.catchClauses()) {
      CatchClause cc = (CatchClause) o;
      SingleVariableDeclaration svd = cc.getException();
      JcompType ctype = JcompAst.getJavaType(svd.getType());
      if (etyp.isCompatibleWith(ctype)) {
	 execution_stack.push(r.getValue());
	 next_node = cc;
	 return;
       }
    }
   throw r;
}



private void visit(WhileStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else if (after == s.getExpression()) {
      if (execution_stack.pop().getBoolean(execution_clock)) next_node = s.getBody();
    }
   else if (after == s.getBody())
      next_node = s.getExpression();
}



private void visitThrow(WhileStatement s,CuminRunError r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) return;
	 else throw r;
      case CONTINUE :
	 if (checkLabel(s,lbl)) next_node = s.getExpression();
	 else throw r;
	 break;
      default :
	 throw r;
    }
}




/********************************************************************************/
/*										*/
/*	Declaration handling							*/
/*										*/
/********************************************************************************/

private void visit(ArrayInitializer n,ASTNode after)
{
   int idx = 0;
   List<?> exprs = n.expressions();
   if (after != null) idx = exprs.indexOf(after) + 1;
   if (idx < exprs.size()) {
      next_node = (ASTNode) exprs.get(idx);
    }
   else {
      int dim = exprs.size();
      JcompType typ = JcompAst.getExprType(n);
      CashewValue arrval = CashewValue.arrayValue(typ,dim);
      for (int i = dim-1; i >= 0; --i) {
	 CashewValue cv = execution_stack.pop();
	 arrval.setIndexValue(execution_clock,i,cv);
       }
      execution_stack.push(arrval);
    }
}



private void visit(FieldDeclaration n,ASTNode after)
{
   int idx = 0;
   List<?> frags = n.fragments();
   if (after != null) idx = frags.indexOf(after)+1;
   if (idx < frags.size()) next_node = (ASTNode) frags.get(idx);
}



private void visit(SingleVariableDeclaration n,ASTNode after)
{
   if (after == null && n.getInitializer() != null) next_node = n.getInitializer();
   else {
      ASTNode par = n.getParent();
      if (par instanceof MethodDeclaration) {
	 // handle formal parameters
       }
      else if (par instanceof CatchClause) {
	 // handle exception
       }
      else {
	 JcompSymbol js = JcompAst.getDefinition(n.getName());
	 handleInitialization(js,n.getInitializer());
       }
    }
}



private void visit(VariableDeclarationFragment n,ASTNode after)
{
   if (after == null && n.getInitializer() != null) next_node = n.getInitializer();
   else {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      handleInitialization(js,n.getInitializer());
    }
}



/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

private boolean checkLabel(Statement s,String lbl)
{
   if (lbl == null) return true;
   if (s.getParent() instanceof LabeledStatement) {
      LabeledStatement lbs = (LabeledStatement) s.getParent();
      if (lbs.getLabel().getIdentifier().equals(lbl)) return true;
    }
   return false;
}



private void handleInitialization(JcompSymbol js,ASTNode init)
{
   CashewValue cv = null;
   if (init != null) {
      cv = execution_stack.pop();
    }
   else {
      cv = CashewValue.createDefaultValue(js.getType());
    }
   CashewValue vv = lookup_context.findReference(js);
   CuminEvaluator.evaluateAssign(execution_clock,CuminOperator.ASG,vv,cv,js.getType());
}



}	// end of class CuminRunnerAst




/* end of CuminRunnerAst.java */

