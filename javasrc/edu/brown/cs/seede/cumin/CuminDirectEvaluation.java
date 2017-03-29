/********************************************************************************/
/*										*/
/*		CuminDirectEvaluation.java					*/
/*										*/
/*	Handle method calls implemented directly				*/
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
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewInputOutputModel;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueClass;
import edu.brown.cs.seede.cashew.CashewValueFile;
import edu.brown.cs.seede.cashew.CashewValueObject;
import edu.brown.cs.seede.cashew.CashewValueString;
import edu.brown.cs.seede.acorn.AcornLog;

class CuminDirectEvaluation implements CuminConstants, CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private CuminRunnerByteCode	exec_runner;

private static AtomicInteger    file_counter = new AtomicInteger(1024);




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminDirectEvaluation(CuminRunnerByteCode bc)
{
   exec_runner = bc;
}



/********************************************************************************/
/*										*/
/*	Local access methods							*/
/*										*/
/********************************************************************************/

private JcodeMethod getMethod() 	{ return exec_runner.getCodeMethod(); }
private CashewClock getClock()		{ return exec_runner.getClock(); }
private int getNumArgs()		{ return exec_runner.getNumArg(); }
private CashewContext getContext()	{ return exec_runner.getLookupContext(); }


private String getString(int idx)
{
   return getContext().findReference(idx).getString(getClock());
}

private int getInt(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).intValue();
}

private double getDouble(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).doubleValue();
}

private float getFloat(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).floatValue();
}

private long getLong(int idx)
{
   return getContext().findReference(idx).getNumber(getClock()).longValue();
}


private char getChar(int idx)
{
   return getContext().findReference(idx).getChar(getClock());
}

private char [] getCharArray(int idx)
{
   CashewValue cv = getContext().findReference(idx).getActualValue(getClock());
   int dim = cv.getDimension(getClock());
   char [] rslt = new char[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(getClock(),i).getChar(getClock());
    }
   return rslt;
}


private byte [] getByteArray(int idx)
{
   CashewValue cv = getContext().findReference(idx).getActualValue(getClock());
   int dim = cv.getDimension(getClock());
   byte [] rslt = new byte[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(getClock(),i).getNumber(getClock()).byteValue();
    }
   return rslt;
}


private int [] getIntArray(int idx)
{
   CashewValue cv = getContext().findReference(idx).getActualValue(getClock());
   int dim = cv.getDimension(getClock());
   int [] rslt = new int[dim];
   for (int i = 0; i < dim; ++i) {
      rslt[i] = cv.getIndexValue(getClock(),i).getNumber(getClock()).intValue();
    }
   return rslt;
}


private boolean getBoolean(int idx)
{
   return getContext().findReference(idx).getBoolean(getClock());
}


private File getFile(int idx)
{
   CashewValueFile cvf = (CashewValueFile) getValue(idx);
   return cvf.getFile();
}

private JcompType getDataType(int idx)
{
   return getContext().findReference(idx).getDataType(getClock());
}

private CashewValue getValue(int idx)
{
   return getContext().findReference(idx).getActualValue(getClock());
}



private CashewValue getArrayValue(int idx)
{
   CashewValue array = getValue(idx);
   if (array.isNull(getClock()))  CuminEvaluator.throwException(NULL_PTR_EXC);
   if (!array.getDataType(getClock()).isArrayType()) CuminEvaluator.throwException(ILL_ARG_EXC);
   return array;
}


private CashewValueClass getTypeValue(int idx)
{
   CashewValue typev = getValue(idx);
   return (CashewValueClass) typev;
}

/********************************************************************************/
/*										*/
/*	String methods								*/
/*										*/
/********************************************************************************/

