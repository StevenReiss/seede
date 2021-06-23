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

import java.io.UnsupportedEncodingException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewSynchronizationModel;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueArray;
import edu.brown.cs.seede.cashew.CashewValueClass;
import edu.brown.cs.seede.cashew.CashewValueObject;
import edu.brown.cs.seede.cashew.CashewValueString;
import edu.brown.cs.seede.acorn.AcornLog;

class CuminDirectEvaluation extends CuminNativeEvaluator
{







/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminDirectEvaluation(CuminRunnerByteCode bc)
{
   super(bc);
}



/********************************************************************************/
/*										*/
/*	String methods								*/
/*										*/
/********************************************************************************/

CuminRunStatus checkStringMethods() throws CuminRunException, CashewException
{
   try {
      return checkStringMethodsLocal();
    }
   catch (IndexOutOfBoundsException e) {
      CuminEvaluator.throwException(getTyper(),e.getClass().getName());
    }
   
   return null;
}


private CuminRunStatus checkStringMethodsLocal() throws CuminRunException, CashewException
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "valueOf" :
	    JcompType dtyp = getDataType(0);
	    if (dtyp.isBooleanType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getBoolean(0)));
	     }
	    else if (dtyp.isCharType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getChar(0)));
	     }
	    else if (dtyp.isDoubleType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getDouble(0)));
	     }
	    else if (dtyp.isFloatType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getFloat(0)));
	     }
	    else if (dtyp.isLongType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getLong(0)));
	     }
	    else if (dtyp.isArrayType() && dtyp.getBaseType().isCharType()) {
	       if (getNumArgs() == 1) {
		  rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getCharArray(0)));
		}
	       else {
		  rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getCharArray(0),getInt(1),getInt(2)));
		}
	     }
	    else if (dtyp.isNumericType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.valueOf(getInt(0)));
	     }
	    else if (dtyp.isStringType()) {
	       rslt = getValue(0);
	     }
	    else {
	       // Object
	       return null;
	     }
	    break;
	 case "copyValueOf" :
	    if (getNumArgs() == 1) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.copyValueOf(getCharArray(0)));
	     }
	    else {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,String.copyValueOf(getCharArray(0),getInt(1),getInt(2)));
	     }
	    break;
	 case "format" :
	    return null;
	 default :
	    return null;
       }
    }
   else if (getMethod().isConstructor()) {
      CashewValueString cvs = (CashewValueString) getContext().findReference(0).getActualValue(getClock());
      if (getNumArgs() == 1) ;
      else if (getNumArgs() == 2 && getDataType(1).isStringType()) {
	 cvs.setInitialValue(getString(1));
       }
      else if (getNumArgs() == 2 && getDataType(1).getBaseType() != null) {
	 if (getNumArgs() == 2 && getDataType(1).getBaseType().isCharType()) {
	    String temp = new String(getCharArray(1));
	    cvs.setInitialValue(temp);
	  }
	 else if (getNumArgs() == 3 && getDataType(1).getBaseType().isCharType()) {	   // char[],boolean
	    String temp = new String(getCharArray(1));
	    cvs.setInitialValue(temp);
	  }
       }
      // handle various constructors
      else return null;
    }
   else { 
      CashewValue thisarg = getValue(0);
      String thisstr = thisarg.getString(getTyper(),getClock());
      switch (getMethod().getName()) {
	 case "charAt" :
	    rslt = CashewValue.characterValue(getTyper().CHAR_TYPE,thisstr.charAt(getInt(1)));
	    break;
	 case "codePointAt" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.codePointAt(getInt(1)));
	    break;
	 case "codePointBefore" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.codePointBefore(getInt(1)));
	    break;
	 case "codePointCount" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.codePointCount(getInt(1),getInt(2)));
	    break;
	 case "compareTo" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.compareTo(getString(1)));
	    break;
	 case "compareToIgnoreCase" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.compareToIgnoreCase(getString(1)));
	    break;
	 case "concat" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.concat(getString(1)));
	    break;
	 case "contains" :
	    if (getDataType(1).isStringType()) {
	       rslt = CashewValue.booleanValue(getTyper(),thisstr.contains(getString(1)));
	     }
	    else return null;
	    break;
	 case "contentEquals" :
	    if (getDataType(1).isStringType()) {
	       rslt = CashewValue.booleanValue(getTyper(),thisstr.contentEquals(getString(1)));
	     }
	    else return null;
	    break;
	 case "endsWith" :
	    rslt = CashewValue.booleanValue(getTyper(),thisstr.endsWith(getString(1)));
	    break;
	 case "equals" :
	    if (!getDataType(1).isStringType())
	       rslt = CashewValue.booleanValue(getTyper(),false);
	    else
	       rslt = CashewValue.booleanValue(getTyper(),thisstr.equals(getString(1)));
	    break;
	 case "equalsIgnoreCase" :
	    rslt = CashewValue.booleanValue(getTyper(),thisstr.equalsIgnoreCase(getString(1)));
	    break;
	 case "hashCode" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.hashCode());
	    break;
	 case "indexOf" :
	    if (getDataType(1).isIntType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.indexOf(getInt(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.indexOf(getInt(1),getInt(2)));
		}
	     }
	    else if (getDataType(1).isStringType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.indexOf(getString(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.indexOf(getString(1),getInt(2)));
		}
	     }
	    break;
	 case "isEmpty" :
	    rslt = CashewValue.booleanValue(getTyper(),thisstr.isEmpty());
	    break;
	 case "lastIndexOf" :
	    if (getDataType(1).isIntType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.lastIndexOf(getInt(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.lastIndexOf(getInt(1),getInt(2)));
		}
	     }
	    else if (getDataType(1).isStringType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.lastIndexOf(getString(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.lastIndexOf(getString(1),getInt(2)));
		}
	     }
	    break;
	 case "length" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.length());
	    break;
	 case "matches" :
	    rslt = CashewValue.booleanValue(getTyper(),thisstr.matches(getString(1)));
	    break;
	 case "offsetByCodePoints" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,thisstr.offsetByCodePoints(getInt(1),getInt(2)));
	    break;
	 case "regionMatches" :
	    if (getNumArgs() == 6) {
	       rslt = CashewValue.booleanValue(getTyper(),
		     thisstr.regionMatches(getBoolean(1),getInt(2),
		     getString(3),getInt(4),getInt(5)));
	     }
	    else {
	       rslt = CashewValue.booleanValue(getTyper(),
		     thisstr.regionMatches(getInt(1),getString(2),getInt(3),getInt(4)));
	     }
	    break;
	 case "replace" :
	    if (getDataType(0).isCharType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.replace(getChar(1),getChar(2)));
	     }
	    else if (getDataType(1).isStringType() && getDataType(2).isStringType()) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.replace(getString(1),getString(2)));
	     }
	    else return null;
	    break;
	 case "replaceAll" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.replaceAll(getString(1),getString(2)));
	    break;
	 case "replaceFirst" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.replaceFirst(getString(1),getString(2)));
	    break;
	 case "startsWith" :
	    if (getNumArgs() == 2) {
	       rslt = CashewValue.booleanValue(getTyper(),thisstr.startsWith(getString(1)));
	     }
	    else {
	       rslt = CashewValue.booleanValue(getTyper(),thisstr.startsWith(getString(1),getInt(2)));
	     }
	    break;
	 case "subSequence" :
	 case "substring" :
            try {
               int a0 = getInt(1);
               if (getNumArgs() == 2) {
                  rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.substring(a0));
                }
               else {
                  rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.substring(a0,getInt(2)));
                }
             }
            catch (IndexOutOfBoundsException e) {
               CuminEvaluator.throwException(getTyper(),"java.lang.StringIndexOutOfBoundsException");
             }
	    break;
	 case "toLowerCase" :
	    if (getNumArgs() == 1) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.toLowerCase());
	     }
	    else {
	       // need to get locale object
	       return null;
	     }
	    break;
	 case "toString" :
	    rslt = thisarg;
	    break;
	 case "toUpperCase" :
	    if (getNumArgs() == 1) {
	       rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.toUpperCase());
	     }
	    else {
	       // need to get locale object
	       return null;
	     }
	    break;
	 case "trim" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thisstr.trim());
	    break;

	 case "getBytes" :
	    byte [] bbuf = null;
	    if (getNumArgs() == 1) {
	       bbuf = thisstr.getBytes();
	     }
	    else if (getNumArgs() == 2 && getDataType(1).isStringType()) {
	       String cset = getString(1);
	       try {
		  bbuf = thisstr.getBytes(cset);
		}
	       catch (UnsupportedEncodingException e) {
		  CuminEvaluator.throwException(getTyper(),"java.io.UnsupportedEncodingException");
		}
	     }
	    if (bbuf == null) return null;
	    rslt = CashewValue.arrayValue(getTyper(),bbuf);
	    break;

	 case "intern":
	    return null;

	 case "getChars" :
	    int srcbegin = getInt(1);
	    int srcend = getInt(2);
	    CashewValue carr = getArrayValue(3);
	    int dstbegin = getInt(4);
	    getClock().freezeTime();
	    try {
	       for (int i = srcbegin; i < srcend; ++i) {
		  CashewValue charv = CashewValue.characterValue(getTyper().CHAR_TYPE,thisstr.charAt(i));
		  carr.setIndexValue(getClock(),dstbegin+i-srcbegin,charv);
		}
	     }
	    finally {
	       getClock().unfreezeTime();
	     }
	    break;

	 case "split" :
	    String [] sarr = null;
	    if (getNumArgs() == 2) {
	       sarr = thisstr.split(getString(1));
	     }
	    else {
	       sarr = thisstr.split(getString(1),getInt(2));
	     }
	    rslt = CashewValue.arrayValue(getTyper(),sarr);
	    break;

	 case "toCharArray" :
	    rslt = CashewValue.arrayValue(getTyper(),thisstr.toCharArray());
	    break;

	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Strict Math native methods						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkMathMethods() throws CashewException
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "abs" :
	 if (getDataType(0).isDoubleType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.abs(getDouble(0)));
	  }
	 else if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.abs(getFloat(0)));
	  }
	 else if (getDataType(0).isLongType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.abs(getLong(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,StrictMath.abs(getInt(0)));
	  }
	 break;
      case "acos" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.acos(getDouble(0)));
	 break;
      case "asin" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.asin(getDouble(0)));
	 break;
      case "atan" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.atan(getDouble(0)));
	 break;
      case "atan2" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.atan2(getDouble(0),getDouble(2)));
	 break;
      case "cbrt" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.cbrt(getDouble(0)));
	 break;
      case "ceil" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.ceil(getDouble(0)));
	 break;
      case "copySign" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.copySign(getFloat(0),getFloat(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.copySign(getDouble(0),getDouble(2)));
	  }
	 break;
      case "cos" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.cos(getDouble(0)));
	 break;
      case "cosh" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.cosh(getDouble(0)));
	 break;
      case "exp" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.exp(getDouble(0)));
	 break;
      case "expm1" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.expm1(getDouble(0)));
	 break;
      case "floor" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.floor(getDouble(0)));
	 break;
      case "getExponent" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,StrictMath.getExponent(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,StrictMath.getExponent(getDouble(0)));
	  }
	 break;
      case "hypot" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.hypot(getDouble(0),getDouble(2)));
	 break;
      case "IEEEremainder" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.IEEEremainder(getDouble(0),getDouble(2)));
	 break;
      case "log" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.log(getDouble(0)));
	 break;
      case "log10" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.log10(getDouble(0)));
	 break;
      case "log1p" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.log1p(getDouble(0)));
	 break;
      case "max" :
	 if (getDataType(0).isDoubleType() || getDataType(1).isDoubleType()) {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.max(getDouble(0),getDouble(2)));
	  }
	 else if (getDataType(0).isFloatType() || getDataType(1).isFloatType()) {
	    rslt = CashewValue.numericValue(getTyper().FLOAT_TYPE,StrictMath.max(getFloat(0),getFloat(1)));
	  }
	 else if (getDataType(0).isLongType() || getDataType(1).isLongType()) {
	    rslt = CashewValue.numericValue(getTyper().LONG_TYPE,StrictMath.max(getLong(0),getLong(2)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,StrictMath.max(getInt(0),getInt(1)));
	  }
	 break;
      case "min" :
	 if (getDataType(0).isDoubleType() || getDataType(1).isDoubleType()) {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.min(getDouble(0),getDouble(2)));
	  }
	 else if (getDataType(0).isFloatType() || getDataType(1).isFloatType()) {
	    rslt = CashewValue.numericValue(getTyper().FLOAT_TYPE,StrictMath.min(getFloat(0),getFloat(1)));
	  }
	 else if (getDataType(0).isLongType() || getDataType(1).isLongType()) {
	    rslt = CashewValue.numericValue(getTyper().LONG_TYPE,StrictMath.min(getLong(0),getLong(2)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,StrictMath.min(getInt(0),getInt(1)));
	  }
	 break;
      case "nextAfter" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.nextAfter(getFloat(0),getDouble(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.nextAfter(getDouble(0),getDouble(2)));
	  }
	 break;
      case "nextUp" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.nextUp(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.nextUp(getDouble(0)));
	  }
	 break;
      case "pow" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.pow(getDouble(0),getDouble(2)));
	 break;
      case "random" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.random());
	 break;
      case "rint" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.rint(getDouble(0)));
	 break;
      case "round" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,StrictMath.round(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().LONG_TYPE,StrictMath.round(getDouble(0)));
	  }
	 break;
      case "scalb" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getTyper().FLOAT_TYPE,StrictMath.scalb(getFloat(0),getInt(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.scalb(getDouble(0),getInt(2)));
	  }
	 break;
      case "signum" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getTyper().FLOAT_TYPE,StrictMath.signum(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.signum(getDouble(0)));
	  }
	 break;
      case "sin" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.sin(getDouble(0)));
	 break;
      case "sinh" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.sinh(getDouble(0)));
	 break;
      case "sqrt" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.sqrt(getDouble(0)));
	 break;
      case "tan" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.tan(getDouble(0)));
	 break;
      case "tanh" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.tanh(getDouble(0)));
	 break;
      case "toDegrees" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.toDegrees(getDouble(0)));
	 break;
      case "toRadians" :
	 rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.toRadians(getDouble(0)));
	 break;
      case "ulp" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getTyper().FLOAT_TYPE,StrictMath.ulp(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,StrictMath.ulp(getDouble(0)));
	  }
	 break;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




