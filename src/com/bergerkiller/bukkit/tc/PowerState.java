package com.bergerkiller.bukkit.tc;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PressureSensor;
import org.bukkit.material.Redstone;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;

public enum PowerState {
	ON, OFF, NONE;

	private static boolean isDistractingColumn(Block main, BlockFace face) {
		Block side = main.getRelative(face);
		Material type = side.getType();
		if (MaterialUtil.ISPOWERSOURCE.get(type)) {
			return true;
		} else if (type == Material.AIR) {
			//check level below
			if (MaterialUtil.ISPOWERSOURCE.get(side.getRelative(BlockFace.DOWN))) {
				return true;
			}
		} else if (type == Material.DIODE_BLOCK_ON || type == Material.DIODE_BLOCK_OFF) {
			//powered by repeater?
			BlockFace facing = BlockUtil.getFacing(side);
			return facing == face;
		}
		if (main.getRelative(BlockFace.UP).getType() == Material.AIR) {
			//check level on top
			return MaterialUtil.ISPOWERSOURCE.get(side.getRelative(BlockFace.UP));
		} else {
			return false;
		}
	}

	private static boolean isDistracted(Block wire, BlockFace face) {
		return isDistractingColumn(wire, FaceUtil.rotate(face, -2)) || isDistractingColumn(wire, FaceUtil.rotate(face, 2));
	}

	public static PowerState get(Block block, BlockFace from) {
		return get(block, from, true);
	}

	/**
	 * Computes the power state for a given block
	 * 
	 * @param block to get the power state of
	 * @param from what BlockFace side the block power should be computed
	 * @param useSignLogic setting, sets whether redstone next to the block acts as power source
	 * @return The Power State of the block
	 */
	public static PowerState get(Block block, BlockFace from, boolean useSignLogic) {
		block = block.getRelative(from);
		Material type = block.getType();

		if (MaterialUtil.ISREDSTONETORCH.get(type)) {
			if (useSignLogic || from == BlockFace.DOWN) {
				return type == Material.REDSTONE_TORCH_ON ? ON : OFF;
			} else {
				return NONE;
			}
		} else if (MaterialUtil.ISDIODE.get(type) && from != BlockFace.DOWN && from != BlockFace.UP) {
			if ((BlockUtil.getFacing(block) != from)) {
				return type == Material.DIODE_BLOCK_ON ? ON : OFF;
			} else {
				return NONE;
			}
		} else if (type == Material.REDSTONE_WIRE) {
			if (useSignLogic || from == BlockFace.UP || (from != BlockFace.DOWN && !isDistracted(block, from))) {
				return (block.getData() != 0) ? ON : OFF;
			} else {
				return NONE;
			}
		}

		// Ignore power from levers
		if (type == Material.LEVER && !useSignLogic) {
			return NONE;
		}
		// Power source read-out
		if (from != BlockFace.DOWN && MaterialUtil.ISPOWERSOURCE.get(type)) {
			MaterialData dat = type.getNewData(block.getData());
			if (dat instanceof Redstone) {
				return ((Redstone) dat).isPowered() ? ON : OFF;
			} else if (dat instanceof PressureSensor) {
				return ((PressureSensor) dat).isPressed() ? ON : OFF;
			}
		}
		return NONE;
	}

	public boolean hasPower() {
		switch (this) {
		case ON : return true;
		default : return false;
		}
	}
}