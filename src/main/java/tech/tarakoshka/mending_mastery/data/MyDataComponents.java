package tech.tarakoshka.mending_mastery.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import tech.tarakoshka.mending_mastery.MyMod;

public class MyDataComponents {

    public static final DataComponentType<Boolean> WAND_SHOOTING = register(
            "wand_shooting",
            builder -> builder.persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
    );
    public static final DataComponentType<Boolean> WAND_CHARGED = register(
            "wand_charged",
            builder -> builder.persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
    );
    public static final DataComponentType<String> VALID_SPELL = register(
            "valid_spell",
            builder -> builder.persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    private static <T> DataComponentType<T> register(
            String name,
            java.util.function.UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, name),
                builder.apply(DataComponentType.builder()).build()
        );
    }

    public static void register() {
    }
}