/********************************************************************************/
/*										*/
/*	Native Runtime methods							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkRuntimeMethods()
{
   CashewValue rslt = null;
   Runtime rt = Runtime.getRuntime();

   switch (getMethod().getName()) {
      case "availableProcessors" :
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,rt.availableProcessors());
	 break;
      case "freeMemory" :
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,rt.freeMemory());
	 break;
      case "gc" :
      case "traceInstructions" :
      case "traceMethodCalls" :
	 break;
      case "maxMemory" :
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,rt.maxMemory());
	 break;
      case "totalMemory" :
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,rt.totalMemory());
	 break;
      case "halt" :
      case "exit" :
	 return CuminRunStatus.Factory.createHalt();

      case "exec" :
      case "load" :
      case "loadLibrary" :
      case "runFinalization" :
	 //TODO: handle the various exec and load calls
	 return null;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Handle float/double methods						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkFloatMethods() throws CashewException
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "floatToIntBits" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,Float.floatToIntBits(getFloat(0)));
	    break;
	 case "floatToRawIntBits" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,Float.floatToRawIntBits(getFloat(0)));
	    break;
	 case "intBitsToFloat" :
	    rslt = CashewValue.numericValue(getTyper().FLOAT_TYPE,Float.intBitsToFloat(getInt(0)));
	    break;
	 case "isInfinite" :
	    rslt = CashewValue.booleanValue(getTyper(),Float.isInfinite(getFloat(0)));
	    break;
	 case "isNaN" :
	    rslt = CashewValue.booleanValue(getTyper(),Float.isNaN(getFloat(0)));
	    break;
	 case "toHexString" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,Float.toHexString(getFloat(0)));
	    break;
	 case "toString" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,Float.toString(getFloat(0)));
	    break;
	 default :
	    return null;
       }
    }
   else if (getMethod().isConstructor()) {
      return null;
    }
   else {
      switch (getMethod().getName()) {
	 case "hashCode" :
	    Float f = Float.valueOf(getFloat(0));
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,f.hashCode());
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



CuminRunStatus checkDoubleMethods() throws CashewException
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "doubleToLongBits" :
	    rslt = CashewValue.numericValue(getTyper().LONG_TYPE,Double.doubleToLongBits(getFloat(0)));
	    break;
	 case "doubleToRawLongBits" :
	    rslt = CashewValue.numericValue(getTyper().LONG_TYPE,Double.doubleToRawLongBits(getFloat(0)));
	    break;
	 case "longBitsToDouble" :
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,Double.longBitsToDouble(getLong(0)));
	    break;
	 case "isInfinite" :
	    rslt = CashewValue.booleanValue(getTyper(),Double.isInfinite(getDouble(0)));
	    break;
	 case "isNaN" :
	    rslt = CashewValue.booleanValue(getTyper(),Double.isNaN(getDouble(0)));
	    break;
	 case "toHexString" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,Double.toHexString(getDouble(0)));
	    break;
	 case "toString" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,Double.toString(getDouble(0)));
	    break;
	 default :
	    return null;
       }
    }
   else if (getMethod().isConstructor()) {
      return null;
    }
   else {
      switch (getMethod().getName()) {
	 case "hashCode" :
	    Double d = Double.valueOf(getDouble(0));
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,d.hashCode());
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




/********************************************************************************/
/*										*/
/*	System methods								*/
/*										*/
/********************************************************************************/

