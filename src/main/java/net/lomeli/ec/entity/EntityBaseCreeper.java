package net.lomeli.ec.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class EntityBaseCreeper extends EntityCreeper {

    protected int lastActiveTime;
    protected int timeSinceIgnited;
    protected int fuseTime = 30;
    protected int explosionRadius = 3;
    protected boolean explosionSound;
    protected boolean startIgnight;

    public EntityBaseCreeper(World par1World) {
        this(par1World, true);
    }

    public EntityBaseCreeper(World par1World, boolean playSound) {
        super(par1World);
        explosionSound = playSound;
    }

    @Override
    protected void fall(float par1) {
        super.fall(par1);
        this.timeSinceIgnited = (int) (this.timeSinceIgnited + par1 * 1.5F);

        if (this.timeSinceIgnited > this.fuseTime - 5)
            this.timeSinceIgnited = this.fuseTime - 5;
    }

    @Override
    public void onUpdate() {
        if (this.isEntityAlive()) {
            this.lastActiveTime = this.timeSinceIgnited;
            int i = this.getCreeperState();

            if (i > 0 && this.timeSinceIgnited == 0)
                this.playSound("random.fuse", 1.0F, 0.5F);

            if (this.startIgnight) {
                if (this.timeSinceIgnited < 0)
                    this.timeSinceIgnited = 0;

                if (++this.timeSinceIgnited >= this.fuseTime)
                    explode();
            } else {
                this.timeSinceIgnited += i;

                if (this.timeSinceIgnited < 0)
                    this.timeSinceIgnited = 0;

                if (this.timeSinceIgnited >= this.fuseTime)
                    explode();
            }
        }

        super.onUpdate();
    }

    public void explode() {
        this.timeSinceIgnited = this.fuseTime;
        if (!this.worldObj.isRemote) {
            boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");

            this.explosion(this.getPowered() ? 2 : 1, flag);

            if (this.diesAfterExplosion())
                this.setDead();
        }
        if (worldObj.isRemote) {
            if (explosionSound)
                worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 4F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
            spawnExplosionParticle();
        }
    }

    public abstract void explosion(int power, boolean flag);

    public boolean diesAfterExplosion() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getCreeperFlashIntensity(float par1) {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * par1) / (this.fuseTime - 2);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeEntityToNBT(par1NBTTagCompound);

        if (this.dataWatcher.getWatchableObjectByte(17) == 1)
            par1NBTTagCompound.setBoolean("powered", true);

        par1NBTTagCompound.setShort("Fuse", (short) this.fuseTime);
        par1NBTTagCompound.setByte("ExplosionRadius", (byte) this.explosionRadius);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.dataWatcher.updateObject(17, Byte.valueOf((byte) (par1NBTTagCompound.getBoolean("powered") ? 1 : 0)));

        if (par1NBTTagCompound.hasKey("Fuse"))
            this.fuseTime = par1NBTTagCompound.getShort("Fuse");

        if (par1NBTTagCompound.hasKey("ExplosionRadius"))
            this.explosionRadius = par1NBTTagCompound.getByte("ExplosionRadius");
    }

    @Override
    protected boolean interact(EntityPlayer player) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack != null && stack.getItem() == Items.flint_and_steel) {
            worldObj.playSoundEffect(posX + 0.5D, posY + 0.5D, posZ + 0.5D, "fire.ignite", 1.0F, rand.nextFloat() * 0.4F + 0.8F);
            player.swingItem();
            if (!worldObj.isRemote) {
                stack.damageItem(1, player);
                this.startIgnight = true;
                this.func_146079_cb();
                return true;
            }
        }
        return false;
    }

    public void domeExplosion(int radius, Block block, int meta) {
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++) {
                    if (block.canPlaceBlockAt(worldObj, (int) posX + x, (int) posY + y, (int) posZ + z) && Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) <= radius) {
                        if (rand.nextInt(4) < 3)//
                            worldObj.setBlock((int) posX + x, (int) posY + y, (int) posZ + z, block, meta, 2);
                    }
                }

    }

    public void domeExplosion(int radius, Block block) {
        this.domeExplosion(radius, block, 0);
    }

    public void wildExplosion(int radius, Block block, int meta) {
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++) {
                    if (block.canPlaceBlockAt(worldObj, (int) posX + x, (int) posY + y, (int) posZ + z)
                            && !block.canPlaceBlockAt(worldObj, (int) posX + x, (int) posY + y - 1, (int) posZ + z)) {
                        if (rand.nextBoolean())
                            worldObj.setBlock((int) posX + x, (int) posY + y, (int) posZ + z, block, meta, 2);
                    }
                }
    }

    public void wildExplosion(int radius, Block block){
        this.wildExplosion(radius, block, 0);
    }
}
