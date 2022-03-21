import FunctionClasses.AdminFunctions;
import FunctionClasses.CustomerFunctions;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J"); // To clear the terminal
        Scanner scn = new Scanner(System.in);
        int choice = 0;
        CustomerFunctions customerFunctions = new CustomerFunctions();
        AdminFunctions adminFunctions = new AdminFunctions();

        while (choice != 3) {
            showMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.print("\033[H\033[2J"); // To clear the terminal
            switch (choice) {
                case 1 -> customerFunctions.startAuthenticationFlow();
                case 2 -> adminFunctions.startAuthenticationFlow();
                case 3 -> {}
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }

        }
    }

    public static void showMenu(){
        System.out.println("NORTH DELHI POWER CONSOLE");
        System.out.println("------ Main Menu ------");
        System.out.println(" 1. Customer Login");
        System.out.println(" 2. Admin Login");
        System.out.println(" 3. Exit");
        System.out.println("-----------------------");
    }
//        try {
//            Customer c = new Customer("samyak", "111", "222", "gh-4", "@.com", 32);
//            Document dd = c.toDoc();
//            MongoDbUtils.CUSTOMERCOLLECTION.insertOne(dd);
//            System.out.println(dd.getObjectId("_id").toString());
//            Document d = MongoDbUtils.CUSTOMERCOLLECTION.find(eq("name", "samyak")).iterator().tryNext();
//            Customer newC = Util.docToCustomer(d);
//            System.out.println(newC.getName() + newC.getId());
//            newC.setName("samyakNew");
//            dd = newC.toDoc();
//            MongoDbUtils.CUSTOMERCOLLECTION.insertOne(dd);
//            MongoDbUtils.CUSTOMERCOLLECTION.updateOne(eq("_id", d.getObjectId("_id")), Updates.set("", ""), new UpdateOptions().upsert(true));
////            Document doc = new Document()
////                    .append("some_key", "value")
////                .append("array", Arrays.asList(
////                        new Document().append("key1", "value1"),
////                        new Document().append("key2", "value2")
////                ));
////            MongoDbCollections.CUSTOMERCOLLECTION.insertOne(doc);
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//    }
}
