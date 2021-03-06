package TestCases;

import controller.Controller;
import fakeAgreementSender.AgreementSender;
import network.communicationHandler.MessageSender;
import chainUtil.KeyGenerator;
import com.google.gson.JsonObject;
import controller.Controller;
import org.json.JSONArray;
import org.json.JSONObject;

public class RegisterVehicleTest {
    public static void main(String[] args) throws InterruptedException {
        startNodeTest();
//        Thread.sleep(3000);
//        sendAgreements();

    }

    public static void startNodeTest() throws InterruptedException {
        Controller controller = new Controller();
        controller.startNode();
        MessageSender.requestIP();
//        Thread.sleep(20000);
//        sendAgreements();
        controller.sendTransaction("RegisterVehicle", "B", createRegisterJSON() );
    }

    public static JSONObject createRegisterJSON(){
        JSONObject registration = new JSONObject();
        registration.put("registrationNumber", "A");
        registration.put("currentOwner", "3081f13081a806072a8648ce38040130819c024100fca682ce8e12caba26efccf7110e526db078b05edecbcd1eb4a208f3ae1617ae01f35b91a47e6df63413c5e12ed0899bcd132acd50d99151bdc43ee737592e17021500962eddcc369cba8ebb260ee6b6a126d9346e38c50240678471b27a9cf44ee91a49c5147db1a9aaf244f05a434d6486931d2d14271b9e35030b71fd73da179069b32e2935630e1c2062354d0da20a6c416e50be794ca4034400024100b4e8f0dc4e2d34820dcbb0218437ef914cfcfdb79fc3161ae9a6453381251ddc1c95d11780aff761761a4168b753cf335a016c662e54a2b96267a7727697616b");
        registration.put("engineNumber", "A");
        registration.put("chassisNumber", "A");
        registration.put("make", "Toyota");
        registration.put("model", "Axio");
        JSONObject secondaryParty = new JSONObject();
        JSONObject thirdParty = new JSONObject();
        registration.put("SecondaryParty", secondaryParty);
        registration.put("ThirdParty", thirdParty);

        return registration;
    }

    public static JSONObject createRegisterJSON2(){
        JSONObject registration = new JSONObject();
        registration.put("registrationNumber", "C");
        registration.put("currentOwner", "3081f13081a806072a8648ce38040130819c024100fca682ce8e12caba26efccf7110e526db078b05edecbcd1eb4a208f3ae1617ae01f35b91a47e6df63413c5e12ed0899bcd132acd50d99151bdc43ee737592e17021500962eddcc369cba8ebb260ee6b6a126d9346e38c50240678471b27a9cf44ee91a49c5147db1a9aaf244f05a434d6486931d2d14271b9e35030b71fd73da179069b32e2935630e1c2062354d0da20a6c416e50be794ca4034400024100b4e8f0dc4e2d34820dcbb0218437ef914cfcfdb79fc3161ae9a6453381251ddc1c95d11780aff761761a4168b753cf335a016c662e54a2b96267a7727697616b");
        registration.put("engineNumber", "C");
        registration.put("chassisNumber", "C");
        registration.put("make", "Toyota");
        registration.put("model", "Audi");
        JSONObject secondaryParty = new JSONObject();
        JSONObject thirdParty = new JSONObject();

        registration.put("SecondaryParty", secondaryParty);
        registration.put("ThirdParty", thirdParty);

        return registration;
    }

    public static void sendAgreements(){
        String[] orgs = {"ServiceStation", "RMV", "SparePartShop", "GodFather"};
        AgreementSender agreementSender = new AgreementSender();
        for( String org : orgs) {
            System.out.println(org);
            agreementSender.sendFakeAgreements(org);
        }
    }


}
