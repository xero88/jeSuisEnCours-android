package hack.opendata.jesuisencours.Model;

/**
 * Created by bernara3 on 19.05.17.
 */

public class Course {

    public String name;
    public String status;

    public Course(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