void checkStringMethods()
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "valueOf" :
	    if (getDataType(0) == BOOLEAN_TYPE) {
	       rslt = CashewValue.stringValue(String.valueOf(getBoolean(0)));
	     }
	    else if (getDataType(0) == CHAR_TYPE) {
	       rslt = CashewValue.stringValue(String.valueOf(getChar(0)));
	     }
	    else if (getDataType(0) == DOUBLE_TYPE) {
	       rslt = CashewValue.stringValue(String.valueOf(getDouble(0)));
	     }
	    else if (getDataType(0) == FLOAT_TYPE) {
	       rslt = CashewValue.stringValue(String.valueOf(getFloat(0)));
	     }
	    else if (getDataType(0) == INT_TYPE || getDataType(0) == SHORT_TYPE ||
		  getDataType(0) == BYTE_TYPE) {
	       rslt = CashewValue.stringValue(String.valueOf(getInt(0)));
	     }
	    else if (getDataType(0) == LONG_TYPE) {
	       rslt = CashewValue.stringValue(String.valueOf(getDouble(0)));
	     }
	    else if (getDataType(0).isArrayType() && getDataType(0).getBaseType() == CHAR_TYPE) {
	       if (getNumArgs() == 1) {
		  rslt = CashewValue.stringValue(String.valueOf(getCharArray(0)));
		}
	       else {
		  rslt = CashewValue.stringValue(String.valueOf(getCharArray(0),getInt(1),getInt(2)));
		}
	     }
	    else {
	       // Object
	       return;
	     }
	    break;
	 case "copyValueOf" :
	    if (getNumArgs() == 1) {
	       rslt = CashewValue.stringValue(String.copyValueOf(getCharArray(0)));
	     }
	    else {
	       rslt = CashewValue.stringValue(String.copyValueOf(getCharArray(0),getInt(1),getInt(2)));
	     }
	    break;
	 case "format" :
	    return;
	 default :
	    return;
       }
    }
   else if (getMethod().isConstructor()) {
      CashewValueString cvs = (CashewValueString) getContext().findReference(0).getActualValue(getClock());
      if (getNumArgs() == 1) ;
      else if (getNumArgs() == 2 && getDataType(1) == STRING_TYPE) {
	 cvs.setInitialValue(getString(1));
       }
      else if (getNumArgs() == 2 && getDataType(1).getBaseType() == CHAR_TYPE) {
	 String temp = new String(getCharArray(1));
	 cvs.setInitialValue(temp);
       }
      // handle various constructors
      else return;
    }
   else {
      CashewValue thisarg = getValue(0);
      String thisstr = thisarg.getString(getClock());
      switch (getMethod().getName()) {
	 case "charAt" :
	    rslt = CashewValue.characterValue(CHAR_TYPE,thisstr.charAt(getInt(1)));
	    break;
	 case "codePointAt" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.codePointAt(getInt(1)));
	    break;
	 case "codePointBefore" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.codePointBefore(getInt(1)));
	    break;
	 case "codePointCount" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.codePointCount(getInt(1),getInt(2)));
	    break;
	 case "compareTo" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.compareTo(getString(1)));
	    break;
	 case "compareToIgnoreCase" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.compareToIgnoreCase(getString(1)));
	    break;
	 case "concat" :
	    rslt = CashewValue.stringValue(thisstr.concat(getString(1)));
	    break;
	 case "contains" :
	    if (getDataType(1) == STRING_TYPE) {
	       rslt = CashewValue.booleanValue(thisstr.contains(getString(1)));
	     }
	    else return;
	    break;
	 case "contentEquals" :
	    if (getDataType(1) == STRING_TYPE) {
	       rslt = CashewValue.booleanValue(thisstr.contentEquals(getString(1)));
	     }
	    else return;
	    break;
	 case "endsWith" :
	    rslt = CashewValue.booleanValue(thisstr.endsWith(getString(1)));
	    break;
	 case "equals" :
	    if (getDataType(1) != STRING_TYPE)
	       rslt = CashewValue.booleanValue(false);
	    else
	       rslt = CashewValue.booleanValue(thisstr.equals(getString(1)));
	    break;
	 case "equalsIgnoreCase" :
	    rslt = CashewValue.booleanValue(thisstr.equalsIgnoreCase(getString(1)));
	    break;
	 case "hashCode" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.hashCode());
	    break;
	 case "indexOf" :
	    if (getDataType(1) == INT_TYPE) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.indexOf(getInt(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.indexOf(getInt(1),getInt(2)));
		}
	     }
	    else if (getDataType(1) == STRING_TYPE) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.indexOf(getString(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.indexOf(getString(1),getInt(2)));
		}
	     }
	    break;
	 case "isEmpty" :
	    rslt = CashewValue.booleanValue(thisstr.isEmpty());
	    break;
	 case "lastIndexOf" :
	    if (getDataType(1) == INT_TYPE) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.lastIndexOf(getInt(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.lastIndexOf(getInt(1),getInt(2)));
		}
	     }
	    else if (getDataType(1) == STRING_TYPE) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.lastIndexOf(getString(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(INT_TYPE,thisstr.lastIndexOf(getString(1),getInt(2)));
		}
	     }
	    break;
	 case "length" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.length());
	    break;
	 case "matches" :
	    rslt = CashewValue.booleanValue(thisstr.matches(getString(1)));
	    break;
	 case "offsetByCodePoints" :
	    rslt = CashewValue.numericValue(INT_TYPE,thisstr.offsetByCodePoints(getInt(1),getInt(2)));
	    break;
	 case "regionMatches" :
	    rslt = CashewValue.booleanValue(thisstr.regionMatches(getInt(1),getString(2),getInt(3),getInt(4)));
	    break;
	 case "replace" :
	    if (getDataType(0) == CHAR_TYPE) {
	       rslt = CashewValue.stringValue(thisstr.replace(getChar(1),getChar(2)));
	     }
	    else if (getDataType(1) == STRING_TYPE && getDataType(2) == STRING_TYPE) {
	       rslt = CashewValue.stringValue(thisstr.replace(getString(1),getString(2)));
	     }
	    else return;
	    break;
	 case "replaceAll" :
	    rslt = CashewValue.stringValue(thisstr.replaceAll(getString(1),getString(2)));
	    break;
	 case "replaceFirst" :
	    rslt = CashewValue.stringValue(thisstr.replaceFirst(getString(1),getString(2)));
	    break;
	 case "startsWith" :
	    if (getNumArgs() == 2) {
	       rslt = CashewValue.booleanValue(thisstr.startsWith(getString(1)));
	     }
	    else {
	       rslt = CashewValue.booleanValue(thisstr.startsWith(getString(1),getInt(2)));
	     }
	    break;
	 case "subSequence" :
	 case "substring" :
	    if (getNumArgs() == 1) {
	       rslt = CashewValue.stringValue(thisstr.substring(getInt(1)));
	     }
	    else {
	       rslt = CashewValue.stringValue(thisstr.substring(getInt(1),getInt(2)));
	     }
	    break;
	 case "toLowerCase" :
	    if (getNumArgs() == 0) {
	       rslt = CashewValue.stringValue(thisstr.toLowerCase());
	     }
	    else {
	       // need to get locale object
	       return;
	     }
	    break;
	 case "toString" :
	    rslt = thisarg;
	    break;
	 case "toUpperCase" :
	    if (getNumArgs() == 0) {
	       rslt = CashewValue.stringValue(thisstr.toUpperCase());
	     }
	    else {
	       // need to get locale object
	       return;
	     }
	    break;
	 case "trim" :
	    rslt = CashewValue.stringValue(thisstr.trim());
	    break;

	 case "getBytes" :
	 case "intern":
	 case "split" :
	    return;
            
	 case "getChars" :
            int srcbegin = getInt(1);
            int srcend = getInt(2);
            CashewValue carr = getArrayValue(3);
            int dstbegin = getInt(4);
            for (int i = srcbegin; i < srcend; ++i) {
               CashewValue charv = CashewValue.characterValue(CHAR_TYPE,thisstr.charAt(i));
               carr.setIndexValue(getClock(),dstbegin+i-srcbegin,charv);
             }
            break;
            
	 case "toCharArray" :
	    rslt = CashewValue.arrayValue(thisstr.toCharArray());
	    return;

	 default :
	    return;
       }
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}



