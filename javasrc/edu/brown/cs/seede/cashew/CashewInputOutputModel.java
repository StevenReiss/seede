/********************************************************************************/
/*										*/
/*		CashewInputOutputModel.java					*/
/*										*/
/*	Handle file operations in simulation					*/
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



package edu.brown.cs.seede.cashew;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class CashewInputOutputModel implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<Integer,OutputData> 	output_files;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public CashewInputOutputModel()
{
   output_files = new TreeMap<Integer,OutputData>();
}



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

synchronized public void clear()
{
   output_files.clear();
}



synchronized public void fileWrite(CashewClock clk,int fd,String path,byte [] buf,int off,int len,boolean app)
{
   if (fd < 0) return;
   OutputData file = output_files.get(fd);
   if (file == null) {
      file = new OutputData(fd,path);
      output_files.put(fd,file);
    }

   file.addBytes(clk.tick(),buf,off,len);
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public void outputXml(IvyXmlWriter xw)
{
   xw.begin("IOMODEL");
   for (OutputData fd : output_files.values()) {
      fd.outputXml(xw);
    }
   xw.end("IOMODEL");
}


/********************************************************************************/
/*										*/
/*	File Representation							*/
/*										*/
/********************************************************************************/

private static class OutputData {

   private int file_fd;
   private String file_path;
   private List<WriteData> write_data;
   private boolean is_binary;

   OutputData(int fd,String path) {
      file_fd = fd;
      if (path == null) {
	 if (fd == 0) path = "*STDIN*";
	 else if (fd == 1) path = "*STDOUT*";
	 else if (fd == 2) path = "*STDERR*";
	 else path = "???";
       }
      file_path = path;
      write_data = new ArrayList<WriteData>();
      is_binary = false;
    }

   void addBytes(long when,byte [] buf,int off,int len) {
      if (!is_binary) {
	 for (int i = 0; i < len; ++i) {
	    if (buf[i+off] >= 128) is_binary = false;
	  }
       }
      write_data.add(new WriteData(when,buf,off,len));
    }

   void outputXml(IvyXmlWriter xw) {
      xw.begin("OUTPUT");
      xw.field("FD",file_fd);
      xw.field("PATH",file_path);
      if (is_binary) xw.field("BINARY",true);
      for (WriteData wd : write_data) {
	 wd.outputXml(xw,is_binary);
       }
      xw.end("OUTPUT");
    }

}	// end of inner class OutputData




private static class WriteData {

   private byte [] write_data;
   private long    write_time;

   WriteData(long when,byte [] data,int off,int len) {
      write_data = new byte[len];
      System.arraycopy(data,off,write_data,0,len);
      write_time = when;
    }

   void outputXml(IvyXmlWriter xw,boolean binary) {
      xw.begin("WRITE");
      xw.field("WHEN",write_time);
      if (binary) {
	 xw.bytesElement("DATA",write_data);
       }
      else {
	 xw.cdataElement("DATA",new String(write_data));
       }
      xw.end("WRITE");
    }

}	// end of inner class WriteData




}	// end of class CashewInputOutputModel




/* end of CashewInputOutputModel.java */

