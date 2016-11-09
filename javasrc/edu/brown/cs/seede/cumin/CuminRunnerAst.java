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


import org.eclipse.jdt.core.dom.ASTNode;

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



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminRunnerAst(ASTNode method)
{
   method_node = method;
   execution_stack = null;
}


/********************************************************************************/
/*                                                                              */
/*      Basic operations                                                        */
/*                                                                              */
/********************************************************************************/

void startRunner()
{
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

