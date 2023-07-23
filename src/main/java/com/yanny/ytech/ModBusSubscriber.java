package com.yanny.ytech;

import com.yanny.ytech.machine.container.MachineContainerMenu;
import com.yanny.ytech.machine.screen.ScreenFactory;
import com.yanny.ytech.network.kinetic.renderer.KineticRenderer;
import com.yanny.ytech.registration.Registration;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = YTechMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusSubscriber {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Registration.REGISTRATION_HOLDER.machine().forEach((machine, tierMap) -> tierMap.forEach((tier, holder) ->
                    MenuScreens.register((MenuType<MachineContainerMenu>) holder.menuType().get(), ScreenFactory::create)));
        });
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        Registration.REGISTRATION_HOLDER.kineticNetwork().forEach((blockType, materialMap) -> materialMap.forEach((material, holder) -> event.registerBlockEntityRenderer(holder.entityType().get(), KineticRenderer::new)));
    }
}
