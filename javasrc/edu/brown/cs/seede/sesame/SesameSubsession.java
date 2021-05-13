/********************************************************************************/
/*                                                                              */
/*              SesameSubsession.java                                           */
/*                                                                              */
/*      Subsession to allow private editing of files                            */
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

import org.eclipse.text.edits.TextEdit;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornLog;

class SesameSubsession extends SesameSessionLaunch
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameSubproject   local_project; 
private SesameSessionLaunch base_session;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSubsession(SesameSessionLaunch base,Element xml)
{
   super(base);
   local_project = new SesameSubproject(base.getProject());
   base_session = base;
   if (IvyXml.getAttrBool(xml,"SHOWALL")) setShowAll(true);
   AcornLog.logD("Create subsession for " + base_session.getSessionId());
}



/********************************************************************************/
/*                                                                              */
/*      Cleanup methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override void removeSession()
{
   super.removeSession();
   local_project = null;
   base_session = null;
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

SesameSubsession getSubsession()                { return this; }

@Override public SesameProject getProject()     { return local_project; }


SesameFile getLocalFile(File file)
{
   if (file == null) return null;
   
   return local_project.getLocalFile(file);
}


SesameFile editLocalFile(File f,int len,int off,String txt)
{
   SesameFile editfile = getLocalFile(f);
   if (editfile == null) return null;
   editfile.editFile(len,off,txt,false);
   
   noteFileChanged(editfile);
   
   return editfile;
}


SesameFile editLocalFile(File f,TextEdit te)
{
   if (te == null) return null;
   
   SesameFile editfile = getLocalFile(f);
   if (editfile == null) return null;
   editfile.editFile(te);
   noteFileChanged(editfile);
   return editfile;
}



/********************************************************************************/
/*                                                                              */
/*      Behavioral methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override void resetCache()
{ 
   // do nothing -- change types as needed
}



}       // end of class SesameSubsession




/* end of SesameSubsession.java */

