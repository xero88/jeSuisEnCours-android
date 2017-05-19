package hack.opendata.jesuisencours;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Created by bernara3 on 19.05.17.
 */

public class JSECApp extends Application {

    public static String TAG = "JSECApp";

    public static String currentUserId = "9001"; // TODO HARDCODED

    /**
     * Instance of JSECApp
     */
    private static JSECApp instance;
    public static JSECApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // firebase
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();

    }

    /**
     * Firebase Instance (db)
     */
    private FirebaseDatabase database;
    public FirebaseDatabase getDatabase() {
        return database;
    }

}
