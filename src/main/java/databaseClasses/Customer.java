package databaseClasses;

import org.bson.Document;
import org.bson.types.ObjectId;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Customer {

    public Customer() {}

    public Customer(String id, String name, String password, String phone, String altPhone, String address, String email, String securityQuestion, String securityAnswer, int age, String meterId, int readingUnits, boolean isPaused, boolean isRemoved, boolean isDefaulter, List<Document> history) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.altPhone = altPhone;
        this.address = address;
        this.email = email;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.age = age;
        this.meterId = meterId;
        this.readingUnits = readingUnits;
        this.isPaused = isPaused;
        this.isRemoved = isRemoved;
        this.isDefaulter = isDefaulter;
        this.history = (history == null || history.size() == 0) ? new ArrayList<>() : history.stream().map(Util::docToUsage).collect(Collectors.toList());
    }

    private String id;
    private String name;
    private String password;
    private String phone;
    private String altPhone;
    private String address;
    private String email;
    private String securityQuestion;
    private String securityAnswer;
    private int age;
    private String meterId;
    private int readingUnits;
    private boolean isPaused;
    private boolean isRemoved;
    private boolean isDefaulter;
    private List<Usage> history = new ArrayList<>();

    public Document toDoc() {
        Document doc = new Document()
                .append("name", this.name)
                .append("password", this.password)
                .append("phone", this.phone)
                .append("altPhone", this.altPhone)
                .append("address", this.address)
                .append("email", this.email)
                .append("age", this.age)
                .append("securityQuestion", this.securityQuestion)
                .append("securityAnswer", this.securityAnswer)
                .append("meterId", this.meterId)
                .append("readingUnits", this.readingUnits)
                .append("isPaused", this.isPaused)
                .append("isRemoved", this.isRemoved)
                .append("isDefaulter", this.isDefaulter)
                .append("history", this.history.stream().map(Util::usageToDoc).collect(Collectors.toList()));
        if(this.id != null){
            doc.append("_id", new ObjectId(this.id));
        }
        return doc;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAltPhone() {
        return altPhone;
    }

    public void setAltPhone(String altPhone) {
        this.altPhone = altPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public int getReadingUnits() {
        return readingUnits;
    }

    public void setReadingUnits(int readingUnits) {
        this.readingUnits = readingUnits;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public List<Usage> getHistory() {
        return history;
    }

    public void setHistory(List<Usage> history) {
        this.history = history;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDefaulter() {
        return isDefaulter;
    }

    public void setDefaulter(boolean defaulter) {
        isDefaulter = defaulter;
    }
}

