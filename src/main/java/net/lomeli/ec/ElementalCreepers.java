package net.lomeli.ec;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.lomeli.ec.core.CommonProxy;
import net.lomeli.ec.core.Config;
import net.lomeli.ec.core.EntityRegistering;
import net.lomeli.ec.core.VersionChecker;
import net.lomeli.ec.entity.EntityFriendlyCreeper;
import net.lomeli.ec.entity.EntityGhostCreeper;
import net.lomeli.ec.entity.IIllusion;
import net.lomeli.ec.lib.ECVars;
import net.lomeli.ec.lib.Strings;

@Mod(modid = Strings.MOD_ID, name = Strings.MOD_NAME, version = Strings.VERSION, guiFactory = Strings.FACTORY)
public class ElementalCreepers {

    @SidedProxy(clientSide = Strings.CLIENT, serverSide = Strings.COMMON)
    public static CommonProxy proxy;

    public static Config config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Config(new Configuration(event.getSuggestedConfigurationFile()));
        config.loadConfig();
    }

    @Mod.EventHandler
    public void Init(FMLInitializationEvent event) {
        proxy.registerRenders();

        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(config);
        EntityRegistering.loadEntities();
    }

    @Mod.EventHandler
    public void postLoad(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("VersionChecker")) {
            VersionChecker.checkForUpdates();
            if (VersionChecker.needUpdate())
                VersionChecker.sendMessage();
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        boolean activate = false;

        if (event.source.getDamageType().equals("player"))
            activate = true;

        if (event.source.getSourceOfDamage() instanceof EntityArrow) {
            if (((EntityArrow) event.source.getSourceOfDamage()).shootingEntity != null) {
                if (((EntityArrow) event.source.getSourceOfDamage()).shootingEntity instanceof EntityPlayer)
                    activate = true;
            }
        }

        if (activate && event.entityLiving != null && (event.entityLiving instanceof EntityCreeper) && !(event.entityLiving instanceof EntityGhostCreeper)
                && !(event.entityLiving instanceof EntityFriendlyCreeper)) {
            if (event.entityLiving instanceof IIllusion) {
                if (((IIllusion) event.entityLiving).isIllusion())
                    return;
            }

            if (event.entityLiving.worldObj.rand.nextInt(100) < ECVars.ghostCreeperChance) {
                EntityGhostCreeper ghost = new EntityGhostCreeper(event.entityLiving.worldObj);
                ghost.setLocationAndAngles(event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, event.entityLiving.rotationYaw, event.entityLiving.rotationPitch);
                event.entityLiving.worldObj.spawnEntityInWorld(ghost);
            }
        }
    }
}
