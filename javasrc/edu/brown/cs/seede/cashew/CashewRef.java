/********************************************************************************/
/*                                                                              */
/*              CashewRef.java                                                  */
/*                                                                              */
/*      Holds a reference to a value                                            */
/*                                                                              */
/*      This holds a set of values that is time dependent.  It can be used      */
/*      to represent variables or objects.  An object value (or array value)    */
/*      contains a pointer to its reference and all access to it from other     */
/*      variables or objects will point to the reference.  All access to        */
/*      vvalues needs to be time-based when computing an expression.  The       */
/*      result of the computation is a value however.                           */
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



package edu.brown.cs.seede.cashew;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

class CashewRef implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SortedMap<Long,CashewValue>     value_map;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewRef() 
{
   value_map = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

CashewValue getValueAt(CashewClock cc)
{
   if (value_map == null) return null;
   
   SortedMap<Long,CashewValue> head = value_map.headMap(cc.getTimeValue());
   if (head.isEmpty()) return null;
   
   return value_map.get(head.lastKey());
}



void setValueAt(CashewClock cc,CashewValue cv)
{
   if (value_map == null) value_map = new TreeMap<Long,CashewValue>();
   value_map.put(cc.getTimeValue(),cv);
   cc.tick();
}
   


}       // end of class CashewRef




/* end of CashewRef.java */

