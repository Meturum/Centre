package com.meturum.centre.util.input;

import com.meturum.centra.input.SignTextInput;
import com.meturum.centre.sessions.SessionImpl;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public final class SignTextInputImpl implements SignTextInput {

    private final SessionImpl owner;
    private final SignTextInputPacketHandler handler;

    private String[] lines = new String[]{"", "", "", ""}; // The editable lines
    private String[] result = new String[]{"", "", "", ""}; // The result lines (Read-only)

    private boolean isEditing = false;

    private @Nullable UpdateLambda updateLambda = null;

    public SignTextInputImpl(@NotNull final SessionImpl owner) {
        this.owner = owner;
        this.handler = new SignTextInputPacketHandler(this);
    }

    public @NotNull SessionImpl getOwner() {
        return owner;
    }

    public @NotNull String[] getLines() {
        return lines.clone();
    }

    public SignTextInputImpl setLines(@NotNull final String[] lines) {
        if(lines.length != 4)
            throw new IllegalArgumentException("The lines array must have a length of 4.");

        this.lines = lines;

        return this;
    }

    public @NotNull String[] getResult() {
        return result.clone();
    }

    private void setResult(@NotNull final String[] result) {
        if(result.length != 4)
            throw new IllegalArgumentException("The result array must have a length of 4.");

        this.result = result;

        update();
    }

    public boolean isEditing() {
        return isEditing;
    }

    private void setEditing(final boolean editing) {
        isEditing = editing;
    }

    public @NotNull SignTextInputImpl setUpdateLambda(@NotNull final UpdateLambda updateLambda) {
        this.updateLambda = updateLambda;

        return this;
    }

    public void open(final boolean force) {
        if(isEditing()) return; setEditing(true); // Toggle editing state.
        this.owner.setTextInput(this); // Set the text input to the session.

        Player owner = this.owner.getPlayer();

        ServerPlayer player = ((CraftPlayer) owner).getHandle();
        if(!(owner.getOpenInventory() instanceof PlayerInventory))
            owner.closeInventory(); // Close the current inventory if it's not the player's inventory.

        if(player.isImmobile()) return; // If the player is frozen, don't open the sign editor.

        Location location = owner.getLocation();
        BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        BlockState originalState = player.level.getBlockState(position); // The current block state at the location.
        BlockState state = Blocks.OAK_SIGN.defaultBlockState();

        player.connection.send(new ClientboundBlockUpdatePacket(position, state)); //  Send a fake sign block update.

        SignBlockEntity entity = new SignBlockEntity(position, state);
        Component[] components = CraftSign.sanitizeLines(lines);

        for (int i = 0; i < components.length; i++) {
            entity.setMessage(i, components[i]);
        }

        player.connection.send(ClientboundBlockEntityDataPacket.create(entity)); // update the sign text
        player.connection.send(new ClientboundOpenSignEditorPacket(position)); // Open that fake sign.
        player.connection.send(new ClientboundBlockUpdatePacket(position, originalState)); // Replace the fake sign with the original block.

        player.connection.connection.channel.pipeline().addBefore("packet_handler", "sign_packet_handler", handler);
    }

    public @NotNull SignTextInputImpl open() {
        open(true);

        return this;
    }

    private void update() {
        if(!isEditing()) return; // If the player is not editing, do nothing.
        setEditing(false); // Toggle editing state.

        if(updateLambda != null)
            updateLambda.run(result, lines);

        lines = result;
        owner.setTextInput(null); // Remove the text input from the session.
    }

    public static class SignTextInputPacketHandler extends ChannelDuplexHandler {

        private final SignTextInputImpl input;

        public SignTextInputPacketHandler(@NotNull final SignTextInputImpl input) {
            this.input = input;
        }

        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            if (msg instanceof ServerboundSignUpdatePacket packet) {
                String[] lines = packet.getLines();

                input.setResult(lines);

                ctx.pipeline().remove(this);
            }

            super.channelRead(ctx, msg);
        }

    }

}
