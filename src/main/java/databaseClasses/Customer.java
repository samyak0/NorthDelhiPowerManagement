package databaseClasses;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    public Customer() {}
    public Customer(String name, String phone, String altPhone, String address, String email, int age) {
        this.name = name;
        this.phone = phone;
        this.altPhone = altPhone;
        this.address = address;
        this.email = email;
        this.age = age;
    }

    public Customer(String id, String name, String password, String phone, String altPhone, String address, String email, int age, String meterId, int readingUnits, boolean isPaused, boolean isRemoved, boolean isDefaulter, List<Usage> history) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.altPhone = altPhone;
        this.address = address;
        this.email = email;
        this.age = age;
        this.meterId = meterId;
        this.readingUnits = readingUnits;
        this.isPaused = isPaused;
        this.isRemoved = isRemoved;
        this.isDefaulter = isDefaulter;
        this.history = history;
    }

    private String id;
    private String name;
    private String password;
    private String phone;
    private String altPhone;
    private String address;
    private String email;
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
                .append("password", hashPassword(this.password))
                .append("phone", this.phone)
                .append("altPhone", this.altPhone)
                .append("address", this.address)
                .append("email", this.email)
                .append("age", this.age)
                .append("meterId", this.meterId)
                .append("readingUnits", this.readingUnits)
                .append("isPaused", this.isPaused)
                .append("isRemoved", this.isRemoved)
                .append("isDefaulter", this.isDefaulter)
                .append("history", this.history);
        if(this.id != null){
            doc.append("_id", this.id);
        }
        return doc;
    }

    private String hashPassword(String password){
        return String.valueOf(password.hashCode());
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

