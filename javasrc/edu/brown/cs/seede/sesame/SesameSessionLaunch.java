/********************************************************************************/
/*                                                                              */
/*              SesameSessionLaunch.java                                        */
/*                                                                              */
/*      Session based on a debugger launch                                      */
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

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

class SesameSessionLaunch extends SesameSession
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          launch_id;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSessionLaunch(SesameMain sm,String sid,Element xml)
{
   super(sm,sid,xml);
   
   launch_id = IvyXml.getAttrString(xml,"LAUNCHID");
   
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/



}       // end of class SesameSessionLaunch




/* end of SesameSessionLaunch.java */

