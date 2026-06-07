package net.hellomouse.createrailgun.block;

import net.hellomouse.createrailgun.Config;
import net.hellomouse.createrailgun.block.be.RailgunCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class RailgunRailBlock extends Block {
    public static final IntegerProperty BRIGHTNESS = IntegerProperty.create("brightness", 0, 3);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public RailgunRailBlock(Properties properties) {
        super(properties.noOcclusion().lightLevel(state -> state.getValue(BRIGHTNESS) * 4));
        this.registerDefaultState(this.stateDefinition.any().setValue(BRIGHTNESS, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BRIGHTNESS, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        notifyCore(state, level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            notifyCore(state, level, pos);
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private void notifyCore(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);
        for (int i = 1; i <= Config.MAX_RAILS.getAsInt() + 1; i++) {
            BlockPos checkPos = pos.relative(facing.getOpposite(), i);
            if (level.getBlockEntity(checkPos) instanceof RailgunCoreBlockEntity core) {
                core.updateMultiblock();
                break;
            }
            checkPos = pos.relative(facing, i);
            if (level.getBlockEntity(checkPos) instanceof RailgunCoreBlockEntity core) {
                core.updateMultiblock();
                break;
            }
        }
    }
}
