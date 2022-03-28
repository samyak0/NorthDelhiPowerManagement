package functionClasses;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import databaseClasses.Complaint;
import databaseClasses.Customer;
import databaseClasses.Request;
import databaseClasses.Usage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import utils.MongoDbUtils;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.*;

public class AdminFunctions {

    private static final Scanner scn = new Scanner(System.in);
    private String adminId;

    public void startAuthenticationFlow(){
        int choice = 0;

        while (choice != 2) {
            System.out.println();
            showAuthenticationMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.println();

            switch (choice) {
                case 1 -> login();
                case 2 -> {}
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }
        }
    }


    private void showAuthenticationMenu(){
        System.out.println("------ Admin Login ------");
        System.out.println(" 1. Login");
        System.out.println(" 2. Go Back");
        System.out.println("-----------------------");
    }


    private void login() {
        String id = Util.inputID("Admin ID: ");
        String password = Util.inputPassword("Password: ");

        Bson filter = and(
                eq("adminId", id),
                eq("password", String.valueOf(password.hashCode()))
        );
        Document doc =  MongoDbUtils.ADMINCOLLECTION
                .find(filter)
                .limit(1)
                .iterator()
                .tryNext();

        if (doc == null){
            System.out.println("!! No admin found, Wrong ID or Password !!");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        }else {
            this.adminId = doc.getString("adminId");
            System.out.format("Logged In Successfully, welcome %S !!\n", doc.getString("name"));
            startMainFlow();
        }
    }


    private void logout() {
        this.adminId = null;
    }


    private void startMainFlow(){
        Scanner scn = new Scanner(System.in);
        int choice = 0;

        while (choice != 10) {
            System.out.println();
            showMainMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.println();

            switch (choice) {
                case 1 -> approveRequests();
                case 2 -> viewComplaints();
                case 3 -> generateReadings();
                case 4 -> viewCustomerData();
                case 5 -> viewUsageDetails();
                case 6 -> register();
                case 7 -> updateAccountDetails();
                case 8 -> pauseConnection();
                case 9 -> terminateConnection();
                case 10 -> logout();
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }
            if (this.adminId == null) break;
        }
    }


    private  void showMainMenu(){
        System.out.println("------ Admin Menu ------");
        System.out.println(" 1. Approve Pending Requests");
        System.out.println(" 2. Customer Complaints");
        System.out.println("-----------------------");
        System.out.println(" 3. Generate connection readings");
        System.out.println(" 4. View Customer Details");
        System.out.println(" 5. View Usage Details");
        System.out.println("-----------------------");
        System.out.println(" 6. Add New Connection");
        System.out.println(" 7. Update Connection Details");
        System.out.println(" 8. Pause/Resume Connection");
        System.out.println(" 9. Terminate connection");
        System.out.println("-----------------------");
        System.out.println(" 10. Logout");
        System.out.println("-----------------------");
    }

