/********************************************************************************/
/*										*/
/*		CuminIOEvaluator.java						*/
/*										*/
/*	Handle native calls to I/O methods					*/
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

import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewInputOutputModel;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueFile;

class CuminIOEvaluator extends CuminNativeEvaluator implements CuminConstants, CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static AtomicInteger	file_counter = new AtomicInteger(1024);





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminIOEvaluator(CuminRunnerByteCode bc)
{
   super(bc);
}



/********************************************************************************/
/*										*/
/*	java.io.File methods							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkFileMethods()
{
   CashewValue rslt = null;
   File rfile = null;

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "createTempFile" :
	    return null;
	 case "listRoots" :
	    return null;
	 default :
	    return null;
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
      if (rfile == null) return null;
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
	       return CuminEvaluator.returnException(CashewConstants.IO_EXCEPTION);
	     }
	    if (rfile == thisfile) rslt = thisarg;
	    break;
	 case "getCanonicalPath" :
	    try {
	       rslt = CashewValue.stringValue(thisfile.getCanonicalPath());
	     }
	    catch (IOException e) {
	       return CuminEvaluator.returnException(CashewConstants.IO_EXCEPTION);
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
	    return null;
	 case "listFiles" :
	    return null;
	 case "mkdir"  :
	    rslt = CashewValue.booleanValue(iomdl.mkdir(thisfile));
	    break;
	 case "mkdirs"  :
	    rslt = CashewValue.booleanValue(iomdl.mkdirs(thisfile));
	    break;
	 case "renameTo" :
	    return null;
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
	    return null;
	 case "toString" :
	    rslt = CashewValue.stringValue(thisfile.toString());
	    break;
	 case "toURI" :
	    return null;
	 case "toURL" :
	    return null;
	
	    // private methods	
	 case "isInvalid" :
	    // access to java.io.File.PathStatus.CHECKED fails for now
	    rslt = CashewValue.booleanValue(false);
	    break;
	
	 default :
	    AcornLog.logE("Unknown file operation: " + getMethod().getName());
	    return null;
	
       }
    }

   if (rslt == null && rfile != null) {
      rslt = new CashewValueFile(rfile);
    }

   return new CuminRunValue(Reason.RETURN,rslt);
}



/********************************************************************************/
/*										*/
/*	Handle java.io.FileOuptutStream methods 				*/
/*										*/
/********************************************************************************/

CuminRunStatus checkOutputStreamMethods()
{
   CashewValue thisarg = getValue(0);
   CashewValue fdval = thisarg.getFieldValue(getClock(),"java.io.FileOutputStream.fd");
   if (fdval.isNull(getClock())) return null;
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
	    if (f.exists() && !f.canWrite()) {
	       return CuminEvaluator.returnException(IO_EXCEPTION);
	     }
	    //TODO: check that directory is writable
	  }
	 if (fdv < 0) {
	    fdv = file_counter.incrementAndGet();
	    fdval.setFieldValue(getClock(),"java.io.FileDescriptor.fd",
		  CashewValue.numericValue(INT_TYPE,fdv));
	  }
	 break;
      case "write" :
	 if (narg != 3) return null;
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
	 return null;
    }

   return new CuminRunValue(Reason.RETURN,rslt);
}



/********************************************************************************/
/*										*/
/*	Handle input streams							*/
/*										*/
/********************************************************************************/

