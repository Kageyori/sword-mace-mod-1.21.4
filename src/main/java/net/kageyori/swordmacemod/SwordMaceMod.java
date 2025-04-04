package net.kageyori.swordmacemod;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import java.util.Map;

public class SwordMaceMod implements ModInitializer {
	@Override
	public void onInitialize() {
		AttackEntityCallback.EVENT.register(this::onAttack);
	}

	private ActionResult onAttack(PlayerEntity player, World world, Hand hand, net.minecraft.entity.Entity entity, EntityHitResult hitResult) {
		if (world.isClient) return ActionResult.PASS;

		ItemStack mainHandItem = player.getMainHandStack();
		ItemStack mace = getMaceFromHotbar(player);

		// Check if player is falling and has a mace in hotbar
		if (player.fallDistance > 1.5f && isSword(mainHandItem) && mace != null) {
			triggerCombinedMaceSwordEffect(player, mainHandItem, mace, hitResult);
		}
		return ActionResult.PASS;
	}

	private boolean isSword(ItemStack item) {
		return item.getItem().toString().contains("sword");
	}

	private ItemStack getMaceFromHotbar(PlayerEntity player) {
		for (ItemStack stack : player.getInventory().main) {
			if (stack.getItem().toString().contains("mace")) {
				return stack;
			}
		}
		return null;
	}

	private int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
		return stack.getEnchantments().stream()
				.filter(nbt -> RegistryKey.of(RegistryKeys.ENCHANTMENT, enchantment.getId()).getValue().equals(enchantment.getId()))
				.map(nbt -> nbt.getInt("lvl"))
				.findFirst()
				.orElse(0);
	}

	private void triggerCombinedMaceSwordEffect(PlayerEntity player, ItemStack sword, ItemStack mace, EntityHitResult hitResult) {
		if (hitResult.getEntity() instanceof LivingEntity target) {
			float baseDamage = 10.0f; // Default mace damage
			float swordBonus = getEnchantmentLevel(sword, net.minecraft.enchantment.Enchantments.SHARPNESS) * 1.5f;
			float maceBonus = getEnchantmentLevel(mace, net.minecraft.enchantment.Enchantments.SHARPNESS) * 1.5f;
			float totalDamage = baseDamage + swordBonus + maceBonus;

			target.damage(player.getDamageSources().playerAttack(player), totalDamage);
		}
	}
}
