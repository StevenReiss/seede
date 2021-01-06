/********************************************************************************/
/*                                                                              */
/*              SesameSubproject.java                                           */
/*                                                                              */
/*      Subproject to allow private editing of files                            */
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
import java.util.HashMap;
import java.util.Map;

class SesameSubproject extends SesameProject
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameProject parent_project;
private Map<File,SesameFile> local_files;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSubproject(SesameProject sp)
{
   super(sp);
   parent_project = sp;
   local_files = new HashMap<>();
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

SesameFile getLocalFile(File f)
{
   if (f == null) return null;
   SesameFile sf = local_files.get(f);
   if (sf != null) return sf;
   
   SesameFile newf = null;
   synchronized(local_files) {
      newf = localizeFile(f);
      if (newf == null) return null;
      local_files.put(f,newf);
    }
   
   return newf;
}


protected SesameFile findFile(File f)
{
   SesameFile sf = local_files.get(f);
   if (sf != null) return sf;
   
   return parent_project.findFile(f);
}



}       // end of class SesameSubproject




/* end of SesameSubproject.java */

