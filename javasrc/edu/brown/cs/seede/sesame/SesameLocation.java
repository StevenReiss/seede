/********************************************************************************/
/*                                                                              */
/*              SesameLocation.java                                             */
/*                                                                              */
/*      Hold a location to be evaluated                                         */
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.Position;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornConstants;

class SesameLocation implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          location_id;
private SesameFile      sesame_file;
private Position        start_position;
private int             line_number;
private String          method_name;
private boolean         is_active;
private String          thread_id;
private String          thread_name;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameLocation(SesameMain sm,Element xml)
{
   location_id = IvyXml.getAttrString(xml,"ID");
   if (location_id == null) {
      location_id = "L_" + new Random().nextInt(10000000);
    }
   String fnm = IvyXml.getAttrString(xml,"FILE");
   File f = AcornConstants.getCanonical(fnm);
   
   sesame_file = sm.getFileManager().openFile(f);
   line_number = IvyXml.getAttrInt(xml,"LINE");
   method_name = IvyXml.getAttrString(xml,"METHOD");
   thread_id = IvyXml.getAttrString(xml,"THREAD");
   thread_name = IvyXml.getAttrString(xml,"THREADNAME","Thread_" + thread_id);
   String cnm = IvyXml.getAttrString(xml,"CLASS");
   if (cnm != null) method_name = cnm + "." + method_name;
   String sign = IvyXml.getAttrString(xml,"SIGNATURE");
   if (sign != null) method_name += sign;
   is_active = IvyXml.getAttrBool(xml,"ACTIVE");
   
   start_position = null;
   if (sesame_file == null) return;
   setupPosition();
}



SesameLocation(SesameFile sf,String method,int lno,String threadid,String threadname)
{
   location_id =  "L_" + new Random().nextInt(10000000);
   sesame_file = sf;
   line_number = lno;
   method_name = method;
   thread_id = threadid;
   thread_name = threadname;
   is_active = true;
   start_position = null;
   setupPosition();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getId()                  { return location_id; }
SesameFile getFile()            { return sesame_file; }
boolean isActive()              { return is_active; }
void setActive(boolean a)       { is_active = a; }
String getMethodName()          { return method_name; } 
int getLineNumber()             { return line_number; }
Position getStartPosition()     { return start_position; }
String getThread()              { return thread_id; }
String getThreadName()          { return thread_name; }

void setThread(String id)       { thread_id = id; }




/********************************************************************************/
/*                                                                              */
/*      Compute file position                                                   */
/*                                                                              */
/********************************************************************************/

private void setupPosition()
{
   if (sesame_file == null) return;
   
   ASTNode root = sesame_file.getAst(); 
   ASTNode posnode = JcompAst.findNodeAtLine(root,line_number);
   while (posnode != null) {
      ASTNode next = posnode.getParent();
      if (posnode instanceof Statement) next = null;
      else {
         switch (posnode.getNodeType()) {
            case ASTNode.METHOD_DECLARATION :
            case ASTNode.FIELD_DECLARATION :
            case ASTNode.COMPILATION_UNIT :
            case ASTNode.TYPE_DECLARATION :
            case ASTNode.TYPE_DECLARATION_STATEMENT :
            case ASTNode.ANONYMOUS_CLASS_DECLARATION :
            case ASTNode.ANNOTATION_TYPE_DECLARATION :
            case ASTNode.ENUM_DECLARATION :
               next = null;
          }
       }
      if (next == null) break;
      posnode = next;
    }
   if (posnode != null) {
      int off = posnode.getStartPosition();
      start_position = sesame_file.createPosition(off);
    }
}






// private class FindPositionVisitor extends ASTVisitor {
// 
// private int start_offset;   
// private CompilationUnit comp_unit;
// 
// FindPositionVisitor() { 
//       start_offset = -1;
//       comp_unit = null;
//     }
// 
// int getStartOffset()                 { return start_offset; }
// 
// @Override public boolean preVisit2(ASTNode n) {
//       if (n instanceof CompilationUnit) {
//          comp_unit = (CompilationUnit) n;
//        }
//       if (comp_unit == null) return true;
//       int startln = comp_unit.getLineNumber(n.getStartPosition());
//       int endln = comp_unit.getLineNumber(n.getStartPosition() + n.getLength() + 1);
//       if (endln < 0) endln = line_number+1;
//       AcornLog.logD("SESAME","CHECK LINES " + startln + " " + endln + " " + line_number + " " +
//             n.getClass());
//       if (n.getNodeType() == ASTNode.METHOD_DECLARATION) 
//          AcornLog.logD("SESAME","METHOD " + ((MethodDeclaration) n).getName());
//       if (n.getRoot() != comp_unit) {
//          AcornLog.logD("SESAME","Bad CompUnit " + n);
//        }
//       
//       if (line_number < startln || line_number > endln) return false;
//       switch (n.getNodeType()) {
//          case ASTNode.METHOD_DECLARATION :
//             return true;
//          case ASTNode.FIELD_DECLARATION :
//             return false;
//          case ASTNode.COMPILATION_UNIT :
//          case ASTNode.TYPE_DECLARATION :
//          case ASTNode.TYPE_DECLARATION_STATEMENT :
//          case ASTNode.ANONYMOUS_CLASS_DECLARATION :
//          case ASTNode.ANNOTATION_TYPE_DECLARATION :
//          case ASTNode.ENUM_DECLARATION :
//             return true;
//          default :
//             return false;
//        }
//     }
// 
// @Override public boolean visit(MethodDeclaration md) {
//       if (comp_unit == null) return false;
//       AcornLog.logD("SESAME","CHECK METHOD LOCATION: " + md.getStartPosition() + " "
//             + comp_unit.getLineNumber(md.getStartPosition()) +
//             " " + md.getStartPosition() + " " + md.getLength() + " " +
//             comp_unit.getLineNumber(md.getStartPosition() + md.getLength()) + " " +
//             line_number + " " +  md.getName());
//       int startln = comp_unit.getLineNumber(md.getStartPosition());   
//       Block b = md.getBody();
//       if (b == null) return false;
//       int blockln = comp_unit.getLineNumber(b.getStartPosition());
//       int firstln = blockln;
//       if (b.statements().size() > 0) {
//          Statement s = (Statement) b.statements().get(0);
//          firstln = comp_unit.getLineNumber(s.getStartPosition());
//        }
//       start_offset = md.getStartPosition();
//       AcornLog.logD("SESAME","CHECK METHOD " + startln + " " + line_number + " " +
//             blockln + " " + firstln);
//       if (startln == line_number || startln == blockln || startln == firstln) {
//          return false;
//        }
//       return true;
//     }
// 
// }       // end of inner class FindPositionVisitor



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("LOC[");
   if (is_active) buf.append("*");
   if (line_number > 0) {
      buf.append(line_number);
      buf.append("@");
    }
   buf.append(sesame_file.getFileName());
   buf.append(":");
   buf.append(method_name);
   buf.append(":");
   buf.append(start_position);
   buf.append("]");
   return buf.toString();
}

}       // end of class SesameLocation




/* end of SesameLocation.java */

