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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
private Map<Integer,InputData>          input_files;
private Set<File>                       files_created;
private Set<File>                       files_removed;
private Map<File,Integer>               file_permissions;


private int     READ_PERM = 1;
private int     WRITE_PERM = 2;
private int     EXEC_PERM = 4;

private int     DIRECTORY_PERM = 32;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public CashewInputOutputModel()
{
   output_files = new TreeMap<Integer,OutputData>();
   input_files = new TreeMap<Integer,InputData>();
   files_created = new HashSet<File>();
   files_removed = new HashSet<File>();
   file_permissions = new HashMap<File,Integer>();
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

synchronized public void clear()
{
   output_files.clear();
   input_files.clear();
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
/*                                                                              */
/*      Input methods                                                           */
/*                                                                              */
/********************************************************************************/

synchronized public long fileRead(CashewClock clk,int fd,byte [] buf,int off,long len)
{
   // note buf can be null to indicate a skip
   
   return 0;
}


synchronized public long fileAvailable(CashewClock clk,int fd)
{
   return 0;
}


synchronized public void checkInputFile(CashewValue cv,int fd,String path,boolean prior)
{
   InputData id = input_files.get(fd);
   if (id != null) return;
   
   // if we know of input file fd, just return
   // else if prior == false, open a new input file using path,fd
   // else open a new input file using path,fd and set its position to the current file
   //   position if available
}




/********************************************************************************/
/*										*/
/*	Model output methods				        		*/
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
/*                                                                              */
/*      File methods                                                            */
/*                                                                              */
/********************************************************************************/

public boolean canExecute(File f)
{
   Boolean fg = checkPermission(f,EXEC_PERM);
   if (fg != null) return fg;
   return f.canExecute();
}


public boolean canRead(File f)
{
   Boolean fg = checkPermission(f,READ_PERM);
   if (fg != null) return fg;
   return f.canExecute();
}



public boolean canWrite(File f)
{
   Boolean fg = checkPermission(f,WRITE_PERM);
   if (fg != null) return fg;
   return f.canExecute();
}



public boolean isDirectory(File f)
{
   Boolean fg = checkPermission(f,DIRECTORY_PERM);
   if (fg != null) return fg;
   return f.isDirectory();
}


public boolean isFile(File f)
{
   if (!exists(f)) return false;
   Boolean fg = checkPermission(f,DIRECTORY_PERM);
   if (fg != null) return !fg;
   return f.isFile();
}



public void setExecutable(File f)
{
   setPermission(f,EXEC_PERM,0);
}



public void setWritable(File f)
{
   setPermission(f,WRITE_PERM,0);
}


public void setReadable(File f)
{
   setPermission(f,READ_PERM,0);
}



public void setReadOnly(File f)
{
   setPermission(f,READ_PERM,WRITE_PERM|EXEC_PERM);
}


public boolean exists(File f)
{
   if (files_removed.contains(f)) return false;
   else if (files_created.contains(f)) return true;
   return f.exists();
}


public boolean delete(File f)
{
   if (!exists(f)) return false;
   files_created.remove(f);
   files_removed.add(f);
   return true;
}


public boolean mkdir(File f)
{
   if (exists(f)) return false;
   if (!exists(f.getParentFile())) return false;
   if (!canWrite(f.getParentFile())) return false;
   files_removed.remove(f);
   files_created.add(f);
   file_permissions.put(f,DIRECTORY_PERM|EXEC_PERM|WRITE_PERM|READ_PERM);
   return true;
}



public boolean mkdirs(File f)
{
   if (exists(f)) return false;
   if (!testMkdirs(f.getParentFile())) return false;
   return mkdir(f);   
}


public boolean renameTo(File s,File d)
{
   File dpar = d.getParentFile();
   if (!exists(dpar) || isDirectory(dpar) || !canWrite(dpar)) return false;
   File spar = s.getParentFile();
   if (!exists(s) || !isDirectory(spar) || !canWrite(spar)) return false;
   int perm = getPermission(s);
   if (!delete(s)) return false;
   files_removed.remove(d);
   files_created.add(d);
   file_permissions.put(d,perm);
   return true;
}



private boolean testMkdirs(File f)
{
   if (f == null) return false;
   if (exists(f) && isDirectory(f) && canWrite(f)) return true;
   if (exists(f)) return false;
   if (testMkdirs(f.getParentFile())) return false;
   if (!mkdir(f)) return false;
   return true;
}


private Boolean checkPermission(File f,int sts)
{
   Integer perm = file_permissions.get(f);
   if (perm == null) return null;
   return (perm & sts) != 0;
}



private void setPermission(File f,int set,int unset)
{
   int perm = getPermission(f);
   perm |= set;
   perm &= ~unset;
   file_permissions.put(f,perm);
}



private int getPermission(File f)
{
   Integer p = file_permissions.get(f);
   if (p != null) return p;
   
   int perm = 0;
   if (f.canRead()) perm |= READ_PERM;
   if (f.canWrite()) perm |= WRITE_PERM;
   if (f.canExecute()) perm |= EXEC_PERM;
   if (f.isDirectory()) perm |= DIRECTORY_PERM;
   
   return perm;
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




/********************************************************************************/
/*                                                                              */
/*      Input file modeling                                                     */
/*                                                                              */
/********************************************************************************/

private static class InputData {

   private int file_fd;
   private String file_path;
   private long current_pos;
   private long start_pos;
   private long file_length;
   
   InputData() {
      file_fd = 0;
      file_path = null;
      current_pos = 0;
      start_pos = 0;
      file_length = 0;
    }
   
   void outputXml(IvyXmlWriter xw) {
      xw.begin("INPUT");
      xw.field("FD",file_fd);
      xw.field("PATH",file_path);
      xw.field("POSITION",current_pos);
      xw.field("START",start_pos);
      xw.field("LENGTH",file_length);
      xw.end("INPUT");
    }
   
}


}	// end of class CashewInputOutputModel




/* end of CashewInputOutputModel.java */

