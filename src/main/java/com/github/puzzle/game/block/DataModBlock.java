package com.github.puzzle.game.block;

import com.badlogic.gdx.utils.Json;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.core.resources.VanillaAssetLocations;
import com.github.puzzle.game.generators.BlockGenerator;
import com.github.puzzle.game.generators.BlockModelGenerator;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @see IModBlock
 * This class allows loading regular Json files
 * as IModBlocks
 */
public class DataModBlock implements IModBlock {

    public static class JsonBlock {
        public String stringId;
        public LinkedHashMap<String, String> defaultParams;
        public LinkedHashMap<String, BlockGenerator.State> blockStates;
        public String blockEntityId;
        public LinkedHashMap<String, ?> blockEntityParams;
    }

    public ResourceLocation debugResourceLocation;
    public String blockJson;
    public String blockName;
    private Identifier identifier;

    public DataModBlock(String blockName) {
        this(blockName, VanillaAssetLocations.getBlock(blockName));
    }

    public DataModBlock(String blockName, ResourceLocation json) {
        this(blockName, json.locate().readString());
        this.debugResourceLocation = json;
    }

    public DataModBlock(String blockName, String blockJson) {
        this.blockName = blockName;
        this.blockJson = blockJson;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public BlockGenerator getBlockGenerator() {
        Json json = new Json();
        JsonBlock block = json.fromJson(JsonBlock.class, blockJson);
        identifier = Identifier.fromString(block.stringId);
        BlockGenerator generator = new BlockGenerator(getIdentifier(), blockName);
        generator.blockEntityId = block.blockEntityId;
        generator.blockEntityParams = block.blockEntityParams;
        generator.defaultParams = block.defaultParams;
        generator.blockStates = block.blockStates;
        return generator;
    }

    @Override
    public List<BlockModelGenerator> getBlockModelGenerators(Identifier blockId) {
        return List.of();
    }
}