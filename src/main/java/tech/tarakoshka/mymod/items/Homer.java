package tech.tarakoshka.mymod.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import tech.tarakoshka.mymod.MySounds;
import tech.tarakoshka.mymod.Mymod;

public class Homer extends Item {

    public Homer(Properties settings) {
        super(settings.durability(100).stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        shootHomer(level, player, 20, 30);


        var item = player.getItemInHand(interactionHand);
        if (item.nextDamageWillBreak()) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    MySounds.DEFLATE,
                    SoundSource.PLAYERS,
                    1.0F, // volume
                    1.0F  // pitch
            );

            item.shrink(1); }
        item.setDamageValue(item.getDamageValue() + 1);

        return InteractionResult.SUCCESS;
    }

    private void shootHomer(Level level, Player player, int maxBlocksDestroyed, int homerLength) {
        if (level.isClientSide()) {
            return;
        }
        var serverLevel = (ServerLevel) level;
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eyePos.add(look.multiply(homerLength, homerLength, homerLength));
        var hit = level.clip(new ClipContext(eyePos, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
        var blocksDestroyed = 0;
        damageEntitiesInBeam(serverLevel, player, eyePos, end);
        spawnLaserParticles(serverLevel, eyePos, end);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                MySounds.SHOOT,
                SoundSource.PLAYERS,
                1.0F, // volume
                1.0F  // pitch
        );

        while (hit.getType() != HitResult.Type.MISS && blocksDestroyed < maxBlocksDestroyed) {
            var type = hit.getType();
            switch (type) {
                case HitResult.Type.BLOCK -> {
                    level.destroyBlock(hit.getBlockPos(), false);
                }
                case  HitResult.Type.ENTITY -> {}
            }
            blocksDestroyed++;
            hit = level.clip(new ClipContext(eyePos, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
        }
    }

    private void damageEntitiesInBeam(ServerLevel serverLevel, Player player, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        AABB searchBox = new AABB(
                Math.min(start.x, end.x) - 1,
                Math.min(start.y, end.y) - 1,
                Math.min(start.z, end.z) - 1,
                Math.max(start.x, end.x) + 1,
                Math.max(start.y, end.y) + 1,
                Math.max(start.z, end.z) + 1
        );

        for (Entity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (entity instanceof LivingEntity) {
                Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(start);
                double projection = toEntity.dot(direction);

                if (projection > 0 && projection < distance) {
                    Vec3 closest = start.add(direction.multiply(projection, projection, projection));
                    if (closest.distanceTo(entity.position()) < 2.0) {
                        entity.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 12.0F);
                    }
                }
            }
        }
    }

    private void spawnLaserParticles(ServerLevel serverLevel, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        for (double d = 2; d < distance; d += 0.5f) {
            Vec3 particlePos = start.add(direction.multiply(d, d, d));

            serverLevel.sendParticles(
                    Mymod.MILK_PARTICLE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0, 0, 0,
                    0
            );
            serverLevel.sendParticles(
                    Mymod.MILK_PARTICLE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    2,
                    .5, .5, .5,
                    .5
            );
        }
    }
}
