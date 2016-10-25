/********************************************************************************/
/*                                                                              */
/*              SesameProject.java                                              */
/*                                                                              */
/*      Hold information about an Bubbles project for compilation               */
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

import java.util.List;

class SesameProject implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameMain      sesame_control;   
private String          project_name;
private List<String>    jar_files;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameProject(SesameMain sm,String name)
{
   sesame_control = sm;
   
   CommandArgs args = new CommandArgs("NAME",
   sm.getXmlReply("OPENPROJECT",

}       // end of class SesameProject




/* end of SesameProject.java */

