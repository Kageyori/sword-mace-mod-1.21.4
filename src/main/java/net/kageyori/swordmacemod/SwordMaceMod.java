package com.example.maceswordmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import java.util.Map;

public class MaceSwordMod implements ModInitializer {
	@Override
	public void onInitialize() {
		AttackEntityCallback.EVENT.register(this::onAttack);
	}

	private ActionResult onAttack(PlayerEntity player, net.minecraft.world.World world, Hand hand, EntityHitResult hitResult) {
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

	private void triggerCombinedMaceSwordEffect(PlayerEntity player, ItemStack sword, ItemStack mace, EntityHitResult hitResult) {
		if (hitResult.getEntity() instanceof LivingEntity target) {
			float baseDamage = 10.0f; // Default mace damage
			float swordBonus = EnchantmentHelper.getLevel(net.minecraft.enchantment.Enchantments.SHARPNESS, sword) * 1.5f;
			float maceBonus = EnchantmentHelper.getLevel(net.minecraft.enchantment.Enchantments.SHARPNESS, mace) * 1.5f;
			float totalDamage = baseDamage + swordBonus + maceBonus;

			// Combine enchantments from both items
			Map<net.minecraft.enchantment.Enchantment, Integer> swordEnchantments = EnchantmentHelper.get(sword);
			Map<net.minecraft.enchantment.Enchantment, Integer> maceEnchantments = EnchantmentHelper.get(mace);

			swordEnchantments.forEach((enchantment, level) -> target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(enchantment.getStatusEffect(), 100, level)));
			maceEnchantments.forEach((enchantment, level) -> target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(enchantment.getStatusEffect(), 100, level)));

			target.damage(player.getDamageSources().playerAttack(player), totalDamage);
		}
	}
}
