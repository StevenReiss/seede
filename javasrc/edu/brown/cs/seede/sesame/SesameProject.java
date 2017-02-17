/********************************************************************************/
/*										*/
/*		SesameProject.java						*/
/*										*/
/*	Hold information about an Bubbles project for compilation		*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.seede.sesame;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcode.JcodeFactory;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cumin.CuminConstants.CuminProject;

public class SesameProject implements SesameConstants, CuminProject
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		project_name;
private List<String>	class_paths;
private Set<SesameFile> active_files;
private JcompProject	base_project;
private JcodeFactory	binary_control;
private SesameMain	sesame_control;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameProject(SesameMain sm,String name)
{
   project_name = name;
   sesame_control = sm;
   base_project = null;
   class_paths = new ArrayList<String>();

   active_files = new HashSet<SesameFile>();

   boolean havepoppy = false;

   // compute class path for project
   CommandArgs args = new CommandArgs("PATHS",true);
   Element xml = sm.getXmlReply("OPENPROJECT",this,args,null,0);
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
      if (bn.contains("poppy.jar")) havepoppy = true;
      class_paths.add(bn);
    }
   if (ignore != null) {
      for (Iterator<String> it = class_paths.iterator(); it.hasNext(); ) {
	 String nm = it.next();
	 if (nm.startsWith(ignore)) it.remove();
       }
    }
   if (!havepoppy) {
      File poppylib = new File("/pro/seede/lib");
      if (!poppylib.exists()) poppylib = new File("/research/people/spr/seede/lib");
      File poppyjar = new File(poppylib,"poppy.jar");

      CommandArgs args2 = new CommandArgs("LOCAL",true);
      IvyXmlWriter xwp = new IvyXmlWriter();
      xwp.begin("PROJECT");
      xwp.field("NAME",getName());
      xwp.begin("PATH");
      xwp.field("TYPE","LIBRARY");
      xwp.field("NEW",true);
      xwp.field("BINARY",poppyjar.getPath());
      xwp.field("EXPORTED",false);
      xwp.field("OPTIONAL",true);
      xwp.end("PATH");
      xwp.end("PROJECT");
      String cnts = xwp.toString();
      xwp.close();
      Element rslt = sesame_control.getXmlReply("EDITPROJECT",this,args2,cnts,0);
      if (!IvyXml.isElement(rslt,"RESULT")) {
	 AcornLog.logE("Problem adding poppy to path");
       }
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getName()			{ return project_name; }

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
      JcompControl jc = SesameMain.getJcompBase();
      jc.freeProject(base_project);
      base_project = null;
    }
}


@Override public synchronized JcompProject getJcompProject()
{
   if (base_project != null) return base_project;

   JcompControl jc = SesameMain.getJcompBase();
   Collection<JcompSource> srcs = new ArrayList<JcompSource>(active_files);
   base_project = jc.getProject(class_paths,srcs,false);

   return base_project;
}




@Override public synchronized JcodeFactory getJcodeFactory()
{
   if (binary_control != null) return binary_control;

   int ct = Runtime.getRuntime().availableProcessors();
   ct = Math.max(1,ct/2);
   JcodeFactory jf = new JcodeFactory(ct);
   for (String s : class_paths) {
      jf.addToClassPath(s);
    }
   jf.load();
   binary_control = jf;

   return binary_control;
}



/********************************************************************************/
/*										*/
/*	Context methods 							*/
/*										*/
/********************************************************************************/

@Override public JcompTyper getTyper()
{
   getJcompProject();
   if (base_project == null) return null;
   base_project.resolve();
   Collection<JcompSemantics> srcs = base_project.getSources();
   for (JcompSemantics js : srcs) {
      ASTNode an = js.getRootNode();
      JcompTyper jt = JcompAst.getTyper(an);
      if (jt != null) return jt;
    }

   return null;
}

}	// end of class SesameProject




/* end of SesameProject.java */

