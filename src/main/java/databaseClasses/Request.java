package databaseClasses;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Request {

    public Request(String customerId, boolean isApproved, RequestType requestType, String email) {
        this.customerId = customerId;
        this.isApproved = isApproved;
        this.requestType = requestType;
        this.email = email;
    }
    public Request(String id, String customerId, boolean isApproved, RequestType requestType, String email) {
        this.id = id;
        this.customerId = customerId;
        this.isApproved = isApproved;
        this.requestType = requestType;
        this.email = email;
    }

    private String id;
    private String customerId;
    private boolean isApproved;
    private RequestType requestType;
    private String email;


    public enum RequestType {
        NEW_CONNECTION
    }

    public Document toDoc() {
        Document doc =  new Document()
                .append("customerId", this.customerId)
                .append("isApproved", this.isApproved)
                .append("requestType", this.requestType.name())
                .append("email", this.email);
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
}

