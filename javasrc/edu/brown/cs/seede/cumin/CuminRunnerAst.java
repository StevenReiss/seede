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


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
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
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
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
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
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

enum TryState { BODY, CATCH, FINALLY };

private MethodDeclaration method_node;
private ASTNode 	current_node;
private ASTNode 	next_node;
private int		last_line;

private static Map<Object,CuminOperator> op_map;


private enum ForState { INIT, HASNEXT, NEXT, BODY };


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
   op_map.put(PrefixExpression.Operator.NOT,CuminOperator.NOT);
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

CuminRunnerAst(CuminProject cp,CashewContext gblctx,CashewClock cc,
      MethodDeclaration method,List<CashewValue> args)
{
   super(cp,gblctx,cc,args);

   method_node = method;
   current_node = method_node;
   last_line = 0;

   setupContext();
}



/********************************************************************************/
/*										*/
/*	Interpretation methods							*/
/*										*/
/********************************************************************************/

@Override public void reset(MethodDeclaration md)
{
   super.reset();

   if (md != null) method_node = md;
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
	 if (r.getReason() == CuminRunError.Reason.CALL) throw r;
	 if (r.getReason() == CuminRunError.Reason.RETURN) {
	    current_node = null;
	    throw r;
	  }
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
   JcompSymbol js = JcompAst.getDefinition(method_node);
   JcompSource src = JcompAst.getSource(method_node.getRoot());
   
   File file = null;
   if (src != null) file = new File(src.getFileName());

   CashewContext ctx = new CashewContext(js,file,global_context);
   LocalFinder lf = new LocalFinder();
   method_node.accept(lf);
   for (JcompSymbol lcl : lf.getLocalVars()) {
      JcompType lty = lcl.getType();
      CashewValue nv = CashewValue.createDefaultValue(lty);
      nv = CashewValue.createReference(nv,false);
      ctx.define(lcl,nv);
    }

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
	    nv = CashewValue.createReference(nv,false);
	    ctx.define(nm,nv);
	  }
       }
    }

   CompilationUnit cu = (CompilationUnit) method_node.getRoot();
   int lno = cu.getLineNumber(method_node.getStartPosition());
   if (lno < 0) lno = 0;
   CashewValue zv = CashewValue.numericValue(CashewConstants.INT_TYPE,lno);
   ctx.define(LINE_NAME,CashewValue.createReference(zv,false));
   setLookupContext(ctx);
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
      if (lno != last_line && lno > 0) {
	 last_line = lno;
	 CashewValue lvl = CashewValue.numericValue(CashewConstants.INT_TYPE,lno);
	 lookup_context.findReference(LINE_NAME).setValueAt(execution_clock,lvl);
       }
      if (Thread.currentThread().isInterrupted()) {
	 throw new CuminRunError(CuminRunError.Reason.STOPPED);
       }

      // System.err.println("EVAL: " + node);
      // check for breakpoint
      // check for step
      // check for timeout
    }
   JcompType jt = JcompAst.getExprType(node);
   if (jt != null && jt.isErrorType()) {
      throw new CuminRunError(CuminRunError.Reason.COMPILER_ERROR);
    }
   else if ((node.getFlags() & ASTNode.MALFORMED) != 0) {
      throw new CuminRunError(CuminRunError.Reason.COMPILER_ERROR);
    }

   AcornLog.logD("EXEC: " + node.getClass());

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
	 visit((ArrayCreation) node,afterchild);
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
	 visit((CatchClause) node,afterchild);
	 break;
      case ASTNode.CHARACTER_LITERAL :
	 visit((CharacterLiteral) node);
	 break;
      case ASTNode.CLASS_INSTANCE_CREATION :
	 visit((ClassInstanceCreation) node,afterchild);
	 break;
      case ASTNode.CONDITIONAL_EXPRESSION :
	 visit((ConditionalExpression) node,afterchild);
	 break;
      case ASTNode.CONSTRUCTOR_INVOCATION :
	 visit((ConstructorInvocation) node,afterchild);
	 break;
      case ASTNode.CONTINUE_STATEMENT :
	 visit((ContinueStatement) node);
	 break;
      case ASTNode.DO_STATEMENT :
	 visit((DoStatement) node,afterchild);
	 break;
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 visit((EnhancedForStatement) node,afterchild);
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
	 visit((SuperConstructorInvocation) node,afterchild);
	 break;
      case ASTNode.SUPER_FIELD_ACCESS :
	 visit((SuperFieldAccess) node);
	 break;
      case ASTNode.SUPER_METHOD_INVOCATION :
	 visit((SuperMethodInvocation) node,afterchild);
	 break;
      case ASTNode.SWITCH_CASE :
	 visit((SwitchCase) node,afterchild);
	 break;
      case ASTNode.SWITCH_STATEMENT :
	 visit((SwitchStatement) node,afterchild);
	 break;
      case ASTNode.SYNCHRONIZED_STATEMENT :
	 visit((SynchronizedStatement) node,afterchild);
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

   AcornLog.logD("EXECT: " + node.getClass() + " " + cause.getReason());
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
	 visitThrow((EnhancedForStatement) node,cause);
	 break;
      case ASTNode.FOR_STATEMENT :
	 visitThrow((ForStatement) node,cause);
	 break;
      case ASTNode.METHOD_INVOCATION :
	 visitThrow((MethodInvocation) node,cause);
	 break;
      case ASTNode.CLASS_INSTANCE_CREATION :
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
/*										*/
/*	Top level methods							*/
/*										*/
/********************************************************************************/

