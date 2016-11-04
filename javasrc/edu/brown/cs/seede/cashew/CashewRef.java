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
private long    last_update;
private CashewValue last_value;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewRef() 
{
   value_map = null;
   last_update = -1;
   last_value = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

CashewValue getValueAt(CashewClock cc)
{
   long tv = cc.getTimeValue();
   
   if (last_update >= 0) {
      if (tv > last_update) return last_value;
    }
   
   if (value_map == null) return null;
   
   SortedMap<Long,CashewValue> head = value_map.headMap(tv);
   if (head.isEmpty()) return null;
   
   return value_map.get(head.lastKey());
}



void setValueAt(CashewClock cc,CashewValue cv)
{
   long tv = cc.getTimeValue();
   
   if (last_update < 0) {
      // first time -- just record value
      last_update = tv;
      last_value = cv;
      return;
    }
   
   if (value_map == null) {
      value_map = new TreeMap<Long,CashewValue>();
      value_map.put(last_update,last_value);
    }
   
   if (tv > last_update) {
      last_update = tv;
      last_value = cv;
    }
   
   value_map.put(cc.getTimeValue(),cv);
   cc.tick();
}
   


}       // end of class CashewRef




/* end of CashewRef.java */

