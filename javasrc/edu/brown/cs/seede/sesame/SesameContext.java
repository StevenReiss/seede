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

public CashewValue findReference(JcompSymbol js)
{
   CashewValue cv = super.findReference(js);
   if (cv != null) return cv;
   
   cv = for_session.lookupValue(js.getFullName(),js.getType().getName());
   if (cv != null) define(js.getFullName(),cv);
   
   return cv;
}


public CashewValue findReference(JcodeField jf)
{
   CashewValue cv = super.findReference(jf);
   if (cv != null) return cv;
   
   String nm = jf.getDeclaringClass().getName() + "." + jf.getName();
   String tnm = jf.getType().getName();
   cv = for_session.lookupValue(nm,tnm);
   if (cv != null) define(nm,cv);
   
   return cv;
}





}       // end of class SesameContext




/* end of SesameContext.java */

