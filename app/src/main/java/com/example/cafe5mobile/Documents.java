package com.example.cafe5mobile;

import java.util.ArrayList;
import java.util.List;

public class Documents {

    public class Document {
        public String uuid;
        public String date;
        public String inputstorename;
        public String outputstorename;
        public String typename;
        public int typeid;
        public String docnumber;
        public String windowid;
    }

    public List<Document> documents = new ArrayList<>();

    public Documents() {

    }

}
