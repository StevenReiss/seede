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

   JcompTyper typer = getTyper();
   Set<CashewValue> done = new HashSet<CashewValue>();
   for (CashewValue cv : call_args) {
      cv.resetType(typer,done);
    }

   if (md != null) method_node = md;
   current_node = method_node;
   last_line = 0;
   setupContext();
}


@Override protected CuminRunStatus interpretRun(CuminRunStatus err) throws CuminRunException
{
   ASTNode afternode = null;
   CuminRunStatus passerror = err;

   for ( ; ; ) {
      CuminRunStatus sts = null;
      try {
	 if (passerror != null) {
	    sts = evalThrow(current_node,passerror);
	    passerror = null;
	  }
	 else {
	    sts = evalNode(current_node,afternode);
	  }
       }
      catch (CuminRunException r) {
	 sts = r;
       }
      if (sts == null) {
	 if (next_node == null) {
	    afternode = current_node;
	    current_node = current_node.getParent();
	  }
	 else {
	    current_node = next_node;
	    afternode = null;
	  }
       }
      else {
	 if (current_node == method_node) return sts;
	 if (sts.getReason() == Reason.CALL) return sts;
	 if (sts.getReason() == Reason.RETURN) {
	    current_node = null;
	    return sts;
	  }
	 passerror = sts;
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

private CuminRunStatus evalNode(ASTNode node,ASTNode afterchild) throws CuminRunException
{
   if (node instanceof Statement && afterchild == null) {
      CompilationUnit cu = (CompilationUnit) node.getRoot();
      int lno = cu.getLineNumber(node.getStartPosition());
      if (lno != last_line && lno > 0) {
	 CuminRunStatus sts = checkTimeout();
	 if (sts != null) return sts;
	 last_line = lno;
	 CashewValue lvl = CashewValue.numericValue(CashewConstants.INT_TYPE,lno);
	 lookup_context.findReference(LINE_NAME).setValueAt(execution_clock,lvl);
       }
      // check for breakpoint
      // check for step
      // check for timeout
    }
   if (node instanceof Statement) {
      if (Thread.currentThread().isInterrupted()) {
	 return CuminRunStatus.Factory.createStopped();
       }
    }
   
   JcompType jt = JcompAst.getExprType(node);
   if (jt != null && jt.isErrorType()) {
      return CuminRunStatus.Factory.createCompilerError();
    }
   else if ((node.getFlags() & ASTNode.MALFORMED) != 0) {
      return CuminRunStatus.Factory.createCompilerError();
    }

   AcornLog.logT(node.getClass());

   next_node = null;
   CuminRunStatus sts = null;

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
	 sts = visit((ArrayAccess) node,afterchild);
	 break;
      case ASTNode.ARRAY_CREATION :
	 sts = visit((ArrayCreation) node,afterchild);
	 break;
      case ASTNode.ARRAY_INITIALIZER :
	 sts = visit((ArrayInitializer) node,afterchild);
	 break;
      case ASTNode.ASSERT_STATEMENT :
	 sts = visit((AssertStatement) node,afterchild);
	 break;
      case ASTNode.ASSIGNMENT :
	 sts = visit((Assignment) node,afterchild);
	 break;
      case ASTNode.BLOCK :
	 sts = visit((Block) node,afterchild);
	 break;
      case ASTNode.BOOLEAN_LITERAL :
	 sts = visit((BooleanLiteral) node);
	 break;
      case ASTNode.BREAK_STATEMENT :
	 sts = visit((BreakStatement) node);
	 break;
      case ASTNode.CAST_EXPRESSION :
	 sts = visit((CastExpression) node,afterchild);
	 break;
      case ASTNode.CATCH_CLAUSE :
	 sts = visit((CatchClause) node,afterchild);
	 break;
      case ASTNode.CHARACTER_LITERAL :
	 sts = visit((CharacterLiteral) node);
	 break;
      case ASTNode.CLASS_INSTANCE_CREATION :
	 sts = visit((ClassInstanceCreation) node,afterchild);
	 break;
      case ASTNode.CONDITIONAL_EXPRESSION :
	 sts = visit((ConditionalExpression) node,afterchild);
	 break;
      case ASTNode.CONSTRUCTOR_INVOCATION :
	 sts = visit((ConstructorInvocation) node,afterchild);
	 break;
      case ASTNode.CONTINUE_STATEMENT :
	 sts = visit((ContinueStatement) node);
	 break;
      case ASTNode.DO_STATEMENT :
	 sts = visit((DoStatement) node,afterchild);
	 break;
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 sts = visit((EnhancedForStatement) node,afterchild);
	 break;
      case ASTNode.EXPRESSION_STATEMENT :
	 sts = visit((ExpressionStatement) node,afterchild);
	 break;
      case ASTNode.FIELD_ACCESS :
	 sts = visit((FieldAccess) node,afterchild);
	 break;
      case ASTNode.FIELD_DECLARATION :
	 sts = visit((FieldDeclaration) node,afterchild);
	 break;
      case ASTNode.FOR_STATEMENT :
	 sts = visit((ForStatement) node,afterchild);
	 break;
      case ASTNode.IF_STATEMENT :
	 sts = visit((IfStatement) node,afterchild);
	 break;
      case ASTNode.INFIX_EXPRESSION :
	 sts = visit((InfixExpression) node,afterchild);
	 break;
      case ASTNode.INITIALIZER :
	 break;
      case ASTNode.INSTANCEOF_EXPRESSION :
	 sts = visit((InstanceofExpression) node,afterchild);
	 break;
      case ASTNode.LABELED_STATEMENT :
	 sts = visit((LabeledStatement) node,afterchild);
	 break;
      case ASTNode.METHOD_DECLARATION :
	 sts = visit((MethodDeclaration) node,afterchild);
	 break;
      case ASTNode.METHOD_INVOCATION :
	 sts = visit((MethodInvocation) node,afterchild);
	 break;
      case ASTNode.NULL_LITERAL :
	 sts = visit((NullLiteral) node);
	 break;
      case ASTNode.NUMBER_LITERAL :
	 sts = visit((NumberLiteral) node);
	 break;
      case ASTNode.PARENTHESIZED_EXPRESSION :
	 sts = visit((ParenthesizedExpression) node,afterchild);
	 break;
      case ASTNode.POSTFIX_EXPRESSION :
	 sts = visit((PostfixExpression) node,afterchild);
	 break;
      case ASTNode.PREFIX_EXPRESSION :
	 sts = visit((PrefixExpression) node,afterchild);
	 break;
      case ASTNode.QUALIFIED_NAME :
	 sts = visit((QualifiedName) node,afterchild);
	 break;
      case ASTNode.RETURN_STATEMENT :
	 sts = visit((ReturnStatement) node,afterchild);
	 break;
      case ASTNode.SIMPLE_NAME :
	 sts = visit((SimpleName) node);
	 break;
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
	 sts = visit((SingleVariableDeclaration) node,afterchild);
	 break;
      case ASTNode.STRING_LITERAL :
	 sts = visit((StringLiteral) node);
	 break;
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
	 sts = visit((SuperConstructorInvocation) node,afterchild);
	 break;
      case ASTNode.SUPER_FIELD_ACCESS :
	 sts = visit((SuperFieldAccess) node);
	 break;
      case ASTNode.SUPER_METHOD_INVOCATION :
	 sts = visit((SuperMethodInvocation) node,afterchild);
	 break;
      case ASTNode.SWITCH_CASE :
	 sts = visit((SwitchCase) node,afterchild);
	 break;
      case ASTNode.SWITCH_STATEMENT :
	 sts = visit((SwitchStatement) node,afterchild);
	 break;
      case ASTNode.SYNCHRONIZED_STATEMENT :
	 sts = visit((SynchronizedStatement) node,afterchild);
	 break;
      case ASTNode.THIS_EXPRESSION :
	 sts = visit((ThisExpression) node);
	 break;
      case ASTNode.THROW_STATEMENT :
	 sts = visit((ThrowStatement) node,afterchild);
	 break;
      case ASTNode.TRY_STATEMENT :
	 sts = visit((TryStatement) node,afterchild);
	 break;
      case ASTNode.TYPE_LITERAL :
	 sts = visit((TypeLiteral) node);
	 break;
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
	 sts = visit((VariableDeclarationExpression) node,afterchild);
	 break;
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
	 sts = visit((VariableDeclarationFragment) node,afterchild);
	 break;
      case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	 sts = visit((VariableDeclarationStatement) node,afterchild);
	 break;
      case ASTNode.WHILE_STATEMENT :
	 sts = visit((WhileStatement) node,afterchild);
	 break;
      default :
	 AcornLog.logE("Unknown AST node " + current_node);
	 sts = CuminRunStatus.Factory.createError("Unknown AST Node " + current_node);
	 break;
    }

   return sts;
}




private CuminRunStatus evalThrow(ASTNode node,CuminRunStatus cause) throws CuminRunException
{
   next_node = null;

   AcornLog.logD("EXECT: " + node.getClass() + " " + cause.getReason());
   // need to restore stack in each of these

   CuminRunStatus sts = null;
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
	 return cause;

      case ASTNode.DO_STATEMENT :
	 sts = visitThrow((DoStatement) node,cause);
	 break;
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 sts = visitThrow((EnhancedForStatement) node,cause);
	 break;
      case ASTNode.FOR_STATEMENT :
	 sts = visitThrow((ForStatement) node,cause);
	 break;
      case ASTNode.METHOD_INVOCATION :
	 sts = visitThrow((MethodInvocation) node,cause);
	 break;
      case ASTNode.CLASS_INSTANCE_CREATION :
	 break;
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
	 break;
      case ASTNode.SUPER_METHOD_INVOCATION :
	 break;
      case ASTNode.SWITCH_STATEMENT :
	 sts = visitThrow((SwitchStatement) node,cause);
	 break;
      case ASTNode.SYNCHRONIZED_STATEMENT :
	 break;
      case ASTNode.TRY_STATEMENT :
	 sts = visitThrow((TryStatement) node,cause);
	 break;
      case ASTNode.WHILE_STATEMENT :
	 sts = visitThrow((WhileStatement) node,cause);
	 break;

      default :
	 AcornLog.logE("Unknown AST node " + current_node);
	 break;
    }

   return sts;
}




/********************************************************************************/
/*										*/
/*	Top level methods							*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(MethodDeclaration md,ASTNode after)
{
   if (after == null) {
      List<CashewValue> argvals = getCallArgs();

      for (int i = 0; i < argvals.size(); ++i) {
	 AcornLog.logD("ARG " + i + " = " + argvals.get(i).getDebugString(execution_clock));
       }

      List<?> args = md.parameters();
      int off = 0;
      int idx = 0;
      JcompSymbol vsym = JcompAst.getDefinition(md.getName());
      if (!vsym.isStatic()) {
	 off = 1;
	 lookup_context.define(THIS_NAME,argvals.get(0));
	 if (md.isConstructor()) {
	    JcompType jtyp = JcompAst.getDefinition(md).getClassType();
	    if (jtyp.needsOuterClass()) {
	       lookup_context.define(OUTER_NAME,argvals.get(1));
	       off = 2;
	     }
	  }
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
      return CuminRunStatus.Factory.createReturn();
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Constant Handling							*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(BooleanLiteral v)
{
   execution_stack.push(CashewValue.booleanValue(v.booleanValue()));

   return null;
}


private CuminRunStatus visit(CharacterLiteral v)
{
   JcompType ctype = JcompAst.getExprType(v);
   execution_stack.push(CashewValue.characterValue(ctype,v.charValue()));

   return null;
}


private CuminRunStatus visit(NullLiteral v)
{
   execution_stack.push(CashewValue.nullValue());

   return null;
}



private CuminRunStatus visit(NumberLiteral v)
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

   return null;
}



private CuminRunStatus visit(StringLiteral v)
{
   execution_stack.push(CashewValue.stringValue(v.getLiteralValue()));

   return null;
}



private CuminRunStatus visit(TypeLiteral v)
{
   JcompType acttyp = JcompAst.getJavaType(v.getType());
   execution_stack.push(CashewValue.classValue(acttyp));

   return null;
}



/********************************************************************************/
/*										*/
/*	Expression handling							*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(ArrayAccess v,ASTNode after)
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

   return null;
}


private CuminRunStatus visit(ArrayCreation v,ASTNode after)
{
   List<?> dims = v.dimensions();
   if (after == null || after != v.getInitializer()) {
      int idx = 0;
      if (after != null) idx = dims.indexOf(after) + 1;
      if (idx < dims.size()) {
	 next_node = (ASTNode) dims.get(idx);
	 return null;
       }
    }
   if (v.getInitializer() != null) {
      next_node = v.getInitializer();
      return null;
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

   return null;
}



private CuminRunStatus visit(Assignment v,ASTNode after) throws CuminRunException
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

   return null;
}


private CuminRunStatus visit(CastExpression v,ASTNode after) throws CuminRunException
{
   if (after == null) next_node = v.getExpression();
   else {
      JcompType tgt = JcompAst.getJavaType(v.getType());
      CashewValue cv = execution_stack.pop();
      cv = CuminEvaluator.castValue(this,cv,tgt);
      execution_stack.push(cv);
    }

   return null;
}



private CuminRunStatus visit(ConditionalExpression v,ASTNode after)
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

   return null;
}



private CuminRunStatus visit(FieldAccess v,ASTNode after) throws CuminRunException
{
   if (after == null) next_node = v.getExpression();
   else {
      JcompSymbol sym = JcompAst.getReference(v.getName());
      execution_stack.push(handleFieldAccess(sym));
    }

   return null;
}


private CuminRunStatus visit(SuperFieldAccess v)
{
   String var = "this";
   if (v.getQualifier() != null) {
      JcompType qtyp = JcompAst.getExprType(v.getQualifier());
      var = qtyp.getName() + ",this";
    }
   CashewValue obj = lookup_context.findReference(var);
   if (obj.isNull(execution_clock))
      return CuminEvaluator.returnException(CashewConstants.NULL_PTR_EXC);

   JcompSymbol sym = JcompAst.getReference(v.getName());
   CashewValue rslt = obj.getFieldValue(execution_clock,sym.getFullName());
   execution_stack.push(rslt);

   return null;
}



private CuminRunStatus visit(InfixExpression v,ASTNode after) throws CuminRunException
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

   return null;
}



private CuminRunStatus visit(InstanceofExpression v,ASTNode after) throws CuminRunException
{
   if (after == null) next_node = v.getLeftOperand();
   else {
      JcompType rt = JcompAst.getJavaType(v.getRightOperand());
      CashewValue nv = CuminEvaluator.castValue(this,execution_stack.pop(),rt);
      execution_stack.push(nv);
    }

   return null;
}


private CuminRunStatus visit(SimpleName v)
{
   JcompSymbol js = JcompAst.getReference(v);
   CashewValue cv = null;
   if (js != null && js.isFieldSymbol()) {
      if (js.isStatic()) cv = handleStaticFieldAccess(js);
      else {
	 CashewValue tv = lookup_context.findReference(THIS_NAME);
	 cv = tv.getFieldValue(execution_clock,js.getFullName(),false);
	 while (cv == null) {
	    tv = tv.getFieldValue(execution_clock,OUTER_NAME,false);
	    if (tv == null) break;
	    cv = tv.getFieldValue(execution_clock,js.getFullName(),false);
	  }
	 if (cv == null) {
	    AcornLog.logE("Missing Field " + js.getFullName());
	  }
       }
    }
   else {
      cv = lookup_context.findReference(js);
    }

   execution_stack.push(cv);

   return null;
}


private CuminRunStatus visit(QualifiedName v,ASTNode after) throws CuminRunException
{
   JcompSymbol sym = JcompAst.getReference(v.getName());
   if (after == null) {
      if (sym != null && sym.isFieldSymbol() && !sym.isStatic()) {
	 next_node = v.getQualifier();
	 return null;
       }
      else if (sym == null && v.getName().getIdentifier().equals("length")) {
	 next_node = v.getQualifier();
       }
      else next_node = v.getName();
    }
   if (after == v.getQualifier() && sym != null) {
      CashewValue rslt = handleFieldAccess(sym);
      execution_stack.push(rslt);
    }
   else if (after == v.getQualifier() && sym == null &&
	       v.getName().getIdentifier().equals("length")) {
      CashewValue obj = execution_stack.pop().getActualValue(execution_clock);
      if (obj.isNull(execution_clock))
	 return CuminEvaluator.returnException(CashewConstants.NULL_PTR_EXC);
      CashewValue rslt = obj.getFieldValue(execution_clock,"length");
      AcornLog.logD("COMPUTE LENGTH " + obj + " = " + rslt);
      execution_stack.push(rslt);
    }

   return null;
}



private CuminRunStatus visit(ParenthesizedExpression v,ASTNode after)
{
   if (after == null) next_node = v.getExpression();

   return null;
}



private CuminRunStatus visit(PostfixExpression v,ASTNode after)
{
   if (after == null) next_node = v.getOperand();
   else {
      CashewValue v1 = execution_stack.pop();
      CuminOperator op = op_map.get(v.getOperator());
      CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1);
      execution_stack.push(v0);
    }

   return null;
}


private CuminRunStatus visit(PrefixExpression v,ASTNode after)
{
   if (after == null) next_node = v.getOperand();
   else {
      CashewValue v1 = execution_stack.pop();
      CuminOperator op = op_map.get(v.getOperator());
      CashewValue v0 = CuminEvaluator.evaluate(execution_clock,op,v1);
      execution_stack.push(v0);
    }

   return null;
}



private CuminRunStatus visit(ThisExpression v)
{
   JcompType base = null;
   if (v.getQualifier() != null) {
      base = JcompAst.getExprType(v.getQualifier());
    }
   CashewValue cv = handleThisAccess(base);
   execution_stack.push(cv);

   return null;
}



/********************************************************************************/
/*										*/
/*	Call methods								*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(ClassInstanceCreation v, ASTNode after) throws CuminRunException
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after) + 1;
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return null;
    }

   JcompType rty = JcompAst.getJavaType(v.getType());
   JcompSymbol csym = JcompAst.getReference(v);

   JcompType ctyp = csym.getType();
   List<JcompType> atyps = ctyp.getComponents();

   CashewValue rval = handleNew(rty);
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
   if (rty.needsOuterClass()) {
      CashewValue outerv = handleThisAccess(rty.getOuterType());
      argv.add(outerv);
    }
   argv.add(rval);
   execution_stack.push(rval);
   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,csym,argv,CallType.SPECIAL);

   return CuminRunStatus.Factory.createCall(crun);
}



private CuminRunStatus visit(ConstructorInvocation v,ASTNode after) throws CuminRunException
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after) + 1;
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return null;
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
   return CuminRunStatus.Factory.createCall(crun);
}



private CuminRunStatus visit(MethodInvocation v,ASTNode after) throws CuminRunException
{
   JcompSymbol js = JcompAst.getReference(v.getName());

   if (after == null && v.getExpression() != null && !js.isStatic()) {
      next_node = v.getExpression();
      return null;
    }
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after)+1;	// will be 0 for v.getExpression()
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return null;
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
   return CuminRunStatus.Factory.createCall(crun);
}


private CuminRunStatus visitThrow(MethodInvocation n,CuminRunStatus cause)
{
   if (cause.getReason() == Reason.RETURN) {
      CashewValue cv = cause.getValue();
      if (cv != null) execution_stack.push(cv);
    }
   else return cause;

   return null;
}



private CuminRunStatus visit(SuperConstructorInvocation v,ASTNode after) throws CuminRunException
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after) + 1;
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return null;
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
   return CuminRunStatus.Factory.createCall(crun);
}




private CuminRunStatus visit(SuperMethodInvocation v,ASTNode after) throws CuminRunException
{
   int idx = 0;
   List<?> args = v.arguments();
   if (after != null) {
      idx = args.indexOf(after)+1;	// will be 0 for v.getExpression()
    }
   if (idx < args.size()) {
      next_node = (ASTNode) args.get(idx);
      return null;
    }

   // need to handle varargs

   List<CashewValue> argv = new ArrayList<CashewValue>();
   JcompSymbol js = JcompAst.getReference(v.getName());
   CallType cty = CallType.VIRTUAL;
   if (!js.isStatic()) {
      JcompType base = null;
      if (v.getQualifier() != null) {
	 base = JcompAst.getExprType(v.getQualifier());
       }
      CashewValue thisv = handleThisAccess(base);
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
   return CuminRunStatus.Factory.createCall(crun);
}



/********************************************************************************/
/*										*/
/*	Statement methods							*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(AssertStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else {
      if (!execution_stack.pop().getBoolean(execution_clock)) {
	 // throw assertion exception
       }
    }
   return null;
}


private CuminRunStatus visit(Block s,ASTNode after)
{
   int idx = 0;
   List<?> stmts = s.statements();
   if (after != null) {
      idx = stmts.indexOf(after)+1;
    }
   if (idx < stmts.size()) {
      next_node = (ASTNode) stmts.get(idx);
    }
   return null;
}


private CuminRunStatus visit(BreakStatement s)
{
   if (s.getLabel() != null)
      return CuminRunStatus.Factory.createBreak(s.getLabel().getIdentifier());
   else
      return CuminRunStatus.Factory.createBreak("");
}


private CuminRunStatus visit(ContinueStatement s)
{
   if (s.getLabel() != null)
      return CuminRunStatus.Factory.createContinue(s.getLabel().getIdentifier());
   else
      return CuminRunStatus.Factory.createContinue("");
}




private CuminRunStatus visit(DoStatement s,ASTNode after)
{
   if (after == null) next_node = s.getBody();
   else if (after == s.getBody()) next_node = s.getExpression();
   else {
      if (execution_stack.pop().getBoolean(execution_clock)) next_node = s.getBody();
    }
   return null;
}


private CuminRunStatus visitThrow(DoStatement s,CuminRunStatus r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) break;
	 else return r;
      case CONTINUE :
	 if (checkLabel(s,lbl)) next_node = s.getExpression();
	 else return r;
	 break;
      default :
	 return r;
    }

   return null;
}


private CuminRunStatus visit(ExpressionStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else {
      JcompType typ = JcompAst.getExprType(s.getExpression());
      if (typ != null && !typ.isVoidType())
	 execution_stack.pop();
    }
   return null;
}



private CuminRunStatus visit(ForStatement s,ASTNode after)
{
   StructuralPropertyDescriptor spd = null;
   if (after != null) spd = after.getLocationInParent();

   if (after != null && after == s.getExpression()) {
      if (execution_stack.pop().getBoolean(execution_clock)) {
	 next_node = s.getBody();
       }
      return null;
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
	 return null;
       }
    }
   if (after == s.getBody() || spd == ForStatement.UPDATERS_PROPERTY) {
      List<?> updts = s.updaters();
      int idx = 0;
      if (after != s.getBody()) idx = updts.indexOf(after)+1;
      if (idx < updts.size()) {
	 next_node = (ASTNode) updts.get(idx);
	 return null;
       }
    }
   if (s.getExpression() == null) next_node = s.getBody();
   else next_node = s.getExpression();

   return null;
}



private CuminRunStatus visitThrow(ForStatement s,CuminRunStatus r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) break;
	 else return r;
      case CONTINUE :
	 if (checkLabel(s,lbl)) {
	    if (s.getExpression() != null) next_node = s.getExpression();
	    else next_node = s.getBody();
	  }
	 else return r;
	 break;
      default :
	 return r;
    }
   return null;
}


private CuminRunStatus visit(EnhancedForStatement s,ASTNode after) throws CuminRunException
{
   if (after == null) {
      next_node = s.getExpression();
      return null;
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
	    return CuminRunStatus.Factory.createCompilerError();
	 List<CashewValue> argv = new ArrayList<>();
	 argv.add(cv);
	 execution_stack.pushMarker(s,ForState.INIT);
	 CuminRunner crun = handleCall(execution_clock,js,argv,CallType.VIRTUAL);
	 return CuminRunStatus.Factory.createCall(crun);
       }
    }

   return enhancedCheck(s);
}



private CuminRunStatus visitThrow(EnhancedForStatement s,CuminRunStatus r)
	throws CuminRunException
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
		  return CuminRunStatus.Factory.createCall(crun);
	       case HASNEXT :
		  if (rval.getBoolean(execution_clock)) {
		     CashewValue iter = execution_stack.peek(0);
		     execution_stack.pushMarker(s,ForState.NEXT);
		     CuminRunner nrun = forMethod(s,iter,ForState.NEXT);
		     return CuminRunStatus.Factory.createCall(nrun);
		   }
		  else {
		     execution_stack.pop();
		     return null;
		   }
	       case NEXT :
		  execution_stack.pushMarker(s,ForState.BODY);
		  enhancedBody(s,rval);
		  break;
	       case BODY :
		  execution_stack.pop();
		  return r;
	     }
	  }
	 else {
	    execution_stack.pop();
	    return r;
	  }
	 break;
      case BREAK :
	 execution_stack.popMarker(s);
	 execution_stack.pop();
	 if (!checkLabel(s,lbl)) return r;
	 break;
      case CONTINUE :
	 if (checkLabel(s,lbl)) {
	    // do next
	  }
	 else {
	    execution_stack.popMarker(s);
	    execution_stack.pop();
	    return r;
	  }
	 break;
      default :
	 return r;
    }

   return null;
}



private CuminRunner forMethod(EnhancedForStatement s,CashewValue cv,ForState state) throws CuminRunException
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
   if (js == null) throw CuminRunStatus.Factory.createCompilerError();
   List<CashewValue> argv = new ArrayList<>();
   argv.add(cv);
   CuminRunner crun = handleCall(execution_clock,js,argv,CallType.VIRTUAL);
   return crun;
}



private void enhancedBody(EnhancedForStatement s,CashewValue next) throws CuminRunException
{
   JcompSymbol js = JcompAst.getDefinition(s.getParameter().getName());
   CashewValue cr = lookup_context.findReference(js);
   CuminEvaluator.evaluateAssign(this,CuminOperator.ASG,cr,next,js.getType());
   next_node = s.getBody();
}


private CuminRunStatus enhancedCheck(EnhancedForStatement s) throws CuminRunException
{
   Object val = execution_stack.popUntil(s);
   CashewValue iter = execution_stack.pop().getActualValue(execution_clock);
   if (val instanceof Integer) {
      int idx = (Integer) val;
      int len = iter.getDimension(execution_clock);
      if (idx >= len) return null;
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
      return CuminRunStatus.Factory.createCall(crun);
    }

   return null;
}



private CuminRunStatus visit(IfStatement s,ASTNode after)
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

   return null;
}



private CuminRunStatus visit(LabeledStatement s,ASTNode after)
{
   if (after == null) next_node = s.getBody();

   return null;
}


private CuminRunStatus visit(ReturnStatement s,ASTNode after)
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
      return CuminRunStatus.Factory.createReturn(rval);
    }

   return null;
}



private CuminRunStatus visit(SwitchStatement s,ASTNode after)
{
   if (after == null) {
      next_node = s.getExpression();
      return null;
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
	 return null;
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
	       return null;
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
		  return null;
		}
	     }
	  }
       }
      return null;
    }

   int next = stmts.indexOf(after)+1;
   while (next < stmts.size()) {
      Statement ns = (Statement) stmts.get(next);
      if (ns instanceof SwitchCase) continue;
      next_node = ns;
      return null;
    }

   return null;
}



private CuminRunStatus visitThrow(SwitchStatement s,CuminRunStatus r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) return null;
	 else return r;
      default :
	 return r;
    }
}





private CuminRunStatus visit(SwitchCase s,ASTNode after)
{
   if (after == null && s.getExpression() != null) next_node = s.getExpression();

   return null;
}



private CuminRunStatus visit(SynchronizedStatement s,ASTNode after)
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

   return null;
}


private CuminRunStatus visit(ThrowStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else {
      return CuminRunStatus.Factory.createException(execution_stack.pop());
    }

   return null;
}



private CuminRunStatus visit(TryStatement s,ASTNode after)
{
   if (after == null) {
      execution_stack.pushMarker(s,TryState.BODY);
      next_node = s.getBody();
    }
   else if (after == s.getFinally()) {
      Object o = execution_stack.popMarker(s);
      if (o instanceof CuminRunStatus) {
	 CuminRunStatus r = (CuminRunStatus) o;
	 return r;
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

   return null;
}


private CuminRunStatus visitThrow(TryStatement s,CuminRunStatus r)
{
   Object sts = execution_stack.popUntil(s);
   assert sts != null;

   if (r.getReason() == Reason.EXCEPTION && sts == TryState.BODY) {
      JcompType etyp = r.getValue().getDataType(execution_clock);
      for (Object o : s.catchClauses()) {
	 CatchClause cc = (CatchClause) o;
	 SingleVariableDeclaration svd = cc.getException();
	 JcompType ctype = JcompAst.getJavaType(svd.getType());
	 if (etyp.isCompatibleWith(ctype)) {
	    execution_stack.pushMarker(s,TryState.CATCH);
	    execution_stack.push(r.getValue());
	    next_node = cc;
	    return null;
	  }
       }
    }

   if (sts instanceof TryState && sts != TryState.FINALLY) {
      Block b = s.getFinally();
      if (b != null) {
	 execution_stack.pushMarker(s,r);
	 next_node = b;
	 return null;
       }
    }

   return r;
}


private CuminRunStatus visit(CatchClause s,ASTNode after)
{
   if (after == null) {
      CashewValue rv = execution_stack.pop();
      ASTNode decl = s.getException().getName();
      JcompSymbol csym = JcompAst.getDefinition(decl);
      CashewValue cv = lookup_context.findReference(csym);
      cv.setValueAt(execution_clock,rv);
      next_node = s.getBody();
    }

   return null;
}



private CuminRunStatus visit(WhileStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else if (after == s.getExpression()) {
      if (execution_stack.pop().getBoolean(execution_clock)) next_node = s.getBody();
    }
   else if (after == s.getBody())
      next_node = s.getExpression();

   return null;
}



private CuminRunStatus visitThrow(WhileStatement s,CuminRunStatus r)
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) return null;
	 else return r;
      case CONTINUE :
	 if (checkLabel(s,lbl)) next_node = s.getExpression();
	 else return r;
	 break;
      default :
	 return r;
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Declaration handling							*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(ArrayInitializer n,ASTNode after)
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

   return null;
}



private CuminRunStatus visit(FieldDeclaration n,ASTNode after)
{
   int idx = 0;
   List<?> frags = n.fragments();
   if (after != null) idx = frags.indexOf(after)+1;
   if (idx < frags.size()) next_node = (ASTNode) frags.get(idx);

   return null;
}



private CuminRunStatus visit(SingleVariableDeclaration n,ASTNode after) throws CuminRunException
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

   return null;
}



private CuminRunStatus visit(VariableDeclarationExpression v,ASTNode after)
{
   List<?> frags = v.fragments();
   int idx = 0;
   if (after != null) idx = frags.indexOf(after)+1;
   if (idx < frags.size()) next_node = (ASTNode) frags.get(idx);

   return null;
}



private CuminRunStatus visit(VariableDeclarationFragment n,ASTNode after)
	throws CuminRunException
{
   if (after == null && n.getInitializer() != null) next_node = n.getInitializer();
   else {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      handleInitialization(js,n.getInitializer());
    }

   return null;
}




private CuminRunStatus visit(VariableDeclarationStatement v,ASTNode after)
{
   List<?> frags = v.fragments();
   int idx = 0;
   if (after != null) idx = frags.indexOf(after)+1;
   if (idx < frags.size()) next_node = (ASTNode) frags.get(idx);

   return null;
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



private void handleInitialization(JcompSymbol js,ASTNode init) throws CuminRunException
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


private CashewValue handleFieldAccess(JcompSymbol sym) throws CuminRunException
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

private CashewValue handleThisAccess(JcompType base)
{
   CashewValue cv = lookup_context.findReference(THIS_NAME);
   if (cv == null) return null;
   if (base == null) return cv;
   JcompType thistyp = cv.getDataType(execution_clock);
   if (thistyp.isCompatibleWith(base)) return cv;

   CashewValue cv1 = lookup_context.findReference(OUTER_NAME);
   cv1 = handleThisAccess(base,cv1);
   if (cv1 != null) return cv1;

   return handleThisAccess(base,cv);
}


private CashewValue handleThisAccess(JcompType base,CashewValue cv)
{
   if (cv == null) return null;
   if (base == null) return cv;
   JcompType thistyp = cv.getDataType(execution_clock);
   if (thistyp.isCompatibleWith(base)) return cv;
   CashewValue cv1 = cv.getFieldValue(execution_clock,OUTER_NAME,false);
   return handleThisAccess(base,cv1);
}




}	// end of class CuminRunnerAst




/* end of CuminRunnerAst.java */


















































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































