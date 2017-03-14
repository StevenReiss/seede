/********************************************************************************/
/*										*/
/*		SesameFile.java 						*/
/*										*/
/*	description of class							*/
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornLog;


class SesameFile implements SesameConstants, JcompSource
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
      ast_roots.clear();
      JcompSemantics semdata = SesameMain.getJcompBase().getSemanticData(this);
      try {
	 if (semdata != null && semdata.getProject() != null) semdata.reparse();
       }
      catch (Throwable t) {
	 AcornLog.logE("Problem reparsing",t);
       }
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
/*	Abstract syntax tree methods						*/
/*										*/
/********************************************************************************/

synchronized ASTNode getAst()
{
   ASTNode an = null;
   synchronized (ast_roots) {
      an = ast_roots.get(NO_PROJECT);
      if (an != null) return an;
    }

   ASTParser parser = ASTParser.newParser(AST.JLS4);
   parser.setKind(ASTParser.K_COMPILATION_UNIT);
   parser.setSource(edit_document.get().toCharArray());
   parser.setResolveBindings(false);
   an = parser.createAST(null);

   synchronized (ast_roots) {
      ASTNode nan = ast_roots.get(NO_PROJECT);
      if (nan != null) an = nan;
      else ast_roots.put(NO_PROJECT,an);
    }

   return an;
}



synchronized ASTNode getResolvedAst(SesameProject sp)
{
   ASTNode an = null;
   synchronized (ast_roots) {
      an = ast_roots.get(sp.getName());
      if (an != null) return an;
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



}	// end of class SesameFile




/* end of SesameFile.java */

