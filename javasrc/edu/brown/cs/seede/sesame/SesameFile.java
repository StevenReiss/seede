/********************************************************************************/
/*										*/
/*		SesameFile.java 						*/
/*										*/
/*	Description of a single editable file					*/
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



package edu.brown.cs.seede.sesame;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompAstCleaner;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornLog;


class SesameFile implements SesameConstants, JcompAstCleaner
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IDocument		edit_document;
private File			for_file;
private Map<String,ASTNode>	ast_roots;
private Set<Integer>		error_lines;
private int			use_count;

private static SesameProject	current_project;
private static Object		project_lock = new Object();


private static final String NO_PROJECT = "*NOPROJECT*";


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameFile(File f,String cnts,String linesep)
{
   for_file = f;
   edit_document = new Document(cnts);
   ast_roots = new HashMap<String,ASTNode>();
   error_lines = new HashSet<>();
   use_count = 0;
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

void editFile(int len,int off,String txt,boolean complete)
{
   if (complete) len = edit_document.getLength();
   try {
      edit_document.replace(off,len,txt);
    }
   catch (BadLocationException e) {
      AcornLog.logE("Problem doing file edit",e);
    }

   synchronized (ast_roots) {
      ast_roots.remove(NO_PROJECT);
    }
}


void resetSemantics(SesameProject sp)
{
   // might need to be project specific if we have multiple sesssions active
   synchronized (ast_roots) {
      ast_roots.remove(sp.getName());
    }
}



/********************************************************************************/
/*										*/
/*	Track errors in file							*/
/*										*/
/********************************************************************************/

boolean handleErrors(Element msgs)
{
   Set<Integer> lines = new HashSet<>();
   for (Element e : IvyXml.children(msgs,"PROBLEM")) {
      if (!IvyXml.getAttrBool(e,"ERROR")) continue;
      int lno = IvyXml.getAttrInt(e,"LINE");
      if (lno <= 0) continue;
      lines.add(lno);
    }

   if (lines.equals(error_lines)) return false;

   error_lines = lines;
   return true;
}



/********************************************************************************/
/*										*/
/*	Methods to provide ASTs to jcomp for current project			*/
/*										*/
/********************************************************************************/

static void setCurrentProject(SesameProject sp)
{
   if (sp != null) {
      synchronized (project_lock) {
	 while (current_project != null) {
	    try {
	       project_lock.wait(5000);
	     }
	    catch (InterruptedException e) { }
	  }
	 current_project = sp;
       }
    }
   else {
      synchronized (project_lock) {
	 current_project = null;
	 project_lock.notifyAll();
       }
    }
}




public ASTNode getAstRootNode() 		// if we use extended source
{
   SesameProject sp = current_project;
   if (sp != null) {
      ASTNode an = null;
      synchronized (ast_roots) {
	 an = ast_roots.get(sp.getName());
	 if (an != null) return an;
       }
      an = getAst();
      synchronized (ast_roots) {
	 ast_roots.put(sp.getName(),an);
       }
      return an;
    }

   return getAst();
}



public ASTNode cleanupAst(ASTNode n)
{
   cleanupAstRoot((CompilationUnit) n);

   return n;
}



/********************************************************************************/
/*										*/
/*	Abstract syntax tree methods						*/
/*										*/
/********************************************************************************/

ASTNode getAst()
{
   ASTNode an = null;
   synchronized (ast_roots) {
      an = ast_roots.get(NO_PROJECT);
      if (an != null) return an;
    }

   an = buildAst();

   synchronized (ast_roots) {
      ASTNode nan = ast_roots.get(NO_PROJECT);
      if (nan != null) an = nan;
      else ast_roots.put(NO_PROJECT,an);
    }

   return an;
}


private ASTNode buildAst()
{
   ASTNode an = JcompAst.parseSourceFile(edit_document.get().toCharArray());
   JcompAst.setSource(an,this);
   cleanupAstRoot((CompilationUnit) an);

   return an;
}



private void cleanupAstRoot(CompilationUnit an)
{
   for (Object typn : an.types()) {
      if (typn instanceof TypeDeclaration) {
	 TypeDeclaration td = (TypeDeclaration) typn;
	 fixType(td);
       }
    }
}



@SuppressWarnings("unchecked")
private void fixType(TypeDeclaration td)
{
   AST ast = td.getAST();
   boolean havecnst = td.isInterface();
   List<VariableDeclarationFragment> cnstinits = null;
   for (Object o : td.bodyDeclarations()) {
      BodyDeclaration bd = (BodyDeclaration) o;
      if (bd instanceof MethodDeclaration) {
	 MethodDeclaration md = (MethodDeclaration) bd;
	 if (md.isConstructor()) havecnst = true;
       }
      else if (bd instanceof FieldDeclaration) {
	 FieldDeclaration fd = (FieldDeclaration) bd;
	 if (Modifier.isStatic(fd.getModifiers())) continue;
	 for (Object o1 : fd.fragments()) {
	    VariableDeclarationFragment vdf = (VariableDeclarationFragment) o1;
	    if (vdf.getInitializer() != null) {
	       if (cnstinits == null) cnstinits = new ArrayList<>();
	       cnstinits.add(vdf);
	     }
	  }
       }
    }

   if (!havecnst) {
      MethodDeclaration md = ast.newMethodDeclaration();
      md.setConstructor(true);
      md.setBody(ast.newBlock());
      md.modifiers().addAll(ast.newModifiers(Modifier.PUBLIC));
      md.setName(ast.newSimpleName(td.getName().getIdentifier()));
      td.bodyDeclarations().add(md);
    }

   if (td.getSuperclassType() != null || cnstinits != null) {
      for (MethodDeclaration md : td.getMethods()) {
	 if (!md.isConstructor()) continue;
	 int idx = 0;
	 Block blk = md.getBody();
	 List<Object> stmts = blk.statements();
	 if (stmts.size() > 0) {
	    Statement st0 = (Statement) stmts.get(0);
	    if (st0 instanceof SuperConstructorInvocation ||
		  st0 instanceof ConstructorInvocation) idx = 1;
	  }
	 if (idx == 0 && td.getSuperclassType() != null) {
	    SuperConstructorInvocation sci = ast.newSuperConstructorInvocation();
	    stmts.add(0,sci);
	    idx = 1;
	  }
	 if (cnstinits != null) {
	    for (VariableDeclarationFragment vdf : cnstinits) {
	       Assignment asgn = ast.newAssignment();
	       asgn.setLeftHandSide(ast.newSimpleName(vdf.getName().getIdentifier()));
	       asgn.setRightHandSide((Expression) ASTNode.copySubtree(ast,vdf.getInitializer()));
	       ExpressionStatement es = ast.newExpressionStatement(asgn);
	       stmts.add(idx++,es);
	     }
	  }
       }
    }

   for (TypeDeclaration itd : td.getTypes()) {
      fixType(itd);
    }
}



ASTNode getResolvedAst(SesameProject sp)
{
   ASTNode an = null;
   synchronized (ast_roots) {
      an = ast_roots.get(sp.getName());
      if (an != null && JcompAst.isResolved(an)) return an;
    }
									
   JcompProject proj = sp.getJcompProject();
   proj.resolve();
   JcompSemantics semdata = SesameMain.getJcompBase().getSemanticData(this);
   an = semdata.getAstNode();

   synchronized (ast_roots) {
      ASTNode nan = ast_roots.get(sp.getName());
      if (nan != null) an = nan;
      else ast_roots.put(sp.getName(),an);
    }

   return an;
}


void resetProject(SesameProject sp)
{
   synchronized (ast_roots) {
      ast_roots.remove(sp.getName());
    }
}



Position createPosition(int pos)
{
   Position p = new Position(pos);
   try {
      edit_document.addPosition(p);
    }
   catch (BadLocationException e) {
      return null;
    }

   return p;
}




/********************************************************************************/
/*										*/
/*	JcompSource methods							*/
/*										*/
/********************************************************************************/

@Override public String getFileContents()
{
   return edit_document.get();
}



@Override public String getFileName()
{
   return for_file.getPath();
}


public File getFile()
{
   return for_file;
}


void addUse()
{
   ++use_count;
}


boolean removeUse()
{
   --use_count;
   return use_count <= 0;
}



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return for_file.getAbsolutePath();
}



}	// end of class SesameFile




/* end of SesameFile.java */
