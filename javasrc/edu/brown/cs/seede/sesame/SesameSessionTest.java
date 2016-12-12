/********************************************************************************/
/*                                                                              */
/*              SesameSessionTest.java                                          */
/*                                                                              */
/*      Session based on specific test inputs                                   */
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewValue;

class SesameSessionTest extends SesameSession
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<CashewValue> test_args;
private Map<String,CashewValue> global_vars;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSessionTest(SesameMain sm,String sid,Element xml) throws SesameException
{
   super(sm,sid,xml);
   
   Element txml = IvyXml.getChild(xml,"TEST");
   JcompTyper typer = getProject().getTyper();
   test_args = new ArrayList<CashewValue>();
   try {
      for (Element axml : IvyXml.children(txml,"ARG")) {
         test_args.add(CashewValue.createValue(typer,axml));
       }
      for (Element gxml : IvyXml.children(txml,"GLOBAL")) {
         String nm = IvyXml.getAttrString(gxml,"NAME");
         CashewValue cv = CashewValue.createValue(typer,gxml);
         global_vars.put(nm,cv);
       }
    }
   catch (CashewException e) {
      throw new SesameException("Illebal value",e);
    }
   
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public List<CashewValue> getCallArgs(SesameLocation loc)
{
   return test_args;
}



@Override CashewValue lookupValue(String name,String type)
{
   CashewValue cv = global_vars.get(name);
   if (cv != null) return cv;
   
   // can ask user for value here
   
   JcompTyper typer = getProject().getTyper();
   JcompType jty = typer.findType(type);
   cv = CashewValue.createDefaultValue(jty);
   
   return cv;
}



}       // end of class SesameSessionTest




/* end of SesameSessionTest.java */

