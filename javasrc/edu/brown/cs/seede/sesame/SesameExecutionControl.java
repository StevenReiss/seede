/********************************************************************************/
/*                                                                              */
/*              SesameExecutionControl.java                                     */
/*                                                                              */
/*      Control for a single execution environment                              */
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

import org.eclipse.jface.text.Position;

class SesameExecutionControl implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          launch_id;
private File            for_file;
private String          method_name;
private Position        start_position;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameExecutionControl(String launch,File file,String method)
{
   launch_id = launch;
   for_file = file;
   method_name = method;
   start_position = null;
}




}       // end of class SesameExecutionControl




/* end of SesameExecutionControl.java */