/********************************************************************************/
/*										*/
/*	Strict Math native methods						*/
/*										*/
/********************************************************************************/

void checkMathMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "abs" :
	 if (getDataType(0) == DOUBLE_TYPE) {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.abs(getDouble(0)));
	  }
	 else if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.abs(getFloat(0)));
	  }
	 else if (getDataType(0) == LONG_TYPE) {
	    rslt = CashewValue.numericValue(LONG_TYPE,StrictMath.abs(getLong(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(INT_TYPE,StrictMath.abs(getInt(0)));
	  }
	 break;
      case "acos" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.acos(getDouble(0)));
	 break;
      case "asin" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.asin(getDouble(0)));
	 break;
      case "atan" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.atan(getDouble(0)));
	 break;
      case "atan2" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.atan2(getDouble(0),getDouble(2)));
	 break;
      case "cbrt" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.cbrt(getDouble(0)));
	 break;
      case "ceil" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.ceil(getDouble(0)));
	 break;
      case "copySign" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.copySign(getFloat(0),getFloat(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.copySign(getDouble(0),getDouble(2)));
	  }
	 break;
      case "cos" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.cos(getDouble(0)));
	 break;
      case "cosh" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.cosh(getDouble(0)));
	 break;
      case "exp" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.exp(getDouble(0)));
	 break;
      case "expm1" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.expm1(getDouble(0)));
	 break;
      case "floor" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.floor(getDouble(0)));
	 break;
      case "getExponent" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(INT_TYPE,StrictMath.getExponent(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(INT_TYPE,StrictMath.getExponent(getDouble(0)));
	  }
	 break;
      case "hypot" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.hypot(getDouble(0),getDouble(2)));
	 break;
      case "IEEEremainder" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.IEEEremainder(getDouble(0),getDouble(2)));
	 break;
      case "log" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.log(getDouble(0)));
	 break;
      case "log10" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.log10(getDouble(0)));
	 break;
      case "log1p" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.log1p(getDouble(0)));
	 break;
      case "max" :
	 if (getDataType(0) == DOUBLE_TYPE || getDataType(1) == DOUBLE_TYPE) {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.max(getDouble(0),getDouble(2)));
	  }
	 else if (getDataType(0) == FLOAT_TYPE || getDataType(1) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.max(getFloat(0),getFloat(1)));
	  }
	 else if (getDataType(0) == LONG_TYPE || getDataType(1) == LONG_TYPE) {
	    rslt = CashewValue.numericValue(LONG_TYPE,StrictMath.max(getLong(0),getLong(2)));
	  }
	 else {
	    rslt = CashewValue.numericValue(INT_TYPE,StrictMath.max(getInt(0),getInt(1)));
	  }
	 break;
      case "min" :
	 if (getDataType(0) == DOUBLE_TYPE || getDataType(1) == DOUBLE_TYPE) {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.min(getDouble(0),getDouble(2)));
	  }
	 else if (getDataType(0) == FLOAT_TYPE || getDataType(1) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.min(getFloat(0),getFloat(1)));
	  }
	 else if (getDataType(0) == LONG_TYPE || getDataType(1) == LONG_TYPE) {
	    rslt = CashewValue.numericValue(LONG_TYPE,StrictMath.min(getLong(0),getLong(2)));
	  }
	 else {
	    rslt = CashewValue.numericValue(INT_TYPE,StrictMath.min(getInt(0),getInt(1)));
	  }
	 break;
      case "nextAfter" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.nextAfter(getFloat(0),getDouble(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.nextAfter(getDouble(0),getDouble(2)));
	  }
	 break;
      case "nextUp" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.nextUp(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.nextUp(getDouble(0)));
	  }
	 break;
      case "pow" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.pow(getDouble(0),getDouble(2)));
	 break;
      case "random" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.random());
	 break;
      case "rint" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.rint(getDouble(0)));
	 break;
      case "round" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(INT_TYPE,StrictMath.round(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(LONG_TYPE,StrictMath.round(getDouble(0)));
	  }
	 break;
      case "scalb" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.scalb(getFloat(0),getInt(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.scalb(getDouble(0),getInt(2)));
	  }
	 break;
      case "signum" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.signum(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.signum(getDouble(0)));
	  }
	 break;
      case "sin" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.sin(getDouble(0)));
	 break;
      case "sinh" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.sinh(getDouble(0)));
	 break;
      case "sqrt" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.sqrt(getDouble(0)));
	 break;
      case "tan" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.tan(getDouble(0)));
	 break;
      case "tanh" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.tanh(getDouble(0)));
	 break;
      case "toDegrees" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.toDegrees(getDouble(0)));
	 break;
      case "toRadians" :
	 rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.toRadians(getDouble(0)));
	 break;
      case "ulp" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    rslt = CashewValue.numericValue(FLOAT_TYPE,StrictMath.ulp(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,StrictMath.ulp(getDouble(0)));
	  }
	 break;
      default :
	 return;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*										*/
