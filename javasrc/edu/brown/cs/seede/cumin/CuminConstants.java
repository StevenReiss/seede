/********************************************************************************/
/*                                                                              */
/*              CuminConstants.java                                             */
/*                                                                              */
/*      ContinUous (M) Interpreter constant definitions                         */
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



public interface CuminConstants
{



/********************************************************************************/
/*                                                                              */
/*      Evaluation methods                                                      */
/*                                                                              */
/********************************************************************************/

enum EvalType {
   RUN, STEP
}


enum CallType {
   STATIC, SPECIAL, INTERFACE, DYNAMIC, VIRTUAL 
}



/********************************************************************************/
/*                                                                              */
/*      Operators                                                               */
/*                                                                              */
/********************************************************************************/

enum CuminOperator {
   MUL, DIV, MOD, ADD, SUB, LSH, RSH, RSHU, LSS, GTR, LEQ, GEQ, EQL, NEQ,
   XOR, AND, OR, POSTINCR, POSTDECR, INCR, DECR, COMP, NEG, NOP,
   ASG, ASG_ADD, ASG_SUB, ASG_MUL, ASG_DIV, ASG_AND, ASG_OR, ASG_XOR, ASG_MOD, 
   ASG_LSH, ASG_RSH, ASG_RSHU, SIG
}


}       // end of interface CuminConstants




/* end of CuminConstants.java */

