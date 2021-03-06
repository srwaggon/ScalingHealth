package net.silentchaos512.scalinghealth;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.conditions.LootConditionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.PetHealthCapability;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.client.KeyBinds.KeyManager;
import net.silentchaos512.scalinghealth.client.gui.DebugOverlay;
import net.silentchaos512.scalinghealth.client.gui.difficulty.DifficultyMeter;
import net.silentchaos512.scalinghealth.client.gui.health.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.command.ModCommands;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.event.DamageScaling;
import net.silentchaos512.scalinghealth.event.PetEventHandler;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.init.ModLoot;
import net.silentchaos512.scalinghealth.loot.TableGlobalModifier;
import net.silentchaos512.scalinghealth.loot.conditions.EntityGroupCondition;
import net.silentchaos512.scalinghealth.loot.conditions.SHMobProperties;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.world.SHWorldFeatures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Random;

@Mod(ScalingHealth.MOD_ID)
public class ScalingHealth {
    public static final String MOD_ID = "scalinghealth";
    public static final String MOD_NAME = "Scaling Health";
    public static final String VERSION = "3.0.2";

    public static final Random random = new Random();

    public static final ItemGroup SH = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.HEART_CRYSTAL.asItem());
        }
    };

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public ScalingHealth() {
        Config.init();
        MinecraftForge.EVENT_BUS.register(PetEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DamageScaling.INSTANCE);

        Network.init();
        ModLoot.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(GlobalLootModifierSerializer.class,this::registerLootModSerializers);
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);

        Registry.register(Registry.LOOT_CONDITION_TYPE, SHMobProperties.NAME, new LootConditionType(new SHMobProperties.Serializer()));
        Registry.register(Registry.LOOT_CONDITION_TYPE, EntityGroupCondition.NAME, new LootConditionType(new EntityGroupCondition.Serializer()));
    }

    private void registerLootModSerializers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event){
        event.getRegistry().register(new TableGlobalModifier.Serializer().setRegistryName(getId("table_loot_mod")));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        DifficultyAffectedCapability.register();
        DifficultySourceCapability.register();
        PlayerDataCapability.register();
        PetHealthCapability.register();
    }

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ModCommands.registerAll(event.getServer().getCommandManager().getDispatcher());
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientForge {
        static {
            MinecraftForge.EVENT_BUS.register(HeartDisplayHandler .INSTANCE);
            MinecraftForge.EVENT_BUS.register(DifficultyMeter.INSTANCE);

            DebugOverlay.init();
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            KeyManager.registerBindings();
        }
    }

    public static String getVersion() {
        return getVersion(false);
    }

    public static String getVersion(boolean correctInDev) {
        Optional<? extends ModContainer> o = ModList.get().getModContainerById(MOD_ID);
        if (o.isPresent()) {
            String str = o.get().getModInfo().getVersion().toString();
            if (correctInDev && "NONE".equals(str))
                return VERSION;
            return str;
        }
        return "0.0.0";
    }

    public static ResourceLocation getId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
