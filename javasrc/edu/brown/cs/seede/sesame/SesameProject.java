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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.seede.acorn.AcornConstants;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cumin.CuminConstants.CuminProject;

public class SesameProject implements SesameConstants, CuminProject
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected SesameMain	sesame_control;
private String		project_name;
private List<String>	class_paths;
private Set<SesameFile> active_files;
private Set<SesameFile> changed_files;
private JcompProject	base_project;
private JcodeFactory	binary_control;
private ReadWriteLock	project_lock;
private Map<File,SesameFile> local_files;

static SesameProject NO_PROJECT = new SesameProject();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameProject(SesameMain sm,String name)
{
   sesame_control = sm;
   project_name = name;
   base_project = null;
   class_paths = new ArrayList<String>();
   local_files = null;

   active_files = new HashSet<SesameFile>();
   changed_files = new HashSet<SesameFile>();

   project_lock = new ReentrantReadWriteLock();

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
      if (bn.endsWith("/lib/jrt-fs.jar")) {
	 int idx = bn.lastIndexOf("/lib/jrt-fs.jar");
	 ignore = bn.substring(0,idx);
       }
      if (bn.contains("poppy.jar")) havepoppy = true;
      if (IvyXml.getAttrBool(rpe,"SYSTEM")) continue;
      class_paths.add(bn);
    }
   if (ignore != null) {
      for (Iterator<String> it = class_paths.iterator(); it.hasNext(); ) {
	 String nm = it.next();
	 if (nm.startsWith(ignore)) it.remove();
       }
    }
   if (!havepoppy) {
      AcornLog.logE("POPPY.JAR missing from class path");
    }
}



SesameProject(SesameProject par)
{
   sesame_control = par.sesame_control;
   project_name = par.project_name;
   base_project = null;
   class_paths = new ArrayList<>();
   binary_control = par.binary_control;

   active_files = new HashSet<>();
   changed_files = new HashSet<>();
   project_lock = new ReentrantReadWriteLock();
   class_paths = par.class_paths;

   local_files = new HashMap<>();

   for (SesameFile sf : par.getActiveFiles()) {
      SesameFile nsf = new SesameFile(sf,false);
      local_files.put(sf.getFile(),nsf);
      addFile(nsf);
    }
}



