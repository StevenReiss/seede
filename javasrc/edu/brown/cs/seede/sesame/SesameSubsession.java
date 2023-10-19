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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.TextEdit;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
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
private SesameLocation      call_location;



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
   call_location = null;
   Element locxml = IvyXml.getChild(xml,"LOCATION");
   if (locxml != null) {
      call_location = new SesameLocation(sesame_control,locxml);
      call_location.setThread(base.getAnyThread());
      call_location.setActive(true);
    }
   AcornLog.logD("SESAME","Subsession " + getSessionId() + " project " +
         local_project.hashCode());
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
   position_map.clear();
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

SesameSubsession getSubsession()                { return this; }

Object getSessionKey()
{
   return base_session.getSessionKey();
}

@Override public SesameProject getProject()     { return local_project; }


SesameFile getLocalFile(File file)
{
   if (file == null) return null;
   
   return local_project.getLocalFile(file);
}



/********************************************************************************/
/*                                                                              */
/*      Editing methods                                                         */
/*                                                                              */
/********************************************************************************/

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
   if (te == null) {
      AcornLog.logE("SESAME","No edit given");
      return null;
    }
   
   SesameFile editfile = getLocalFile(f);
   if (editfile == null) {
      AcornLog.logE("SESAME","Can't find local file " + f);
      return null;
    }   
   editFileSetup(editfile);
   editfile.editFile(te,getSessionId());
   editFileFixup(editfile);
   
   noteFileChanged(editfile);
   
   AcornLog.logD("SESAME","Done edit of " + getSessionId() + " " + editfile);
   return editfile;
}


SesameFile editLocalFile(SesameFile sf,ASTRewrite rw)
{
   sf.editFile(rw);
   noteFileChanged(sf);
   return sf;
}



private void editFileSetup(SesameFile sf)
{
   for (SesameLocation loc : super.getActiveLocations()) {
      if (loc.getFile().getFile().equals(sf.getFile())) {
         Position pos = sf.createPosition(loc.getStartPosition().getOffset());
         AcornLog.logD("SESAME","SETUP LOCATION " + 
               loc.getStartPosition() + " " + loc.getStartPosition().getOffset() + " " +
               pos);
         position_map.put(loc.getStartPosition(),pos); 
       }
    }
}



private void editFileFixup(SesameFile sf)
{
   // nothing to do for now - positions will be needed later
}






/********************************************************************************/
/*                                                                              */
/*      Handle initializations                                                  */
/*                                                                              */
/********************************************************************************/

@SuppressWarnings("unchecked")
SesameFile handleInitialization(String thread,String expr,boolean rem)
{
   SesameLocation useloc = null;
   for (SesameLocation loc : getActiveLocations()) {
      if (loc.getThread().equals(thread) || loc.getThreadName().equals(thread)) {
         useloc = loc;
         break;
       }
    }
   if (useloc == null) return null;
   
   SesameFile sf = getLocalFile(useloc.getFile().getFile());
   if (sf == null) return null;
   if (sf != useloc.getFile()) {
      if (expr == null) return null;
      // we created a new local file -- restart to get proper lcoation for it
      return handleInitialization(thread,expr,rem);
    }
   
   MethodDeclaration md = getCallMethod(useloc);
   AST ast = md.getAST();
   Block b = md.getBody();
   Block initblk = findInitBlock(md,b);
   
   if (expr == null) {
      if (rem && initblk != null) {
         b.statements().remove(initblk);
       }   
      return null;
    }
   
   Expression exprast = JcompAst.parseExpression(expr);
   if (exprast == null) return null;
   exprast = (Expression) ASTNode.copySubtree(ast,exprast);
   Statement exprstmt = ast.newExpressionStatement(exprast);
   if (b == null) return null;
   ASTRewrite rw = ASTRewrite.create(ast);
   if (initblk == null) {
      initblk = md.getAST().newBlock();
      initblk.statements().add(ast.newEmptyStatement());
      initblk.statements().add(ast.newEmptyStatement());
      initblk.statements().add(exprstmt);
      initblk = addInitBlock(md,initblk,rw);
    }
   else {
      ListRewrite lrw = rw.getListRewrite(initblk,Block.STATEMENTS_PROPERTY);
      lrw.insertLast(exprstmt,null);
    }
   
   editLocalFile(sf,rw);
   
   return sf;
}



private Block findInitBlock(MethodDeclaration md,Block b)
{
   for (Object o : b.statements()) {
      if (o instanceof Block) {
         Block tryblk = (Block) o;
         if (isInitBlock(tryblk)) return tryblk;
       }
    }
   
   return null;
}


private Block addInitBlock(MethodDeclaration md,Block newblk,ASTRewrite rw)
{
   Block b = md.getBody();
   int idx = 0;
   Statement before = null;
   for (Object o : b.statements()) {
      if (before == null) {
         if (md.isConstructor()) {
            if (o instanceof ConstructorInvocation || o instanceof SuperConstructorInvocation) 
               ++idx;
          }
         before = (Statement) o;
       }
    }
   
   ListRewrite lrw = rw.getListRewrite(b,Block.STATEMENTS_PROPERTY);
   lrw.insertAt(newblk,idx,null);
   
   return newblk;
}



private boolean isInitBlock(Block b)
{
   int ctr = 0;
   for (Object o : b.statements()) {
      if (o instanceof EmptyStatement) {
         if (++ctr == 2) return true;
       }
      else break;
    }
   
   return false;
}



/********************************************************************************/
/*                                                                              */
/*      Locations methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public List<SesameLocation> getActiveLocations()
{
   List<SesameLocation> rslt = super.getActiveLocations();
   List<SesameLocation> nrslt = new ArrayList<>();
   
   if (call_location != null) {
      rslt.clear();
      rslt.add(call_location);
    }
   
   AcornLog.logD("START LOCATIONS " + rslt.size());
   
   for (SesameLocation loc : rslt) {
      AcornLog.logD("SESAME","WORK ON LOCATION " + loc + " " + loc.getLineNumber() + " " +
          loc.getStartPosition());
      SesameFile sf = local_project.findLocalFile(loc.getFile().getFile());
      if (sf != loc.getFile()) {
         Position pos0 = loc.getStartPosition();
         int lno = loc.getLineNumber();
         Position pos1 = position_map.get(pos0);
         int olno = loc.getFile().getLineOfPosition(pos0);
         int nlno = sf.getLineOfPosition(pos1);
         if (nlno == 0) nlno = olno;
         AcornLog.logD("SESAME","CREATE LOCATION " + loc.getMethodName() + " " + sf + 
               " " + loc.getThread() + " " + loc.getThreadName());
         SesameLocation nloc = new SesameLocation(sf,loc.getMethodName(),
               lno + (nlno-olno),loc.getThread(),loc.getThreadName());
         nloc.setActive(true);
         nrslt.add(nloc);
       }
      else {
         nrslt.add(loc);
       }
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

