/********************************************************************************/
/*                                                                              */
/*              SesameContext.java                                              */
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

import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewInputOutputModel;
import edu.brown.cs.seede.cashew.CashewValue;

public class SesameContext extends CashewContext implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameSession   for_session;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameContext(SesameSession ss)
{
   super("GLOBAL_CONTEXT",null,null);
   
   for_session = ss;
}




/********************************************************************************/
/*                                                                              */
/*      Overridden methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public CashewValue findStaticFieldReference(String name,String type)
{
   CashewValue cv = super.findStaticFieldReference(name,type);
   if (cv != null) return cv;
   
   if (name.endsWith(".$assertionsDisabled")) {
      cv = CashewValue.booleanValue(false);
      return cv;
    }
   if (name.endsWith(".$assertionsEnabled")) {
      cv = CashewValue.booleanValue(true);
      return cv;
    }
   
   cv = for_session.lookupValue(name,type);
   if (cv != null) {
      cv = CashewValue.createReference(cv);
      define(name,cv);
    }
   
   return cv;
}


@Override public CashewValue evaluate(String expr)
{
   return for_session.evaluate(expr,null);
}


@Override public CashewValue evaluateVoid(String expr)
{
   for_session.evaluateVoid(expr);
   
   return null;
}




@Override public void enableAccess(String type)
{
   for_session.enableAccess(type);
}


@Override public CashewInputOutputModel getIOModel()
{
   return for_session.getIOModel();
}





}       // end of class SesameContext




/* end of SesameContext.java */

