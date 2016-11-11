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

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
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
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewConstants;

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
private CuminRunnerAst  ast_runner;
private ASTNode         root_node;
private ASTNode         current_node;
private EvalType        eval_type;
private Stack<EvalData> eval_stack;
private int             stack_level;

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

CuminRunnerAstVisitor(CuminRunnerAst runner,ASTNode root)
{
   ast_runner = runner;
   execution_stack = runner.getStack();
   execution_clock = runner.getClock();
   execution_context = runner.getLookupContext();
   eval_type = EvalType.RUN;
   current_node = null;
   root_node = root;
   eval_stack = new Stack<>();
   stack_level = -1;
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

ASTNode getCurrentNode()                        { return current_node; }

void setEvalType(EvalType et)                   { eval_type = et; }



/********************************************************************************/
/*                                                                              */
/*      General methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public boolean preVisit2(ASTNode n)
{
   current_node = n;
   
   JcompType jt = JcompAst.getExprType(n);
   if (jt == CashewConstants.ERROR_TYPE) {
      throw new CuminRunError(CuminRunError.Reason.ERROR);
    }
   if ((n.getFlags() & ASTNode.MALFORMED) != 0) {
      throw new CuminRunError(CuminRunError.Reason.ERROR);
    }
   
   return true;
}




/********************************************************************************/
/*                                                                              */
/*      Handle method initialization                                            */
/*                                                                              */
/********************************************************************************/

@Override public boolean visit(MethodDeclaration v)
{
   List<CashewValue> args = ast_runner.getCallArgs();
   
   int whence = checkRoot(v);
   
   if (whence < 0) {
      int idx = 0;
      int off = 0;
      JcompSymbol vsym = JcompAst.getDefinition(v);
      if (!vsym.isStatic()) {
         off = 1;
         execution_context.define("this",args.get(0));
       } 
      // need to handle nested this values as well
      
      int nparm = v.parameters().size();
      for (Object o : v.parameters()) {
         SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
         JcompSymbol psym = JcompAst.getDefinition(svd.getName());
         if (idx == nparm-1 && v.isVarargs()) {
            // handle var args
          }
         else {
            execution_context.define(psym,args.get(idx+off));
          }
         ++idx;
       }
    }
   
   v.getBody().accept(this);
   
   return false;
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
   CashewValue cv = execution_stack.pop();
   CashewValue av = execution_stack.pop();
   int idx = cv.getNumber(execution_clock).intValue();
   CashewValue rv = av.getIndexValue(execution_clock,idx);
   execution_stack.push(rv);
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


@Override public boolean visit(InstanceofExpression v)
{
   v.getLeftOperand().accept(this);
   JcompType rt = JcompAst.getJavaType(v.getRightOperand());
   CashewValue nv = CuminEvaluator.castValue(execution_clock,execution_stack.pop(),rt);
   execution_stack.push(nv);
   
   return false;
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
   String name = "this";
   if (v.getQualifier() != null) {
      name = v.getQualifier().getFullyQualifiedName() + "." + name;
    }
   CashewValue cv = execution_context.findReference(name);
   execution_stack.push(cv);
}


@Override public void endVisit(VariableDeclarationExpression v)
{
   // nothing to do here
}



/********************************************************************************/
/*                                                                              */
/*      Statement visitors                                                      */
/*                                                                              */
/********************************************************************************/

@Override public boolean visit(AssertStatement s)
{
   s.getExpression().accept(this);
   if (!execution_stack.pop().getBoolean(execution_clock)) {
      // throw assertion exception
    }
   
   return false;
}



@Override public boolean visit(Block s)
{
   int whence = enter(s);
   int idx = 0;
   for (Object o : s.statements()) {
      Statement st = (Statement) o;
      if (whence < 0 || idx >= whence) {
         st.accept(this);
         mark(s,idx+1);
       }
      ++idx;
    }
   exit(s);
   
   return false;
}


@Override public boolean visit(BreakStatement s)
{
   throw new CuminRunError(CuminRunError.Reason.BREAK,s.getLabel().getIdentifier());
}


@Override public boolean visit(ConstructorInvocation s)
{
   return true;
}


@Override public boolean visit(ContinueStatement s)
{
   throw new CuminRunError(CuminRunError.Reason.CONTINUE,s.getLabel().getIdentifier());
}


@Override public boolean visit(DoStatement s)
{
   boolean exit = false;
   for ( ; ; ) {
      try {
         s.getBody().accept(this);
       }
      catch (CuminRunError r) {
         String lbl = r.getMessage();
         switch (r.getReason()) {
            case BREAK :
               if (checkLabel(s,lbl)) exit = true;
               else throw r;
               break;
            case CONTINUE :
               if (!checkLabel(s,lbl)) throw r;;
               break;
            default :
               throw r;
          }
       }
      if (exit) break;
      s.getExpression().accept(this);
      if (!execution_stack.pop().getBoolean(execution_clock)) break;
    }
   return false;
}



@Override public boolean visit(EmptyStatement s)
{
   return false;
}


@Override public boolean visit(ExpressionStatement s)
{
   int lvl = execution_stack.size();
   s.getExpression().accept(this);
   while (execution_stack.size() > lvl) execution_stack.pop();
   return false;
}


@Override public boolean visit(ForStatement s)
{
   return true;
}


@Override public boolean visit(EnhancedForStatement s)
{ 
   return true;
}



@Override public boolean visit(IfStatement s)
{
   s.getExpression().accept(this);
   if (execution_stack.pop().getBoolean(execution_clock)) {
      s.getThenStatement().accept(this);
    }
   else if (s.getElseStatement() != null) {
      s.getElseStatement().accept(this);
    }
   
   return false;
}


@Override public boolean visit(LabeledStatement s)
{
   return true;
}


@Override public boolean visit(ReturnStatement s)
{
   CashewValue rval = null;
   if (s.getExpression() != null) {
      s.getExpression().accept(this);
      rval = execution_stack.pop();
    }
   throw new CuminRunError(CuminRunError.Reason.RETURN,rval);
}



@Override public boolean visit(SuperConstructorInvocation s)
{
   return true;
}


@Override public boolean visit(SwitchStatement s)
{
   return true;
}


@Override public boolean visit (SwitchCase s)
{
   return true;
}


@Override public boolean visit(SynchronizedStatement s)
{
   return true;
}


@Override public boolean visit(ThrowStatement s)
{
   s.getExpression().accept(this);
   throw new CuminRunError(CuminRunError.Reason.EXCEPTION,execution_stack.pop());
}


@Override public boolean visit(TryStatement s)
{
   try {
      s.getBody().accept(this);
    }
   catch (CuminRunError r) {
      if (r.getReason() != CuminRunError.Reason.EXCEPTION) throw r;
      JcompType etyp = r.getValue().getDataType(execution_clock);
      for (Object o : s.catchClauses()) {
         CatchClause cc = (CatchClause) o;
         SingleVariableDeclaration svd = cc.getException();
         JcompType ctype = JcompAst.getJavaType(svd.getType());
         if (etyp.isCompatibleWith(ctype)) {
            execution_stack.push(r.getValue());
            cc.accept(this);
            break;
          }
       }
      throw r;
    }
   
   return false;
}



@Override public boolean visit(CatchClause s)
{
   return true;
}



@Override public boolean visit(TypeDeclarationStatement s)
{
   return true;
}


@Override public boolean visit(VariableDeclarationStatement s)
{
   return true;
}


@Override public boolean visit(WhileStatement s)
{
   boolean exit = false;
   while (!exit) {
      s.getExpression().accept(this);
      if (!execution_stack.pop().getBoolean(execution_clock)) break;
      try {
         s.getBody().accept(this);
       }
      catch (CuminRunError r) {
         String lbl = r.getMessage();
         switch (r.getReason()) {
            case BREAK :
               if (checkLabel(s,lbl)) exit = true;
               else throw r;
               break;
            case CONTINUE :
               if (!checkLabel(s,lbl)) throw r;;
               break;
            default :
               throw r;
          }
       }
    }
   return false;
}



private boolean checkLabel(Statement s,String lbl)
{
   if (lbl == null) return true;
   if (s.getParent() instanceof LabeledStatement) {
      LabeledStatement lbs = (LabeledStatement) s.getParent();
      if (lbs.getLabel().getIdentifier().equals(lbl)) return true;
    }
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Declaration handling                                                    */
/*                                                                              */
/********************************************************************************/

@Override public void endVisit(ArrayInitializer n)
{ 
   int dim = n.expressions().size();
   JcompType typ = JcompAst.getExprType(n);
   CashewValue arrval = CashewValue.arrayValue(typ,dim);
   for (int i = dim-1; i >= 0; --i) {
      CashewValue cv = execution_stack.pop();
      arrval.setIndexValue(execution_clock,i,cv);
    }
   execution_stack.push(arrval);
}



@Override public boolean visit(EnumConstantDeclaration n)
{ 
   return false;
}


@Override public boolean visit(EnumDeclaration n)
{ 
   return false;
}

@Override public boolean visit(FieldDeclaration n)
{ 
   return true;                 // handle initializations
}

@Override public boolean visit(Modifier n)
{ 
   return false;
}

@Override public boolean visit(SingleVariableDeclaration n)
{ 
   ASTNode par = n.getParent();
   if (par instanceof MethodDeclaration) {
      // handle formal parameters
      return false;             // already handled
    }
   else if (par instanceof CatchClause) {
      // handle exception
    }
   else {
      JcompSymbol js = JcompAst.getDefinition(n.getName());
      handleInitialization(js,n.getInitializer());
    }
   
   return false;
}


private void handleInitialization(JcompSymbol js,ASTNode init)
{
   CashewValue cv = null;
   if (init != null) {
      init.accept(this);
      cv = execution_stack.pop();
    }
   else {
      cv = CashewValue.createDefaultValue(js.getType());
    }
   CashewValue vv = execution_context.findReference(js);
   CuminEvaluator.evaluate(execution_clock,CuminOperator.ASG,vv,cv);
}



@Override public boolean visit(TypeDeclaration n)
{ 
   return false;
}

@Override public boolean visit(VariableDeclarationFragment n)
{ 
   JcompSymbol js = JcompAst.getDefinition(n.getName());
   handleInitialization(js,n.getInitializer());
   
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Miscellaneous visitors                                                  */
/*                                                                              */
/********************************************************************************/


@Override public boolean visit(AnnotationTypeDeclaration n)
{
   return false;
}


@Override public boolean visit(AnnotationTypeMemberDeclaration n)
{ 
   return false;
}


@Override public boolean visit(AnonymousClassDeclaration n)
{ 
   return false;
}


@Override public boolean visit(CompilationUnit n) 
{ 
   return false;
}

@Override public boolean visit(ImportDeclaration n)
{ 
   return false;
}


@Override public boolean visit(Initializer n)
{ 
   return true;
}


@Override public boolean visit(MarkerAnnotation n) 
{ 
   return false;
}


@Override public boolean visit(MemberValuePair n)
{ 
   return false;
}


@Override public boolean visit(NormalAnnotation n)
{ 
   return false;
}


@Override public boolean visit(PackageDeclaration n)
{ 
   return false;
}


@Override public boolean visit(SingleMemberAnnotation n)
{ 
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Type visitors                                                           */
/*                                                                              */
/********************************************************************************/

@Override public boolean visit(ArrayType n)
{
   return false;
}


@Override public boolean visit(ParameterizedType n)
{
   return false;
}


@Override public boolean visit(PrimitiveType n)
{
   return false;
}


@Override public boolean visit(QualifiedType n)
{
   return false;
}



@Override public boolean visit(SimpleName n)
{
   return false;
}


@Override public boolean visit(TypeParameter n)
{
   return false;
}


@Override public boolean visit(UnionType n)
{ 
   return false;
}


@Override public boolean visit(WildcardType n)
{ 
   return false;
}


/********************************************************************************/
/*                                                                              */
/*      Eval stack methods                                                      */
/*                                                                              */
/********************************************************************************/

private int enter(ASTNode n) 
{
   if (stack_level >= 0) {
      EvalData ed = eval_stack.get(stack_level);
      if (ed.getNode() == n) {
         ++stack_level;
         if (stack_level >= eval_stack.size()) stack_level = -1;
         return ed.getIndex();
       }
    }
   
   eval_stack.push(new EvalData(n,0));
   return -1;
}


private void mark(ASTNode n,int idx)
{
   EvalData ed = eval_stack.peek();
   if (ed.getNode() != n) {
      System.err.println("Missing stack entry for mark");
      return;
    }
   ed.setIndex(idx);
}


private void exit(ASTNode n)
{
   EvalData ed = eval_stack.pop();
   if (ed.getNode() == n) return;
   System.err.println("Missing stack entry for exit");
   eval_stack.push(ed);
}



private int checkRoot(ASTNode n) 
{
   if (eval_stack.size() == 0) return -1;
   if (eval_stack.get(0).getNode() != n) return -1;
   stack_level = 1;
   return eval_stack.get(0).getIndex();
}




private static class EvalData {

   private ASTNode for_node;
   private int     node_index;
   
   EvalData(ASTNode n,int idx) {
      for_node = n;
      node_index = idx;
    }
   
   ASTNode getNode()                    { return for_node; }
   int getIndex()                       { return node_index; }
   void setIndex(int idx)               { node_index = idx; }

}       // end of inner class EvalData




}       // end of class CuminRunnerAstVisitor





/* end of CuminRunnerAstVisitor.java */

