package com.example.trialrun.tag;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class Tag implements Serializable {

    private final String epcMem;
    private final Timestamp timestamp;

    // Tag class that contains all the information of a Tag (can be extended to hold more data)
    public Tag(String epcMem) {
        this.epcMem = epcMem;
        this.timestamp = new Timestamp(new Date().getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tag tag = (Tag) o;
        return epcMem.equals(tag.epcMem);
    }

    public String getEpcMem() {
        return epcMem;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
