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

import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewException;
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

//CHECKSTYLE:OFF
CuminRunStatus checkFileMethods() throws CashewException, CuminRunException
//CHECKSTYLE:ON
{
   CashewValue rslt = null;
   File rfile = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

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
	 if (getDataType(1) == typer.STRING_TYPE) {
	    rfile = new File(getString(1));
	  }
	 // handle URI arg
       }
      else {
	 if (getDataType(1) == typer.STRING_TYPE) {
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

      try {
         switch (getMethod().getName()) {
            case "canExecute" :
               rslt = CashewValue.booleanValue(typer,iomdl.canExecute(thisfile));
               break;
            case "canRead" :
               rslt = CashewValue.booleanValue(typer,iomdl.canRead(thisfile));
               break;
            case "canWrite" :
               rslt = CashewValue.booleanValue(typer,iomdl.canWrite(thisfile));
               break;
            case "compareTo" :
               rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisfile.compareTo(getFile(1)));
               break;
            case "delete" :
               rslt = CashewValue.booleanValue(typer,iomdl.delete(thisfile));
               break;
            case "deleteOnExit" :
               break;
            case "equals" :
               CashewValue cv = getValue(1);
               if (cv instanceof CashewValueFile) {
                  rslt = CashewValue.booleanValue(typer,thisfile.equals(getFile(1)));
                }
               else rslt = CashewValue.booleanValue(typer,false);
               break;
            case "exists" :
               rslt = CashewValue.booleanValue(typer,iomdl.exists(thisfile));
               break;
            case "getAbsoluteFile" :
               rfile = thisfile.getAbsoluteFile();
               if (rfile == thisfile) rslt = thisarg;
               break;
            case "getAbsolutePath" :
               rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisfile.getAbsolutePath());
               break;
            case "getCanonicalFile" :
               rfile = thisfile.getCanonicalFile();
               if (rfile == thisfile) rslt = thisarg;
               break;
            case "getCanonicalPath" :
               rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisfile.getCanonicalPath());
               break;
            case "getFreeSpace" :
               rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,thisfile.getFreeSpace());
               break;
            case "getName" :
               rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisfile.getName());
               break;
            case "getParent" :
               rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisfile.getParent());
               break;
            case "getParentFile" :
               rfile = thisfile.getParentFile();
               break;
            case "getPath" :
               rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisfile.getPath());
               break;
            case "getTotalSpace" :
               rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,thisfile.getTotalSpace());
               break;
            case "getUsableSpace" :
               rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,thisfile.getUsableSpace());
               break;
            case "hashCode" :
               rslt = CashewValue.numericValue(typer,typer.INT_TYPE,thisfile.hashCode());
               break;
            case "isAbsolute" :
               rslt = CashewValue.booleanValue(typer,thisfile.isAbsolute());
               break;
            case "isDirectory" :
               rslt = CashewValue.booleanValue(typer,iomdl.isDirectory(thisfile));
               break;
            case "isFile" :
               rslt = CashewValue.booleanValue(typer,iomdl.isFile(thisfile));
               break;
            case "isHidden" :
               rslt = CashewValue.booleanValue(typer,thisfile.isHidden());
               break;
            case "lastModified" :
               rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,thisfile.lastModified());
               break;
            case "length" :
               rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,thisfile.length());
               break;
            case "list" :
               return null;
            case "listFiles" :
               return null;
            case "mkdir"  :
               rslt = CashewValue.booleanValue(typer,iomdl.mkdir(thisfile));
               break;
            case "mkdirs"  :
               rslt = CashewValue.booleanValue(typer,iomdl.mkdirs(thisfile));
               break;
            case "renameTo" :
               return null;
            case "setExecutable" :
               iomdl.setExecutable(thisfile);
               rslt = CashewValue.booleanValue(typer,true);
               break;
            case "setLastModified" :
               rslt = CashewValue.booleanValue(typer,false);
               break;
            case "setReadable" :
               iomdl.setReadable(thisfile);
               rslt = CashewValue.booleanValue(typer,true);
               break;
            case "setReadOnly" :
               iomdl.setReadOnly(thisfile);
               rslt = CashewValue.booleanValue(typer,true);
               break;
            case "setWritable" :
               iomdl.setWritable(thisfile);
               rslt = CashewValue.booleanValue(typer,true);
               break;
            case "toPath" :
               return null;
            case "toString" :
               rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,thisfile.toString());
               break;
            case "toURI" :
               return null;
            case "toURL" :
               return null;
               
               // private methods	
            case "isInvalid" :
               // access to java.io.File.PathStatus.CHECKED fails for now
               rslt = CashewValue.booleanValue(typer,false);
               break;
               
            default :
               AcornLog.logE("Unknown file operation: " + getMethod().getName());
               return null;
               
          }
       }
      catch (IOException e) {
         return CuminEvaluator.returnException(sess,getContext(),typer,"java.io.IOException");
       }
      catch (NullPointerException e) {
         CuminEvaluator.throwException(getSession(),getContext(),getTyper(),e.getClass().getName());
       }
    }

   if (rslt == null && rfile != null) {
      rslt = new CashewValueFile(typer,rfile);
    }

   return new CuminRunValue(Reason.RETURN,rslt);
}



