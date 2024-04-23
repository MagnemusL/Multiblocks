package net.lindholm.multiblocks.block.entity;

import net.lindholm.multiblocks.MultiBlockMod;
import net.lindholm.multiblocks.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MultiBlockMod.MODID);

    public static final RegistryObject<BlockEntityType<MasterBlockEntity>> MASTER_BE = BLOCK_ENTITIES.register("master_block_entity", () -> BlockEntityType.Builder.of(MasterBlockEntity::new, ModBlocks.MASTER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
