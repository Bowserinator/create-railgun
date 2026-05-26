package net.hellomouse.createrailgun.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Trail {
    public static final int MAX_LIFE = 300;

    private final int entityId;
    private final Vec3 startPos;
    private Vec3 endPos;
    private Vec3 prevEndPos;
    private int life;

    public Trail(int entityId, Vec3 startPos) {
        this.entityId = entityId;
        this.startPos = startPos;
        this.endPos = startPos;
        this.prevEndPos = startPos;
        this.life = MAX_LIFE;
    }

    public void tick(ClientLevel level) {
        this.prevEndPos = this.endPos;

        // Try to find the projectile to update the end of the line
        Entity entity = level.getEntity(this.entityId);
        if (entity != null && entity.isAlive())
            this.endPos = entity.position();
        this.life--;
    }

    public boolean isDead() {
        return this.life <= 0;
    }

    public Vec3 getStartPos() { return startPos; }
    public Vec3 getEndPos() { return endPos; }
    public Vec3 getPrevEndPos() { return prevEndPos; }
    public int getLife() { return life; }
    public int getId() { return entityId; }
}