CuminRunStatus checkSystemMethods() throws CashewException
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "arraycopy" :
	 CuminRunStatus sts = handleArrayCopy(getValue(0),getInt(1),getValue(2),getInt(3),getInt(4));
	 if (sts != null) return sts;
	 break;
      case "currentTimeMillis" :
	 rslt = CashewValue.numericValue(getTyper().LONG_TYPE,System.currentTimeMillis());
	 break;
      case "exit" :
	 return CuminRunStatus.Factory.createHalt();
      case "gc" :
	 break;
      case "identityHashCode" :
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,getValue(0).getFieldValue(getTyper(),getClock(),HASH_CODE_FIELD).
	       getNumber(getClock()).intValue());
	 break;
      case "load" :
      case "loadLibrary" :
	 break;
      case "mapLibraryName" :
	 rslt = CashewValue.stringValue(getTyper().STRING_TYPE,System.mapLibraryName(getString(0)));
	 break;
      case "nanoTime" :
	 rslt = CashewValue.numericValue(getTyper().LONG_TYPE,System.nanoTime());
	 break;
      case "runFinalization" :
	 break;
      case "setErr" :
      case "setIn" :
      case "setOut" :
	 break;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



private CuminRunStatus handleArrayCopy(CashewValue src,int spos,CashewValue dst,int dpos,int len)
	throws CashewException
{
   if (src.isNull(getClock()) || dst.isNull(getClock())) {
      return CuminEvaluator.returnException(getTyper(),"java.lang.NullPointerException");
    }

   int sdim = -1;
   int ddim = -1;
   try {
      sdim = src.getDimension(getClock());
      ddim = dst.getDimension(getClock());
    }
   catch (Throwable t) {
      return CuminEvaluator.returnException(getTyper(),"java.lang.ArrayStoreException");
    }
   // check array element types

   if (spos < 0 || dpos < 0 || len < 0 || spos+len > sdim || dpos+len > ddim) {
      return CuminEvaluator.returnException(getTyper(),"java.lang.ArrayIndexOutOfBoundsException");
    }

   getClock().freezeTime();
   try {
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
   finally {
      getClock().unfreezeTime();
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Object methods								*/
/*										*/
/********************************************************************************/

CuminRunStatus checkObjectMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "<init>" :
	 break;
      case "getClass" :
	rslt = CashewValue.classValue(getTyper(),getDataType(0));
	break;
      case "equals" :
	 rslt = CashewValue.booleanValue(getTyper(),getValue(0) == getValue(1));
	 break;
      case "hashCode" :
	 if (getValue(0).isNull(getClock()))
	    return CuminEvaluator.returnException(getTyper(),"java.lang.NullPointerException");
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,getValue(0).getFieldValue(getTyper(),getClock(),HASH_CODE_FIELD).
	       getNumber(getClock()).intValue());
	 break;
      case "clone" :
	 CashewValue v0 = getValue(0);
	 if (v0.isNull(getClock()))
	    return CuminEvaluator.returnException(getTyper(),"java.lang.NullPointerException");
	 if (v0 instanceof CashewValueArray) {
	    CashewValueArray arr = (CashewValueArray) v0;
	    rslt = arr.cloneObject(getTyper(),getClock(),0);
	  }
	 else {
	    CashewValueObject obj = (CashewValueObject) v0;
	    rslt = obj.cloneObject(getTyper(),getClock(),0);
	  }
	 break;
      case "wait" :
	 CashewSynchronizationModel scm = getContext().getSynchronizationModel();
	 if (scm != null) {
	    long time = 0;
	    if (getNumArgs() > 1) time = getLong(1);
	    try {
	       scm.synchWait(getValue(0),time);
	     }
	    catch (InterruptedException ex) {
	       CuminEvaluator.throwException(getTyper(),"java.lang.InterruptedException");
	     }
	    break;
	  }
	 return CuminRunStatus.Factory.createWait();

      case "notify" :
	 scm = getContext().getSynchronizationModel();
	 if (scm != null) {
	    scm.synchNotify(getValue(0),false);
	  }
	 break;
      case "notifyAll" :
	 scm = getContext().getSynchronizationModel();
	 if (scm != null) {
	    scm.synchNotify(getValue(0),false);
	  }
	 break;
      case "toString" :
	 rslt = CashewValue.stringValue(getTyper().STRING_TYPE,getString(0));
	 break;
      default :
	 AcornLog.logD("UNKNOWN CALL TO OBJECT: " + getMethod().getName());
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}








/********************************************************************************/
/*										*/
/*	Class methods								*/
/*										*/
/********************************************************************************/

CuminRunStatus checkClassMethods() throws CashewException, CuminRunException
{
   CashewValue rslt = null;
   JcompType rtype = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "forName" :
	    String cls = getString(0);
	    JcompTyper typer = exec_runner.getTyper();
	    JcompType typ1 = typer.findType(cls);
	    if (typ1 == null) typ1 = typer.findSystemType(cls);
	    if (typ1 == null) return null;
	    rslt = CashewValue.classValue(getTyper(),typ1);
	    break;
	 default :
	    return null;
       }
    }
   else {
      CashewValue thisarg = getValue(0);
      JcompType thistype = ((CashewValueClass) thisarg).getJcompType();
      switch (getMethod().getName()) {
	 case "getComponentType" :
	    if (thistype.isArrayType()) {
	       rtype = thistype.getBaseType();
	     }
	    else rslt = CashewValue.nullValue(getTyper());
	    break;
	 case "getName" :
	    rslt = CashewValue.stringValue(getTyper().STRING_TYPE,thistype.getName());
	    break;
	 case "getClassLoader" :
	 case "getClassLoader0" :
	    exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
	    String expr = "edu.brown.cs.seede.poppy.PoppyValue.getClassLoaderUsingPoppy(\"" +
		thistype.getName() + "\")";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "newInstance" :
	    JcompSymbol csym = null;
	    JcompType ctyp = getTyper().createMethodType(null,null,false,null);
	    csym = thistype.lookupMethod(getTyper(),"<init>",ctyp);
	    if (csym != null) {
	       CashewValue nv = exec_runner.handleNew(thistype);
	       List<CashewValue> argv = new ArrayList<>();
	       argv.add(nv);
	       exec_runner.getStack().push(nv);
	       CuminRunner nrun = exec_runner.handleCall(getClock(),csym,argv,CallType.SPECIAL);
	       return CuminRunStatus.Factory.createCall(nrun);
	     }
	    // exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
	    // expr = "edu.brown.cs.seede.poppy.PoppyValue.getNewInstance(\"" +
	    // thistype.getName() + "\")";
	    // rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "isPrimitive" :
	    rslt = CashewValue.booleanValue(getTyper(),thistype.isPrimitiveType());
	    break;
	 case "isArray" :
	    rslt = CashewValue.booleanValue(getTyper(),thistype.isArrayType());
	    break;
	 case "isInterface" :
	    rslt = CashewValue.booleanValue(getTyper(),thistype.isInterfaceType());
	    break;
	 case "getModifiers" :
	    JcompSymbol sym = thistype.getDefinition();
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,sym.getModifiers());
	    break;
	 case "getSuperclass" :
	    rtype = thistype.getSuperType();
	    if (rtype == null) rslt = CashewValue.nullValue(getTyper());
	    break;
	 case "getConstructor" :
	    expr = "edu.brown.cs.seede.poppy.PoppyValue.getConstructorUsingPoppy(\"" +
		thistype.getName() + "\"";
	    CashewValue av = getValue(1);
	    int sz = av.getFieldValue(getTyper(),getClock(),"length").getNumber(getClock()).intValue();
	    for (int i = 0; i < sz; ++i) {
	       CashewValue tv = av.getIndexValue(getClock(),i);
	       tv = tv.getActualValue(getClock());
	       JcompType argtype = ((CashewValueClass) tv).getJcompType();
	       expr += ",\"" + argtype.getName() + "\"";
	     }
	    expr += ")";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "getDeclaredConstructor" :
	    expr = "edu.brown.cs.seede.poppy.PoppyValue.getDeclaredConstructorUsingPoppy(\"" +
	    thistype.getName() + "\"";
	    av = getValue(1);
	    sz = av.getFieldValue(getTyper(),getClock(),"length").getNumber(getClock()).intValue();
	    for (int i = 0; i < sz; ++i) {
	       CashewValue tv = av.getIndexValue(getClock(),i);
	       tv = tv.getActualValue(getClock());
	       JcompType argtype = ((CashewValueClass) tv).getJcompType();
	       expr += ",\"" + argtype.getName() + "\"";
	     }
	    expr += ")";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "getMethod" :
	 case "getDeclaredMethod" :
	    expr = "edu.brown.cs.seede.poppy.PoppyValue.getMethodUsingPoppy(\"" +
	    thistype.getName() + "\"";
	    String nm = getString(1);
	    expr += ",\"" + nm + "\"";
	    boolean declfg = (getMethod().getName().equals("getMethod") ? false : true);
	    expr += "," + declfg;
	    av = getValue(2);
	    sz = av.getFieldValue(getTyper(),getClock(),"length").getNumber(getClock()).intValue();
	    for (int i = 0; i < sz; ++i) {
	       CashewValue tv = av.getIndexValue(getClock(),i);
	       tv = tv.getActualValue(getClock());
	       JcompType argtype = ((CashewValueClass) tv).getJcompType();
	       expr += ",\"" + argtype.getName() + "\"";
	     }
	    expr += ")";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "getDelcaredConstructors" :
	 case "privateGetDeclaredConstructors" :
	 case "getDeclaredConstructors0" :
	    exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
	    boolean fg = false;
	    if (getNumArgs() == 2) fg = getBoolean(1);
	    expr = "edu.brown.cs.seede.poppy.PoppyValue.getDeclaredConstructorsUsingPoppy(\"" +
		thistype.getName() + "\"," + fg + ")";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "isInstance" :
	    CashewValue v0 = getValue(1);
	    fg = v0.getDataType(getClock(),getTyper()).isCompatibleWith(thistype);
	    rslt = CashewValue.booleanValue(getTyper(),fg);
	    break;
	 default :
	    AcornLog.logE("Unknown call to java.lang.Class." + getMethod());
	    return null;
       }
    }
   if (rslt == null && rtype != null) {
      rslt = CashewValue.classValue(getTyper(),rtype);
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



CuminRunStatus checkClassReturn(CuminRunStatus r)
{
   if (!getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "newInstance" :
	    CashewValue rslt = exec_runner.getStack().pop();
	    return CuminRunStatus.Factory.createReturn(rslt);
       }
    }

   return r;

}



