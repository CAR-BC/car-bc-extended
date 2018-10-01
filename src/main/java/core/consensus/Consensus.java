package core.consensus;

import chainUtil.ChainUtil;
import core.blockchain.Block;
import core.blockchain.BlockInfo;
import core.blockchain.Transaction;
import core.connection.BlockJDBCDAO;
import core.connection.Identity;
import core.smartContract.BlockValidity;
import network.Neighbour;
import network.Node;
import core.smartContract.TimeKeeper;
import network.communicationHandler.MessageSender;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Consensus {

    private static Consensus consensus;
    private final Logger log = LoggerFactory.getLogger(Consensus.class);
    private ArrayList<Block> nonApprovedBlocks;
    private ArrayList<AgreementCollector> agreementCollectors;
    private ArrayList<Block> approvedBlocks;
    //to automate agreement process
    private ArrayList<Transaction> addedTransaction;

    private Consensus() {
        nonApprovedBlocks = new ArrayList<>();
        agreementCollectors = new ArrayList<>();
        approvedBlocks = new ArrayList<>();

    }

    public static Consensus getInstance() {
        if (consensus == null) {
            consensus = new Consensus();
        }
        return consensus;
    }

    //block broadcasting and sending agreements
    public synchronized void handleNonApprovedBlock(Block block) throws NoSuchAlgorithmException, SQLException {

        if (!isDuplicateBlock(block)) {
            nonApprovedBlocks.add(block);
            boolean isPresent = false;
            for (Block b : this.nonApprovedBlocks) {
                if (b.getBlockHeader().getPreviousHash().equals(block.getBlockHeader().getPreviousHash())) {
                    isPresent = true;
                    break;
                }
            }
            if (!isPresent) {
                TimeKeeper timeKeeper = new TimeKeeper(block.getBlockHeader().getPreviousHash());
                timeKeeper.start();
            }

            AgreementCollector agreementCollector = new AgreementCollector(block);
            agreementCollectors.add(agreementCollector);
            agreementCollector.start();

            //TODO: there is a problem here. Regardless of the block validity we anyway append the agreement collector to the arraylist. so whats the point of doing block validity below??

            //now need to check the relevant party is registered as with desired roles
            //if want, we can check the validity of the block creator/transaction creator
            BlockValidity blockValidity = new BlockValidity(block);
            if (blockValidity.isSecondaryPartyValid()) {
                //pop up notification to confirm
            }
        }
    }

    public void checkAgreementsForBlock(String preBlockHash) throws SQLException {
        ArrayList<Block> qualifiedBlocks = new ArrayList<>();
        for (Block b : this.nonApprovedBlocks) {
            if (b.getBlockHeader().getPreviousHash().equals(preBlockHash)) {
                String blockHash = b.getBlockHeader().getHash();
                AgreementCollector agreementCollector = getAgreementCollector(blockHash);

                synchronized (agreementCollectors) {
                    if (agreementCollector.getMandatoryValidators().size() == 0) {
                        if (agreementCollector.getAgreements().size() >= agreementCollector.getThreshold()) {
                            qualifiedBlocks.add(b);
                            this.agreementCollectors.remove(agreementCollector);
                        }
                    } else {
                        //blocks with insufficient agreements
                    }
                }
            }
        }
        addBlockToBlockchain(qualifiedBlocks);
    }

    public Block selectQualifiedBlock(ArrayList<Block> qualifiedBlocks) throws SQLException {
        Block qualifiedBlock = null;

        if (qualifiedBlocks.size() != 0) {
            qualifiedBlock = qualifiedBlocks.get(0);
            Timestamp blockTimestamp = qualifiedBlock.getBlockHeader().getBlockTime();

            synchronized (nonApprovedBlocks) {
                for (Block b : qualifiedBlocks) {
                    if (blockTimestamp.after(b.getBlockHeader().getBlockTime())) {
                        this.nonApprovedBlocks.remove(qualifiedBlock);
                        qualifiedBlock = b;
                        blockTimestamp = b.getBlockHeader().getBlockTime();
                    } else {
                        this.nonApprovedBlocks.remove(b);
                    }
                }
            }
            //TODO: for now we discard all delayed blocks. only consider blocks that got enough agreements within the specific time period
//            this.approvedBlocks.add(qualifiedBlock);
        } else {
            //need to restart the timer again
        }
        return qualifiedBlock;
    }

    public void addBlockToBlockchain(ArrayList<Block> qualifiedBlocks) throws SQLException {
        Block block = selectQualifiedBlock(qualifiedBlocks);

        if (block != null) {
            BlockInfo blockInfo = new BlockInfo();
            blockInfo.setPreviousHash(block.getBlockHeader().getPreviousHash());
            blockInfo.setHash(block.getBlockHeader().getHash());
            blockInfo.setBlockTime(block.getBlockHeader().getBlockTime());
            blockInfo.setBlockNumber(block.getBlockHeader().getBlockNumber());
            blockInfo.setTransactionId(block.getBlockBody().getTransaction().getTransactionId());
            blockInfo.setSender(block.getBlockBody().getTransaction().getSender());
            blockInfo.setEvent(block.getBlockBody().getTransaction().getEvent());
            blockInfo.setData(block.getBlockBody().getTransaction().getData().toString());
            blockInfo.setAddress(block.getBlockBody().getTransaction().getAddress());

            Identity identity = null;
            if (block.getBlockBody().getTransaction().getTransactionId().substring(0, 1).equals("I")) {
                JSONObject body = block.getBlockBody().getTransaction().getData();
                String publicKey = body.getString("publicKey");
                String role = body.getString("role");
                String name = body.getString("name");

                identity = new Identity(block.getBlockHeader().getHash(), publicKey, role, name);
            }
            //TODO: need to check that this is the right block to add based on the previous hash
            BlockJDBCDAO blockJDBCDAO = new BlockJDBCDAO();
            blockJDBCDAO.addBlockToBlockchain(blockInfo, identity);
        }
    }

    //no need of synchronizing
    public boolean isDuplicateBlock(Block block) {
        if (nonApprovedBlocks.contains(block)) {
            return true;
        }
        return false;
    }

    //no need of synchronizing
    public void sendAgreementForBlock(Block block) {
        String blockHash = ChainUtil.getInstance().getBlockHash(block);
        String signedBlock = ChainUtil.getInstance().digitalSignature(blockHash);
        MessageSender.getInstance().sendAgreement(signedBlock, blockHash);
        System.out.println("agreement sent");
    }

    //no need of synchronizing
    public void handleAgreement(Agreement agreement) {
        getAgreementCollector(agreement.getBlockHash()).addAgreementForBlock(agreement);
    }

    //no need of synchronizing
    private AgreementCollector getAgreementCollector(String id) {
        for (AgreementCollector agreementCollector : this.agreementCollectors) {
            if (agreementCollector.getAgreementCollectorId().equals(id)) {
                return agreementCollector;
            }
        }
        return null;
    }

    //no need of synchronizing
    public void handleReceivedAgreement(String signature, String signedBlock, String blockHash, String publicKey) {
        handleAgreement(new Agreement(signature, blockHash, signedBlock, publicKey));
    }


    public void requestAdditionalData(Block block) {
        String blockHash = ChainUtil.getInstance().getBlockHash(block.getBlockBody());

    }

    public void handleAdditionalDataRequest(String ip, int listeningPort, String signedBlock, String blockHash, String peerID) {
        String data = getAdditionalDataForBlock(blockHash).toString();
    }

    public JSONObject getAdditionalDataForBlock(String blockHash) {
        return new JSONObject();
    }

    public ArrayList<Block> getBlocksToBeAdded() {
        return approvedBlocks;
    }

}
