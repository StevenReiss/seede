/********************************************************************************/
/*                                                                              */
/*              CuminMethdRunner.java                                           */
/*                                                                              */
/*      Evaluate a method                                                       */
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

import edu.brown.cs.seede.cashew.CashewValue;

class CuminMethdRunner implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          method_name;
private List<CashewValue> initial_parameters;
private CuminRunner     current_runner;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminMethdRunner(String name,List<CashewValue> params)
{
   method_name = name;
   initial_parameters = params;
   current_runner = null;
}




/********************************************************************************/
/*                                                                              */
/*      Evaluation methods                                                      */
/*                                                                              */
/********************************************************************************/

void evaluate(EvalType et)
{
   setupRunner();
   
}





private void setupRunner()
{
   if (current_runner != null) return;
   
   // first split method into class/method/signature
   // then determine if class is known or not
   // if known, find its AST and use AST evaluation
   // else find the JcodeMethod and use byte code evaluation
   // set up context
   // set up initial stack
}


   

}       // end of class CuminMethdRunner




/* end of CuminMethdRunner.java */