/*	Native Runtime methods							*/
/*										*/
/********************************************************************************/

void checkRuntimeMethods()
{
   CashewValue rslt = null;
   Runtime rt = Runtime.getRuntime();

   switch (getMethod().getName()) {
      case "availableProcessors" :
	 rslt = CashewValue.numericValue(INT_TYPE,rt.availableProcessors());
	 break;
      case "freeMemory" :
	 rslt = CashewValue.numericValue(INT_TYPE,rt.freeMemory());
	 break;
      case "gc" :
      case "traceInstructions" :
      case "traceMethodCalls" :
	 break;
      case "maxMemory" :
	 rslt = CashewValue.numericValue(INT_TYPE,rt.maxMemory());
	 break;
      case "totalMemory" :
	 rslt = CashewValue.numericValue(INT_TYPE,rt.totalMemory());
	 break;
      case "halt" :
      case "exit" :
	 throw new CuminRunError(CuminRunError.Reason.HALTED);

      case "exec" :
      case "load" :
      case "loadLibrary" :
      case "runFinalization" :
	 //TODO: handle the various exec and load calls
	 return;
      default :
	 return;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}



/********************************************************************************/
/*										*/
/*	Handle float/double methods						*/
/*										*/
/********************************************************************************/

void checkFloatMethods()
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "floatToIntBits" :
	    rslt = CashewValue.numericValue(INT_TYPE,Float.floatToIntBits(getFloat(0)));
	    break;
	 case "floatToRawIntBits" :
	    rslt = CashewValue.numericValue(INT_TYPE,Float.floatToRawIntBits(getFloat(0)));
	    break;
	 case "intBitsToFloat" :
	    rslt = CashewValue.numericValue(FLOAT_TYPE,Float.intBitsToFloat(getInt(0)));
	    break;
	 case "isInfinite" :
	    rslt = CashewValue.booleanValue(Float.isInfinite(getFloat(0)));
	    break;
	 case "isNaN" :
	    rslt = CashewValue.booleanValue(Float.isNaN(getFloat(0)));
	    break;
	 case "toHexString" :
	    rslt = CashewValue.stringValue(Float.toHexString(getFloat(0)));
	    break;
	 case "toString" :
	    rslt = CashewValue.stringValue(Float.toString(getFloat(0)));
	    break;
	 default :
	    return;
       }
    }
   else if (getMethod().isConstructor()) {
      return;
    }
   else {
      switch (getMethod().getName()) {
	 case "hashCode" :
	    Float f = Float.valueOf(getFloat(0));
	    rslt = CashewValue.numericValue(INT_TYPE,f.hashCode());
	    break;
	 default :
	    return;
       }
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}



