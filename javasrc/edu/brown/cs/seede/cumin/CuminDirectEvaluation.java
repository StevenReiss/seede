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
import java.text.DecimalFormat;
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
      CuminEvaluator.throwException(getSession(),getContext(),
	    getTyper(),e.getClass().getName());
    }
   catch (UnsupportedEncodingException e) {
      CuminEvaluator.throwException(getSession(),getContext(),
	    getTyper(),e.getClass().getName());
    }

   return null;
}



CuminRunStatus checkCharacterMethods() throws CuminRunException, CashewException
{
   JcompTyper typer = getTyper();
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "digit" :
	    int ch = getInt(0);
	    int radix = getInt(1);
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,Character.digit(ch,radix));
	    break;
	 case "codePointOf" :
	    String a0 = getString(0);
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,Character.codePointOf(a0));
	    break;
	 case "getName" :
	    ch = getInt(0);
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,Character.getName(ch));
	    break;
	 case "reverseBytes" :
	    char ch0 = getChar(0);
	    rslt = CashewValue.characterValue(typer.CHAR_TYPE,Character.reverseBytes(ch0));
	    break;
	 case "isMirrored" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isMirrored(ch));
	    break;
	 case "getDirectionality" :
	    ch = getInt(0);
	    rslt = CashewValue.numericValue(typer,typer.BYTE_TYPE,Character.getDirectionality(ch));
	    break;
	 case "forDigit" :
	    ch = getInt(0);
	    radix = getInt(1);
	    rslt = CashewValue.characterValue(typer.CHAR_TYPE,Character.forDigit(ch,radix));
	    break;
	 case "getType" :
	    ch = getInt(0);
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,Character.getType(ch));
	    break;
	 case "isISOControl" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isISOControl(ch));
	    break;
	 case "isWhitespace" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isWhitespace(ch));
	    break;
	 case "isSpaceChar" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isSpaceChar(ch));
	    break;
	 case "getNumericValue" :
	    ch = getInt(0);
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,Character.getNumericValue(ch));
	    break;
	 case "toTitleCase" :
	    ch = getInt(0);
	    rslt = CashewValue.numericValue(typer,getDataType(0),Character.toTitleCase(ch));
	    break;
	 case "toUpperCase" :
	    ch = getInt(0);
	    rslt = CashewValue.numericValue(typer,getDataType(0),Character.toUpperCase(ch));
	    break;
	 case "toLowerCase" :
	    ch = getInt(0);
	    rslt = CashewValue.numericValue(typer,getDataType(0),Character.toLowerCase(ch));
	    break;
	 case "isIdentifierIgnorable" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isIdentifierIgnorable(ch));
	    break;
	 case "isUnicodeIdentifierPart" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isUnicodeIdentifierPart(ch));
	    break;
	 case "isUnicodeIdentifierStart" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isUnicodeIdentifierStart(ch));
	    break;
	 case "isJavaIdentifierPart" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isJavaIdentifierPart(ch));
	    break;
	 case "isJavaIdentifierStart" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isJavaIdentifierStart(ch));
	    break;
	 case "isIdeographic" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isIdeographic(ch));
	    break;
	 case "isAlphabetic" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isAlphabetic(ch));
	    break;
	 case "isLetterOrDigit" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isLetterOrDigit(ch));
	    break;
	 case "isLetter" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isLetter(ch));
	    break;
	 case "isDefined" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isDefined(ch));
	    break;
	 case "isDigit" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isDigit(ch));
	    break;
	 case "isTitleCase" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isTitleCase(ch));
	    break;
	 case "isUpperCase" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isUpperCase(ch));
	    break;
	 case "isLowerCase" :
	    ch = getInt(0);
	    rslt = CashewValue.booleanValue(typer,Character.isLowerCase(ch));
	    break;
	 default :
	    return null;
       }
    }
   else {
      return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}


