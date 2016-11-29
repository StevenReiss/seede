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

import edu.brown.cs.ivy.jcode.JcodeField;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.seede.cashew.CashewContext;
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
   super("GLOBAL_CONTEXT",null);
   
   for_session = ss;
}




/********************************************************************************/
/*                                                                              */
/*      Overridden methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override protected CashewValue findStaticFieldReference(String name,String type)
{
   CashewValue cv = super.findStaticFieldReference(name,type);
   if (cv != null) return cv;
   cv = for_session.lookupValue(name,type);
   if (cv != null) define(name,cv);
   
   return cv;
}


@Override public CashewValue evaluate(String expr)
{
   return for_session.evaluate(expr);
}









}       // end of class SesameContext




/* end of SesameContext.java */

