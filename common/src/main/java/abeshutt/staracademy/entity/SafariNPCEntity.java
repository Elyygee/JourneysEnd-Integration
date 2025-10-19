package abeshutt.staracademy.entity;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModWorldData;
import abeshutt.staracademy.world.data.SafariData;
import fr.harmex.cobbledollars.common.utils.extensions.PlayerExtensionKt;
import java.math.BigInteger;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SafariNPCEntity extends HumanEntity {

    public SafariNPCEntity(EntityType<? extends PathAwareEntity> type, World world) {
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
        super.tick();
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(!player.getWorld().isClient() && hand == Hand.MAIN_HAND) {
            SafariData data = ModWorldData.SAFARI.getGlobal(player.getWorld());
            SafariData.Entry entry = data.getOrCreate(player.getUuid());

            // Check if player is OP and bypass unlock requirement
            boolean isOp = player.getServer() != null && player.getServer().getPlayerManager().isOperator(player.getGameProfile());
            
            if(!entry.isUnlocked() && !isOp) {
                BigInteger cost = BigInteger.valueOf(ModConfigs.NPC.getSafariCurrencyCost());
                if(!PlayerExtensionKt.canBuy(player, cost)) {
                    if(!entry.isPrompted()) {
                        player.sendMessage(Text.empty()
                                .append(Text.translatable("text.journeysend.safari.initial_locked", cost.toString()).formatted(Formatting.GRAY)));
                        entry.setPrompted(true);
                        data.markDirty();
                    } else {
                        player.sendMessage(Text.empty()
                                .append(Text.translatable("text.journeysend.safari.unlock_broke", cost.toString()).formatted(Formatting.GRAY)));
                    }
                } else {
                    // Show confirmation dialog instead of immediately taking money
                    MutableText confirmationText = Text.empty()
                            .append(Text.translatable("text.journeysend.safari.unlock_confirmation", cost.toString()).formatted(Formatting.GRAY))
                            .append(" ")
                            .append(Text.literal("[Yes]").formatted(Formatting.GREEN)
                                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/journeysend safari confirm_unlock " + player.getUuid().toString()))))
                            .append(" ")
                            .append(Text.literal("[No]").formatted(Formatting.RED)
                                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/journeysend safari cancel_unlock"))));
                    
                    player.sendMessage(confirmationText, false);
                }

                return ActionResult.SUCCESS;
            }

            if(data.isPaused()) {
                player.sendMessage(Text.empty()
                        .append(Text.translatable("text.journeysend.safari.initial_closed").formatted(Formatting.GRAY)));
            } else {
                player.sendMessage(Text.empty()
                        .append(Text.translatable("text.journeysend.safari.initial_open").formatted(Formatting.GRAY)));
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public Identifier getSkinTexture() {
        return StarAcademyMod.id("textures/entity/safari_npc.png");
    }

}

