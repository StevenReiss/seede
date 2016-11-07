/********************************************************************************/
/*                                                                              */
/*              CuminRunnerAst.java                                             */
/*                                                                              */
/*      AST-based code interpreter                                              */
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



package edu.brown.cs.seede.cumin;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import edu.brown.cs.seede.cashew.CashewValue;

class CuminRunnerAst extends CuminRunner
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ASTNode         method_node;
private CuminStack      execution_stack;
private ASTNode         current_node;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminRunnerAst(ASTNode method)
{
   method_node = method;
   current_node = null;
   execution_stack = null;
}


/********************************************************************************/
/*                                                                              */
/*      Basic operations                                                        */
/*                                                                              */
/********************************************************************************/

void startRunner()
{
   current_node = method_node;
   execution_stack = new CuminStack();
   for (CashewValue cv : getInitialParameters()) {
      execution_stack.push(cv);
    }
}


void runOneStatement()
{
   
}


void runToStop()
{
   
}




}       // end of class CuminRunnerAst




/* end of CuminRunnerAst.java */