private void visit(MethodDeclaration md,ASTNode after)
{
   if (after == null) {
      List<CashewValue> argvals = getCallArgs();

      for (int i = 0; i < argvals.size(); ++i)
	 AcornLog.logD("ARG " + i + " = " + argvals.get(i).getDebugString(execution_clock));

      List<?> args = md.parameters();
      int off = 0;
      int idx = 0;
      JcompSymbol vsym = JcompAst.getDefinition(md.getName());
      if (!vsym.isStatic()) {
	 off = 1;
	 lookup_context.define(THIS_NAME,argvals.get(0));
       }
      //TODO:  need to handle nested this values as well
      int nparm = args.size();
      for (Object o : args) {
	 SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
	 JcompSymbol psym = JcompAst.getDefinition(svd.getName());
	 if (idx == nparm-1 && md.isVarargs()) {
	    // TODO: handle var args
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


private void visit(ArrayCreation v,ASTNode after)
{
   List<?> dims = v.dimensions();
   if (after == null || after != v.getInitializer()) {
      int idx = 0;
      if (after != null) idx = dims.indexOf(after) + 1;
      if (idx < dims.size()) {
	 next_node = (ASTNode) dims.get(idx);
	 return;
       }
    }
   if (v.getInitializer() != null) {
      next_node = v.getInitializer();
      return;
    }

   JcompType jty = JcompAst.getExprType(v);
   int jsize = 0;
   JcompType base = null;
   for (base = jty; base.isArrayType(); base = base.getBaseType()) {
      ++jsize;
    }
   CashewValue init = null;
   if (v.getInitializer() != null) init = execution_stack.pop();
   AcornLog.logD("CREAT ARRAY " + jsize + " " + dims.size() + " " + init);
   //TODO: what should we do here to actually create the array
   // Need to know the size of the array to create, what dummy dims to
   //	 use or ignore, etc.
   // What are the java semantics for this?
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
      CashewValue v0 = CuminEvaluator.evaluateAssign(this,op,v1,v2,tgt);
      execution_stack.push(v0);
    }
}


private void visit(CastExpression v,ASTNode after)
{
   if (after == null) next_node = v.getExpression();
   else {
      JcompType tgt = JcompAst.getJavaType(v.getType());
      CashewValue cv = execution_stack.pop();
      cv = CuminEvaluator.castValue(this,cv,tgt);
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
      JcompSymbol sym = JcompAst.getReference(v.getName());
      execution_stack.push(handleFieldAccess(sym));
    }
}


private void visit(SuperFieldAccess v)
{
   String var = "this";
   if (v.getQualifier() != null) {
      JcompType qtyp = JcompAst.getExprType(v.getQualifier());
      var = qtyp.getName() + ",this";
    }
   CashewValue obj = lookup_context.findReference(var);
   if (obj.isNull(execution_clock))
      CuminEvaluator.throwException(CashewConstants.NULL_PTR_EXC);

   JcompSymbol sym = JcompAst.getReference(v.getName());
   CashewValue rslt = obj.getFieldValue(execution_clock,sym.getFullName());
   execution_stack.push(rslt);
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
      CashewValue nv = CuminEvaluator.castValue(this,execution_stack.pop(),rt);
      execution_stack.push(nv);
    }
}


private void visit(SimpleName v)
{
   JcompSymbol js = JcompAst.getReference(v);
   CashewValue cv = null;
   if (js != null && js.isFieldSymbol()) {
      if (js.isStatic()) cv = handleStaticFieldAccess(js);
      else {
	 CashewValue tv = lookup_context.findReference(THIS_NAME);
	 execution_stack.push(tv);
	 cv = handleFieldAccess(js);
       }
    }
   else {
      cv = lookup_context.findReference(js);
    }

   execution_stack.push(cv);
}


private void visit(QualifiedName v,ASTNode after)
{
   JcompSymbol sym = JcompAst.getReference(v.getName());
   if (after == null) {
      if (sym != null && sym.isFieldSymbol() && !sym.isStatic()) {
	 next_node = v.getQualifier();
	 return;
       }
      else if (sym == null && v.getName().getIdentifier().equals("length")) {
	 next_node = v.getQualifier();
       }
      else next_node = v.getName();
    }
   if (after == v.getQualifier() && sym != null) {
      handleFieldAccess(sym);
    }
   else if (after == v.getQualifier() && sym == null &&
	       v.getName().getIdentifier().equals("length")) {
      CashewValue obj = execution_stack.pop().getActualValue(execution_clock);
      if (obj.isNull(execution_clock))
	 CuminEvaluator.throwException(CashewConstants.NULL_PTR_EXC);
      CashewValue rslt = obj.getFieldValue(execution_clock,"length");
      AcornLog.logD("COMPUTE LENGTH " + obj + " = " + rslt);
      execution_stack.push(rslt);
    }
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
   String name = THIS_NAME;
   if (v.getQualifier() != null) {
      name = v.getQualifier().getFullyQualifiedName() + "." + THIS_NAME;
    }
   CashewValue cv = lookup_context.findReference(name);
   execution_stack.push(cv);
}



/********************************************************************************/
/*										*/
/*	Call methods								*/
/*										*/
/********************************************************************************/

private void visit(ClassInstanceCreation v, ASTNode after)
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after) + 1;
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return;
    }

   JcompType rty = JcompAst.getJavaType(v.getType());
   JcompSymbol csym = JcompAst.getReference(v);

   JcompType ctyp = csym.getType();
   List<JcompType> atyps = ctyp.getComponents();

   CashewValue rval = handleNew (rty);
   List<CashewValue> argv = new ArrayList<CashewValue>();
   for (int i = 0; i < args.size(); ++i) {
      CashewValue cv = execution_stack.pop();
      cv = cv.getActualValue(execution_clock);
      JcompType argtyp = atyps.get(args.size()-1-i);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,argtyp);
      if (ncv == null) {
	 AcornLog.logD("Conversion problem " + cv + " " + argtyp + " " +
			  cv.getDataType(execution_clock) );
       }
      argv.add(ncv);
    }
   argv.add(rval);
   execution_stack.push(rval);
   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,csym,argv,CallType.SPECIAL);
   throw new CuminRunError(crun);
}



