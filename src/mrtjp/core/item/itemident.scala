/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.item

import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.generic.Growable
import scala.collection.immutable.HashMap

object ItemKey
{
    def apply(stack:ItemStack) = get(stack)
    def get(stack:ItemStack) =
    {
        if (stack == null) null
        else new ItemKey(stack.getItem, stack.getItemDamage, stack.getTagCompound)
    }
}

class ItemKey(val item:Item, val itemDamage:Int, val tag:NBTTagCompound) extends Ordered[ItemKey]
{
    val itemID = Item.getIdFromItem(item)
    private val hash = itemID*1000001*itemDamage+(if (tag != null) tag.hashCode else 0)

    override def hashCode = hash

    override def equals(other:Any) = other match
    {
        case that:ItemKey =>
            item == that.item && itemDamage == that.itemDamage &&
                tag == that.tag
        case _ => false
    }

    override def toString = getName

    def compare(that:ItemKey) =
    {
        val c = itemID-that.itemID
        if (c == 0) itemDamage-that.itemDamage
        else c
    }

    def makeStack(size:Int) =
    {
        val stack = new ItemStack(item, size, itemDamage)
        if (tag != null) stack.setTagCompound(tag.copy().asInstanceOf[NBTTagCompound])
        stack
    }

    def copy = new ItemKey(item, itemDamage, tag)

    /** Interactions **/
    def getItem = item
    def getMaxStackSize = makeStack(0).getMaxStackSize
    def getName = makeStack(0).getDisplayName
}

object ItemKeyStack
{
    def apply(key:ItemKey, size:Int) = get(key, size)
    def get(key:ItemKey, size:Int) = new ItemKeyStack(key, size)

    def apply(stack:ItemStack) = get(stack)
    def get(stack:ItemStack) =
    {
        if (stack == null) null
        else new ItemKeyStack(ItemKey.get(stack), stack.stackSize)
    }
}

class ItemKeyStack(val key:ItemKey, var stackSize:Int) extends Ordered[ItemKeyStack]
{
    override def hashCode = key.hashCode

    override def equals(other:Any) = other match
    {
        case that:ItemKeyStack =>
            key == that.key && stackSize == that.stackSize
        case _ => false
    }

    override def toString = "["+key.toString+", "+stackSize+"]"

    def makeStack = key.makeStack(stackSize)

    def copy = new ItemKeyStack(key.copy, stackSize)

    def compare(that:ItemKeyStack) =
    {
        val c = key.compare(that.key)
        if (c == 0) stackSize-that.stackSize
        else c
    }
}

class ItemQueue extends Growable[(ItemKey, Int)]
{
    private var collection = HashMap[ItemKey, Int]()

    override def +=(elem:(ItemKey, Int)) =
    {
        val current = collection.getOrElse(elem._1, 0)
        collection += elem._1 -> (current+elem._2)
        this
    }

    override def clear(){collection = HashMap[ItemKey, Int]()}

    def ++=(that:ItemQueue) = {that.result.foreach(this += _); this}

    def result = collection
}