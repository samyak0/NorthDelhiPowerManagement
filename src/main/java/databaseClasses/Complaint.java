package databaseClasses;

import org.bson.Document;

public class Complaint {

    public Complaint() {}
    public Complaint(String customerId, String meterId, String title, String message, String status) {
        this.customerId = customerId;
        this.meterId = meterId;
        this.title = title;
        this.message = message;
        this.status = status;
    }
    public Complaint(String id, String customerId, String meterId, String title, String message, String status) {
        this.id = id;
        this.customerId = customerId;
        this.meterId = meterId;
        this.title = title;
        this.message = message;
        this.status = status;
    }

    private String id;
    private String customerId;
    private String meterId;
    private String title;
    private String message;
    private String status;

    public Document toDoc() {
        Document doc = new Document()
                .append("customerId", this.customerId)
                .append("meterId", this.meterId)
                .append("title", this.title)
                .append("message", this.message)
                .append("status", this.status);
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

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
