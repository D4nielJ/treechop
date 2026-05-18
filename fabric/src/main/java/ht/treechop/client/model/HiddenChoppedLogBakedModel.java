package ht.treechop.client.model;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class HiddenChoppedLogBakedModel extends ChoppedLogBakedModel {
    // Emits no quads - used when Sodium is present without Indium
    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        // Intentionally empty - hidden rendering uses block entity renderer instead
    }
}

