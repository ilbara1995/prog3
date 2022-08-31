package Common;

import java.util.Date;

public class Log {
    private String message;
    private Date time;


    public Log(String message) {
        this.message = message;
        this.time = new Date();
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
