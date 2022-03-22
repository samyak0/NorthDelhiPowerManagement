package functionClasses;

import utils.MongoDbUtils;
import utils.Util;
import databaseClasses.Complaint;
import databaseClasses.Customer;
import databaseClasses.Request;
import databaseClasses.Usage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Scanner;

import static com.mongodb.client.model.Filters.*;

public class CustomerFunctions {

    private static final Scanner scn = new Scanner(System.in);
    private Customer currentUser;

    public void startAuthenticationFlow(){
        System.out.print("\033[H\033[2J"); // To clear the terminal
        int choice = 0;

        while (choice != 3) {
            showAuthenticationMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.print("\033[H\033[2J"); // To clear the terminal

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {}
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }

        }
    }


    private void showAuthenticationMenu(){
        System.out.println("------ Customer Login ------");
        System.out.println(" 1. Register New Connection");
        System.out.println(" 2. Login");
        System.out.println(" 3. Go Back");
        System.out.println("-----------------------");
    }


    private void register(){

        Customer customer = new Customer();
        System.out.print("\033[H\033[2J"); // To clear the terminal
        System.out.println("--- Registration ---");
        System.out.println("Please fill in the details below to register for a new connection");

        customer.setName(Util.inputName());
        customer.setEmail(Util.inputEmail());
        customer.setAge(Util.inputAge());

        while(true) {
            String phone = Util.inputPhone("Phone: ");

            //Check if phone number is used
            Bson query = eq("phone", phone);
            if(MongoDbUtils.CUSTOMERCOLLECTION.find(query).iterator().hasNext())
                System.out.println("Phone number already in use, please use another number.");
            else {
                customer.setPhone(phone);
                break;
            }
        }

        customer.setAltPhone(Util.inputPhone("Alternate Phone: "));
        customer.setAddress(Util.inputAddress());

        while (true) {
                String pass = Util.inputPassword("Password: ");
                String rePass = Util.inputPassword("Re-enter Password: ");
                if(!pass.equals(rePass))
                    System.out.println("Passwords do not match. Please re-try.");
                else {
                    customer.setPassword(pass);
                    break;
                }
        }

        try {
            //Insert Customer Document in Customer Collection.
            Document doc = customer.toDoc();
            MongoDbUtils.CUSTOMERCOLLECTION.insertOne(doc);

            //Create a request for admin to Approve.
            Request req = new Request(doc.getObjectId("_id").toString(), false, Request.RequestType.NEW_CONNECTION, null);
            Document reqDoc = req.toDoc();
            MongoDbUtils.REQUESTCOLLECTION.insertOne(reqDoc);
        } catch (Exception ignore) {
            System.out.println("Some error occurred. Please try again later.");
            return;
        }

        System.out.println("Account Created Successfully.");
        System.out.println("New connection request sent successfully.");
        System.out.println("Please wait for admin to approve your request.");
    }


    private void login() {
        String phone = Util.inputPhone("Please enter your Phone No.: ");
        String password = Util.inputPassword("Password: ");

        Bson filter = and(
                eq("phone", phone),
                eq("password", String.valueOf(password.hashCode()))
        );
        Document doc =  MongoDbUtils.CUSTOMERCOLLECTION
                .find(filter)
                .limit(1)
                .iterator()
                .tryNext();

        if (doc == null){
            System.out.println("!! No user found, Wrong ID or Password !!");
            login();
        }

        this.currentUser = Util.docToCustomer(doc);
        System.out.format("Logged In Successfully, welcome %S !!\n", currentUser.getName());
        startMainFlow();
    }


    private void startMainFlow(){
        System.out.print("\033[H\033[2J"); // To clear the terminal
        Scanner scn = new Scanner(System.in);
        int choice = 0;

        while (choice != 8) {
            showMainMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.print("\033[H\033[2J"); // To clear the terminal

            switch (choice) {
                case 1 -> showAccountDetails();
                case 2 -> updateAccountDetails();
                case 3 -> showUsageHistory();
//                case 4 -> TODO()
                case 5 -> registerComplaint();
                case 6 -> terminateConnection();
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
        System.out.format("Phone: \t\t%s\n", currentUser.getAltPhone());
        System.out.format("Alt. Phone: \t%s\n", currentUser.getAddress());
        System.out.format("Meter ID: \t\t%s\n", currentUser.getMeterId());
        System.out.format("Meter Reading: \t%s\n", currentUser.getReadingUnits());
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
            System.out.print("\033[H\033[2J"); // To clear the terminal

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
                case 3 -> {
                    String newPhone = Util.inputPhone("New Phone: ");

                    Bson query = and(
                            eq("phone", newPhone),
                            not(eq("_id", new ObjectId(currentUser.getId())))
                    );
                    if(MongoDbUtils.CUSTOMERCOLLECTION.find(query).iterator().hasNext())
                        System.out.println("Phone number already in use, please use another number.");
                    else {
                        Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"phone", newPhone);
                    }
                }
                case 4 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"name", Util.inputPhone("Alternate Phone: "));
                case 5 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"email", Util.inputEmail());
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
        if(currentUser.getHistory() == null){
            System.out.println("No usage history found.");
            return;
        }
        System.out.println(
                "MONTH\tREADING\tUNITS\tAMOUNT\tPAID"
        );
        System.out.println("------------------------------------");
        for(Usage usage: currentUser.getHistory()) {
            System.out.format("%s\t%d\t%d\t%d\t%b",
                    usage.month.toUpperCase(),
                    usage.readingUnits,
                    usage.usedUnits,
                    usage.billAmount,
                    usage.paid
            );
        }
    }


    private void payBill(){

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
    }


    private void terminateConnection() {
        if(currentUser.getMeterId() == null)
            System.out.println("No connection found with your id.");
        else if(currentUser.isPaused())
            System.out.println("Please pay your bill before terminating your connection");
        else if(currentUser.isRemoved())
            System.out.println("Your connection is already terminated.");
        else{
            Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, currentUser.getId(),"isRemoved", true);
            System.out.println("Connection terminated successfully");
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