CuminRunStatus checkConstructorMethods()
{
   switch (getMethod().getName()) {
      case "newInstance" :
	 try {
	    CashewValue cnst = getValue(0);
	    CashewValue clzz = cnst.getFieldValue(getTyper(),getClock(),"java.lang.reflect.Constructor.clazz");
	    clzz = clzz.getActualValue(getClock());
	    CashewValue prms = cnst.getFieldValue(getTyper(),getClock(),"java.lang.reflect.Constructor.parameterTypes");
	    prms = prms.getActualValue(getClock());
	    CashewValueClass cvc = (CashewValueClass) clzz;
	    JcompType newtyp = cvc.getJcompType();
	    List<JcompType> argtyp = new ArrayList<>();
	    CashewValue szv = prms.getFieldValue(getTyper(),getClock(),"length");
	    int sz = szv.getNumber(getClock()).intValue();
	    for (int i = 0; i < sz; ++i) {
	       CashewValue v0 = prms.getIndexValue(getClock(),i);
	       CashewValueClass avc = (CashewValueClass) v0.getActualValue(getClock());
	       argtyp.add(avc.getJcompType());
	     }
	    JcompType mtyp = getTyper().createMethodType(null,argtyp,false,null);
	    JcompSymbol jsym = newtyp.lookupMethod(getTyper(),"<init>",mtyp);
	    if (jsym != null) {
	       CashewValue pval = getValue(1);
	       CashewValue nv = exec_runner.handleNew(newtyp);
	       exec_runner.getStack().push(nv);
	       List<CashewValue> argv = new ArrayList<>();
	       argv.add(nv);
	       CashewValue pszv = pval.getFieldValue(getTyper(),getClock(),"length");
	       int psz = pszv.getNumber(getClock()).intValue();
	       for (int i = 0; i < sz; ++i) {
		  JcompType t0 = argtyp.get(i);
		  CashewValue v0 = pval.getIndexValue(getClock(),i);
		  if (i == sz-1 && jsym.getType().isVarArgs()) {
		     if (psz == sz) {
			// check for array as last arg
		      }
		     // handle varargs here
		   }
		  CashewValue v1 = CuminEvaluator.castValue(exec_runner,v0,t0);
		  argv.add(v1);
		}
	       CuminRunner nrun = exec_runner.handleCall(getClock(),jsym,argv,CallType.SPECIAL);
	       return CuminRunStatus.Factory.createCall(nrun);
	     }
	  }
	 catch (CashewException e) {

	  }
	 catch (CuminRunException _ex) {}

	 // todo: create object and call constructor
	 break;
    }
   return null;
}