void checkDoubleMethods()
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "doubleToLongBits" :
	    rslt = CashewValue.numericValue(LONG_TYPE,Double.doubleToLongBits(getFloat(0)));
	    break;
	 case "doubleToRawLongBits" :
	    rslt = CashewValue.numericValue(LONG_TYPE,Double.doubleToRawLongBits(getFloat(0)));
	    break;
	 case "longBitsToDouble" :
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,Double.longBitsToDouble(getLong(0)));
	    break;
	 case "isInfinite" :
	    rslt = CashewValue.booleanValue(Double.isInfinite(getDouble(0)));
	    break;
	 case "isNaN" :
	    rslt = CashewValue.booleanValue(Double.isNaN(getDouble(0)));
	    break;
	 case "toHexString" :
	    rslt = CashewValue.stringValue(Double.toHexString(getDouble(0)));
	    break;
	 case "toString" :
	    rslt = CashewValue.stringValue(Double.toString(getDouble(0)));
	    break;
	 default :
	    return;
       }
    }
   else if (getMethod().isConstructor()) {
      return;
    }
   else {
      switch (getMethod().getName()) {
	 case "hashCode" :
	    Double d = Double.valueOf(getDouble(0));
	    rslt = CashewValue.numericValue(INT_TYPE,d.hashCode());
	    break;
	 default :
	    return;
       }
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*										*/
/*	System methods								*/
/*										*/
/********************************************************************************/

void checkSystemMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "arraycopy" :
	 handleArrayCopy(getValue(0),getInt(1),getValue(2),getInt(3),getInt(4));
	 break;
      case "currentTimeMillis" :
	 rslt = CashewValue.numericValue(LONG_TYPE,System.currentTimeMillis());
	 break;
      case "exit" :
	 throw new CuminRunError(CuminRunError.Reason.HALTED);
      case "gc" :
	 break;
      case "identityHashCode" :
	 rslt = CashewValue.numericValue(INT_TYPE,getValue(0).getFieldValue(getClock(),HASH_CODE_FIELD).
	       getNumber(getClock()).intValue());
	 break;
      case "load" :
      case "loadLibrary" :
	 break;
      case "mapLibraryName" :
	 rslt = CashewValue.stringValue(System.mapLibraryName(getString(0)));
	 break;
      case "nanoTime" :
	 rslt = CashewValue.numericValue(LONG_TYPE,System.nanoTime());
	 break;
      case "runFinalization" :
	 break;
      case "setErr" :
      case "setIn" :
      case "setOut" :
	 break;
      default :
	 return;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}



private void handleArrayCopy(CashewValue src,int spos,CashewValue dst,int dpos,int len)
{
   if (src.isNull(getClock()) || dst.isNull(getClock())) CuminEvaluator.throwException(NULL_PTR_EXC);

   int sdim = -1;
   int ddim = -1;
   try {
      sdim = src.getDimension(getClock());
      ddim = dst.getDimension(getClock());
    }
   catch (Throwable t) {
      CuminEvaluator.throwException(ARRAY_STORE_EXC);
    }
   // check array element types

   if (spos < 0 || dpos < 0 || len < 0 || spos+len > sdim || dpos+len > ddim)
      CuminEvaluator.throwException(IDX_BNDS_EXC);

   if (src == dst && spos < dpos) {
      for (int i = len-1; i >= 0; --i) {
	 CashewValue cv = src.getIndexValue(getClock(),spos+i);
	 dst.setIndexValue(getClock(),dpos+i,cv.getActualValue(getClock()));
       }
    }
   else {
      for (int i = 0; i < len; ++i) {
	 CashewValue cv = src.getIndexValue(getClock(),spos+i);
	 // should type check the assignment here
	 dst.setIndexValue(getClock(),dpos+i,cv.getActualValue(getClock()));
       }
    }
}



/********************************************************************************/
/*										*/
/*	Object methods								*/
/*										*/
/********************************************************************************/

void checkObjectMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "<init>" :
	 break;
      case "getClass" :
	rslt = CashewValue.classValue(getDataType(0));
	break;
      case "equals" :
	 rslt = CashewValue.booleanValue(getValue(0) == getValue(1));
	 break;
      case "hashCode" :
	 rslt = CashewValue.numericValue(INT_TYPE,getValue(0).getFieldValue(getClock(),HASH_CODE_FIELD).
	       getNumber(getClock()).intValue());
	 break;
      case "clone" :
         CashewValueObject obj = (CashewValueObject) getValue(0);
         rslt = obj.cloneObject(getClock());
	 break;
      case "notify" :
      case "notifyAll" :
      case "wait" :
	 // TODO: handle synchronization
	 break;
      default :
	 AcornLog.logD("UNKNOWN CALL TO OBJECT: " + getMethod().getName());
	 return;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}








/********************************************************************************/
/*                                                                              */
/*      Class methods                                                           */
/*                                                                              */
/********************************************************************************/