private void visit(ConstructorInvocation v,ASTNode after)
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after) + 1;
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return;
    }

   JcompSymbol csym = JcompAst.getReference(v);
   JcompType ctyp = csym.getType();
   List<JcompType> atyps = ctyp.getComponents();

   CashewValue rval = lookup_context.findReference(THIS_NAME);
   List<CashewValue> argv = new ArrayList<CashewValue>();
   for (int i = 0; i < args.size(); ++i) {
      CashewValue cv = execution_stack.pop();
      cv = cv.getActualValue(execution_clock);
      JcompType argtyp = atyps.get(args.size()-1-i);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,argtyp);
      if (ncv == null) {
	 AcornLog.logD("Conversion problem " + cv + " " + argtyp + " " +
			  cv.getDataType(execution_clock) );
       }
      argv.add(ncv);
    }
   argv.add(rval);
   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,csym,argv,CallType.SPECIAL);
   throw new CuminRunError(crun);
}



private void visit(MethodInvocation v,ASTNode after)
{
   JcompSymbol js = JcompAst.getReference(v.getName());

   if (after == null && v.getExpression() != null && !js.isStatic()) {
      next_node = v.getExpression();
      return;
    }
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after)+1;	// will be 0 for v.getExpression()
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return;
    }

   // need to handle varargs

   AcornLog.logD("INVOKE " + args.size() + " " + v);

   JcompType ctyp = js.getType();
   List<JcompType> atyps = ctyp.getComponents();

   List<CashewValue> argv = new ArrayList<CashewValue>();
   for (int i = 0; i < args.size(); ++i) {
      CashewValue cv = execution_stack.pop().getActualValue(execution_clock);
      JcompType argtyp = atyps.get(args.size()-1-i);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,argtyp);
      if (ncv == null) {
	 AcornLog.logD("Conversion problem " + cv + " " + argtyp + " " +
			  cv.getDataType(execution_clock) );
       }
      argv.add(ncv);
    }
   CallType cty = CallType.VIRTUAL;
   if (!js.isStatic()) {
      CashewValue thisv = null;
      if (v.getExpression() != null) thisv = execution_stack.pop();
      else thisv = lookup_context.findReference(THIS_NAME);
      thisv = thisv.getActualValue(execution_clock);
      argv.add(thisv);
    }
   else cty = CallType.STATIC;
   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,js,argv,cty);
   throw new CuminRunError(crun);
}


