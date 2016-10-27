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

import java.util.HashMap;
import java.util.Map;
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

static SesameSession createSession(SesameMain sm,String sid,Element xml)
{
   SesameSession ss = null;
   
   String typ = IvyXml.getAttrString(xml,"TYPE");
   
   if (typ.equals("LAUNCH")) {
      ss = new SesameSessionLaunch(sm,sid,xml);
    }
   else if (typ.equals("TEST")) {
      ss = new SesameSessionTest(sm,sid,xml);
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
private SesameProject   for_project;
private Map<String,SesameLocation> location_map; 



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected SesameSession(SesameMain sm,String sid,Element xml)
{
   sesame_control = sm;
   
   String proj = IvyXml.getAttrString(xml,"PROJECT");
   for_project = sm.getProject(proj);
   
   if (sid == null) {
      Random r = new Random();
      sid = "SESAME_" + r.nextInt(10000000);
    }
   session_id = sid;
   
   location_map = new HashMap<String,SesameLocation>();
   for (Element locxml : IvyXml.children(xml,"LOCATION")) {
      SesameLocation sloc = new SesameLocation(sm,locxml);
      location_map.put(sloc.getId(),sloc);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getSessionId()                   { return session_id; }

SesameProject getProject()              { return for_project; }




}       // end of class SesameSession




/* end of SesameSession.java */

