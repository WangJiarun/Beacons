package arfox.beacons;

import arfox.beacons.blocks.BeaconFireBlock;
import arfox.beacons.blocks.BeaconFireEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(BeaconsMain.MODID)
public class BeaconsMain {

    public static final String MODID = "beacons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BeaconsMain.MODID);
    public static final RegistryObject<Block> BEACON_FIRE = BLOCKS.register("beacon_fire", BeaconFireBlock::new);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BeaconsMain.MODID);
    public static final RegistryObject<Item> BEACON_FIRE_BLOCK_ITEM = ITEMS.register("beacon_fire", () -> new BlockItem(BEACON_FIRE.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, BeaconsMain.MODID);
    public static final RegistryObject<BlockEntityType<BeaconFireEntity>> BEACON_FIRE_ENTITY = BLOCK_ENTITIES.register("beacon_fire", () -> BlockEntityType.Builder.of(BeaconFireEntity::new, BEACON_FIRE.get()).build(null));

    public BeaconsMain() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(BeaconsMain.BEACON_FIRE.get(), RenderType.cutout());
        });
    }
}
