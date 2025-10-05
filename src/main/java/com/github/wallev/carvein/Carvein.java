package com.github.wallev.carvein;

import com.github.wallev.carvein.init.ModEntities;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Carvein.MOD_ID)
public class Carvein {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "carvein";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Carvein() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.MEMORY_MODULE_TYPES.register(modEventBus);
    }
}
