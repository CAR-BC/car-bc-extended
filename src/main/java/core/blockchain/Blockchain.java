package core.blockchain;

import java.util.LinkedList;

public class Blockchain {
    private  LinkedList<Block> blockchainArray;
    private static Blockchain blockchain;

    private Blockchain() {
        this.blockchainArray = new LinkedList<Block>();
        this.blockchainArray.add(Block.createGenesis());
    }

    public static Blockchain getBlockchain() {
        if (blockchain == null){
            blockchain = new Blockchain();
        }
        return blockchain;
    }

    public  LinkedList<Block> getBlockchainArray() {
        return blockchainArray;
    }

    public void addBlock(Block block){
        this.blockchainArray.add(block);
    }


    public  void rollBack(long blockNumber){
        LinkedList<Block> validBlockchain = new LinkedList<Block>();
        if (blockchainArray.size()>(int)blockNumber){
            validBlockchain = (LinkedList<Block>) blockchainArray.subList(0, (int) blockNumber);
            blockchainArray = validBlockchain;
        }
    }


    public Block getBlockByNumber(long blockNumber){
        if (blockchainArray.size()>(int)blockNumber){
            Block block = blockchainArray.get((int) blockNumber);
            return block;
        }
        return null;
    }
}