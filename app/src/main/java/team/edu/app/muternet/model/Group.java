package team.edu.app.muternet.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Group {
    public String address;
    public int port;
    public Group(){

    }
    public Group(String address, int port){
        this.address = address;
        this.port = port;
    }
}
