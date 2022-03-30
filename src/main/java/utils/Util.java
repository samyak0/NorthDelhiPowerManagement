package utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import databaseClasses.Complaint;
import databaseClasses.Customer;
import databaseClasses.Request;
import databaseClasses.Usage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import javax.naming.SizeLimitExceededException;
import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Map.entry;

public class Util {

    private static final Scanner scn = new Scanner(System.in);
    public static final Map<Integer, String> MONTH_MAPPING = Map.ofEntries(
            entry(1, "JAN"),
            entry(2, "FEB"),
            entry(3, "MAR"),
            entry(4, "APR"),
            entry(5, "MAY"),
            entry(6, "JUN"),
            entry(7, "JUL"),
            entry(8, "AUG"),
            entry(9, "SEP"),
            entry(10, "OCT"),
            entry(11, "NOV"),
            entry(12, "DEC")
    );

    public static Customer docToCustomer(final @NotNull Document doc) {
        return new Customer(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("password"),
                doc.getString("phone"),
                doc.getString("altPhone"),
                doc.getString("address"),
                doc.getString("email"),
                doc.getString("securityQuestion"),
                doc.getString("securityAnswer"),
                doc.getInteger("age"),
                doc.getString("meterId"),
                doc.getInteger("readingUnits"),
                doc.getBoolean("isPaused"),
                doc.getBoolean("isRemoved"),
                doc.getBoolean("isDefaulter"),
                doc.getList("history", Document.class),
                doc.get("identityDocument", Binary.class)
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
                Request.RequestType.valueOf(doc.getString("requestType")),
                doc.getString("email"),
                doc.getList("paidMonths", String.class)
        );
    }

    public static Document usageToDoc(Usage u){
        return new Document()
                .append("month", u.month)
                .append("year", u.year)
                .append("readingUnits", u.readingUnits)
                .append("usedUnits", u.usedUnits)
                .append("billAmount", u.billAmount)
                .append("paid", u.paid);
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
        String input;
        while (true) {
            System.out.print("Full Name: ");
            input = scn.nextLine();
            if (input.isEmpty()) System.out.println("Value cannot be null.");
            else break;
        }
        System.out.println();
        return input;
    }

    public static String inputEmail() {
        String regex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern p = Pattern.compile(regex);
        Matcher m;
        String input;
        while (true) {
            System.out.print("Email: ");
            input = scn.nextLine();
            m = p.matcher(input);
            if (!m.matches()) System.out.println("Please enter a valid email");
            else break;
        }
        System.out.println();
        return input;
    }

    public static int inputAge() {
        int age = 0;
        while (age < 18) {
            System.out.print("Age: ");
            age = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.println();
            if (age < 18) {
                System.out.println("!! Please enter a valid age above 18 !!");
            }
        }
        System.out.println();
        return age;
    }

    public static String inputPhone(String inputString) {
        String phone = "";
        while (phone.length() != 10) {
            System.out.print(inputString);
            phone = scn.nextLine();
            System.out.println();
            if (phone.length() != 10) {
                System.out.println("!! Please enter a valid phone number !!");
            }else{
                try {
                    Long.parseLong(phone);
                } catch (Exception ignore) {
                    System.out.println("!! Please enter a valid phone number !!");
                }
            }
        }
        return phone;
    }

    public static String inputID(String input) {
        String id;
        while (true) {
            System.out.print(input);
            id = scn.nextLine();
            System.out.println();
            try {
                Long.parseLong(id);
                break;
            } catch (Exception ignore) {
                System.out.println("!! Please enter a valid numeric ID !!");
            }
        }
        return id;
    }

    public static String inputAddress() {
        String input;
        while (true) {
            System.out.print("Address: ");
            input = scn.nextLine();
            if (input.isEmpty()) System.out.println("Value cannot be null.");
            else break;
        }
        System.out.println();
        return input;
    }

    public static String inputPassword(String inputString) {
        String input;
        Console console = System.console();
        while (true) {
            input = String.valueOf(console.readPassword(inputString));
            if (input.isEmpty()) System.out.println("Value cannot be null.");
            else break;
        }
        System.out.println();
        return input;
    }

    public static String validatePassword(String password) {
        if (password==null) return "Password must not be empty";

//        String regex = "^(?=.*[0-9])$";.*[0-9].*
        String regex = ".*[0-9].*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if(!m.matches()) return "The password must have a digit";

        regex = ".*[a-z].*";
        p = Pattern.compile(regex);
        m = p.matcher(password);
        if(!m.matches()) return "The password must have a lower case letter";

        regex = ".*[A-Z].*";
        p = Pattern.compile(regex);
        m = p.matcher(password);
        if(!m.matches()) return "The password must have a Upper Case letter";

        regex = ".*[!@#$%^*&-+=()].*";
        p = Pattern.compile(regex);
        m = p.matcher(password);
        if(!m.matches()) return "The password must have a special character";

        regex = ".*[\\s+].*";
        p = Pattern.compile(regex);
        m = p.matcher(password);
        if(m.matches()) return "The password must not have spaces";

        regex = "^*.{8,20}$";
        p = Pattern.compile(regex);
        m = p.matcher(password);
        if(!m.matches()) return "The password must be between 8-20 characters";

        return null;
    }

    public static Usage docToUsage(Document doc) {
        return new Usage(
                doc.getString("month"),
                doc.getString("year"),
                doc.getInteger("readingUnits"),
                doc.getInteger("usedUnits"),
                doc.getDouble("billAmount"),
                doc.getString("paid")
        );
    }

    public static String inputSecurityQuestion() {
        String input;
        while (true) {
            System.out.print("Enter a security Question: ");
            input = scn.nextLine();
            if (input.isEmpty()) System.out.println("Value cannot be null.");
            else break;
        }
        System.out.println();
        return input;
    }

    public static String inputSecurityAnswer() {
        String input;
        while (true) {
            System.out.print("Enter answer: ");
            input = scn.nextLine();
            if (input.isEmpty()) System.out.println("Value cannot be null.");
            else break;
        }
        System.out.println();
        return input;
    }

    public static Binary  inputIdentityDoc() {

        Binary binaryDoc;
        while (true) {
            System.out.println();
            System.out.println("Please enter path to your identity Document.(0 to exit)");
            System.out.println("File Type: PDF\nMax Size: 10 MB");
            System.out.println("Path: ");
            String path = scn.nextLine();
            if (path.equals(String.valueOf(0))){
                return null;
            }
            try{
                if (!path.endsWith(".pdf")) throw new IllegalArgumentException();
                Path file = Paths.get(path);
                byte [] fileBytes = Files.readAllBytes(file);
                if(fileBytes.length > 10485760) throw new SizeLimitExceededException();
                binaryDoc = new Binary(fileBytes);
                break;
            } catch (InvalidPathException e){
                System.out.println("No file found at the path. Please try again...");
            } catch (IOException | OutOfMemoryError | SecurityException e) {
                System.out.println("Unable to read data from the file. Please try again...");
            } catch (SizeLimitExceededException e) {
                System.out.println("File size cannot be greater than 10 MB. Please try again...");
            } catch (IllegalArgumentException e){
                System.out.println("Only PDFs are allowed. Please try again...");
            }
        }
        return binaryDoc;
    }
}
