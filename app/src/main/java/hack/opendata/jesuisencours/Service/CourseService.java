package hack.opendata.jesuisencours.Service;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import hack.opendata.jesuisencours.JSECApp;
import hack.opendata.jesuisencours.Model.Course;

/**
 * Created by bernara3 on 19.05.17.
 */

public class CourseService {

    private static final String TAG = "CourseService";

    // refs
    private DatabaseReference mStudentsRef;

    public CourseService(){
        mStudentsRef = JSECApp.getInstance().getDatabase().getReference("students");
    }

    public void getCurrentCourse(final CurrentCourseCallback callback){

        // current dat
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyy-HH00");
        final String currentDate = df.format(c.getTime());

        Log.e(TAG, "getCurrentCourse: " + currentDate + " for this date" );

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                
                //// TODO: 19.05.17  
                /*Course course = dataSnapshot.getValue(Course.class);

                Log.e(TAG, "onDataChange: " + course.getName());
                Log.e(TAG, "onDataChange: " + course.getStatus());*/

                //callback.getCurrentCourse(course, currentDate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mStudentsRef
                .child(JSECApp.currentUserId)
                .child("courses").child(currentDate)
                .addValueEventListener(postListener);
    }

    public void setPresentCourse(Course course, String currentDateString, final ValidatedCourseCallback callback){

        // gift to bought gift
        course.setStatus("present");

        // add gift to archive of member
        mStudentsRef
                .child(JSECApp.currentUserId)
                .child("courses")
                .child(currentDateString).setValue(course, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                callback.courseValidated();
            }
        });

    }

    public interface CurrentCourseCallback {
        void getCurrentCourse(Course course, String date);
    }


    public interface ValidatedCourseCallback {
        void courseValidated();
    }
}
