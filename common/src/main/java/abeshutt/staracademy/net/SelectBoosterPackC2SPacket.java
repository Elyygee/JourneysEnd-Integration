package abeshutt.staracademy.net;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.data.bit.BitBuffer;
import abeshutt.staracademy.item.BaseBoosterPackItem;
import abeshutt.staracademy.item.BoosterPackItem;
import dev.architectury.hooks.item.ItemStackHooks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectBoosterPackC2SPacket extends ModPacket<ServerPlayNetworkHandler> {

    private static final Logger LOGGER = LoggerFactory.getLogger("JourneysEnd-SelectBoosterPack");

    public static final Id<SelectBoosterPackC2SPacket> ID = new Id<>(StarAcademyMod.id("select_booster_pack_c2s"));

    private final List<Integer> selected;

    public SelectBoosterPackC2SPacket() {
        this.selected = new ArrayList<>();
    }

    public SelectBoosterPackC2SPacket(Collection<Integer> selected) {
        this.selected = new ArrayList<>(selected);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Override
    public void onReceive(ServerPlayNetworkHandler listener) {
        ServerPlayerEntity player = listener.getPlayer();
        ItemStack stack = player.getStackInHand(player.getActiveHand());
        
        // Handle both BoosterPackItem and BaseBoosterPackItem
        if((stack.getItem() instanceof BoosterPackItem || stack.getItem() instanceof BaseBoosterPackItem) && stack.contains(DataComponentTypes.CONTAINER)) {
            ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
            List<ItemStack> stacks = new ArrayList<>();
            List<ItemStack> toGive = new ArrayList<>();
            container.iterateNonEmpty().forEach(stacks::add);

            // If no cards were selected, give all cards
            if (this.selected.isEmpty()) {
                toGive.addAll(stacks);
                stacks.clear();
            } else {
                // Give selected cards
                for(int index : this.selected) {
                    if(index >= 0 && index < stacks.size()) {
                        toGive.add(stacks.get(index));
                        stacks.set(index, ItemStack.EMPTY);
                    }
                }
            }

            // Remove empty slots and update container
            stacks.removeIf(ItemStack::isEmpty);
            
            if (stacks.isEmpty()) {
                // All cards given, consume the pack
                stack.setCount(0);
            } else {
                // Update container with remaining cards
                stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
            }

            // Give cards to player
            for(ItemStack giving : toGive) {
                ItemStackHooks.giveItem(player, giving);
            }
        }
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        Adapters.INT_SEGMENTED_3.writeBits(this.selected.size(), buffer);

        this.selected.forEach(index -> {
            Adapters.INT_SEGMENTED_3.writeBits(index, buffer);
        });
    }

    @Override
    public void readBits(BitBuffer buffer) {
        this.selected.clear();
        int size = Adapters.INT_SEGMENTED_3.readBits(buffer).orElseThrow();

        for(int i = 0; i < size; i++) {
            this.selected.add(Adapters.INT_SEGMENTED_3.readBits(buffer).orElseThrow());
        }
    }

}

