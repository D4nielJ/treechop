package ht.treechop.client.model;

import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Map;

/**
 * Per-frame FIFO queue that passes chopped-log entity data from
 * {@code LevelRenderer.extractBlockDestroyAnimation} (where the live level is accessible)
 * to {@code FabricChoppedLogBakedModel.emitQuads} (called later during crack animation
 * rendering with a fake EMPTY level / ZERO pos by Fabric's BlockFeatureRendererMixin).
 *
 * <p>Cleared and repopulated every frame by LevelRendererCrackMixin. Entries are pushed in
 * the same order that Fabric's mixin will call {@code emitQuads} on our model, so the
 * FIFO discipline guarantees correct matching even when multiple blocks break at once.</p>
 */
public class ChoppedLogCrackContext {

    public static final ThreadLocal<ArrayDeque<Snapshot>> QUEUE =
            ThreadLocal.withInitial(ArrayDeque::new);

    public record Snapshot(
            BlockState strippedState,
            ChoppedLogShape shape,
            int radius,
            Map<Direction, BlockState> strippedNeighbors
    ) {}
}
