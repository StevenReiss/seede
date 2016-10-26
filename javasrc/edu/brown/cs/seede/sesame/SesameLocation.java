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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.Position;

class SesameLocation implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameMain      sesame_control;
private SesameFile      sesame_file;
private Position        start_position;
private int             line_number;
private String          method_name;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameLocation(SesameMain sm,File f,int line,String method,String sign)
{
   sesame_control = sm;
   sesame_file = sm.getFileManager().openFile(f);
   line_number = line;
   method_name = method;
   if (sign != null) method_name += sign;
   start_position = null;
   
   if (sesame_file == null) return;
   setupPosition();
}



/********************************************************************************/
/*                                                                              */
/*      Compute file position                                                   */
/*                                                                              */
/********************************************************************************/

private void setupPosition()
{
   ASTNode root = sesame_file.getAst(); 
   FindPositionVisitor fpv = new FindPositionVisitor();
   root.accept(fpv);
   int pos = fpv.getStartOffset();
   if (pos >= 0) {
      start_position = sesame_file.createPosition(pos);
    }
}



private class FindPositionVisitor extends ASTVisitor {
   
   private int start_offset;   
   private CompilationUnit comp_unit;
   
   FindPositionVisitor() { 
      start_offset = -1;
      comp_unit = null;
    }
   
   
   int getStartOffset()                 { return start_offset; }
   
   @Override public boolean preVisit2(ASTNode n) {
      if (n instanceof CompilationUnit) {
         comp_unit = (CompilationUnit) n;
       }
      if (comp_unit == null) return true;
      int startln = comp_unit.getLineNumber(n.getStartPosition());
      int endln = comp_unit.getLineNumber(n.getStartPosition() + n.getLength() + 1);
      if (line_number < startln || line_number > endln) return false;
      switch (n.getNodeType()) {
         case ASTNode.METHOD_DECLARATION :
            return true;
         case ASTNode.FIELD_DECLARATION :
            return false;
         case ASTNode.TYPE_DECLARATION :
         case ASTNode.TYPE_DECLARATION_STATEMENT :
         case ASTNode.ANONYMOUS_CLASS_DECLARATION :
         case ASTNode.ANNOTATION_TYPE_DECLARATION :
         case ASTNode.ENUM_DECLARATION :
            return true;
         default :
            return false;
       }
    }
   
   @Override public boolean visit(MethodDeclaration md) {
      if (comp_unit == null) return false;
      int startln = comp_unit.getLineNumber(md.getStartPosition());   
      Block b = md.getBody();
      if (b == null) return false;
      int blockln = comp_unit.getLineNumber(b.getStartPosition());
      int firstln = blockln;
      if (b.statements().size() > 0) {
         Statement s = (Statement) b.statements().get(0);
         firstln = comp_unit.getLineNumber(s.getStartPosition());
       }
      start_offset = md.getStartPosition();
      if (startln == line_number || startln == blockln || startln == firstln) {
         return false;
       }
      return true;
    }
   
}       // end of inner class FindPositionVisitor


}       // end of class SesameLocation




/* end of SesameLocation.java */

