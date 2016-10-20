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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


class SesameFile implements SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IDocument               edit_document;
private File                    for_file;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameFile(File f,String cnts)
{
   for_file = f;
   edit_document = new Document(cnts);
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
}




}	// end of class SesameFile




/* end of SesameFile.java */

