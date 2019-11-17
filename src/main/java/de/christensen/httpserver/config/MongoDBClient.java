package de.christensen.httpserver.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.christensen.httpserver.exceptions.HttpHandlerNotInitializedException;
import org.bson.Document;

import java.util.Properties;

/*
 * Used to establish a connection to a MongoDB server.
 * Provides access to the following collections at the moment:
 *  - comments
 */
public class MongoDBClient {

    private static final String COLLECTIONS_COMMENTS = "comments";

    private static MongoDBClient instance;
    private final String host;
    private final String port;
    private final String database;
    private MongoDatabase mongoDatabase;

    public static void initialize(Properties properties) {
        instance = new MongoDBClient(properties);
    }

    private MongoDBClient(Properties properties) {
        this.host = properties.getProperty("mongodb.host");
        this.port = properties.getProperty("mongodb.port");
        this.database = properties.getProperty("mongodb.database");
        establishConnection();
    }

    private void establishConnection() {
        final String connectionString = String.format("mongodb://%s:%s", host, port);
        MongoClient mongoClient = MongoClients.create(connectionString);
        mongoDatabase = mongoClient.getDatabase(this.database);
    }

    public static MongoDBClient getInstance() {
        if (instance == null) {
            throw new HttpHandlerNotInitializedException();
        }
        return instance;
    }

    public MongoCollection<Document> getCommentsCollection(){
        return mongoDatabase.getCollection(COLLECTIONS_COMMENTS);
    }
}
