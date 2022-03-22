package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDbUtils {

    public static MongoCollection<Document> CUSTOMERCOLLECTION;
    public static MongoCollection<Document> REQUESTCOLLECTION;
    public static MongoCollection<Document> COMPLAINTCOLLECTION;
    public static MongoCollection<Document> ADMINCOLLECTION;

    static {
        try {
            String uri = "mongodb+srv://samyakJain:samyak329@radhikamajorprojectclus.jddsq.mongodb.net/NorthDelhiPowerManagement?retryWrites=true&w=majority";
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("NorthDelhiPowerManagement");
            CUSTOMERCOLLECTION = database.getCollection("customer");
            REQUESTCOLLECTION = database.getCollection("request");
            COMPLAINTCOLLECTION = database.getCollection("complaint");
            ADMINCOLLECTION = database.getCollection("admin");
        } catch (Exception ignored) {
        }
    }
}
