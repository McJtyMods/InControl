package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.tools.rules.TestingBlockTools;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.function.BiPredicate;

public class EventTypeBlockBroken implements EventType {

    private BiPredicate<LevelAccessor, BlockPos> predicate;

    public EventTypeBlockBroken() {
    }

    @Override
    public Type type() {
        return Type.BLOCK_BROKEN;
    }

    public BiPredicate<LevelAccessor, BlockPos> getBlockTest() {
        return predicate;
    }

    @Override
    public boolean parse(JsonObject object) {
        JsonElement block = object.get("block");
        if (block == null) {
            ErrorHandler.error("Block broken event has no 'block' parameter!");
            return false;
        }
        predicate = TestingBlockTools.parseBlockJson(block);
        return predicate != null;
    }
}
