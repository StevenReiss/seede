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
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;

import java.util.List;

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
private JcompTyper	type_converter;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminRunnerByteCode(CuminProject sp,CashewClock clock,
      JcodeMethod mthd,List<CashewValue> args)
{
   super(sp,clock,args);

   execution_stack = new CuminStack();
   execution_clock = clock;
   jcode_method = mthd;
   type_converter = sp.getTyper();
   current_instruction = 0;

   setupContext();
}



/********************************************************************************/
/*										*/
/*     Entry methods								*/
/*										*/
/********************************************************************************/

@Override protected void interpretRun(CuminRunError r)
{
   current_instruction = 0;
   try {
      while (current_instruction > 0) {
	 evaluateInstruction();
       }
    }
   catch (Throwable t) {

    }
}



/********************************************************************************/
/*										*/
/*	Context setup								*/
/*										*/
/********************************************************************************/

private void setupContext()
{
   CashewContext ctx = new CashewContext(jcode_method);

   int nlcl = jcode_method.getLocalSize();
   for (int i = 0; i <= nlcl; ++i) {
      ctx.define(Integer.valueOf(i),CashewValue.nullValue());
    }

   setLoockupContext(ctx);
}



/********************************************************************************/
/*										*/
/*	Main evaluation routine 						*/
/*										*/
/********************************************************************************/

private void evaluateInstruction() throws CuminRunError
{
   CashewValue vstack = null;
   JcodeInstruction nextins = null;
   CashewValue v0,v1,v2,v3;
   int next = current_instruction+1;

   JcodeInstruction jins = jcode_method.getInstruction(current_instruction);
   switch (jins.getOpcode()) {

// Arithmetic operators
      case DADD :
      case FADD :
      case IADD :
      case LADD :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.ADD,v0,v1);
	 break;
      case DSUB :
      case FSUB :
      case ISUB :
      case LSUB :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.SUB,v0,v1);
	 break;
      case DMUL :
      case FMUL :
      case IMUL :
      case LMUL :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.MUL,v0,v1);
	 break;
      case DDIV :
      case FDIV :
      case IDIV :
      case LDIV :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.DIV,v0,v1);
	 break;
      case DREM :
      case FREM :
      case IREM :
      case LREM :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.MOD,v0,v1);
	 break;
      case IAND :
      case LAND :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.AND,v0,v1);
	 break;
      case IOR :
      case LOR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.OR,v0,v1);
	 break;
      case IXOR :
      case LXOR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.XOR,v0,v1);
	 break;
      case ISHL :
      case LSHL :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.LSH,v0,v1);
	 break;
      case ISHR :
      case LSHR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.RSH,v0,v1);
	 break;
      case IUSHR :
      case LUSHR :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.RSHU,v0,v1);
	 break;
      case DCMPG :
      case DCMPL :
      case FCMPG :
      case FCMPL :
      case LCMP :
	 v1 = execution_stack.pop();
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.SIG,v0,v1);
	 break;
      case DNEG :
      case FNEG :
      case INEG :
      case LNEG :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.evaluate(execution_clock,CuminOperator.NEG,v0);
	 break;


// Constant operators
      case ACONST_NULL :
	 vstack = CashewValue.nullValue();
	 break;
      case DCONST_0 :
	 vstack = CashewValue.numericValue(DOUBLE_TYPE,0.0);
	 break;
      case DCONST_1 :
	 vstack = CashewValue.numericValue(DOUBLE_TYPE,1.0);
	 break;
      case FCONST_0 :
	 vstack = CashewValue.numericValue(FLOAT_TYPE,0.0f);
	 break;
      case FCONST_1 :
	 vstack = CashewValue.numericValue(FLOAT_TYPE,1.0f);
	 break;
      case FCONST_2 :
	 vstack = CashewValue.numericValue(FLOAT_TYPE,2.0f);
	 break;
      case ICONST_0 :
      case ICONST_1 :
      case ICONST_2 :
      case ICONST_3 :
      case ICONST_4 :
      case ICONST_5 :
      case ICONST_M1 :
	 vstack = CashewValue.numericValue(INT_TYPE,jins.getIntValue());
	 break;
      case LCONST_0 :
      case LCONST_1 :
	 vstack = CashewValue.numericValue(LONG_TYPE,jins.getIntValue());
	 break;
      case LDC :
	 Object o = jins.getObjectValue();
	 if (o instanceof String) {
	    vstack = CashewValue.stringValue((String) o);
	  }
	 else if (o instanceof Integer) {
	    vstack = CashewValue.numericValue(INT_TYPE,((Number) o).intValue());
	  }
	 else if (o instanceof Long) {
	    vstack = CashewValue.numericValue(LONG_TYPE,((Number) o).longValue());
	  }
	 else if (o instanceof Float) {
	    vstack = CashewValue.numericValue(FLOAT_TYPE,((Number) o).floatValue());
	  }
	 else if (o instanceof Double) {
	    vstack = CashewValue.numericValue(DOUBLE_TYPE,((Number) o).doubleValue());
	  }
	 else if (o instanceof Type) {
	    Type t = (Type) o;
	    JcompType jtyp = type_converter.findSystemType(t.getClassName());
	    vstack = CashewValue.classValue(jtyp);
	  }
	 break;
      case BIPUSH :
      case SIPUSH :
	 vstack = CashewValue.numericValue(INT_TYPE,jins.getIntValue());
	 break;

