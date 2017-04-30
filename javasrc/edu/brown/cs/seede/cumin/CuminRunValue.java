/********************************************************************************/
/*										*/
/*		CuminRunStatus.java						*/
/*										*/
/*	description of class							*/
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



package edu.brown.cs.seede.cumin;

import edu.brown.cs.seede.cashew.CashewValue;

public class CuminRunValue implements CuminRunStatus
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Reason		throw_reason;
private CashewValue	associated_value;
private CuminRunner	call_value;
private String		associated_id;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminRunValue(Reason r,CashewValue v)
{
   throw_reason = r;
   associated_value = v;
   call_value = null;
   associated_id = r.toString();
}


CuminRunValue(Reason r)
{
   this(r,(CashewValue) null);
}


CuminRunValue(CuminRunner r)
{
   this(Reason.CALL,(CashewValue) null);
   call_value = r;
}


CuminRunValue(Reason r,String id)
{
   throw_reason = r;
   associated_value = null;
   associated_id = id;
   call_value = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Reason getReason()		{ return throw_reason; }

@Override public CashewValue getValue() { return associated_value; }

@Override public CuminRunner getCallRunner()	{ return call_value; }

@Override public String getMessage()		{ return associated_id; }

@Override public Throwable getCause()		{ return null; }


}	// end of class CuminRunStatus




/* end of CuminRunStatus.java */

































































































