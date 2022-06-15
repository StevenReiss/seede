/********************************************************************************/
/*                                                                              */
/*              SesameSessionCache.java                                         */
/*                                                                              */
/*      description of class                                                    */
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewConstants.CashewValueSession;

class SesameSessionCache implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<String,Map<String,SesameValueData>> thread_map;
private Map<String,CashewValue> initial_map;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSessionCache()
{
   thread_map = new HashMap<>();
   initial_map = new HashMap<>();
}



/********************************************************************************/
/*                                                                              */
/*      Lookup methods                                                          */
/*                                                                              */
/********************************************************************************/

SesameValueData lookup(String thread,String expr)
{
   if (thread == null || thread.equals("")) thread = "*";
   
   if (thread.equals("*")) {
      CashewValue cv = null;
      synchronized (initial_map) {
         cv = initial_map.get(expr);
       }
      if (cv != null) {
         return new SesameValueData(cv);
       }
    }    
   
   synchronized (thread_map) {
      Map<String,SesameValueData> mapr = thread_map.get(thread);
      if (mapr == null) return null;
      return mapr.get(expr);
    }
}



void cacheValue(String thread,String expr,SesameValueData svd)
{
   if (svd == null) return;
   if (thread == null || thread.equals("")) thread = "*";
   
   synchronized (thread_map) {
      Map<String,SesameValueData> mapr = thread_map.get(thread);
      if (mapr == null) {
         mapr = new HashMap<>();
         thread_map.put(thread,mapr);
       }
      mapr.put(expr,svd);
    }
}



void setInitialValue(String expr,CashewValue cv)
{
   if (cv == null) initial_map.remove(expr);
   else initial_map.put(expr,cv);
}



void clearCache()
{
   thread_map.clear();
}



void updateCache(CashewValueSession sess,JcompTyper typer)
{
   Set<CashewValue> done = new HashSet<>();
   
   for (CashewValue cv : initial_map.values()) {
      cv.resetType(sess,typer,done);
    }
   for (Map<String,SesameValueData> mp1 : thread_map.values()) {
      for (SesameValueData svd : mp1.values()) {
         svd.resetType(typer,done);
       }
    }
}




}       // end of class SesameSessionCache




/* end of SesameSessionCache.java */