private CuminRunStatus checkStringMethodsLocal() throws CuminRunException, CashewException, UnsupportedEncodingException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "valueOf" :
	    JcompType dtyp = getDataType(0);
	    if (dtyp.isBooleanType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getBoolean(0)));
	     }
	    else if (dtyp.isCharType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getChar(0)));
	     }
	    else if (dtyp.isDoubleType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getDouble(0)));
	     }
	    else if (dtyp.isFloatType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getFloat(0)));
	     }
	    else if (dtyp.isLongType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getLong(0)));
	     }
	    else if (dtyp.isArrayType() && dtyp.getBaseType().isCharType()) {
	       if (getNumArgs() == 1) {
		  rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getCharArray(0)));
		}
	       else {
		  rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getCharArray(0),
			getInt(1),getInt(2)));
		}
	     }
	    else if (dtyp.isNumericType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.valueOf(getInt(0)));
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
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.copyValueOf(getCharArray(0)));
	     }
	    else {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,String.copyValueOf(getCharArray(0),
		     getInt(1),getInt(2)));
	     }
	    break;
	 case "format" :
	    return null;
	 default :
	    return null;
       }
    }
   else if (getMethod().isConstructor()) {
      CashewValueString cvs = (CashewValueString) getContext().findReference(0).getActualValue(sess,getClock());
      if (getNumArgs() == 1) ;							// new String()
      else if (getNumArgs() == 2 && getDataType(1).isStringType()) {		// new String(String)
	 cvs.setInitialValue(typer,getString(1),-1);
       }
      else if (getNumArgs() == 2 && getDataType(1).isArrayType() &&
	    getDataType(1).getBaseType().isCharType()) {	// new String(char[])
	 String temp = new String(getCharArray(1));
	 cvs.setInitialValue(typer,temp,-1);
       }
      else if (getNumArgs() == 4 && getDataType(1).isArrayType() &&
	    getDataType(1).getBaseType().isCharType()) {			// new String(char[],int,int)
	 String temp = new String(getCharArray(1),getInt(2),getInt(3));
	 cvs.setInitialValue(typer,temp,-1);
       }
      else if (getNumArgs() == 2 && getDataType(1).isArrayType() &&
	    getDataType(1).getBaseType().isByteType()) {	// new String(byte[])
	 String temp = new String(getByteArray(1));
	 cvs.setInitialValue(typer,temp,-1);
       }
      else if (getNumArgs() >= 2 &&
	    (getDataType(1).getName().contains("StringBuilder") ||
		  getDataType(1).getName().contains("StringBuffer"))) {
	 CashewValue cv = getValue(1);
	 CashewValue cvbytes = cv.getFieldValue(sess,getTyper(),getClock(),
	       "java.lang.AbstractStringBuilder.value",getContext());
	 cvbytes = cvbytes.getActualValue(sess,getClock());
	 int coder = getIntFieldValue(cv,"java.lang.AbstractStringBuilder.coder");
	 int cvlen = getIntFieldValue(cv,"java.lang.AbstractStringBuilder.count");
	 if (coder == 1) cvlen *= 2;
	 byte [] rl = new byte[cvlen];
	 for (int i = 0; i < cvlen; ++i) {
	    rl[i] = cvbytes.getIndexValue(sess,getClock(),i).getNumber(sess,getClock()).byteValue();
	  }
	 String temp = new String(rl);
	
//	 System.err.println("CREATION " + temp + " " + temp.length());
//	 byte [] b1 = temp.getBytes();
//	 for (int i = 0; i < b1.length; ++i) {
//	    System.err.println("CREATE " + i + " " + b1[i]);
//	  }
//	 for (int i = 0; i < rl.length; ++i) {
//	    System.err.println("CREATE " + i + " " + rl[i]);
//	  }
	
	 cvs.setInitialValue(typer,temp,-1);
       }
      else if (getNumArgs() == 3 && getDataType(1).isArrayType() &&
	    getDataType(1).getBaseType().isByteType()) {	// new String(byte[],charset)
	 String temp = null;
	 String enc = null;
	 int coder = -1;
	 if (getDataType(2).isByteType() || getDataType(2).isIntType()) {
	    coder = getInt(2);													  
	  }
	 else if (getDataType(2).isStringType()) {
	    enc = getString(2);
	  }
	 if (enc != null) temp = new String(getByteArray(1),enc);
	 else temp = new String(getByteArray(1));
	 cvs.setInitialValue(typer,temp,coder);
       }
      else if (getNumArgs() == 4 && getDataType(1).isArrayType() &&
	    getDataType(1).getBaseType().isByteType()) {	// new String(byte[],int,int)
	 String temp = new String(getCharArray(1),getInt(2),getInt(3));
	 cvs.setInitialValue(typer,temp,-1);
       }
      else if (getNumArgs() == 4 && getDataType(1).isArrayType() &&
	    getDataType(1).getBaseType().isIntType()) {	// new String(int[],int,int)
	 String temp = new String(getIntArray(1),getInt(2),getInt(3));
	 cvs.setInitialValue(typer,temp,-1);
       }
      else if (getNumArgs() == 5 && getDataType(1).isArrayType() &&
	    getDataType(1).getBaseType().isByteType()) {
	 String temp = null;
	 if (getDataType(4).isStringType()) {						// new String(byte[],int,int,String)
	    temp = new String(getByteArray(1),getInt(2),getInt(3),getString(4));
	  }
	 else {
	    temp = new String(getCharArray(1),getInt(2),getInt(3));			// new String(byte[],int,int,charset)
	  }
	 cvs.setInitialValue(typer,temp,-1);
       }
      else {
	 AcornLog.logE("CUMIN","Missing String constructor for " + getMethod());
	 return null;
       }
    }
   else {
      CashewValue thisarg = getValue(0);
      String thisstr = thisarg.getString(getSession(),typer,getClock());
      switch (getMethod().getName()) {
	 case "charAt" :
	    int cpos = getInt(1);
	    rslt = CashewValue.characterValue(typer.CHAR_TYPE,thisstr.charAt(cpos));
	    break;
	 case "codePointAt" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.codePointAt(getInt(1)));
	    break;
	 case "codePointBefore" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.codePointBefore(getInt(1)));
	    break;
	 case "codePointCount" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.codePointCount(getInt(1),getInt(2)));
	    break;
	 case "compareTo" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.compareTo(getString(1)));
	    break;
	 case "compareToIgnoreCase" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.compareToIgnoreCase(getString(1)));
	    break;
	 case "concat" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.concat(getString(1)));
	    break;
	 case "contains" :
	    if (getDataType(1).isStringType()) {
	       rslt = CashewValue.booleanValue(typer,thisstr.contains(getString(1)));
	     }
	    else return null;
	    break;
	 case "contentEquals" :
	    if (getDataType(1).isStringType()) {
	       rslt = CashewValue.booleanValue(typer,thisstr.contentEquals(getString(1)));
	     }
	    else return null;
	    break;
	 case "endsWith" :
	    rslt = CashewValue.booleanValue(typer,thisstr.endsWith(getString(1)));
	    break;
	 case "equals" :
	    if (!getDataType(1).isStringType()) {
	       rslt = CashewValue.booleanValue(typer,false);
	     }
	    else {
	       String s1 = getString(1);
//	       System.err.println("CHECK " + s1 + " " + s1.length() + " " + s1.getBytes().length + " " +
//		     thisstr + " " + thisstr.length() + " " + thisstr.getBytes().length);
//	       byte [] b = s1.getBytes();
//	       for (int i = 0; i < b.length; ++i) {
//		  System.err.println("CHECKING " + i + " " + b[i]);
//		}
//	       b = thisstr.getBytes();
//	       for (int i = 0; i < b.length; ++i) {
//		  System.err.println("CHECKING " + i + " " + b[i]);
//		}
	       rslt = CashewValue.booleanValue(typer,thisstr.equals(s1));
	     }
	    break;
	 case "equalsIgnoreCase" :
	    rslt = CashewValue.booleanValue(typer,thisstr.equalsIgnoreCase(getString(1)));
	    break;
	 case "hashCode" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.hashCode());
	    break;
	 case "indexOf" :
	    if (getDataType(1).isIntType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.indexOf(getInt(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.indexOf(getInt(1),getInt(2)));
		}
	     }
	    else if (getDataType(1).isStringType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.indexOf(getString(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.indexOf(getString(1),getInt(2)));
		}
	     }
	    break;
	 case "intern" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.intern());
	    break;
	 case "isEmpty" :
	    rslt = CashewValue.booleanValue(typer,thisstr.isEmpty());
	    break;
	 case "lastIndexOf" :
	    if (getDataType(1).isIntType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.lastIndexOf(getInt(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.lastIndexOf(getInt(1),getInt(2)));
		}
	     }
	    else if (getDataType(1).isStringType()) {
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.lastIndexOf(getString(1)));
		}
	       else {
		  rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.lastIndexOf(getString(1),getInt(2)));
		}
	     }
	    break;
	 case "length" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisstr.length());
	    break;
	 case "matches" :
	    rslt = CashewValue.booleanValue(typer,thisstr.matches(getString(1)));
	    break;
	 case "offsetByCodePoints" :
	    rslt = CashewValue.numericValue(typer.INT_TYPE,thisstr.offsetByCodePoints(getInt(1),getInt(2)));
	    break;
	 case "regionMatches" :
	    if (getNumArgs() == 6) {
	       rslt = CashewValue.booleanValue(typer,
		     thisstr.regionMatches(getBoolean(1),getInt(2),
		     getString(3),getInt(4),getInt(5)));
	     }
	    else {
	       rslt = CashewValue.booleanValue(typer,
		     thisstr.regionMatches(getInt(1),getString(2),getInt(3),getInt(4)));
	     }
	    break;
	 case "replace" :
	    if (getDataType(0).isCharType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.replace(getChar(1),getChar(2)));
	     }
	    else if (getDataType(1).isStringType() && getDataType(2).isStringType()) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.replace(getString(1),getString(2)));
	     }
	    else return null;
	    break;
	 case "replaceAll" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.replaceAll(getString(1),getString(2)));
	    break;
	 case "replaceFirst" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.replaceFirst(getString(1),getString(2)));
	    break;
	 case "startsWith" :
	    if (getNumArgs() == 2) {
	       rslt = CashewValue.booleanValue(typer,thisstr.startsWith(getString(1)));
	     }
	    else {
	       rslt = CashewValue.booleanValue(typer,thisstr.startsWith(getString(1),getInt(2)));
	     }
	    break;
	 case "subSequence" :
	 case "substring" :
	    try {
	       int a0 = getInt(1);
	       if (getNumArgs() == 2) {
		  rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.substring(a0));
		}
	       else {
		  rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.substring(a0,getInt(2)));
		}
	     }
	    catch (IndexOutOfBoundsException e) {
	       CuminEvaluator.throwException(getSession(),getContext(),
		     typer,"java.lang.StringIndexOutOfBoundsException");
	     }
	    break;
	 case "toLowerCase" :
	    if (getNumArgs() == 1) {
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.toLowerCase());
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
	       rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.toUpperCase());
	     }
	    else {
	       // need to get locale object
	       return null;
	     }
	    break;
	 case "trim" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisstr.trim());
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
		  CuminEvaluator.throwException(getSession(),getContext(),
			typer,"java.io.UnsupportedEncodingException");
		}
	     }
	    if (bbuf == null) return null;
	    rslt = CashewValue.arrayValue(typer,bbuf);
	    break;

	 case "getChars" :
	    int srcbegin = getInt(1);
	    int srcend = getInt(2);
	    CashewValue carr = getArrayValue(3);
	    int dstbegin = getInt(4);
	    int dimsize = carr.getDimension(sess,getClock());
	    getClock().freezeTime();
	    if (srcbegin < 0 || srcend < 0 || srcbegin >= thisstr.length() ||
		  srcend > thisstr.length() || srcbegin > srcend ||
		  dstbegin < 0 || dstbegin >= dimsize ||
		  dstbegin + (srcend-srcbegin) > dimsize)
	       throw new StringIndexOutOfBoundsException();
	    try {
	       for (int i = srcbegin; i < srcend; ++i) {
		  CashewValue charv = CashewValue.characterValue(typer.CHAR_TYPE,thisstr.charAt(i));
		  carr.setIndexValue(sess,getClock(),dstbegin+i-srcbegin,charv);
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
	    rslt = CashewValue.arrayValue(typer,sarr);
	    break;

	 case "toCharArray" :
	    rslt = CashewValue.arrayValue(typer,thisstr.toCharArray());
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
   JcompTyper typer = getTyper();

   switch (getMethod().getName()) {
      case "abs" :
	 if (getDataType(0).isDoubleType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.abs(getDouble(0)));
	  }
	 else if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.abs(getFloat(0)));
	  }
	 else if (getDataType(0).isLongType()) {
	    rslt = CashewValue.numericValue(typer,getDataType(0),StrictMath.abs(getLong(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,StrictMath.abs(getInt(0)));
	  }
	 break;
      case "acos" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.acos(getDouble(0)));
	 break;
      case "asin" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.asin(getDouble(0)));
	 break;
      case "atan" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.atan(getDouble(0)));
	 break;
      case "atan2" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.atan2(getDouble(0),getDouble(2)));
	 break;
      case "cbrt" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.cbrt(getDouble(0)));
	 break;
      case "ceil" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.ceil(getDouble(0)));
	 break;
      case "copySign" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.copySign(getFloat(0),getFloat(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.copySign(getDouble(0),getDouble(2)));
	  }
	 break;
      case "cos" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.cos(getDouble(0)));
	 break;
      case "cosh" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.cosh(getDouble(0)));
	 break;
      case "exp" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.exp(getDouble(0)));
	 break;
      case "expm1" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.expm1(getDouble(0)));
	 break;
      case "floor" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.floor(getDouble(0)));
	 break;
      case "getExponent" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,StrictMath.getExponent(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,StrictMath.getExponent(getDouble(0)));
	  }
	 break;
      case "hypot" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.hypot(getDouble(0),getDouble(2)));
	 break;
      case "IEEEremainder" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.IEEEremainder(getDouble(0),getDouble(2)));
	 break;
      case "log" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.log(getDouble(0)));
	 break;
      case "log10" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.log10(getDouble(0)));
	 break;
      case "log1p" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.log1p(getDouble(0)));
	 break;
      case "max" :
	 if (getDataType(0).isDoubleType() || getDataType(1).isDoubleType()) {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.max(getDouble(0),getDouble(2)));
	  }
	 else if (getDataType(0).isFloatType() || getDataType(1).isFloatType()) {
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,StrictMath.max(getFloat(0),getFloat(1)));
	  }
	 else if (getDataType(0).isLongType() || getDataType(1).isLongType()) {
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,
		  StrictMath.max(getLong(0),getLong(2)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,StrictMath.max(getInt(0),getInt(1)));
	  }
	 break;
      case "min" :
	 if (getDataType(0).isDoubleType() || getDataType(1).isDoubleType()) {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.min(getDouble(0),getDouble(2)));
	  }
	 else if (getDataType(0).isFloatType() || getDataType(1).isFloatType()) {
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,StrictMath.min(getFloat(0),getFloat(1)));
	  }
	 else if (getDataType(0).isLongType() || getDataType(1).isLongType()) {
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,StrictMath.min(getLong(0),getLong(2)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,
		  StrictMath.min(getInt(0),getInt(1)));
	  }
	 break;
      case "nextAfter" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.nextAfter(getFloat(0),getDouble(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.nextAfter(getDouble(0),getDouble(2)));
	  }
	 break;
      case "nextUp" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(getDataType(0),StrictMath.nextUp(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.nextUp(getDouble(0)));
	  }
	 break;
      case "pow" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.pow(getDouble(0),getDouble(2)));
	 break;
      case "random" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.random());
	 break;
      case "rint" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.rint(getDouble(0)));
	 break;
      case "round" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,StrictMath.round(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,StrictMath.round(getDouble(0)));
	  }
	 break;
      case "scalb" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,StrictMath.scalb(getFloat(0),getInt(1)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.scalb(getDouble(0),getInt(2)));
	  }
	 break;
      case "signum" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,StrictMath.signum(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.signum(getDouble(0)));
	  }
	 break;
      case "sin" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.sin(getDouble(0)));
	 break;
      case "sinh" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.sinh(getDouble(0)));
	 break;
      case "sqrt" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.sqrt(getDouble(0)));
	 break;
      case "tan" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.tan(getDouble(0)));
	 break;
      case "tanh" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.tanh(getDouble(0)));
	 break;
      case "toDegrees" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.toDegrees(getDouble(0)));
	 break;
      case "toRadians" :
	 rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.toRadians(getDouble(0)));
	 break;
      case "ulp" :
	 if (getDataType(0).isFloatType()) {
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,StrictMath.ulp(getFloat(0)));
	  }
	 else {
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,StrictMath.ulp(getDouble(0)));
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
   JcompTyper typer = getTyper();

   switch (getMethod().getName()) {
      case "availableProcessors" :
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,rt.availableProcessors());
	 break;
      case "freeMemory" :
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,rt.freeMemory());
	 break;
      case "gc" :
      case "traceInstructions" :
      case "traceMethodCalls" :
	 break;
      case "maxMemory" :
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,rt.maxMemory());
	 break;
      case "totalMemory" :
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,rt.totalMemory());
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
   JcompTyper typer = getTyper();

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "floatToIntBits" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,Float.floatToIntBits(getFloat(0)));
	    break;
	 case "floatToRawIntBits" :
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,Float.floatToRawIntBits(getFloat(0)));
	    break;
	 case "intBitsToFloat" :
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,Float.intBitsToFloat(getInt(0)));
	    break;
	 case "isInfinite" :
	    rslt = CashewValue.booleanValue(typer,Float.isInfinite(getFloat(0)));
	    break;
	 case "isNaN" :
	    rslt = CashewValue.booleanValue(typer,Float.isNaN(getFloat(0)));
	    break;
	 case "toHexString" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,Float.toHexString(getFloat(0)));
	    break;
	 case "toString" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,Float.toString(getFloat(0)));
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
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,f.hashCode());
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
   JcompTyper typer = getTyper();

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "doubleToLongBits" :
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,Double.doubleToLongBits(getDouble(0)));
	    break;
	 case "doubleToRawLongBits" :
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,Double.doubleToRawLongBits(getDouble(0)));
	    break;
	 case "longBitsToDouble" :
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,Double.longBitsToDouble(getLong(0)));
	    break;
	 case "isInfinite" :
	    rslt = CashewValue.booleanValue(typer,Double.isInfinite(getDouble(0)));
	    break;
	 case "isNaN" :
	    rslt = CashewValue.booleanValue(typer,Double.isNaN(getDouble(0)));
	    break;
	 case "toHexString" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,Double.toHexString(getDouble(0)));
	    break;
	 case "toString" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,Double.toString(getDouble(0)));
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
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,d.hashCode());
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}


