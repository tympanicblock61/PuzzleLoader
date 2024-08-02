package dev.crmodders.puzzle.loader.entrypoint.interfaces;

import dev.crmodders.puzzle.annotations.Stable;
import dev.crmodders.puzzle.util.PuzzleEntrypointUtil;

@Stable
public interface ModInitializer {
    String ENTRYPOINT_KEY = "init";

    void onInit();

    static void invokeEntrypoint() {
        PuzzleEntrypointUtil.invoke(ENTRYPOINT_KEY, ModInitializer.class, ModInitializer::onInit);
    }
}
