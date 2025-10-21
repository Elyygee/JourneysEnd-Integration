package abeshutt.staracademy;

import abeshutt.staracademy.init.ModBlocks;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModItems;
import abeshutt.staracademy.item.BoosterPackItem;
import abeshutt.staracademy.item.CardAlbumItem;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;

@EmiEntrypoint
public class AcademyEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        this.registerSafariTickets(registry);
        this.registerBoosterPacks(registry);
        this.registerCardAlbums(registry);

        registry.removeEmiStacks(stack -> {
            return stack.getItemStack().getItem() == ModItems.LEGENDARY_PLACEHOLDER.get()
                    || stack.getItemStack().getItem() == ModBlocks.ERROR.get().asItem()
                    || stack.getItemStack().getItem() == ModBlocks.SAFARI_PORTAL.get().asItem()
                    || stack.getItemStack().getItem() == ModItems.ACCEPTANCE_LETTER.get()
                    || stack.getItemStack().getItem() == ModItems.OUTFIT.get();
        });
    }

    public void registerSafariTickets(EmiRegistry registry) {
        // Register the three specific safari ticket types
        registry.addEmiStack(EmiStack.of(ModItems.SAFARI_TICKET_BASE.get()));
        registry.addEmiStack(EmiStack.of(ModItems.SAFARI_TICKET_GREAT.get()));
        registry.addEmiStack(EmiStack.of(ModItems.SAFARI_TICKET_GOLDEN.get()));
    }

    public void registerBoosterPacks(EmiRegistry registry) {
        ModConfigs.CARD_BOOSTERS.getValues().forEach((id, entry) -> {
            registry.addEmiStackAfter(EmiStack.of(BoosterPackItem.create(id)), stack -> {
                return stack.getItemStack().getItem() == ModItems.BASE_BOOSTER_PACK.get();
            });
        });

        registry.removeEmiStacks(stack -> {
            return stack.getItemStack().getItem() == ModItems.BASE_BOOSTER_PACK.get()
                    && BoosterPackItem.get(stack.getItemStack(), true).isEmpty();
        });
    }

    public void registerCardAlbums(EmiRegistry registry) {
        ModConfigs.CARD_ALBUMS.getValues().forEach((id, entry) -> {
            registry.addEmiStackAfter(EmiStack.of(CardAlbumItem.create(id)), stack -> {
                return stack.getItemStack().getItem() == ModItems.CARD_ALBUM.get();
            });
        });

        registry.removeEmiStacks(stack -> {
            return stack.getItemStack().getItem() == ModItems.CARD_ALBUM.get()
                    && CardAlbumItem.get(stack.getItemStack(), true).isEmpty();
        });
    }

}