    private static void approveRequests(){
        System.out.println();

        Bson query = eq("isApproved", false);
        ArrayList<Document> requests = new ArrayList<>();
        MongoDbUtils.REQUESTCOLLECTION.find(query).into(requests);


        System.out.println("--- REQUESTS ---");
        if (requests.isEmpty()) {
            System.out.println("No requests found !!");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        }
        else{
            for(int i = 0; i < requests.size(); ++i){
                System.out.println("---------------");
                Request request = Util.docToRequest(requests.get(i));
                System.out.format("%d. %s\n", i+1, request.getEmail());
                System.out.format("\tRequest Type: %s\n",request.getRequestType());
            }
            System.out.println("---------------");
            System.out.println();

            int choice = -1;
            while(choice != 0){
                System.out.println("Enter index of request to approve (0 to exit): ");
                choice = scn.hasNextInt() ? scn.nextInt() : -1;
                choice = choice <= requests.size() && choice > -1 ? choice : -1;
                scn.nextLine();

                if (choice != 0 && choice != -1){
                    resolveRequest(Util.docToRequest(requests.get(choice-1)));
                }else if (choice == -1){
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
        }
    }

    private static void resolveRequest(Request request){
        try{
            Bson query = eq("settings", "settings");
            int meterId = MongoDbUtils.ADMINCOLLECTION.find(query).limit(1).iterator().next().getInteger("nextMeterId");

            Bson update = Updates.set("nextMeterId", meterId+1);
            MongoDbUtils.ADMINCOLLECTION.updateOne(query, update);

            if(
                    Util.updateKeyValue(
                            MongoDbUtils.REQUESTCOLLECTION,
                            request.getId(),
                            "isApproved",
                            true
                    ) &&
                    Util.updateKeyValue(
                            MongoDbUtils.CUSTOMERCOLLECTION,
                            request.getCustomerId(),
                            "meterId",
                            String.valueOf(meterId)
                    ) &&
                    Util.updateKeyValue(
                            MongoDbUtils.CUSTOMERCOLLECTION,
                            request.getCustomerId(),
                            "isPaused",
                            false
                    )
            )
                System.out.println("Request approved successfully");
            else
                System.out.println("Error occurred. Please try again.");
        } catch (Exception ignore){
            System.out.println("Error occurred. Please try again.");
        }

    }

    private static void generateReadings() {
        Bson query = eq("settings", "settings");
        Document settings = MongoDbUtils.ADMINCOLLECTION.find(query).limit(1).iterator().tryNext();
        if(settings == null) {
            System.out.println("Internal error occurred, please try again later.");
            System.out.println("Press any key to continue.");
            return;
        }
        String currentMonth = Util.MONTH_MAPPING.get(settings.getInteger("month"));
        int currentYear = settings.getInteger("year");
        Bson updateMonth = Updates.set("month", settings.getInteger("month")%12 + 1);
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);

        if(settings.getInteger("month") == 12) {
            Bson updateYear = Updates.set("year", settings.getInteger("year") + 1);
            MongoDbUtils.ADMINCOLLECTION.updateOne(query, updateYear, updateOptions);
        }
        MongoDbUtils.ADMINCOLLECTION.updateOne(query, updateMonth, updateOptions);


        query = and(
                eq("isPaused", false),
                eq("isRemoved", false),
                not(eq("meterId",null))
        );

        List<Document> customers = new ArrayList<>();
        MongoDbUtils.CUSTOMERCOLLECTION.find(query).into(customers);
        ArrayList<Usage> usageList;
        for(Document customer : customers) {
            Customer c = Util.docToCustomer(customer);
            Usage u = new Usage();
            u.month = currentMonth;
            u.year = String.valueOf(currentYear);
            int thisMonthUsage = generateRandomUsage();
            u.readingUnits = c.getReadingUnits() + thisMonthUsage;
            c.setReadingUnits(u.readingUnits);
            u.billAmount = calculateBill(thisMonthUsage);
            u.paid = false;
            u.usedUnits = thisMonthUsage;
            usageList = (ArrayList<Usage>) c.getHistory();
            usageList.add(0, u);

            if(usageList.size() > 1)
                if (!usageList.get(1).paid) {
                    c.setPaused(true);
                    c.setDefaulter(true);
                }


            Document updatedCustomer = c.toDoc();
            query = eq("_id", customer.getObjectId("_id"));
            MongoDbUtils.CUSTOMERCOLLECTION.replaceOne(query, updatedCustomer);
        }
        System.out.println("Readings generated successfully");
        System.out.println("Press any key to continue.");
        scn.nextLine();
    }

    private static void viewCustomerData() {
        List<Document> allCustomers = new ArrayList<>();
        MongoDbUtils.CUSTOMERCOLLECTION.find().into(allCustomers);
        List<Document> activeCustomers = allCustomers.stream().filter(x -> {
            Customer c = Util.docToCustomer(x);
            return !(c.isPaused() || c.isRemoved());
        }).toList();
        List<Document> defaulters = allCustomers.stream().filter(x -> {
            Customer c = Util.docToCustomer(x);
            return (c.isDefaulter());
        }).toList();
        System.out.println();
        System.out.format("Total Connections: %d\n", allCustomers.size());
        System.out.format("Active Connections: %d\n", activeCustomers.size());
        System.out.format("Defaulters : %d\n", defaulters.size());
        System.out.println("------------------");

        System.out.println("Enter email of user you want to see data of: ");
        String email = Util.inputEmail();

        Document doc = allCustomers.stream().filter(x -> {
            Customer customer = Util.docToCustomer(x);
            return customer.getEmail().equals(email);
        }).findAny().orElse(null);

        if (doc == null){
            System.out.println("No customer found for this phone number !!");
            System.out.println("Press any key to continue.");
            scn.nextLine();
            return;
        }

        Customer customer = Util.docToCustomer(doc);

        System.out.println("------ Customer Information -------");
        System.out.format("Name: \t\t\t%s\n", customer.getName());
        System.out.format("Phone: \t\t\t%s\n", customer.getPhone());
        System.out.format("Alt. Phone: \t\t%s\n", customer.getAltPhone());
        System.out.format("Email: \t\t\t%s\n", customer.getEmail());
        System.out.format("Age: \t\t\t%d\n", customer.getAge());
        System.out.format("Address: \t\t%s\n", customer.getAddress());
        System.out.format("Meter ID: \t\t%s\n", customer.getMeterId());
        System.out.format("Reading units: \t\t%d\n", customer.getReadingUnits());
        System.out.format("Connection Paused: \t%b\n", customer.isPaused());
        System.out.format("Connection Removed: \t%b\n", customer.isRemoved());
        System.out.format("Defaulter: \t\t%b\n", customer.isDefaulter());
        System.out.println("-------------");
        System.out.println();
        System.out.println("Press any key to continue.");
        scn.nextLine();
    }

    private static void viewUsageDetails() {

        System.out.println();

        Customer customer = getUserFromEmail();
        ArrayList<Usage> usage = (ArrayList<Usage>) customer.getHistory();
        if (usage == null || usage.size() == 0){
            System.out.println("No history found for this Customer !!");
            System.out.println("Press any key to continue.");
            scn.nextLine();
            return;
        }
        for (Usage u : usage){
            System.out.println("----------------");
            System.out.format("Period: %s, %s\n", u.month, u.year);
            System.out.format("\tTotal usage: %d units\n", u.readingUnits);
            System.out.format("\tThis month Usage: %d units\n", u.usedUnits);
            System.out.format("\tBill Amount: %f\n", u.billAmount);
            System.out.format("\tPaid: %b\n", u.paid);
        }
        System.out.println("---------------");
        System.out.println("Press any key to continue.");
        scn.nextLine();
    }

    private static void viewComplaints() {

        System.out.println();

        Bson query = eq("status", "pending");
        ArrayList<Document> complaints = new ArrayList<>();
        MongoDbUtils.COMPLAINTCOLLECTION.find(query).into(complaints);


        System.out.println("--- COMPLAINTS ---");
        if (complaints.isEmpty()) {
            System.out.println("No complaints found. Everyone is Happy!!");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        } else {
            for(int i = 0; i < complaints.size(); ++i){
                System.out.println("---------------");
                Complaint complaint = Util.docToComplaint(complaints.get(i));
                System.out.format("%d. %s\n", i+1, complaint.getTitle());
                System.out.format("\tcomplaint: %s\n", complaint.getMessage());
                System.out.format("\tMeterId: %s\n", complaint.getMeterId());
            }
            System.out.println("---------------");
            System.out.println();

            int choice = -1;
            while(choice != 0){
                System.out.println("Enter index of complaint to resolve (0 to exit): ");
                choice = scn.hasNextInt() ? scn.nextInt() : -1;
                choice = choice <= complaints.size() && choice > -1 ? choice : -1;
                scn.nextLine();

                if (choice != 0 && choice != -1){
                    resolveComplaint(Util.docToComplaint(complaints.get(choice-1)));
                }else if (choice == -1){
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
        }
    }

    private static void resolveComplaint(@NotNull Complaint complaint){
        if(Util.updateKeyValue(MongoDbUtils.COMPLAINTCOLLECTION, complaint.getId(), "status", "resolved"))
            System.out.println("Complaint resolved successfully");
        else
            System.out.println("Error occurred. Please try again.");
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
        try {

            //Alot meter id to customer
            Bson query = eq("settings", "settings");
            int meterId = MongoDbUtils.ADMINCOLLECTION.find(query).limit(1).iterator().next().getInteger("nextMeterId");
            Bson update = Updates.set("nextMeterId", meterId+1);
            MongoDbUtils.ADMINCOLLECTION.updateOne(query, update);
            customer.setMeterId(String.valueOf(meterId));

            //Insert Customer Document in Customer Collection.
            customer.setPassword(String.valueOf(customer.getPassword().hashCode()));
            Document doc = customer.toDoc();
            MongoDbUtils.CUSTOMERCOLLECTION.insertOne(doc);

            //Create a request which is pre-approved for logging.
            Request req = new Request(doc.getObjectId("_id").toString(), true, Request.RequestType.NEW_CONNECTION, doc.getString("email"));
            Document reqDoc = req.toDoc();
            MongoDbUtils.REQUESTCOLLECTION.insertOne(reqDoc);

            System.out.println("User Account Created Successfully.");
            System.out.println("Press any key to continue.");
            scn.nextLine();

        } catch (Exception ignore) {
            System.out.println("Some error occurred. Please try again later.");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        }
    }

    private static void updateAccountDetails() {
        int choice = 0;
        System.out.println("--- UPDATE DETAILS PAGE ---");
        System.out.println("What do you want to update ?");
        while(choice != 7){
            System.out.println("1. Name");
            System.out.println("2. Phone Number");
            System.out.println("3. Alternate Number");
            System.out.println("4. Email");
            System.out.println("5. Age");
            System.out.println("6. Address");
            System.out.println("7. Go Back");
            System.out.println("-------------------");
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.println();

            Customer customer = null;
            if(choice != 7) {
                System.out.println("Enter email of user:");
                customer = getUserFromEmail();
            }

                switch (choice) {
                    case 1 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"name", Util.inputName());
                    case 2 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"phone", Util.inputPhone("New Phone: "));
                    case 3 -> {
                        String altPhone;
                        while (true) {
                            altPhone = Util.inputPhone("Alternate Phone: ");
                            if(altPhone.equals(customer.getAltPhone()))
                                System.out.println("!! Main phone and alternate phone number cannot be same !!");
                            else
                                break;
                        }
                        Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"altPhone", altPhone);
                    }
                    case 4 -> {
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
                        Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"email", customer.getEmail());
                    }
                    case 5 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"age", Util.inputAge());
                    case 6 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"address", Util.inputAddress());
                    case 7 -> {}
                    default -> System.out.println("!! Please enter a valid choice !!\n");
                }
            }
    }

    private static void pauseConnection() {

        Customer customer = getUserFromEmail();

        if(customer.getMeterId() == null){
            System.out.println("No connection found with this id.");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        } else if(customer.isRemoved()){
            System.out.println("Connection is terminated. Cannot pause.");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        } else if(customer.isPaused()) {
            System.out.println("Connection is already paused, resuming now...");
            Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"isPaused", false);
            System.out.println("Press any key to continue.");
            scn.nextLine();
        } else{
            Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"isPaused", true);
            System.out.println("Connection paused successfully");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        }
    }

    private static void terminateConnection() {

        Customer customer = getUserFromEmail();

        if (customer.getMeterId() == null){
            System.out.println("No connection found with this id.");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        } else if(customer.isRemoved()) {
            System.out.println("This connection is already terminated.");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        } else{
            Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"isRemoved", true);
            System.out.println("Connection terminated successfully");
            System.out.println("Press any key to continue.");
            scn.nextLine();
        }
    }

    private static Customer getUserFromEmail() {
        Customer customer;
        while(true) {
            String email = Util.inputEmail();

            //Check if email is used
            Bson query = eq("email", email);
            Document doc = MongoDbUtils.CUSTOMERCOLLECTION.find(query).iterator().tryNext();
            if(doc != null) {
                System.out.println("User Found.");
                customer = Util.docToCustomer(doc);
                break;
            } else
                System.out.println("User not found, please try again.");
        }
        return customer;
    }

    private static int generateRandomUsage(){
        Random rand = new Random();
        return rand.nextInt(1000);
    }

    private static Double calculateBill(int usage){
        double price;
        if(usage <= 200){
          price = usage * 4;
        } else if (usage <= 500) {
            price = 200*4 + (usage-200)*5;
        } else
            price = 200*4 + 300*5 + (usage-500)*6.5;
        return price;
    }
}
