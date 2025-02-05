package mod.pilot.jar_of_chaos.blocks.custom;

import mod.pilot.jar_of_chaos.items.JarItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class SlimeLayerBlock extends SnowLayerBlock {
    public SlimeLayerBlock(Properties pProperties) {
        super(pProperties);
    }
    public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState bState, @NotNull BlockGetter level, @NotNull BlockPos bPos) {
        return Shapes.empty();
    }
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState bState, @NotNull BlockGetter level,
                                              @NotNull BlockPos bPos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    public boolean canSurvive(@NotNull BlockState bState, @NotNull LevelReader level, @NotNull BlockPos bPos) {
        return true;
    }

    @Override
    public float getFriction() {
        return 1.0f;
    }
    @Override
    public float getJumpFactor() {
        return 0.75f;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos bPos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.ARROW)){
            stack.shrink(1);
            player.addItem(new ItemStack(JarItems.SLIME_ARROW.get()));
            player.playSound(SoundEvents.SLIME_SQUISH);
            if (state.getValue(LAYERS) <= 1) level.setBlock(bPos, Blocks.AIR.defaultBlockState(), 3);
            else level.setBlock(bPos, state.setValue(LAYERS, state.getValue(LAYERS) - 1), 3);

            return InteractionResult.CONSUME_PARTIAL;
        } else return InteractionResult.FAIL;
    }
}
