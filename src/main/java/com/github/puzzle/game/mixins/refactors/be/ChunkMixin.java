package com.github.puzzle.game.mixins.refactors.be;

import com.badlogic.gdx.graphics.Camera;
import com.github.puzzle.game.blockentities.ExtendedBlockEntity;
import com.github.puzzle.game.blockentities.INeighborUpdateListener;
import com.github.puzzle.game.blockentities.IRenderable;
import com.github.puzzle.game.util.DirectionUtil;
import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.constants.Direction;
import finalforeach.cosmicreach.util.IPoint3DMap;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements IRenderable {
    @Shadow
    IPoint3DMap<BlockEntity> blockEntities;

    @Shadow public Region region;

    @Shadow public int blockX;

    @Shadow public int blockY;

    @Shadow public int blockZ;

    @Shadow public int chunkX;

    @Shadow public int chunkY;

    @Shadow public int chunkZ;

    @Override
    public void onRender(Camera camera) {
        if(blockEntities != null)
            for (int x = 0; x < Chunk.CHUNK_WIDTH; x++) {
                for (int y = 0; y < Chunk.CHUNK_WIDTH; y++) {
                    for (int z = 0; z < Chunk.CHUNK_WIDTH; z++) {
                        if (blockEntities.get(x, y, z) instanceof IRenderable renderable) {
                            renderable.onRender(camera);
                        }
                    }
                }
            }
    }

    @Inject(method = "setBlockEntity", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/blockentities/BlockEntity;onRemove()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void destroyBlockEntity(BlockState blockState, int localX, int localY, int localZ, CallbackInfoReturnable<BlockEntity> cir, @Local BlockEntity blockEntity) {
        if(blockEntity instanceof ExtendedBlockEntity extendedBlockEntity) {
            extendedBlockEntity.x = 0;
            extendedBlockEntity.y = 0;
            extendedBlockEntity.z = 0;
        }
    }

    @Inject(method = "setBlockEntity", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/blockentities/BlockEntity;onCreate(Lfinalforeach/cosmicreach/blocks/BlockState;)V", shift = At.Shift.BEFORE))
    private void initializeBlockEntity(BlockState blockState, int localX, int localY, int localZ, CallbackInfoReturnable<BlockEntity> cir, @Local BlockEntity blockEntity) {
        if(blockEntity instanceof ExtendedBlockEntity extendedBlockEntity) {
            extendedBlockEntity.initialize((Chunk) (Object) this, localX, localY, localZ);
        }
    }

    @Inject(method = "setBlockEntityDirect", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/blockentities/BlockEntity;onRemove()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void destroyBlockEntity2(BlockState blockState, BlockEntity blockEntity, int localX, int localY, int localZ, CallbackInfo ci) {
        if(blockEntity instanceof ExtendedBlockEntity extendedBlockEntity) {
            extendedBlockEntity.x = 0;
            extendedBlockEntity.y = 0;
            extendedBlockEntity.z = 0;
        }
    }

    @Inject(method = "setBlockEntityDirect", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/blockentities/BlockEntity;onCreate(Lfinalforeach/cosmicreach/blocks/BlockState;)V", shift = At.Shift.BEFORE))
    private void initializeBlockEntity2(BlockState blockState, BlockEntity blockEntity, int localX, int localY, int localZ, CallbackInfo ci) {
        if(blockEntity instanceof ExtendedBlockEntity extendedBlockEntity) {
            extendedBlockEntity.initialize((Chunk) (Object) this, localX, localY, localZ);
        }
    }

    @Inject(method = "setBlockEntity", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/blockentities/BlockEntity;onCreate(Lfinalforeach/cosmicreach/blocks/BlockState;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fireNeighbors(BlockState blockState, int localX, int localY, int localZ, CallbackInfoReturnable<BlockEntity> cir, @Local BlockEntity blockEntity) {
        for(Direction face : Direction.values()) {
            int neighborX = blockX + localX + face.getXOffset();
            int neighborY = blockY + localY + face.getYOffset();
            int neighborZ = blockZ + localZ + face.getZOffset();

            int cx = Math.floorDiv(neighborX, 16);
            int cy = Math.floorDiv(neighborY, 16);
            int cz = Math.floorDiv(neighborZ, 16);

            boolean neighborIsInThisChunk = (cx == chunkX && cy == chunkY && cz == chunkZ);
            Chunk neighbor = null;
            if(neighborIsInThisChunk) neighbor = (Chunk) (Object) this;
            else if(region != null && region.zone != null) neighbor = region.zone.getChunkAtBlock(neighborX, neighborY, neighborZ);
            else System.err.println("Region or Zone is not initialized, problems will occur");

            if(neighbor != null) {
                int neighborLocalX = neighborX - neighbor.blockX;
                int neighborLocalY = neighborY - neighbor.blockY;
                int neighborLocalZ = neighborZ - neighbor.blockZ;

                BlockEntity neighborEntity = neighbor.getBlockEntity(neighborLocalX, neighborLocalY, neighborLocalZ);

                if(blockEntity instanceof INeighborUpdateListener neighborChangeListener) {
                    BlockState neighborBlockState = neighbor.getBlockState(neighborLocalX, neighborLocalY, neighborLocalZ);
                    neighborChangeListener.onNeighborUpdate(face, neighborBlockState, neighborEntity);
                }

                if(neighborEntity instanceof INeighborUpdateListener neighborChangeListener) {
                    neighborChangeListener.onNeighborUpdate(DirectionUtil.opposite(face), blockState, blockEntity);
                }
            }

        }
    }

}