private void visitThrow(MethodInvocation n,CuminRunError cause)
{
   if (cause.getReason() == CuminRunError.Reason.RETURN) {
      CashewValue cv = cause.getValue();
      if (cv != null) execution_stack.push(cv);
    }
   else throw cause;
}



private void visit(SuperConstructorInvocation v,ASTNode after)
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after) + 1;
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return;
    }

   JcompSymbol csym = JcompAst.getReference(v);
   CashewValue rval = lookup_context.findReference(THIS_NAME);
   List<CashewValue> argv = new ArrayList<CashewValue>();
   for (int i = 0; i < args.size(); ++i) {
      CashewValue cv = execution_stack.pop();
      cv = cv.getActualValue(execution_clock);
      argv.add(cv);
    }
   argv.add(rval);
   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,csym,argv,CallType.SPECIAL);
   throw new CuminRunError(crun);
}




private void visit(SuperMethodInvocation v,ASTNode after)
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after)+1;	// will be 0 for v.getExpression()
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
      String nm = THIS_NAME;
      if (v.getQualifier() != null) {
	 JcompType jty = JcompAst.getExprType(v.getQualifier());
	 nm = jty.getName() + "." + THIS_NAME;
       }
      CashewValue thisv = lookup_context.findReference(nm);
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
   if (s.getLabel() != null)
      throw new CuminRunError(CuminRunError.Reason.BREAK,s.getLabel().getIdentifier());
   else
      throw new CuminRunError(CuminRunError.Reason.BREAK,"");
}


