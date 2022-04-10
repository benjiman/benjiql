package uk.co.benjiweber.benjiql.example;

import java.util.HashSet;
import java.util.Set;

public class Conspiracy {
    private Set<Person> members = new HashSet<>();
    private String name;

    public Conspiracy() {}

    public Conspiracy(String name) {
        this.name = name;
    }

    public Set<Person> getMembers() {
        return members;
    }

    public void setMembers(Set<Person> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
