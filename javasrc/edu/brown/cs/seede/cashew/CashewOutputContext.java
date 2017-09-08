/********************************************************************************/
/*                                                                              */
/*              CashewOutputContext.java                                        */
/*                                                                              */
/*      Information for providing concise outputs                               */
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import edu.brown.cs.ivy.xml.IvyXmlWriter;



public class CashewOutputContext implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private IvyXmlWriter xml_writer;
private Map<CashewValue,Integer> values_output;
private Set<String> expand_names;

private static AtomicInteger id_counter = new AtomicInteger();



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public CashewOutputContext(IvyXmlWriter xw,Set<String> exp)
{
   values_output = new HashMap<CashewValue,Integer>();
   xml_writer = xw;
   expand_names = exp;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public IvyXmlWriter getXmlWriter()
{
   return xml_writer;
}


public String getContents() 
{
   return xml_writer.toString();
}



/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

public int noteValue(CashewValue cv)
{
   // returns -id if new, id if old
   
   Integer v = values_output.get(cv);
   if (v != null && v != 0) return v;
   v = id_counter.incrementAndGet();
   values_output.put(cv,v);
   return -v;
}


public boolean noteChecked(CashewValue cv)
{
   Integer v = values_output.get(cv);
   if (v != null) return true;
   values_output.put(cv,0);
   return false;
}



public boolean expand(String name)
{
   if (name == null) return false;
   if (expand_names == null) return false;
   
   return expand_names.contains(name);
}






}       // end of class CashewOutputContext




/* end of CashewOutputContext.java */

