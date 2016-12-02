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

import java.util.HashSet;
import java.util.Set;

import edu.brown.cs.ivy.xml.IvyXmlWriter;



public class CashewOutputContext implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private IvyXmlWriter xml_writer;
private Set<CashewValue> values_output;
private Set<String> fields_output;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public CashewOutputContext() 
{
   values_output = new HashSet<CashewValue>();
   fields_output = new HashSet<String>();
   xml_writer = new IvyXmlWriter();
}



public CashewOutputContext(IvyXmlWriter xw)
{
   values_output = new HashSet<CashewValue>();
   fields_output = new HashSet<String>();
   xml_writer = xw;
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

public boolean noteValue(CashewValue cv)
{
   // return true if the value has been previously output
   if (values_output.add(cv)) return false;
   return true;
}


public boolean noteField(String name)
{
   // return true if the field has been previously output
   if (fields_output.add(name)) return false;
   return true;
}



}       // end of class CashewOutputContext




/* end of CashewOutputContext.java */

