/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.init;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.utils.Lazy;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum ModSounds {
    CURSED_HEART_USE,
    ENCHANTED_HEART_USE,
    HEART_CRYSTAL_USE,
    PLAYER_DIED;

    private final Lazy<SoundEvent> sound;

    ModSounds() {
        this.sound = Lazy.of(() -> {
            ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, getName());
            return new SoundEvent(id);
        });
    }

    public SoundEvent get() {
        return sound.get();
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public void play(PlayerEntity entity) {
        play(entity, 0.5f, 1 + 0.1f * (float) ScalingHealth.random.nextGaussian());
    }

    public void play(PlayerEntity entity, float volume, float pitch) {
        entity.world.playSound(entity, entity.getPosition(), this.get(), SoundCategory.PLAYERS, volume, pitch);
    }

    @SubscribeEvent
    public static void registerAll(RegistryEvent.Register<SoundEvent> event) {
        for (ModSounds sound : values()) {
            register(sound.getName(), sound.get());
        }
    }

    private static void register(String name, SoundEvent sound) {
        ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, name);
        sound.setRegistryName(id);
        ForgeRegistries.SOUND_EVENTS.register(sound);
    }
}
