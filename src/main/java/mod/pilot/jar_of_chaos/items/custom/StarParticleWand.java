package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.particles.JarParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StarParticleWand extends Item {
    public StarParticleWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        if (level instanceof ServerLevel server){
            RandomSource rand = player.getRandom();
            server.sendParticles(JarParticles.STAR_PARTICLE.get(), player.getX(), player.getY(), player.getZ(),
                    4,
                    rand.nextDouble() * (rand.nextBoolean() ? 1 : -1),
                    rand.nextDouble() * (rand.nextBoolean() ? 1 : -1),
                    rand.nextDouble() * (rand.nextBoolean() ? 1 : -1),
                    rand.nextDouble());
        }
        return super.use(level, player, pUsedHand);
    }
}
