package com.mekeng.github.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mekeng.github.common.ItemAndBlocks;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.MekanismFluids;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class DummyGasModel implements IModel {

    @SuppressWarnings("deprecation")
    protected static final ItemCameraTransforms CAMERA_TRANSFORMS = new ItemCameraTransforms(
            new ItemTransformVec3f(new Vector3f(0F, 0F, 0F), new Vector3f(0F, 0.1875F, 0.0625F), new Vector3f(0.55F, 0.55F, 0.55F)),
            new ItemTransformVec3f(new Vector3f(0F, 0F, 0F), new Vector3f(0F, 0.1875F, 0.0625F), new Vector3f(0.55F, 0.55F, 0.55F)),
            new ItemTransformVec3f(new Vector3f(0F, -90F, 25F), new Vector3f(0.070625F, 0.2F, 0.070625F), new Vector3f(0.68F, 0.68F, 0.68F)),
            new ItemTransformVec3f(new Vector3f(0F, -90F, 25F), new Vector3f(0.070625F, 0.2F, 0.070625F), new Vector3f(0.68F, 0.68F, 0.68F)),
            new ItemTransformVec3f(new Vector3f(0F, 180F, 0F), new Vector3f(0F, 0.8125F, 0.4375F), new Vector3f(1F, 1F, 1F)),
            ItemTransformVec3f.DEFAULT,
            new ItemTransformVec3f(new Vector3f(0F, 0F, 0F), new Vector3f(0F, 0.125F, 0F), new Vector3f(0.5F, 0.5F, 0.5F)),
            new ItemTransformVec3f(new Vector3f(0F, 180F, 0F), new Vector3f(0F, 0F, 0F), new Vector3f(1F, 1F, 1F)));

    @Override
    @Nonnull
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> textureBakery) {
        return new BakedGasModel(state, format);
    }

    public static class Loader implements ICustomModelLoader {

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
            // NO-OP
        }

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            return modelLocation.compareTo(ItemAndBlocks.DUMMY_GAS.getModelPath()) == 0;
        }

        @Override
        @Nonnull
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
            return new DummyGasModel();
        }

    }

    protected static class BakedGasModel implements IBakedModel {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        protected final Optional<TRSRTransformation> modelTransform;
        protected final VertexFormat vertexFormat;
        protected final ItemOverrideList overrides;
        private final IBakedModel defaultOverride;

        public BakedGasModel(IModelState modelState, VertexFormat vertexFormat) {
            this.modelTransform = modelState.apply(Optional.empty());
            this.vertexFormat = vertexFormat;
            this.overrides = this.genOverrides();
            this.defaultOverride = this.genDefaultOverrides();
        }

        @Override
        @Nonnull
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return defaultOverride.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return defaultOverride.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return defaultOverride.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return defaultOverride.isBuiltInRenderer();
        }

        @Override
        @Nonnull
        public TextureAtlasSprite getParticleTexture() {
            return defaultOverride.getParticleTexture();
        }

        @Override
        public boolean isAmbientOcclusion(@Nonnull IBlockState state) {
            return defaultOverride.isAmbientOcclusion(state);
        }

        @SuppressWarnings("deprecation")
        @Override
        @Nonnull
        public ItemCameraTransforms getItemCameraTransforms() {
            return defaultOverride.getItemCameraTransforms();
        }

        @Override
        @Nonnull
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
            return defaultOverride.handlePerspective(cameraTransformType);
        }

        @Override
        @Nonnull
        public ItemOverrideList getOverrides() {
            return overrides;
        }

        protected ItemOverrideList genOverrides() {
            return new OverrideCache();
        }

        protected IBakedModel genDefaultOverrides() {
            return ((OverrideCache) this.overrides).resolve(new GasStack(MekanismFluids.Hydrogen, 1000));
        }

        private class OverrideCache extends ItemOverrideList {

            private final Cache<Gas, OverrideModel> cache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

            OverrideCache() {
                super(Collections.emptyList());
            }

            @Override
            @Nonnull
            public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack,
                                               @Nullable World world, @Nullable EntityLivingBase entity) {
                if (!(stack.getItem() == ItemAndBlocks.DUMMY_GAS)) {
                    return originalModel;
                }
                GasStack gas = ItemAndBlocks.DUMMY_GAS.getGasStack(stack);
                return gas != null ? resolve(gas) : originalModel;
            }

            OverrideModel resolve(GasStack gas) {
                try {
                    return cache.get(gas.getGas(), () -> new OverrideModel(gas));
                } catch (ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            }

            class OverrideModel implements IBakedModel {

                private final TextureAtlasSprite texture;
                private final List<BakedQuad> quads;

                OverrideModel(GasStack gasStack) {
                    this.texture = gasStack.getGas().getSprite();
                    this.quads = ItemLayerModel.getQuadsForSprite(1, texture, vertexFormat, modelTransform);
                }

                @Override
                @Nonnull
                public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
                    return quads;
                }

                @Override
                public boolean isAmbientOcclusion() {
                    return false;
                }

                @Override
                public boolean isGui3d() {
                    return false;
                }

                @Override
                public boolean isBuiltInRenderer() {
                    return false;
                }

                @Override
                @Nonnull
                public TextureAtlasSprite getParticleTexture() {
                    return texture;
                }

                @Override
                @Nonnull
                @SuppressWarnings("deprecation")
                public ItemCameraTransforms getItemCameraTransforms() {
                    return CAMERA_TRANSFORMS;
                }

                @Override
                @Nonnull
                public ItemOverrideList getOverrides() {
                    return OverrideCache.this;
                }

            }

        }

    }

}