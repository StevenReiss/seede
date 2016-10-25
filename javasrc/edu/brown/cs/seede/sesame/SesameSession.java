/********************************************************************************/
/*                                                                              */
/*              SesameSession.java                                              */
/*                                                                              */
/*      Abstarct representation of a evaluation session                         */
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
import java.util.Random;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

abstract class SesameSession implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Factory methods                                                         */
/*                                                                              */
/********************************************************************************/

static SesameSession createSession(SesameMain sm,Element xml)
{
   SesameSession ss = null;
   
   if (IvyXml.isElement(xml,"LAUNCH")) {
      ss = new SesameSessionLaunch(sm,xml);
    }
   else if (IvyXml.isElement(xml,"TEST")) {
      ss = new SesameSessionTest(sm,xml);
    }
   
   return ss;
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected SesameMain    sesame_control;
private String          session_id;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected SesameSession(SesameMain sm)
{
   this(sm,(String) null);
}


protected SesameSession(SesameMain sm,Element xml)
{
   this(sm,IvyXml.getAttrString(xml,"ID"));
}
   

protected SesameSession(SesameMain sm,String id)
{
   sesame_control = sm;
   if (id == null) {
      Random r = new Random();
      id = "SESAME_" + r.nextInt(10000000);
    }
   session_id = id;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getSessionId()                   { return session_id; }




}       // end of class SesameSession




/* end of SesameSession.java */

