package com.mekeng.github.core.transformers;

import com.mekeng.github.core.MkEClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PartP2PTunnelTransformer extends MkEClassTransformer.ClassMapper {

    public static PartP2PTunnelTransformer INSTANCE = new PartP2PTunnelTransformer();

    private PartP2PTunnelTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformPartP2PTunnel(Opcodes.ASM5, downstream);
    }

    private static class TransformPartP2PTunnel extends ClassVisitor {

        TransformPartP2PTunnel(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("onPartActivate")) {
                return new TransformOnPartActivate(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformOnPartActivate extends MethodVisitor {

        Label L = new Label();

        TransformOnPartActivate(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitVarInsn(Opcodes.ALOAD, 2);
            super.visitVarInsn(Opcodes.ALOAD, 3);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/mekeng/github/core/CoreHooks", "hooker$customTunnel", "(Lappeng/parts/p2p/PartP2PTunnel;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/math/Vec3d;)Z", false);
            super.visitJumpInsn(Opcodes.IFEQ, L);
            super.visitInsn(Opcodes.ICONST_1);
            super.visitInsn(Opcodes.IRETURN);
            super.visitLabel(L);
            super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }

    }

}
