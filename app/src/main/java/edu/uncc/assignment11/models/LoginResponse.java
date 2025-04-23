package edu.uncc.assignment11.models;

public class LoginResponse {
    private String status;
    private String token;
    private int user_id;
    private String user_email;
    private String user_fname;
    private String user_lname;
    private String user_role;

    private String message;

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getUser_email() {
        return user_email;
    }

    public String getUser_fname() {
        return user_fname;
    }

    public String getUser_lname() {
        return user_lname;
    }

    public String getUser_role() {
        return user_role;
    }

}
