/********************************************************************************/
/*										*/
/*		CuminNativeEvaluator.java					*/
/*										*/
/*	Common code for native evaluators					*/
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

import java.io.File;

import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueClass;
import edu.brown.cs.seede.cashew.CashewValueFile;

abstract class CuminNativeEvaluator implements CuminConstants, CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected CuminRunnerByteCode	exec_runner;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected CuminNativeEvaluator(CuminRunnerByteCode bc)
{
   exec_runner = bc;
}



/********************************************************************************/
/*										*/
/*	Local access methods							*/
/*										*/
/********************************************************************************/

protected JcodeMethod getMethod()	{ return exec_runner.getCodeMethod(); }
protected CashewClock getClock()	{ return exec_runner.getClock(); }
protected int getNumArgs()		{ return exec_runner.getNumArg(); }
protected CashewContext getContext()	{ return exec_runner.getLookupContext(); }
protected JcompTyper getTyper() 	{ return exec_runner.getTyper(); }
protected CashewValueSession getSession() 
{
   return exec_runner.getSession();
}


protected String getString(int idx) throws CashewException
{
   return getContext().findReference(idx).getString(getSession(),getTyper(),getClock());
}

protected int getInt(int idx) throws CashewException 
{
   return getContext().findReference(idx).getNumber(getSession(),getClock()).intValue();
}

protected double getDouble(int idx) throws CashewException
{
   return getContext().findReference(idx).getNumber(getSession(),getClock()).doubleValue();
}

protected float getFloat(int idx) throws CashewException
{
   return getContext().findReference(idx).getNumber(getSession(),getClock()).floatValue();
}

protected long getLong(int idx) throws CashewException
{
   return getContext().findReference(idx).getNumber(getSession(),getClock()).longValue();
}


protected char getChar(int idx) throws CashewException
{
   return getContext().findReference(idx).getChar(getSession(),getClock());
}

protected char [] getCharArray(int idx) throws CashewException
{
   CashewValueSession sess = getSession();
   CashewClock cc = getClock();
   CashewValue cv = getContext().findReference(idx).getActualValue(sess,cc);
   int dim = cv.getDimension(sess,cc);
   char [] rslt = new char[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(sess,cc,i).getChar(sess,cc);
    }
   return rslt;
}


protected byte [] getByteArray(int idx) throws CashewException
{
   CashewValueSession sess = getSession();
   CashewClock cc = getClock();
   CashewValue cv = getContext().findReference(idx).getActualValue(sess,cc);
   int dim = cv.getDimension(sess,cc);
   byte [] rslt = new byte[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(sess,cc,i).getNumber(sess,getClock()).byteValue();
    }
   return rslt;
}


protected int [] getIntArray(int idx) throws CashewException
{
   CashewValueSession sess = getSession();
   CashewClock cc = getClock();
   CashewValue cv = getContext().findReference(idx).getActualValue(sess,cc);
   int dim = cv.getDimension(sess,cc);
   int [] rslt = new int[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(sess,cc,i).getNumber(sess,cc).intValue();
    }
   return rslt;
}


protected float [] getFloatArray(int idx) throws CashewException
{
   CashewValueSession sess = getSession();
   CashewClock cc = getClock();
   CashewValue cv = getContext().findReference(idx).getActualValue(sess,cc);
   int dim = cv.getDimension(sess,cc);
   float [] rslt = new float[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(sess,cc,i).getNumber(sess,cc).floatValue();
    }
   return rslt;
}



protected double [] getDoubleArray(int idx) throws CashewException
{
   CashewValueSession sess = getSession();
   CashewClock cc = getClock();
   CashewValue cv = getContext().findReference(idx).getActualValue(sess,cc);
   int dim = cv.getDimension(sess,cc);
   double [] rslt = new double[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(sess,cc,i).getNumber(sess,cc).doubleValue();
    }
   return rslt;
}


protected boolean getBoolean(int idx) throws CashewException 
{
   return getContext().findReference(idx).getBoolean(getSession(),getClock());
}


protected File getFile(int idx)
{
   CashewValueFile cvf = (CashewValueFile) getValue(idx);
   return cvf.getFile();
}

protected JcompType getDataType(int idx)
{
   return getContext().findReference(idx).getDataType(getSession(),getClock(),getTyper());
}

protected CashewValue getValue(int idx)
{
   return getContext().findReference(idx).getActualValue(getSession(),getClock());
}



protected CashewValue getArrayValue(int idx) throws CuminRunException
{
   CashewValueSession sess = getSession();
   CashewClock cc = getClock();
   CashewValue array = getValue(idx);
   String exc = null;
   if (array.isNull(sess,cc))
      exc = "java.lang.NullPointerException";
   if (!array.getDataType(sess,cc,getTyper()).isArrayType())
      exc = "java.lang.IllegalArgumentException";
   if (exc != null) CuminEvaluator.throwException(sess,getTyper(),exc);
   return array;
}


protected CashewValueClass getTypeValue(int idx)
{
   CashewValue typev = getValue(idx);
   return (CashewValueClass) typev;
}


protected String getStringFieldValue(CashewValue obj,String fld) throws CashewException
{
   CashewValue cv = obj.getFieldValue(getSession(),getTyper(),getClock(),fld);
   return cv.getString(getSession(),getTyper(),getClock());
}


protected int getIntFieldValue(CashewValue obj,String fld) throws CashewException
{
   CashewValue cv = obj.getFieldValue(getSession(),getTyper(),getClock(),fld);
   return cv.getNumber(getSession(),getClock()).intValue();
}  



protected void copyField(CashewValue from,CashewValue to,String fld) throws CashewException
{
   CashewValue v0 = from.getFieldValue(getSession(),getTyper(),getClock(),fld);
   to.setFieldValue(getSession(),getTyper(),getClock(),fld,v0);
}


}	// end of class CuminNativeEvaluator




/* end of CuminNativeEvaluator.java */

