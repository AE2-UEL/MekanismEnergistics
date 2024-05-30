package com.mekeng.github.core;

import com.mekeng.github.core.transformers.PartP2PTunnelTransformer;
import com.mekeng.github.core.transformers.TileChestTransformer;
import com.mekeng.github.core.transformers.TileEntityPressurizedTubeTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class MkEClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        Transform tform;
        switch (transformedName) {
            case "mekanism.common.tile.transmitter.TileEntityPressurizedTube":
                tform = TileEntityPressurizedTubeTransformer.INSTANCE;
                break;
            case "appeng.tile.storage.TileChest":
                tform = TileChestTransformer.INSTANCE;
                break;
            case "appeng.parts.p2p.PartP2PTunnel":
                tform = PartP2PTunnelTransformer.INSTANCE;
                break;
            default:
                return code;
        }
        System.out.println("[MekEng] Transforming class: " + transformedName);
        return tform.transformClass(code);
    }

    public interface Transform {

        byte[] transformClass(byte[] code);

    }

    public static abstract class ClassMapper implements Transform {

        @Override
        public byte[] transformClass(byte[] code) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, getWriteFlags());
            reader.accept(getClassMapper(writer), 0);
            return writer.toByteArray();
        }

        protected int getWriteFlags() {
            return 0;
        }

        protected abstract ClassVisitor getClassMapper(ClassVisitor downstream);

    }

}
