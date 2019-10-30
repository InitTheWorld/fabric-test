package org.hyperledger.fabric.chaincode.Models;

public class Wallet {//nodeid;dataid;datavalue;datatype;
    private String nodeid;
    private String dataid;
    private Double datavalue;
    private String datatype;
    private String timestamp;
    private String taskid;
    

    public Wallet(String nodeid,String dataid, Double datavalue,String datatype,String timestamp,String taskid) {
        this.nodeid = nodeid;
	this.dataid = dataid;
        this.datavalue = datavalue;
        this.datatype = datatype;
        this.timestamp = timestamp;
        this.taskid = taskid;

    }
    
    private Wallet() {}

    public String getdataid() {
        return this.dataid;
    }

    public Double getdatavalue() {
        return this.datavalue;
    }

    public String getdatatype() {
        return this.datatype;
    }

    public String getnodeid() {
        return this.nodeid;
    }

    public String gettimestamp() {
        return this.timestamp;
    }

    public String gettaskid() {
        return this.taskid;
    }

    public void setdatatid(String dataid) {
        this.dataid = dataid;
    }

    public void setdatavalue(Double datavalue) {
        this.datavalue = datavalue;
    }

    public void setdatatype(String datatype) {
        this.datatype = datatype;
    }

    public void setnodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public void settimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void settaskid(String taskid) {
        this.taskid = taskid;
    }

}
