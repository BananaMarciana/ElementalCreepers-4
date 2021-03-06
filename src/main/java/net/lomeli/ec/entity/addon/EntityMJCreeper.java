package net.lomeli.ec.entity.addon;

import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.transport.IPipeTile;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.lomeli.ec.entity.EntityBaseCreeper;
import net.lomeli.ec.lib.ECVars;

public class EntityMJCreeper extends EntityBaseCreeper {

    public EntityMJCreeper(World par1World) {
        super(par1World);
    }

    @Override
    public void explosion(int power, boolean flag) {
        int radius = getPowered() ? (ECVars.mjCreeperRadius * power) : ECVars.mjCreeperRadius;
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++) {
                    TileEntity tile = worldObj.getTileEntity((int) posX + x, (int) posY + y, (int) posZ + z);
                    if (tile != null) {
                        try {
                            if (tile instanceof IPowerReceptor || tile instanceof IPowerEmitter || Class.forName("buildcraft.core.IMachine").isInstance(tile)) {
                                if (worldObj.rand.nextInt(100) < 35) {
                                    if (tile instanceof IPipeTile)
                                        removePipe((int) posX + x, (int) posY + y, (int) posZ + z);
                                    else {
                                        ItemStack itemStack = new ItemStack(worldObj.getBlock((int) posX + x, (int) posY + y, (int) posZ + z), 1, worldObj.getBlockMetadata((int) posX + x, (int) posY + y, (int) posZ + z));
                                        if (itemStack != null) {
                                            EntityItem item = new EntityItem(worldObj, posX + x, posY + y, posZ + z, itemStack);
                                            if (!worldObj.isRemote)
                                                worldObj.spawnEntityInWorld(item);
                                            worldObj.setBlockToAir((int) posX + x, (int) posY + y, (int) posZ + z);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
    }

    public void removePipe(int x, int y, int z) {
        ItemStack pipe = worldObj.getBlock(x, y, z).getPickBlock(null, worldObj, x, y, z);
        if (pipe != null) {
            EntityItem item = new EntityItem(worldObj, x, y, z, pipe);
            if (!worldObj.isRemote)
                worldObj.spawnEntityInWorld(item);
            worldObj.setBlockToAir(x, y, z);
        }
    }
}
