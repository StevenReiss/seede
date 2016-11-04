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

private Map<Object,CashewAddress> context_map;



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

public CashewAddress findReference(JcompSymbol sym) throws CashewException
{
   // for AST-based lookup
   return findReference((Object) sym);
}



public CashewAddress findReference(Object var)
{
   // for byte-code based lookup
   return context_map.get(var);
}


public CashewContext pop() throws CashewException
{
   throw new CashewException("Can't pop a non-stack context");
}


public void define(JcompSymbol sym,CashewAddress addr)
{
   define((Object) sym,addr);
}


public void define(Object var,CashewAddress addr)
{
   context_map.put(var,addr);
}




}       // end of class CashewContext




/* end of CashewContext.java */