CuminRunStatus checkConstructorReturn(CuminRunStatus r)
{
   if (!getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "newInstance" :
	    CashewValue rslt = exec_runner.getStack().pop();
	    return CuminRunStatus.Factory.createReturn(rslt);
       }
    }

   return r;

}


CuminRunStatus checkMethodMethods()
{
   switch (getMethod().getName()) {
      case "invoke" :
	 try {
	    CashewValue mthd = getValue(0);
	    CashewValue clzz = mthd.getFieldValue(getTyper(),getClock(),"java.lang.reflect.Method.clazz");
	    clzz = clzz.getActualValue(getClock());
	    CashewValue prms = mthd.getFieldValue(getTyper(),getClock(),"java.lang.reflect.Method.parameterTypes");
	    prms = prms.getActualValue(getClock());
	    CashewValue rettvl = mthd.getFieldValue(getTyper(),getClock(),"java.lang.reflect.Method.returnType");
	    rettvl = rettvl.getActualValue(getClock());
	    CashewValue namvl = mthd.getFieldValue(getTyper(),getClock(),"java.lang.reflect.Method.name");
	    String name = namvl.getString(getTyper(),getClock());
	    CashewValueClass cvc = (CashewValueClass) clzz;
	    JcompType newtyp = cvc.getJcompType();
	    CashewValueClass rvc = (CashewValueClass) rettvl;
	    JcompType rettyp = rvc.getJcompType();
	    List<JcompType> argtyp = new ArrayList<>();
	    CashewValue szv = prms.getFieldValue(getTyper(),getClock(),"length");
	    int sz = szv.getNumber(getClock()).intValue();
	    for (int i = 0; i < sz; ++i) {
	       CashewValue v0 = prms.getIndexValue(getClock(),i);
	       CashewValueClass avc = (CashewValueClass) v0.getActualValue(getClock());
	       argtyp.add(avc.getJcompType());
	     }
	    JcompType mtyp = getTyper().createMethodType(rettyp,argtyp,false,null);
	    JcompSymbol jsym = newtyp.lookupMethod(getTyper(),name,mtyp);
	    if (jsym != null) {
	       CashewValue pval = getValue(2);
	       List<CashewValue> argv = new ArrayList<>();
	       if (!jsym.isStatic()) {
		  argv.add(getValue(1));
		}
	       CashewValue pszv = pval.getFieldValue(getTyper(),getClock(),"length");
	       int psz = pszv.getNumber(getClock()).intValue();
	       for (int i = 0; i < sz; ++i) {
		  JcompType t0 = argtyp.get(i);
		  CashewValue v0 = pval.getIndexValue(getClock(),i);
		  if (i == sz-1 && jsym.getType().isVarArgs()) {
		     if (psz == sz) {
			// check for array as last arg
		      }
		     // handle varargs here
		   }
		  CashewValue v1 = CuminEvaluator.castValue(exec_runner,v0,t0);
		  argv.add(v1);
		}
	       CuminRunner nrun = exec_runner.handleCall(getClock(),jsym,argv,CallType.SPECIAL);
	       return CuminRunStatus.Factory.createCall(nrun);
	     }
	  }
	 catch (CashewException e) {

	  }
	 catch (CuminRunException e) { }
	 break;
    }
   return null;
}



