package org.hyperledger.fabric.chaincode;
import java.util.List;
import org.hyperledger.fabric.chaincode.Models.Wallet;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccountBasedChaincode extends ChaincodeBase {
    private class ChaincodeResponse {
        public String message;
        public String code;
        public boolean OK;

        public ChaincodeResponse(String message, String code, boolean OK) {
            this.code = code;
            this.message = message;
            this.OK = OK;
        }
    }

    private String responseError(String errorMessage, String code) {
        try {
            return (new ObjectMapper()).writeValueAsString(new ChaincodeResponse(errorMessage, code, false));
        } catch (Throwable e) {
            return "{\"code\":'" + code + "', \"message\":'" + e.getMessage() + " AND " + errorMessage + "', \"OK\":" + false + "}";
        }
    }

    private String responseSuccess(String successMessage) {
        try {
            return (new ObjectMapper()).writeValueAsString(new ChaincodeResponse(successMessage, "", true));
        } catch (Throwable e) {
            return "{\"message\":'" + e.getMessage() + " BUT " + successMessage + " (NO COMMIT)', \"OK\":" + false + "}";
        }
    }

    private String responseSuccessObject(String object) {
        return "{\"message\":" + object + ", \"OK\":" + true + "}";
    }

    private boolean checkString(String str) {
        if (str.trim().length() <= 0 || str == null)
            return false;
        return true;
    }
//init

    @Override

    public Response init(ChaincodeStub stub) {
    	String nodeid = "nodeid";
    	String dataid = "dataid";
    	String datavalue = "0";
	    String datatype = "datatype";
	    String timestamp = "timestamp";
	    String taskid = "taskid";
	    double tokenAmountDouble = 0.0;
	    tokenAmountDouble = Double.parseDouble(datavalue);
	    Wallet wallet = new Wallet(nodeid,dataid,tokenAmountDouble,datatype,timestamp,taskid);

        try {
	    String func = stub.getFunction();
            if (!func.equals("init")) {
                return newErrorResponse("function other than init is not supported");
            }

            if(checkString(stub.getStringState(nodeid)))
            	return newSuccessResponse();
                //return newErrorResponse(responseError("Existent wallet", ""));
            stub.putState(nodeid, (new ObjectMapper()).writeValueAsBytes(wallet));
            return newSuccessResponse();  //responseSuccess("Wallet created")
        } catch (Throwable e) {
            return newErrorResponse(e);//responseError(e.getMessage(), "")
        }
    }
    @Override

//invoke
    public Response invoke(ChaincodeStub stub) {
        String func = stub.getFunction();
        List<String> params = stub.getParameters();
        if (func.equals("createWallet"))
            return createWallet(stub, params);
        else if (func.equals("getWallet"))
            return getWallet(stub, params);
        else if (func.equals("transfer"))
            return transfer(stub, params);
        else if (func.equals("delete"))
            return delete(stub, params);
        return newErrorResponse(responseError("Unsupported method", ""));
    }
//create{nodeid;dataid;datavalue;datatype;timestamp;taskid}
    private Response createWallet(ChaincodeStub stub, List<String> args) {
        if (args.size() != 6)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 6", ""));
        String nodeid = args.get(0);
	    String dataid = args.get(1);
        String datavalue = args.get(2);
	    String datatype = args.get(3);
	    String timestamp = args.get(4);
	    String taskid = args.get(5);
	
        if (!checkString(dataid) || !checkString(datavalue)||!checkString(datatype)||!checkString(nodeid)||!checkString(timestamp)||!checkString(taskid))
            return newErrorResponse(responseError("Invalid argument(s)", ""));
	
	    double tokenAmountDouble = Double.parseDouble(datavalue);

        Wallet wallet = new Wallet(nodeid,dataid,tokenAmountDouble,datatype,timestamp,taskid);

        try {
            if(checkString(stub.getStringState(nodeid)))
                return newErrorResponse(responseError("Existent wallet", ""));
            stub.putState(nodeid, (new ObjectMapper()).writeValueAsBytes(wallet));
            return newSuccessResponse(responseSuccess("Wallet created"));
        } catch (Throwable e) {
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

//get{nodeid}
    private Response getWallet(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 1", ""));
        String nodeid = args.get(0);
        if (!checkString(nodeid))
            return newErrorResponse(responseError("Invalid argument", ""));
        try {
            String walletString = stub.getStringState(nodeid);
            if(!checkString(walletString))
                return newErrorResponse(responseError("Nonexistent wallet", ""));
            return newSuccessResponse((new ObjectMapper()).writeValueAsBytes(responseSuccessObject(walletString)));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

// Deletes an entity from state{nodeid}
    private Response delete(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            return newErrorResponse("Incorrect number of arguments. Expecting 1");
        }
        String key = args.get(0);
        // Delete the key from the state in ledger
        stub.delState(key);
        return newSuccessResponse("success delete");
    }

//transfer{fromnodeid;tonodeid;datavalue}
    private Response transfer(ChaincodeStub stub, List<String> args) {
        if (args.size() != 3)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 3", ""));
        String fromnodeid = args.get(0);
        String tonodeid = args.get(1);
        String datavalue = args.get(2);
        if (!checkString(fromnodeid) || !checkString(tonodeid) || !checkString(datavalue))
            return newErrorResponse(responseError("Invalid argument(s)", ""));
        if(fromnodeid.equals(tonodeid))
            return newErrorResponse(responseError("From-node is same as to-noid", ""));

        double tokenAmountDouble = 0.0;       
        tokenAmountDouble = Double.parseDouble(datavalue);
            

        try {

            String fromnodeString = stub.getStringState(fromnodeid);
            if(!checkString(fromnodeString))
                return newErrorResponse(responseError("Nonexistent from-node", ""));
            String tonodeString = stub.getStringState(tonodeid);
            if(!checkString(tonodeString))
                return newErrorResponse(responseError("Nonexistent to-node", ""));

            ObjectMapper objectMapper = new ObjectMapper();            
            Wallet fromnode = objectMapper.readValue(fromnodeString, Wallet.class);
            Wallet tonode = objectMapper.readValue(tonodeString, Wallet.class);

            if(fromnode.getdatavalue() < tokenAmountDouble)
                return newErrorResponse(responseError("Token amount not enough", ""));

            fromnode.setdatavalue(fromnode.getdatavalue()-tokenAmountDouble);
            tonode.setdatavalue(tonode.getdatavalue()+tokenAmountDouble);
         
            stub.putState(fromnodeid,objectMapper.writeValueAsBytes(fromnode));
            stub.putState(tonodeid,objectMapper.writeValueAsBytes(tonode));
            //stub.putStringState(fromnodeid,objectMapper.writeValueAsString(fromnode));
            //stub.putStringState(tonodeid,objectMapper.writeValueAsString(tonode));
            
            return newSuccessResponse(responseSuccess("Transferred"));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    public static void main(String[] args) {
        new AccountBasedChaincode().start(args);
    }
}
