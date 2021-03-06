package controller;

import chainUtil.ChainUtil;
import chainUtil.KeyGenerator;
import config.CommonConfigHolder;
import constants.Constants;
import core.blockchain.*;
import core.consensus.Consensus;
import core.consensus.DataCollector;
import core.serviceStation.webSocketServer.webSocket.WebSocketMessageHandler;
import network.Client.RequestMessage;
import network.Neighbour;
import network.Node;
import network.Protocol.MessageCreator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.sql.SQLException;
import java.text.ParseException;

public class Controller {

    private final Logger log = LoggerFactory.getLogger(Controller.class);


    public void requestTransactionDataTest(String type, String vehicleID, String date, String peerID) {
        DataCollector.getInstance().requestTransactionData(type, vehicleID, date, peerID);
    }

    public void sendTransaction(String event, String vehicleID, JSONObject data) {
        BlockSender blockSender = new BlockSender(event, vehicleID, data);
        blockSender.start();
    }

    public void sendServiceTransaction(String vehicleID, JSONObject data) {
        String sender = KeyGenerator.getInstance().getPublicKeyAsString();
        String nodeID = Node.getInstance().getNodeConfig().getNodeID();

//        JSONObject dataJson = new JSONObject();
//        dataJson.put("serviceType", data.get("serviceType"));
//        dataJson.put("servicedDate", data.get("servicedDate"));
//        dataJson.put("SparePartSerialNumber", data.get("SparePartSerialNumber"));
//        dataJson.put("SparePartShop", data.get("SparePartShop"));
//
//        JSONObject secondaryParty = new JSONObject();
//        JSONObject serviceStation = new JSONObject();
//        serviceStation.put("name", data.get("name"));
//        serviceStation.put("publicKey", data.get("name"));
//        secondaryParty.put("serviceStation", serviceStation);
//
//        JSONObject thirdParty = new JSONObject();
//        thirdParty.put("SparePartProvider", data.get("SparePartProvider"));
//        dataJson.put("SecondaryParty", secondaryParty);
//        dataJson.put("ThirdParty", thirdParty);

        data.put("cost", ChainUtil.getHash(data.getString("cost")));
        Transaction transaction = new Transaction("V", sender, "ServiceRepair", data.toString(), vehicleID);

        BlockBody blockBody = new BlockBody();
        blockBody.setTransaction(transaction);
        String blockHash = ChainUtil.getInstance().getBlockHash(blockBody);
        BlockHeader blockHeader = new BlockHeader(blockHash);

        Block block = new Block(blockHeader, blockBody);
        System.out.println(new JSONObject(block));
        Consensus.getInstance().broadcastBlock(block, data.toString());
    }

    public void sendInsureTransaction(String vehicleID, JSONObject data) {
        String sender = KeyGenerator.getInstance().getPublicKeyAsString();
        Transaction transaction = new Transaction("V", sender, "Insure", data.toString(), vehicleID);

        BlockBody blockBody = new BlockBody();
        blockBody.setTransaction(transaction);
        String blockHash = ChainUtil.getInstance().getBlockHash(blockBody);
        BlockHeader blockHeader = new BlockHeader(blockHash);

        Block block = new Block(blockHeader, blockBody);
        System.out.println(new JSONObject(block));
        Consensus.getInstance().broadcastBlock(block, data.toString());
    }

    public void sendOwnershipTransaction(String vehicleID, JSONObject data) {
        String sender = KeyGenerator.getInstance().getPublicKeyAsString();
        Transaction transaction = new Transaction("V", sender, "Insure", data.toString(), vehicleID);

        BlockBody blockBody = new BlockBody();
        blockBody.setTransaction(transaction);
        String blockHash = ChainUtil.getInstance().getBlockHash(blockBody);
        BlockHeader blockHeader = new BlockHeader(blockHash);

        Block block = new Block(blockHeader, blockBody);
        System.out.println(new JSONObject(block));
        Consensus.getInstance().broadcastBlock(block, data.toString());
    }

    public void sendConfirmation(String blockHash) {
        Consensus.getInstance().sendAgreementForBlock(blockHash);
    }

    public void requestAdditionalData(String blockHash, String sender) {
        DataCollector.getInstance().requestAdditionalData(blockHash, sender);
    }

    public String getCarBCno(String vehicleID) {
        return vehicleID;
    }


    public void resendBlock(Block block, String data) {
        block.getBlockBody().getTransaction().setTime();
        block.getBlockHeader().setPreviousHash(Blockchain.getPreviousHash());
        block.getBlockHeader().setBlockNumber(Blockchain.getRecentBlockNumber()+1);
        block.getBlockHeader().setHash(ChainUtil.getBlockHash(block.getBlockBody()));
        block.getBlockHeader().setSignature(ChainUtil.digitalSignature(block.getBlockHeader().getHash()));
        Consensus.getInstance().broadcastBlock(block, data);
    }


    public void handleAdditionalDataRequest(String blockHash, Neighbour dataRequester) throws SQLException {
        log.info("Additional Data Request for block: {} From: {} ",blockHash, dataRequester.getPeerID());
        WebSocketMessageHandler.giveAdditionalData(blockHash, dataRequester.getPeerID());
    }

    public void sendAddtionalDataForRequester(String blockHash, Neighbour dataRequester) {
        DataCollector.getInstance().sendAdditionalData(blockHash, dataRequester);
    }

    public void notifyReceivedAdditionalData() {
        log.info("Additional Data Received");
    }

    public void searchVehicle(String vehicleID) {

    }

    //test methods
    public void testNetwork(String ip, int listeningPort, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);
        RequestMessage testMessage = MessageCreator.createMessage(jsonObject, "Test");
        Node.getInstance().sendMessageToPeer(ip, listeningPort, testMessage);
    }

    public void startNode() {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

        /*
         * Set the main directory as home
         * */
        System.setProperty(Constants.CARBC_HOME, System.getProperty("user.dir"));

        /*
         * At the very beginning
         * A Config common to all: network, blockchain, etc.
         * */
        CommonConfigHolder commonConfigHolder = CommonConfigHolder.getInstance();
        commonConfigHolder.setConfigUsingResource("peer2");

        /*
         * when initializing the network
         * */
        Node node = Node.getInstance();
        node.initTest();

        /*
         * when we want our node to start listening
         * */
        node.startListening();
    }

}