private void visit(ContinueStatement s)
{
   if (s.getLabel() != null)
      throw new CuminRunError(CuminRunError.Reason.CONTINUE,s.getLabel().getIdentifier());
   else
      throw new CuminRunError(CuminRunError.Reason.CONTINUE,"");
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
      JcompType typ = JcompAst.getJavaType(s.getExpression());
      if (typ != null && !typ.isVoidType())
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
      return;
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


private void visit(EnhancedForStatement s,ASTNode after)
{
   if (after == null) {
      next_node = s.getExpression();
      return;
    }

   if (after == s.getExpression()) {
      CashewValue cv = execution_stack.peek(0).getActualValue(execution_clock);
      JcompType jt = cv.getDataType(execution_clock);
      if (jt.isArrayType()) {
	 execution_stack.pushMarker(s,0);
       }
      else {
	 JcompTyper typer = JcompAst.getTyper(s);
	 JcompType rty = typer.findSystemType("java.util.Iterator");
	 List<JcompType> args = new ArrayList<>();
	 JcompType mty = JcompType.createMethodType(rty,args,false);
	 JcompSymbol js = jt.lookupMethod(typer,"iterator",mty);
	 if (js == null)
	    throw new CuminRunError(CuminRunError.Reason.COMPILER_ERROR,"Bad type for enhanced for");
	 List<CashewValue> argv = new ArrayList<>();
	 argv.add(cv);
	 execution_stack.pushMarker(s,ForState.INIT);
	 CuminRunner crun = handleCall(execution_clock,js,argv,CallType.VIRTUAL);
	 throw new CuminRunError(crun);
       }
    }

   enhancedCheck(s);
}



private void visitThrow(EnhancedForStatement s,CuminRunError r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case RETURN :
	 Object val = execution_stack.popMarker(s);
	 CashewValue rval = r.getValue();
	 if (val instanceof ForState) {
	    ForState state = (ForState) val;
	    switch (state) {
	       case INIT :
		  execution_stack.pop();
		  execution_stack.push(rval);
		  execution_stack.pushMarker(s,ForState.HASNEXT);
		  CuminRunner crun = forMethod(s,rval,ForState.HASNEXT);
		  throw new CuminRunError(crun);
	       case HASNEXT :
		  if (rval.getBoolean(execution_clock)) {
		     CashewValue iter = execution_stack.peek(0);
		     execution_stack.pushMarker(s,ForState.NEXT);
		     CuminRunner nrun = forMethod(s,iter,ForState.NEXT);
		     throw new CuminRunError(nrun);
		   }
		  else {
		     execution_stack.pop();
		     return;
		   }
	       case NEXT :
		  execution_stack.pushMarker(s,ForState.BODY);
		  enhancedBody(s,rval);
		  break;
	       case BODY :
		  execution_stack.pop();
		  throw r;
	     }
	  }
	 else {
	    execution_stack.pop();
	    throw r;
	  }
	 break;
      case BREAK :
	 execution_stack.popMarker(s);
	 execution_stack.pop();
	 if (!checkLabel(s,lbl)) throw r;
	 break;
      case CONTINUE :
	 if (checkLabel(s,lbl)) {
	    // do next
	  }
	 else {
	    execution_stack.popMarker(s);
	    execution_stack.pop();
	    throw r;
	  }
	 break;
      default :
	 throw r;
    }
}



private CuminRunner forMethod(EnhancedForStatement s,CashewValue cv,ForState state)
{
   JcompTyper typer = JcompAst.getTyper(s);
   List<JcompType> args = new ArrayList<>();

   String mnm = null;
   JcompType rty = null;
   switch (state) {
      case INIT :
	 mnm = "iterator";
	 rty = typer.findSystemType("java.util.Iterator");
	 break;
      case HASNEXT :
	 mnm = "hasNext";
	 rty = CashewConstants.BOOLEAN_TYPE;
	 break;
      case NEXT :
	 mnm = "next";
	 rty = typer.findSystemType("java.lang.Object");
	 break;
      default :
	 return null;
    }

   JcompType jt = cv.getDataType(execution_clock);
   JcompType mty = JcompType.createMethodType(rty,args,false);
   JcompSymbol js = jt.lookupMethod(typer,mnm,mty);
   if (js == null)
      throw new CuminRunError(CuminRunError.Reason.COMPILER_ERROR,"Bad type for enhanced for");
   List<CashewValue> argv = new ArrayList<>();
   argv.add(cv);
   CuminRunner crun = handleCall(execution_clock,js,argv,CallType.VIRTUAL);
   return crun;
}

private void enhancedBody(EnhancedForStatement s,CashewValue next)
{
   JcompSymbol js = JcompAst.getDefinition(s.getParameter().getName());
   CashewValue cr = lookup_context.findReference(js);
   CuminEvaluator.evaluateAssign(this,CuminOperator.ASG,cr,next,js.getType());
   next_node = s.getBody();
}


private void enhancedCheck(EnhancedForStatement s)
{
   Object val = execution_stack.popMarker(s);
   CashewValue iter = execution_stack.pop().getActualValue(execution_clock);
   if (val instanceof Integer) {
      int idx = (Integer) val;
      int len = iter.getDimension(execution_clock);
      if (idx >= len) return;
      CashewValue next = iter.getIndexValue(execution_clock,idx);
      ++idx;
      execution_stack.push(iter);
      execution_stack.pushMarker(s,idx);
      enhancedBody(s,next);
    }
   else {
      execution_stack.push(iter);
      execution_stack.pushMarker(s,ForState.HASNEXT);
      CuminRunner crun = forMethod(s,iter,ForState.HASNEXT);
      throw new CuminRunError(crun);
    }
}

