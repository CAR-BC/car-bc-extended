package core.smartContract;

import core.blockchain.*;
import core.connection.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws Exception {

//        TransactionDummy tr = new TransactionDummy();
//        tr.executeSmartContractMethod();

//        VehicleHistoryJDBCDAO smartContractJDBCDAO = new VehicleHistoryJDBCDAO();
//        VehicleHistory vehicleHistory = new VehicleHistory("a","a","a","a","a","a","a","a");
//
//        smartContractJDBCDAO.add(vehicleHistory);

//        System.out.println((getClassFromFile("VehicleRegistration")));

        Connection connection = null;
        PreparedStatement ptmt = null;
        ResultSet resultSet = null;

        add(connection, ptmt);

//        DatabaseClassLoader databaseClassLoader = new DatabaseClassLoader();
//        Class<?> cl = databaseClassLoader.findClass("VehicleRegistration");

    }


    public static void add(Connection connection, PreparedStatement ptmt) throws FileNotFoundException {

        File file = new File("/home/sajinie/Desktop/VehicleHistory.class");
        FileInputStream input = new FileInputStream(file);

        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try {
            String queryString = "INSERT INTO `SmartContract`(`signature`,`contractName`, `code`, " +
                    "`owner`, `message`, `block_number`, `block_timestamp`, `block_hash`) VALUES(?,?,?,?,?,?,?,?)";

            connection = ConnectionFactory.getInstance().getConnection();
            ptmt = connection.prepareStatement(queryString);
            ptmt.setString(1, "sa");
            ptmt.setString(2, "sa");
            ptmt.setBinaryStream(3, input);
            ptmt.setString(4, "sa");
            ptmt.setString(5, "sa");
            ptmt.setInt(6, 3);
            ptmt.setTimestamp(7, timestamp);
            ptmt.setString(8, "aaa");
            ptmt.executeUpdate();
            System.out.println("Data Added Successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try{
                if (ptmt != null)
                    ptmt.close();
                if (connection != null)
                    connection.close();
            }catch(SQLException e){
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String getFileContent(FileInputStream fis) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader r = new InputStreamReader(fis, "UTF-8");  //or whatever encoding
        int ch = r.read();
        while(ch >= 0) {
            sb.append(ch);
            ch = r.read();
        }
        return sb.toString();
    }

    public static String readCodeFromFile(String fileLocation) throws IOException {
        File file = new File(fileLocation);
        FileInputStream input = new FileInputStream(file);
        String contractCode = getFileContent(input);
        return contractCode;
    }

    public static InputStream getContractInputStream(String contract){
        InputStream stream = new ByteArrayInputStream(contract.getBytes(StandardCharsets.UTF_8));
        return stream;
    }


    public void executeTransaction(Block block) throws SQLException {
        //getting block details
        BlockHeader blockHeader = block.getHeader();
        int blockNumber = (int)blockHeader.getBlockNumber();
        String blockHash = blockHeader.getHash();
        Timestamp blockTimestamp = blockHeader.getTimestamp();

        //getting transaction details
        Transaction transaction = block.getTransaction();

        String transactionID = transaction.getTransactionID();
        String sender = transaction.getSender();
        ArrayList<Validation> validation = transaction.getValidations();
        TransactionInfo transactionInfo = transaction.getTransactionInfo();

        //getting transactionInfo
        String smartContractName = transactionInfo.getSmartContractName();
        String smartContractSignature = transactionInfo.getSmartContractSignature();
        String smartContractMethod = transactionInfo.getSmartContractMethod();
        Object[] parameters = transactionInfo.getParameters();
        String event = transactionInfo.getEvent();
        String data = transactionInfo.getData();
        String vehicleID = transactionInfo.getVehicleId();

        //making validation array
        int noOfValidators = validation.size();
        String validationArray[][] = new String[1][noOfValidators];
        for (int i=0; i<noOfValidators; i++){
            Validation validations = validation.get(i);
            validationArray[i][0] = validations.getValidator().getValidator();
            validationArray[i][1] = validations.getSignature().toString();
        }

        boolean isSuccess = false;
        if (event.equals("deployContract")){
            isSuccess = deploySmartContract(smartContractSignature, smartContractName, data,
                    sender, data, blockNumber, blockTimestamp, blockHash);
        }else{
            isSuccess = addVehicleRecord(vehicleID, transactionID, blockNumber, blockHash,
                    event, sender, validationArray, data, smartContractSignature, smartContractMethod,
                    parameters);
        }

    }

    public boolean addVehicleRecord(String vID, String transactionID, int blockNumber, String blockHash,
                                           String event, String sender, String[][] validationArray,
                                           String data, String smartContractSignature,
                                           String smartContractMethod, Object[] parameters) throws SQLException {

        VehicleHistoryJDBCDAO vehicleHistoryJDBCDAO = new VehicleHistoryJDBCDAO();
        VehicleHistory vehicleHistory = new VehicleHistory(vID, transactionID,
                blockNumber, blockHash, event, sender, validationArray.toString(), data,
                smartContractSignature, smartContractMethod, parameters);

        //find contract
        SmartContractJDBCDAO smartContractJDBCDAO = new SmartContractJDBCDAO();
        Map contract = smartContractJDBCDAO.getSmartContract(smartContractSignature);

        //find vehicle
        ResultSet resultSet = vehicleHistoryJDBCDAO.findVehicle(vehicleHistory);
        DatabaseClassLoader databaseClassLoader = new DatabaseClassLoader();

        if(event.equals("vehicleRegistration")){
            //check possibility
            if (resultSet.next()){
                return false;
            }
            vID = transactionID + blockHash;
            vehicleHistory.setVid(vID);
            vehicleHistoryJDBCDAO.add(vehicleHistory);
        }else{
            boolean success = databaseClassLoader.findClass((byte[])contract.get("code"),
                    (String)contract.get("contractName"), parameters);
        }
        return true;
    }

    public boolean deploySmartContract(String signature, String contractName, String code,
                                           String owner, String message, int block_number,
                                           Timestamp block_timestamp, String block_hash){

        SmartContractJDBCDAO smartContractJDBCDAO = new SmartContractJDBCDAO();
        SmartContract smartContract = new SmartContract(signature, contractName, code,
                owner, message, block_number, block_timestamp, block_hash);

        boolean contractExists = smartContractJDBCDAO.findDuplicates(smartContract);
        if (!contractExists){
            smartContractJDBCDAO.add(smartContract);
            return true;
        }
        return false;
    }

}




























//        String message;
//        JSONObject obj = new JSONObject();
//        JSONParser parser = new JSONParser();

//        obj.put("name", "foo");
//        obj.put("num", new Integer(100));
//        obj.put("balance", new Double(1000.21));
//        obj.put("is_vip", new Boolean(true));
//
//        byte[] objAsBytes = obj.toString().getBytes("UTF-8");
//
//        System.out.println(objAsBytes.toString());
//
//
//
//        JSONObject testV = new JSONObject(new String(objAsBytes));
//
//        System.out.println(testV.toString());
//        String testV=new JSONObject(new String(responseBody)).toString();