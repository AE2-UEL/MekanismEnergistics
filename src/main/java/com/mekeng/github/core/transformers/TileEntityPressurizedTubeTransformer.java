package com.mekeng.github.core.transformers;

import com.mekeng.github.core.MkEClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TileEntityPressurizedTubeTransformer extends MkEClassTransformer.ClassMapper {

    public static TileEntityPressurizedTubeTransformer INSTANCE = new TileEntityPressurizedTubeTransformer();

    private TileEntityPressurizedTubeTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformTileEntityPressurizedTube(Opcodes.ASM5, downstream);
    }

    private static class TransformTileEntityPressurizedTube extends ClassVisitor {

        TransformTileEntityPressurizedTube(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("update") || name.equals("func_73660_a")) {
                return new TransformUpdate(api, super.visitMethod(access, name, desc, signature, exceptions));
            } else if (name.equals("doRestrictedTick")) {
                return new TransformUpdateU(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformUpdate extends MethodVisitor {

        boolean ready = false;

        TransformUpdate(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKEINTERFACE && name.equals("drawGas")) {
                if (ready) {
                    super.visitVarInsn(Opcodes.ALOAD, 5);
                    super.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            "com/mekeng/github/core/CoreHooks",
                            "hooker$wrapGasHandler",
                            "(Lmekanism/api/gas/IGasHandler;Lnet/minecraft/util/EnumFacing;IZLmekanism/api/gas/GasStack;)Lmekanism/api/gas/GasStack;",
                            false
                    );
                    ready = false;
                    return;
                } else {
                    ready = true;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

    }

    private static class TransformUpdateU extends MethodVisitor {

        boolean ready = false;

        TransformUpdateU(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKEINTERFACE && name.equals("drawGas")) {
                if (ready) {
                    super.visitVarInsn(Opcodes.ALOAD, 6);
                    super.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            "com/mekeng/github/core/CoreHooks",
                            "hooker$wrapGasHandler",
                            "(Lmekanism/api/gas/IGasHandler;Lnet/minecraft/util/EnumFacing;IZLmekanism/api/gas/GasStack;)Lmekanism/api/gas/GasStack;",
                            false
                    );
                    ready = false;
                    return;
                } else {
                    ready = true;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

    }

}