/********************************************************************************/
/*										*/
/*	Handle java.io.FileOuptutStream methods 				*/
/*										*/
/********************************************************************************/

CuminRunStatus checkOutputStreamMethods() throws CashewException
{
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();
   CashewValue thisarg = getValue(0);
   CashewValue fdval = thisarg.getFieldValue(sess,typer,getClock(),
         "java.io.FileOutputStream.fd",getContext());
   if (fdval.isNull(sess,getClock())) return null;
   CashewValue fd = fdval.getFieldValue(sess,typer,getClock(),
         "java.io.FileDescriptor.fd",getContext());
   int fdv = fd.getNumber(sess,getClock()).intValue();
   String path = null;
   try {
      CashewValue pathv = thisarg.getFieldValue(sess,typer,getClock(),
            "java.io.FileOutputStream.path",getContext());
      if (!pathv.isNull(sess,getClock())) path = pathv.getString(sess,typer,getClock());
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
	       return CuminEvaluator.returnException(sess,getContext(),typer,"java.io.IOException");
	     }
	    //TODO: check that directory is writable
	  }
	 if (fdv < 0) {
	    fdv = file_counter.incrementAndGet();
	    fdval.setFieldValue(sess,typer,getClock(),"java.io.FileDescriptor.fd",
		  CashewValue.numericValue(typer,typer.INT_TYPE,fdv));
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

CuminRunStatus checkInputStreamMethods() throws CashewException 
{
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();
   CashewValue thisarg = getValue(0);
   CashewValue fdval = thisarg.getFieldValue(sess,typer,getClock(),
         "java.io.FileInputStream.fd",getContext());
   if (fdval.isNull(sess,getClock())) return null;
   CashewValue fd = fdval.getFieldValue(sess,typer,getClock(),
         "java.io.FileDescriptor.fd",getContext());
   int fdv = fd.getNumber(sess,getClock()).intValue();
   String path = null;
   try {
      CashewValue pathv = thisarg.getFieldValue(sess,typer,getClock(),
            "java.io.FileInputStream.path",getContext());
      if (!pathv.isNull(sess,getClock())) path = pathv.getString(sess,typer,getClock());
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
                  return CuminEvaluator.returnException(sess,getContext(),typer,"java.io.IOException");
		}
	     }
	    if (fdv < 0) {
	       fdv = file_counter.incrementAndGet();
	       fdval.setFieldValue(sess,typer,getClock(),"java.io.FileDescriptor.fd",
		     CashewValue.numericValue(typer,typer.INT_TYPE,fdv));
	       mdl.checkInputFile(sess,typer,getContext(),getClock(),thisarg,fdv,path,false);
	     }
	    break;
	 case "read0" :
	    mdl.checkInputFile(sess,typer,getContext(),getClock(),thisarg,fdv,path,true);
	    wbuf = new byte[1];
	    wbuf[0] = (byte) getInt(1);
	    long lenread = mdl.fileRead(getClock(),fdv,wbuf,0,1);
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,lenread);
	    break;
	 case "readBytes" :
	    mdl.checkInputFile(sess,typer,getContext(),getClock(),thisarg,fdv,path,true);
	    int len = getInt(3);
	    wbuf = new byte[len];
	    lenread = mdl.fileRead(getClock(),fdv,wbuf,0,len);
	    // copy wbuf into byteArray(1), offset getInt(2), for lenread
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,lenread);
	    break;
	 case "skip" :
	    mdl.checkInputFile(sess,typer,getContext(),getClock(),thisarg,fdv,path,true);
	    lenread = mdl.fileRead(getClock(),fdv,null,0,getLong(1));
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,lenread);
	    break;
	 case "available" :
	    mdl.checkInputFile(sess,typer,getContext(),getClock(),thisarg,fdv,path,true);
	    lenread = mdl.fileAvailable(getClock(),fdv);
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,lenread);
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
      return CuminEvaluator.returnException(sess,getContext(),typer,"java.io.IOException");
    }

   return new CuminRunValue(Reason.RETURN,rslt);
}

CuminRunStatus checkFileCleanableMethods()
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "register" :
      case "unregister" :
      case "performCleanup" :
         return new CuminRunValue(Reason.RETURN,rslt);
      default : 
         break;
    }
   
   return null;
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
         rslt = CashewValue.nullValue(getTyper());
         break;
      case "echo" :
         rslt = CashewValue.booleanValue(getTyper(),true);
         break;
      case "istty" :
         rslt = CashewValue.booleanValue(getTyper(),false);
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

