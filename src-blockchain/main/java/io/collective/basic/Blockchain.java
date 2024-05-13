package io.collective.basic;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Blockchain {

    /*
    Keeps a tally of the number of blocks in the blockchain.
     */
    int blockchainSize;

    /*
    The initial/base block of the blockchain
     */
    Block initialBlock;

    /*
     The next block to be added into the chain, as well as any subsequent blocks to be added
     to the blockchain.
     */
    Block next;

    ArrayList<Block> blockChain;

    public Blockchain() {
        blockchainSize = 0;
        this.blockChain = new ArrayList<>();
        this.initialBlock = null;
        this.next = null;
    }

    public boolean isEmpty() { return this.blockChain.isEmpty(); }

    public void add(Block block) throws NoSuchAlgorithmException {

        if (block == null) {
            throw new NullPointerException("No block here");
        }
        this.blockChain.add(blockchainSize, block);
        blockchainSize++;
//        if (!(block.getPreviousHash().equals("0")
//                && block.getNonce() == 0)) {
//            this.initialBlock = block;
//            this.blockChain.add(blockchainSize, block);
//            blockchainSize++;
//        } else if (block.getNonce() != 0
//                && block.getHash().equals(this.next.getPreviousHash())) {
//            this.next = block;
//            this.blockChain.add(blockchainSize, block);
//            blockchainSize++;
//        }

    }

    public int size() { return this.blockChain.size(); }

    public boolean isValid() throws NoSuchAlgorithmException {

        Block curr, prev;

        // TODO check empty blockchain
        if (blockChain.isEmpty()) return true;

        // TODO check blockchain of size 1
        if (blockChain.size() == 1) {
            curr = blockChain.get(0);
            return isMined(curr) &&
                    curr.getHash().equals(curr.calculatedHash());

        }

        // TODO check blockchain of size many
        else {
            for (int i = 1; i < blockChain.size(); i++) {
                curr = blockChain.get(i);
                prev = blockChain.get(i - 1);

                if (!(isMined(prev) || isMined(curr))) return false;

                if (!curr.getPreviousHash().equals(prev.getHash())) return false;

                if (!curr.getHash().equals(curr.calculatedHash())) return false;

                if (!prev.getHash().equals(prev.calculatedHash())) return false;

                prev = curr;

            }

        }
        return true;

    }


    /// Supporting functions that you'll need.

    public static Block mine(Block block) throws NoSuchAlgorithmException {
        Block mined = new Block(block.getPreviousHash(), block.getTimestamp(), block.getNonce());

        while (!isMined(mined)) {
            mined = new Block(mined.getPreviousHash(), mined.getTimestamp(), mined.getNonce() + 1);
        }
        return mined;
    }

    public static boolean isMined(Block minedBlock) {
        return minedBlock.getHash().startsWith("00");
    }
}