package com.kryeit.content.exchanger.fabric;

import com.kryeit.content.exchanger.MechanicalExchangerBlockEntity;
import io.github.fabricators_of_create.porting_lib.transfer.StorageViewArrayIterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.stream.Collectors;

public class MechanicalExchangerContainerInterface extends SnapshotParticipant<MechanicalExchangerContainerInterface.ContainerSnapshot> implements Storage<ItemVariant> {

	private final MechanicalExchangerBlockEntity be;
	private final MechanicalExchangerContainerSlotView[] views;


	public MechanicalExchangerContainerInterface(MechanicalExchangerBlockEntity be) {
        this.be = be;
		this.views = new MechanicalExchangerContainerSlotView[2];
		this.views[0] = new MechanicalExchangerContainerSlotView(this, 0);
		this.views[1] = new MechanicalExchangerContainerSlotView(this, 1);

	}


	// TODO: Fix insert and extract methods

	// For some reason que ItemVariant is always the Item in the slot 0
	// Test with the extract() method first
	// This causes the "long" return value to be aplicated to the wrong slot,
	// so the Item type that is in the slot 0 is the one that is being spawned by a chute for example

	// Example: 1 diamond -> 16 gold, ends up outputting the 16 diamonds and deletes the 16 gold
	// The only issue is the "return remove" line, which makes Diamonds to be dropped by a chute
	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long total = 0;
		this.updateSnapshots(transaction);

		int maxInsert = be.inventory.get(0).getMaxStackSize() - be.inventory.get(0).getCount();
		if (maxInsert > 0) {
			int add = Math.min((int) maxAmount, maxInsert);
			if (add > 0 && be.canInsertItemIntoSlot(0, resource.toStack())) {
				resource.toStack().shrink(add);
				be.inventory.set(0, be.inventory.get(0).copy());
				total += add;
			}
		}
		return total;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		this.updateSnapshots(transaction);

		int maxExtract = be.inventory.get(1).getCount();
		if (maxExtract > 0) {
			int remove = Math.min((int) maxAmount, maxExtract);
			if (remove > 0) {
				be.inventory.get(1).split(remove);
				return remove;
			}
		}
		return 0;
	}

	@Nonnull
	public ItemStack getStack(int slot) {
		return this.be.inventory.get(slot);
	}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		be.notifyUpdate();
	}


	public int getCapacityForSlot(int slot) {
		return this.be.inventory.get(slot).getMaxStackSize() - this.be.inventory.get(slot).getCount();
	}

	public void restoreViewSnapshot(int slot, ItemStack snapshot) {
		be.inventory.set(slot, snapshot);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return new StorageViewArrayIterator<>(this.views);
	}

	@Override protected ContainerSnapshot createSnapshot() { return new ContainerSnapshot(this); }
	@Override protected void readSnapshot(ContainerSnapshot snapshot) { snapshot.apply(this); }

	public static class ContainerSnapshot {
		private final NonNullList<ItemStack> inventory;

		public ContainerSnapshot(MechanicalExchangerContainerInterface inventory) {
			this.inventory = inventory.be.inventory.stream().map(ItemStack::copy).collect(Collectors.toCollection(NonNullList::create));
		}

		public void apply(MechanicalExchangerContainerInterface inventory) {
			inventory.be.inventory = this.inventory;
		}
	}
}
