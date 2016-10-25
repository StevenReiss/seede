/********************************************************************************/
/*                                                                              */
/*              SesameLocation.java                                             */
/*                                                                              */
/*      Hold a location to be evaluated                                         */
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

class SesameLocation implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameMain      sesame_control;
private SesameFile      sesame_file;
private int             line_number;
private String          method_name;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameLocation(SesameMain sm,File f,int line,String method,String sign)
{
   sesame_control = sm;
   sesame_file = sm.getFileManager().openFile(f);
   
  
}

}       // end of class SesameLocation




/* end of SesameLocation.java */

