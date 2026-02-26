/********************************************************************************/
/*                                                                              */
/*              SesameVarHistory.java                                           */
/*                                                                              */
/*      Compute variable history in execution                                   */
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



package edu.brown.cs.seede.sesame;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornConstants;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewContext;


class SesameVarHistory implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameSession   for_session;
private String          file_name;
private int             line_number;
private String          variable_name;
private long            base_time;
private CashewContext   relative_context;
private CashewContext   actual_context;
private CashewContext   call_context;
private String          call_file;
private int             call_line;
private String          call_method;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameVarHistory(SesameSession ss,Element xml)
{
   for_session = ss;
   
   file_name = IvyXml.getAttrString(xml,"FILE");
   line_number = IvyXml.getAttrInt(xml,"LINE");
   variable_name = IvyXml.getAttrString(xml,"VARIABLE");
   base_time = IvyXml.getAttrLong(xml,"TIME");
   relative_context = ss.findContext(IvyXml.getAttrInt(xml,"CONTEXT"));
   actual_context = findActualContext(relative_context,base_time);
   call_context = ss.findContext(IvyXml.getAttrInt(xml,"CALLEDCONTEXT"));
   call_file = IvyXml.getAttrString(xml,"CALLEDFILE");
   call_line = IvyXml.getAttrInt(xml,"CALLEDLINE");
   call_method = IvyXml.getAttrString(xml,"CALLEDMETHOD");
}



/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

void process(IvyXmlWriter xw)
{
   File ff2 = AcornConstants.getCanonical(file_name);
   SesameFile sf = for_session.getControl().getFileManager().openFile(ff2);
   if (sf == null) {
      xw.textElement("ERROR","Couldn't find file " + file_name);
      return;
    }
   
   CompilationUnit root = (CompilationUnit) sf.getResolvedAst(for_session.getProject());
   FindStatementVisitor fsv = new FindStatementVisitor(root,line_number);
   root.accept(fsv);
   ASTNode stmt = fsv.getMatch();
   if (stmt == null) {
      xw.textElement("ERROR","Can't find statement for line");
      return;
    }
   
   if (call_context != null) {
      MethodDeclaration md = fsv.getMethod();
      if (md == null) {
         xw.textElement("ERROR","Can't find method declaration for line");
         return; 
       }
      int idx = 0;
      JcompSymbol js = JcompAst.getDefinition(md.getName());
      String mnm = js.getFullName();
      for (Object o : md.parameters()) {
         SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
         String vnm = svd.getName().getIdentifier();
         if (vnm.equals(variable_name)) break;
         ++idx;
       }
      
      File ff1 = AcornConstants.getCanonical(call_file);
      SesameFile csf = for_session.getControl().getFileManager().openFile(ff1);
      if (csf == null) {
         xw.textElement("ERROR","Couldn't find call file " + file_name);
         return;
       }
      CompilationUnit croot = (CompilationUnit) csf.getResolvedAst(for_session.getProject());
      FindStatementVisitor cfsv = new FindStatementVisitor(croot,call_line,mnm);
      croot.accept(cfsv);
      stmt = cfsv.getMatch();
      if (stmt instanceof MethodInvocation) {
         MethodInvocation mi = (MethodInvocation) stmt;
         try {
            Object arg = mi.arguments().get(idx);
            stmt = (ASTNode) arg;
          }
         catch (Throwable t) {
            xw.textElement("ERROR","Couldn't find proper argument " + mi + " " + idx);
            return; 
          }
       }
      else if (stmt instanceof ClassInstanceCreation) {
         ClassInstanceCreation mi = (ClassInstanceCreation) stmt;
         try {
            Object arg = mi.arguments().get(idx);
            stmt = (ASTNode) arg;
          }
         catch (Throwable t) {
            xw.textElement("ERROR","Couldn't find proper argument " + mi + " " + idx);
            return; 
          }
       }
    }
   
   xw.begin("DEPEND");
   
   if (call_context != null) {
      xw.field("TYPE","PARAMETER");
      xw.textElement("BODY",call_method);
    }
   else if (actual_context != relative_context) {
      xw.field("TYPE","CALL");
      xw.field("INNERMETHOD",actual_context.getName());
      CashewContext ctx1 = actual_context;
      
      while (ctx1 != null && ctx1.getParentContext() != relative_context) {
         ctx1 = ctx1.getParentContext();
       }
      if (ctx1 == null) {
         AcornLog.logE("SESAME","Contexts differ " + actual_context.getId() + " " +
               relative_context.getId() + " " + base_time);
         ctx1 = relative_context;
       }
      xw.field("CALLMETHOD",ctx1.getName());
      // stmt = the part of statement corresponding to the function call to ctx1
    }
   else {
      xw.field("TYPE","STATEMENT");
      xw.textElement("BODY",stmt.toString().trim());
    }
   
   RefVisitor rv = new RefVisitor();
   stmt.accept(rv);
   Set<JcompSymbol> refs = rv.getReferencedSymbols();
   
   for (JcompSymbol js : refs) {
      xw.begin("VAR");
      xw.field("NAME",js.getFullName());
      xw.field("TYPE",js.getType().getName());
      if (js.getType().isBinaryType()) xw.field("BINARY",true);
      xw.field("KIND",js.getSymbolKind());
      JcompType ctyp = js.getClassType();
      if (ctyp != null) {
         xw.field("CLASS",ctyp.getName());
         if (ctyp.isBinaryType()) xw.field("BINCLASS",true);
       }
      xw.end("VAR");
    }
   
   xw.end("DEPEND");             
}



/********************************************************************************/
/*                                                                              */
/*      Find the actual context for a given time                                */
/*                                                                              */
/********************************************************************************/