CuminRunStatus checkNumberFormatMethods() throws CashewException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();

   switch (getMethod().getName()) {
      case "getInstance" :
      case "getNumberInstance" :
	 rslt = getNumberFormatInstance("Number");
	 break;
      case "getCurrencyInstance" :
	 rslt = getNumberFormatInstance("Currency");
	 break;
      case "getIntegerInstance" :
	 rslt = getNumberFormatInstance("Integer");
	 break;
      case "getPercentInstance" :
	 rslt = getNumberFormatInstance("Percent");
	 break;
      case "format" :
	 CashewValue thisarg = getValue(0);
	 JcompType thistyp = getDataType(0);
	 if (!thistyp.getName().contains("DecimalFormat")) return null;
	 if (getNumArgs() == 2) {
	    CashewValue pat = exec_runner.executeCall("java.text.DecimalFormat.toPattern",thisarg);
	    String patstr = pat.getString(getSession(),typer,getClock());
	    DecimalFormat df = new DecimalFormat(patstr);
	    JcompType atyp = getDataType(1);
	    String srslt = null;
	    if (atyp.isDoubleType()) {
	       double d = getDouble(1);
	       srslt = df.format(d);
	     }
	    else if (atyp.isLongType()) {
	       long l = getLong(1);
	       srslt = df.format(l);
	     }
	    AcornLog.logD("CUMIN","Decimal format " + patstr + " " + srslt);
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,srslt);
	  }
	 else {
	    AcornLog.logD("CUMIN","Bad decimal format call: " + getNumArgs());
	    return null;
	  }
	 break;
      default :
	 return null;
    }
	
   return CuminRunStatus.Factory.createReturn(rslt);
}


