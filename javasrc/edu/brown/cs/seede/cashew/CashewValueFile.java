/********************************************************************************/
/*                                                                              */
/*              CashewValueFile.java                                            */
/*                                                                              */
/*      Representation of java.io.File objects                                  */
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



package edu.brown.cs.seede.cashew;

import java.io.File;
import java.util.Map;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;

public class CashewValueFile extends CashewValueObject implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private File            user_file;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewValueFile(JcompTyper typer,String path)
{
   this(typer,new File(path));
}


public CashewValueFile(JcompTyper typer,File path)
{
   super(typer,typer.findSystemType("java.io.File"),null,false);
   user_file = path;
}


CashewValueFile(CashewValueSession sess,CashewContext ctx,JcompTyper typer,
      JcompType jt,Map<String,Object> inits,boolean caninit)
{
   super(typer,jt,inits,caninit);
   user_file = null;
   CashewValue cv = super.getFieldValue(sess,typer,null,"java.io.File.path",ctx,true);
   AcornLog.logD("CASHEW","Setup CashewValueFile " + cv + " " + inits + " " + caninit);
   if (!cv.isNull(sess,null)) {
      try { 
         String path = cv.getString(sess,typer,null);
         user_file = new File(path);
       }
      catch (CashewException e) { }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public void setInitialValue(File f) 
{
   user_file = f;
}


@Override public String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,int idx,boolean dbg)
{
   if (user_file == null) return "File(null)";
   return user_file.toString();
}



public File getFile()                   { return user_file; }



@Override public CashewValue getFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String nm,CashewContext ctx,boolean force)
{
   switch (nm) {
      case "java.io.File.path" :
      case "path" :
         if (user_file == null) return CashewValue.nullValue(typer);
         return CashewValue.stringValue(typer,typer.STRING_TYPE,user_file.getPath());
      case "java.io.File.status" :
      case "status" :
         // return PathStatus.CHECKED
         break;
      case "java.io.File.prefixLength" :
      case "prefixLength" :
         return CashewValue.numericValue(typer.INT_TYPE,0);
         
      // static fields 
      case "java.io.File.fs" :
      case "fs" :
         // return DefaultFileSystem.getFileSystem()
         break;
      case "java.io.File.separator" :
      case "separator" :
         return CashewValue.stringValue(typer,typer.STRING_TYPE,File.separator);
      case "java.io.File.separatorChar" :
      case "separatorChar" :
         return CashewValue.numericValue(typer,typer.CHAR_TYPE,File.separatorChar);
      case "java.io.File.pathSeparator" :
      case "pathSeparator" :
         return CashewValue.stringValue(typer,typer.STRING_TYPE,File.pathSeparator);     
      case "java.io.File.pathSeparatorChar" :
      case "pathSeparatorChar" :
         return CashewValue.numericValue(typer,typer.CHAR_TYPE,File.pathSeparatorChar);
      default :
         AcornLog.logE("CASHEW","Unknown File field: " + nm);
         break;
    }
   
   return null;
}



@Override public CashewValue setFieldValue(CashewValueSession sess,JcompTyper typer,
      CashewClock cc,String nm,CashewValue cv)
{
   return this;
}




/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name)
{
   xw.field("OBJECT",true);
   if (user_file == null) xw.field("FILE","*UNKNOWN*");
   else xw.field("FILE",user_file.toString());
}




}       // end of class CashewValueFile




/* end of CashewValueFile.java */

