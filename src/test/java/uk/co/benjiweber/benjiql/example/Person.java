package uk.co.benjiweber.benjiql.example;

public class Person {
    private String firstName;
    private String lastName;
    private Integer favouriteNumber;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Person() {}

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

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    public Integer getFavouriteNumber() {
        return favouriteNumber;
    }

    public void setFavouriteNumber(Integer favouriteNumber) {
        this.favouriteNumber = favouriteNumber;
    }
}
