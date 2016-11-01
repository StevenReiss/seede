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

import org.w3c.dom.Element;

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

private TestCase        test_case;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSessionTest(SesameMain sm,String sid,Element xml) throws SesameException
{
   super(sm,sid,xml);
   
   Element txml = IvyXml.getChild(xml,"TEST");
   test_case = null;
   if (txml != null) test_case = new TestCase(txml);
}



/********************************************************************************/
/*                                                                              */
/*      Holder of the test case                                                 */
/*                                                                              */
/********************************************************************************/

private class TestCase {
   
   private String test_method;
   private List<CashewValue> test_args;
   
   TestCase(Element xml) throws SesameException {
      JcompTyper typer = getProject().getTyper();
      test_args = new ArrayList<CashewValue>();
      test_method = IvyXml.getAttrString(xml,"METHOD");
      try {
         for (Element axml : IvyXml.children(xml,"ARG")) {
            test_args.add(CashewValue.createValue(typer,axml));
          }
       }
      catch (CashewException e) {
         throw new SesameException("Illebal value",e);
       }
    }
   
}       // end of inner class TestCase




}       // end of class SesameSessionTest




/* end of SesameSessionTest.java */

