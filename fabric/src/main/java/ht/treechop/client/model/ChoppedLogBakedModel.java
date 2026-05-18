package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.tuber.math.Vector3;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ChoppedLogBakedModel extends WrapperBlockStateModel {

    private static BlockState getStrippedNeighbor(BlockAndTintGetter level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        return ChopUtil.getStrippedState(level, pos, level.getBlockState(neighborPos));
    }

    public static Map<Direction, BlockState> getStrippedNeighbors(BlockAndTintGetter level, BlockPos pos, ChoppedLogBlock.MyEntity entity) {
        if (entity.getOriginalState().isSolidRender()) {
            return entity.streamSolidSides(level, pos).collect(Collectors.toMap(
                    side -> side,
                    side -> getStrippedNeighbor(level, pos, side)
            ));
        } else {
            return Collections.emptyMap();
        }
    }

    public static BlockStateModel getBlockModel(BlockState blockState) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
    }

    protected static List<BakedQuad> getBlockQuads(BlockState blockState, Direction side, RandomSource rand) {
        BlockStateModel model = getBlockModel(blockState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(rand, parts);
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            quads.addAll(part.getQuads(side));
        }
        return quads;
    }

    protected void emitQuads(QuadEmitter emitter, BlockState strippedState, ChoppedLogShape shape, int radius, RandomSource random, Map<Direction, BlockState> strippedNeighbors) {
        final Direction[] allDirections = { Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null };
        AABB box = shape.getBoundingBox(radius);
        Vector3 mins = new Vector3(box.minX, box.minY, box.minZ);
        Vector3 maxes = new Vector3(box.maxX, box.maxY, box.maxZ);

        for (Direction side : allDirections) {
            for (BakedQuad quad : getBlockQuads(strippedState, side, random)) {
                emitter.fromBakedQuad(quad);
                ModelUtil.trimQuadInEmitter(emitter, mins, maxes);
                emitter.emit();
            }
        }

        for (Map.Entry<Direction, BlockState> entry : strippedNeighbors.entrySet()) {
            Direction side = entry.getKey();
            BlockState strippedNeighbor = entry.getValue();
            Vec3i normal = side.getUnitVec3i().multiply(16);
            float dx = normal.getX() / 16f;
            float dy = normal.getY() / 16f;
            float dz = normal.getZ() / 16f;

            for (BakedQuad quad : getBlockQuads(strippedNeighbor, side.getOpposite(), random)) {
                emitter.fromBakedQuad(quad);
                ModelUtil.translateQuadInEmitter(emitter, dx, dy, dz);
                emitter.emit();
            }
        }
    }

    // Fallback collectParts: delegate to wrapped (oak log) model for crack animation
    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        wrapped.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            BlockState originalState = entity.getOriginalState();
            return getBlockModel(originalState).particleMaterial(level, pos, originalState);
        }
        return super.particleMaterial(level, pos, state);
    }

    public void setWrapped(BlockStateModel model) {
        this.wrapped = model;
    }

}
