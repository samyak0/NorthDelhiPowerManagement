package Utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import databaseClasses.Complaint;
import databaseClasses.Customer;
import databaseClasses.Request;
import databaseClasses.Usage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class Util {

    private static final Scanner scn = new Scanner(System.in);

    public static Customer docToCustomer(final @NotNull Document doc) {
        return new Customer(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("password"),
                doc.getString("phone"),
                doc.getString("altPhone"),
                doc.getString("address"),
                doc.getString("email"),
                doc.getInteger("age"),
                doc.getString("meterId"),
                doc.getInteger("readingUnits"),
                doc.getBoolean("isPaused"),
                doc.getBoolean("isRemoved"),
                doc.getBoolean("isDefaulter"),
                doc.getList("history", Usage.class)
        );
    }

    public static Complaint docToComplaint(final @NotNull Document doc) {
        return new Complaint(
                doc.getObjectId("_id").toString(),
                doc.getString("customerId"),
                doc.getString("meterId"),
                doc.getString("title"),
                doc.getString("message"),
                doc.getString("status")
        );
    }

    public static Request docToRequest(final @NotNull Document doc) {
        return new Request(
                doc.getObjectId("_id").toString(),
                doc.getString("customerId"),
                doc.getBoolean("isApproved"),
                (Request.RequestType) doc.get("requestType"),
                (Customer) doc.get("updatedCustomer")
        );
    }

    public static <T> void updateInDatabase(MongoCollection<Document> collection, String id, String key, T value) {

        if (Util.updateKeyValue(collection, id, key, value))
            System.out.println("Updated Successfully !!");
        else
            System.out.println("Error Occurred. Please try again.");
    }

    public static <T> boolean updateKeyValue(MongoCollection<Document> collection, String id, String key, T value) {
        Bson query = eq("_id", new ObjectId(id));
        Bson update = Updates.set(key, value);
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);

        try {
            collection.updateOne(query, update, updateOptions);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String inputName() {
        System.out.print("Full Name: ");
        String input = scn.nextLine();
        System.out.print("\033[H\033[2J"); // To clear the terminal
        return input;
    }

    public static String inputEmail() {
        System.out.print("Email: ");
        String input = scn.nextLine();
        System.out.print("\033[H\033[2J"); // To clear the terminal
        return input;
    }

    public static int inputAge() {
        int age = 0;
        while (age < 18) {
            System.out.print("Age: ");
            age = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.print("\033[H\033[2J"); // To clear the terminal
            if (age < 18) {
                System.out.println("!! Please enter a valid age above 18 !!");
            }
        }
        System.out.print("\033[H\033[2J"); // To clear the terminal
        return age;
    }

    public static String inputPhone(String inputString) {
        String phone = "";
        while (phone.length() != 10) {
            System.out.print(inputString);
            phone = scn.nextLine();
            System.out.print("\033[H\033[2J"); // To clear the terminal
            try {
                Long.parseLong(phone);
            } catch (Exception ignore) {
                System.out.println("!! Please enter a valid phone number !!");
            } finally {
                if (phone.length() != 10)
                    System.out.println("!! Please enter a valid phone number !!");
            }
        }
        return phone;
    }

    public static String inputAddress() {
        System.out.print("Address: ");
        String input = scn.nextLine();
        System.out.print("\033[H\033[2J"); // To clear the terminal
        return input;
    }

    public static String inputPassword(String inputString) {
        System.out.print(inputString);
        String input = scn.nextLine();
        System.out.print("\033[H\033[2J"); // To clear the terminal
        return input;
    }
}
