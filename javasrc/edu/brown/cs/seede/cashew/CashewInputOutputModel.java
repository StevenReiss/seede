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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class CashewInputOutputModel implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<Integer,OutputData> 	output_files;
private Map<Integer,InputData>		input_files;
private Set<File>			files_created;
private Set<File>			files_removed;
private Map<File,Integer>		file_permissions;


private int	READ_PERM = 1;
private int	WRITE_PERM = 2;
private int	EXEC_PERM = 4;

private int	DIRECTORY_PERM = 32;




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
/*	Setup methods								*/
/*										*/
/********************************************************************************/

synchronized public void clear()
{
   output_files.clear();
}


synchronized public void reset()
{
   output_files.clear();

   for (InputData id : input_files.values()) {
      id.reset();
    }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

synchronized public void fileWrite(CashewClock clk,int fd,String path,byte [] buf,int off,int len,boolean app)
{
   if (fd < 0) return;
   OutputData file = output_files.get(fd);
   if (file == null) {
      file = new OutputData(fd,path);
      output_files.put(fd,file);
    }
   
   // if (len > 4096) len = 4096;

   file.addBytes(clk.tick(),buf,off,len);
}




/********************************************************************************/
/*										*/
/*	Input methods								*/
/*										*/
/********************************************************************************/

synchronized public long fileRead(CashewClock clk,int fd,byte [] buf,int off,long len)
	throws IOException
{
   InputData id = input_files.get(fd);
   if (id == null) throw new IOException("No such file");

   if (buf == null) {
      return id.skip(len);
    }

   return id.read(buf,off,(int) len);
}



synchronized public long fileAvailable(CashewClock clk,int fd)
	throws IOException
{
   InputData id = input_files.get(fd);
   if (id == null) throw new IOException("No such file");

   return id.available();
}



synchronized public void checkInputFile(CashewValueSession sess,JcompTyper typer,CashewContext ctx,CashewClock cc,CashewValue cv,int fd,String path,
      boolean prior) throws IOException
{
   InputData id = input_files.get(fd);

   if (id == null && path.equals("*STDIN*")) {
      id = new StandardInput();
      input_files.put(fd,id);
    }

   if (id == null) {
      long pos = 0;

      if (prior) {
	 String name = ctx .findNameForValue(cv);
	 if (name != null) {
	    String expr = "edu.brown.cs.seede.poppy.PoppyValue.getFileData(" + name  + ")";
	    CashewValue rv = ctx.evaluate(expr);
	    if (rv != null && rv.getDataType(null).isStringType()) {
               try {
                  String finfo = rv.getString(sess,typer,cc);
                  if (finfo.equals("*")) {
                     throw new IOException("File not open");
                   }
                  int idx = finfo.indexOf("@");
                  pos = Long.parseLong(finfo.substring(idx+1));
                }
               catch (CashewException e) {
                  throw new IOException("Bad file");
                }
	     }
	  }
       }
      id = new InputData(path,pos);
      input_files.put(fd,id);
    }
   id.ensureOpen(ctx);
}



synchronized public void closeFile(int fd) throws IOException
{
   InputData id = input_files.get(fd);
   if (id != null) id.reset();
}




/********************************************************************************/
/*										*/
/*	Model output methods							*/
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
/*	File methods								*/
/*										*/
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
/*										*/
/*	Input file modeling							*/
/*										*/
/********************************************************************************/

private static class InputData {

   private String file_path;
   private long start_pos;
   private FileInputStream input_stream;

   InputData(String path,long pos) {
      file_path = path;
      start_pos = pos;
      input_stream = null;
    }

   void ensureOpen(CashewContext ctx) throws IOException {
      if (input_stream != null) return;
      input_stream = new FileInputStream(file_path);
      if (start_pos != 0) input_stream.skip(start_pos);
    }

   void reset() {
      try {
	 if (input_stream != null) {
	    input_stream.close();
	  }
       }
      catch (IOException e) { }
      input_stream = null;
    }

   long skip(long len) throws IOException {
      return input_stream.skip(len);
    }

   int read(byte [] buf,int off,int len) throws IOException {
      return input_stream.read(buf,off,len);
    }

   long available() throws IOException {
      return input_stream.available();
    }

   String getFileName() 		{ return file_path; }

}	// end of inner class InputData




/********************************************************************************/
/*										*/
/*	Handler for standard input						*/
/*										*/
/********************************************************************************/

private static class StandardInput extends InputData {

   private List<byte []> input_strings;
   private int		string_index;
   private int		string_offset;
   private CashewContext using_context;

   StandardInput() {
      super("*STDIN*",0);
      input_strings = new ArrayList<>();
      string_index = 0;
      string_offset = 0;
      using_context = null;
    }

   @Override void ensureOpen(CashewContext ctx) {
      if (using_context == null) using_context = ctx;
    }

   @Override void reset() {
      string_index = 0;
      string_offset = 0;
      using_context = null;
    }

   @Override long skip(long len) {
      return 0L;
    }

   @Override int read(byte [] buf,int off,int len) throws IOException {
      byte [] cur;
      for ( ; ; ) {
	 if (string_index == input_strings.size()) {
	    String next = using_context.getNextInputLine(getFileName());
	    if (next == null) return 0;
	    input_strings.add(next.getBytes());
	  }
	 else if (string_index > input_strings.size()) return 0;
	 cur = input_strings.get(string_index);
	 if (cur.length == 0) return 0; 		// handle EOF
	 if (string_offset >= cur.length) {
	    ++string_index;
	    string_offset = 0;
	  }
	 else break;
       }
      int rlen = Math.min(len,buf.length-string_offset);
      System.arraycopy(cur,string_offset,buf,off,rlen);
      string_offset += rlen;
      if (string_offset >= cur.length) {
	 ++string_index;
	 string_offset = 0;
       }
      return rlen;
    }

   @Override long available() throws IOException {
      if (string_index < input_strings.size()) {
	 byte [] cur = input_strings.get(string_index);
	 return cur.length - string_offset;
       }
      return 0;
    }

}	// end of inner class StandardInput


}	// end of class CashewInputOutputModel




/* end of CashewInputOutputModel.java */

