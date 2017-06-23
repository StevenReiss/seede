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

import edu.brown.cs.ivy.xml.IvyXmlWriter;

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

CashewValueFile(String path)
{
   this(new File(path));
}


public CashewValueFile(File path)
{
   super(FILE_TYPE,null,false);
   user_file = path;
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


@Override public String getString(CashewClock cc,int idx,boolean dbg)
{
   if (user_file == null) return "File(null)";
   return user_file.toString();
}



public File getFile()                   { return user_file; }



@Override public CashewValue getFieldValue(CashewClock cc,String nm,boolean force)
{
   switch (nm) {
      case "path" :
         return CashewValue.stringValue(user_file.getPath());
      case "status" :
         // return PathStatus.CHECKED
         break;
      case "prefixLength" :
         return CashewValue.numericValue(INT_TYPE,0);
         
      // static fields   
      case "fs" :
         // return DefaultFileSystem.getFileSystem()
         break;
      case "separator" :
         return CashewValue.stringValue(File.separator);
      case "separatorChar" :
         return CashewValue.numericValue(CHAR_TYPE,File.separatorChar);
      case "pathSeparator" :
         return CashewValue.stringValue(File.pathSeparator);     
      case "pathSeparatorChar" :
         return CashewValue.numericValue(CHAR_TYPE,File.pathSeparatorChar);
    }
   
   return null;
}



@Override public CashewValue setFieldValue(CashewClock cc,String nm,CashewValue cv)
{
   return this;
}




/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx)
{
   xw.field("OBJECT",true);
   if (user_file == null) xw.field("FILE","*UNKNOWN*");
   else xw.field("FILE",user_file.toString());
}




}       // end of class CashewValueFile




/* end of CashewValueFile.java */

