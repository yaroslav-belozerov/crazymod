package tech.tarakoshka.mending_mastery.items.wand;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.tarakoshka.mending_mastery.MyMod;
import tech.tarakoshka.mending_mastery.MySounds;
import tech.tarakoshka.mending_mastery.data.MyDataComponents;

import static java.lang.Math.max;

public class MagicWand extends Item {
    public MagicWand(Properties settings) {
        super(settings.durability(100).stacksTo(1));
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {
            itemStack.set(MyDataComponents.WAND_SHOOTING, true);
            int ticksUsed = this.getUseDuration(itemStack, livingEntity) - i;
            if (ticksUsed >= 40) {
                itemStack.set(MyDataComponents.WAND_CHARGED, true);
            }
        }
        super.onUseTick(level, livingEntity, itemStack, i);
    }


    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
        var isCharged = itemStack.get(MyDataComponents.WAND_CHARGED);
        itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE.equals(isCharged));
        super.inventoryTick(itemStack, serverLevel, entity, equipmentSlot);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        return itemStack;
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {
            var isCharged = itemStack.get(MyDataComponents.WAND_CHARGED);
            var spell = itemStack.get(MyDataComponents.VALID_SPELL);
            if (Boolean.TRUE.equals(isCharged) && WandShapes.shapes.containsKey(spell)) {
                wandShoot(level, player, 20, 30, itemStack);
            }
            itemStack.set(MyDataComponents.WAND_SHOOTING, false);
            itemStack.set(MyDataComponents.WAND_CHARGED, false);
            itemStack.set(MyDataComponents.VALID_SPELL, "");
        }
        return super.releaseUsing(itemStack, level, livingEntity, i);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 300;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    private void wandShoot(Level level, Player player, int maxBlocksDestroyed, int range, ItemStack itemStack) {
        if (level.isClientSide()) {
            return;
        }
        var serverLevel = (ServerLevel) level;
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eyePos.add(look.multiply(range, range, range));
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

        if (itemStack.nextDamageWillBreak()) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    MySounds.DEFLATE,
                    SoundSource.PLAYERS,
                    2.0F, // volume
                    1.0F  // pitch
            );

            itemStack.shrink(1);
        }
        itemStack.setDamageValue(itemStack.getDamageValue() + 1);

        while (hit.getType() == HitResult.Type.BLOCK && blocksDestroyed < maxBlocksDestroyed) {
            level.destroyBlock(hit.getBlockPos(), false);
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

        for (double d = .5; d < distance; d += .5) {
            Vec3 particlePos = start.add(direction.multiply(d, d, d));

            serverLevel.sendParticles(
                    MyMod.MILK_PARTICLE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0, 0, 0,
                    0
            );
            serverLevel.sendParticles(
                    MyMod.MILK_PARTICLE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    2,
                    direction.x / 3, direction.y / 3, direction.z / 3,
                    .1
            );
        }
    }

}