void checkClassMethods()
{
   CashewValue rslt = null;
   JcompType rtype = null;
   
   if (getMethod().isStatic()) {
      return;
    }
   else {
      CashewValue thisarg = getValue(0);
      JcompType thistype = ((CashewValueClass) thisarg).getJcompType();
      switch (getMethod().getName()) {
         case "getComponentType" :
            if (thistype.isArrayType()) {
               rtype = thistype.getBaseType();
             }
            else rslt = CashewValue.nullValue();
            break;
         case "getName" :
            rslt = CashewValue.stringValue(thistype.getName());
            break;
         default :
            AcornLog.logE("Unknown call to java.lang.Class." + getMethod());
            return;
       }
    }
   if (rslt == null && rtype != null) {
      rslt = CashewValue.classValue(rtype);
    }
   
   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*										*/
/*	Thread methods								*/
/*										*/
/********************************************************************************/

void checkThreadMethods()
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "currentThread" :
	    rslt = exec_runner.getLookupContext().findStaticFieldReference(
		  CURRENT_THREAD_FIELD,"java.lang.Thread");
	    break;
	 default :
	    return;
       }
    }
   else {
      // CashewValue thisarg = getContext().findReference(0).getActualValue(getClock());
      switch (getMethod().getName()) {
	 case "yield" :
	 case "sleep" :
	    break;
	 case "start0" :
	    // start a thread here
	    break;
	 case "getStackTrace" :
	 case "getAllStackTraces" :
	    // These should be handled by calling code
	    break;
	 case "holdsLock" :
	    // TODO: interact with locking here
	    rslt = CashewValue.booleanValue(false);
	    break;
	 case "isInterrupted" :
	    rslt = CashewValue.booleanValue(false);
	    break;
	 case "isAlive" :
	    rslt = CashewValue.booleanValue(true);
	    break;
	 case "countStackFrames" :
	    rslt = CashewValue.numericValue(INT_TYPE,1);
	    break;
       }
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*										*/
/*	Throwable/Exception methods						*/
/*										*/
/********************************************************************************/

void checkThrowableMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "fillInStackTrace" :
	 rslt = getValue(0);
	 break;
      case "getStackTraceDepth" :
	 rslt = CashewValue.numericValue(INT_TYPE,0);
	 break;
      case "getStackTraceElement" :
	 CuminEvaluator.throwException(IDX_BNDS_EXC);
	 break;
      default :
	 return;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*										*/
/*	Handle sun.misc.FloatingDecimal for efficiency				*/
/*										*/
/********************************************************************************/

void checkFloatingDecimalMehtods()
{
   CashewValue rslt = null;
   String s1;

   switch (getMethod().getName()) {
      case "toJavaFormatString" :
	 if (getDataType(0) == FLOAT_TYPE) {
	    s1 = String.valueOf(getFloat(0));
	  }
	 else s1 = String.valueOf(getDouble(0));
	 rslt = CashewValue.stringValue(s1);
	 break;
      case "parseDouble" :
	 try {
	    double d1 = Double.parseDouble(getString(0));
	    rslt = CashewValue.numericValue(DOUBLE_TYPE,d1);
	  }
	 catch (NumberFormatException e) {
	    CuminEvaluator.throwException(NUM_FMT_EXC);
	  }
	 break;
      case "parseFloat" :
	 try {
	    float f1 = Float.parseFloat(getString(0));
	    rslt = CashewValue.numericValue(FLOAT_TYPE,f1);
	  }
	 catch (NumberFormatException e) {
	    CuminEvaluator.throwException(NUM_FMT_EXC);
	  }
	 break;
      default  :
	 return;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}



/********************************************************************************/
/*										*/
/*	Handle java.io.FileOuptutStream methods 				*/
/*										*/
/********************************************************************************/

void checkOutputStreamMethods()
{
   CashewValue thisarg = getValue(0);
   CashewValue fdval = thisarg.getFieldValue(getClock(),"java.io.FileOutputStream.fd");
   if (fdval.isNull(getClock())) return;
   CashewValue fd = fdval.getFieldValue(getClock(),"java.io.FileDescriptor.fd");
   int fdv = fd.getNumber(getClock()).intValue();
   String path = null;
   try {
      CashewValue pathv = thisarg.getFieldValue(getClock(),"java.io.FileOutputStream.path");
      if (!pathv.isNull(getClock())) path = pathv.getString(getClock());
    }
   catch (Throwable t) {
      // path is not defined before jdk 1.8
    }

   int narg = getNumArgs();
   CashewInputOutputModel mdl = getContext().getIOModel();

   CashewValue rslt = null;
   byte [] wbuf = null;

   switch (getMethod().getName()) {
      case "open" :
         if (path != null && fdv < 0) {
            File f = new File(path);
            if (!f.canWrite()) {
               CuminEvaluator.throwException(IO_EXCEPTION);
             }
          }
         if (fdv < 0) {
            fdv = file_counter.incrementAndGet();
            fdval.setFieldValue(getClock(),"java.io.FileDescriptor.fd",
                  CashewValue.numericValue(INT_TYPE,fdv));
          }
	 break;
      case "write" :
	 if (narg != 3) return;
	 wbuf = new byte[1];
	 wbuf[0] = (byte) getInt(1);
	 mdl.fileWrite(getClock(),fdv,path,wbuf,0,1,getBoolean(2));
	 break;
      case "writeBytes" :
	 wbuf = getByteArray(1);
	 mdl.fileWrite(getClock(),fdv,path,wbuf,getInt(2),getInt(3),getBoolean(4));
	 break;
      case "close" :
	 break;
      case "initIDs" :
	 break;
      default :
	 return;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*										*/
/*	java.io.File methods							*/
/*										*/
/********************************************************************************/

void checkFileMethods()
{
   CashewValue rslt = null;
   File rfile = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
         case "createTempFile" :
            return;
         case "listRoots" :
            return;
         default :
            return;
       }
    }
   else if (getMethod().isConstructor()) {
      if (getNumArgs() == 2) {
         if (getDataType(1) == STRING_TYPE) {
            rfile = new File(getString(1));
          }
         // handle URI arg
       }
      else {
         if (getDataType(1) == STRING_TYPE) {
            rfile = new File(getString(1),getString(2));
          }
         else {
            rfile = new File(getFile(1),getString(2));
          }
       }
      if (rfile == null) return;
      CashewValueFile cvf = (CashewValueFile) getValue(0);
      cvf.setInitialValue(rfile);
      rfile = null;
    }
   else {
      CashewValue thisarg = getValue(0);
      File thisfile = ((CashewValueFile) thisarg).getFile();
      CashewInputOutputModel iomdl = getContext().getIOModel();
      
      switch (getMethod().getName()) {
         case "canExecute" :
            rslt = CashewValue.booleanValue(iomdl.canExecute(thisfile));
            break;
         case "canRead" :
            rslt = CashewValue.booleanValue(iomdl.canRead(thisfile));
            break;
         case "canWrite" :
            rslt = CashewValue.booleanValue(iomdl.canWrite(thisfile));
            break;
         case "compareTo" :
            rslt = CashewValue.numericValue(INT_TYPE,thisfile.compareTo(getFile(1)));
            break;
         case "delete" :
            rslt = CashewValue.booleanValue(iomdl.delete(thisfile));
            break;
         case "deleteOnExit" :
            break;
         case "equals" :
            CashewValue cv = getValue(1);
            if (cv instanceof CashewValueFile) {
               rslt = CashewValue.booleanValue(thisfile.equals(getFile(1)));
             }
            else rslt = CashewValue.booleanValue(false);
            break;
         case "exists" :
            rslt = CashewValue.booleanValue(iomdl.exists(thisfile));
            break;
         case "getAbsoluteFile" :
            rfile = thisfile.getAbsoluteFile();
            if (rfile == thisfile) rslt = thisarg;
            break;
         case "getAbsolutePath" :
            rslt = CashewValue.stringValue(thisfile.getAbsolutePath());
            break;
         case "getCanonicalFile" :
            try {
               rfile = thisfile.getCanonicalFile();
             }
            catch (IOException e) {
               CuminEvaluator.throwException(CashewConstants.IO_EXCEPTION);
             }
            if (rfile == thisfile) rslt = thisarg;
            break;
         case "getCanonicalPath" :
            try {
               rslt = CashewValue.stringValue(thisfile.getCanonicalPath());
             }
            catch (IOException e) {
               CuminEvaluator.throwException(CashewConstants.IO_EXCEPTION);
             }
            break;
         case "getFreeSpace" :
            rslt = CashewValue.numericValue(LONG_TYPE,thisfile.getFreeSpace());
            break;
         case "getName" :
            rslt = CashewValue.stringValue(thisfile.getName());
            break;
         case "getParent" :
            rslt = CashewValue.stringValue(thisfile.getParent());
            break;
         case "getParentFile" :
            rfile = thisfile.getParentFile();
            break;
         case "getPath" :
            rslt = CashewValue.stringValue(thisfile.getPath());
            break;
         case "getTotalSpace" :
            rslt = CashewValue.numericValue(LONG_TYPE,thisfile.getTotalSpace());
            break;
         case "getUsableSpace" :
            rslt = CashewValue.numericValue(LONG_TYPE,thisfile.getUsableSpace());
            break; 
         case "hashCode" :
            rslt = CashewValue.numericValue(INT_TYPE,thisfile.hashCode());
            break;
         case "isAbsolute" :
            rslt = CashewValue.booleanValue(thisfile.isAbsolute());
            break;
         case "isDirectory" :
            rslt = CashewValue.booleanValue(iomdl.isDirectory(thisfile));
            break;
         case "isFile" :
            rslt = CashewValue.booleanValue(iomdl.isFile(thisfile));
            break;    
         case "isHidden" :
            rslt = CashewValue.booleanValue(thisfile.isHidden());
            break;
         case "lastModified" :
            rslt = CashewValue.numericValue(LONG_TYPE,thisfile.lastModified());
            break;
         case "length" :
            rslt = CashewValue.numericValue(LONG_TYPE,thisfile.length());
            break;
         case "list" :
            return;
         case "listFiles" :
            return;
         case "mkdir"  :
            rslt = CashewValue.booleanValue(iomdl.mkdir(thisfile));
            break;
         case "mkdirs"  :
            rslt = CashewValue.booleanValue(iomdl.mkdirs(thisfile));
            break;
         case "renameTo" :
            return;
         case "setExecutable" :
            iomdl.setExecutable(thisfile);
            rslt = CashewValue.booleanValue(true);
            break;
         case "setLastModified" :
            rslt = CashewValue.booleanValue(false);
            break;
         case "setReadable" :
            iomdl.setReadable(thisfile);
            rslt = CashewValue.booleanValue(true);
            break;
         case "setReadOnly" :
            iomdl.setReadOnly(thisfile);
            rslt = CashewValue.booleanValue(true);
            break;
         case "setWritable" :
            iomdl.setWritable(thisfile);
            rslt = CashewValue.booleanValue(true);
            break;
         case "toPath" :
            return;
         case "toString" :
            rslt = CashewValue.stringValue(thisfile.toString());
            break;
         case "toURI" :
            return;
         case "toURL" :
            return;
            
         // private methods   
         case "isInvalid" :
            // access to java.io.File.PathStatus.CHECKED fails for now
            rslt = CashewValue.booleanValue(false);
            break;
            
         default :
            AcornLog.logE("Unknown file operation: " + getMethod().getName());
            return;
            
       }
    }

   if (rslt == null && rfile != null) {
      rslt = new CashewValueFile(rfile);
    }
   
   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*                                                                              */
/*      Swing handling methods                                                  */
/*                                                                              */
/********************************************************************************/

void checkSwingUtilityMethods()
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "isEventDispatchThread" :
         rslt = CashewValue.booleanValue(true);
         break;
      default :
         return;
    }
   
   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*                                                                              */
/*      Reflection methods                                                      */
/*                                                                              */
/********************************************************************************/

void checkReflectArrayMethods()
{
   CashewValue rslt = null;
      
   switch (getMethod().getName()) {
      case "get" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         break;
      case "getBoolean" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,BOOLEAN_TYPE);
         break;
      case "getByte" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,BYTE_TYPE);
         break;
      case "getChar" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,CHAR_TYPE);
         break;
      case "getDouble" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,DOUBLE_TYPE);
         break;
      case "getFloat" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,FLOAT_TYPE);
         break;
      case "getInt" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,INT_TYPE);
         break;
      case "getLong" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,LONG_TYPE);
         break;
      case "getShort" :
         rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
         rslt = CuminEvaluator.castValue(exec_runner,rslt,SHORT_TYPE);
         break;
      case "set" :
         getArrayValue(0).setIndexValue(getClock(),getInt(1),getValue(2));
         break;
      case "setBoolean" :
         CashewValue cv = CuminEvaluator.castValue(exec_runner,getValue(2),BOOLEAN_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break;
      case "setByte" :
         cv = CuminEvaluator.castValue(exec_runner,getValue(2),BYTE_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break;
      case "setChar" :
         cv = CuminEvaluator.castValue(exec_runner,getValue(2),CHAR_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break;
      case "setDouble" :
         cv = CuminEvaluator.castValue(exec_runner,getValue(2),DOUBLE_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break;
      case "setFloat" :
         cv = CuminEvaluator.castValue(exec_runner,getValue(2),FLOAT_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break;
      case "setInt" :
         cv = CuminEvaluator.castValue(exec_runner,getValue(2),INT_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break;
      case "setLong" :
         cv = CuminEvaluator.castValue(exec_runner,getValue(2),LONG_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break; 
      case "setShort" :
         cv = CuminEvaluator.castValue(exec_runner,getValue(2),SHORT_TYPE);
         getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
         break; 
      case "newArray" :
         JcompType atype = getTypeValue(0).getJcompType();
         atype = exec_runner.getTyper().findArrayType(atype);
         rslt = CashewValue.arrayValue(atype,getInt(1));
         break;
      case "multiNewArray" :
         atype = getTypeValue(0).getJcompType();
         int [] dims = getIntArray(1);
         rslt = CuminEvaluator.buildArray(exec_runner,0,dims,atype);
         break;
      default :
         return;
    }
   
   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}




/********************************************************************************/
/*                                                                              */
/*      Access controller methods                                               */
/*                                                                              */
/********************************************************************************/

void checkAccessControllerMethods()
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "doPrivileged" :
         CashewValue action = getValue(0);
         String rtn = null;
         if (action.getDataType(getClock()).isCompatibleWith(PRIV_ACTION_TYPE)) {
            rtn = "java.security.PrivilegedAction.run";
          }
         else {
            rtn = "java.security.PrivilegedExceptionAction.run";
          }
         rslt = exec_runner.executeCall(rtn,action);
         break;
      default :
         return;
    }
   
   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}
}	// end of class CuminDirectEvaluation




/* end of CuminDirectEvaluation.java */

