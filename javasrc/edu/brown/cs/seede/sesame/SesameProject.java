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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.xml.IvyXml;

class SesameProject implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameMain      sesame_control;   
private String          project_name;
private List<String>    class_paths;
private Set<SesameFile> active_files;
private JcompProject    base_project;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameProject(SesameMain sm,String name)
{
   sesame_control = sm;
   project_name = name;
   base_project = null;
   class_paths = new ArrayList<String>();
   
   active_files = new HashSet<SesameFile>();
   
   // compute class path for project
   CommandArgs args = new CommandArgs("PATHS",true);
   Element xml = sm.getXmlReply("OPENPROJECT",name,args,null,0);
   if (IvyXml.isElement(xml,"RESULT")) xml = IvyXml.getChild(xml,"PROJECT");
   Element cp = IvyXml.getChild(xml,"CLASSPATH");
   String ignore = null;
   for (Element rpe : IvyXml.children(cp,"PATH")) {
      String bn = null;
      String ptyp = IvyXml.getAttrString(rpe,"TYPE");
      if (ptyp != null && ptyp.equals("SOURCE")) {
         bn = IvyXml.getTextElement(rpe,"OUTPUT");
       }
      else {
         bn = IvyXml.getTextElement(rpe,"BINARY");
       }
      if (bn == null) continue;
      if (bn.endsWith("/lib/rt.jar")) {
         int idx = bn.lastIndexOf("rt.jar");
         ignore = bn.substring(0,idx);
       }
      class_paths.add(bn);
    }
   if (ignore != null) {
      for (Iterator<String> it = class_paths.iterator(); it.hasNext(); ) {
         String nm = it.next();
         if (nm.startsWith(ignore)) it.remove();
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getName()                        { return project_name; }

void addFile(SesameFile sf)
{
   active_files.add(sf);
   clearProject();
}


void removeFile(SesameFile sf)
{
   active_files.remove(sf);
   clearProject();
}


Collection<String> getClassPath()
{
   return class_paths;
}


Collection<SesameFile> getActiveFiles()
{
   return active_files;
}


private synchronized void clearProject()
{
   if (base_project != null) {
      JcompControl jc = sesame_control.getJcompBase();
      jc.freeProject(base_project);
      base_project = null;
    }
}


synchronized JcompProject getJcompProject()
{
   if (base_project != null) return base_project;
   
   JcompControl jc = sesame_control.getJcompBase();
   Collection<JcompSource> srcs = new ArrayList<JcompSource>(active_files);
   base_project = jc.getProject(class_paths,srcs,false);
   
   return base_project;
}

}       // end of class SesameProject




/* end of SesameProject.java */

