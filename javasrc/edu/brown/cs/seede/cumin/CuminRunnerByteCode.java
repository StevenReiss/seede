/********************************************************************************/
/*										*/
/*		CuminRunnerByteCode.java					*/
/*										*/
/*	Java byte-code interpreter						*/
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

import edu.brown.cs.ivy.jcode.JcodeDataType;
import edu.brown.cs.ivy.jcode.JcodeField;
import edu.brown.cs.ivy.jcode.JcodeInstruction;
import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcode.JcodeTryCatchBlock;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewSynchronizationModel;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueFunctionRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


class CuminRunnerByteCode extends CuminRunner implements CuminConstants,
	Opcodes, CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcodeMethod	jcode_method;
private int		current_instruction;
private int		last_line;
private int		num_arg;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminRunnerByteCode(CashewValueSession sess,CuminProject sp,
      CashewContext gblctx,CashewClock clock,
      JcodeMethod mthd,List<CashewValue> args,int depth)
{
   super(sess,sp,gblctx,clock,args,depth);

   jcode_method = mthd;
   current_instruction = 0;
   last_line = 0;
   num_arg = args.size();

   setupContext(args);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

JcodeMethod getCodeMethod()		{ return jcode_method; }

@Override String getMethodName()	{ return getCodeMethod().getFullName(); }
int getNumArg() 			{ return num_arg; }
String getCallingClass()
{
   return jcode_method.getDeclaringClass().getName();
}


@Override protected CashewValue synchronizeOn()
{
   if (!jcode_method.isSynchronized()) return null;
   if (!jcode_method.isStatic()) {
      return lookup_context.findReference(0);
    }
   else {
      JcompType typ = convertType(jcode_method.getDeclaringClass());
      if (typ == null) return null;
      return CashewValue.classValue(getTyper(),typ);
    }
}




/********************************************************************************/
/*										*/
/*     Entry methods								*/
/*										*/
/********************************************************************************/

@Override public void reset()
{
   super.reset();
   current_instruction = 0;
   last_line = 0;
}


@Override protected CuminRunStatus interpretRun(CuminRunStatus r)
{
   if (r == null) {
      CuminRunStatus sts = null;
      try {
	 sts = checkSpecial();
       }
      catch (CuminRunException e) {
	 sts = e;
       }
      if (sts != null && sts.getReason() == Reason.CALL) current_instruction = -1;
      if (sts != null) return sts;
      current_instruction = 0;
      lookup_context.enableAccess(jcode_method.getDeclaringClass().getName());
    }
   else if (r.getReason() == Reason.RETURN) {
      if (current_instruction < 0) {
	 CuminRunStatus sts = checkSpecialReturn(r);
	 if (sts != null) return sts;
	 else return r;
       }
      current_instruction = current_instruction+1;
      CashewValue rv = r.getValue();
      if (rv != null) execution_stack.push(rv);
    }
   else if (r.getReason() == Reason.EXCEPTION) {
      CuminRunStatus sts = handleException(r);
      if (sts != null) return sts;
    }
   else if (r.getReason() == Reason.TIMEOUT) {
      return r;
    }

   try {
      while (current_instruction >= 0) {
	 CuminRunStatus sts = null;
	 try {
	    sts = evaluateInstruction();
	  }
	 catch (CuminRunException cr) {
	    sts = cr;
	  }
	 catch (CashewException e) {
	    AcornLog.logD("Bad value access: " + e);
	    sts = CuminRunStatus.Factory.createCompilerError();
	  }
	 if (sts != null) {
	    if (sts.getReason() == Reason.EXCEPTION) {
	       sts = handleException(sts);
	       if (sts != null) return sts;
	     }
	    else return sts;
	  }
       }
    }
   catch (Throwable t) {
       if (t instanceof CuminRunException) return (CuminRunException) t;
       CuminRunException re = CuminRunStatus.Factory.createError(t);
       return re;
    }

   return null;
}



private CuminRunStatus handleException(CuminRunStatus cr)
{
   AcornLog.logD("CUMIN","Handle exception " + cr.getMessage() + " " + cr.getValue() + " " +
         jcode_method.getFullName() + " " + current_instruction);

   if (cr.getMessage() != null && cr.getMessage().equals("SEEDE_TIMEOUT")) return cr;

   CashewValue ev = cr.getValue();
   JcompType etyp = ev.getDataType(runner_session,execution_clock,type_converter);
   JcodeTryCatchBlock tcb = null;
   int len = 0;
   for (JcodeTryCatchBlock jtcb : jcode_method.getTryCatchBlocks()) {
      JcodeDataType jdt = jtcb.getException();
      JcompType cdt = null;
      if (jdt != null) cdt = convertType(jdt);
      AcornLog.logD("CUMIN","Check exception " + cdt + " " + etyp + " " + 
            jtcb.getStart().getIndex() + " " + jtcb.getEnd().getIndex());
      if (cdt == null || etyp.isCompatibleWith(cdt)) {
	 int sidx = jtcb.getStart().getIndex();
	 int eidx = jtcb.getEnd().getIndex();
	 if (current_instruction >= sidx &&  current_instruction <= eidx) {
	    if (tcb != null && len <= eidx - sidx) continue;
	    tcb = jtcb;
	    len = eidx - sidx;
	  }
       }
    }
   if (tcb == null) return cr;
   while (execution_stack.size() > 0) {
      execution_stack.pop();
    }
   execution_stack.push(ev);
   current_instruction = tcb.getHandler().getIndex();

   return null;
}




/********************************************************************************/
/*										*/
/*	Context setup								*/
/*										*/
/********************************************************************************/

private void setupContext(List<CashewValue> args)
{
   CashewContext ctx = new CashewContext(jcode_method,global_context);
// CashewContext ctx = new CashewContext(jcode_method,lookup_context); ???
   JcompTyper typer = getTyper();

   int nlcl = jcode_method.getLocalSize();
   int vct = 0;
   int act = -1;
   if (jcode_method.isStatic()) act = 0;
   for (CashewValue cv : args) {
      // AcornLog.logD("ARG " + vct + " " + cv);
      CashewValue ref = CashewValue.createReference(cv,false);
      ctx.define(Integer.valueOf(vct),ref);
      ++vct;
      boolean cat2 = false;
      if (act < 0) ++act;
      else {
	 JcodeDataType jdt = jcode_method.getArgType(act++);
	 if (jdt != null) cat2 = jdt.isCategory2();
       }

      if (cv != null  && cat2)	{ // && cv.isCategory2(execution_clock))
	 ref = CashewValue.createReference(CashewValue.nullValue(typer),false);
	 ctx.define(Integer.valueOf(vct),ref);
	 ++vct;
       }
    }
   while (vct < nlcl) {
      CashewValue ref = CashewValue.createReference(CashewValue.nullValue(typer),false);
      ctx.define(Integer.valueOf(vct),ref);
      ++vct;
    }

   int lno = 0;
   JcodeInstruction jins = jcode_method.getInstruction(0);
   if (jins != null) lno = jins.getLineNumber();
   if (lno < 0) lno = 0;
   CashewValue zv = CashewValue.numericValue(typer,typer.INT_TYPE,lno);
   ctx.define(LINE_NAME,CashewValue.createReference(zv,false));

   setLookupContext(ctx);
}



 /********************************************************************************/
/*										*/
/*	Main evaluation routine 						*/
/*										*/
/********************************************************************************/

//CHECKSTYLE:OFF
private CuminRunStatus evaluateInstruction() throws CuminRunException, CashewException
//CHECKSTYLE:ON
{
   CashewValue vstack = null;
   JcodeInstruction nextins = null;
   CashewValue v0;
   CashewValue v1;
   CashewValue v2;
   CashewValue v3;
   int next = current_instruction+1;
   int idxv;

   JcodeInstruction jins = jcode_method.getInstruction(current_instruction);
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   if (jins == null)
      return CuminRunStatus.Factory.createError("Native method " + jcode_method);
   int lno = jins.getLineNumber();
   if (lno > 0 && lno != last_line) {
      CuminRunStatus tsts = checkTimeout();
      if (tsts != null) return tsts;
      last_line = lno;
      CashewValue lvl = CashewValue.numericValue(typer,typer.INT_TYPE,lno);
      lookup_context.findReference(LINE_NAME).setValueAt(sess,execution_clock,lvl);
      if (Thread.currentThread().isInterrupted()) {
	 return CuminRunStatus.Factory.createStopped();
       }
    }

   if (AcornLog.isTracing()) AcornLog.logT(jins + " @ " + execution_clock.getTimeValue());

   switch (jins.getOpcode()) {

// Arithmetic operators
      case DADD :
      case FADD :
      case IADD :
      case LADD :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.ADD,v0,v1);
	 break;
      case DSUB :
      case FSUB :
      case ISUB :
      case LSUB :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.SUB,v0,v1);
	 break;
      case DMUL :
      case FMUL :
      case IMUL :
      case LMUL :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.MUL,v0,v1);
	 break;
      case DDIV :
      case FDIV :
      case IDIV :
      case LDIV :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.DIV,v0,v1);
	 break;
      case DREM :
      case FREM :
      case IREM :
      case LREM :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.MOD,v0,v1);
	 break;
      case IAND :
      case LAND :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.AND,v0,v1);
	 break;
      case IOR :
      case LOR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.OR,v0,v1);
	 break;
      case IXOR :
      case LXOR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.XOR,v0,v1);
	 break;
      case ISHL :
      case LSHL :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.LSH,v0,v1);
	 break;
      case ISHR :
      case LSHR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.RSH,v0,v1);
	 break;
      case IUSHR :
      case LUSHR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.RSHU,v0,v1);
	 break;
      case DCMPG :
      case DCMPL :
      case FCMPG :
      case FCMPL :
      case LCMP :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.SIG,v0,v1);
	 break;
      case DNEG :
      case FNEG :
      case INEG :
      case LNEG :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(sess,lookup_context,
               typer,execution_clock,CuminOperator.NEG,v0);
	 break;


