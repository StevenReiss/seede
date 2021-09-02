/********************************************************************************/
/*										*/
/*		CashewValueFunctionRef.java					*/
/*										*/
/*	Value for dynamic function references					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.seede.cashew;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;

public class CashewValueFunctionRef extends CashewValueObject implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<Object,CashewValue> initial_bindings;
private String			method_name;
private ASTNode 		eval_node;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public CashewValueFunctionRef(JcompTyper typer,JcompType typ,
      ASTNode nx,List<JcompSymbol> px,
      Map<Object,CashewValue> bind)
{
   super(typer,typ,null,false);
   initial_bindings = bind;
   method_name = null;
   eval_node = nx;
}


public CashewValueFunctionRef(JcompTyper typer,JcompType typ,String method,
        Map<Object,CashewValue> bind)
{
   super(typer,typ,null,false);
   initial_bindings = bind;
   method_name = method;
   eval_node = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Map<Object,CashewValue> getBindings()	{ return initial_bindings; }

public String getMethodName()			
{
   return method_name;
}

public ASTNode getEvalNode()
{
   return eval_node;
}

@Override public boolean isFunctionRef(CashewValueSession sess,CashewClock cc)	
{ 
   return true; 
}


}	// end of class CashewValueFunctionRef




/* end of CashewValueFunctionRef.java */

