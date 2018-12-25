package com.example.susmitha.gym_pilot;

public class Users {

    String userName;
    String email;
    String mobileNumber;

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Users(String userName, String email, String mobileNumber){
        this.userName=userName;
        this.email=email;

        this.mobileNumber=mobileNumber;
    }


    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
