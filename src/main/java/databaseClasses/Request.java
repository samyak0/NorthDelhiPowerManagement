package databaseClasses;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

public class Request {

    public Request(String customerId, boolean isApproved, RequestType requestType, String email) {
        this.customerId = customerId;
        this.isApproved = isApproved;
        this.requestType = requestType;
        this.email = email;
    }
    public Request(String id, String customerId, boolean isApproved, RequestType requestType, String email, List<String> paidMonths) {
        this.id = id;
        this.customerId = customerId;
        this.isApproved = isApproved;
        this.requestType = requestType;
        this.email = email;
        this.paidMonths = paidMonths;
    }

    public Request(String customerId, boolean isApproved, RequestType requestType, String email, List<String> paidMonths) {
        this.customerId = customerId;
        this.isApproved = isApproved;
        this.requestType = requestType;
        this.email = email;
        this.paidMonths = paidMonths;
    }

    private String id;
    private String customerId;
    private boolean isApproved;
    private RequestType requestType;
    private String email;
    private List<String> paidMonths;


    public enum RequestType {
        NEW_CONNECTION,
        BILL_PAYMENT,
        TERMINATION
    }

    public Document toDoc() {
        Document doc =  new Document()
                .append("customerId", this.customerId)
                .append("isApproved", this.isApproved)
                .append("requestType", this.requestType.name())
                .append("email", this.email)
                .append("paidMonths", this.paidMonths);
        if(this.id != null){
            doc.append("_id", new ObjectId(this.id));
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getPaidMonths() {
        return paidMonths;
    }

    public void setPaidMonths(List<String> paidMonths) {
        this.paidMonths = paidMonths;
    }
}

