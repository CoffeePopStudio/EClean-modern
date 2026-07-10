package feature.trashcan

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import setupMockBukkit
import top.e404.eclean.feature.trashcan.TrashcanItemStore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrashcanItemStoreTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpClass() {
            setupMockBukkit()
        }
    }

    @Test
    fun `addItem merges similar items`() {
        val store = TrashcanItemStore(maxSlots = 54)
        store.addItem(ItemStack(Material.DIAMOND, 30))
        store.addItem(ItemStack(Material.DIAMOND, 30))
        val slots = arrayOfNulls<ItemStack>(54)
        store.loadInto(slots)
        val diamond = slots[0]
        assertNotNull(diamond)
        assertEquals(Material.DIAMOND, diamond.type)
        assertEquals(60, diamond.amount)
    }

    @Test
    fun `addItem overflows to new slot when exceeding maxStackSize`() {
        val store = TrashcanItemStore(maxSlots = 54)
        assertTrue(store.addItem(ItemStack(Material.DIAMOND, 64)))
        assertTrue(store.addItem(ItemStack(Material.DIAMOND, 10)))
        assertEquals(2, store.size)
    }

    @Test
    fun `addItem returns false when store is full`() {
        val store = TrashcanItemStore(maxSlots = 1)
        assertTrue(store.addItem(ItemStack(Material.DIAMOND, 1)))
        assertFalse(store.addItem(ItemStack(Material.IRON_INGOT, 1)))
        assertEquals(1, store.size)
    }

    @Test
    fun `removeItem subtracts amount and returns removed count`() {
        val store = TrashcanItemStore(maxSlots = 54)
        store.addItem(ItemStack(Material.DIAMOND, 10))
        val removed = store.removeItem(ItemStack(Material.DIAMOND), 3)
        assertEquals(3, removed)
        val slots = arrayOfNulls<ItemStack>(54)
        store.loadInto(slots)
        assertEquals(7, slots[0]?.amount)
        assertEquals(Material.DIAMOND, slots[0]?.type)
    }

    @Test
    fun `removeItem deletes entry when amount exhausted`() {
        val store = TrashcanItemStore(maxSlots = 54)
        store.addItem(ItemStack(Material.DIAMOND, 5))
        val removed = store.removeItem(ItemStack(Material.DIAMOND), 5)
        assertEquals(5, removed)
        assertTrue(store.isEmpty())
    }

    @Test
    fun `removeItem supports cross-stack removal`() {
        val store = TrashcanItemStore(maxSlots = 54)
        store.addItem(ItemStack(Material.DIAMOND, 64))
        store.addItem(ItemStack(Material.DIAMOND, 10))
        val removed = store.removeItem(ItemStack(Material.DIAMOND), 70)
        assertEquals(70, removed)
        val slots = arrayOfNulls<ItemStack>(54)
        store.loadInto(slots)
        assertEquals(4, slots[0]?.amount)
        assertNull(slots[1])
    }

    @Test
    fun `clear empties the store`() {
        val store = TrashcanItemStore(maxSlots = 54)
        store.addItem(ItemStack(Material.DIAMOND, 1))
        store.addItem(ItemStack(Material.IRON_INGOT, 1))
        store.clear()
        assertTrue(store.isEmpty())
        assertEquals(0, store.size)
    }

    @Test
    fun `loadInto and saveFrom round-trip`() {
        val store = TrashcanItemStore(maxSlots = 54)
        store.addItem(ItemStack(Material.DIAMOND, 64))
        store.addItem(ItemStack(Material.IRON_INGOT, 32))

        val slots = arrayOfNulls<ItemStack>(54)
        store.loadInto(slots)
        assertEquals(Material.DIAMOND, slots[0]?.type)
        assertEquals(64, slots[0]?.amount)
        assertEquals(Material.IRON_INGOT, slots[1]?.type)
        assertEquals(32, slots[1]?.amount)

        slots[0]!!.amount = 16
        store.saveFrom(slots)
        val restored = arrayOfNulls<ItemStack>(54)
        store.loadInto(restored)
        assertEquals(16, restored[0]?.amount)
        assertEquals(32, restored[1]?.amount)
    }

    @Test
    fun `saveFrom skips air slots`() {
        val store = TrashcanItemStore(maxSlots = 54)
        store.addItem(ItemStack(Material.DIAMOND, 1))
        val slots = arrayOfNulls<ItemStack>(54)
        slots[0] = ItemStack(Material.AIR)
        store.saveFrom(slots)
        assertTrue(store.isEmpty())
    }
}
