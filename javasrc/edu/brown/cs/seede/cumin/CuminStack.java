/********************************************************************************/
/*                                                                              */
/*              CuminStack.java                                                 */
/*                                                                              */
/*      Stack for holding execution values                                      */
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

import java.util.Stack;

import edu.brown.cs.seede.cashew.CashewValue;

class CuminStack implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Stack<Object>       execution_stack;

/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminStack()
{
   execution_stack = new Stack<>();
}



/********************************************************************************/
/*                                                                              */
/*      Stack Operations                                                        */
/*                                                                              */
/********************************************************************************/

CashewValue push(CashewValue cv)
{
   execution_stack.push(cv);
   return cv;
}


CashewValue pop()
{
   return (CashewValue) execution_stack.pop();
}


CashewValue peek(int idx)
{
   int itm = execution_stack.size() - idx - 1;
   if (itm < 0) throw new Error("Attempt to peek too far down the stack");
   return (CashewValue) execution_stack.get(itm);
}


void pushMarker(Object v)
{
   execution_stack.push(v);
}


Object popMarker()
{
   Object v = execution_stack.pop();
   if (v instanceof CashewValue) throw new Error("Non-marker popped from stack");
   return v;
}


int size()                      { return execution_stack.size(); }




}       // end of class CuminStack




/* end of CuminStack.java */

