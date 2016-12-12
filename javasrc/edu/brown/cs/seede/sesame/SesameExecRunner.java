/********************************************************************************/
/*										*/
/*		SesameExecRunner.java						*/
/*										*/
/*	Threead to run current execution					*/
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



package edu.brown.cs.seede.sesame;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewOutputContext;
import edu.brown.cs.seede.cumin.CuminConstants;
import edu.brown.cs.seede.cumin.CuminRunError;
import edu.brown.cs.seede.cumin.CuminRunner;

class SesameExecRunner implements SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SesameMonitor	for_monitor;
private SesameSession   for_session;
private List<CuminRunner> cumin_runners;
private String		reply_id;
private Map<CuminRunner,RunnerThread> runner_threads;
private Map<CuminRunner,CuminRunError> run_status;
private MasterThread	master_thread;
private boolean 	is_continuous;
private boolean 	run_again;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameExecRunner(SesameSession ss,String rid,boolean contin,CuminRunner... rs)
{
   for_session = ss;
   for_monitor = ss.getControl().getMonitor();
   cumin_runners = new ArrayList<CuminRunner>();
   runner_threads = new HashMap<CuminRunner,RunnerThread>();
   run_status = new HashMap<CuminRunner,CuminRunError>();
   is_continuous = contin;
   
   for (CuminRunner r : rs) {
      cumin_runners.add(r);
    }
   master_thread = null;
   run_again = false;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

void addRunner(CuminRunner cr)
{
   stopCurrentRun();
   cumin_runners.add(cr);
   if (is_continuous) startRunner();
}


void removeRunner(CuminRunner cr)
{
   stopCurrentRun();
   cumin_runners.remove(cr);
   if (is_continuous) startRunner();
}



void startExecution()
{
   startRunner();
}


synchronized void stopExecution()
{
   stopRunner();
   while (master_thread != null) {
      try {
	 wait(1000);
       }
      catch (InterruptedException e) { }
    }
}



void restartExecution()
{
   stopCurrentRun();

   if (is_continuous) startRunner();
}


private synchronized void stopCurrentRun()
{
   if (master_thread == null) return;

   for (RunnerThread rt : runner_threads.values()) {
      rt.interrupt();
    }

   while (!runner_threads.isEmpty()) {
      try {
	 runner_threads.wait(1000);
       }
      catch (InterruptedException e) { }
    }
}


private synchronized void stopRunner()
{
   stopCurrentRun();
   if (master_thread != null) master_thread.interrupt();
}


private synchronized void startRunner()
{
   if (master_thread == null) {
      master_thread = new MasterThread();
      master_thread.start();
    }
   else {
      run_again = true;
      notifyAll();
    }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

private void report()
{
   boolean empty = false;
   for (CuminRunError sts : run_status.values()) {
      if (sts == null || sts.getReason() == CuminRunError.Reason.STOPPED) empty = true;
    }

   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SEEDE");
   xw.field("TYPE","EXEC");
   if (reply_id != null) xw.field("ID",reply_id);
   if (empty || run_status.isEmpty()) xw.field("EMPTY",true);
   else {
      for (Map.Entry<CuminRunner,CuminRunError> ent : run_status.entrySet()) {
	 CuminRunner cr = ent.getKey();
	 CuminRunError sts = ent.getValue();
	 outputResult(xw,cr,sts);
       }
    }
   xw.end("SEEDE");

   String rslt = xw.toString();
   xw.close();

   AcornLog.logD("Exec Result: " + rslt);
   if (reply_id != null) {
      MintDefaultReply hdlr = new MintDefaultReply();
      for_monitor.sendMessage(rslt,hdlr,MintConstants.MINT_MSG_FIRST_REPLY);
      hdlr.waitFor();
    }

   run_status.clear();
}



private void outputResult(IvyXmlWriter xw,CuminRunner cr,CuminRunError sts)
{
   CashewOutputContext outctx = new CashewOutputContext(xw);
   
   xw.begin("RUNNER");
   SesameLocation loc = for_session.getLocation(cr);
   xw.field("METHOD",loc.getMethodName());
   xw.field("LINE",loc.getLineNumber());
   xw.field("THREAD",loc.getThread());
   
   xw.begin("RETURN");
   xw.field("REASON",sts.getReason());
   xw.field("MESSAGE",sts.getMessage());
   if (sts.getCause() != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      sts.getCause().printStackTrace(pw);
      xw.textElement("STACK",sw.toString());
      pw.close();
    }
   if (sts.getValue() != null) {
      xw.begin("VALUE");
      sts.getValue().outputXml(outctx);
      xw.end("VALUE");
    }
   xw.end("RETURN");
   CashewContext ctx = cr.getLookupContext();
   ctx.outputXml(outctx);
   xw.end("RUNNER");
}




/********************************************************************************/
/*										*/
/*	Running methods 							*/
/*										*/
/********************************************************************************/

private synchronized void setStatus(CuminRunner cr,CuminRunError sts)
{
      run_status.put(cr,sts);
}


private class MasterThread extends Thread {

   MasterThread() {
      super("SeedeExec_" + reply_id);
    }

   @Override public void run()
   {
      for ( ; ; ) {
         // first start all the threads
         run_status.clear();
         List<RunnerThread> waits = new ArrayList<RunnerThread>();
         synchronized (this) {
            for (CuminRunner cr : cumin_runners) {
               RunnerThread rt = new RunnerThread(SesameExecRunner.this,cr);
               runner_threads.put(cr,rt);
               waits.add(rt);
               rt.start();
             }
          }
        
         // wait for all to exit
         while (!waits.isEmpty()) {
            for (Iterator<RunnerThread> it = waits.iterator(); it.hasNext(); ) {
               RunnerThread rt = it.next();
               try {
        	  rt.join();
        	  synchronized (this) {
        	     runner_threads.remove(rt.getRunner());
        	     if (runner_threads.isEmpty()) {
        		notifyAll();
        	      }
        	   }
        	  it.remove();
        	}
               catch (InterruptedException e) { }
             }
          }
         report();
         synchronized (this) {
            if (!is_continuous) break;
            while (!run_again) {
               if (isInterrupted()) break;
               try {
        	  wait(5000);
        	}
               catch (InterruptedException e) { }
             }
          }
        
         master_thread = null;
       }
    }

}	// end of inner class MasterRunner




/********************************************************************************/
/*										*/
/*	Thread to run a single runner						*/
/*										*/
/********************************************************************************/

private static class RunnerThread extends Thread {

   private SesameExecRunner exec_runner;
   private CuminRunner cumin_runner;

   RunnerThread(SesameExecRunner er,CuminRunner cr) {
      super("Runner_" + er.reply_id + "_" + cr);
      exec_runner = er;
      cumin_runner = cr;
    }

   CuminRunner getRunner()			{ return cumin_runner; }

   @Override public void run() {
      CuminRunError sts = null;
      try {
         cumin_runner.interpret(CuminConstants.EvalType.RUN);
       }
      catch (CuminRunError r) {
         sts = r;
       }
      catch (Throwable t) {
         sts = new CuminRunError(t);
       }
   
      exec_runner.setStatus(cumin_runner,sts);
    }

}	// end of inner class RunnerThread




}	// end of class SesameExecRunner




/* end of SesameExecRunner.java */