private CashewValue getNumberFormatInstance(String typ)
{
   String expr;
   String lang = "null";

   if (getNumArgs() == 1) {
      try {
	 CashewValue loc = getValue(0);
	 CashewValue bloc = loc.getFieldValue(getSession(),getTyper(),
	       getClock(),"java.util.Locale.baseLocale",getContext());
	 CashewValue lval = bloc.getFieldValue(getSession(),getTyper(),
	       getClock(),"sun.util.locale.BaseLocale.language",getContext());
	 String s = lval.getString(getSession(),getTyper(),getClock());
	 lang = "\"" + s + "\"";
       }
      catch (CashewException e) { }
    }

   exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
   expr = "edu.brown.cs.seede.poppy.PoppyValue.getNumberFormatInstance(\"" + typ + "\"," + lang + ")";
   CashewValue rslt = exec_runner.getLookupContext().evaluate(expr);
   return rslt;
}

/********************************************************************************/
/*										*/
/*	Handle integer methods							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkIntegerMethods() throws CashewException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "toString" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,Integer.toString(getInt(0)));
	    break;
	 case "valueOf" :
	    int v = getInt(0);
	    exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
	    String expr = "edu.brown.cs.seede.poppy.PoppyValue.getInteger(" + v + ")";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 default :
	    return null;
       }
    }
   else {
      return null;
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
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "arraycopy" :
	 CuminRunStatus sts = handleArrayCopy(getValue(0),getInt(1),getValue(2),getInt(3),getInt(4));
	 if (sts != null) return sts;
	 break;
      case "currentTimeMillis" :
	 rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,System.currentTimeMillis());
	 break;
      case "exit" :
	 return CuminRunStatus.Factory.createHalt();
      case "gc" :
	 break;
      case "identityHashCode" :
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,
	       getValue(0).getFieldValue(sess,typer,getClock(),HASH_CODE_FIELD,getContext()).
	       getNumber(sess,getClock()).intValue());
	 break;
      case "load" :
      case "loadLibrary" :
	 break;
      case "mapLibraryName" :
	 rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,System.mapLibraryName(getString(0)));
	 break;
      case "nanoTime" :
	 rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,System.nanoTime());
	 break;
      case "runFinalization" :
	 break;
      case "setErr" :
      case "setIn" :
      case "setOut" :
	 break;
      case "allowSecurityManager" :
	 rslt = CashewValue.booleanValue(typer,false);
	 break;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



private CuminRunStatus handleArrayCopy(CashewValue src,int spos,CashewValue dst,int dpos,int len)
	throws CashewException
{
   CashewValueSession sess = getSession();
   JcompTyper typer = getTyper();

   if (src.isNull(sess,getClock()) || dst.isNull(sess,getClock())) {
      return CuminEvaluator.returnException(sess,getContext(),
	    typer,"java.lang.NullPointerException");
    }

   int sdim = -1;
   int ddim = -1;
   try {
      sdim = src.getDimension(sess,getClock());
      ddim = dst.getDimension(sess,getClock());
    }
   catch (Throwable t) {
      return CuminEvaluator.returnException(sess,getContext(),
	    typer,"java.lang.ArrayStoreException");
    }
   // check array element types

   if (spos < 0 || dpos < 0 || len < 0 || spos+len > sdim || dpos+len > ddim) {
      return CuminEvaluator.returnException(sess,getContext(),
	    typer,"java.lang.ArrayIndexOutOfBoundsException");
    }

   getClock().freezeTime();
   try {
      if (src == dst && spos < dpos) {
	 for (int i = len-1; i >= 0; --i) {
	    CashewValue cv = src.getIndexValue(sess,getClock(),spos+i);
	    dst.setIndexValue(sess,getClock(),dpos+i,cv.getActualValue(sess,getClock()));
	  }
       }
      else {
	 for (int i = 0; i < len; ++i) {
	    CashewValue cv = src.getIndexValue(sess,getClock(),spos+i);
	    // should type check the assignment here
	    dst.setIndexValue(sess,getClock(),dpos+i,cv.getActualValue(sess,getClock()));
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
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "<init>" :
	 break;
      case "getClass" :
	rslt = CashewValue.classValue(typer,getDataType(0));
	break;
      case "equals" :
	 rslt = CashewValue.booleanValue(typer,getValue(0) == getValue(1));
	 break;
      case "hashCode" :
	 if (getValue(0).isNull(sess,getClock()))
	    return CuminEvaluator.returnException(sess,getContext(),
		  typer,"java.lang.NullPointerException");
	 CashewValue cv0 = getValue(0);
	 CashewValue cv1 = cv0.getFieldValue(sess,typer,getClock(),HASH_CODE_FIELD,getContext());
	 AcornLog.logD("CUMIN","Hash compute " + cv1 + " " + cv0);
	 Number cv2 = cv1.getNumber(sess,getClock());
	 AcornLog.logD("CUMIN","Hash value " + cv2);
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,
	       cv2.intValue());
	 break;
      case "clone" :
	 CashewValue v0 = getValue(0);
	 if (v0.isNull(sess,getClock()))
	    return CuminEvaluator.returnException(sess,getContext(),
		  typer,"java.lang.NullPointerException");
	 if (v0 instanceof CashewValueArray) {
	    CashewValueArray arr = (CashewValueArray) v0;
	    rslt = arr.cloneObject(sess,typer,getClock(),0);
	  }
	 else {
	    CashewValueObject obj = (CashewValueObject) v0;
	    rslt = obj.cloneObject(sess,typer,getClock(),0);
	  }
	 break;
      case "wait" :
	 CashewSynchronizationModel scm = getContext().getSynchronizationModel();
	 if (scm != null) {
	    long time = 0;
	    if (getNumArgs() > 1) time = getLong(1);
	    try {
	       scm.synchWait(exec_runner.getCurrentThread(),getValue(0),time);
	     }
	    catch (InterruptedException ex) {
	       CuminEvaluator.throwException(sess,getContext(),
		     typer,"java.lang.InterruptedException");
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
	 rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,getString(0));
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
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();
   int sz = 0;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "forName" :
	    String cls = getString(0);
	    JcompType typ1 = typer.findType(cls);
	    if (typ1 == null) typ1 = typer.findSystemType(cls);
	    if (typ1 == null) return null;
	    rslt = CashewValue.classValue(typer,typ1);
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
	    else rslt = CashewValue.nullValue(typer);
	    break;
	 case "getName" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thistype.getName());
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
	    JcompType ctyp = typer.createMethodType(null,null,false,null);
	    csym = thistype.lookupMethod(typer,"<init>",ctyp);
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
	    rslt = CashewValue.booleanValue(typer,thistype.isPrimitiveType());
	    break;
	 case "isArray" :
	    rslt = CashewValue.booleanValue(typer,thistype.isArrayType());
	    break;
	 case "isInterface" :
	    rslt = CashewValue.booleanValue(typer,thistype.isInterfaceType());
	    break;
         case "isRecord" :
            rslt = CashewValue.booleanValue(typer,thistype.isRecord());
            break;
	 case "getModifiers" :
	    JcompSymbol sym = thistype.getDefinition();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,sym.getModifiers());
	    break;
	 case "getSuperclass" :
	    rtype = thistype.getSuperType();
	    if (rtype == null) rslt = CashewValue.nullValue(typer);
	    break;
	 case "getConstructor" :
	    expr = "edu.brown.cs.seede.poppy.PoppyValue.getConstructorUsingPoppy(\"" +
		thistype.getName() + "\"";
	    CashewValue av = getValue(1);
	    if (av.isNull(sess,getClock())) sz = 0;
	    else {
	       sz = av.getFieldValue(sess,typer,getClock(),"length",getContext()).
		  getNumber(sess,getClock()).intValue();
	     }
	    for (int i = 0; i < sz; ++i) {
	       CashewValue tv = av.getIndexValue(sess,getClock(),i);
	       tv = tv.getActualValue(sess,getClock());
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
	    if (av.isNull(sess,getClock())) sz = 0;
	    else {
	       sz = av.getFieldValue(sess,typer,getClock(),"length",getContext()).
		  getNumber(sess,getClock()).intValue();
	     }
	    for (int i = 0; i < sz; ++i) {
	       CashewValue tv = av.getIndexValue(sess,getClock(),i);
	       tv = tv.getActualValue(sess,getClock());
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
	    if (av.isNull(sess,getClock())) sz = 0;
	    else {
	       sz = av.getFieldValue(sess,typer,getClock(),"length",getContext()).
		  getNumber(sess,getClock()).intValue();
	     }
	    for (int i = 0; i < sz; ++i) {
	       CashewValue tv = av.getIndexValue(sess,getClock(),i);
	       tv = tv.getActualValue(sess,getClock());
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
	    fg = v0.getDataType(sess,getClock(),typer).isCompatibleWith(thistype);
	    rslt = CashewValue.booleanValue(typer,fg);
	    break;
	 case "getSimpleName" :
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,getSimpleClassName(thistype));
	    break;
	 case "isLocalClass" :
	    rslt = CashewValue.booleanValue(typer,thistype.getOuterType() != null);
	    break;
	 case "isMemberClass" :
	    rslt = CashewValue.booleanValue(typer,thistype.isFunctionRef());
	    break;
	 case "isAnnotation" :
	    rslt = CashewValue.booleanValue(typer,thistype.isAnnotationType());
	    break;
	 case "isAnonymousClass" :
	    rslt = CashewValue.booleanValue(typer,thistype.getName().contains("$00"));
	    break;
	 case "getModule" :
	    rslt = thisarg.getFieldValue(sess,typer,getClock(),"module",getContext());
	    return null;

	 default :
	    AcornLog.logE("Unknown call to java.lang.Class." + getMethod());
	    return null;
       }
    }
   if (rslt == null && rtype != null) {
      rslt = CashewValue.classValue(typer,rtype);
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




private static String getSimpleClassName(JcompType jt)
{
   String s = jt.getName();
   if (s == null) return null;
   s = s.replace("$",".");
   int idx = s.lastIndexOf(".");
   if (idx > 0) s = s.substring(idx+1);
   if (s.length() == 0) return null;
   char c = s.charAt(0);
   if (Character.isDigit(c)) return null;
   return s;
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
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "newInstance" :
	 try {
	    CashewValue cnst = getValue(0);
	    CashewValue clzz = cnst.getFieldValue(sess,typer,getClock(),
		  "java.lang.reflect.Constructor.clazz",getContext());
	    clzz = clzz.getActualValue(sess,getClock());
	    CashewValue prms = cnst.getFieldValue(sess,typer,getClock(),
		  "java.lang.reflect.Constructor.parameterTypes",getContext());
	    prms = prms.getActualValue(sess,getClock());
	    CashewValueClass cvc = (CashewValueClass) clzz;
	    JcompType newtyp = cvc.getJcompType();
	    List<JcompType> argtyp = new ArrayList<>();
	    CashewValue szv = prms.getFieldValue(sess,typer,getClock(),
		  "length",getContext());
	    int sz = szv.getNumber(sess,getClock()).intValue();
	    for (int i = 0; i < sz; ++i) {
	       CashewValue v0 = prms.getIndexValue(sess,getClock(),i);
	       CashewValueClass avc = (CashewValueClass) v0.getActualValue(sess,getClock());
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
	       CashewValue pszv = pval.getFieldValue(sess,typer,getClock(),
		     "length",getContext());
	       int psz = pszv.getNumber(sess,getClock()).intValue();
	       for (int i = 0; i < sz; ++i) {
		  JcompType t0 = argtyp.get(i);
		  CashewValue v0 = pval.getIndexValue(sess,getClock(),i);
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
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "invoke" :
	 try {
	    CashewValue mthd = getValue(0);
	    CashewValue clzz = mthd.getFieldValue(sess,typer,getClock(),
		  "java.lang.reflect.Method.clazz",getContext());
	    clzz = clzz.getActualValue(sess,getClock());
	    CashewValue prms = mthd.getFieldValue(sess,typer,getClock(),
		  "java.lang.reflect.Method.parameterTypes",getContext());
	    prms = prms.getActualValue(sess,getClock());
	    CashewValue rettvl = mthd.getFieldValue(sess,typer,getClock(),
		  "java.lang.reflect.Method.returnType",getContext());
	    rettvl = rettvl.getActualValue(sess,getClock());
	    CashewValue namvl = mthd.getFieldValue(sess,typer,getClock(),
		  "java.lang.reflect.Method.name",getContext());
	    String name = namvl.getString(sess,typer,getClock());
	    CashewValueClass cvc = (CashewValueClass) clzz;
	    JcompType newtyp = cvc.getJcompType();
	    CashewValueClass rvc = (CashewValueClass) rettvl;
	    JcompType rettyp = rvc.getJcompType();
	    List<JcompType> argtyp = new ArrayList<>();
	    int sz = 0;
	    if (!prms.isNull(sess,getClock())) {
	       CashewValue szv = prms.getFieldValue(sess,typer,getClock(),
		     "length",getContext());
	       sz = szv.getNumber(sess,getClock()).intValue();
	     }
	    for (int i = 0; i < sz; ++i) {
	       CashewValue v0 = prms.getIndexValue(sess,getClock(),i);
	       CashewValueClass avc = (CashewValueClass) v0.getActualValue(sess,getClock());
	       argtyp.add(avc.getJcompType());
	     }
	    JcompType mtyp = getTyper().createMethodType(rettyp,argtyp,false,null);
	    JcompSymbol jsym = newtyp.lookupMethod(typer,name,mtyp);
	    if (jsym != null) {
	       CashewValue pval = getValue(2);
	       List<CashewValue> argv = new ArrayList<>();
	       if (!jsym.isStatic()) {
		  argv.add(getValue(1));
		}
	       int psz = 0;
	       if (!pval.isNull(sess,getClock())) {
		  CashewValue pszv = pval.getFieldValue(sess,typer,getClock(),
			"length",getContext());
		  psz = pszv.getNumber(sess,getClock()).intValue();
		}
	       for (int i = 0; i < sz; ++i) {
		  JcompType t0 = argtyp.get(i);
		  CashewValue v0 = pval.getIndexValue(sess,getClock(),i);
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
/*	Handle Module methods							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkModuleMethods() throws CashewException, CuminRunException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValue thisarg = getValue(0);

   switch (getMethod().getName()) {
      case "addExports" :
      case "addOpens" :
      case "addReads" :
      case "addUses" :
	 rslt = thisarg;
	 break;
      default :
      case "isNamed" :
      case "getName" :
      case "toString" :
      case "getDescriptor" :
      case "getLayer" :
      case "getPackages" :
      case "getClassLoader" :
      case "getAnnotations" :
      case "getAnnotation" :
      case "getDeclaredAnnotations" :
      case "getResourceAsStream" :
	 return null;
      case "canRead" :
      case "canUse" :
      case "isOpen" :
      case "isExported" :
	 rslt = CashewValue.booleanValue(typer,true);
	 break;
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
	    rslt = exec_runner.getCurrentThread();
	    break;
	 case "holdsLock" :
	    CashewSynchronizationModel csm = exec_runner.getLookupContext().getSynchronizationModel();
	    // assume we hold all needed locks
	    boolean fg = csm.holdsLock(exec_runner.getCurrentThread(),getValue(0));
	    rslt = CashewValue.booleanValue(getTyper(),fg);
	    break;
	 case "yield" :
	 case "sleep" :
	    break;
	 case "dumpThreads" :
	    // need to implement
	    break;
	 case "getThreads" :
	    // need to implement
	    break;
	 default :
	    return null;
       }
    }
   else {
      // CashewValue thisarg = getContext().findReference(0).getActualValue(getClock());
      switch (getMethod().getName()) {
	 case "start0" :
	    // start a thread here
	    break;
	 case "getStackTrace" :
	 case "getAllStackTraces" :
	    // These should be handled by calling code
	    break;
	 case "isInterrupted" :
	    rslt = CashewValue.booleanValue(getTyper(),false);
	    break;
	 case "isAlive" :
	    CashewValue curthd = exec_runner.getCurrentThread();
	    boolean iscur = curthd.equals(getValue(0));
	    AcornLog.logD("CUMIN","Thread is alive " + curthd + " " + getValue(0) + " " + iscur);
	    rslt = CashewValue.booleanValue(getTyper(),iscur);
	    break;
	 case "countStackFrames" :
	    rslt = CashewValue.numericValue(getTyper(),getTyper().INT_TYPE,1);
	    break;
	 case "setPriority0" :
	 case "stop0" :
	 case "suspend0" :
	 case "resume0" :
	 case "interrupt0" :
	 case "clearInterruptEvent" :
	 case "setNativeName" :
	    break;

	 default :
	    return null;
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
   CashewValueSession sess = getSession();
   JcompTyper typer = getTyper();

   switch (getMethod().getName()) {
      case "fillInStackTrace" :
	 rslt = getValue(0);
	 break;
      case "getStackTraceDepth" :
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,0);
	 break;
      case "getStackTraceElement" :
	 return CuminEvaluator.returnException(sess,getContext(),
	       typer,"java.lang.IndexOutOfBoundsException");
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
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "toJavaFormatString" :
	 if (getDataType(0).isFloatType()) {
	    s1 = String.valueOf(getFloat(0));
	  }
	 else s1 = String.valueOf(getDouble(0));
	 rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,s1);
	 break;
      case "parseDouble" :
	 try {
	    double d1 = Double.parseDouble(getString(0));
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,d1);
	  }
	 catch (NumberFormatException e) {
	    return CuminEvaluator.returnException(sess,getContext(),
		  typer,"java.lang.NumberFormatException");
	  }
	 break;
      case "parseFloat" :
	 try {
	    float f1 = Float.parseFloat(getString(0));
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,f1);
	  }
	 catch (NumberFormatException e) {
	    return CuminEvaluator.returnException(sess,getContext(),
		  typer,"java.lang.NumberFormatException");
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
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "get" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 break;
      case "getBoolean" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.BOOLEAN_TYPE);
	 break;
      case "getByte" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.BYTE_TYPE);
	 break;
      case "getChar" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.CHAR_TYPE);
	 break;
      case "getDouble" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.DOUBLE_TYPE);
	 break;
      case "getFloat" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.FLOAT_TYPE);
	 break;
      case "getInt" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.INT_TYPE);
	 break;
      case "getLong" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.LONG_TYPE);
	 break;
      case "getShort" :
	 rslt = getArrayValue(0).getIndexValue(sess,getClock(),getInt(1));
	 rslt = CuminEvaluator.castValue(exec_runner,rslt,typer.SHORT_TYPE);
	 break;
      case "set" :
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),getValue(2));
	 break;
      case "setBoolean" :
	 CashewValue cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.BOOLEAN_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "setByte" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.BYTE_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "setChar" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.CHAR_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "setDouble" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.DOUBLE_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "setFloat" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.FLOAT_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "setInt" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.INT_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "setLong" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.LONG_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "setShort" :
	 cv = CuminEvaluator.castValue(exec_runner,getValue(2),typer.SHORT_TYPE);
	 getArrayValue(0).setIndexValue(sess,getClock(),getInt(1),cv);
	 break;
      case "newArray" :
	 JcompType atype = getTypeValue(0).getJcompType();
	 atype =  typer.findArrayType(atype);
	 rslt = CashewValue.arrayValue(typer,atype,getInt(1));
	 break;
      case "multiNewArray" :
	 atype = getTypeValue(0).getJcompType();
	 int [] dims = getIntArray(1);
	 rslt = CuminEvaluator.buildArray(exec_runner,0,dims,atype);
	 break;
      case "getLength" :
	 rslt = getArrayValue(0);
	 rslt = rslt.getFieldValue(getSession(),typer,getClock(),
	       "length",getContext(),false);
	 break;
      default :
	 return null;
    }

   return CuminRunValue.Factory.createReturn(rslt);
}


CuminRunStatus checkReferenceMethods()
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "clear" :
      case "clear0" :
	 break;
      default :
	 return null;
    }

   return CuminRunValue.Factory.createReturn(rslt);
}


CuminRunStatus checkSunReflectionMethods() throws CuminRunException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();

   switch (getMethod().getName()) {
      case "getClassAccessFlags" :
	 rslt = CashewValue.numericValue(getTyper(),getTyper().INT_TYPE,Modifier.PUBLIC);
	 break;
      case "getCallerClass" :
	 CuminRunner cr = exec_runner.getOuterCall();
	 if (cr == null) return null;
	 CuminRunner cr1 = cr.getOuterCall();
	 if (cr1 == null) return null;
	 String cls = cr1.getCallingClass();
	 JcompType typ1 = typer.findType(cls);
	 if (typ1 == null) typ1 = typer.findSystemType(cls);
	 if (typ1 == null) return null;
	 rslt = CashewValue.classValue(typer,typ1);
	 break;
      case "isSamePackage" :
	 rslt = CashewValue.booleanValue(typer,true);
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
	 if (action.getDataType(getSession(),getClock(),getTyper()).isCompatibleWith(patype)) {
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
	 CashewValue nv = CashewValue.numericValue(getTyper(),inttype,173797);
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
/*										*/
/*	Pattern methods 							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkPatternMethods() throws CuminRunException
{
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "matcherX" :
	 try {
	    CashewValue patv = getValue(0);
	    CashewValue pats = patv.getFieldValue(sess,typer,getClock(),
		  "java.util.regex.Pattern.pattern",getContext());
	    String ps = pats.getString(sess,typer,getClock());
	    String v = getString(1);
	    exec_runner.ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
	    String expr = "edu.brown.cs.seede.poppy.PoppyValue.getPatternMatcher(\"" +
		  IvyFormat.formatString(ps) + "\",\"" + IvyFormat.formatString(v)  + "\")";
	    CashewValue rslt = exec_runner.getLookupContext().evaluate(expr);
	    return CuminRunStatus.Factory.createReturn(rslt);
//	    Pattern p = Pattern.compile(ps);
//	    Matcher m = p.matcher(v);
//	    System.err.println("RESULT IS " + m);
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
   int to = getIntFieldValue(mval,"java.util.regex.Matcher.to");
   if (from < 0) from = 0;
   if (to < 0) to = textv.length();
   String expr = "edu.brown.cs.seede.poppy.PoppyValue.matchFinder(\"" +
      IvyFormat.formatString(ps) + "\",\"" +
      IvyFormat.formatString(textv)  + "\"," + pos + "," + from + "," + to + "," + fail + "," + anch + ")";
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
   CashewValue patv = mval.getFieldValue(getSession(),getTyper(),getClock(),
	 "java.util.regex.Matcher.parentPattern",getContext());
   if (patv.isNull(getSession(),getClock())) return null;

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



/********************************************************************************/
/*										*/
/*	Hidden calls needed for swing						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkSegmentMethods() throws CuminRunException
{
   CashewValue rslt = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "getSharedSegment" :
	    exec_runner.ensureLoaded("javax.swing.text.Segment");
	    String expr = "new javax.swing.text.Segment()";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "releaseSharedSegment" :
	    break;
	 default :
	    return null;
       }
    }
   else {
      switch (getMethod().getName()) {
	 case "getSegment" :
	    exec_runner.ensureLoaded("javax.swing.text.Segment");
	    String expr = "new javax.swing.text.Segment()";
	    rslt = exec_runner.getLookupContext().evaluate(expr);
	    break;
	 case "releaseSegment" :
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Handle jdk.internal.access methods					*/
/*										*/
/********************************************************************************/

