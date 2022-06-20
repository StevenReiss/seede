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
import edu.brown.cs.seede.acorn.AcornConstants;
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

SesameFile findOpenFile(File f) 
{
   synchronized (known_files) {
      SesameFile sf = known_files.get(f);
      if (sf != null) return sf;
    }
   
   return null;
}



SesameFile openFile(File f)
{
   return openFile(f,null);
}



SesameFile openFile(File f,String cnts)
{
   f = AcornConstants.getCanonical(f);
   
   synchronized (known_files) {
      SesameFile sf = known_files.get(f);
      if (sf != null) return sf;
    }
   
   String linesep = "\n";
   boolean islcl = false;

   if (f.getPath().contains(LOCAL_FILE_DIR_PREFIX)) {
      File par = f.getParentFile();
      if (par.getName().equals(LOCAL_FILE_DIR_PREFIX) &&
            f.getName().startsWith(LOCAL_FILE_NAME_PREFIX)) {
          if (cnts == null) cnts = "";
//        islcl = true;
          AcornLog.logD("SESAME","LOCAL FILE DETECTED");
       }
    }
   else {
      Map<String,Object> args = new HashMap<>();
      args.put("FILE",f.getPath());
      args.put("CONTENTS",Boolean.TRUE);
      cnts = null;
      Element filerslt = sesame_control.getXmlReply("STARTFILE",null,args,null,0);
      if (!IvyXml.isElement(filerslt,"RESULT")) {
         AcornLog.logE("Can't open file " + f);
         return null;
       }
      linesep = IvyXml.getAttrString(filerslt,"LINESEP");
      byte [] data = IvyXml.getBytesElement(filerslt,"CONTENTS");
      if (data != null && data.length > 0) {
         cnts = new String(data);
       }
    }
      
   if (cnts == null) {
      AcornLog.logE("Can't open file " + f);
      return null;
    }
   
   AcornLog.logD("File started: " + f);

   synchronized (known_files) {
      SesameFile sf = known_files.get(f);
      if (sf == null) {
	 sf = new SesameFile(f,cnts,linesep);
         if (islcl) sf = new SesameFile(sf,true);
	 known_files.put(f,sf);
       }
      return sf;
    }
}


void removeFileUse(SesameFile sf)
{
   if (sf.removeUse()) {
      if (!sf.isLocal()) {
	 File f = sf.getFile();
	 synchronized (known_files) {
	    known_files.remove(f);
	  }
	 Map<String,Object> args = new HashMap<>();
	 args.put("FILE",f.getPath());
	 sesame_control.getStringReply("FINISHFILE",null,args,null,0);

       }
      sf.clear();
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

SesameFile handleEdit(File f,int len,int offset,boolean complete,String txt)
{
   SesameFile sf = known_files.get(f);
   if (sf == null) return null;
   if (complete && txt == null) {
      closeFile(f);
      return null;
    }

   sf.editFile(len,offset,txt,complete);

   return sf;
}



/********************************************************************************/
/*										*/
/*	Error handling methods							*/
/*										*/
/********************************************************************************/

boolean handleErrors(File f,Element msgs)
{
   SesameFile sf = known_files.get(f);
   if (sf == null) return false;
   return sf.handleErrors(msgs);
}



}	// end of class SesameFileManager




/* end of SesameFileManager.java */