private void visit(IfStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else if (after == s.getExpression()) {
      boolean fg = execution_stack.pop().getBoolean(execution_clock);
      if (fg) {
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



private void visit(SynchronizedStatement s,ASTNode after)
{
   if (after == null) {
      next_node = s.getExpression();
    }
   else if (after == s.getExpression()) {
      execution_stack.peek(0).getActualValue(execution_clock);
      // do sync start
      next_node = s.getBody();
    }
   else {
      execution_stack.pop().getActualValue(execution_clock);
      // do syn end
    }
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
   if (after == null) {
      execution_stack.pushMarker(s,TryState.BODY);
      next_node = s.getBody();
    }
   else if (after == s.getFinally()) {
      Object o = execution_stack.popMarker(s);
      if (o instanceof CuminRunError) {
	 CuminRunError r = (CuminRunError) o;
	 throw r;
       }
    }
   else {
      Object o = execution_stack.popMarker(s);
      assert o != null;
      if (o == TryState.BODY || o == TryState.CATCH) {
	 Block b = s.getFinally();
	 if (b != null) {
	    execution_stack.pushMarker(s,TryState.FINALLY);
	    next_node = b;
	  }
       }
    }
}


private void visitThrow(TryStatement s,CuminRunError r)
{
   Object sts = execution_stack.popUntil(s);
   assert sts != null;

   if (r.getReason() == CuminRunError.Reason.EXCEPTION && sts == TryState.BODY) {
      JcompType etyp = r.getValue().getDataType(execution_clock);
      for (Object o : s.catchClauses()) {
	 CatchClause cc = (CatchClause) o;
	 SingleVariableDeclaration svd = cc.getException();
	 JcompType ctype = JcompAst.getJavaType(svd.getType());
	 if (etyp.isCompatibleWith(ctype)) {
	    execution_stack.pushMarker(s,TryState.CATCH);
	    execution_stack.push(r.getValue());
	    next_node = cc;
	    return;
	  }
       }
    }

   if (sts instanceof TryState && sts != TryState.FINALLY) {
      Block b = s.getFinally();
      if (b != null) {
	 execution_stack.pushMarker(s,r);
	 next_node = b;
	 return;
       }
    }

   throw r;
}


private void visit(CatchClause s,ASTNode after)
{
   if (after == null) {
      CashewValue rv = execution_stack.pop();
      ASTNode decl = s.getException().getName();
      JcompSymbol csym = JcompAst.getDefinition(decl);
      CashewValue cv = lookup_context.findReference(csym);
      cv.setValueAt(execution_clock,rv);
      next_node = s.getBody();
    }
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



private void visit(VariableDeclarationExpression v,ASTNode after)
{
   List<?> frags = v.fragments();
   int idx = 0;
   if (after != null) idx = frags.indexOf(after)+1;
   if (idx < frags.size()) next_node = (ASTNode) frags.get(idx);
}



private void visit(VariableDeclarationFragment n,ASTNode after)
{
   if (after == null && n.getInitializer() != null) next_node = n.getInitializer();
   else {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      handleInitialization(js,n.getInitializer());
    }
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
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

private boolean checkLabel(Statement s,String lbl)
{
   if (lbl == null || lbl.length() == 0) return true;
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
   CuminEvaluator.evaluateAssign(this,CuminOperator.ASG,vv,cv,js.getType());
}


private CashewValue handleFieldAccess(JcompSymbol sym)
{
   CashewValue obj = execution_stack.pop().getActualValue(execution_clock);
   if (sym.isStatic()) return handleStaticFieldAccess(sym);
   if (obj.isNull(execution_clock))
      CuminEvaluator.throwException(CashewConstants.NULL_PTR_EXC);

   CashewValue rslt = obj.getFieldValue(execution_clock,sym.getFullName());
   return rslt;
}



private CashewValue handleStaticFieldAccess(JcompSymbol sym)
{
   CashewValue rslt = lookup_context.findReference(sym);

   return rslt;
}


}	// end of class CuminRunnerAst




/* end of CuminRunnerAst.java */

