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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

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
private ASTNode                 file_ast;

private static JcompControl     jcomp_base = new JcompControl();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameFile(File f,String cnts)
{
   for_file = f;
   edit_document = new Document(cnts);
   file_ast = null;
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
   
   file_ast = null;
}



/********************************************************************************/
/*                                                                              */
/*      Abstract syntax tree methods                                            */
/*                                                                              */
/********************************************************************************/

synchronized ASTNode getAST()
{
   ASTNode an = file_ast;
   if (an != null) return an;
   
   List<JcompSource> srcs = new ArrayList<JcompSource>();
   srcs.add(this);
   JcompProject proj = jcomp_base.getProject(srcs);
   proj.resolve();
   JcompSemantics semdata = jcomp_base.getSemanticData(this);
   file_ast = semdata.getAstNode();
   jcomp_base.freeProject(proj);
   
   return null;
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

