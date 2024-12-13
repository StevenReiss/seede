/********************************************************************************/
/*                                                                              */
/*              AcornConstants.java                                             */
/*                                                                              */
/*      General constants for SEEDE                                             */
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



package edu.brown.cs.seede.acorn;

import java.io.File;
import java.io.IOException;

public interface AcornConstants
{


static File getCanonical(File f)
{
   if (!f.exists()) return f;
   
   try {
      f = f.getCanonicalFile();
    }
   catch (IOException e) {
      AcornLog.logE("ACORN","Problem getting canonical file " + f,e);
    }
   return f;
}



static File getCanonical(String s)
{
   if (s == null) return null;
   
   File f = new File(s);
   
   return getCanonical(f);
}


}       // end of interface AcornConstants




/* end of AcornConstants.java */

