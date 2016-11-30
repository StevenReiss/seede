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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.cashew.CashewValue;

public abstract class SesameSession implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Factory methods                                                         */
/*                                                                              */
/********************************************************************************/

static SesameSession createSession(SesameMain sm,String sid,Element xml) throws SesameException
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
private Set<Thread>     exec_runners;



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
      addLocation(sloc);
    }
   
   exec_runners = new HashSet<Thread>();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public String getSessionId()                    { return session_id; }

public SesameProject getProject()               { return for_project; }

public MethodDeclaration getCallMethod()   
{
   for (SesameLocation loc : location_map.values()) {
      if (loc.isActive()) {
         // need to find AST for the method
         // need to find JcompSymbol for the method
         SesameFile sf = loc.getFile();
         ASTNode root = sf.getResolvedAst(for_project);
         ASTNode mnode = JcompAst.findNodeAtOffset(root,loc.getStartPositiion().getOffset());
         while (!(mnode instanceof MethodDeclaration)) {
            mnode = mnode.getParent();
          }
         return (MethodDeclaration) mnode;
       }
    }
   return null;
}

public List<CashewValue> getCallArgs()          { return null; }



SesameMain getControl()                         { return sesame_control; }

protected void addLocation(SesameLocation sl)
{
   location_map.put(sl.getId(),sl);
   for_project.addFile(sl.getFile());
}




/********************************************************************************/
/*                                                                              */
/*      Runner management methods                                               */
/*                                                                              */
/********************************************************************************/

synchronized void addRunner(Thread th)
{
   stopRunners();
   
   exec_runners.add(th);
}



synchronized void removeRunner(Thread th)
{
   exec_runners.remove(th);
}


synchronized void stopRunners()
{
   for (Thread run : exec_runners) {
      run.interrupt();
    }
}



/********************************************************************************/
/*                                                                              */
/*      Methods to get global values from underlying execution                  */
/*                                                                              */
/********************************************************************************/

CashewValue lookupValue(String name,String type)
{ 
   return null;
}


CashewValue evaluate(String expr)
{
   return null;
}


void enableAccess(String type)          { }



}       // end of class SesameSession




/* end of SesameSession.java */

