/********************************************************************************/
/*										*/
/*		SesameFileManager.java						*/
/*										*/
/*	Manage file buffers							*/
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



package edu.brown.cs.seede.sesame;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornLog;



class SesameFileManager implements SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<File,SesameFile>	known_files;
private SesameMain		sesame_control;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameFileManager(SesameMain sm)
{
   sesame_control = sm;
   known_files = new HashMap<File,SesameFile>();
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

SesameFile openFile(File f)
{
   synchronized (known_files) {
      SesameFile sf = known_files.get(f);
      if (sf != null) return sf;
    }

   Map<String,Object> args = new HashMap<String,Object>();
   args.put("FILE",f.getPath());
   args.put("CONTENTS",Boolean.TRUE);

   Element filerslt = sesame_control.getXmlReply("STARTFILE",null,args,null,0);
   if (!IvyXml.isElement(filerslt,"RESULT")) {
      AcornLog.logE("Can't open file " + f);
      return null;
    }
   String linesep = IvyXml.getAttrString(filerslt,"LINESEP");
   byte [] data = IvyXml.getBytesElement(filerslt,"CONTENTS");
   String cnts = new String(data);

   AcornLog.logD("File " + f + " started");

   synchronized (known_files) {
      SesameFile sf = known_files.get(f);
      if (sf == null) {
	 sf = new SesameFile(f,cnts,linesep);
	 known_files.put(f,sf);
       }
      return sf;
    }
}


void closeFile(File f)
{
   synchronized (known_files) {
      known_files.remove(f);
    }
}



/********************************************************************************/
/*										*/
/*	File Action methods							*/
/*										*/
/********************************************************************************/

void handleEdit(File f,int len,int offset,boolean complete,String txt)
{
   SesameFile sf = known_files.get(f);
   if (sf == null) return;
   if (complete && txt == null) {
      closeFile(f);
      return;
    }
   sf.editFile(len,offset,txt,complete);
}







}	// end of class SesameFileManager




/* end of SesameFileManager.java */