/********************************************************************************/
/*										*/
/*	Thread methods								*/
/*										*/
/********************************************************************************/

CuminRunStatus checkThreadMethods()
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "currentThread" :
	    rslt = exec_runner.getLookupContext().findStaticFieldReference(
		  getTyper(),CURRENT_THREAD_FIELD,"java.lang.Thread");
	    break;
	 default :
	    return null;
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
	    rslt = CashewValue.booleanValue(getTyper(),false);
	    break;
	 case "isInterrupted" :
	    rslt = CashewValue.booleanValue(getTyper(),false);
	    break;
	 case "isAlive" :
	    rslt = CashewValue.booleanValue(getTyper(),true);
	    break;
	 case "countStackFrames" :
	    rslt = CashewValue.numericValue(getTyper().INT_TYPE,1);
	    break;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




/********************************************************************************/
/*										*/
/*	Throwable/Exception methods						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkThrowableMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "fillInStackTrace" :
	 rslt = getValue(0);
	 break;
      case "getStackTraceDepth" :
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,0);
	 break;
      case "getStackTraceElement" :
	 return CuminEvaluator.returnException(getTyper(),"java.lang.IndexOutOfBoundsException");
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




/********************************************************************************/
/*										*/
/*	Handle sun.misc.FloatingDecimal for efficiency				*/
/*										*/
/********************************************************************************/