CuminRunStatus checkInputStreamMethods()
{
   CashewValue thisarg = getValue(0);
   CashewValue fdval = thisarg.getFieldValue(getClock(),"java.io.FileInputStream.fd");
   if (fdval.isNull(getClock())) return null;
   CashewValue fd = fdval.getFieldValue(getClock(),"java.io.FileDescriptor.fd");
   int fdv = fd.getNumber(getClock()).intValue();
   String path = null;
   try {
      CashewValue pathv = thisarg.getFieldValue(getClock(),"java.io.FileInputStream.path");
      if (!pathv.isNull(getClock())) path = pathv.getString(getClock());
    }
   catch (Throwable t) {
      // path is not defined before jdk 1.8
    }

   CashewInputOutputModel mdl = getContext().getIOModel();

   CashewValue rslt = null;
   byte [] wbuf = null;

   try {
      switch (getMethod().getName()) {
	 case "open" :
	    if (path != null && fdv < 0) {
	       File f = new File(path);
	       if (!f.canRead()) {
		  return CuminEvaluator.returnException(IO_EXCEPTION);
		}
	     }
	    if (fdv < 0) {
	       fdv = file_counter.incrementAndGet();
	       fdval.setFieldValue(getClock(),"java.io.FileDescriptor.fd",
		     CashewValue.numericValue(INT_TYPE,fdv));
	       mdl.checkInputFile(getContext(),getClock(),thisarg,fdv,path,false);
	     }
	    break;
	 case "read0" :
	    mdl.checkInputFile(getContext(),getClock(),thisarg,fdv,path,true);
	    wbuf = new byte[1];
	    wbuf[0] = (byte) getInt(1);
	    long lenread = mdl.fileRead(getClock(),fdv,wbuf,0,1);
	    rslt = CashewValue.numericValue(INT_TYPE,lenread);
	    break;
	 case "readBytes" :
	    mdl.checkInputFile(getContext(),getClock(),thisarg,fdv,path,true);
	    int len = getInt(3);
	    wbuf = new byte[len];
	    lenread = mdl.fileRead(getClock(),fdv,wbuf,0,len);
	    // copy wbuf into byteArray(1), offset getInt(2), for lenread
	    rslt = CashewValue.numericValue(INT_TYPE,lenread);
	    break;
	 case "skip" :
	    mdl.checkInputFile(getContext(),getClock(),thisarg,fdv,path,true);
	    lenread = mdl.fileRead(getClock(),fdv,null,0,getLong(1));
	    rslt = CashewValue.numericValue(LONG_TYPE,lenread);
	    break;
	 case "available" :
	    mdl.checkInputFile(getContext(),getClock(),thisarg,fdv,path,true);
	    lenread = mdl.fileAvailable(getClock(),fdv);
	    rslt = CashewValue.numericValue(INT_TYPE,lenread);
	    break;
	 case "close" :
	    mdl.closeFile(fdv);
	    break;
	 case "initIDs" :
	    break;
	 default :
	    return null;
       }
    }
   catch (IOException e) {
      return CuminEvaluator.returnException(IO_EXCEPTION);
    }

   return new CuminRunValue(Reason.RETURN,rslt);
}



/********************************************************************************/
/*                                                                              */
/*      Handle console methods                                                  */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkConsoleMethods()
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "encoding" :
         rslt = CashewValue.nullValue();
         break;
      case "echo" :
         rslt = CashewValue.booleanValue(true);
         break;
      case "istty" :
         rslt = CashewValue.booleanValue(false);
         break;
      default :
         return null;
    }
   
   return new CuminRunValue(Reason.RETURN,rslt);
}



/********************************************************************************/
/*                                                                              */
/*      Handle printWriter/printStream methods                                  */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkPrintMethods(String cls)
{
   CashewValue rslt = null;
   String ocls = cls;
   if (cls.equals("java.io.PrintStream")) ocls = "java.io.FilterOutputStream";
   String mnm = getMethod().getName();
   
   if (mnm.equals("flush") || mnm.equals("<init>")) return null;
   boolean forceflush = false;
   
   CashewValue thisarg = getValue(0);
   CashewValue fdval = thisarg.getFieldValue(getClock(),cls + ".autoFlush");
   if (!fdval.getBoolean(getClock())) {
      forceflush = true;
    }
   CashewValue cv1 = thisarg.getFieldValue(getClock(),ocls + ".out");
   while (!cv1.getDataType(getClock()).getName().equals("java.io.FileOutputStream")) {
      String typ = cv1.getDataType(getClock()).getName();
      String fld = "out";
      String nxt = null;
      switch (typ) {
         case "java.io.PrintWriter" :
             nxt = typ;
             break;
         case "java.io.FileWriter" :
            nxt = "java.io.Writer";
            fld = "lock";
            break;
         case "java.io.BufferedOutputStream" :
            nxt = "java.io.FilterOutputStream";
            break;
         default : 
            nxt = null;
            break;
       }
      if (nxt == null) return null;
      cv1 = cv1.getFieldValue(getClock(),nxt + "." + fld,false);
      if (cv1 == null) return null;
    }
    
   String sfx = null;
   CashewInputOutputModel mdl = getContext().getIOModel();
   fdval = cv1.getFieldValue(getClock(),"java.io.FileOutputStream.fd");
   if (fdval.isNull(getClock())) return null;
   CashewValue fd = fdval.getFieldValue(getClock(),"java.io.FileDescriptor.fd");
   int fdv = fd.getNumber(getClock()).intValue();
   CashewValue appv = cv1.getFieldValue(getClock(),"java.io.FileOutputStream.append");
   boolean app = appv.getBoolean(getClock());
   String path = null;
   try {
      CashewValue pathv = cv1.getFieldValue(getClock(),"java.io.FileOutputStream.path");
      if (!pathv.isNull(getClock())) path = pathv.getString(getClock());
    }
   catch (Throwable t) {
      // path is not defined before jdk 1.8
    }
   
   String toout = null;
   CashewValue argv = null;
   if (getNumArgs() > 1) argv = getValue(1);
   
   switch (mnm) {
      default : 
         return null;
         
      case "write" :
         if (forceflush) {
            exec_runner.executeCall(cls + ".flush",thisarg);
          }
         if (cls.equals("java.io.PrintWriter") && getNumArgs() == 2 &&
               argv.getDataType(getClock()).getName().equals("java.lang.String")) {
            toout = argv.getString(getClock());
          }
         else if (cls.equals("java.io.PrintWriter") && getNumArgs() == 4 &&
               argv.getDataType(getClock()).getName().equals("java.lang.String")) {
            String s = argv.getString(getClock());
            int off = getInt(2);
            int len = getInt(3);
            toout = s.substring(off,len);
          }
         else return null;
         break;
      case "println" :
      case "print" :
         if (forceflush) {
            exec_runner.executeCall(cls + ".flush",thisarg);
          }
         if (mnm.equals("println")) 
            sfx = "\n";
         if (getNumArgs() == 1) toout = "";
         else if (getNumArgs() != 2) return null;
         else if (argv.isNull(getClock())) toout = "null";
         else {
            switch (argv.getDataType(getClock()).getName()) {
               case "int" :
               case "short" :
               case "byte" :
               case "long" :
               case "float" :
               case "double" :
               case "java.lang.String" :
                  toout = argv.getString(getClock());
                  break;
               case "boolean" :
                  toout = Boolean.toString(argv.getBoolean(getClock()));
                  break;
               case "char" :
                  toout = Character.toString(argv.getChar(getClock()));
                  break;
               default :
                  return null;
             }
          }
         break;
    }
   
   if (toout == null) return null;
   if (sfx != null) toout += sfx;
   byte [] bytes = toout.getBytes();
   mdl.fileWrite(getClock(),fdv,path,bytes,0,bytes.length,app);
   
   return new CuminRunValue(Reason.RETURN,rslt);
}



