package com.vis.beans;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huzefaasger on 07-09-2015.
 */
public class FbProfile {

    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    private String email;
    private Location location;
    private String profileImagePath;
    @SerializedName("id")
    private String fbUserId;
    @SerializedName("birthday")
    private String dateOfBirth;
    private String gender;
    private String fbProfileLink;
    private String mobRegId;
    private String mobileNumber;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getFbUserId() {
        return fbUserId;
    }

    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFbProfileLink() {
        return fbProfileLink;
    }

    public void setFbProfileLink(String fbProfileLink) {
        this.fbProfileLink = fbProfileLink;
    }

    public String getMobRegId() {
        return mobRegId;
    }

    public void setMobRegId(String mobRegId) {
        this.mobRegId = mobRegId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