CuminRunStatus checkAccessMethods() throws CashewException
{
   CashewValueSession sess = getSession();
   JcompTyper typer = getTyper();
   CashewValue rslt = null;

   CashewValue v0 = getValue(0);
   if (!v0.isNull(sess,getClock())) return null;

   switch (getMethod().getName()) {
      case "decodeASCII" :
	 byte [] sa = getByteArray(1);
	 int sp = getInt(2);
	 CashewValue da = getValue(3);
	 int dp = getInt(4);
	 int len= getInt(5);
	 // StringCoding.countPositives(sa,sp,len);
	 int count = len;
	 int limit = sp+len;
	 for (int i = sp; i < limit; ++i) {
	    if (sa[i] < 0) {
	       count = i - sp;
	       break;
	     }
	  }
	 while (count < len) {
	    if (sa[sp+count] < 0) break;
	    ++count;
	  }
	 // StringLatin1.inflate(sa sp,da,dp,count);
	 int dstoff = dp;
	 int srcoff = sp;
	 for (int i = 0; i < len; ++i) {
	    // da[dstoff++] = (char)(sa[srcoff++]&0xff);
	    char bv = (char)(sa[srcoff++]&0xff);
	    CashewValue charv = CashewValue.characterValue(typer.CHAR_TYPE,bv);
	    da.setIndexValue(sess,getClock(),dstoff++,charv);
	  }
	 // return count
	 rslt = CashewValue.numericValue(typer,typer.INT_TYPE,count);
	 break;
      default :
	 AcornLog.logE("CUMIN","No implementation for " + getMethod().getFullName());
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}

/********************************************************************************/
/*										*/
/*	Handle Array comparisons which are effectively native			*/
/*										*/
/********************************************************************************/

CuminRunStatus checkArraysMethods() throws CuminRunException
{
   if (!getMethod().isStatic()) return null;

   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   try {
      switch (getMethod().getName()) {
	 case "equals" :
	    int fidxa = 0;
	    int fidxb = 0;
	    int tidxa = 0;
	    int tidxb = 0;
	    CashewValue arra = getArrayValue(0);
	    if (!arra.getDataType(sess,getClock(),typer).getBaseType().isPrimitiveType()) {
	       return null;
	     }
	    CashewValue arrb = null;
	    if (getNumArgs() == 2) {
	       tidxa = arra.getDimension(sess,getClock());
	       arrb = getArrayValue(1);
	       tidxb = arrb.getDimension(sess,getClock());
	     }
	    else if (getNumArgs() == 6) {
	       fidxa = getInt(1);
	       tidxa = getInt(2);
	       arrb = getArrayValue(3);
	       fidxb = getInt(4);
	       tidxb = getInt(5);
	     }
	    boolean match = true;
	    if (tidxa - fidxa != tidxb-fidxb) match = false;
	    else {
	       for (int i = fidxa; i < tidxa && match; ++i) {
		  int j = i - fidxa + fidxb;
		  CashewValue v0 = arra.getIndexValue(sess,getClock(),i);
		  CashewValue v1 = arrb.getIndexValue(sess,getClock(),j);
		  Number n0 = v0.getNumber(sess,getClock());
		  Number n1 = v1.getNumber(sess,getClock());
		  if (!n0.equals(n1)) match = false;
		}
	     }
	    rslt = CashewValue.booleanValue(typer,match);
	    break;

	 default :
	    return null;
       }
    }
   catch (CashewException e) { }

   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Handle Resource Bundles 						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkResourceBundleMethods() throws CuminRunException
{
   if (!getMethod().isStatic()) return null;

   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "getBundle" :
	 CuminEvaluator.throwException(sess,getContext(),
	      typer,"java.util.MissingResourceException");
	 break;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Handle Time Zones							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkTimeZoneMethods() throws CuminRunException
{
   if (!getMethod().isStatic()) return null;

   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "getTimeZone" :
	 if (getNumArgs() == 1) {
	    CashewValue z = getValue(0);
	    JcompType typ = z.getDataType(sess,getClock(),typer);
	    if (typ.isStringType()) {
	       try {
		  String zs = z.getString(sess,typer,getClock());
		  String exp = "java.util.TimeZone.getTimeZone(\"" + zs + "\")";
		  rslt = exec_runner.getLookupContext().evaluate(exp);
		  break;
		}
	       catch (CashewException e) {
		  return null;
		}
	     }
	  }
	 return null;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}


}	// end of class CuminDirectEvaluation




/* end of CuminDirectEvaluation.java */