// Constant operators
      case ACONST_NULL :
	 vstack = CashewValue.nullValue(typer);
	 break;
      case DCONST_0 :
	 vstack = CashewValue.numericValue(typer.DOUBLE_TYPE,0.0);
	 break;
      case DCONST_1 :
	 vstack = CashewValue.numericValue(typer.DOUBLE_TYPE,1.0);
	 break;
      case FCONST_0 :
	 vstack = CashewValue.numericValue(typer.FLOAT_TYPE,0.0f);
	 break;
      case FCONST_1 :
	 vstack = CashewValue.numericValue(typer.FLOAT_TYPE,1.0f);
	 break;
      case FCONST_2 :
	 vstack = CashewValue.numericValue(typer.FLOAT_TYPE,2.0f);
	 break;
      case ICONST_0 :
      case ICONST_1 :
      case ICONST_2 :
      case ICONST_3 :
      case ICONST_4 :
      case ICONST_5 :
      case ICONST_M1 :
	 vstack = CashewValue.numericValue(typer,typer.INT_TYPE,jins.getIntValue());
	 break;
      case LCONST_0 :
      case LCONST_1 :
	 vstack = CashewValue.numericValue(typer,typer.LONG_TYPE,jins.getIntValue());
	 break;
      case LDC :
	 Object o = jins.getObjectValue();
	 if (o instanceof String) {
	    vstack = CashewValue.stringValue(typer,typer.STRING_TYPE,(String) o);
	  }
	 else if (o instanceof Integer) {
	    vstack = CashewValue.numericValue(typer,typer.INT_TYPE,((Number) o).intValue());
	  }
	 else if (o instanceof Long) {
	    vstack = CashewValue.numericValue(typer,typer.LONG_TYPE,((Number) o).longValue());
	  }
	 else if (o instanceof Float) {
	    vstack = CashewValue.numericValue(typer.FLOAT_TYPE,((Number) o).floatValue());
	  }
	 else if (o instanceof Double) {
	    vstack = CashewValue.numericValue(typer.DOUBLE_TYPE,((Number) o).doubleValue());
	  }
	 else if (o instanceof Type) {
	    Type t = (Type) o;
	    JcompType jtyp = type_converter.findSystemType(t.getClassName());
	    vstack = CashewValue.classValue(typer,jtyp);
	  }
	 break;
      case BIPUSH :
      case SIPUSH :
	 vstack = CashewValue.numericValue(typer,typer.INT_TYPE,jins.getIntValue());
	 break;

