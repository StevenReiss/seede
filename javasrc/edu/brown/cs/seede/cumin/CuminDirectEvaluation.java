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

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.cashew.CashewValue;
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

CuminRunStatus checkStringMethods() throws CuminRunException
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
	       return null;
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
	    return null;
	 default :
	    return null;
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
      else return null;
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
	    else return null;
	    break;
	 case "contentEquals" :
	    if (getDataType(1) == STRING_TYPE) {
	       rslt = CashewValue.booleanValue(thisstr.contentEquals(getString(1)));
	     }
	    else return null;
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
	    else return null;
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
	    if (getNumArgs() == 2) {
	       rslt = CashewValue.stringValue(thisstr.substring(getInt(1)));
	     }
	    else {
	       rslt = CashewValue.stringValue(thisstr.substring(getInt(1),getInt(2)));
	     }
	    break;
	 case "toLowerCase" :
	    if (getNumArgs() == 1) {
	       rslt = CashewValue.stringValue(thisstr.toLowerCase());
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
	       rslt = CashewValue.stringValue(thisstr.toUpperCase());
	     }
	    else {
	       // need to get locale object
	       return null;
	     }
	    break;
	 case "trim" :
	    rslt = CashewValue.stringValue(thisstr.trim());
	    break;

	 case "getBytes" :
            byte [] bbuf = null;
            if (getNumArgs() == 1) {
               bbuf = thisstr.getBytes();
             }
            else if (getNumArgs() == 2 && getDataType(1) == STRING_TYPE) {
               String cset = getString(1);
               try {
                  bbuf = thisstr.getBytes(cset);
                }
               catch (UnsupportedEncodingException e) {
                  CuminEvaluator.throwException(UNSUP_ENC_EXC);
                }
             }
            if (bbuf == null) return null;
            rslt = CashewValue.arrayValue(bbuf);
            break;
            
	 case "intern":
	    return null;

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

         case "split" :
            String [] sarr = null;
            if (getNumArgs() == 2) {
               sarr = thisstr.split(getString(1));
             }
            else {
               sarr = thisstr.split(getString(1),getInt(2));
             }
            rslt = CashewValue.arrayValue(sarr);
            break;
            
	 case "toCharArray" :
	    rslt = CashewValue.arrayValue(thisstr.toCharArray());
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

CuminRunStatus checkMathMethods()
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

CuminRunStatus checkFloatMethods()
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
	    rslt = CashewValue.numericValue(INT_TYPE,f.hashCode());
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



CuminRunStatus checkDoubleMethods()
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
	    rslt = CashewValue.numericValue(INT_TYPE,d.hashCode());
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

CuminRunStatus checkSystemMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "arraycopy" :
	 CuminRunStatus sts = handleArrayCopy(getValue(0),getInt(1),getValue(2),getInt(3),getInt(4));
	 if (sts != null) return sts;
	 break;
      case "currentTimeMillis" :
	 rslt = CashewValue.numericValue(LONG_TYPE,System.currentTimeMillis());
	 break;
      case "exit" :
	 return CuminRunStatus.Factory.createHalt();
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
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



private CuminRunStatus handleArrayCopy(CashewValue src,int spos,CashewValue dst,int dpos,int len)
{
   if (src.isNull(getClock()) || dst.isNull(getClock())) {
      return CuminEvaluator.returnException(NULL_PTR_EXC);
    }

   int sdim = -1;
   int ddim = -1;
   try {
      sdim = src.getDimension(getClock());
      ddim = dst.getDimension(getClock());
    }
   catch (Throwable t) {
      return CuminEvaluator.returnException(ARRAY_STORE_EXC);
    }
   // check array element types

   if (spos < 0 || dpos < 0 || len < 0 || spos+len > sdim || dpos+len > ddim)
      return CuminEvaluator.returnException(IDX_BNDS_EXC);

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

   return null;
}



/********************************************************************************/
/*										*/
/*	Object methods								*/
/*										*/
/********************************************************************************/

CuminRunStatus checkObjectMethods()
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
      case "wait" :
	 return CuminRunStatus.Factory.createWait();

      case "notify" :
      case "notifyAll" :
	 // TODO: handle synchronization
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

CuminRunStatus checkClassMethods()
{
   CashewValue rslt = null;
   JcompType rtype = null;

   if (getMethod().isStatic()) {
      return null;
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
         case "getClassLoader" :
         case "getClassLoader0" :
            exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
            String expr = "edu.brown.cs.seede.poppy.PoppyValue.getClassLoaderUsingPoppy(\"" +
                thistype.getName() + "\")";
            rslt = exec_runner.getLookupContext().evaluate(expr);
            break;
            
	 default :
	    AcornLog.logE("Unknown call to java.lang.Class." + getMethod());
	    return null;
       }
    }
   if (rslt == null && rtype != null) {
      rslt = CashewValue.classValue(rtype);
    }

   return CuminRunStatus.Factory.createReturn(rslt);
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
		  CURRENT_THREAD_FIELD,"java.lang.Thread");
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
	 rslt = CashewValue.numericValue(INT_TYPE,0);
	 break;
      case "getStackTraceElement" :
	 return CuminEvaluator.returnException(IDX_BNDS_EXC);
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

CuminRunStatus checkFloatingDecimalMehtods()
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
	    return CuminEvaluator.returnException(NUM_FMT_EXC);
	  }
	 break;
      case "parseFloat" :
	 try {
	    float f1 = Float.parseFloat(getString(0));
	    rslt = CashewValue.numericValue(FLOAT_TYPE,f1);
	  }
	 catch (NumberFormatException e) {
	   return CuminEvaluator.returnException(NUM_FMT_EXC);
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
	 rslt = CashewValue.booleanValue(true);
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

CuminRunStatus checkReflectArrayMethods() throws CuminRunException
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
	 return null;
    }

   return CuminRunValue.Factory.createReturn(rslt);
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
	 if (action.getDataType(getClock()).isCompatibleWith(PRIV_ACTION_TYPE)) {
	    rtn = "java.security.PrivilegedAction.run";
	  }
	 else {
	    rtn = "java.security.PrivilegedExceptionAction.run";
	  }
	 rslt = exec_runner.executeCall(rtn,action);
	 break;
      default :
	 return null;
    }

   return CuminRunValue.Factory.createReturn(rslt);
}




}	// end of class CuminDirectEvaluation




/* end of CuminDirectEvaluation.java */

