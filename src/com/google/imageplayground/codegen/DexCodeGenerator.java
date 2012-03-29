/* 
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.imageplayground.codegen;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

import com.google.dexmaker.BinaryOp;
import com.google.dexmaker.Code;
import com.google.dexmaker.Local;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;
import com.google.dexmaker.UnaryOp;
import com.google.imageplayground.parser.ScriptGrammarLexer;
import com.google.imageplayground.parser.ScriptGrammarParser;

public class DexCodeGenerator {
	
	static Map<String, BinaryOp> BINARY_OPS = new HashMap<String, BinaryOp>();
	static Map<String, UnaryOp> UNARY_OPS = new HashMap<String, UnaryOp>();

    static {
    	BINARY_OPS.put("+", BinaryOp.ADD);
    	BINARY_OPS.put("-", BinaryOp.SUBTRACT);
    	BINARY_OPS.put("*", BinaryOp.MULTIPLY);
    	BINARY_OPS.put("/", BinaryOp.DIVIDE);
    	BINARY_OPS.put("%", BinaryOp.REMAINDER);
    	BINARY_OPS.put("&", BinaryOp.AND);
    	BINARY_OPS.put("|", BinaryOp.OR);
    	BINARY_OPS.put("^", BinaryOp.XOR);
    	BINARY_OPS.put(">>", BinaryOp.SHIFT_RIGHT);
    	BINARY_OPS.put("<<", BinaryOp.SHIFT_LEFT);
    	BINARY_OPS.put(">>>", BinaryOp.UNSIGNED_SHIFT_RIGHT);
    	
    	UNARY_OPS.put("NEG", UnaryOp.NEGATE);
    	UNARY_OPS.put("~", UnaryOp.NOT);
    }
    
    static class InstructionContext {
    	public Set<String> locals = new HashSet<String>();
    	public Set<String> labels = new HashSet<String>();
    	public List<Instruction> instructions = new ArrayList<Instruction>();
    	
    	int syntheticLocalCounter = 0;
    	public String nextSyntheticLocal() {
    		syntheticLocalCounter++;
    		String name = "!" + syntheticLocalCounter;
    		locals.add(name);
    		return name;
    	}
    	public void resetSyntheticLocals() {
    		syntheticLocalCounter = 0;
    	}
    }
    
    static Tree createParseTree(String userScript) throws Exception {
    	userScript = userScript.trim() + "\n";
        ANTLRInputStream input = new ANTLRInputStream(new ByteArrayInputStream(userScript.getBytes("utf-8")));
        ScriptGrammarLexer lexer = new ScriptGrammarLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScriptGrammarParser parser = new ScriptGrammarParser(tokens);
        ScriptGrammarParser.prog_return r = parser.prog();
        return (Tree)r.getTree();
    }
    
    public static InstructionContext createInstructionList(String userScript) throws Exception {
    	InstructionContext context = new InstructionContext();
    	// parse input
    	Tree tree = createParseTree(userScript);
    	// generate instructions in memory
    	generateInstructions(tree, context);
    	return context;
    }
    
    public static void generateMethodCode(Code code, Map<String, Local> providedLocals, TypeId thisType, String userScript) throws Exception {
    	InstructionContext context = createInstructionList(userScript);
    	// create locals that aren't provided
    	Map<String, Local> allLocals = new HashMap(providedLocals);
    	for(String localName : context.locals) {
    		if (!allLocals.containsKey(localName)) {
    			// FIXME: need to support boolean at least, float would be nice
    			allLocals.put(localName, code.newLocal(TypeId.INT));
    		}
    	}
    	// write code now that we have all the locals available
    	for(Instruction inst : context.instructions) {
    		inst.generateCode(code, allLocals, thisType);
    	}
    }
	
    static abstract class Instruction {
    	abstract void generateCode(Code code, Map<String, Local> localMap, TypeId thisType);
    	
    	public boolean equals(Object other) {
    		return (other!=null && this.getClass()==other.getClass() && this.toString().equals(other.toString()));
    	}
    	
    	public int hashCode() {
    		return this.toString().hashCode();
    	}
    }
    
    static class ConstantIntAssignment extends Instruction {
		public final String targetLocal;
    	public final int value;
    	public ConstantIntAssignment(String targetLocal, int value) {
			this.targetLocal = targetLocal;
			this.value = value;
		}

    	public void generateCode(Code code, Map<String, Local> localMap, TypeId thisType) {
    		code.loadConstant(localMap.get(targetLocal), value);
    	}
    	public String toString() {
    		return String.format("[%s] <- %s", targetLocal, value);
    	}
    }
    
    static class IntAssignment extends Instruction {
    	public final String targetLocal;
    	public final String sourceLocal;
    	public IntAssignment(String targetLocal, String sourceLocal) {
			this.targetLocal = targetLocal;
			this.sourceLocal = sourceLocal;
		}
    	
		public void generateCode(Code code, Map<String, Local> localMap, TypeId thisType) {
    		code.move(localMap.get(targetLocal), localMap.get(sourceLocal));
    	}
    	public String toString() {
    		return String.format("[%s] <- [%s]", targetLocal, sourceLocal);
    	}
    }
    
    static class BinaryIntOperation extends Instruction {
    	public final BinaryOp operator;
    	public final String leftLocal;
    	public final String rightLocal;
    	public final String targetLocal;
    	public BinaryIntOperation(String targetLocal, BinaryOp operator, String leftLocal, String rightLocal) {
			this.operator = operator;
			this.leftLocal = leftLocal;
			this.rightLocal = rightLocal;
			this.targetLocal = targetLocal;
		}
    	
		public void generateCode(Code code, Map<String, Local> localMap, TypeId thisType) {
    		code.op(operator, localMap.get(targetLocal), localMap.get(leftLocal), localMap.get(rightLocal));
    	}
    	public String toString() {
    		return String.format("[%s] <- %s([%s], [%s])", targetLocal, operator, leftLocal, rightLocal);
    	}
    }
    
    static class UnaryIntOperation extends Instruction {
    	public final UnaryOp operator;
    	public final String sourceLocal;
    	public final String targetLocal;
    	public UnaryIntOperation(String targetLocal, UnaryOp operator, String sourceLocal) {
			this.operator = operator;
			this.sourceLocal = sourceLocal;
			this.targetLocal = targetLocal;
		}
    	
		public void generateCode(Code code, Map<String, Local> localMap, TypeId thisType) {
    		code.op(operator, localMap.get(targetLocal), localMap.get(sourceLocal));
    	}
    	public String toString() {
    		return String.format("[%s] <- %s([%s])", targetLocal, operator, sourceLocal);
    	}
    }
    
    static class FunctionCallInstruction extends Instruction {
    	public final String functionName;
    	public final String[] argumentLocals;
    	public final String targetLocal;
    	public FunctionCallInstruction(String targetLocal, String functionName, String[] argumentLocals) {
			this.functionName = functionName;
			this.argumentLocals = argumentLocals;
			this.targetLocal = targetLocal;
		}

		public void generateCode(Code code, Map<String, Local> localMap, TypeId thisType) {
    		// call the superclass (DexImageScript) method with the script_ prefix
    		TypeId superclass = TypeId.get(DexImageScript.class);
    		String methodName = "script_" + functionName;
    		// get method parametesr
    		TypeId[] parameterTypes = null;
    		try {
    			Method[] declaredMethods = DexImageScript.class.getDeclaredMethods();
    			for(Method m : declaredMethods) {
    				if (methodName.equals(m.getName())) {
    					Class[] paramClasses = m.getParameterTypes();
    					parameterTypes = new TypeId[paramClasses.length];
    					for(int i=0; i<paramClasses.length; i++) {
    						parameterTypes[i] = TypeId.get(paramClasses[i]);
    					}
    				}
    			}
    		}
    		catch(Exception ex) {
    			throw new IllegalStateException(ex);
    		}
    		MethodId methodId = superclass.getMethod(TypeId.INT, methodName, parameterTypes);
    		Local[] parameterLocals = new Local[argumentLocals.length];
    		for(int i=0; i<argumentLocals.length; i++) {
    			parameterLocals[i] = localMap.get(argumentLocals[i]);
    		}
    		code.invokeSuper(methodId, localMap.get(targetLocal), code.getThis(thisType), parameterLocals);
    	}

    	public String toString() {
    		StringBuilder argString = new StringBuilder();
    		if (argumentLocals!=null) {
    			if (argumentLocals.length>0) argString.append(String.format("[%s]", argumentLocals[0]));
    			for(int i=1; i<argumentLocals.length; i++) {
    				argString.append(String.format(", [%s]", argumentLocals[i]));
    			}
    		}
    		return String.format("[%s] <- CALL %s(%s)", targetLocal, functionName, argString.toString());
    	}
    }
    
    static class ReturnInstruction extends Instruction {
    	public final String targetLocal;
    	public ReturnInstruction(String targetLocal) {
			this.targetLocal = targetLocal;
		}
    	
		public void generateCode(Code code, Map<String, Local> localMap, TypeId thisType) {
    		code.returnValue(localMap.get(targetLocal));
    	}
    	public String toString() {
    		return String.format("RETURN [%s]", targetLocal);
    	}
    }
    
    static void generateInstructions(Tree root, InstructionContext context) {
    	if (root.getText()!=null) {
    		// this is a single instruction instead of a list of instructions
            generateInstructionsForSubtree(root, context);
            context.resetSyntheticLocals();
    	}
    	else {
        	int size = root.getChildCount();
        	for(int i=0; i<size; i++) {
        		generateInstructionsForSubtree(root.getChild(i), context);
        		context.resetSyntheticLocals();
        	}
    	}
    }
    
    static String generateInstructionsForSubtree(Tree tree, InstructionContext context) {
    	return generateInstructionsForSubtree(tree, context, null);
    }
    
    static String generateInstructionsForSubtree(Tree tree, InstructionContext context, String targetName) {
    	String token = tree.getText();
    	if (tree.getChildCount()==0) {
    		// this is a literal or a variable name
    		return resolveLocal(token, context, targetName);
    	}
    	else if (BINARY_OPS.containsKey(token)) {
    		// create synthetic local to store result if needed
    		String leftArg = generateInstructionsForSubtree(tree.getChild(0), context);
    		String rightArg = generateInstructionsForSubtree(tree.getChild(1), context);
    		String target = (targetName!=null) ? targetName : context.nextSyntheticLocal();
    		BinaryIntOperation inst = new BinaryIntOperation(target, BINARY_OPS.get(token), leftArg, rightArg);
    		context.instructions.add(inst);
    		return target;
    	}
    	else if (UNARY_OPS.containsKey(token)) {
    		// TODO: if argument is a constant, modify just-generated load instruction to store computed value directly
    		String arg = generateInstructionsForSubtree(tree.getChild(0), context);
    		String target = (targetName!=null) ? targetName : context.nextSyntheticLocal();
    		UnaryIntOperation inst = new UnaryIntOperation(target, UNARY_OPS.get(token), arg);
    		context.instructions.add(inst);
    		return target;
    	}
    	else if ("=".equals(token)) {
    		// TODO: verify that target can be assigned to
    		String target = tree.getChild(0).getText();
            // We need to add the target to the set of locals, in case it's not referenced in an expression.
            // (In which case this instruction is likely useless).
            context.locals.add(target);
    		// passing target to the recursive call will cause its result to be assigned to target
    		generateInstructionsForSubtree(tree.getChild(1), context, target);
    		return target;
    	}
    	else if ("CALL".equals(token)) {
    		int tsize = tree.getChildCount();
    		String functionName = tree.getChild(0).getText();
    		String[] arguments = new String[tsize-1];
    		for(int i=1; i<tsize; i++) {
    			arguments[i-1] = generateInstructionsForSubtree(tree.getChild(i), context);
    		}
    		String target = (targetName!=null) ? targetName : context.nextSyntheticLocal();
    		FunctionCallInstruction inst = new FunctionCallInstruction(target, functionName, arguments);
    		context.instructions.add(inst);
    		return inst.targetLocal;
    	}
    	else if ("return".equals(token)) {
    		String result = generateInstructionsForSubtree(tree.getChild(0), context);
    		ReturnInstruction inst = new ReturnInstruction(result);
    		context.instructions.add(inst);
    		return ""; // shouldn't be used because this should be a top-level statement
    	}
        else if ("BLOCK".equals(token)) {
            for (int ii=0; ii<tree.getChildCount(); ii++) {
                generateInstructions(tree.getChild(ii), context);
            }
            return "";
        }
    	System.err.println("Unknown token: " + token);
    	return "";
    }
    
    static String resolveLocal(String text, InstructionContext context, String targetName) {
    	// number?
    	try {
    		int value = Integer.parseInt(text);
    		// create local and add assignment instruction
    		String constLocal = (targetName!=null) ? targetName : context.nextSyntheticLocal();
    		ConstantIntAssignment inst = new ConstantIntAssignment(constLocal, value);
    		context.instructions.add(inst);
    		return constLocal;
    	}
    	catch(Exception ignored) {}
    	// variable name; assign if we have a target
    	context.locals.add(text);
    	if (targetName!=null) {
    		IntAssignment inst = new IntAssignment(targetName, text);
    		context.instructions.add(inst);
    	}
    	return text;
    }

}