// CONVERSION OPERATORS
      case I2B :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(this,v0,typer.BYTE_TYPE);
	 break;
      case I2C :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(this,v0,typer.CHAR_TYPE);
	 break;
      case F2D :
      case I2D :
      case L2D :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(this,v0,typer.DOUBLE_TYPE);
	 break;
      case D2F :
      case I2F :
      case L2F :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(this,v0,typer.FLOAT_TYPE);
	 break;
      case D2L :
      case F2L :
      case I2L :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(this,v0,typer.LONG_TYPE);
	 break;
      case I2S :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(this,v0,typer.SHORT_TYPE);
	 break;
      case D2I :
      case F2I :
      case L2I :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(this,v0,typer.INT_TYPE);
	 break;

// Stack manipulation
      case DUP :
	 vstack = execution_stack.peek(0);
	 break;
      case DUP_X1 :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 execution_stack.push(v0);
	 execution_stack.push(v1);
	 execution_stack.push(v0);
	 break;
      case DUP_X2 :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 if (v1.isCategory2(sess,execution_clock)) {
	    execution_stack.push(v0);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 else {
	    v2 = execution_stack.pop();
	    execution_stack.push(v0);
	    execution_stack.push(v2);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 break;
      case DUP2 :
	 v0 = execution_stack.pop();
	 if (v0.isCategory2(sess,execution_clock)) {
	    execution_stack.push(v0);
	    execution_stack.push(v0);
	  }
	 else {
	    v1 = execution_stack.pop();
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 break;
      case DUP2_X1 :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 if (v0.isCategory2(sess,execution_clock)) {
	    execution_stack.push(v0);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 else {
	    v2 = execution_stack.pop();
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	    execution_stack.push(v2);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 break;
      case DUP2_X2 :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 if (v0.isCategory2(sess,execution_clock) && v1.isCategory2(sess,execution_clock)) {
	    execution_stack.push(v0);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 else if (v0.isCategory2(sess,execution_clock)) {
	    v2 = execution_stack.pop();
	    execution_stack.push(v0);
	    execution_stack.push(v2);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 else {
	    v2 = execution_stack.pop();
	    if (v2.isCategory2(sess,execution_clock)) {
	       execution_stack.push(v1);
	       execution_stack.push(v0);
	       execution_stack.push(v2);
	       execution_stack.push(v1);
	       execution_stack.push(v0);
	     }
	    else {
	       v3 = execution_stack.pop();
	       execution_stack.push(v1);
	       execution_stack.push(v0);
	       execution_stack.push(v3);
	       execution_stack.push(v2);
	       execution_stack.push(v1);
	       execution_stack.push(v0);
	     }
	  }
	 break;
      case POP :
	 execution_stack.pop();
	 break;
      case POP2 :
	 v0 = execution_stack.pop();
	 if (!v0.isCategory2(sess,execution_clock)) execution_stack.pop();
	 break;
      case NOP :
	 break;
      case SWAP :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 execution_stack.push(v0);
	 execution_stack.push(v1);
	 break;

// BRANCHING OPERATIONS
      case GOTO :
	 nextins = jins.getTargetInstruction();
	 break;
      case IF_ACMPEQ :
      case IF_ICMPEQ :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.EQL,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ACMPNE :
      case IF_ICMPNE :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.NEQ,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPGE :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.GEQ,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPGT :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.GTR,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPLE :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.LEQ,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPLT :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.LSS,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFEQ :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(sess,execution_clock,type_converter),0);
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.EQL,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFGE :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(sess,execution_clock,type_converter),0);
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.GEQ,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFGT :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(sess,execution_clock,type_converter),0);
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.GTR,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFLE :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(sess,execution_clock,type_converter),0);
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.LEQ,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFLT :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(sess,execution_clock,type_converter),0);
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.LSS,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFNE :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(sess,execution_clock,type_converter),0);
	 v2 = CuminEvaluator.evaluate(this,typer,execution_clock,CuminOperator.NEQ,v0,v1);
	 if (v2.getBoolean(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFNONNULL :
	 v0 = execution_stack.pop();
	 if (!v0.isNull(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFNULL :
	 v0 = execution_stack.pop();
	 if (v0.isNull(sess,execution_clock)) nextins = jins.getTargetInstruction();
	 break;

      case AALOAD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 idxv = v0.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v1.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 vstack = v1.getIndexValue(sess,execution_clock,idxv);
	 break;
      case BALOAD :
      case CALOAD :
      case SALOAD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 idxv = v0.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v1.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 vstack = v1.getIndexValue(sess,execution_clock,idxv);
	 vstack = CuminEvaluator.castValue(this,vstack,typer.INT_TYPE);
	 break;
      case DALOAD :
      case LALOAD :
      case IALOAD :
      case FALOAD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 idxv = v0.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v1.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 vstack = v1.getIndexValue(sess,execution_clock,idxv);
	 break;


      case AASTORE :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;
      case BASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(this,v0,typer.BYTE_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;
      case CASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(this,v0,typer.CHAR_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;
      case DASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(this,v0,typer.DOUBLE_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;
      case FASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(this,v0,typer.FLOAT_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;
      case IASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(this,v0,typer.INT_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;
      case LASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(this,v0,typer.LONG_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;
      case SASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(this,v0,typer.SHORT_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(sess,execution_clock).intValue();
	 if (idxv < 0 || idxv >= v2.getDimension(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ArrayIndexOutOfBoundsException");
	 v2.setIndexValue(sess,execution_clock,idxv,v0);
	 break;

      case ALOAD :
	 Integer vidx = jins.getLocalVariable();
	 vstack = lookup_context.findReference(vidx).getActualValue(sess,execution_clock);
	 break;
      case DLOAD :
	 vidx = jins.getLocalVariable();
	 vstack = lookup_context.findReference(vidx).getActualValue(sess,execution_clock);
	 vstack = CuminEvaluator.castValue(this,vstack,typer.DOUBLE_TYPE);
	 break;
      case FLOAD :
	 vidx = jins.getLocalVariable();
	 vstack = lookup_context.findReference(vidx).getActualValue(sess,execution_clock);
	 vstack = CuminEvaluator.castValue(this,vstack,typer.FLOAT_TYPE);
	 break;
      case ILOAD :
	 vidx = jins.getLocalVariable();
	 vstack = lookup_context.findReference(vidx).getActualValue(sess,execution_clock);
	 vstack = CuminEvaluator.castValue(this,vstack,typer.INT_TYPE);
	 break;
      case LLOAD :
	 vidx = jins.getLocalVariable();
	 vstack = lookup_context.findReference(vidx).getActualValue(sess,execution_clock);
	 vstack = CuminEvaluator.castValue(this,vstack,typer.LONG_TYPE);
	 break;

      case ASTORE :
	 vidx = jins.getLocalVariable();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(sess,execution_clock);
	 v0.setValueAt(sess,execution_clock,v1);
	 break;
      case DSTORE :
	 vidx = jins.getLocalVariable();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(sess,execution_clock);
	 v1 = CuminEvaluator.castValue(this,v1,typer.DOUBLE_TYPE);
	 v0.setValueAt(sess,execution_clock,v1);
	 break;
      case FSTORE :
	 vidx = jins.getLocalVariable();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(sess,execution_clock);
	 v1 = CuminEvaluator.castValue(this,v1,typer.FLOAT_TYPE);
	 v0.setValueAt(sess,execution_clock,v1);
	 break;
      case LSTORE :
	 vidx = jins.getLocalVariable();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(sess,execution_clock);
	 v1 = CuminEvaluator.castValue(this,v1,typer.LONG_TYPE);
	 v0.setValueAt(sess,execution_clock,v1);
	 break;
      case ISTORE :
	 vidx = jins.getLocalVariable();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(sess,execution_clock);
	 v1 = CuminEvaluator.castValue(this,v1,typer.INT_TYPE);
	 v0.setValueAt(sess,execution_clock,v1);
	 break;

      case ARETURN :
      case DRETURN :
      case FRETURN :
      case IRETURN :
      case LRETURN :
	 v0 = execution_stack.pop().getActualValue(sess,execution_clock);
	 execution_clock.tick();
	 return CuminRunStatus.Factory.createReturn(v0);
      case RETURN :
	 execution_clock.tick();
	 return CuminRunStatus.Factory.createReturn();
      case ATHROW :
	 execution_clock.tick();
	 v0 = execution_stack.pop().getActualValue(sess,execution_clock);
	 return CuminRunStatus.Factory.createException(sess,v0);

      case ARRAYLENGTH :
	 v0 = execution_stack.pop();
	 vstack = CashewValue.numericValue(typer,typer.INT_TYPE,v0.getDimension(sess,execution_clock));
	 break;
      case IINC :
	 vidx = jins.getLocalVariable();
	 v0 = lookup_context.findReference(vidx);
	 v1 = CashewValue.numericValue(typer,typer.INT_TYPE,v0.getNumber(sess,execution_clock).intValue() +
	       jins.getIntValue());
	 v0.setValueAt(sess,execution_clock,v1);
	 break;

      case INSTANCEOF :
	 JcompType jty = convertType(jins.getTypeReference());
	 v0 = execution_stack.pop();
	 if (v0.isNull(sess,execution_clock)) {
	    vstack = CashewValue.booleanValue(typer,false);
	  }
	 else if (v0.getDataType(sess,execution_clock,type_converter).isCompatibleWith(jty)) {
	    vstack = CashewValue.booleanValue(typer,true);
	  }
	 else {
	    vstack = CashewValue.booleanValue(typer,false);
	  }
	 break;
      case CHECKCAST :
	 v0 = execution_stack.peek(0);
	 jty = convertType(jins.getTypeReference());
	 if (!v0.getDataType(sess,execution_clock,type_converter).isCompatibleWith(jty)) {
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.ClassCastException");
	  }
	 break;

      case GETFIELD :
	 v0 = execution_stack.pop();
	 JcodeField fld = jins.getFieldReference();
	 String nm = fld.getName();
	 if (!nm.contains(".")) {
	    nm = fld.getDeclaringClass().getName() + "." + fld.getName();
	  }
	 if (v0.isNull(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.NullPointerException");
         AcornLog.logD("CUMIN","Field lookup for " + v0.toString(runner_session) +
               " " + execution_clock.getTimeValue());
	 vstack = v0.getFieldValue(sess,typer,execution_clock,nm,lookup_context);
         AcornLog.logD("CUMIN","RESULT: " + nm + " = " +  vstack.toString(runner_session));
	 vstack = vstack.getActualValue(sess,execution_clock);
         
         AcornLog.logD("CUMIN","ACTUAL RESULT: " + (vstack == null ? null : 
            IvyXml.xmlSanitize(vstack.toString(runner_session))));
	 break;
      case GETSTATIC :
	 fld = jins.getFieldReference();
	 JcompType fldtyp = convertType(fld.getDeclaringClass());
	 fldtyp.defineAll(type_converter);
	 lookup_context.enableAccess(fldtyp.getName());
	 vstack = lookup_context.findReference(type_converter,fld);
	 if (vstack == null) vstack = CashewValue.nullValue(typer);
	 else vstack = vstack.getActualValue(sess,execution_clock);
	 break;
      case PUTFIELD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 fld = jins.getFieldReference();
	 String dcname = fld.getDeclaringClass().getName();
	 nm = dcname + "." + fld.getName();
	 if (v1.isNull(sess,execution_clock))
	    return CuminEvaluator.returnException(sess,lookup_context,
                  typer,"java.lang.NullPointerException");
	 v1.setFieldValue(sess,typer,execution_clock,nm,v0);
	 if (AcornLog.isTracing()) AcornLog.logT("RESULT IS " + v0.toString(runner_session));
	 break;
      case PUTSTATIC :
	 v0 = execution_stack.pop();
	 v0 = v0.getActualValue(sess,execution_clock);
	 fld = jins.getFieldReference();
	 v1 = lookup_context.findReference(type_converter,fld);
	 if (v1 == null) {
	    AcornLog.logE("Cannot find field " + fld);
	  }
	 else {
	    v1.setValueAt(sess,execution_clock,v0);
	  }
	 break;

      case NEW :
	 JcompType nty = convertType(jins.getTypeReference());
	 vstack = handleNew(nty);
	 break;

      case INVOKEDYNAMIC :
	 handleDynamicCall(jins);
	 break;

      case INVOKEINTERFACE :
	 return handleCall(jins.getMethodReference(),CallType.INTERFACE,-1);
      case INVOKESPECIAL :
	 return handleCall(jins.getMethodReference(),CallType.SPECIAL,-1);
      case INVOKESTATIC :
	 return handleCall(jins.getMethodReference(),CallType.STATIC,-1);
      case INVOKEVIRTUAL :
	 int act0 = jins.getDescriptionArgCount();
	 return handleCall(jins.getMethodReference(),CallType.VIRTUAL,act0);

      case JSR :
	 execution_stack.pushMarker(jins,next);
	 nextins = jins.getTargetInstruction();
	 break;
      case RET :
	 next = (Integer) execution_stack.popMarker(jins);
	 break;

      case LOOKUPSWITCH :
      case TABLESWITCH :
	 v0 = execution_stack.pop();
	 nextins = jins.getTargetInstruction(v0.getNumber(sess,execution_clock).intValue());
	 break;

      case NEWARRAY :
      case ANEWARRAY :
	 JcodeDataType dty = jins.getTypeReference();
	 JcompType arrtyp = convertType(dty);
	 arrtyp = type_converter.findArrayType(arrtyp);
	 int dim = execution_stack.pop().getNumber(sess,execution_clock).intValue();
	 vstack = CashewValue.arrayValue(typer,arrtyp,dim);
	 break;

      case MULTIANEWARRAY :
	 dty = jins.getTypeReference();
	 arrtyp = convertType(dty);
	 int mnact = jins.getIntValue();
	 int [] bnds = new int[mnact];
	 for (int i = mnact-1; i >= 0; --i) {
	    bnds[i] = execution_stack.pop().getNumber(sess,execution_clock).intValue();
	    arrtyp = arrtyp.getBaseType();
	  }
	 vstack = CuminEvaluator.buildArray(this,0,bnds,arrtyp);
	 break;

      case MONITORENTER :
	 v0 = execution_stack.pop();
	 CashewSynchronizationModel csm = lookup_context.getSynchronizationModel();
	 if (csm != null) csm.synchEnter(getCurrentThread(),v0);
	 break;
      case MONITOREXIT :
	 v0 = execution_stack.pop();
	 csm = lookup_context.getSynchronizationModel();
	 if (csm != null) csm.synchExit(getCurrentThread(),v0);
	 break;

      default :
	 AcornLog.logE("Unknown instruction: " + jins);
	 throw CuminRunStatus.Factory.createError("Unknown instruction");
    }

   if (vstack != null) {
      if (AcornLog.isTracing()) AcornLog.logT("RESULT: " +
	    vstack.getString(sess,typer,execution_clock,0,true));
      execution_stack.push(vstack);
    }
   if (nextins != null) next = nextins.getIndex();
   current_instruction = next;

   return null;
}




/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

private CuminRunStatus handleCall(JcodeMethod method,CallType cty,int act0)
	throws CuminRunException
{
  int act = method.getNumArguments();
  if (act0 >= 0) {
     if (act != act0) {
	AcornLog.logD("CUMIN","Argument counts differ " + act + " " + act0 + " " +
	      method.isVarArgs());
	act = act0;
      }
   }

  if (!method.isStatic()) ++act;
  List<CashewValue> args = new ArrayList<CashewValue>();
  for (int i = 0; i < act; ++i) {
     CashewValue cv = execution_stack.pop().getActualValue(runner_session,execution_clock);
     args.add(cv);
   }
  Collections.reverse(args);

  CuminRunner cr = handleCall(execution_clock,method,args,cty);

  return CuminRunStatus.Factory.createCall(cr);
}



private void handleDynamicCall(JcodeInstruction ins)
{
   CashewValue cv = null;
   String [] args = ins.getDynamicReference();
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   if (args[2].startsWith("java/lang/invoke/LambdaMetafactory.metafactory")) {
      JcompType t1 = buildMethodType1(args[5]);
      Map<Object,CashewValue> bind = null;
      int act = 0;
      if (!args[1].startsWith("()")) {
	  if (bind == null) bind = new HashMap<>();
	  Type t0 = Type.getType(args[1]);
	  Type [] argtyps = t0.getArgumentTypes();
	  for (int i = 0; i < argtyps.length; ++i) {
	     CashewValue v0 = execution_stack.pop();
	     bind.put(act++,v0);
	   }
       }
      JcompType rt1 = JcompType.createFunctionRefType(t1,null,null);

      cv = new CashewValueFunctionRef(typer,rt1,args[4],bind);
    }
   else if (args[2].startsWith("java/lang/invoke/StringConcatFactory.makeConcatWithConstants")) {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < args[3].length(); ++i) {
	 char c = args[3].charAt(i);
	 if (c == 1) {
	    CashewValue v0 = execution_stack.pop();
	    if (v0.isNull(sess,getClock())) buf.append("null");
	    try {
	       String v1 = CuminEvaluator.getStringValue(sess,v0,typer,getClock(),lookup_context);
	       buf.append(v1);
	     }
	    catch (CashewException e) {
	       buf.append("???");
	     }
	  }
	 else buf.append(c);
       }
      cv = CashewValue.stringValue(typer,typer.STRING_TYPE,buf.toString());
    }
   else if (args[2].startsWith("java/lang/invoke/StringConcatFactory.makeConcat")) {
      StringBuffer buf = new StringBuffer();
      Type t0 = Type.getType(args[1]);
      Type [] argtyps = t0.getArgumentTypes();
      for (int i = 0; i < argtyps.length; ++i) {
	 CashewValue v0 = execution_stack.pop();
	 try {
	    String v1 = CuminEvaluator.getStringValue(sess,v0,typer,getClock(),lookup_context);
	    buf.append(v1);
	  }
	 catch (CashewException e) {
	    buf.append("???");
	  }
       }
      cv = CashewValue.stringValue(typer,typer.STRING_TYPE,buf.toString());
    }
   else {
      AcornLog.logE("Unknown dynamic call user: " + args[2]);
      // need to do something here, setting cv
    }

   try {
      if (AcornLog.isTracing()) {
	 AcornLog.logT("RESULT: " + cv.getString(sess,typer,execution_clock,0,true));
       }
    }
   catch (CashewException e) { }

   execution_stack.push(cv);

   /***** this doesn't work
   if (args[2].startsWith("java/lang/invoke/LambdaMetafactory.metafactory")) {
      JcodeMethod m1 = ins.getMethodReference();
      JcompType typ = typer.findSystemType("java.lang.invoke.MethodHandles.Lookup");
      CashewValue lookup = handleNew(typ);
      String initer = "java/lang/invoke/MethodHandles$Lookup.<init>(Ljava/lang/Class;I)V";
      String clsnm = getCodeMethod().getDeclaringClass().getName();
      lookup_context.enableAccess(clsnm);
      JcompType t1 = typer.findSystemType(clsnm);
      CashewValue cv = CashewValue.classValue(t1);
      int mod = Modifier.PUBLIC|Modifier.PRIVATE|Modifier.PROTECTED|Modifier.STATIC;
      CashewValue cv1 = CashewValue.numericValue(INT_TYPE,mod);
      executeCall(initer,lookup,cv,cv1);
      CashewValue mtype = buildMethodType(args[1]);
      CashewValue samtype = buildMethodType(args[3]);
      String mts1 = args[4];
      int idx = mts1.indexOf("(");
      CashewValue mt1 = buildMethodType(mts1.substring(idx));
      String mn1 = mts1.substring(0,idx);
      idx = mn1.indexOf(".");
      String clsnm1 = mn1.substring(0,idx);
      CashewValue mth1 = CashewValue.stringValue(mn1.substring(idx+1));
      JcompType cls1v = typer.findSystemType(clsnm1);
      CashewValue cls1 = CashewValue.classValue(cls1v);
      String lupname = "java/lang/invoke/MethodHandles$Lookup.findStatic";
      CashewValue mh = executeCall(lupname,lookup,cls1,mth1,mt1);
      CashewValue mt2 = buildMethodType(args[5]);
      CashewValue name = CashewValue.stringValue(args[0]);
      CashewValue cs = executeCall(args[2],name,mtype,samtype,mh,mt2);
      CashewValue cstgt = executeCall("java/lang/invoke/CallSite.getTarget",cs);
      CashewValue rslt = executeCall("java/lang/invoke/MethodHandle.invoke",cstgt);
      execution_stack.push(rslt);
      return null;
    }
   ***********/

   /***** this doesn't work either when the class is not public
   if (args[2].startsWith("java/lang/invoke/LambdaMetafactory.metafactory")) {
      String call = "edu.brown.cs.seede.poppy.PoppyValue.invokeLambdaMetaFactory(";
      String clsnm = getCodeMethod().getDeclaringClass().getName();
      lookup_context.enableAccess(clsnm);
      call += "\"" + clsnm + "\"";
      call += ",\"" + args[0] + "\"";
      call += ",\"" + args[1] + "\"";
      call += ",\"" + args[3] + "\"";
      call += ",\"" + args[4] + "\"";
      call += ",\"" + args[5] + "\"";
      call += ")";
      ensureLoaded("edu.brown.cs.seede.poppy.PoppyValue");
      CashewValue rslt = getLookupContext().evaluate(call);
      execution_stack.push(rslt);
    }
    **************/
}



/*************
private CashewValue buildMethodType(String typ)
{
   JcodeDataType jdt = getCodeFactory().findJavaType(typ);
   JcompType rtyp = getTyper().findSystemType(jdt.getReturnType().getName());
   CashewValue rval = CashewValue.classValue(rtyp);
   JcodeDataType [] atyps = jdt.getArgumentTypes();
   JcompType catype = getTyper().findArrayType(CLASS_TYPE);
   CashewValue aval = CashewValue.arrayValue(catype,atyps.length);
   for (int i = 0; i < atyps.length; ++i) {
      JcompType atyp = getTyper().findSystemType(atyps[i].getName());
      CashewValue cv = CashewValue.classValue(atyp);
      aval.setIndexValue(execution_clock,i,cv);
    }
<<<<<<< HEAD
   String m = "java/lang/invoke/MethodType.methodType(Ljava/lang/Class;" +
        "[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;";
=======
   String m = "java/lang/invoke/MethodType.methodType(Ljava/lang/Class;" + 
        *"[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;";
>>>>>>> ab1620ce64e2f8ba2f5f16bb5d28081f5a6bdf9a
   CashewValue rslt = executeCall(m,rval,aval);

   return rslt;
}
****************/



private JcompType buildMethodType1(String typ)
{
   JcodeDataType jdt = getCodeFactory().findJavaType(typ);
   JcompType rtyp = getTyper().findSystemType(jdt.getReturnType().getName());
   JcodeDataType [] atyps = jdt.getArgumentTypes();
   List<JcompType> atypl = new ArrayList<>();
   for (int i = 0; i < atyps.length; ++i) {
      JcompType atyp = getTyper().findSystemType(atyps[i].getName());
      atypl.add(atyp);
    }
   return getTyper().createMethodType(rtyp,atypl,false,null);
}



/********************************************************************************/
/*										*/
/*	Handle simulation internally						*/
/*										*/
/********************************************************************************/

//CHECKSTYLE:OFF
private CuminRunStatus checkSpecial() throws CuminRunException
//CHECKSTYLE:ON
{
   String cls = jcode_method.getDeclaringClass().getName();

   CuminRunStatus sts = null;

   try {
      switch (cls) {
	 case "java.lang.String" :
	    CuminDirectEvaluation cde = new CuminDirectEvaluation(this);
	    sts = cde.checkStringMethods();
	    break;
	 case "java.lang.Character" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkCharacterMethods();
	    break;
	 case "java.lang.StrictMath" :
	 case "java.lang.Math" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkMathMethods();
	    break;
	 case "java.lang.Runtime" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkRuntimeMethods();
	    break;
	 case "java.lang.Float" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkFloatMethods();
	    break;
	 case "java.lang.Double" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkDoubleMethods();
	    break;
         case "java.lang.Integer" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkIntegerMethods();
	    break;
	 case "java.lang.ClassLoader" :
	    // handle class loader methods
	    break;
	 case "java.lang.Class" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkClassMethods(); 
	    break;
         case "java.lang.Module" :
            cde = new CuminDirectEvaluation(this);
            sts = cde.checkModuleMethods();
            break;
	 case "java.lang.Object" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkObjectMethods();
	    break;
	 case "java.lang.System" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkSystemMethods();
	    break;
	 case "java.lang.Thread" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkThreadMethods();
	    break;
	 case "java.lang.Throwable" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkThrowableMethods();
	    break;
	 case "java.util.Random" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkRandomMethods();
	    break;

	 case "java.io.FileDescriptor" :
	 case "java.io.RandomAccessFile" :
	 case "java.nio.file.FileSystem" :
	 case "java.nio.file.spi.FileSystemProvider" :
	 case "java.nio.file.Files" :
	 case "sun.nio.cs.FastCharsetProvider" :
	    // TODO: handle other IO methods
	    break;

	 case "java.io.File" :
	    CuminIOEvaluator cie = new CuminIOEvaluator(this);
	    sts = cie.checkFileMethods();
	    break;
	 case "java.io.FileOutputStream" :
	    cie = new CuminIOEvaluator(this);
	    sts = cie.checkOutputStreamMethods();
	    break;
	 case "java.io.FileInputStream" :
	    cie = new CuminIOEvaluator(this);
	    sts = cie.checkInputStreamMethods();
	    break;
	 case "java.io.Console" :
	    cie = new CuminIOEvaluator(this);
	    sts = cie.checkConsoleMethods();
	    break;
	 case "java.io.PrintStream" :
	    cie = new CuminIOEvaluator(this);
	    sts  = cie.checkPrintMethods("java.io.PrintStream");
	    break;
	 case "java.io.PrintWriter" :
	    cie = new CuminIOEvaluator(this);
	    sts  = cie.checkPrintMethods("java.io.PrintWriter");
	    break;

	 case "java.io.ObjectOutputStream" :
	 case "java.io.ObjectInputStream" :
	    cie = new CuminIOEvaluator(this);
	    sts = cie.checkObjectStreamMethods();
	    break;
	 case "java.io.FileCleanable" :
	    cie = new CuminIOEvaluator(this);
	    sts = cie.checkFileCleanableMethods();
	    break;

	 case "sun.misc.FloatingDecimal" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkFloatingDecimalMehtods();
	    break;
	 case "javax.swing.SwingUtilities" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkSwingUtilityMethods();
	    break;
	 case "java.lang.reflect.Array" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkReflectArrayMethods();
	    break;
	 case "java.security.AccessController" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkAccessControllerMethods();
	    break;
	 case "sun.reflect.Reflection" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkSunReflectionMethods();
	    break;
         case "sun.invoke.util.VerifyAccess" :
         case "jdk.internal.reflect.Reflection" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkSunReflectionMethods();
	    break;
	 case "java.lang.Class$Atomic" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkClassAtomicMethods();
	    break;
	 case "java.lang.reflect.Constructor" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkConstructorMethods();
	    break;
	 case "java.lang.reflect.Method" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkMethodMethods();
	    break;
            
	 case "edu.brown.cs.seede.poppy.PoppyGraphics" :
	    CuminGraphicsEvaluator cge = new CuminGraphicsEvaluator(this);
	    sts = cge.checkPoppyGraphics();
	    break;
	 case "sun.awt.SunGraphicsCallback" :
	    cge = new CuminGraphicsEvaluator(this);
	    sts = cge.checkGraphicsCallback();
	    break;
         case "sun.java2d.loops.Blit" :
         case "sun.java2d.loops.DrawGlyphList"  :
         case "sun.java2d.loops.DrawGlyphListAA" :
         case "sun.java2d.loops.DrawGlyphListLCD" :
         case "sun.java2d.loops.DrawLine" :
         case "sun.java2d.loops.DrawParallelogram" :
         case "sun.java2d.loops.DrawPath" :
         case "sun.java2d.loops.DrawPolygons" :
         case "sun.java2d.loops.DrawRect" :  
         case "sun.java2d.loops.FillParallelogram" :
         case "sun.java2d.loops.FillPath" :
         case "sun.java2d.loops.FillRect" :
         case "sun.java2d.loops.FillSpans" : 
         case "sun.java2d.loops.MaskBlit" :
         case "sun.java2d.loops.MaskFill" :  
         case "sun.java2d.loops.ScaledBlit" :  
         case "sun.java2d.loops.TransformBlit" :           
         case "sun.java2d.loops.TransformHelper" :             
            cge = new CuminGraphicsEvaluator(this);
            sts = cge.checkLoops();
            break;
         case "javax.swing.JFrame" :
            cge = new CuminGraphicsEvaluator(this);
            sts = cge.checkJFrame();
            break;
         case "java.awt.Toolkit" :
            cge = new CuminGraphicsEvaluator(this);
            sts = cge.checkToolkit();
            break;
         case "java.awt.Component" :
            cge = new CuminGraphicsEvaluator(this);
            sts = cge.checkComponent();
            break;

	 case "java.util.concurrent.atomic.AtomicInteger" :
	 case "java.util.concurrent.atomic.AtomicLong" :
	    CuminConcurrentEvaluator cce = new CuminConcurrentEvaluator(this);
	    sts = cce.checkAtomicIntMethods();
	    break;
	 case "java.util.concurrent.atomic.AtomicBoolean" :
	    cce = new CuminConcurrentEvaluator(this);
	    sts = cce.checkAtomicBooleanMethods();
	    break;
	 case "java.util.concurrent.ConcurrentHashMap" :
	    cce = new CuminConcurrentEvaluator(this);
	    sts = cce.checkConcurrentHashMapMethods();
	    break;
	 case "sun.awt.SunToolkit" :
	    // this can be removed once VarHandle works
	    cce = new CuminConcurrentEvaluator(this);
	    sts = cce.checkSunToolkitMethods();
	    break;
	 case "java.lang.invoke.VarHandle" :
	    cce = new CuminConcurrentEvaluator(this);
	    sts = cce.checkVarHandleMethods();
	    break;

	 // need to handle java.util.concurrent.locks.ReentrantLock
	 // need to handle java.lang.VarHandle methods
	
	 case "sun.misc.Unsafe" :
	 case "jdk.internal.misc.Unsafe" :
	    cce = new CuminConcurrentEvaluator(this);
	    sts = cce.checkUnsafeMethods();
	    break;
	
	 case "java.util.regex.Pattern" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkPatternMethods();
	    break;
	 case "java.util.regex.Matcher" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkMatcherMethods();
	    break;
	 case "java.util.Arrays" :
	    cde = new CuminDirectEvaluation(this);
	    sts = cde.checkArraysMethods();
	    break;
         case "javax.swing.text.SegmentCache" :
            cde = new CuminDirectEvaluation(this);
            sts = cde.checkSegmentMethods();
            break;
            
         case "jdk.internal.access.JavaLangAccess" :
            cde = new CuminDirectEvaluation(this);
            sts = cde.checkAccessMethods();
            break;
         case "java.text.NumberFormat" :
         case "java.text.DecimalFormat" :
            cde = new CuminDirectEvaluation(this);
            sts = cde.checkNumberFormatMethods();
            break;
         case "java.lang.ref.Reference" :
            cde = new CuminDirectEvaluation(this);
            sts = cde.checkReferenceMethods();
            break;
         case "java.util.ResourceBundle" :
            cde = new CuminDirectEvaluation(this);
            sts = cde.checkResourceBundleMethods();
            break;
         case "java.util.TimeZone" :
            cde = new CuminDirectEvaluation(this);
            sts = cde.checkTimeZoneMethods();
            break;
         case "jdk.internal.misc.VM" :
            cde = new CuminDirectEvaluation(this); 
            sts = cde.checkVMMethods();
            break;
       }
    }
   catch (CuminRunException e) {
      throw e;
    }
   catch (CashewException e) {
      AcornLog.logD("Problem in special evaluation",e);
      throw CuminRunStatus.Factory.createCompilerError();
    }
   catch (Throwable t) {
      AcornLog.logE("Unknown Problem in special evaluation",t);
      throw CuminRunStatus.Factory.createCompilerError();
    }

   return sts;
}



private CuminRunStatus checkSpecialReturn(CuminRunStatus r)
{
   String cls = jcode_method.getDeclaringClass().getName();
   switch (cls) {
      case "java.lang.Class" :
	 CuminDirectEvaluation cde = new CuminDirectEvaluation(this);
	 r = cde.checkClassReturn(r);
	 break;
      case "java.lang.reflect.Constructor" :
	 cde = new CuminDirectEvaluation(this);
	 r = cde.checkConstructorReturn(r);
	 break;
    }
   return r;
}


}	// end of class CuminRunnerByteCode




/* end of CuminRunnerByteCode.java */





































































































































































































