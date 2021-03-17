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

class SesameSubproject extends SesameProject
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/
 


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSubproject(SesameProject sp)
{
   super(sp);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

boolean isLocal()                               { return true; }


SesameFile getLocalFile(File f)
{
   SesameFile sf = findLocalFile(f);
   if (sf != null && sf.isLocal()) return sf;
   
   return localizeFile(f);
}




}       // end of class SesameSubproject




/* end of SesameSubproject.java */

