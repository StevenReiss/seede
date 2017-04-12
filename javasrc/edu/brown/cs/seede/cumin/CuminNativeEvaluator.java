/********************************************************************************/
/*                                                                              */
/*              CuminNativeEvaluator.java                                       */
/*                                                                              */
/*      Common code for native evaluators                                       */
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



package edu.brown.cs.seede.cumin;

import java.io.File;

import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueClass;
import edu.brown.cs.seede.cashew.CashewValueFile;

abstract class CuminNativeEvaluator implements CuminConstants, CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected CuminRunnerByteCode   exec_runner;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
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

protected JcodeMethod getMethod() 	{ return exec_runner.getCodeMethod(); }
protected CashewClock getClock()		{ return exec_runner.getClock(); }
protected int getNumArgs()		{ return exec_runner.getNumArg(); }
protected CashewContext getContext()	{ return exec_runner.getLookupContext(); }


protected String getString(int idx)
{
   return getContext().findReference(idx).getString(getClock());
}

protected int getInt(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).intValue();
}

protected double getDouble(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).doubleValue();
}

protected float getFloat(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).floatValue();
}

protected long getLong(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).longValue();
}


protected char getChar(int idx)
{
   return getContext().findReference(idx).getChar(getClock());
}

protected char [] getCharArray(int idx)
{
   CashewValue cv = getContext().findReference(idx).getActualValue(getClock());
   int dim = cv.getDimension(getClock());
   char [] rslt = new char[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(getClock(),i).getChar(getClock());
    }
   return rslt;
}


protected byte [] getByteArray(int idx)
{
   CashewValue cv = getContext().findReference(idx).getActualValue(getClock());
   int dim = cv.getDimension(getClock());
   byte [] rslt = new byte[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(getClock(),i).getNumber(getClock()).byteValue();
    }
   return rslt;
}


protected int [] getIntArray(int idx)
{
   CashewValue cv = getContext().findReference(idx).getActualValue(getClock());
   int dim = cv.getDimension(getClock());
   int [] rslt = new int[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(getClock(),i).getNumber(getClock()).intValue();
    }
   return rslt;
}


protected boolean getBoolean(int idx)
{
   return getContext().findReference(idx).getBoolean(getClock());
}


protected File getFile(int idx)
{
   CashewValueFile cvf = (CashewValueFile) getValue(idx);
   return cvf.getFile();
}

protected JcompType getDataType(int idx)
{
   return getContext().findReference(idx).getDataType(getClock());
}

protected CashewValue getValue(int idx)
{
   return getContext().findReference(idx).getActualValue(getClock());
}



protected CashewValue getArrayValue(int idx)
{
   CashewValue array = getValue(idx);
   if (array.isNull(getClock()))  CuminEvaluator.throwException(NULL_PTR_EXC);
   if (!array.getDataType(getClock()).isArrayType()) CuminEvaluator.throwException(ILL_ARG_EXC);
   return array;
}


protected CashewValueClass getTypeValue(int idx)
{
   CashewValue typev = getValue(idx);
   return (CashewValueClass) typev;
}



}       // end of class CuminNativeEvaluator




/* end of CuminNativeEvaluator.java */

