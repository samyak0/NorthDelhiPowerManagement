package databaseClasses;

import org.bson.Document;

public class Request {

    public Request(){}
    public Request(String customerId, boolean isApproved, RequestType requestType, Customer updatedCustomer) {
        this.customerId = customerId;
        this.isApproved = isApproved;
        this.requestType = requestType;
        this.updatedCustomer = updatedCustomer;
    }
    public Request(String id, String customerId, boolean isApproved, RequestType requestType, Customer updatedCustomer) {
        this.id = id;
        this.customerId = customerId;
        this.isApproved = isApproved;
        this.requestType = requestType;
        this.updatedCustomer = updatedCustomer;
    }

    private String id;
    private String customerId;
    private boolean isApproved;
    private RequestType requestType;
    private Customer updatedCustomer;


    public enum RequestType {
        NEW_CONNECTION,
        UPDATE_CONNECTION
    }

    public Document toDoc() {
        Document doc =  new Document()
                .append("customerId", this.customerId)
                .append("isApproved", this.isApproved)
                .append("requestType", this.requestType.name())
                .append("updatedCustomer", this.updatedCustomer);
        if(this.id != null){
            doc.append("_id", this.id);
        }
        return doc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Customer getUpdatedCustomer() {
        return updatedCustomer;
    }

    public void setUpdatedCustomer(Customer updatedCustomer) {
        updatedCustomer = updatedCustomer;
    }
}