// CONVERSION OPERATORS
      case I2B :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(execution_clock,v0,BYTE_TYPE);
	 break;
      case I2C :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(execution_clock,v0,CHAR_TYPE);
	 break;
      case F2D :
      case I2D :
      case L2D :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(execution_clock,v0,DOUBLE_TYPE);
	 break;
      case D2F :
      case I2F :
      case L2F :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(execution_clock,v0,FLOAT_TYPE);
	 break;
      case D2L :
      case F2L :
      case I2L :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(execution_clock,v0,LONG_TYPE);
	 break;
      case I2S :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(execution_clock,v0,SHORT_TYPE);
	 break;
      case D2I :
      case F2I :
      case L2I :
	 v0 = execution_stack.pop();
	 vstack = CuminEvaluator.castValue(execution_clock,v0,INT_TYPE);
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
	 if (v1.isCategory2(execution_clock)) {
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
	 if (v0.isCategory2(execution_clock)) {
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
	 if (v0.isCategory2(execution_clock)) {
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
	 if (v0.isCategory2(execution_clock) && v1.isCategory2(execution_clock)) {
	    execution_stack.push(v0);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 else if (v0.isCategory2(execution_clock)) {
	    v2 = execution_stack.pop();
	    execution_stack.push(v0);
	    execution_stack.push(v2);
	    execution_stack.push(v1);
	    execution_stack.push(v0);
	  }
	 else {
	    v2 = execution_stack.pop();
	    if (v2.isCategory2(execution_clock)) {
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
	 if (!v0.isCategory2(execution_clock)) execution_stack.pop();
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
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.EQL,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ACMPNE :
      case IF_ICMPNE :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.NEQ,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPGE :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.GEQ,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPGT :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.GTR,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPLE :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.LEQ,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IF_ICMPLT :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.LSS,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFEQ :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(execution_clock),0);
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.EQL,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFGE :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(execution_clock),0);
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.GEQ,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break; 
      case IFGT :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(execution_clock),0);
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.GTR,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFLE :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(execution_clock),0);
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.LEQ,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFLT :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(execution_clock),0);
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.LSS,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFNE :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.numericValue(v0.getDataType(execution_clock),0);
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.NEQ,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFNONNULL :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.nullValue();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.NEQ,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;
      case IFNULL :
	 v0 = execution_stack.pop();
	 v1 = CashewValue.nullValue();
	 v2 = CuminEvaluator.evaluate(execution_clock,CuminOperator.EQL,v0,v1);
	 if (v2.getBoolean(execution_clock)) nextins = jins.getTargetInstruction();
	 break;

      case AALOAD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 vstack = v1.getIndexValue(execution_clock,v0.getNumber(execution_clock).intValue());
	 break;
      case BALOAD :
      case CALOAD :
      case SALOAD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 vstack = v1.getIndexValue(execution_clock,v0.getNumber(execution_clock).intValue());
	 vstack = CuminEvaluator.castValue(execution_clock,vstack,INT_TYPE);
	 break;
      case DALOAD :
      case LALOAD :
      case IALOAD :
      case FALOAD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 vstack = v1.getIndexValue(execution_clock,v0.getNumber(execution_clock).intValue());
	 break;


      case AASTORE :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 int idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;
      case BASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(execution_clock,v0,BYTE_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;
      case CASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(execution_clock,v0,CHAR_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;
      case DASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(execution_clock,v0,DOUBLE_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;
      case FASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(execution_clock,v0,FLOAT_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;
      case IASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(execution_clock,v0,INT_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;
      case LASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(execution_clock,v0,LONG_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;
      case SASTORE :
	 v0 = execution_stack.pop();
	 v0 = CuminEvaluator.castValue(execution_clock,v0,SHORT_TYPE);
	 v1 = execution_stack.pop();
	 v2 = execution_stack.pop();
	 idxv = v1.getNumber(execution_clock).intValue();
	 v2.setIndexValue(execution_clock,idxv,v0);
	 break;

      case ALOAD :
	 Integer vidx = jins.getIntValue();
	 vstack = lookup_context.findReference(vidx).getActualValue(execution_clock);
	 break;
      case DLOAD :
	 vidx = jins.getIntValue();
	 vstack = lookup_context.findReference(vidx).getActualValue(execution_clock);
	 vstack = CuminEvaluator.castValue(execution_clock,vstack,DOUBLE_TYPE);
	 break;
      case FLOAD :
	 vidx = jins.getIntValue();
	 vstack = lookup_context.findReference(vidx).getActualValue(execution_clock);
	 vstack = CuminEvaluator.castValue(execution_clock,vstack,FLOAT_TYPE);
	 break;
      case ILOAD :
	 vidx = jins.getIntValue();
	 vstack = lookup_context.findReference(vidx).getActualValue(execution_clock);
	 vstack = CuminEvaluator.castValue(execution_clock,vstack,INT_TYPE);
	 break;
      case LLOAD :
	 vidx = jins.getIntValue();
	 vstack = lookup_context.findReference(vidx).getActualValue(execution_clock);
	 vstack = CuminEvaluator.castValue(execution_clock,vstack,LONG_TYPE);
	 break;

      case ASTORE :
	 vidx = jins.getIntValue();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(execution_clock);
	 v0.setValueAt(execution_clock,v1);
	 break;
      case DSTORE :
	 vidx = jins.getIntValue();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(execution_clock);
	 v1 = CuminEvaluator.castValue(execution_clock,v1,DOUBLE_TYPE);
	 v0.setValueAt(execution_clock,v1);
	 break;
      case FSTORE :
	 vidx = jins.getIntValue();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(execution_clock);
	 v1 = CuminEvaluator.castValue(execution_clock,v1,FLOAT_TYPE);
	 v0.setValueAt(execution_clock,v1);
	 break;
      case LSTORE :
	 vidx = jins.getIntValue();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(execution_clock);
	 v1 = CuminEvaluator.castValue(execution_clock,v1,LONG_TYPE);
	 v0.setValueAt(execution_clock,v1);
	 break;
      case ISTORE :
	 vidx = jins.getIntValue();
	 v0 = lookup_context.findReference(vidx);
	 v1 = execution_stack.pop().getActualValue(execution_clock);
	 v1 = CuminEvaluator.castValue(execution_clock,v1,INT_TYPE);
	 v0.setValueAt(execution_clock,v1);
	 break;

      case ARETURN :
      case DRETURN :
      case FRETURN :
      case IRETURN :
      case LRETURN :
	 v0 = execution_stack.pop().getActualValue(execution_clock);
	 throw new CuminRunError(CuminRunError.Reason.RETURN,v0);
      case RETURN :
	 throw new CuminRunError(CuminRunError.Reason.RETURN);
      case ATHROW :
	 v0 = execution_stack.pop().getActualValue(execution_clock);
	 throw new CuminRunError(CuminRunError.Reason.EXCEPTION,v0);

      case ARRAYLENGTH :
	 v0 = execution_stack.pop();
	 vstack = CashewValue.numericValue(INT_TYPE,v0.getDimension(execution_clock));
	 break;
      case IINC :
	 vidx = jins.getIntValue();
	 v0 = lookup_context.findReference(vidx);
	 v1 = CashewValue.numericValue(INT_TYPE,v0.getNumber(execution_clock).intValue() + 1);
	 v0.setValueAt(execution_clock,v1);
	 break;

      case CHECKCAST :
	 // might want to actually do something here
	 break;
      case GETFIELD :
	 v0 = execution_stack.pop();
	 JcodeField fld = jins.getFieldReference();
	 String nm = fld.getDeclaringClass().getName() + "." + fld.getName();
	 vstack = v0.getFieldValue(execution_clock,nm);
	 break;
      case GETSTATIC :
	 fld = jins.getFieldReference();
	 nm = fld.getDeclaringClass().getName() + "." + fld.getName();
	 vstack = lookup_context.findReference(nm);
	 vstack = vstack.getActualValue(execution_clock);
	 break;
      case PUTFIELD :
	 v0 = execution_stack.pop();
	 v1 = execution_stack.pop();
	 fld = jins.getFieldReference();
	 nm = fld.getDeclaringClass().getName() + "." + fld.getName();
	 v1.setFieldValue(execution_clock,nm,v0);
	 break;
      case PUTSTATIC :
	 v0 = execution_stack.pop();
	 v0 = v0.getActualValue(execution_clock);
	 fld = jins.getFieldReference();
	 nm = fld.getDeclaringClass().getName() + "." + fld.getName();
	 v1 = lookup_context.findReference(nm);
	 v1.setValueAt(execution_clock,v0);
	 break;
      case NEW :
	 JcompType nty = convertType(jins.getTypeReference());
	 vstack = CashewValue.objectValue(nty);
	 break;


      case INVOKEDYNAMIC :
      case INVOKEINTERFACE :
      case INVOKESPECIAL :
      case INVOKESTATIC :
      case INVOKEVIRTUAL :
	 break;

      case JSR :
	 execution_stack.pushMarker(next);
	 nextins = jins.getTargetInstruction();
	 break;
      case RET :
	 next = (Integer) execution_stack.popMarker();
	 break;

      case LOOKUPSWITCH :
      case MONITORENTER :
      case MONITOREXIT :
      case MULTIANEWARRAY :
      case NEWARRAY :
      case TABLESWITCH :
	 break;
      case INSTANCEOF :
      case ANEWARRAY :
	 break;

    }

   if (vstack != null) execution_stack.push(vstack);
   if (nextins != null) next = nextins.getIndex();
   current_instruction = next;
}




/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

JcompType convertType(JcodeDataType cty)
{
   return type_converter.findType(cty.getName());
}





}	// end of class CuminRunnerByteCode




/* end of CuminRunnerByteCode.java */

