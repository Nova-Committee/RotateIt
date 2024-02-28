package committee.nova.rotateit;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod(RotateIt.MODID)
public class RotateIt {
    public static final String MODID = "rotateit";
    public static final TagKey<Item> rotateTools = ItemTags.create(new ResourceLocation(MODID, "rotate_tools"));
    public static final TagKey<Block> rotatable = BlockTags.create(new ResourceLocation(MODID, "rotatable"));
    private static final Map<Direction, Direction> aFacingMap;
    private static final Map<Direction, Direction> hFacingMap;

    public RotateIt() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getHand().equals(InteractionHand.MAIN_HAND)) return;
        final Level level = event.getLevel();
        if (level.isClientSide) return;
        final Player player = event.getEntity();
        if (!player.isShiftKeyDown()) return;
        if (!event.getItemStack().is(rotateTools)) return;
        final BlockPos pos = event.getHitVec().getBlockPos();
        final BlockState state = level.getBlockState(pos);
        if (!state.is(rotatable)) return;
        final SoundType sound = state.getSoundType();
        state.getOptionalValue(BlockStateProperties.FACING).ifPresent(d -> {
            final Direction next = aFacingMap.get(d);
            if (next == null) return;
            level.setBlock(pos, state.setValue(BlockStateProperties.FACING, next), 3);
            level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            event.setCanceled(true);
        });
        if (!event.isCanceled()) state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).ifPresent(d -> {
            final Direction next = hFacingMap.get(d);
            if (next == null) return;
            level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, next), 3);
            level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            event.setCanceled(true);
        });
    }

    static {
        Map<Direction, Direction> m = new HashMap<>();
        Direction[] values = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        int l = values.length;
        for (int i = 0; i < l; i++) m.put(values[i], values[(i + 1) % l]);
        hFacingMap = ImmutableMap.copyOf(m);
        m = new HashMap<>();
        values = Direction.values();
        l = values.length;
        for (int i = 0; i < l; i++) m.put(values[i], values[(i + 1) % l]);
        aFacingMap = ImmutableMap.copyOf(m);
    }
}
