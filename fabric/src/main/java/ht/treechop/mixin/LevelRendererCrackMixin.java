package ht.treechop.mixin;

import ht.treechop.client.model.ChoppedLogBakedModel;
import ht.treechop.client.model.ChoppedLogCrackContext;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Map;

/**
 * Intercepts the end of {@code LevelRenderer.extractBlockDestroyAnimation} — the last
 * point in each frame where the live {@code ClientLevel} is accessible alongside the
 * freshly built {@code blockBreakingRenderStates} list — and pre-populates
 * {@link ChoppedLogCrackContext#QUEUE} with entity snapshots for every chopped log
 * that is currently being mined.
 *
 * <p>Fabric's {@code BlockFeatureRendererMixin} later calls
 * {@code BlockStateModel.emitQuads(emitter, EMPTY, ZERO, AIR, ...)} on our model,
 * discarding the real block position. The queue lets {@link ht.treechop.client.model.FabricChoppedLogBakedModel}
 * still emit the correct trimmed geometry for the crack overlay.</p>
 */
@Mixin(LevelRenderer.class)
public class LevelRendererCrackMixin {

    @Shadow
    private ClientLevel level;

    @Inject(method = "extractBlockDestroyAnimation", at = @At("TAIL"))
    private void populateChoppedLogCrackQueue(Camera camera, LevelRenderState renderState, CallbackInfo ci) {
        if (level == null) return;

        ArrayDeque<ChoppedLogCrackContext.Snapshot> queue = ChoppedLogCrackContext.QUEUE.get();
        queue.clear();

        for (BlockBreakingRenderState brs : renderState.blockBreakingRenderStates) {
            BlockPos pos = brs.blockPos();
            if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
                BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
                Map<Direction, BlockState> strippedNeighbors = ChoppedLogBakedModel.getStrippedNeighbors(level, pos, entity);
                queue.offer(new ChoppedLogCrackContext.Snapshot(
                        strippedState, entity.getShape(), entity.getRadius(), strippedNeighbors));
            }
        }
    }
}
