package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.logging.Level;

public class MongoDbUtils {

    public static MongoCollection<Document> CUSTOMERCOLLECTION;
    public static MongoCollection<Document> REQUESTCOLLECTION;
    public static MongoCollection<Document> COMPLAINTCOLLECTION;
    public static MongoCollection<Document> ADMINCOLLECTION;
    public static boolean isConnected;

    static {
        try {
            java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
//            String uri = "mongodb+srv://samyakJain:samyak329@radhikamajorprojectclus.jddsq.mongodb.net/NorthDelhiPowerManagement?retryWrites=true&w=majority";
            String uri = "mongodb+srv://radhika:radhika@cluster0.nmlk2.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("NorthDelhiPowerManagement");
            CUSTOMERCOLLECTION = database.getCollection("customer");
            REQUESTCOLLECTION = database.getCollection("request");
            COMPLAINTCOLLECTION = database.getCollection("complaint");
            ADMINCOLLECTION = database.getCollection("admin");
            System.out.println("Connection to Database successful");
            System.out.println("Starting Application...");
            isConnected = true;
            Thread.sleep(2000);
        } catch (Exception ignored) {
            System.out.println("Could not connect to Database");
            System.out.println("Exiting...");
            isConnected = false;
        }
    }

    public static boolean initialize(){
        return isConnected;
    }
}
