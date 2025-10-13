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
                                .append(Text.translatable("text.academy.safari.initial_locked").formatted(Formatting.GRAY)));
                        entry.setPrompted(true);
                        data.markDirty();
                    } else {
                        player.sendMessage(Text.empty()
                                .append(Text.translatable("text.academy.safari.unlock_broke").formatted(Formatting.GRAY)));
                    }
                } else {
                    BigInteger currentBalance = PlayerExtensionKt.getCobbleDollars(player);
                    PlayerExtensionKt.setCobbleDollars(player, currentBalance.subtract(cost));
                    player.sendMessage(Text.empty().append(Text.translatable("text.academy.safari.unlock_complete")
                            .formatted(Formatting.GRAY)));
                    entry.setUnlocked(true);
                    entry.setPrompted(true);
                    data.markDirty();
                }

                return ActionResult.SUCCESS;
            }

            if(data.isPaused()) {
                player.sendMessage(Text.empty()
                        .append(Text.translatable("text.academy.safari.initial_closed").formatted(Formatting.GRAY)));
            } else {
                player.sendMessage(Text.empty()
                        .append(Text.translatable("text.academy.safari.initial_open").formatted(Formatting.GRAY)));
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public Identifier getSkinTexture() {
        return StarAcademyMod.id("textures/entity/safari_npc.png");
    }

}

