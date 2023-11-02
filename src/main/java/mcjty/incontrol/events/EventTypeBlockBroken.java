package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.tools.rules.CommonRuleEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class EventTypeBlockBroken implements EventType {

    private List<BiPredicate<LevelAccessor, BlockPos>> blocks;

    public EventTypeBlockBroken() {
    }

    @Override
    public Type type() {
        return Type.BLOCK_BROKEN;
    }

    public List<BiPredicate<LevelAccessor, BlockPos>> getBlocks() {
        return blocks;
    }

    @Override
    public boolean parse(JsonObject object) {
        blocks = new ArrayList<>();
        JsonElement block = object.get("block");
        if (block.isJsonArray()) {
            for (JsonElement element : block.getAsJsonArray()) {
                BiPredicate<LevelAccessor, BlockPos> predicate = CommonRuleEvaluator.parseBlock(element.getAsString());
                if (predicate == null) {
                    return false;
                }
                blocks.add(predicate);
            }
        } else {
            BiPredicate<LevelAccessor, BlockPos> predicate = CommonRuleEvaluator.parseBlock(block.getAsString());
            if (predicate == null) {
                return false;
            }
            blocks.add(predicate);
        }
        if (blocks.isEmpty()) {
            ErrorHandler.error("No blocks specified!");
            return false;
        }
        return true;
    }
}
