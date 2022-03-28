package functionClasses;

import databaseClasses.Complaint;
import databaseClasses.Customer;
import databaseClasses.Request;
import databaseClasses.Usage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import utils.MongoDbUtils;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class CustomerFunctions {

    private static final Scanner scn = new Scanner(System.in);
    private Customer currentUser;

    public void startAuthenticationFlow(){
        int choice = 0;

        while (choice != 4) {
            System.out.println();
            showAuthenticationMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    register();
                    System.out.println("press any key to continue.");
                    scn.nextLine();
                }
                case 2 -> login();
                case 3 -> {
                    forgotPassword();
                    System.out.println("press any key to continue.");
                    scn.nextLine();
                }
                case 4 -> {}
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }
        }
    }


    private void showAuthenticationMenu(){
        System.out.println("------ Customer Login ------");
        System.out.println(" 1. Register New Connection");
        System.out.println(" 2. Login");
        System.out.println(" 3. Forgot Password");
        System.out.println(" 4. Go Back");
        System.out.println("-----------------------");
    }

    private void register(){

        Customer customer = new Customer();
        System.out.println();
        System.out.println("--- Registration ---");
        System.out.println("Please fill in the details below to register for a new connection");

        customer.setName(Util.inputName());
        customer.setAge(Util.inputAge());

        while(true) {
            String email = Util.inputEmail();

            //Check if email is used
            Bson query = eq("email", email);
            if(MongoDbUtils.CUSTOMERCOLLECTION.find(query).iterator().hasNext())
                System.out.println("Email already in use, please use another email.");
            else {
                customer.setEmail(email);
                break;
            }
        }

        customer.setPhone(Util.inputPhone("Phone Number: "));
        String altPhone;
        while (true) {
            altPhone = Util.inputPhone("Alternate Phone: ");
            if(altPhone.equals(customer.getPhone()))
                System.out.println("!! Main phone and alternate phone number cannot be same !!");
            else
                break;
        }
        customer.setAltPhone(altPhone);
        customer.setAddress(Util.inputAddress());

        while (true) {
                String pass = Util.inputPassword("Password: ");
                String error = Util.validatePassword(pass);
                if(error != null) {
                    System.out.println(error);
                    continue;
                }
                String rePass = Util.inputPassword("Re-enter Password: ");
                if(!pass.equals(rePass))
                    System.out.println("Passwords do not match. Please re-try.");
                else {
                    customer.setPassword(pass);
                    break;
                }
        }

        customer.setSecurityQuestion(Util.inputSecurityQuestion());
        customer.setSecurityAnswer(Util.inputSecurityAnswer());
        customer.setPaused(true);
        try {
            //Insert Customer Document in Customer Collection.
            customer.setPassword(String.valueOf(customer.getPassword().hashCode()));
            Document doc = customer.toDoc();
            MongoDbUtils.CUSTOMERCOLLECTION.insertOne(doc);

            //Create a request for admin to Approve.
            Request req = new Request(doc.getObjectId("_id").toString(), false, Request.RequestType.NEW_CONNECTION, doc.getString("email"));
            Document reqDoc = req.toDoc();
            MongoDbUtils.REQUESTCOLLECTION.insertOne(reqDoc);
        } catch (Exception ignore) {
            System.out.println("Some error occurred. Please try again later.");
            return;
        }

        System.out.println();
        System.out.println("Account Created Successfully.");
        System.out.println("New connection request sent successfully.");
        System.out.println("Please wait for admin to approve your request.");
    }


    private void login() {
        String email = Util.inputEmail();
        String password = Util.inputPassword("Password: ");

        Bson filter = and(
                eq("email", email),
                eq("password", String.valueOf(password.hashCode()))
        );
        Document doc =  MongoDbUtils.CUSTOMERCOLLECTION
                .find(filter)
                .limit(1)
                .iterator()
                .tryNext();

        if (doc == null){
            System.out.println("!! No user found, Wrong email or Password !!");
            System.out.println("press any key to continue.");
            scn.nextLine();
        }else {
            this.currentUser = Util.docToCustomer(doc);
            System.out.format("Logged In Successfully, welcome %S !!\n", currentUser.getName());
            startMainFlow();
        }
    }

    private void forgotPassword() {

        System.out.println();
        System.out.println("Enter you email: ");
        String email = Util.inputEmail();
        Bson query = eq("email", email);
        Document doc = MongoDbUtils.CUSTOMERCOLLECTION.find(query).limit(1).iterator().tryNext();
        if(doc == null){
            System.out.println("No user with this email found");
            return;
        }
        Customer customer = Util.docToCustomer(doc);
        String securityQuestion = customer.getSecurityQuestion();
        String securityAnswer = customer.getSecurityAnswer();
        System.out.println("Please answer your security question:");
        System.out.println(securityQuestion);
        System.out.print("Answer: ");
        String answer = scn.nextLine();

        if(answer.equals(securityAnswer)){
            System.out.println("Correct answer, please enter New Password.");
            while (true) {
                String pass = Util.inputPassword("New Password: ");
                String error = Util.validatePassword(pass);
                if(error != null) {
                    System.out.println(error);
                    continue;
                }
                String rePass = Util.inputPassword("Re-enter Password: ");
                if(!pass.equals(rePass))
                    System.out.println("Passwords do not match. Please re-try.");
                else {
                    customer.setPassword(pass);
                    break;
                }
            }
            try{
                Util.updateKeyValue(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(), "password", String.valueOf(customer.getPassword().hashCode()));
                System.out.println("Password Updated Successfully");
            } catch (Exception ignore) {
                System.out.println("Error occurred, please try again later.");
            }
        }else
            System.out.println("Invalid answer.");
    }

    private void startMainFlow(){
        Scanner scn = new Scanner(System.in);
        int choice = 0;

        while (choice != 7) {
            System.out.println();
            showMainMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    showAccountDetails();
                    System.out.println("press any key to continue.");
                    scn.nextLine();
                }
                case 2 -> updateAccountDetails();
                case 3 -> {
                    showUsageHistory();
                    System.out.println("press any key to continue.");
                    scn.nextLine();
                }
                case 4 -> {
                    payBill();
                    System.out.println("press any key to continue.");
                    scn.nextLine();
                }
                case 5 -> {
                    registerComplaint();
                    System.out.println("press any key to continue.");
                    scn.nextLine();
                }
                case 6 -> {
                    terminateConnection();
                    System.out.println("press any key to continue.");
                    scn.nextLine();
                }
                case 7 -> logout();
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }
            if (currentUser == null) break;
        }
    }


    private  void showMainMenu(){
        System.out.println("------ Customer Menu ------");
        System.out.println(" 1. Check Account Details");
        System.out.println(" 2. Update Account Details");
        System.out.println(" 3. Check Usage Details");
        System.out.println(" 4. Pay Bill");
        System.out.println(" 5. Register Complaint");
        System.out.println(" 6. Terminate Connection");
        System.out.println(" 7. Logout");
        System.out.println("-----------------------");
    }


    private void showAccountDetails() {
        System.out.println("--- Account Details ---");
        System.out.format("NAME: \t\t%s\n", currentUser.getName());
        System.out.format("AGE: \t\t%s\n", currentUser.getAge());
        System.out.format("EMAIL: \t\t%s\n", currentUser.getEmail());
        System.out.format("PHONE: \t\t%s\n", currentUser.getPhone());
        System.out.format("ALT. PHONE: \t%s\n", currentUser.getAltPhone());
        System.out.format("ADDRESS: \t%s\n", currentUser.getAddress());
        System.out.format("METER ID: \t%s\n", currentUser.getMeterId());
        System.out.format("UNITS USED: \t%s\n", currentUser.getReadingUnits());
        System.out.println("----------------------");
    }


    private void updateAccountDetails() {
        int choice = 0;
        System.out.println("--- UPDATE DETAILS PAGE ---");
        System.out.println("What do you want to update ?");
        while(choice != 8){
            System.out.println("1. Name");
            System.out.println("2. Password");
            System.out.println("3. Phone Number");
            System.out.println("4. Alternate Number");
            System.out.println("5. Email");
            System.out.println("6. Age");
            System.out.println("7. Address");
            System.out.println("8. Go Back");
            System.out.println("-------------------");
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.println();

            switch (choice) {
                case 1 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"name", Util.inputName());
                case 2 -> {
                    String currPass = Util.inputPassword("Current Password: ");
                    String newPass = Util.inputPassword("New Password: ");

                    Bson filter = and(
                            eq("_id", new ObjectId(currentUser.getId())),
                            eq("password", String.valueOf(currPass.hashCode()))
                    );
                    Document doc = MongoDbUtils.CUSTOMERCOLLECTION.find(filter).limit(1).iterator().tryNext();
                    if(doc != null){
                        Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"password", String.valueOf(newPass.hashCode()));
                    }else
                        System.out.println("Wrong password, please try again");
                }
                case 3 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"phone", Util.inputPhone("New Phone: "));
                case 4 -> {
                    String altPhone;
                    while (true) {
                        altPhone = Util.inputPhone("Alternate Phone: ");
                        if(altPhone.equals(currentUser.getAltPhone()))
                            System.out.println("!! Main phone and alternate phone number cannot be same !!");
                        else
                            break;
                    }
                    Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"altPhone", altPhone);
                }
                case 5 -> {
                    while(true) {
                        String email = Util.inputEmail();

                        //Check if email is used
                        Bson query = eq("email", email);
                        if(MongoDbUtils.CUSTOMERCOLLECTION.find(query).iterator().hasNext())
                            System.out.println("Email already in use, please use another email.");
                        else {
                            currentUser.setEmail(email);
                            break;
                        }
                    }
                    Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"email", currentUser.getEmail());
                }
                case 6 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"age", Util.inputAge());
                case 7 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"address", Util.inputAddress());
                case 8 -> {}
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }
        }
        refreshData();
    }


    private void showUsageHistory() {
        System.out.println("--- Usage History ---");
        if(currentUser.getHistory() == null || currentUser.getHistory().isEmpty()){
            System.out.println("No usage history found.");
            return;
        }
        System.out.println(
                "MONTH\t\tREADING\t\tUNITS\t\tAMOUNT\t\tPAID"
        );
        System.out.println("----------------------------------------------------");
        for(Usage usage: currentUser.getHistory()) {
            System.out.format("%s\t\t%d\t\t%d\t\t%f\t\t%b\n",
                    usage.month.toUpperCase(),
                    usage.readingUnits,
                    usage.usedUnits,
                    usage.billAmount,
                    usage.paid
            );
        }
    }


    private void payBill(){
        if (currentUser.getMeterId() == null){
            System.out.println("No meter allotted to you !!");
            return;
        } else if (currentUser.getHistory().isEmpty()) {
            System.out.println("No usage for your meter found !!");
            return;
        } else if (!currentUser.getHistory().get(0).paid.equals("unpaid")){
            System.out.println("All bills paid !!");
            return;
        }

        double totalPrice = 0;
        for(Usage u : currentUser.getHistory()) {
            if(!u.paid.equals("unpaid")) break;
            totalPrice += u.billAmount;
            System.out.println("----------------");
            System.out.format("Period: %s, %s\n", u.month, u.year);
            System.out.format("\tTotal usage: %d units\n", u.readingUnits);
            System.out.format("\tThis month Usage: %d units\n", u.usedUnits);
            System.out.format("\tBill Amount: %f\n", u.billAmount);
            System.out.format("\tPaid: %S\n", u.paid);
        }
        System.out.println("--------------------");
        System.out.format("Your total bill amount is %f\n", totalPrice);

        int choice;
        while(true){
            System.out.println("Press 1 to pay your bill\n or 0 to go back: ");
            choice = scn.hasNextInt() ? scn.nextInt() : -1;
            if (choice != 0 && choice != 1)
                System.out.println("Please enter a valid choice !!");
            else
                break;
        }

        if (choice == 1){
            List<String> months = new ArrayList<>();
            for (Usage u : currentUser.getHistory()){
                if (!u.paid.equals("unpaid")) break;
                u.paid = "pending approval";
                months.add(u.month + ", " + u.year);
            }
            if(currentUser.isDefaulter()){
                currentUser.setDefaulter(false);
                currentUser.setPaused(false);
            }

            Document docToCustomer = currentUser.toDoc();
            Bson query = eq("_id", new ObjectId(currentUser.getId()));
            MongoDbUtils.CUSTOMERCOLLECTION.replaceOne(query, docToCustomer);

            Request req = new Request(currentUser.getId(), false, Request.RequestType.BILL_PAYMENT, currentUser.getEmail(), months);
            Document reqDoc = req.toDoc();
            MongoDbUtils.REQUESTCOLLECTION.insertOne(reqDoc);

            System.out.println("Bill paid successfully, please wait for admin to approve your payment");
        }

    }


    private void registerComplaint() {
        Complaint complaint = new Complaint();
        complaint.setCustomerId(currentUser.getId());
        complaint.setMeterId(currentUser.getMeterId());
        System.out.println("Enter Complaint title: ");
        complaint.setTitle(scn.nextLine());
        System.out.println("Enter Complaint: ");
        complaint.setMessage(scn.nextLine());

        MongoDbUtils.COMPLAINTCOLLECTION.insertOne(complaint.toDoc());
        System.out.println("Complaint Registered Successfully !!");
    }


    private void terminateConnection() {
        if(currentUser.getMeterId() == null)
            System.out.println("No connection found with your id.");
        else if(currentUser.isPaused())
            System.out.println("Please pay your bill before terminating your connection");
        else if(currentUser.isRemoved())
            System.out.println("Your connection is already terminated.");
        else{
            try {
                //Create a request for admin to Approve.
                Request req = new Request(currentUser.getId(), false, Request.RequestType.TERMINATION, currentUser.getEmail());
                Document reqDoc = req.toDoc();
                MongoDbUtils.REQUESTCOLLECTION.insertOne(reqDoc);
                //Pause the connection
                Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(), "isPaused", true);

                System.out.println("Request for termination submitted.");
                System.out.println("Connection paused till then...");
            }catch (Exception ignore){
                System.out.println("Something went wrong, please try again.");
            }
        }
        refreshData();
    }

    private void logout() {
        currentUser = null;
    }


    private void refreshData(){
        Bson filter = eq("_id", new ObjectId(currentUser.getId()));
        Document doc =  MongoDbUtils.CUSTOMERCOLLECTION
                .find(filter)
                .limit(1)
                .iterator()
                .tryNext();

        if (doc == null){
            System.out.println("!! Something went wrong, Please try logging in again !!");
            logout();
            return;
        }

        this.currentUser = Util.docToCustomer(doc);
    }

}
