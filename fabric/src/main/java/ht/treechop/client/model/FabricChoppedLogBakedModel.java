package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel {
    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
            Map<Direction, BlockState> strippedNeighbors = getStrippedNeighbors(level, pos, entity);
            emitQuads(emitter, strippedState, entity.getShape(), entity.getRadius(), random, strippedNeighbors);
        } else {
            // Crack animation context: Fabric's BlockFeatureRendererMixin calls emitQuads with
            // EMPTY level / ZERO pos / AIR state, so the block entity lookup above returns null.
            // LevelRendererCrackMixin pre-populates the queue during extractBlockDestroyAnimation
            // (where the live level is still accessible), so we poll the real entity data here.
            ChoppedLogCrackContext.Snapshot snapshot = ChoppedLogCrackContext.QUEUE.get().poll();
            if (snapshot != null) {
                emitQuads(emitter, snapshot.strippedState(), snapshot.shape(), snapshot.radius(), random, snapshot.strippedNeighbors());
            } else {
                wrapped.emitQuads(emitter, level, pos, state, random, cullTest);
            }
        }
    }
}

