/********************************************************************************/
/*                                                                              */
/*              CashewContext.java                                              */
/*                                                                              */
/*      Context for looking up symbols/names                                    */
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

import edu.brown.cs.ivy.jcomp.JcompSymbol;

public abstract class CashewContext implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<Object,CashewValue> context_map;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected CashewContext()
{ 
   context_map = new HashMap<>();
}


/********************************************************************************/
/*                                                                              */
/*      Context Operators                                                       */
/*                                                                              */
/********************************************************************************/

public CashewValue findReference(JcompSymbol sym)
{
   // for AST-based lookup
   return findReference((Object) sym);
}



public CashewValue findReference(Object var)
{
   // for byte-code based lookup
   CashewValue cv = context_map.get(var);
   if (cv != null) return cv;
   // need to create a new value here
   return cv;
}


public CashewContext pop() throws CashewException
{
   throw new CashewException("Can't pop a non-stack context");
}


public void define(JcompSymbol sym,CashewValue addr)
{
   define((Object) sym,addr);
}


public void define(Object var,CashewValue addr)
{
   context_map.put(var,addr);
}




}       // end of class CashewContext




/* end of CashewContext.java */

