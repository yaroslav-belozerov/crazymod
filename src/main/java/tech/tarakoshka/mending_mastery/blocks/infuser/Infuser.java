package tech.tarakoshka.mending_mastery.blocks.infuser;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tech.tarakoshka.mending_mastery.MyMod;
import tech.tarakoshka.mending_mastery.blocks.ModBlocks;

public class Infuser extends BaseEntityBlock {
    public static final Block INFUSER = ModBlocks.register("infuser", Infuser::create, BlockBehaviour.Properties.of().sound(SoundType.STONE), true);

    public static final BlockEntityType<InfuserEntity> INFUSER_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, "infuser_entity"),
                    FabricBlockEntityTypeBuilder.create(InfuserEntity::new,
                            INFUSER).build());


    public static final MenuType<InfuserMenu> INFUSER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, "infuser_menu"),
            new MenuType<>(InfuserMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static void init() {}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, INFUSER_ENTITY, InfuserEntity::tick);
    }

    protected Infuser(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(Infuser::new);
    }

    public static Infuser create(Properties properties) {
        return new Infuser(properties);
    }

    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            this.openContainer(level, blockPos, player);
        }

        return InteractionResult.SUCCESS;
    }

    protected void openContainer(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        player.openMenu((MenuProvider)blockEntity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new InfuserEntity(blockPos, blockState);
    }
}

