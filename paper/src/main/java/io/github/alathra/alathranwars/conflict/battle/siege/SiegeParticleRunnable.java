package io.github.alathra.alathranwars.conflict.battle.siege;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.conflict.battle.BattleRunnable;
import io.github.alathra.alathranwars.packet.ParticleCircle;
import org.bukkit.Location;

/**
 * A runnable which sends a packets to players on the battlefield drawing a circle around the capture point.
 */
public class SiegeParticleRunnable extends BattleRunnable {
    private final SiegeImpl siege;

    public SiegeParticleRunnable(SiegeImpl siege) {
        super(1);
        this.siege = siege;
    }

    @Override
    public void run() {
        final Location controlPoint = siege.getControlPoint();

        if (controlPoint == null)
            return;

        final Location loc = controlPoint.clone().add(0, 0.4, 0);
        final Town t = siege.getTown();
        siege.getPlayersOnBattlefield().forEach(player -> {
            ParticleCircle.sendCircle(player, loc, SiegeRunnable.CAPTURE_RANGE, 90, AlathranWarsAPIImpl.getInstance().getColor(player, t));
        });
    }
}
