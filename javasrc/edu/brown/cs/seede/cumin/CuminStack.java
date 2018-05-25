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

import edu.brown.cs.seede.acorn.AcornLog;

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
   if (cv == null) {
      AcornLog.logX("Pushing null onto stack");
      throw new Error("Attempt to push null onto stack");
    }
   
   execution_stack.push(cv);
   return cv;
}


CashewValue pop() 
{
   if (execution_stack.isEmpty()) {
      AcornLog.logE("ATTEMPT TO POP EMTPY STACK");
    }
   return (CashewValue) execution_stack.pop();
}


CashewValue peek(int idx)
{
   int itm = execution_stack.size() - idx - 1;
   if (itm < 0) throw new Error("Attempt to peek too far down the stack");
   return (CashewValue) execution_stack.get(itm);
}


void pushMarker(Object v,Object data)
{
   StackMarker sm = new StackMarker(v,data);
   execution_stack.push(sm);
}


Object popMarker(Object itm)
{
   Object v = execution_stack.pop();
   if (v instanceof StackMarker) {
      StackMarker sm = (StackMarker) v;
      if (sm.getItem() != itm) throw new Error("Unexpected marker popped from stack");
      return sm.getData();
    }
   else throw new Error("Non-marker popped from stack");
}


Object popUntil(Object v)
{
   boolean fnd = false;
   for (int i = execution_stack.size()-1; i >= 0; --i) {
      Object o = execution_stack.get(i);
      if (o instanceof StackMarker) {
         StackMarker sm = (StackMarker) o;
         if (sm.getItem() == v) {
            fnd = true;
            break;
          }
       }
    }
   if (!fnd) return null;
   
   for ( ; ; ) {
      Object o = execution_stack.pop();
      if (o instanceof StackMarker) {
         StackMarker sm = (StackMarker) o;
         if (sm.getItem() == v) {
            return sm.getData();
          }
       }
    }
}


int size()                      { return execution_stack.size(); }


/********************************************************************************/
/*                                                                              */
/*      Marker class                                                            */
/*                                                                              */
/********************************************************************************/

private static class StackMarker {
   
   private Object for_object;
   private Object marker_data;
   
   StackMarker(Object itm,Object data) {
      for_object = itm;
      marker_data = data;
    }
   
   Object getItem()                     { return for_object; }
   Object getData()                     { return marker_data; }
   
}       // end of inner class StackMarker

}       // end of class CuminStack




/* end of CuminStack.java */