private SesameProject()
{
   sesame_control = null;
   project_name = "*NOPROJECT*";
   class_paths = null;
   active_files = null;
   changed_files = null;
   base_project = null;
   binary_control = null;
   project_lock = null;
   local_files = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean isLocal()			{ return false; }

String getName()			{ return project_name; }

void addFile(SesameFile sf)
{
   if (sf != null && active_files.add(sf)) {
      noteFileChanged(sf,false);
      sf.addUse();
    }
}


boolean removeFile(SesameFile sf)
{
   if (active_files.remove(sf)) {
      AcornLog.logD("SESAME","File removed " + sf + " " + hashCode());
      noteFileChanged(sf,true);
      if (local_files != null && local_files.get(sf.getFile()) == null) {
	 sesame_control.getFileManager().removeFileUse(sf);
       }
      return true;
    }
   else {
      AcornLog.logD("SESAME","Failed to remove file " +
	    sf + " " + hashCode());
      return false;
    }
}


protected SesameFile findFile(File f)
{
   f = AcornConstants.getCanonical(f);

   if (local_files != null) {
      synchronized (local_files) {
	 SesameFile sf = local_files.get(f);
	 if (sf != null) return sf;
       }
    }

   return sesame_control.getFileManager().openFile(f);
}



protected SesameFile findLocalFile(File f)
{
   if (f == null || local_files == null) return null;
   return local_files.get(f);
}



protected SesameFile localizeFile(File f)
{
   if (f == null) return null;

   SesameFile sf = findFile(f);

   if (sf == null) {
      AcornLog.logD("SESAME","File " + f + " not found");
    }

   if (sf != null && sf.isLocal() && local_files != null) {
      local_files.put(f,sf);
      active_files.add(sf);
      sf.addUse();
      return sf;
    }

   if (sf == null || sf.isLocal() || local_files == null) {
      return sf;
    }

   SesameFile newfile = null;

   synchronized (local_files) {
      newfile = new SesameFile(sf,true);
      if (!removeFile(sf)) {
	 for (Iterator<SesameFile> it = active_files.iterator(); it.hasNext(); ) {
	    SesameFile sf1 = it.next();
	    AcornLog.logD("SESAME","Check active file " + sf1 + " " + sf);
	    if (sf1.getFile().equals(f)) {
	       AcornLog.logD("SESAME","Remove active file " + sf1);
	       it.remove();
	     }
	  }
       }
      local_files.put(f,newfile);
      active_files.add(newfile);
      newfile.addUse();
      base_project = null;
      if (changed_files.remove(sf)) changed_files.add(newfile);
    }

   AcornLog.logD("SESAME","Localize file " + f + "->" + newfile + " " + newfile.hashCode() + " " + hashCode());
   return newfile;
}


Collection<String> getClassPath()
{
   return class_paths;
}


Collection<SesameFile> getActiveFiles()
{
   return active_files;
}



/********************************************************************************/
/*										*/
/*	Locking methods 							*/
/*										*/
/********************************************************************************/

void executionLock()
{
   project_lock.readLock().lock();
}



void executionUnlock()
{
   project_lock.readLock().unlock();
}




/********************************************************************************/
/*										*/
/*	Compilation related methods						*/
/*										*/
/********************************************************************************/

boolean noteFileChanged(SesameFile sf,boolean force)
{
   if (!force && !active_files.contains(sf)) return false;

   project_lock.readLock().lock();
   try {
      if (force || active_files.contains(sf)) {
	 synchronized (changed_files) {
	    if (sf != null) changed_files.add(sf);
	  }
	 return true;
       }
    }
   finally {
      project_lock.readLock().unlock();
    }

   return false;
}


void compileProject()
{
   List<SesameFile> newfiles = null;
   synchronized (changed_files) {
      newfiles = new ArrayList<>(changed_files);
      changed_files.clear();
    }

   if (!newfiles.isEmpty()) {
      project_lock.writeLock().lock();
      try {
	 for (SesameFile sf : newfiles) {
	    sf.resetSemantics(this);
	  }
	 clearProject();
	 getJcompProject();
	 getTyper();			// this resolves the project
       }
      finally {
	 project_lock.writeLock().unlock();
       }
    }
}


synchronized void clearProject()
{
   if (base_project != null) {
      JcompControl jc = SesameMain.getJcompBase();
      jc.freeProject(base_project);
      base_project = null;
      for (SesameFile sf : active_files) {
	 sf.resetProject(this);
       }
    }
}


synchronized void removeProject()
{
   clearProject();
   base_project = null;
   List<SesameFile> rem = new ArrayList<>(active_files);
   for (SesameFile sf : rem) removeFile(sf);
   active_files = null;
   changed_files = null;
   class_paths = null;
   for (SesameFile sf : local_files.values()) {
      if (!rem.contains(sf)) {
	 removeFile(sf);
       }
    }
   local_files = null;
}


@Override public synchronized JcompProject getJcompProject()
{
   if (base_project != null) return base_project;

   JcompControl jc = SesameMain.getJcompBase();
   Collection<JcompSource> srcs = new ArrayList<>(active_files);
  //  base_project = jc.getProject(class_paths,srcs,false);
   AcornLog.logD("SESAME","Create Jcomp project for " + hashCode() + " " +
	 srcs);

   base_project = jc.getProject(getJcodeFactory(),srcs);
   base_project.setProjectKey(this);

   AcornLog.logD("SESAME","Jcomp project for " + hashCode() + " = " +
	 base_project.hashCode());

   return base_project;
}




@Override public synchronized JcodeFactory getJcodeFactory()
{
   if (binary_control != null) return binary_control;

   int ct = Runtime.getRuntime().availableProcessors();
   ct = Math.max(1,ct/2);
   // ct = 1;			// for debugging only
   JcodeFactory jf = new JcodeFactory(ct);
   for (String s : class_paths) {
      AcornLog.logD("SESAME","Add to class path " + s);
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
