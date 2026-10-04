package com.kf;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent ADVANCEMENT = register("music.other.advancement");
    public static final SoundEvent ADVANCEMENT_CHALLENGE = register("music.other.challenge");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Discs.MOD_ID, name);
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceKey.create(Registries.SOUND_EVENT, id), event);
    }

    public static void register() {
    }
}