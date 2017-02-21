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
import java.util.Map;

class SesameSessionCache implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<String,Map<String,SesameValueData>> thread_map;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSessionCache()
{
   thread_map = new HashMap<>();
}



/********************************************************************************/
/*                                                                              */
/*      Lookup methods                                                          */
/*                                                                              */
/********************************************************************************/

SesameValueData lookup(String thread,String expr)
{
   if (thread == null || thread.equals("")) thread = "*";
   
   synchronized (thread_map) {
      Map<String,SesameValueData> mapr = thread_map.get(thread);
      if (mapr == null) return null;
      return mapr.get(expr);
    }
}



void cacheValue(String thread,String expr,SesameValueData svd)
{
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




void clearCache()
{
   thread_map.clear();
}


}       // end of class SesameSessionCache




/* end of SesameSessionCache.java */

