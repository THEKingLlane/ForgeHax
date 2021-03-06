package com.matt.forgehax.asm.utils;

import com.matt.forgehax.asm.utils.asmtype.ASMField;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ASMHelper {
    public final static Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<Class<?>, Class<?>>();
    public final static Map<Class<?>, Class<?>> wrapperToPrimitive = new HashMap<Class<?>, Class<?>>();
    public final static Map<Class<?>, Character> primitiveToDescriptor = new HashMap<Class<?>, Character>();

    static {
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(void.class, Void.class);

        wrapperToPrimitive.put(Boolean.class, boolean.class);
        wrapperToPrimitive.put(Byte.class, byte.class);
        wrapperToPrimitive.put(Short.class, short.class);
        wrapperToPrimitive.put(Character.class, char.class);
        wrapperToPrimitive.put(Integer.class, int.class);
        wrapperToPrimitive.put(Long.class, long.class);
        wrapperToPrimitive.put(Float.class, float.class);
        wrapperToPrimitive.put(Double.class, double.class);
        wrapperToPrimitive.put(Void.class, void.class);

        primitiveToDescriptor.put(boolean.class, 'Z');
        primitiveToDescriptor.put(byte.class, 'B');
        primitiveToDescriptor.put(short.class, 'S');
        primitiveToDescriptor.put(char.class, 'C');
        primitiveToDescriptor.put(int.class, 'I');
        primitiveToDescriptor.put(long.class, 'J');
        primitiveToDescriptor.put(float.class, 'F');
        primitiveToDescriptor.put(double.class, 'D');
        primitiveToDescriptor.put(void.class, 'V');
    }

    /**
     * Finds a pattern of opcodes and returns the first node of the matched pattern if found
     * @param start starting node
     * @param pattern integer array of opcodes
     * @param mask same length as the pattern. 'x' indicates the node will be checked, '?' indicates the node will be skipped over (has a bad opcode)
     * @return top node of matching pattern or null if nothing is found
     */
    public static AbstractInsnNode findPattern(AbstractInsnNode start, int[] pattern, char[] mask) {
        if(start != null &&
                pattern.length == mask.length) {
            int found = 0;
            AbstractInsnNode next = start;
            do {
                switch(mask[found]) {
                    // Analyze this node
                    case 'x': {
                        // Check if node and pattern have same opcode
                        if(next.getOpcode() == pattern[found]) {
                            // Increment number of matched opcodes
                            found++;
                        } else {
                            // Go back to the starting node
                            for(int i = 1; i <= (found - 1); i++) {
                                next = next.getPrevious();
                            }
                            // Reset the number of opcodes found
                            found = 0;
                        }
                        break;
                    }
                    // Skips over this node
                    default:
                    case '?':
                        found++;
                        break;
                }
                // Check if found entire pattern
                if(found >= mask.length) {
                    // Go back to top node
                    for(int i = 1; i <= (found - 1); i++)
                        next = next.getPrevious();
                    return next;
                }
                next = next.getNext();
            } while(next != null &&
                    found < mask.length);
        }
        return null;
        //throw new NoMatchingPatternException("Failed to match pattern '" + getPatternAsString(pattern, mask) + "'");
    }
    public static AbstractInsnNode findPattern(AbstractInsnNode start, int[] pattern, String mask) {
        return findPattern(start,
                pattern,
                mask.toCharArray());
    }

    public static AbstractInsnNode findStart(AbstractInsnNode start) {
        AbstractInsnNode next = start;
        do {
            if(next.getOpcode() != -1)
                return next;
            else
                next = next.getNext();
        } while(next != null);
        return null;
    }

    public static String getClassData(ClassNode node) {
        StringBuilder builder = new StringBuilder("METHODS:\n");
        for(MethodNode method : node.methods) {
            builder.append("\t");
            builder.append(method.name);
            builder.append(method.desc);
            builder.append("\n");
        }
        builder.append("\nFIELDS:\n");
        for(FieldNode field : node.fields) {
            builder.append("\t");
            builder.append(field.desc);
            builder.append(" ");
            builder.append(field.name);
            builder.append("\n");
        }
        return builder.toString();
    }

    public static MethodInsnNode call(int opcode, boolean isInterface, ASMMethod method) {
        Objects.requireNonNull(method.getParentClass(), "Method requires assigned parent class");
        return new MethodInsnNode(opcode,
                method.getParentClass().getRuntimeInternalName(),
                method.getRuntimeName(),
                method.getRuntimeDescriptor(),
                false
        );
    }

    public static MethodInsnNode call(int opcode, ASMMethod method) {
        return call(opcode, false, method);
    }

    public static FieldInsnNode call(int opcode, ASMField field) {
        Objects.requireNonNull(field.getParentClass(), "Field requires assigned parent class");
        return new FieldInsnNode(opcode,
                field.getParentClass().getRuntimeInternalName(),
                field.getRuntimeName(),
                field.getRuntimeDescriptor()
        );
    }
}
