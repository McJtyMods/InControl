package mcjty.tools.varia;

public class DummyCommandSender {} /* @todo 1.15 implements ICommandSender {

    private final World world;
    private final PlayerEntity player;

    public DummyCommandSender(World world, PlayerEntity player) {
        this.world = world;
        this.player = player;
    }

    @Override
    public String getName() {
        return "dummy";
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("dummy");
    }

    @Override
    public void sendMessage(ITextComponent component) {
        System.out.println(component.getFormattedText());
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName) {
        return true;
    }

    @Override
    public BlockPos getPosition() {
        return new BlockPos(0, 0, 0);
    }

    @Override
    public Vec3d getPositionVector() {
        return new Vec3d(0, 0, 0);
    }

    @Override
    public World getEntityWorld() {
        return world;
    }

    @Nullable
    @Override
    public Entity getCommandSenderEntity() {
        return player;
    }

    @Override
    public boolean sendCommandFeedback() {
        return false;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {

    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return world.getMinecraftServer();
    }
}
*/