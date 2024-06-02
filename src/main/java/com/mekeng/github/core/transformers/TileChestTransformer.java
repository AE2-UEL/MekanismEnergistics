package com.mekeng.github.core.transformers;

import com.mekeng.github.core.MkEClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TileChestTransformer extends MkEClassTransformer.ClassMapper {

    public static TileChestTransformer INSTANCE = new TileChestTransformer();

    private TileChestTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformTileChest(Opcodes.ASM5, downstream);
    }

    @Override
    protected int getWriteFlags() {
        return ClassWriter.COMPUTE_FRAMES;
    }

    private static class TransformTileChest extends ClassVisitor {

        TransformTileChest(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("getGuiBridge")) {
                return new TransformGetGuiBridge(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformGetGuiBridge extends MethodVisitor {

        boolean ready = false;
        Label L = new Label();

        TransformGetGuiBridge(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);
            if (opcode == Opcodes.IFNULL) {
                ready = true;
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
            if (ready && opcode == Opcodes.ALOAD) {
                ready = false;
                super.visitFieldInsn(Opcodes.GETFIELD, "appeng/tile/storage/TileChest", "cellHandler", "Lappeng/tile/storage/TileChest$ChestMonitorHandler;");
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "appeng/tile/storage/TileChest$ChestMonitorHandler", "getChannel", "()Lappeng/api/storage/IStorageChannel;", false);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/mekeng/github/core/CoreHooks", "hooker$getGuiBridge", "(Lappeng/api/storage/IStorageChannel;)Lappeng/core/sync/GuiBridge;", false);
                super.visitInsn(Opcodes.DUP);
                super.visitJumpInsn(Opcodes.IFNULL, L);
                super.visitInsn(Opcodes.ARETURN);
                super.visitLabel(L);
                super.visitInsn(Opcodes.POP);
                super.visitVarInsn(Opcodes.ALOAD, 0);
            }
        }

    }

}
