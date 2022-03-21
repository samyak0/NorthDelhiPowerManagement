package FunctionClasses;

import Utils.MongoDbUtils;
import Utils.Util;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import databaseClasses.Complaint;
import databaseClasses.Customer;
import databaseClasses.Usage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class AdminFunctions {

    private static final Scanner scn = new Scanner(System.in);
    private String adminId;

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
        String id = Util.inputPhone("Please enter your admin ID: ");
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
        System.out.print("\033[H\033[2J"); // To clear the terminal
        Scanner scn = new Scanner(System.in);
        int choice = 0;

        while (choice != 11) {
            showMainMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.print("\033[H\033[2J"); // To clear the terminal

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

    }

    private static void generateReadings() {
        Bson query = eq("settings", "settings");
        Document settings = MongoDbUtils.ADMINCOLLECTION.find(query).limit(1).iterator().tryNext();
        if(settings == null) {
            System.out.println("Internal error occurred, please try again later.");
            return;
        }
        String currentMonth = Util.MONTH_MAPPING.get(settings.getInteger("month"));
        String currentYear = Util.MONTH_MAPPING.get(settings.getInteger("year"));
        Bson updateMonth = Updates.set("month", settings.getInteger("month")%12 + 1);
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);

        if(settings.getInteger("month") == 12) {
            Bson updateYear = Updates.set("year", settings.getInteger("year") + 1);
            MongoDbUtils.ADMINCOLLECTION.updateOne(query, updateYear, updateOptions);
        }
        MongoDbUtils.ADMINCOLLECTION.updateOne(query, updateMonth, updateOptions);


        query = and(
                eq("isPaused", false),
                eq("isRemoved", false)
        );

        List<Document> customers = new ArrayList<>();
        MongoDbUtils.CUSTOMERCOLLECTION.find(query).into(customers);
        ArrayList<Usage> usageList;
        for(Document customer : customers) {
            Customer c = Util.docToCustomer(customer);
            Usage u = new Usage();
            u.month = currentMonth;
            u.year = currentYear;
            int thisMonthUsage = generateRandomUsage();
            u.readingUnits = c.getReadingUnits() + thisMonthUsage;
            c.setReadingUnits(u.readingUnits);
            u.billAmount = calculateBill(thisMonthUsage);
            u.paid = false;
            u.usedUnits = thisMonthUsage;
            usageList = (ArrayList<Usage>) c.getHistory();
            usageList.add(0, u);

            for (int i = usageList.size() - 2; i >= 0; --i) {
                if (!usageList.get(i).paid) {
                    c.setPaused(true);
                    c.setDefaulter(true);
                }
            }

            Document updatedCustomer = c.toDoc();
            query = eq("_id", customer.getObjectId("_id"));
            MongoDbUtils.CUSTOMERCOLLECTION.replaceOne(query, updatedCustomer);
        }
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
        System.out.print("\033[H\033[2J"); // To clear the terminal
        System.out.format("Total Connections: %d\n", allCustomers.size());
        System.out.format("Active Connections: %d\n", activeCustomers.size());
        System.out.format("Defaulters : %d\n", defaulters.size());
        System.out.println("------------------");

        String phone = Util.inputPhone("Enter phone number of customer you want details of: ");

        Document doc = allCustomers.stream().filter(x -> {
            Customer customer = Util.docToCustomer(x);
            return customer.getPhone().equals(phone);
        }).findAny().orElse(null);

        if (doc == null){
            System.out.println("No customer found for this phone number !!");
            return;
        }

        Customer customer = Util.docToCustomer(doc);

        System.out.println("-------------");
        System.out.format("Name: %s\n", customer.getName());
        System.out.format("Phone: %s\n", customer.getPhone());
        System.out.format("Alt. Phone: %s\n", customer.getAltPhone());
        System.out.format("Email: %s\n", customer.getEmail());
        System.out.format("Age: %d\n", customer.getAge());
        System.out.format("Address: %s\n", customer.getAddress());
        System.out.format("Meter ID: %s\n", customer.getMeterId());
        System.out.format("Reading units: %d\n", customer.getReadingUnits());
        System.out.format("Connection Paused: %b\n", customer.isPaused());
        System.out.format("Connection Removed: %b\n", customer.isRemoved());
        System.out.format("Defaulter: %b\n", customer.isDefaulter());
        System.out.println("-------------");
    }

    private static void viewUsageDetails() {

        System.out.print("\033[H\033[2J"); // To clear the terminal
        String phone = Util.inputPhone("Enter phone number of customer you want details of: ");
        Bson query = eq("phone", phone);
        Document doc = MongoDbUtils.CUSTOMERCOLLECTION.find(query).limit(1).iterator().tryNext();
        if (doc == null){
            System.out.println("No customer found for this phone number !!");
            return;
        }

        Customer customer = Util.docToCustomer(doc);
        ArrayList<Usage> usage = (ArrayList<Usage>) customer.getHistory();
        if (usage == null || usage.size() == 0){
            System.out.println("No history found for this Customer !!");
            return;
        }
        for (Usage u : usage){
            System.out.println("----------------");
            System.out.format("Period: %s, %s", u.month, u.year);
            System.out.format("\tTotal usage: %d units", u.readingUnits);
            System.out.format("\tThis month Usage: %d units", u.usedUnits);
            System.out.format("\tBill Amount: %d", u.billAmount);
            System.out.format("\tPaid: %b", u.paid);
        }
        System.out.println("---------------");
    }

    private static void viewComplaints() {

        System.out.print("\033[H\033[2J"); // To clear the terminal

        Bson query = not(eq("status", "resolved"));
        ArrayList<Document> complaints = new ArrayList<>();
        MongoDbUtils.COMPLAINTCOLLECTION.find(query).into(complaints);


        System.out.println("--- COMPLAINTS ---");
        if (complaints.isEmpty())
            System.out.println("No complaints found. Everyone is Happy!!");
        else{
            for(int i = 0; i < complaints.size(); ++i){
                System.out.println("---------------");
                Complaint complaint = Util.docToComplaint(complaints.get(i));
                System.out.format("%d. %s\n", i+1, complaint.getTitle());
                System.out.format("\tcomplaint: %s", complaint.getMessage());
                System.out.format("\tMeterId: %s", complaint.getMeterId());
            }
            System.out.println("---------------");
            System.out.println();

            int choice = -1;
            while(choice != 0){
                System.out.println("Enter index of complaint to resolve (0 to exit): ");
                choice = scn.hasNextInt() ? scn.nextInt() : -1;
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
        if(Util.updateKeyValue(MongoDbUtils.COMPLAINTCOLLECTION, complaint.getCustomerId(), "status", "resolved"))
            System.out.println("Complaint resolved successfully");
        else
            System.out.println("Error occurred. Please try again.");
    }

    private static void register(){

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

            System.out.println("User Account Created Successfully.");

        } catch (Exception ignore) {
            System.out.println("Some error occurred. Please try again later.");
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
            System.out.print("\033[H\033[2J"); // To clear the terminal

            Customer customer = getUserFromPhone();

            switch (choice) {
                case 1 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"name", Util.inputName());
                case 2 -> {
                    String newPhone = Util.inputPhone("New Phone: ");

                    Bson query = and(
                            eq("phone", newPhone),
                            not(eq("_id", new ObjectId(customer.getId())))
                    );
                    if(MongoDbUtils.CUSTOMERCOLLECTION.find(query).iterator().hasNext())
                        System.out.println("Phone number already in use, please use another number.");
                    else {
                        Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"phone", newPhone);
                    }
                }
                case 3 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"name", Util.inputPhone("Alternate Phone: "));
                case 4 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"email", Util.inputEmail());
                case 5 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"age", Util.inputAge());
                case 6 -> Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"address", Util.inputAddress());
                case 7 -> {}
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }
        }
    }

    private static void pauseConnection() {

        Customer customer = getUserFromPhone();

        if(customer.getMeterId() == null)
            System.out.println("No connection found with your id.");
        else if(customer.isPaused())
            System.out.println("Connection is already paused");
        else if(customer.isRemoved())
            System.out.println("Connection is terminated. Cannot pause.");
        else{
            Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"isPaused", true);
            System.out.println("Connection paused successfully");
        }
    }

    private static void terminateConnection() {

        Customer customer = getUserFromPhone();

        if (customer.getMeterId() == null)
            System.out.println("No connection found with this id.");
        else if(customer.isRemoved())
            System.out.println("This connection is already terminated.");
        else{
            Util.updateInDatabase(MongoDbUtils.CUSTOMERCOLLECTION, customer.getId(),"isRemoved", true);
            System.out.println("Connection terminated successfully");
        }
    }

    private static Customer getUserFromPhone() {
        Customer customer;
        while(true) {
            String phone = Util.inputPhone("Phone: ");

            //Check if phone number is used
            Bson query = eq("phone", phone);
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
        return rand.nextInt(500);
    }

    private static int calculateBill(int usage){
        return 42;
    }
}