/********************************************************************************/
/*                                                                              */
/*      Handle Object input/output streams                                      */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkObjectStreamMethods()
{
   CashewValue rslt = null;
   
   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
         case "bytesToFloats" :
            byte [] src = getByteArray(0);
            int srcpos = getInt(1);
            CashewValue dst = getValue(2);
            int dstpos = getInt(3);
            int nfloats = getInt(4);
            for (int i = 0; i < nfloats; ++i) {
               int v = ((src[srcpos+i*4] & 0xff) << 24) +
                  ((src[srcpos+i*4+1] & 0xff) << 16) +
                  ((src[srcpos+i*4+2] & 0xff) << 8) + 
                  ((src[srcpos+i*4+3] & 0xff) << 0) ;
               float fv = Float.intBitsToFloat(v);
               CashewValue rv = CashewValue.numericValue(FLOAT_TYPE,fv);
               dst.setIndexValue(getClock(),dstpos+i,rv);
             }
            break;
         case "bytesToDoubles" :
            src = getByteArray(0);
            srcpos = getInt(1);
            dst = getValue(2);
            dstpos = getInt(3);
            nfloats = getInt(4);
            for (int i = 0; i < nfloats; ++i) {
               long v = ((src[srcpos+i*8] & 0xff) << 56) +
               ((src[srcpos+i*8+1] & 0xff) << 48) +
               ((src[srcpos+i*8+2] & 0xff) << 40) + 
               ((src[srcpos+i*8+3] & 0xff) << 32) +
               ((src[srcpos+i*8+4] & 0xff) << 24) +
               ((src[srcpos+i*8+5] & 0xff) << 16) +
               ((src[srcpos+i*8+6] & 0xff) << 8) + 
               ((src[srcpos+i*8+7] & 0xff) << 0) ;
               double fv = Double.longBitsToDouble(v);
               CashewValue rv = CashewValue.numericValue(FLOAT_TYPE,fv);
               dst.setIndexValue(getClock(),dstpos+i,rv);
             }
            break;
         case "floatsToBytes" :
            float [] fsrc = getFloatArray(0);
            srcpos = getInt(1);
            dst = getValue(2);
            dstpos = getInt(3);
            nfloats = getInt(4);
            for (int i = 0; i < nfloats; ++i) {
               int v = Float.floatToIntBits(fsrc[srcpos+i]);
               for (int j = 0; j < 4; ++j) {
                  CashewValue cv = CashewValue.numericValue(BYTE_TYPE,(v&0xff));
                  dst.setIndexValue(getClock(),dstpos+i*4+(3-j),cv);
                  v = v>>8;
                }
             }
            break;
         case "doublesToBytes" :
            double [] dsrc = getDoubleArray(0);
            srcpos = getInt(1);
            dst = getValue(2);
            dstpos = getInt(3);
            nfloats = getInt(4);
            for (int i = 0; i < nfloats; ++i) {
               long v = Double.doubleToLongBits(dsrc[srcpos+i]);
               for (int j = 0; j < 8; ++j) {
                  CashewValue cv = CashewValue.numericValue(BYTE_TYPE,(v&0xff));
                  dst.setIndexValue(getClock(),dstpos+i*8+(7-j),cv);
                  v = v>>8;
                }
             }
            break;
         default :
            return null;
       }
    }
   else return null;
   
   return new CuminRunValue(Reason.RETURN,rslt); 
}



}	// end of class CuminIOEvaluator




/* end of CuminIOEvaluator.java */

