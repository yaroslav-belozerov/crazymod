package tech.tarakoshka.mending_mastery;

import com.mojang.blaze3d.platform.ScreenManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.mixin.screen.HandledScreenMixin;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tech.tarakoshka.mending_mastery.blocks.ModBlocks;
import tech.tarakoshka.mending_mastery.blocks.infuser.Infuser;
import tech.tarakoshka.mending_mastery.particles.MilkParticle;
import tech.tarakoshka.mending_mastery.screens.InfuserScreen;

public class MyModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(MyMod.MILK_PARTICLE, MilkParticle.Factory::new);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, "before_chat"), new ModHud());
        BlockRenderLayerMap.putBlock(Infuser.INFUSER, ChunkSectionLayer.TRANSLUCENT);
        MenuScreens.register(Infuser.INFUSER_MENU, InfuserScreen::new);
	}
}