//CHECKSTYLE:OFF
CuminRunStatus checkPrintMethods(String cls) throws CashewException, CuminRunException
//CHECKSTYLE:ON
{
   CashewValue rslt = null;
   String ocls = cls;
   if (cls.equals("java.io.PrintStream")) ocls = "java.io.FilterOutputStream";
   String mnm = getMethod().getName();
   
   if (mnm.equals("flush") || mnm.equals("<init>")) return null;
   boolean forceflush = false;
   
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();
   
   CashewValue thisarg = getValue(0);
   CashewValue fdval = thisarg.getFieldValue(sess,typer,getClock(),
         cls + ".autoFlush",getContext());
   if (!fdval.getBoolean(sess,getClock())) {
      forceflush = true;
    }
   CashewValue cv1 = thisarg.getFieldValue(sess,typer,getClock(),
         ocls + ".out",getContext());
   while (!cv1.getDataType(sess,getClock(),null).getName().equals("java.io.FileOutputStream")) {
      String typ = cv1.getDataType(sess,getClock(),null).getName();
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
      cv1 = cv1.getFieldValue(sess,typer,getClock(),
            nxt + "." + fld,getContext(),false);
      if (cv1 == null) return null;
    }
    
   String sfx = null;
   CashewInputOutputModel mdl = getContext().getIOModel();
   fdval = cv1.getFieldValue(sess,typer,getClock(),
         "java.io.FileOutputStream.fd",getContext());
   if (fdval.isNull(sess,getClock())) return null;
   CashewValue fd = fdval.getFieldValue(sess,typer,getClock(),
         "java.io.FileDescriptor.fd",getContext());
   int fdv = fd.getNumber(sess,getClock()).intValue();
   
   boolean app = false;
// try {
//    CashewValue appv = cv1.getFieldValue(sess,typer,getClock(),
//          "java.io.FileOutputStream.append",getContext());
//    app = appv.getBoolean(sess,getClock());
//  }
// catch (Throwable t) {
      // append is not defined in Java 10 -- need to compute this some other way
//  }
   
   String path = null;
   try {
      CashewValue pathv = cv1.getFieldValue(sess,typer,getClock(),
            "java.io.FileOutputStream.path",getContext());
      if (!pathv.isNull(sess,getClock())) path = pathv.getString(sess,typer,getClock());
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
               argv.getDataType(sess,getClock(),null).getName().equals("java.lang.String")) {
            toout = argv.getString(sess,typer,getClock());
          }
         else if (cls.equals("java.io.PrintWriter") && getNumArgs() == 4 &&
               argv.getDataType(sess,getClock(),null).getName().equals("java.lang.String")) {
            String s = argv.getString(sess,typer,getClock());
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
         else if (argv.isNull(sess,getClock())) toout = "null";
         else {
            switch (argv.getDataType(sess,getClock(),null).getName()) {
               case "int" :
               case "short" :
               case "byte" :
               case "long" :
               case "float" :
               case "double" :
               case "java.lang.String" :
                  toout = argv.getString(sess,typer,getClock());
                  break;
               case "boolean" :
                  toout = Boolean.toString(argv.getBoolean(sess,getClock()));
                  break;
               case "char" :
                  toout = Character.toString(argv.getChar(sess,getClock()));
                  break;
               default :
                  return null;
             }
          }
         break;
      case "printf" :
      case "format" :
         int base = 1;
         if (getNumArgs() == 4) {
            base = 2;
            argv = getValue(2);
          }
         String fmt = argv.getString(sess,typer,getClock());
         CashewValue arr = getValue(base+1);
         Object [] args = new Object[0];
         if (!arr.isNull(sess,getClock())) {
            int sz = arr.getDimension(sess,getClock());
            if (sz > 0) {
               args = new Object[sz];
               for (int i = 0; i < sz; ++i) {
                  CashewValue v0 = arr.getIndexValue(sess,getClock(),i);
                  String s0 = v0.getString(sess,typer,getClock());
                  args[i] = s0;
                }
             }
          }
         toout = String.format(fmt,args);
         rslt = thisarg;
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

CuminRunStatus checkObjectStreamMethods() throws CashewException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();
   
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
                  ((src[srcpos+i*4+3] & 0xff) << 0);
               float fv = Float.intBitsToFloat(v);
               CashewValue rv = CashewValue.numericValue(typer.FLOAT_TYPE,fv);
               dst.setIndexValue(sess,getClock(),dstpos+i,rv);
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
               ((src[srcpos+i*8+7] & 0xff) << 0);
               double fv = Double.longBitsToDouble(v);
               CashewValue rv = CashewValue.numericValue(typer.FLOAT_TYPE,fv);
               dst.setIndexValue(sess,getClock(),dstpos+i,rv);
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
                  CashewValue cv = CashewValue.numericValue(typer,typer.BYTE_TYPE,(v&0xff));
                  dst.setIndexValue(sess,getClock(),dstpos+i*4+(3-j),cv);
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
                  CashewValue cv = CashewValue.numericValue(typer,typer.BYTE_TYPE,(v&0xff));
                  dst.setIndexValue(sess,getClock(),dstpos+i*8+(7-j),cv);
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

