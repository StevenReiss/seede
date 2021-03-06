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

SesameSubsession(SesameSessionLaunch base)
{
   super(base);
   local_project = new SesameSubproject(base.getProject());
   base_session = base;
   AcornLog.logD("Create subsession for " + base_session.getSessionId());
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


}       // end of class SesameSubsession




/* end of SesameSubsession.java */

