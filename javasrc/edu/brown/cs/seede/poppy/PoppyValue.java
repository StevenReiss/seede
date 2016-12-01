/********************************************************************************/
/*                                                                              */
/*              PoppyValue.java                                                 */
/*                                                                              */
/*      Provide named access to run time values                                 */
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



package edu.brown.cs.seede.poppy;

import java.util.HashMap;
import java.util.Map;

public class PoppyValue implements PoppyConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static Map<String,Object>       value_map;


static {
   value_map = new HashMap<String,Object>();
}


/********************************************************************************/
/*                                                                              */
/*      Registration methods                                                    */
/*                                                                              */
/********************************************************************************/

public Object register(String id,Object v)
{
   if (v != null && id != null) value_map.put(id,v);
   return v; 
}



public Object unregister(String id)
{
   if (id == null) return null;
   
   return value_map.remove(id);
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public Object getValue(String id)
{
   if (id == null) return null;
   return value_map.get(id);
}


}       // end of class PoppyValue




/* end of PoppyValue.java */

