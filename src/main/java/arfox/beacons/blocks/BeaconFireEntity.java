package arfox.beacons.blocks;

import arfox.beacons.BeaconsMain;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class BeaconFireEntity extends BlockEntity {

    public BeaconFireEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BeaconsMain.BEACON_FIRE_ENTITY.get(), pWorldPosition, pBlockState);
    }

    public void operateBeaconsChain() {
        if (!hasLevel()) return;

        boolean lit = !getBlockState().getValue(BeaconFireBlock.LIT);
        LinkedHashSet<BeaconFireEntity> blocks = findBeaconsActively();

        blocks.forEach(block -> getLevel().setBlock(block.getBlockPos(), block.getBlockState().setValue(BeaconFireBlock.LIT, lit), 2));
    }

    private void findBeaconsPassively(LinkedHashSet<BeaconFireEntity> blocks) {
        LinkedHashSet<BeaconFireEntity> uncalledBlocks = findBlocksNearby(2);
        uncalledBlocks.removeAll(blocks);

        for (BeaconFireEntity block : Sets.newLinkedHashSet(uncalledBlocks)) {
            blocks.add(block);
            block.findBeaconsPassively(blocks);
        }
    }

    private LinkedHashSet<BeaconFireEntity> findBeaconsActively() {
        LinkedHashSet<BeaconFireEntity> blocks = findBlocksNearby(2);

        for (BeaconFireEntity block : Sets.newLinkedHashSet(blocks)) {
            block.findBeaconsPassively(blocks);
        }

        return blocks;
    }

    private LinkedHashSet<BeaconFireEntity> findBlocksNearby(int radius) {
        if (!hasLevel()) return Sets.newLinkedHashSet();

        LinkedHashSet<BeaconFireEntity> blocks = Sets.newLinkedHashSet();
        int chunkX = (getBlockPos().getX() >> 4) - radius;
        int chunkZ = (getBlockPos().getZ() >> 4) - radius;
        int diameter = radius * 2 + 1;

        for (int x = chunkX; x < (chunkX + diameter); x ++) {
            for (int z = chunkZ; z < (chunkZ + diameter); z ++) {
                LevelChunk chunk = getLevel().getChunk(x, z);
                System.out.println(x + " " + z);
                chunk.getBlockEntities().values().forEach((block) -> {
                    System.out.println(block.getBlockPos());
                    if (block instanceof BeaconFireEntity bfe && !bfe.getBlockState().getValue(BeaconFireBlock.EMPTY)) {
                        System.out.println("1");
                        blocks.add(bfe);
                    }
                });
            }
        }

        return blocks;
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BeaconFireEntity pBlockEntity) {
        Random random = pLevel.random;
        if (random.nextFloat() < 0.11F) {
            for(int i = 0; i < random.nextInt(2) + 2; ++i) {
                CampfireBlock.makeParticles(pLevel, pPos, true, false);
            }
        }
    }
}
