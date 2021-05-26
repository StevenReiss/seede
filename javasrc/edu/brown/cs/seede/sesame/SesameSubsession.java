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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.TextEdit;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
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
private Map<Position,Position> position_map;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSubsession(SesameSessionLaunch base,Element xml)
{
   super(base);
   local_project = new SesameSubproject(base.getProject());
   base_session = base;
   if (IvyXml.getAttrBool(xml,"SHOWALL")) setShowAll(true);
   position_map = new HashMap<>();
   AcornLog.logD("Create subsession for " + base_session.getSessionId());
}



/********************************************************************************/
/*                                                                              */
/*      Cleanup methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override void removeSession()
{
   super.removeSession();
   local_project = null;
   base_session = null;
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
   
   editFileSetup(editfile);
   editfile.editFile(len,off,txt,false);
   editFileFixup(editfile);
   
   noteFileChanged(editfile);
   
   return editfile;
}


SesameFile editLocalFile(File f,TextEdit te)
{
   if (te == null) return null;
   
   SesameFile editfile = getLocalFile(f);
   if (editfile == null) return null;
   
   editFileSetup(editfile);
   editfile.editFile(te);
   editFileFixup(editfile);
   
   noteFileChanged(editfile);
   return editfile;
}



private void editFileSetup(SesameFile sf)
{
   for (SesameLocation loc : super.getActiveLocations()) {
      if (loc.getFile().getFile().equals(sf.getFile())) {
         Position pos = sf.createPosition(loc.getStartPosition().getOffset());
         position_map.put(loc.getStartPosition(),pos); 
       }
    }
}


private void editFileFixup(SesameFile sf)
{
   
}

@Override public List<SesameLocation> getActiveLocations()
{
   List<SesameLocation> rslt = super.getActiveLocations();
   List<SesameLocation> nrslt = new ArrayList<>();
   AcornLog.logD("START LOCATIONS " + rslt.size());

   for (SesameLocation loc : rslt) {
      AcornLog.logD("WORK ON LOCATION " + loc);
      SesameFile sf = local_project.findLocalFile(loc.getFile().getFile());
      if (sf != loc.getFile()) {
         Position pos0 = loc.getStartPosition();
         int lno = loc.getLineNumber();
         Position pos1 = position_map.get(pos0);
         int olno = loc.getFile().getLineOfPosition(pos0);
         int nlno = sf.getLineOfPosition(pos1);
         AcornLog.logD("FILES " + sf + " " + loc.getFile() + " " + (sf == loc.getFile()) + " " + 
               lno + " " + nlno + " " + olno + " " + ((lno + (nlno-olno))));
         if (nlno == 0) nlno = olno;
         SesameLocation nloc = new SesameLocation(sf,loc.getMethodName(),
               lno + (nlno-olno),loc.getThread(),loc.getThreadName());
         nloc.setActive(true);
         nrslt.add(nloc);
       }
      else nrslt.add(loc);
    }
   
   AcornLog.logD("END LOCATION " + nrslt.size());
   return nrslt; 
}




/********************************************************************************/
/*                                                                              */
/*      Behavioral methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override void resetCache()
{ 
   // do nothing -- change types as needed
}



}       // end of class SesameSubsession




/* end of SesameSubsession.java */

