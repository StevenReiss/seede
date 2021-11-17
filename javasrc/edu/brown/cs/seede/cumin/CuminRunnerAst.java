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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
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
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
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
import org.eclipse.jdt.core.dom.SuperMethodReference;
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
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompSymbolKind;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewSynchronizationModel;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueFunctionRef;

class CuminRunnerAst extends CuminRunner
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

enum TryState { BODY, CATCH, FINALLY };

private ASTNode 	method_node;
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

CuminRunnerAst(CashewValueSession sess,CuminProject cp,CashewContext gblctx,CashewClock cc,
      ASTNode method,List<CashewValue> args,boolean top,int depth)
{
   super(sess,cp,gblctx,cc,args,depth);

   method_node = method;
   current_node = method_node;
   last_line = 0;

   setupContext(top);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getCallingClass()
{
   if (method_node != null) {
      for (ASTNode p = method_node.getParent(); p != null; p = p.getParent()) {
	 if (p instanceof AbstractTypeDeclaration) {
	    JcompType jt = JcompAst.getJavaType(p);
	    if (jt != null) return jt.getName();
	  }
       }
    }
   return null;
}



/********************************************************************************/
/*										*/
/*	Interpretation methods							*/
/*										*/
/********************************************************************************/

@Override public void reset(MethodDeclaration md)
{
   super.reset();

   JcompTyper typer1 = JcompAst.getTyper(md);
   JcompTyper typer = type_converter;
   if (typer1 != typer) {
      setTyper(typer1);
      typer = typer1;
    }
   Set<CashewValue> done = new HashSet<CashewValue>();
   for (CashewValue cv : call_args) {
      if (cv != null) cv.resetType(typer,done);
    }

   if (md != null) method_node = md;
   current_node = method_node;
   last_line = 0;
   setupContext(true);
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
      catch (CashewException e) {
	 AcornLog.logI("Exception evaluating " + current_node + " in:\n " + method_node + " using: " + call_args);
	 sts = CuminRunStatus.Factory.createCompilerError();
       }
      catch (Throwable t) {
	 String msg = "Problem evaluating " + current_node + " in:\n " + method_node;
	 Error t1 = new Error(msg,t);
	 throw t1;
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

private void setupContext(boolean top)
{
   JcompSymbol js = JcompAst.getDefinition(method_node);
   JcompSource src = JcompAst.getSource(method_node.getRoot());
   JcompTyper typer = type_converter;

   File file = null;
   if (src != null) file = new File(src.getFileName());

   CashewContext ctx = new CashewContext(js,file,global_context);
   LocalFinder lf = new LocalFinder();
   method_node.accept(lf);
   for (JcompSymbol lcl : lf.getLocalVars()) {
      JcompType lty = lcl.getType();
      CashewValue nv = CashewValue.createDefaultValue(typer,lty);
      boolean caninit = false;
      if (top) caninit = lf.isParameter(lcl);
      nv = CashewValue.createReference(nv,caninit);
      ctx.define(lcl,nv);
    }

   if (!js.isStatic()) {
      ctx.define("this",CashewValue.nullValue(typer));
    }

   JcompType cty = js.getClassType();
   for (ASTNode n = method_node; n != null; n = n.getParent()) {
      if (n instanceof TypeDeclaration) {
	 TypeDeclaration td = (TypeDeclaration) n;
	 JcompSymbol sty = JcompAst.getDefinition(td.getName());
	 JcompType ty = JcompAst.getJavaType(td);
	 if (ty != cty && !sty.isStatic()) {
	    String nm = sty.getFullName() + ".this";
	    CashewValue nv = CashewValue.nullValue(typer);
	    nv = CashewValue.createReference(nv,true);
	    ctx.define(nm,nv);
	  }
       }
    }

   CompilationUnit cu = (CompilationUnit) method_node.getRoot();
   int lno = cu.getLineNumber(method_node.getStartPosition());
   if (lno < 0) lno = 0;
   CashewValue zv = CashewValue.numericValue(typer,typer.INT_TYPE,lno);
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
   private Set<JcompSymbol> param_vars;

   LocalFinder() {
      local_vars = new HashSet<JcompSymbol>();
      param_vars = new HashSet<JcompSymbol>();
    }

   Set<JcompSymbol> getLocalVars()		{ return local_vars; }
   boolean isParameter(JcompSymbol js)		{ return param_vars.contains(js); }

   @Override public void endVisit(SingleVariableDeclaration n) {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      if (js != null) {
	 local_vars.add(js);
	 if (n.getParent() instanceof MethodDeclaration) {
	    param_vars.add(js);
	  }
       }
    }

   @Override public void endVisit(VariableDeclarationFragment n) {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      local_vars.add(js);
    }

}	// end of inner class LocalFinder




/********************************************************************************/
/*										*/
/*	Syncrhonization methods 						*/
/*										*/
/********************************************************************************/

@Override protected CashewValue synchronizeOn()
{
   JcompSymbol js = JcompAst.getDefinition(method_node);
   if (js == null) return null;
   if ((js.getModifiers() & Modifier.SYNCHRONIZED) == 0) return null;
   if (!js.isStatic()) {
      return lookup_context.findReference(THIS_NAME);
    }
   else {
      return CashewValue.classValue(type_converter,js.getClassType());
    }
}


/********************************************************************************/
/*										*/
/*	Evauation dispatch methods						*/
/*										*/
/********************************************************************************/

private CuminRunStatus evalNode(ASTNode node,ASTNode afterchild)
	throws CuminRunException, CashewException
{
   JcompTyper typer = type_converter;

   if (node instanceof Statement) {
      boolean report = true;
      if (afterchild != null) {
	 switch (node.getNodeType()) {
	    case ASTNode.FOR_STATEMENT :
	    case ASTNode.ENHANCED_FOR_STATEMENT :
	    case ASTNode.WHILE_STATEMENT :
	    case ASTNode.DO_STATEMENT :
	       report = true;
	       break;
	    default :
	       report = false;
	       break;
	  }
       }
      if (report) {
	 CompilationUnit cu = (CompilationUnit) node.getRoot();
	 int lno = cu.getLineNumber(node.getStartPosition());
	 if (lno != last_line && lno > 0) {
	    last_line = lno;
	    CashewValue lvl = CashewValue.numericValue(typer,typer.INT_TYPE,lno);
	    lookup_context.findReference(LINE_NAME).setValueAt(runner_session,execution_clock,lvl);
	  }
         else execution_clock.tick();
       }
      CuminRunStatus sts = checkTimeout();
      if (sts != null) return sts;
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
      AcornLog.logD("COMPILER SEMANTIC ERROR " + node);
      return CuminRunStatus.Factory.createCompilerError();
    }
   else if ((node.getFlags() & ASTNode.RECOVERED) != 0) {
      return CuminRunStatus.Factory.createCompilerError();
    }

   if (AcornLog.isTracing()) {
      if (afterchild == null)
	 AcornLog.logT(node.getClass());
      else
	 AcornLog.logT("*" + node.getClass().getName());
    }

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
      case ASTNode.NAME_QUALIFIED_TYPE :
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
      case ASTNode.CREATION_REFERENCE :
	 sts = visit((CreationReference) node,afterchild);
	 break;
      case ASTNode.DO_STATEMENT :
	 sts = visit((DoStatement) node,afterchild);
	 break;
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 sts = visit((EnhancedForStatement) node,afterchild);
	 break;
      case ASTNode.EXPRESSION_METHOD_REFERENCE :
	 sts = visit((ExpressionMethodReference) node,afterchild);
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
      case ASTNode.LAMBDA_EXPRESSION :
	 sts = visit((LambdaExpression) node,afterchild);
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
      case ASTNode.SUPER_METHOD_REFERENCE :
	 sts = visit((SuperMethodReference) node,afterchild);
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
      case ASTNode.TYPE_DECLARATION_STATEMENT :
         sts = visit((TypeDeclarationStatement) node,afterchild);
         break;
      case ASTNode.TYPE_LITERAL :
	 sts = visit((TypeLiteral) node);
	 break;
      case ASTNode.TYPE_METHOD_REFERENCE :
	 sts = visit((TypeMethodReference) node,afterchild);
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




private CuminRunStatus evalThrow(ASTNode node,CuminRunStatus cause)
	throws CuminRunException, CashewException
{
   next_node = null;

   if (AcornLog.isTracing()) AcornLog.logT("EXECT: " + node.getClass() + " " + cause.getReason());
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
      case ASTNode.LAMBDA_EXPRESSION :
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

      case ASTNode.CLASS_INSTANCE_CREATION :
         sts = visitThrow((ClassInstanceCreation) node,cause);
         break;
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
         sts = visitThrow((SuperConstructorInvocation) node,cause);
         break;
      case ASTNode.SYNCHRONIZED_STATEMENT :
         sts = visitThrow((SynchronizedStatement) node,cause);
         break;
         
      case ASTNode.DO_STATEMENT :
	 sts = visitThrow((DoStatement) node,cause);
	 break;
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 sts = visitThrow((EnhancedForStatement) node,cause);
	 break;
      case ASTNode.FOR_STATEMENT :
	 sts = visitThrow((ForStatement) node,cause);
	 break;
      case ASTNode.LABELED_STATEMENT :
         sts = visitThrow((LabeledStatement) node,cause);
         break;
      case ASTNode.METHOD_INVOCATION :
	 sts = visitThrow((MethodInvocation) node,cause);
	 break;
      case ASTNode.SUPER_METHOD_INVOCATION :
	 sts = visitThrow((SuperMethodInvocation) node,cause);
	 break;
      case ASTNode.SWITCH_STATEMENT :
	 sts = visitThrow((SwitchStatement) node,cause);
	 break;
      case ASTNode.TRY_STATEMENT :
	 sts = visitThrow((TryStatement) node,cause);
	 break;
      case ASTNode.WHILE_STATEMENT :
	 sts = visitThrow((WhileStatement) node,cause);
	 break;

      case ASTNode.EXPRESSION_METHOD_REFERENCE :
      case ASTNode.CREATION_REFERENCE :
      case ASTNode.SUPER_METHOD_REFERENCE :
      case ASTNode.TYPE_METHOD_REFERENCE :
	 sts = visitThrow((MethodReference) node,cause);
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

private CuminRunStatus visit(MethodDeclaration md,ASTNode after) throws CashewException
{
   if (after == null) {
      if (AcornLog.isTracing()) AcornLog.logT("Start executing method " + md.getName());
      List<CashewValue> argvals = getCallArgs();

      for (int i = 0; i < argvals.size(); ++i) {
	 CashewValue arg = argvals.get(i);
	 if (arg == null) {
	    AcornLog.logE("ARG " + i + " is UNDEFINED for " + argvals.size() + " " +
		  JcompAst.getJavaType(md) + " " + md);
	  }
	 else if (AcornLog.isTracing()) {
	    AcornLog.logT("ARG " + i + " = " +
                  argvals.get(i).getDebugString(runner_session,type_converter,execution_clock));
	  }
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
      //TODO: handle initial varargs calls
      for (Object o : args) {
	 SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
	 JcompSymbol psym = JcompAst.getDefinition(svd.getName());
	 CashewValue pv = lookup_context.findReference(type_converter,psym);
	 pv.setValueAt(runner_session,execution_clock,argvals.get(idx+off));
	 ++idx;
       }
      next_node = md.getBody();
    }
   else {
      JcompType typ = JcompAst.getJavaType(md);
      JcompType rtyp = typ.getBaseType();
      if (rtyp == null || rtyp.isVoidType()) {
	 return CuminRunStatus.Factory.createReturn();
       }
      else {
	// return CuminRunStatus.Factory.createReturn();
	 return CuminRunStatus.Factory.createCompilerError();
       }
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
   execution_stack.push(CashewValue.booleanValue(type_converter,v.booleanValue()));

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
   execution_stack.push(CashewValue.nullValue(type_converter));

   return null;
}



private CuminRunStatus visit(NumberLiteral v)
{
   JcompType jt = JcompAst.getExprType(v);
   switch (jt.getName()) {
      case "float" :
      case "double" :
	 String ds = v.getToken();
	 if (ds.endsWith("f") || ds.endsWith("F")) {
	    ds = ds.substring(0,ds.length()-1);
	  }
	 double dv = Double.parseDouble(ds);
	 execution_stack.push(CashewValue.numericValue(jt,dv));
	 break;
      default :
	 String sv = v.getToken();
	 long lv = 0;
	 if (sv.endsWith("L") || sv.endsWith("l")) {
	    sv = sv.substring(0,sv.length()-1);
	  }
	 if ((sv.startsWith("0x") || sv.startsWith("0X")) && sv.length() > 2) {
	    sv = sv.substring(2);
	    if (sv.length() == 16) {
	       for (int i = 0; i < 16; ++i) {
		  int v0 = Character.digit(sv.charAt(i),16);
		  lv = (lv<<4) | v0;
		}
	     }
	    else lv = Long.parseLong(sv,16);
	  }
	 else if ((sv.startsWith("0b") || sv.startsWith("0B")) && sv.length() > 2) {
	    sv = sv.substring(2);
	    lv = Long.parseLong(sv,2);
	  }
	 else if (sv.startsWith("0") && sv.length() > 1) {
	    sv = sv.substring(1);
	    lv = Long.parseLong(sv,8);
	  }
	 else lv = Long.parseLong(sv);
	 execution_stack.push(CashewValue.numericValue(type_converter,jt,lv));
	 break;
    }

   return null;
}



private CuminRunStatus visit(StringLiteral v)
{
   try {
      JcompTyper typer = type_converter;
      execution_stack.push(CashewValue.stringValue(typer,typer.STRING_TYPE,v.getLiteralValue()));
    }
   catch (IllegalArgumentException e) {
      return CuminRunStatus.Factory.createCompilerError();
    }
   catch (StringIndexOutOfBoundsException e) {
      return CuminRunStatus.Factory.createCompilerError();
    }

   return null;
}



private CuminRunStatus visit(TypeLiteral v)
{
   JcompType acttyp = JcompAst.getJavaType(v.getType());
   execution_stack.push(CashewValue.classValue(type_converter,acttyp));

   return null;
}



/********************************************************************************/
/*										*/
/*	Expression handling							*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(ArrayAccess v,ASTNode after) throws CashewException, CuminRunException
{
   if (after == null) next_node = v.getArray();
   else if (after == v.getArray()) next_node = v.getIndex();
   else {
      CashewValue cv = execution_stack.pop();
      CashewValue av = execution_stack.pop();
      CashewValueSession sess = runner_session;
      JcompTyper typer = type_converter;
      cv = CuminEvaluator.unboxValue(sess,typer,execution_clock,cv);
      int idx = cv.getNumber(sess,execution_clock).intValue();
      if (idx < 0 || idx >= av.getDimension(sess,execution_clock)) {
	 return CuminEvaluator.returnException(sess,typer,"java.lang.ArrayIndexOutOfBoundsException");
       }
      CashewValue rv = av.getIndexValue(sess,execution_clock,idx);
      execution_stack.push(rv);
    }

   return null;
}


private CuminRunStatus visit(ArrayCreation v,ASTNode after) throws CashewException
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
   if (v.getInitializer() != null && after != v.getInitializer()) {
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
   int [] idims = new int[jsize];
   int k = dims.size();
   for (int i = 0; i < jsize; ++i) idims[i] = 0;
   for (int i = 0; i < k; ++i) {
      int v0 = execution_stack.pop().getNumber(runner_session,execution_clock).intValue();
      if (v0 < 0) {
	 return CuminEvaluator.returnException(runner_session,type_converter,"java.lang.NegativeArraySizeException");
       }
      idims[k-i-1] = v0;
    }
   CashewValue cv = CuminEvaluator.buildArray(this,0,idims,base);

   if (init != null) {
      // TODO: check that init has the right dimensions
      execution_stack.push(init);
    }
   else execution_stack.push(cv);

   return null;
}



private CuminRunStatus visit(Assignment v,ASTNode after)
	throws CuminRunException, CashewException
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


private CuminRunStatus visit(CastExpression v,ASTNode after)
	throws CuminRunException, CashewException
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
	throws CuminRunException, CashewException
{
   if (after == null) next_node = v.getExpression();
   else if (after == v.getExpression()) {
      CashewValue cv = execution_stack.pop();
      if (getBoolean(cv)) {
	 next_node = v.getThenExpression();
       }
      else {
	 next_node = v.getElseExpression();
       }
    }

   return null;
}



private CuminRunStatus visit(FieldAccess v,ASTNode after)
	throws CuminRunException, CashewException
{
   if (after == null) next_node = v.getExpression();
   else {
      JcompSymbol sym = JcompAst.getReference(v.getName());
      if (sym != null) {
	 execution_stack.push(handleFieldAccess(sym));
       }
      else {
	 String s = v.getName().getIdentifier();
	 CashewValue obj = execution_stack.pop().getActualValue(runner_session,execution_clock);
	 CashewValue rslt = obj.getFieldValue(runner_session,type_converter,execution_clock,s);
	 execution_stack.push(rslt);
       }
    }

   return null;
}


private CuminRunStatus visit(SuperFieldAccess v) throws CashewException
{
   String var = "this";
   if (v.getQualifier() != null) {
      JcompType qtyp = JcompAst.getExprType(v.getQualifier());
      var = qtyp.getName() + ".this";
    }
   CashewValue obj = lookup_context.findReference(var);
   if (obj.isNull(runner_session,execution_clock)) {
      return CuminEvaluator.returnException(runner_session,type_converter,"java.lang.NullPointerException");
    }

   JcompSymbol sym = JcompAst.getReference(v.getName());
   try {
      CashewValue rslt = obj.getFieldValue(runner_session,type_converter,execution_clock,sym.getFullName());
      execution_stack.push(rslt);
    }
   catch (NullPointerException e) {
      return CuminEvaluator.returnException(runner_session,type_converter,"java.lang.NullPointerException");
    }

   return null;
}



private CuminRunStatus visit(InfixExpression v,ASTNode after)
	throws CuminRunException, CashewException
{
   if (after == null) next_node = v.getLeftOperand();
   else if (after == v.getLeftOperand()) {
      if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
	 CashewValue v1 = execution_stack.pop();
	 if (getBoolean(v1)) {
	    next_node = v.getRightOperand();
	  }
	 else {
	    execution_stack.push(v1);
	  }
       }
      else if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
	 CashewValue v1 = execution_stack.pop();
	 if (getBoolean(v1)) {
	    execution_stack.push(v1);
	  }
	 else {
	    next_node = v.getRightOperand();
	  }
       }
      else next_node = v.getRightOperand();
    }
   else if (after == v.getRightOperand() && !v.hasExtendedOperands()) {
      if (v.getOperator() != InfixExpression.Operator.CONDITIONAL_AND &&
	    v.getOperator() != InfixExpression.Operator.CONDITIONAL_OR) {
	 CashewValue v2 = execution_stack.pop();
	 CashewValue v1 = execution_stack.pop();
	 CuminOperator op = op_map.get(v.getOperator());
	 CashewValue v0 = CuminEvaluator.evaluate(this,type_converter,execution_clock,op,v1,v2);
	 execution_stack.push(v0);
       }
    }
   else {
      List<?> ext = v.extendedOperands();
      int idx = ext.indexOf(after) + 1;
      ASTNode nxt = null;
      if (idx < ext.size()) nxt = (ASTNode) ext.get(idx);

      if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
	 if (nxt != null) {
	    CashewValue v1 = execution_stack.pop();
	    if (getBoolean(v1)) {
	       next_node = nxt;
	     }
	    else {
	       execution_stack.push(v1);
	     }
	  }
       }
      else if (v.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
	 if (nxt != null) {
	    CashewValue v1 = execution_stack.pop();
	    if (getBoolean(v1)) {
	       execution_stack.push(v1);
	     }
	    else {
	       next_node = nxt;
	     }
	  }
       }
      else {
	 CashewValue v2 = execution_stack.pop();
	 CashewValue v1 = execution_stack.pop();
	 CuminOperator op = op_map.get(v.getOperator());
	 CashewValue v0 = CuminEvaluator.evaluate(this,type_converter,execution_clock,op,v1,v2);
	 execution_stack.push(v0);
	 next_node = nxt;
       }
    }

   return null;
}



private CuminRunStatus visit(InstanceofExpression v,ASTNode after) throws CuminRunException
{
   if (after == null) next_node = v.getLeftOperand();
   else {
      JcompType rt = JcompAst.getJavaType(v.getRightOperand());
      CashewValue nv = execution_stack.pop();
      boolean fg = false;
      if (!nv.isNull(runner_session,execution_clock)) {
         fg = nv.getDataType(runner_session,execution_clock,type_converter).isCompatibleWith(rt);
       }
      CashewValue rv = CashewValue.booleanValue(type_converter,fg);
      execution_stack.push(rv);
    }

   return null;
}


private CuminRunStatus visit(SimpleName v) throws CashewException
{
   JcompSymbol js = JcompAst.getReference(v);
   CashewValue cv = null;
   if (js != null && js.isFieldSymbol()) {
      if (js.isStatic()) cv = handleStaticFieldAccess(js);
      else {
	 CashewValue tv = lookup_context.findReference(THIS_NAME);
	 cv = tv.getFieldValue(runner_session,type_converter,execution_clock,js.getFullName(),false);
	 while (cv == null) {
	    String nm = tv.getDataType(runner_session,execution_clock,null).getName() + "." + OUTER_NAME;
	    CashewValue xtv = tv.getFieldValue(runner_session,type_converter,execution_clock,nm,false);
	    if (xtv == null) xtv = tv.getFieldValue(runner_session,type_converter,execution_clock,OUTER_NAME,false);
	    if (xtv == null) break;
	    tv = xtv;
	    cv = tv.getFieldValue(runner_session,type_converter,execution_clock,js.getFullName(),false);
	  }
	 if (cv == null) {
	    AcornLog.logE("Missing Field " + js.getFullName());
	  }
       }
    }
   else {
      cv = lookup_context.findReference(type_converter,js);
      if (AcornLog.isTracing()) AcornLog.logT("\tReference " + js.getName() + " at " + v.getStartPosition());
    }

   if (cv == null || JcompAst.getExprType(v).isErrorType()) {
      AcornLog.logI("Unknown simple name " + v + " in " + v.getParent());
      String msg = v.getIdentifier() + " is undefined";
      return CuminRunStatus.Factory.createCompilerError(msg);
    }

   execution_stack.push(cv);

   return null;
}


private CuminRunStatus visit(QualifiedName v,ASTNode after)
	throws CuminRunException, CashewException
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
      CashewValue obj = execution_stack.pop().getActualValue(runner_session,execution_clock);
      if (obj.isNull(runner_session,execution_clock)) {
	 return CuminEvaluator.returnException(runner_session,type_converter,"java.lang.NullPointerException");
       }
      CashewValue rslt = obj.getFieldValue(runner_session,type_converter,execution_clock,"length");
      if (AcornLog.isTracing()) AcornLog.logT("LENGTH " + obj + " = " + rslt);
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
	throws CuminRunException, CashewException
{
   if (after == null) next_node = v.getOperand();
   else {
      CashewValue v1 = execution_stack.pop();
      CuminOperator op = op_map.get(v.getOperator());
      CashewValue v0 = CuminEvaluator.evaluate(runner_session,type_converter,execution_clock,op,v1);
      execution_stack.push(v0);
    }

   return null;
}


private CuminRunStatus visit(PrefixExpression v,ASTNode after)
	throws CuminRunException, CashewException
{
   if (after == null) next_node = v.getOperand();
   else {
      CashewValue v1 = execution_stack.pop();
      CuminOperator op = op_map.get(v.getOperator());
      CashewValue v0 = CuminEvaluator.evaluate(runner_session,type_converter,execution_clock,op,v1);
      execution_stack.push(v0);
    }

   return null;
}



private CuminRunStatus visit(ThisExpression v) throws CashewException
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

private CuminRunStatus visit(ClassInstanceCreation v, ASTNode after)
	throws CuminRunException, CashewException
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
   Map<JcompSymbol,JcompSymbol> inits = null;
   if (v.getAnonymousClassDeclaration() != null) {
      inits = fixMethodClass(v.getAnonymousClassDeclaration());
      rty = JcompAst.getJavaType(v.getAnonymousClassDeclaration());
    }
   else {
      JcompSymbol js1 = rty.getDefinition();
      if (js1 != null) {
         ASTNode defn = js1.getDefinitionNode();
         if (defn != null) inits = getMethodClassData(defn);
         
       }
    }
   
   JcompSymbol csym = JcompAst.getReference(v);
   if (csym == null) {
      JcompType ctyp = type_converter.createMethodType(null,null,false,null);
      for (JcompType sty = rty.getSuperType(); sty != null; rty = rty.getSuperType()) {
	 if (sty.getName().equals("java.lang.Object")) break;
	 JcompSymbol js = sty.lookupMethod(type_converter,"<init>",ctyp);
	 if (js != null && js.getClassType().equals(sty)) {
	    csym = js;
	    break;
	  }
       }
    }

   CashewValue rval = handleNew(rty);
   if (inits != null) {
      fixMethodInits(rval,inits);
    }
   
   if (csym == null) {
      execution_stack.push(rval);
      return null;
    }

   List<CashewValue> argv = new ArrayList<CashewValue>();
   JcompType ctyp = csym.getType();

   List<JcompType> atyps = ctyp.getComponents();
   for (int i = 0; i < args.size(); ++i) {
      CashewValue cv = execution_stack.pop();
      cv = cv.getActualValue(runner_session,execution_clock);
      int atypct = args.size() - 1 - i;
      if (ctyp.isVarArgs() && atypct >= atyps.size()) {
	 JcompType argtyp = atyps.get(atyps.size()-1);
	 int arrct = args.size() - atyps.size() + 1;
	 CashewValue arrv = CashewValue.arrayValue(type_converter,argtyp,arrct);
	 for (int j = 0; j < arrct; ++j) {
	    if (j > 0) {
	       cv = execution_stack.pop();
	       cv = cv.getActualValue(runner_session,execution_clock);
	       i++;
	       atypct--;
	     }
	    arrv.setIndexValue(runner_session,execution_clock,arrct-j-1,cv);
	  }
	 cv = arrv;
       }
      JcompType argtyp = atyps.get(atypct);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,argtyp);
      if (ncv == null) {
	 AcornLog.logD("Conversion problem " + cv + " " + argtyp + " " +
			  cv.getDataType(runner_session,execution_clock,null) );
       }
      argv.add(ncv);
    }
   if (args.size() < atyps.size() && ctyp.isVarArgs()) {
      JcompType argtyp = atyps.get(atyps.size()-1);
      CashewValue arrv = CashewValue.arrayValue(type_converter,argtyp,0);
      argv.add(arrv);
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


private CuminRunStatus visit(TypeDeclarationStatement v, ASTNode after)
        throws CuminRunException, CashewException
{
   fixMethodClass(v.getDeclaration());
   
   return null;
} 


private CuminRunStatus visitThrow(ClassInstanceCreation n,CuminRunStatus cause)
{
   if (cause.getReason() == Reason.RETURN) {
      CashewValue cv = cause.getValue();
      if (cv != null) execution_stack.push(cv);
    }
   else return cause;
   
   return null;
}



private CuminRunStatus visit(ConstructorInvocation v,ASTNode after)
	throws CuminRunException, CashewException
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

   // need to handle anonymous class declarations

   JcompSymbol csym = JcompAst.getReference(v);
   JcompType ctyp = csym.getType();
   List<JcompType> atyps = ctyp.getComponents();

   CashewValue rval = lookup_context.findReference(THIS_NAME);
   List<CashewValue> argv = new ArrayList<CashewValue>();
   for (int i = 0; i < args.size(); ++i) {
      CashewValue cv = execution_stack.pop();
      cv = cv.getActualValue(runner_session,execution_clock);
      JcompType argtyp = atyps.get(args.size()-1-i);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,argtyp);
      if (ncv == null) {
	 AcornLog.logD("Conversion problem " + cv + " " + argtyp + " " +
			  cv.getDataType(runner_session,execution_clock,null) );
       }
      argv.add(ncv);
    }
   argv.add(rval);
   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,csym,argv,CallType.SPECIAL);
   return CuminRunStatus.Factory.createCall(crun);
}



private CuminRunStatus visit(MethodInvocation v,ASTNode after)
	throws CuminRunException, CashewException
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

   if (AcornLog.isTracing()) AcornLog.logT("INVOKE " + args.size() + " " + v + " " + js.getType());

   JcompType ctyp = js.getType();
   int narg = ctyp.getComponents().size();

   if (ctyp.isVarArgs()) {
      int ncomp = ctyp.getComponents().size();
      JcompType vtyp = ctyp.getComponents().get(ncomp-1);
      JcompType btyp = vtyp.getBaseType();
      int sz = args.size() - ncomp + 1;
      if (sz == 1) {
	 CashewValue cv = execution_stack.peek(0);
	 JcompType cvtyp = cv.getDataType(runner_session,execution_clock,type_converter);
	 if (cvtyp.isArrayType() && cvtyp.isCompatibleWith(vtyp)) {
	    sz = -1;
	  }
       }
      if (sz >= 0) {
	 Map<Integer,Object> inits = new HashMap<Integer,Object>();
	 for (int i = ncomp; i <= args.size(); ++i) {
	    CashewValue cv = execution_stack.pop().getActualValue(runner_session,execution_clock);
	    CashewValue ncv = CuminEvaluator.castValue(this,cv,btyp);
	    inits.put(sz-(i-ncomp)-1,ncv);
	  }
	 CashewValue varval = CashewValue.arrayValue(type_converter,vtyp,sz,inits);
	 execution_stack.push(varval);
       }
    }

   List<JcompType> atyps = ctyp.getComponents();

   List<CashewValue> argv = new ArrayList<CashewValue>();
   for (int i = 0; i < narg; ++i) {
      CashewValue cv = execution_stack.pop().getActualValue(runner_session,execution_clock);
      JcompType argtyp = atyps.get(narg-1-i);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,argtyp);
      if (ncv == null) {
	 AcornLog.logD("Conversion problem " + cv + " " + argtyp + " " +
			  cv.getDataType(runner_session,execution_clock,null) );
       }
      argv.add(ncv);
    }
   CallType cty = CallType.VIRTUAL;
   if (!js.isStatic()) {
      CashewValue thisv = null;
      if (v.getExpression() != null) {
	 ASTNode next = v.getExpression();
	 JcompType jty = JcompAst.getJavaType(next);
	 JcompType ety = JcompAst.getExprType(next);
	 if (jty != null && jty != ety) {
	    thisv = null;
	  }
	 else {
	    thisv = execution_stack.pop();
	  }
       }
      else thisv = lookup_context.findReference(THIS_NAME);
      if (thisv != null) {
	 thisv = thisv.getActualValue(runner_session,execution_clock);
	 argv.add(thisv);
       }
      else {
	 AcornLog.logE("THIS problem " + js + " " + v );
       }
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


private CuminRunStatus visitThrow(SuperMethodInvocation n,CuminRunStatus cause)
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
      cv = cv.getActualValue(runner_session,execution_clock);
      argv.add(cv);
    }
   argv.add(rval);
   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,csym,argv,CallType.SPECIAL);
   return CuminRunStatus.Factory.createCall(crun);
}



private CuminRunStatus visitThrow(SuperConstructorInvocation n,CuminRunStatus cause)
{
   if (cause.getReason() == Reason.RETURN) {
      CashewValue cv = cause.getValue();
      if (cv != null) execution_stack.push(cv);
    }
   else return cause;
   
   return null;
}




private CuminRunStatus visit(SuperMethodInvocation v,ASTNode after)
	throws CuminRunException, CashewException
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

   //TODO: need to handle varargs

   // List<CashewValue> argv = new ArrayList<CashewValue>();
   // JcompSymbol js = JcompAst.getReference(v.getName());
   // CallType cty = CallType.VIRTUAL;
   // if (!js.isStatic()) {
      // JcompType base = null;
      // if (v.getQualifier() != null) {
	 // base = JcompAst.getExprType(v.getQualifier());
       // }
      // CashewValue thisv = handleThisAccess(base);
      // thisv = thisv.getActualValue(runner_session,execution_clock);
      // argv.add(thisv);
      // cty = CallType.STATIC;
    // }
   // for (int i = 0; i < args.size(); ++i) argv.add(null);
   // int off = argv.size();
   // for (int i = 0; i < args.size(); ++i) {
      // CashewValue cv = execution_stack.pop().getActualValue(runner_session,execution_clock);
      // argv.set(args.size()-i-1+off,cv);
    // }
   // CuminRunner crun = handleCall(execution_clock,js,argv,cty);
   // return CuminRunStatus.Factory.createCall(crun);

   if (AcornLog.isTracing()) AcornLog.logT("SUPER INVOKE " + args.size() + " " + v);

   JcompSymbol js = JcompAst.getReference(v.getName());
   JcompType ctyp = js.getType();
   int narg = ctyp.getComponents().size();

   if (ctyp.isVarArgs()) {
      int ncomp = ctyp.getComponents().size();
      JcompType vtyp = ctyp.getComponents().get(ncomp-1);
      JcompType btyp = vtyp.getBaseType();
      int sz = args.size() - ncomp + 1;
      if (sz == 1) {
	 CashewValue cv = execution_stack.peek(0);
	 JcompType cvtyp = cv.getDataType(runner_session,execution_clock,type_converter);
	 if (cvtyp.isArrayType() && cvtyp.isCompatibleWith(vtyp)) {
	    sz = -1;
	  }
       }
      if (sz >= 0) {
	 Map<Integer,Object> inits = new HashMap<Integer,Object>();
	 for (int i = ncomp; i <= args.size(); ++i) {
	    CashewValue cv = execution_stack.pop().getActualValue(runner_session,execution_clock);

	    CashewValue ncv = CuminEvaluator.castValue(this,cv,btyp);
	    inits.put(sz-(i-ncomp)-1,ncv);
	  }
	 CashewValue varval = CashewValue.arrayValue(type_converter,vtyp,sz,inits);
	 execution_stack.push(varval);
       }
    }

   List<JcompType> atyps = ctyp.getComponents();

   List<CashewValue> argv = new ArrayList<CashewValue>();
   for (int i = 0; i < narg; ++i) {
      CashewValue cv = execution_stack.pop().getActualValue(runner_session,execution_clock);
      JcompType argtyp = atyps.get(narg-1-i);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,argtyp);
      if (ncv == null) {
	 AcornLog.logD("Conversion problem " + cv + " " + argtyp + " " +
	       cv.getDataType(runner_session,execution_clock,null) );
       }
      argv.add(ncv);
    }
   CallType cty = CallType.SPECIAL;
   if (!js.isStatic()) {
      JcompType base = null;
      if (v.getQualifier() != null) {
	 base = JcompAst.getExprType(v.getQualifier());
       }
      CashewValue thisv = handleThisAccess(base);
      thisv = thisv.getActualValue(runner_session,execution_clock);
      argv.add(thisv);
    }
   else cty = CallType.STATIC;

   js = js.getBaseSymbol(type_converter);

   Collections.reverse(argv);
   CuminRunner crun = handleCall(execution_clock,js,argv,cty);
   return CuminRunStatus.Factory.createCall(crun);
}



/********************************************************************************/
/*										*/
/*	Statement methods							*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(AssertStatement s,ASTNode after)
	throws CashewException, CuminRunException
{
   if (after == null) next_node = s.getExpression();
   else {
      CashewValue cv = execution_stack.pop();
      if (!getBoolean(cv)) {
	 CuminEvaluator.throwException(runner_session,type_converter,"java.lang.AssertionError");
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




private CuminRunStatus visit(DoStatement s,ASTNode after) throws CuminRunException, CashewException
{
   if (after == null) next_node = s.getBody();
   else if (after == s.getBody()) next_node = s.getExpression();
   else {
      if (getBoolean(execution_stack.pop())) next_node = s.getBody();
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



private CuminRunStatus visit(ForStatement s,ASTNode after) throws CuminRunException, CashewException
{
   StructuralPropertyDescriptor spd = null;
   if (after != null) spd = after.getLocationInParent();

   if (after != null && after == s.getExpression()) {
      if (getBoolean(execution_stack.pop())) {
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
      execution_clock.tick();
      List<?> updts = s.updaters();
      int idx = 0;
      if (after != s.getBody()) {
         JcompType jt = JcompAst.getExprType(after);
         if (jt != null && !jt.isVoidType()) {
            execution_stack.pop();
          }
         idx = updts.indexOf(after)+1;
       }
      if (idx < updts.size()) {
	 next_node = (ASTNode) updts.get(idx);
	 return null;
       }
    }
   
   if (s.getExpression() == null) {
      next_node = s.getBody();
    }
   else {
      next_node = s.getExpression();
    }
   
   return null;
}



private CuminRunStatus visitThrow(ForStatement s,CuminRunStatus r)
	throws CuminRunException, CashewException
{
   String lbl = r.getMessage();
   switch (r.getReason()) {
      case BREAK :
	 if (checkLabel(s,lbl)) break;
	 else return r;
      case CONTINUE :
	 if (checkLabel(s,lbl)) {
	    return visit(s,s.getBody());
	  }
	 else return r;
      default :
	 return r;
    }
   return null;
}


private CuminRunStatus visit(EnhancedForStatement s,ASTNode after)
	throws CuminRunException, CashewException
{
   if (after == null) {
      next_node = s.getExpression();
      return null;
    }

   if (after == s.getExpression()) {
      JcompType ety = JcompAst.getExprType(s.getExpression());
      if (ety == null || ety.isVoidType()) {
         throw CuminRunStatus.Factory.createCompilerError();
       }
      CashewValue cv = execution_stack.peek(0).getActualValue(runner_session,execution_clock);
      JcompType jt = cv.getDataType(runner_session,execution_clock,type_converter);
      if (jt.isArrayType()) {
	 execution_stack.pushMarker(s,0);
       }
      else {
	 JcompTyper typer = JcompAst.getTyper(s);
	 JcompType rty = typer.findSystemType("java.util.Iterator");
	 List<JcompType> args = new ArrayList<>();
	 JcompType mty = type_converter.createMethodType(rty,args,false,null);
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
	throws CuminRunException, CashewException
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
		  if (getBoolean(rval)) {
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
	    return enhancedCheck(s);
	  }
	 else {
	    execution_stack.popMarker(s);
	    execution_stack.pop();
	    return r;
	  }
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
	 rty = type_converter.BOOLEAN_TYPE;
	 break;
      case NEXT :
	 mnm = "next";
	 rty = typer.findSystemType("java.lang.Object");
	 break;
      default :
	 return null;
    }

   JcompType jt = cv.getDataType(runner_session,execution_clock,type_converter);
   JcompType mty = type_converter.createMethodType(rty,args,false,null);
   JcompSymbol js = jt.lookupMethod(typer,mnm,mty);
   if (js == null) throw CuminRunStatus.Factory.createCompilerError();
   List<CashewValue> argv = new ArrayList<>();
   argv.add(cv);
   CuminRunner crun = handleCall(execution_clock,js,argv,CallType.VIRTUAL);
   return crun;
}



private void enhancedBody(EnhancedForStatement s,CashewValue next)
	throws CuminRunException, CashewException
{
   JcompSymbol js = JcompAst.getDefinition(s.getParameter().getName());
   CashewValue cr = lookup_context.findReference(type_converter,js);
   CuminEvaluator.evaluateAssign(this,CuminOperator.ASG,cr,next,js.getType());
   next_node = s.getBody();
}


private CuminRunStatus enhancedCheck(EnhancedForStatement s)
	throws CuminRunException, CashewException
{
   Object val = execution_stack.popUntil(s);
   CashewValue iter = execution_stack.pop().getActualValue(runner_session,execution_clock);
   if (val instanceof Integer) {
      int idx = (Integer) val;
      int len = iter.getDimension(runner_session,execution_clock);
      if (idx >= len) return null;
      CashewValue next = iter.getIndexValue(runner_session,execution_clock,idx);
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



private CuminRunStatus visit(IfStatement s,ASTNode after) throws CashewException,
        CuminRunException
{
   if (after == null) next_node = s.getExpression();
   else if (after == s.getExpression()) {
      boolean fg = getBoolean(execution_stack.pop());
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


private CuminRunStatus visitThrow(LabeledStatement s,CuminRunStatus r)
{
   switch (r.getReason()) {
      case BREAK :
         String lbl = r.getMessage();
         if (lbl == null) return r;
         if (lbl.equals(s.getLabel().getIdentifier())) return null;
         return r;
      default :
         break;
    }
   
   return r;
}


private CuminRunStatus visit(ReturnStatement s,ASTNode after)
{
   if (after == null && s.getExpression() != null) {
      next_node = s.getExpression();
    }
   else {
      JcompType jt = JcompAst.getJavaType(method_node);
      if (jt == null) return CuminRunStatus.Factory.createCompilerError();
      jt = jt.getBaseType();
      if (jt == null) return CuminRunStatus.Factory.createCompilerError();
      if (jt == null || jt.isVoidType()) {
	 if (s.getExpression() != null)
	    return CuminRunStatus.Factory.createCompilerError();
       }
      else {
	 if (s.getExpression() == null)
	    return CuminRunStatus.Factory.createCompilerError();
       }

      CashewValue rval = null;
      if (s.getExpression() != null) {
	 rval = execution_stack.pop();
	 rval = rval.getActualValue(runner_session,execution_clock);
	 if (rval == null) return CuminRunStatus.Factory.createCompilerError();
       }
      execution_clock.tick();
      return CuminRunStatus.Factory.createReturn(rval);
    }

   return null;
}



private CuminRunStatus visit(SwitchStatement s,ASTNode after)
	throws CashewException
{
   if (after == null) {
      next_node = s.getExpression();
      return null;
    }

   List<?> stmts = s.statements();

   if (after != null && after instanceof SwitchCase) {
      List<CashewValue> cases = new ArrayList<>();
      int sz = ((SwitchCase) after).expressions().size();
      for (int i = 0; i < sz; ++i) {
	 CashewValue casev = execution_stack.pop();
	 casev = casev.getActualValue(runner_session,execution_clock);
	 cases.add(casev);
       }
      CashewValue switchv = execution_stack.pop();
      switchv = switchv.getActualValue(runner_session,execution_clock);
      boolean match = false;
      for (CashewValue casev : cases) {
	 if (casev == switchv) match = true;
	 if (!match && switchv.getDataType(runner_session,execution_clock,type_converter).isStringType()) {
	    String s1 = switchv.getString(runner_session,type_converter,execution_clock);
	    String s2 = casev.getString(runner_session,type_converter,execution_clock);
	    match = s1.equals(s2);
	  }
	 else if (!match && casev.getDataType(runner_session,execution_clock,type_converter).isNumericType()) {
	    Number n0 = casev.getNumber(runner_session,execution_clock);
	    Number n1 = switchv.getNumber(runner_session,execution_clock);
	    if (n0 instanceof Float || n0 instanceof Double)
	       match = n0.doubleValue() == n1.doubleValue();
	    else match = n0.longValue() == n1.longValue();
	  }
	 if (match) break;
       }
      int idx = stmts.indexOf(after) + 1;
      if (match) {
	 // execute first statement after switch case
	 while (idx < stmts.size()) {
	    Statement stmt = (Statement) stmts.get(idx);
	    if (stmt instanceof SwitchCase) ++idx;
	    else {
	       next_node = stmt;
	       return null;
	     }
	  }
	 return null;
       }
      execution_stack.push(switchv);
    }
   if (after == s.getExpression() || after instanceof SwitchCase) {
      int idx = 0;
      if (after != s.getExpression()) idx = stmts.indexOf(after)+1;
      // find next case to check
      while (idx < stmts.size()) {
	 Statement stmt = (Statement) stmts.get(idx);
	 if (stmt instanceof SwitchCase) {
	    SwitchCase sc = (SwitchCase) stmt;
	    if (!sc.isDefault()) {
	       next_node = stmt;
	       return null;
	     }
	  }
	 ++idx;
       }
      execution_stack.pop();
      // find default if nothing matched
      idx = 0;
      while (idx < stmts.size()) {
	 Statement stmt = (Statement) stmts.get(idx);
	 if (stmt instanceof SwitchCase) {
	    SwitchCase sc = (SwitchCase) stmt;
	    if (sc.isDefault()) {
	       ++idx;
	       while (idx < stmts.size()) {
		  stmt = (Statement) stmts.get(idx);
		  if (stmt instanceof SwitchCase) ++idx;
		  else {
		     next_node = stmt;
		     break;
		   }
		}
	       return null;
	     }
	  }
	 ++idx;
       }
      return null;
    }

   // executing statements -- execute next statement in switch, ignore
   //	SwitchCase elements
   int next = stmts.indexOf(after)+1;
   while (next < stmts.size()) {
      Statement ns = (Statement) stmts.get(next);
      if (ns instanceof SwitchCase) ++next;
      else {
	 next_node = ns;
	 break;
       }
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





@SuppressWarnings("deprecation")
private CuminRunStatus visit(SwitchCase s,ASTNode after)
{
   try {
      List<?> exprs = s.expressions();
      int idx = 0;
      if (after != null) idx = exprs.indexOf(after)+1;
      if (idx < exprs.size()) next_node = (ASTNode) exprs.get(idx);
    }
   catch (Throwable t) {
      if (after == null) next_node = s.getExpression();
    }

   return null;
}



private CuminRunStatus visit(SynchronizedStatement s,ASTNode after)
{
   if (after == null) {
      next_node = s.getExpression();
    }
   else if (after == s.getExpression()) {
      CashewValue cv = execution_stack.peek(0).getActualValue(runner_session,execution_clock);
      CashewSynchronizationModel csm = lookup_context.getSynchronizationModel();
      if (csm != null) {
	 csm.synchEnter(cv);
       }
      next_node = s.getBody();
    }
   else {
      CashewValue cv = execution_stack.pop().getActualValue(runner_session,execution_clock);
      CashewSynchronizationModel csm = lookup_context.getSynchronizationModel();
      if (csm != null) {
	 csm.synchExit(cv);
       }
    }

   return null;
}


private CuminRunStatus visitThrow(SynchronizedStatement s,CuminRunStatus cause)
{
   // need to release the lock if its held
   return cause;
}


private CuminRunStatus visit(ThrowStatement s,ASTNode after)
{
   if (after == null) next_node = s.getExpression();
   else {
      CashewValue cv = execution_stack.pop();
      cv = cv.getActualValue(runner_session,execution_clock);
      execution_clock.tick();
      return CuminRunStatus.Factory.createException(runner_session,cv);
    }

   return null;
}



private CuminRunStatus visit(TryStatement s,ASTNode after) throws CuminRunException
{
   if (after == null || after.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY) {
      if (after == null) {
	 execution_stack.pushMarker(s,TryState.BODY);
       }
      List<?> res = s.resources();
      int idx = 0;
      if (after != null) idx = res.indexOf(after)+1;
      if (idx >= res.size()) {
	 next_node = s.getBody();
       }
      else {
	 next_node = (ASTNode) res.get(idx);
       }
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
      if (o != TryState.FINALLY) {
	 boolean usenext = false;
	 if (o == TryState.BODY || o == TryState.CATCH) usenext = true;
	 JcompTyper typer = type_converter;
	 for (Object o1 : s.resources()) {
	    VariableDeclarationExpression vde = (VariableDeclarationExpression) o1;
	    for (Object o2 : vde.fragments()) {
	       VariableDeclarationFragment vdf = (VariableDeclarationFragment) o2;
	       JcompSymbol js = JcompAst.getDefinition(vdf);
	       CashewValue cv = lookup_context.findReference(typer,js);
	       if (cv == null) continue;
	       cv = cv.getActualValue(runner_session,execution_clock);
	       if (cv == null || cv.isNull(runner_session,execution_clock)) continue;
	       JcompType cty = cv.getDataType(runner_session,execution_clock,typer);
	       JcompType vty = typer.findSystemType("void");
	       List<JcompType> args = new ArrayList<>();
	       JcompType mty = type_converter.createMethodType(vty,args,false,null);
	       JcompSymbol msy = cty.lookupMethod(typer,"close",mty);
	       if (msy == null) continue;
	       if (usenext) {
		  execution_stack.pushMarker(s,js);
		  List<CashewValue> argv = new ArrayList<>();
		  argv.add(cv);
		  CuminRunner crun = handleCall(execution_clock,msy,argv,CallType.VIRTUAL);
		  return CuminRunStatus.Factory.createCall(crun);
		}
	       else if (o == js) usenext = true;
	     }
	  }
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
      JcompType etyp = r.getValue().getDataType(runner_session,execution_clock,type_converter);
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


private CuminRunStatus visit(CatchClause s,ASTNode after) throws CashewException
{
   if (after == null) {
      CashewValue rv = execution_stack.pop();
      ASTNode decl = s.getException().getName();
      JcompSymbol csym = JcompAst.getDefinition(decl);
      CashewValue cv = lookup_context.findReference(type_converter,csym);
      cv.setValueAt(runner_session,execution_clock,rv);
      next_node = s.getBody();
    }

   return null;
}



private CuminRunStatus visit(WhileStatement s,ASTNode after) throws CuminRunException, CashewException
{
   if (after == null) next_node = s.getExpression();
   else if (after == s.getExpression()) {
      if (getBoolean(execution_stack.pop())) next_node = s.getBody();
    }
   else if (after == s.getBody()) {
      execution_clock.tick();
      next_node = s.getExpression();
    }

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
        throws CashewException, CuminRunException
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
      CashewValue arrval = CashewValue.arrayValue(type_converter,typ,dim);
      for (int i = dim-1; i >= 0; --i) {
	 CashewValue cv = execution_stack.pop();
         cv = CuminEvaluator.boxValue(this,cv);
	 arrval.setIndexValue(runner_session,execution_clock,i,cv);
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



private CuminRunStatus visit(SingleVariableDeclaration n,ASTNode after)
	throws CuminRunException, CashewException
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
	throws CuminRunException, CashewException
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
/*	Lambda and reference handling						*/
/*										*/
/********************************************************************************/

private CuminRunStatus visit(LambdaExpression v,ASTNode after) throws CashewException
{
   if (v == method_node && after == null) {
      List<CashewValue> argvals = getCallArgs();
      int idx = 0;
      for (Object o : v.parameters()) {
	 ASTNode n = (ASTNode) o;
	 JcompSymbol psym = JcompAst.getDefinition(n);
	 CashewValue pv = lookup_context.findReference(type_converter,psym);
	 pv.setValueAt(runner_session,execution_clock,argvals.get(idx+1));
	 ++idx;
       }
      CashewValue lv = argvals.get(0).getActualValue(runner_session,execution_clock);
      Map<Object,CashewValue> binds = lv.getBindings();
      if (binds != null) {
	 for (Map.Entry<Object,CashewValue> ent : binds.entrySet()) {
	    CashewValue cv = lookup_context.findActualReference(ent.getKey());
	    if (cv != null) {
	       cv.setValueAt(runner_session,execution_clock,ent.getValue());
	     }
	    else {
	       CashewValue nv = CashewValue.createReference(ent.getValue(),false);
	       lookup_context.define(ent.getKey(),nv);
	     }
	  }
       }
      next_node = v.getBody();
      return null;
    }
   else if (v == method_node && after != null) {
      CashewValue rval = null;
      if (after instanceof Expression) {
	 rval = execution_stack.pop();
	 rval = rval.getActualValue(runner_session,execution_clock);
       }
      execution_clock.tick();
      return CuminRunStatus.Factory.createReturn(rval);
    }
   else {
      // handle lambda definition in code
      Map<Object,CashewValue> bindings = new HashMap<>();
      List<JcompSymbol> params = new ArrayList<>();

      for (Object o : v.parameters()) {
	 if (o instanceof SingleVariableDeclaration) {
	    SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
	    JcompSymbol js = JcompAst.getDefinition(svd.getName());
	    params.add(js);
	  }
	 else {
	    VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	    JcompSymbol js = JcompAst.getDefinition(vdf);
	    params.add(js);
	  }
       }

      LambdaVisitor lv = new LambdaVisitor();
      v.accept(lv);
      if (lv.getUseThis()) {
	 CashewValue cv = lookup_context.findReference("this");
	 bindings.put("this",cv.getActualValue(runner_session,execution_clock));
       }
      for (JcompSymbol js : lv.getReferences()) {
	 CashewValue cv = lookup_context.findReference(type_converter,js);
	 if (cv == null) continue;
	 bindings.put(js,cv.getActualValue(runner_session,execution_clock));
       }

      JcompType typ = JcompAst.getExprType(v);

      CashewValue cv = new CashewValueFunctionRef(type_converter,typ,v,params,bindings);
      execution_stack.push(cv);

      return null;
    }
}


private CuminRunStatus visit(CreationReference v,ASTNode after)
	throws CuminRunException, CashewException
{
   if (v == method_node) {
      return evaluateReference(v,after);
    }
   else {
      CashewValue cv = generateReferenceValue(v);
      execution_stack.push(cv);
      return null;
    }
}


private CuminRunStatus visit(ExpressionMethodReference v,ASTNode after)
	throws CuminRunException, CashewException
{
   if (v == method_node) {
      return evaluateReference(v,after);
    }

   JcompSymbol js = JcompAst.getReference(v);
   if (after == null && !js.isStatic()) {
      ASTNode next = v.getExpression();
      JcompType jty = JcompAst.getJavaType(next);
      JcompType ety = JcompAst.getExprType(next);
      if (jty == null || jty != ety) {
	 next_node = next;
	 return null;
       }
    }

   CashewValue refval = null;
   if (!js.isStatic() && after != null && after == v.getExpression()) {
      refval = execution_stack.pop();
    }

   CashewValue cv = generateReferenceValue(v,refval);
   execution_stack.push(cv);
   return null;
}



private CuminRunStatus visit(SuperMethodReference v,ASTNode after)
	throws CuminRunException, CashewException
{
   if (v == method_node) {
      return evaluateReference(v,after);
    }
   else {
      CashewValue cv = generateReferenceValue(v);
      execution_stack.push(cv);
      return null;
    }
}


private CuminRunStatus visit(TypeMethodReference v,ASTNode after)
	throws CuminRunException, CashewException
{
   if (v == method_node) {
      return evaluateReference(v,after);
    }
   else {
      CashewValue cv = generateReferenceValue(v);
      execution_stack.push(cv);
      return null;
    }
}




private static class LambdaVisitor extends ASTVisitor {

   private Set<JcompSymbol> used_syms;
   private Set<JcompSymbol> defd_syms;
   private boolean use_this;

   LambdaVisitor() {
      used_syms = new HashSet<>();
      defd_syms = new HashSet<>();
      use_this = false;
    }

   Set<JcompSymbol> getReferences() {
      used_syms.removeAll(defd_syms);
      return used_syms;
    }

   boolean getUseThis() 		{ return use_this; }

   @Override public void postVisit(ASTNode n) {
      JcompSymbol js = JcompAst.getReference(n);
      if (js != null) {
	 if (js.isMethodSymbol()) {
	    if (!js.isStatic()) use_this = true;
	  }
	 else if (js.isFieldSymbol()) {
	    if (!js.isStatic()) use_this = true;
	    else used_syms.add(js);
	  }
	 else if (js.isTypeSymbol()) ;
	 else if (js.isEnumSymbol()) ;
	 else {
	    used_syms.add(js);
	  }
       }
      js =JcompAst.getDefinition(n);
      if (js != null) defd_syms.add(js);
    }


}	// end of inner class LambdaVisitor





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
	throws CuminRunException, CashewException
{
   CashewValue cv = null;
   if (init != null) {
      cv = execution_stack.pop();
    }
   else {
      cv = CashewValue.createDefaultValue(type_converter,js.getType());
    }
   CashewValue vv = lookup_context.findReference(type_converter,js);
   CuminEvaluator.evaluateAssign(this,CuminOperator.ASG,vv,cv,js.getType());
}


private CashewValue handleFieldAccess(JcompSymbol sym) throws CuminRunException, CashewException
{
   CashewValue obj = execution_stack.pop().getActualValue(runner_session,execution_clock);
   if (sym != null && sym.isStatic()) return handleStaticFieldAccess(sym);
   if (obj.isNull(runner_session,execution_clock))
      CuminEvaluator.throwException(runner_session,type_converter,"java.lang.NullPointerException");

   CashewValue rslt = obj.getFieldValue(runner_session,type_converter,execution_clock,sym.getFullName());
   return rslt;
}



private CashewValue handleStaticFieldAccess(JcompSymbol sym)
{
   CashewValue rslt = lookup_context.findReference(type_converter,sym);

   return rslt;
}

private CashewValue handleThisAccess(JcompType base)
	throws CashewException
{
   CashewValue cv = lookup_context.findReference(THIS_NAME);
   if (cv == null) return null;
   if (base == null) return cv;
   JcompType thistyp = cv.getDataType(runner_session,execution_clock,type_converter);
   if (thistyp.isCompatibleWith(base)) return cv;

   String nm = thistyp.getName() + "." + OUTER_NAME;
   CashewValue cv1 = lookup_context.findReference(OUTER_NAME);
   if (cv1 == null) cv1 = lookup_context.findReference(nm);
   if (cv1 == null) cv1 = cv.getFieldValue(runner_session,type_converter,execution_clock,nm);
   if (cv1 == null) {
      AcornLog.logE("Can't find outer this for " + base + " " + thistyp);
    }
   cv1 = handleThisAccess(base,cv1);
   if (cv1 != null) return cv1;

   return handleThisAccess(base,cv);
}


private CashewValue handleThisAccess(JcompType base,CashewValue cv)
	throws CashewException
{
   if (cv == null) return null;
   if (base == null) return cv;
   JcompType thistyp = cv.getDataType(runner_session,execution_clock,type_converter);
   if (thistyp.isCompatibleWith(base)) return cv;
   CashewValue cv1 = cv.getFieldValue(runner_session,type_converter,execution_clock,OUTER_NAME,false);
   return handleThisAccess(base,cv1);
}



private CashewValue generateReferenceValue(ASTNode n)
{
   return generateReferenceValue(n,null);
}


private CashewValue generateReferenceValue(ASTNode n,CashewValue ref)
{
   JcompType ntyp = JcompAst.getExprType(n);
   HashMap<Object,CashewValue> bind = null;
   if (ref != null) {
      bind = new HashMap<>();
      bind.put("this",ref);
    }
   return new CashewValueFunctionRef(type_converter,ntyp,n,null,bind);
}


private CuminRunStatus evaluateReference(ASTNode n,ASTNode after)
	throws CuminRunException, CashewException
{
   JcompSymbol js = JcompAst.getReference(n);
   JcompType typ = js.getType();
   List<CashewValue> args = new ArrayList<>(getCallArgs());
   List<JcompType> atyps = typ.getComponents();
   CashewValue refval = args.get(0);
   CashewValue thisval = refval;
   if (n instanceof CreationReference) {
      CreationReference cr = (CreationReference) n;
      JcompType jty = JcompAst.getExprType(cr.getType());
      if (jty == null) jty = JcompAst.getJavaType(cr.getType());
      thisval = handleNew(jty);
    }
   if (after == null) {
      if (!js.isStatic()) {
	 if (atyps.size() == args.size()-2) {
	    thisval = args.remove(1);
	  }
	 else {
	    Map<Object,CashewValue> binds = refval.getBindings();
	    if (binds != null && binds.get("this") != null) thisval = binds.get("this");
	  }
	 args.remove(0);
       }
      else {
	 args.remove(0);
	 if (atyps.size() == args.size()+1) {
	    args.add(0,thisval);
	  }
	 thisval = null;
       }
    }
   List<CashewValue> cargs = new ArrayList<>();
   CallType cty = CallType.VIRTUAL;
   if (!js.isStatic()) {
      cargs.add(thisval);
    }
   else cty = CallType.STATIC;

   for (int i = 0; i < atyps.size(); ++i) {
      JcompType ttyp = atyps.get(i);
      CashewValue cv = args.get(i).getActualValue(runner_session,execution_clock);
      CashewValue ncv = CuminEvaluator.castValue(this,cv,ttyp);
      cargs.add(ncv);
    }
   CuminRunner crun = handleCall(execution_clock,js,cargs,cty);
   return CuminRunStatus.Factory.createCall(crun);
}


private CuminRunStatus visitThrow(ASTNode n,CuminRunStatus cause)
	throws CuminRunException, CashewException
{
   if (cause.getReason() == Reason.RETURN) {
      JcompSymbol js = JcompAst.getReference(n);
      JcompType typ = js.getType();
      JcompType rtyp = typ.getBaseType();
      if (rtyp != null && !rtyp.isVoidType()) {
	 CashewValue cv = cause.getValue();
	 if (cv == null) {
	    // handle creation?
	  }
	 CashewValue ncv = CuminEvaluator.castValue(this,cv,rtyp);
	 CuminRunStatus rsts = CuminRunStatus.Factory.createReturn(ncv);
	 return rsts;
       }
      else {
	 CuminRunStatus rsts = CuminRunStatus.Factory.createReturn();
	 return rsts;
       }
    }
   else return cause;
}


private boolean getBoolean(CashewValue cv) throws CashewException, CuminRunException
{
   JcompType jt = cv.getDataType(runner_session,execution_clock,type_converter);
   if (jt.isPrimitiveType()) {
      return cv.getBoolean(runner_session,execution_clock);
    }
   else if (jt.getName().equals("java.lang.Boolean")) {
       CashewValue ncv = CuminEvaluator.unboxValue(runner_session,type_converter,execution_clock,cv); 
       return ncv.getBoolean(runner_session,execution_clock);
    }
   else {
      CuminEvaluator.throwException(runner_session,type_converter,"java.lang.ClassCastException");
    }
   
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Code to handle anonymous classes                                        */
/*                                                                              */
/********************************************************************************/

private Map<JcompSymbol,JcompSymbol> fixMethodClass(ASTNode classdef)
{
   Map<JcompSymbol,JcompSymbol> rslt = getMethodClassData(classdef);
   if (rslt != null) return rslt;
   
   rslt = new HashMap<>();
   
   OuterVariableFinder ovf = new OuterVariableFinder(classdef);
   classdef.accept(ovf);
   Set<JcompSymbol> syms = ovf.getUsedLocals();
   
   if (!syms.isEmpty()) {
      JcompType ctyp = JcompAst.getJavaType(classdef);
      for (JcompSymbol js : syms) {
         JcompSymbol newsym = JcompSymbol.createDummyField(js,ctyp);
         rslt.put(js,newsym);
       }
      OuterVariableReplacer ovr = new OuterVariableReplacer(rslt);
      classdef.accept(ovr);
    }
   
   classdef.setProperty(PROP_ANON_MAP,rslt);
   
   return rslt;
}


@SuppressWarnings("unchecked") 
private Map<JcompSymbol,JcompSymbol> getMethodClassData(ASTNode def)
{
   Map<JcompSymbol,JcompSymbol> rslt =(Map<JcompSymbol,JcompSymbol>) def.getProperty(PROP_ANON_MAP);
   
   return rslt;
}


private void fixMethodInits(CashewValue oval,Map<JcompSymbol,JcompSymbol> inits)
{
   if (inits == null || inits.isEmpty()) return;
   
   for (Map.Entry<JcompSymbol,JcompSymbol> ent : inits.entrySet()) {
      try {
         JcompSymbol orig = ent.getKey();
         CashewValue origrval = lookup_context.findReference(type_converter,orig);
         CashewValue origval = origrval.getActualValue(runner_session,execution_clock);
         JcompSymbol tgt = ent.getValue();
         CashewValue tgtrval = oval.getFieldValue(runner_session,type_converter,
               execution_clock,tgt.getFullName());
         CuminEvaluator.assignValue(this,tgtrval,origval,tgt.getType());
       }
      catch (Exception e) { 
         AcornLog.logE("CUMIN","Problem with method class initialization",e);
       }
    }
}


private static class OuterVariableFinder extends ASTVisitor {
   
   private Set<JcompSymbol> outer_syms;
   ASTNode base_node;
   
   OuterVariableFinder(ASTNode base) {
      outer_syms = new HashSet<>();
      base_node = base;
    }
   
   Set<JcompSymbol> getUsedLocals()                     { return outer_syms; }
   
   @Override public void preVisit(ASTNode n) {
      JcompSymbol js = JcompAst.getReference(n);
      if (js != null && js.getSymbolKind() == JcompSymbolKind.LOCAL) {
         ASTNode p = js.getDefinitionNode();
         boolean fnd = false;
         while (p != null && !fnd) {
            if (p == base_node) {
               fnd = true;
             }
            p = p.getParent();
          }
         if (!fnd) outer_syms.add(js);
       }
    }
   
}       // end of inner class OuterVariableFinder


private static class OuterVariableReplacer extends ASTVisitor {
   
   private Map<JcompSymbol,JcompSymbol> replace_map;
   
   OuterVariableReplacer(Map<JcompSymbol,JcompSymbol> rmap) {
      replace_map = rmap;
    }
   
   @Override public void preVisit(ASTNode n) {
      JcompSymbol js = JcompAst.getReference(n);
      if (js != null) {
         JcompSymbol njs = replace_map.get(js);
         if (njs != null) JcompAst.setReference(n,njs);
       }
    }
   
}       // end of inner class OuterVariableReplacer


}	// end of class CuminRunnerAst




/* end of CuminRunnerAst.java */
