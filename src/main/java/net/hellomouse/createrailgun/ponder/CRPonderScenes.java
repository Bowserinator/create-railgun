package net.hellomouse.createrailgun.ponder;

import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.core.Direction;

public class CRPonderScenes {
    public static final PonderStoryBoard RAILGUN_PART_1 = (scene, util) -> {
        scene.title("railgun_intro_1", "Assembling the Railgun: Phase 1");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        var coreBlock = util.select().position(5, 1, 3);
        var restOfSchematic = util.select().layersFrom(1).substract(coreBlock);
        scene.world().showSection(coreBlock, Direction.DOWN);
        scene.idle(15);

        scene.world().showSection(restOfSchematic, Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(60)
                .text("text1")
                .pointAt(util.vector().topOf(5, 1, 3))
                .placeNearTarget();
        scene.idle(70);
    };

    public static final PonderStoryBoard RAILGUN_PART_2 = (scene, util) -> {
        scene.title("railgun_intro_2", "text2");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("text3")
                .pointAt(util.vector().centerOf(5, 1, 2))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("text4")
                .pointAt(util.vector().blockSurface(util.grid().at(5, 1, 3), Direction.WEST))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Provide rotational power here")
                .pointAt(util.vector().blockSurface(util.grid().at(5, 2, 3), Direction.WEST))
                .placeNearTarget();
        scene.idle(70);
    };
}