CuminRunStatus checkFloatingDecimalMehtods() throws CashewException
{
   CashewValue rslt = null;
   String s1;

   switch (getMethod().getName()) {
      case "toJavaFormatString" :
	 if (getDataType(0).isFloatType()) {
	    s1 = String.valueOf(getFloat(0));
	  }
	 else s1 = String.valueOf(getDouble(0));
	 rslt = CashewValue.stringValue(getTyper().STRING_TYPE,s1);
	 break;
      case "parseDouble" :
	 try {
	    double d1 = Double.parseDouble(getString(0));
	    rslt = CashewValue.numericValue(getTyper().DOUBLE_TYPE,d1);
	  }
	 catch (NumberFormatException e) {
	    return CuminEvaluator.returnException(getTyper(),"java.lang.NumberFormatException");
	  }
	 break;
      case "parseFloat" :
	 try {
	    float f1 = Float.parseFloat(getString(0));
	    rslt = CashewValue.numericValue(getTyper().FLOAT_TYPE,f1);
	  }
	 catch (NumberFormatException e) {
	    return CuminEvaluator.returnException(getTyper(),"java.lang.NumberFormatException");
	  }
	 break;
      default  :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}





/********************************************************************************/
/*										*/
/*	Swing handling methods							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkSwingUtilityMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "isEventDispatchThread" :
	 rslt = CashewValue.booleanValue(getTyper(),true);
	 break;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




/********************************************************************************/
/*										*/
/*	Reflection methods							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkReflectArrayMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "get" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 break;
      case "getBoolean" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().BOOLEAN_TYPE);
	 break;
      case "getByte" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().BYTE_TYPE);
	 break;
      case "getChar" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().CHAR_TYPE);
	 break;
      case "getDouble" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().DOUBLE_TYPE);
	 break;
      case "getFloat" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().FLOAT_TYPE);
	 break;
      case "getInt" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().INT_TYPE);
	 break;
      case "getLong" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().LONG_TYPE);
	 break;
      case "getShort" :
	 rslt = getArrayValue(0).getIndexValue(getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,getTyper().SHORT_TYPE);
	 break;
      case "set" :
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),getValue(2));
	 break;
      case "setBoolean" :
	 CashewValue cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().BOOLEAN_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "setByte" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().BYTE_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "setChar" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().CHAR_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "setDouble" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().DOUBLE_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "setFloat" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().FLOAT_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "setInt" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().INT_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "setLong" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().LONG_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "setShort" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),getTyper().SHORT_TYPE);
	 getArrayValue(0).setIndexValue(getClock(),getInt(1),cv);
	 break;
      case "newArray" :
	 JcompType atype = getTypeValue(0).getJcompType();
	 atype = exec_runner.getTyper().findArrayType(atype);
	 rslt = CashewValue.arrayValue(getTyper(),atype,getInt(1));
	 break;
      case "multiNewArray" :
	 atype = getTypeValue(0).getJcompType();
	 int [] dims = getIntArray(1);
	 rslt = CuminEvaluator.buildArray(exec_runner,0,dims,atype);
	 break;
      case "getLength" :
	 rslt = getArrayValue(0);
	 rslt = rslt.getFieldValue(getTyper(),getClock(),"length",false);
	 break;
      default :
	 return null;
    }

   return CuminRunValue.Factory.createReturn(rslt);
}


CuminRunStatus checkSunReflectionMethods() throws CuminRunException
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "getClassAccessFlags" :
	 rslt = CashewValue.numericValue(getTyper().INT_TYPE,Modifier.PUBLIC);
	 break;
      case "getCallerClass" :
	 CuminRunner cr = exec_runner.getOuterCall();
	 if (cr == null) return null;
	 CuminRunner cr1 = cr.getOuterCall();
	 if (cr1 == null) return null;
	 String cls = cr1.getCallingClass();
	 JcompTyper typer = exec_runner.getTyper();
	 JcompType typ1 = typer.findType(cls);
	 if (typ1 == null) typ1 = typer.findSystemType(cls);
	 if (typ1 == null) return null;
	 rslt = CashewValue.classValue(getTyper(),typ1);
	 break;
      default :
	 return null;
    }

   return CuminRunValue.Factory.createReturn(rslt);
}



CuminRunStatus checkClassAtomicMethods() throws CuminRunException
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "casReflectionData" :
      case "casAnnotationType" :
      case "casAnnotationData" :
	 synchronized (this) {
	    rslt = CashewValue.booleanValue(getTyper(),true);
	    return CuminRunValue.Factory.createReturn(rslt);
	  }
    }
   return null;
}




/********************************************************************************/
/*										*/
/*	Access controller methods						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkAccessControllerMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "doPrivileged" :
	 CashewValue action = getValue(0);
	 String rtn = null;
	 JcompType patype = getTyper().findSystemType("java.security.PrivilegedAction");
	 if (action.getDataType(getClock(),getTyper()).isCompatibleWith(patype)) {
	    rtn = "java.security.PrivilegedAction.run";
	  }
	 else {
	    rtn = "java.security.PrivilegedExceptionAction.run";
	  }
	 rslt = exec_runner.executeCall(rtn,action);
	 break;
      case "getContext" :
	 String expr = "java.security.AccessController.getContext()";
	 rslt = exec_runner.getLookupContext().evaluate(expr);
	 break;
      default :
	 return null;
    }

   return CuminRunValue.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Random number methods							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkRandomMethods() throws CuminRunException
{
   if (getMethod().isConstructor() && getNumArgs() == 1) {
      // new Random()
      JcompType inttype = getTyper().LONG_TYPE;
      String tnm = getMethod().getDeclaringClass().getName();
      JcompType thistype = getTyper().findType(tnm);
      List<JcompType> argt = new ArrayList<>();
      argt.add(inttype);
      JcompType mtyp = getTyper().createMethodType(null,argt,false,null);
      JcompSymbol jsym = thistype.lookupMethod(getTyper(),"<init>",mtyp);

      if (jsym != null) {
	 List<CashewValue> argv = new ArrayList<>();
	 CashewValue cv = getValue(0);
	 argv.add(cv);
	 CashewValue nv = CashewValue.numericValue(inttype,173797);
	 argv.add(nv);
	 CuminRunner nrun = exec_runner.handleCall(getClock(),jsym,argv,CallType.SPECIAL);
	 return CuminRunStatus.Factory.createCall(nrun);
       }
      // exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
      // String expr = "edu.brown.cs.seede.poppy.PoppyValue.getDefaultRandom()";
      // CashewValue nrslt = exec_runner.getLookupContext().evaluate(expr);
      //
      // CashewValue rslt = getContext().findReference(0).getActualValue(getClock());
      // try {
	 // CashewValue srslt = nrslt.getFieldValue(getTyper(),getClock(),"java.util.Random.seed");
	 // rslt.setFieldValue(getTyper(),getClock(),"java.util.Random.seed",srslt);
	 // return  CuminRunStatus.Factory.createReturn();
       // }
      // catch (CashewException e) { }
    }

   return null;
}




/********************************************************************************/
/*                                                                              */
/*      Pattern methods                                                         */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkPatternMethods() throws CuminRunException 
{
   switch (getMethod().getName()) {
      case "matcherX" :
         try {
            CashewValue patv = getValue(0);
            CashewValue pats = patv.getFieldValue(getTyper(),getClock(),"java.util.regex.Pattern.pattern");
            String ps = pats.getString(getTyper(),getClock());
            String v = getString(1);
            exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
	    String expr = "edu.brown.cs.seede.poppy.PoppyValue.getPatternMatcher(\"" +
                  IvyFormat.formatString(ps) + "\",\"" + IvyFormat.formatString(v)  + "\")";
	    CashewValue rslt = exec_runner.getLookupContext().evaluate(expr);
            return CuminRunStatus.Factory.createReturn(rslt);
//          Pattern p = Pattern.compile(ps);
//          Matcher m = p.matcher(v);
//          System.err.println("RESULT IS " + m);
          }
         catch (CashewException e) { 
            AcornLog.logE("CUMIN","Problem getting pattern information",e);
          }
         break;
    }
   return null;
}



CuminRunStatus checkMatcherMethods() throws CuminRunException
{
   if (getMethod().isConstructor() || getMethod().isStatic()) return null;

   CashewValue mval = getValue(0);
   CashewValue retv = null;
   try {
      String ps = getPatternTextFromMatcher(mval);
      String textv = getStringFieldValue(mval,"java.util.regex.Matcher.text");
   
      switch (getMethod().getName()) {
         case "find" :
            int first = getIntFieldValue(mval,"java.util.regex.Matcher.first");
            int last = getIntFieldValue(mval,"java.util.regex.Matcher.last");
            int from = getIntFieldValue(mval,"java.util.regex.Matcher.from");
            int to = getIntFieldValue(mval,"java.util.regex.Matcher.to");
            int pos = last;
            boolean fail = false;
            if (getNumArgs() == 2) {
               pos = getInt(1);
             }
            else {
               if (pos == first) pos = last+1;
               if (pos < from) pos = from;
               if (pos > to) {
                  fail = true;
                }
             }
            retv = runMatcher(mval,ps,textv,pos,fail,-1);
            break;
         case "matches" :
            retv = runMatcher(mval,ps,textv,-1,false,1);
            break;
         case "lookingAt" :
            retv = runMatcher(mval,ps,textv,-1,false,0);
            break;
       }
    }
   catch (CashewException e) {
      AcornLog.logE("CUMIN","Problem getting pattern information",e);
    }
   
   if (retv != null) return CuminRunStatus.Factory.createReturn(retv);
   
   return null;
}


private CashewValue runMatcher(CashewValue mval,String ps,String textv,int pos,boolean fail,int anch)
        throws CashewException
{
   int from = getIntFieldValue(mval,"java.util.regex.Matcher.from");
   int to = getIntFieldValue(mval,"java.util.regex.Matcher.to");  String expr = "edu.brown.cs.seede.poppy.PoppyValue.matchFinder(\"" +
      IvyFormat.formatString(ps) + "\",\"" + 
      IvyFormat.formatString(textv)  + "\"," + pos + "," + from + "," + to + "," + fail + ",-1)";
   CashewValue rslt = exec_runner.getLookupContext().evaluate(expr);
   copyMatcherFields(rslt,mval);
   int first = getIntFieldValue(rslt,"java.util.regex.Matcher.first");
   CashewValue retv = null;
   if (first >= 0) retv = CashewValue.booleanValue(getTyper(),true);
   else retv = CashewValue.booleanValue(getTyper(),false);
   return retv;
}



private String getPatternTextFromMatcher(CashewValue mval) throws CashewException
{
   CashewValue patv = mval.getFieldValue(getTyper(),getClock(),"java.util.regex.Matcher.parentPattern");
   if (patv.isNull(getClock())) return null;
   
   String ps = getStringFieldValue(patv,"java.util.regex.Pattern.pattern");
   return ps;
}


private void copyMatcherFields(CashewValue from,CashewValue to) throws CashewException
{
   copyField(from,to,"java.util.regex.Matcher.groups");
   copyField(from,to,"java.util.regex.Matcher.from");
   copyField(from,to,"java.util.regex.Matcher.to");
   copyField(from,to,"java.util.regex.Matcher.from");
   copyField(from,to,"java.util.regex.Matcher.first");
   copyField(from,to,"java.util.regex.Matcher.last");
   copyField(from,to,"java.util.regex.Matcher.lookbehindTo");
   copyField(from,to,"java.util.regex.Matcher.oldLast");
   copyField(from,to,"java.util.regex.Matcher.lastAppendPosition");
   copyField(from,to,"java.util.regex.Matcher.locals");
   copyField(from,to,"java.util.regex.Matcher.localsPos");
   copyField(from,to,"java.util.regex.Matcher.hitEnd");
   copyField(from,to,"java.util.regex.Matcher.requireEnd");
   copyField(from,to,"java.util.regex.Matcher.modCount");
   copyField(from,to,"java.util.regex.Matcher.acceptMode");
}



}	// end of class CuminDirectEvaluation




/* end of CuminDirectEvaluation.java */

