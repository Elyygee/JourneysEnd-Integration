package abeshutt.staracademy.entity;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModWorldData;
import abeshutt.staracademy.item.CardItem;
import abeshutt.staracademy.world.data.CardGradingData;
import abeshutt.staracademy.world.random.JavaRandom;
import abeshutt.staracademy.world.random.RandomSource;
import fr.harmex.cobbledollars.common.utils.extensions.PlayerExtensionKt;
import java.math.BigInteger;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.function.Function;

public class CardGraderNPCEntity extends HumanEntity {

    public CardGraderNPCEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F, 0.1F));
        this.goalSelector.add(2, new LookAroundGoal(this));
    }

    @Override
    public void tick() {
        this.setInvulnerable(true);

        if(this.getWorld().isClient()) {
            this.equipCard();
            this.updateName();
        }

        super.tick();
    }

    @Environment(EnvType.CLIENT)
    private void equipCard() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        this.setStackInHand(Hand.MAIN_HAND, CardGradingData.CLIENT.getStack(player.getUuid()));
    }

    @Environment(EnvType.CLIENT)
    private void updateName() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if(player == null) {
            this.setCustomNameVisible(false);
            return;
        }

        ItemStack stack = CardGradingData.CLIENT.getStack(player.getUuid());

        if(stack.isEmpty()) {
            this.setCustomNameVisible(false);
            return;
        }

        long millis = Math.max(CardGradingData.CLIENT.getTimeLeft(player.getUuid()), 0);
        long hours = millis / 1000 / 3600;
        long minutes = millis / 1000 % 3600 / 60;
        long seconds = millis / 1000 % 60;
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        this.setCustomNameVisible(true);
        this.setCustomName(Text.literal(time));
    }

    @Override
    protected ActionResult interactMob(PlayerEntity user, Hand hand) {
        RandomSource random = JavaRandom.ofNanoTime();

        if(user instanceof ServerPlayerEntity player) {
            ItemStack stack = player.getStackInHand(hand);
            CardGradingData data = ModWorldData.CARD_GRADING.getGlobal(player.getWorld());

            if(data.has(player.getUuid())) {
                if(data.getTimeLeft(player.getUuid()) < 0) {
                    ItemStack returned = data.getStack(player.getUuid());
                    int grade = ModConfigs.CARD_SCALARS.getGrade(random);

                    if(returned.getItem() instanceof CardItem) {
                        CardItem.get(returned).ifPresent(card -> card.setGrade(grade));
                    }

                    ItemStackHooks.giveItem(player, returned);
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                            ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    player.sendMessage(Text.empty().append(Text.translatable(REQUEST_COMPLETE.apply(random))
                            .formatted(Formatting.GRAY)));
                    data.remove(player.getUuid());
                } else {
                    player.sendMessage(Text.empty().append(Text.translatable(REQUEST_IMPATIENT.apply(random))
                            .formatted(Formatting.GRAY)));
                }
            } else if(stack.getItem() instanceof CardItem && CardItem.get(stack).map(card -> card.getGrade() == 0).orElse(false)) {
                if(Platform.isModLoaded("cobbledollars")) {
                    BigInteger cost = BigInteger.valueOf(ModConfigs.NPC.getGradingCurrencyCost());
                    if(!PlayerExtensionKt.canBuy(player, cost)) {
                        player.sendMessage(Text.empty().append(Text.translatable("text.journeysend.grader.unlock_broke", cost.toString())
                                .formatted(Formatting.GRAY)));
                    } else {
                        // Show confirmation dialog instead of immediately taking money
                        MutableText confirmationText = Text.empty()
                                .append(Text.translatable("text.journeysend.grader.unlock_confirmation", cost.toString()).formatted(Formatting.GRAY))
                                .append(" ")
                                .append(Text.literal("[Yes]").formatted(Formatting.GREEN)
                                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/journeysend grader confirm_unlock " + player.getUuid().toString()))))
                                .append(" ")
                                .append(Text.literal("[No]").formatted(Formatting.RED)
                                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/journeysend grader cancel_unlock"))));
                        
                        player.sendMessage(confirmationText, false);
                    }
                } else {
                    data.add(player.getUuid(), stack.copy());
                    player.setStackInHand(hand, ItemStack.EMPTY);
                    player.sendMessage(Text.empty().append(Text.translatable(INITIAL_CARD.apply(random))
                            .formatted(Formatting.GRAY)));
                }
            } else {
                player.sendMessage(Text.empty().append(Text.translatable(INITIAL_NO_CARD.apply(random))
                        .formatted(Formatting.GRAY)));
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public Identifier getSkinTexture() {
        return StarAcademyMod.id("textures/entity/card_grader_npc.png");
    }

    public static final Function<RandomSource, String> INITIAL_NO_CARD = create(
            "text.journeysend.grader.initial_no_card_1",
            "text.journeysend.grader.initial_no_card_2",
            "text.journeysend.grader.initial_no_card_3",
            "text.journeysend.grader.initial_no_card_4",
            "text.journeysend.grader.initial_no_card_5",
            "text.journeysend.grader.initial_no_card_6",
            "text.journeysend.grader.initial_no_card_7"
    );

    public static final Function<RandomSource, String> INITIAL_CARD = create(
            "text.journeysend.grader.initial_card_1",
            "text.journeysend.grader.initial_card_2",
            "text.journeysend.grader.initial_card_3"
    );

    public static final Function<RandomSource, String> INITIAL_BROKE = create(
            "text.journeysend.grader.initial_broke_1",
            "text.journeysend.grader.initial_broke_2",
            "text.journeysend.grader.initial_broke_3"
    );

    public static final Function<RandomSource, String> REQUEST_IMPATIENT = create(
            "text.journeysend.grader.request_impatient_1",
            "text.journeysend.grader.request_impatient_2",
            "text.journeysend.grader.request_impatient_3"
    );

    public static final Function<RandomSource, String> REQUEST_COMPLETE = create(
            "text.journeysend.grader.request_complete_1",
            "text.journeysend.grader.request_complete_2",
            "text.journeysend.grader.request_complete_3"
    );

    public static Function<RandomSource, String> create(String... lines) {
        return random -> lines[random.nextInt(lines.length)];
    }

}
