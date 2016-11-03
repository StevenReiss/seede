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
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompSource;


class SesameFile implements SesameConstants, JcompSource
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IDocument               edit_document;
private File                    for_file;
private Map<String,ASTNode>     ast_roots;


private static JcompControl     jcomp_base = new JcompControl();

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
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

void editFile(int len,int off,String txt,boolean complete)
{
   if (complete) len = edit_document.getLength();
   try {
      edit_document.replace(off,len,txt);
    }
   catch (BadLocationException e) {
      SesameLog.logE("Problem doing file edit",e);
    }
   
   synchronized (ast_roots) {
      ast_roots.clear();
    }
}



/********************************************************************************/
/*                                                                              */
/*      Abstract syntax tree methods                                            */
/*                                                                              */
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
   JcompSemantics semdata = jcomp_base.getSemanticData(this);
   an = semdata.getAstNode();
   
   synchronized (ast_roots) {
      ASTNode nan = ast_roots.get(sp.getName());
      if (nan != null) an = nan;
      else ast_roots.put(sp.getName(),an);
    }
   
   return an;
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
/*                                                                              */
/*      JcompSource methods                                                     */
/*                                                                              */
/********************************************************************************/

@Override public String getFileContents()
{
   return edit_document.get();
}



@Override public String getFileName()
{
   return for_file.getPath();
}



}	// end of class SesameFile




/* end of SesameFile.java */

