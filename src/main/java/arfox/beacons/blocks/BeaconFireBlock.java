package arfox.beacons.blocks;

import arfox.beacons.BeaconsMain;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BeaconFireBlock extends BaseEntityBlock implements IForgeBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty EMPTY = BooleanProperty.create("empty");

    public static final String MESSAGE_CAMPFIRE_REQUIRED = "message.campfire_required";

    public BeaconFireBlock() {
        super(
                BlockBehaviour.
                        Properties.of(Material.STONE)
                        .requiresCorrectToolForDrops()
                        .strength(3.5F)
                        .sound(SoundType.STONE)
                        .lightLevel((state) -> state.getValue(LIT) ? 15 : 0)
                        .noOcclusion()
        );

        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(EMPTY, true));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (pLevel.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack inHand = pPlayer.getItemInHand(pHand);

        if (pState.getValue(EMPTY)) {
            if (inHand.is(Items.CAMPFIRE)) {
                inHand.shrink(1);
                pLevel.setBlock(pPos, pState.setValue(EMPTY, false), 2);
                return InteractionResult.CONSUME;
            }
        } else {
            Optional<BeaconFireEntity> optional = pLevel.getBlockEntity(pPos, BeaconsMain.BEACON_FIRE_ENTITY.get());
            optional.ifPresent(BeaconFireEntity::operateBeaconsChain);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Random random) {
        if (!level.isClientSide()) return;
        if (!state.getValue(LIT)) return;

        if (random.nextInt(10) == 0) level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, random.nextFloat() / 2.0F, 5.0E-5D, random.nextFloat() / 2.0F);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(EMPTY, LIT);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter reader, List<Component> list, @NotNull TooltipFlag flags) {
        list.add(new TranslatableComponent(MESSAGE_CAMPFIRE_REQUIRED).withStyle(ChatFormatting.BLUE));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(LIT, false).setValue(EMPTY, true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return Block.box(0, 0, 0, 16, 12, 16);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new BeaconFireEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return (pState.getValue(LIT) && pLevel.isClientSide()) ? createTickerHelper(pBlockEntityType, BeaconsMain.BEACON_FIRE_ENTITY.get(), BeaconFireEntity::tick) : null;
    }
}
