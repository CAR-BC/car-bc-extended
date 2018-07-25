package core.connection;

//import com.sun.org.apache.xml.internal.serialize.Method;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseClassLoader extends ClassLoader{

    Connection connection = null;
    PreparedStatement ptmt = null;
    ResultSet resultSet = null;

    public DatabaseClassLoader() {
        super();
    }

    public boolean findClass(byte[] code, String contractName, Object[] parameters) {
//        byte[] data = findContract();
        Class<?> beanClass = defineClass(contractName, code, 0, code.length);
        try {
            Class<?>[] paramTypes = new Class[parameters.length];
            for (int i = 0; i<parameters.length; i++){
                paramTypes[i] = parameters[i].getClass();
            }
            Object contract = beanClass.newInstance();
            java.lang.reflect.Method method = contract.getClass().getMethod(contractName, paramTypes);
            method.invoke(contract, parameters);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }
//    private byte[] loadDataFromDatabase(String name) {
//        // this is your job.
//        try {
//            String queryString = "SELECT `code` FROM `SmartContract` WHERE 1";
//
//            connection = getConnection();
//            ptmt = connection.prepareStatement(queryString);
//            ptmt.executeUpdate();
//            System.out.println("Data Added Successfully");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try{
//                if (ptmt != null)
//                    ptmt.close();
//                if (connection != null)
//                    connection.close();
//            }catch(SQLException e){
//                e.printStackTrace();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//        return a;
//    }

    private Connection getConnection() throws SQLException {
        Connection conn;
        conn = ConnectionFactory.getInstance().getConnection();
        return conn;
    }

    public byte[] findContract() {
        byte[] result = null;
        try {
            String queryString = "SELECT `code` FROM `SmartContract` WHERE 1";
            connection = getConnection();
            ptmt = connection.prepareStatement(queryString);
            resultSet = ptmt.executeQuery();

//            File file = new File("class.bin");
//            FileOutputStream outputStream = new FileOutputStream(file);

            if (resultSet.next()){
                InputStream results = resultSet.getBinaryStream("code");
                result = resultSet.getBytes("code");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (ptmt != null)
                    ptmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {

        if (name.startsWith("com.baeldung")) {
            System.out.println("Loading Class from Custom Class Loader");
            return findClass(name);
        }
        return super.loadClass(name);
    }

}