private CashewContext findActualContext(CashewContext base0,long time)
{
   if (base0 == null) return null;
   
   CashewContext base = base0;
   
   for (CashewContext cc : base.getChildContexts()) {
      if (cc.getStartTime() < time && cc.getEndTime() > time) {
         return findActualContext(cc,time);
       }
    }
   
   while (base.getParentContext() != null) {
      if (base.isOutput()) break;
      AcornLog.logD("SESAME","Skipping found non-output context " + base.getId());
      base = base.getParentContext();
    }
   
   return base;
}



/********************************************************************************/
/*                                                                              */
/*      Visitor for finding statement at offset                                 */
/*                                                                              */
/********************************************************************************/

private static class FindStatementVisitor extends ASTVisitor {

   private CompilationUnit comp_unit;
   private int line_number;
   private String call_method;
   private String call_tail;
   private ASTNode best_match;
   private MethodDeclaration method_match;
   
   FindStatementVisitor(CompilationUnit cu,int lno) {
      this(cu,lno,null);
    }
   
   FindStatementVisitor(CompilationUnit cu,int lno,String call) {
      comp_unit = cu;
      line_number = lno;
      best_match = null;
      method_match = null;
      call_method = call;
      if (call == null) call_tail = null;
      else {
         int idx = call_method.lastIndexOf(".");
         call_tail = call_method.substring(idx);
       }
    }
   
   
   ASTNode getMatch() {
      if (best_match == null) return method_match;
      return best_match;
    }
   
   MethodDeclaration getMethod() {
      return method_match; 
    }
   
   @Override public boolean preVisit2(ASTNode n) {
      if (n instanceof Statement) {
         int soff = n.getStartPosition();
         int eoff = soff + n.getLength();
         int sln = comp_unit.getLineNumber(soff);
         int eln = comp_unit.getLineNumber(eoff);
         if (eln < line_number) return false;
         if (sln > line_number) return false;
         best_match = n;
       }
      else if (n instanceof VariableDeclarationFragment) {
         int soff = n.getStartPosition();
         int eoff = soff + n.getLength();
         int sln = comp_unit.getLineNumber(soff);
         int eln = comp_unit.getLineNumber(eoff);
         if (eln < line_number) return false;
         if (sln > line_number) return false;
         best_match = n;
       }
      else if (n instanceof MethodDeclaration) {
         int soff = n.getStartPosition();
         int eoff = soff + n.getLength();
         int sln = comp_unit.getLineNumber(soff);
         int eln = comp_unit.getLineNumber(eoff);
         if (eln < line_number) return false;
         if (sln > line_number) return false;
         method_match = (MethodDeclaration) n;
       }
      else if (n instanceof MethodInvocation && call_method != null) {
         MethodInvocation mi = (MethodInvocation) n;
         JcompSymbol js = JcompAst.getReference(mi);
         String cnm = js.getFullName();
         if (cnm.equals(call_method)) best_match = n;
         else if (cnm.endsWith(call_tail)) best_match = n;
       }
      else if (n instanceof ClassInstanceCreation && call_method != null) {
         ClassInstanceCreation ci = (ClassInstanceCreation) n;
         JcompSymbol js = JcompAst.getReference(ci);
         if (js.getFullName().equals(call_method)) best_match = n;
       }
      return true;
    }

}       // end of inner class FindStatementVisitor




/********************************************************************************/
/*                                                                              */
/*      Visitor for finding references                                          */
/*                                                                              */
/********************************************************************************/

private static class RefVisitor extends ASTVisitor {
   
   private Set<JcompSymbol> ref_syms;
   private boolean in_lvalue;
   
   RefVisitor() {
      ref_syms = new HashSet<JcompSymbol>();
      in_lvalue = false;
    }
   
   Set<JcompSymbol> getReferencedSymbols()              { return ref_syms; }
   
   @Override public void postVisit(ASTNode n) {
      JcompSymbol sym = JcompAst.getReference(n);
      if (sym != null && !in_lvalue) ref_syms.add(sym);
    }
   
   @Override public boolean visit(Assignment n) {
      boolean lv = in_lvalue;
      in_lvalue = true;
      n.getLeftHandSide().accept(this);
      in_lvalue = false;
      n.getRightHandSide().accept(this);
      in_lvalue = lv;
      return false;
    }
   
   @Override public boolean visit(ArrayAccess n) {
      boolean lv = in_lvalue;
      n.getArray().accept(this);
      in_lvalue = false;
      n.getIndex().accept(this);
      in_lvalue = lv;
      return false;
    }
   
   @Override public boolean visit(FieldAccess n) {
      boolean lv = in_lvalue;
      in_lvalue = false;
      n.getExpression().accept(this);
      in_lvalue = lv;
      n.getName().accept(this);
      return false;
    }
   
   @Override public boolean visit(QualifiedName n) {
      boolean lv = in_lvalue;
      in_lvalue = false;
      n.getQualifier().accept(this);
      in_lvalue = lv;
      n.getName().accept(this);
      return false;
    }
   
   @Override public boolean visit(PostfixExpression n) {
      boolean lv = in_lvalue;
      in_lvalue = false;                // forces var to be accepted
      n.getOperand().accept(this);
      in_lvalue = lv;
      return false;
    }
   
   @Override public boolean visit(PrefixExpression n) {
      boolean lv = in_lvalue;
      in_lvalue = false;                // forces var to be accepted
      n.getOperand().accept(this);
      in_lvalue = lv;
      return false;
    }
   
   @Override public boolean visit(VariableDeclarationFragment n) {
      if (n.getInitializer() != null) {
         n.getInitializer().accept(this);
       }
      return false;
    }
   
}       // end of inner class RefVisitor




}       // end of class SesameVarHistory




/* end of SesameVarHistory.java */

