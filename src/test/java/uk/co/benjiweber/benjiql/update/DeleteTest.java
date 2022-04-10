package uk.co.benjiweber.benjiql.update;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.benjiweber.benjiql.example.Conspiracy;
import uk.co.benjiweber.benjiql.example.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.benjiweber.benjiql.ddl.Create.relationship;
import static uk.co.benjiweber.benjiql.update.Delete.delete;

@RunWith(MockitoJUnitRunner.class)
public class DeleteTest {

    @Mock Connection mockConnection;
    @Mock PreparedStatement mockStatement;

    @Test public void should_match_example_with_no_restrictions() {
        String sql = delete(Person.class).toSql();
        assertEquals("DELETE FROM person", sql.trim());
    }

    @Test public void should_match_example_with_restrictions() {
        String sql = delete(Person.class)
            .where(Person::getLastName)
            .equalTo("weber")
            .and(Person::getFavouriteNumber)
            .equalTo(6)
            .and(Person::getFirstName)
            .like("%w%")
            .and(Person::getFirstName)
            .notEqualTo("bob")
            .toSql();

        assertEquals("DELETE FROM person WHERE last_name = ? AND favourite_number = ? AND first_name LIKE ? AND first_name != ?", sql.trim());
    }

    @Test public void join_example() {
        String sql = delete(relationship(Conspiracy.class, Person.class))
                .whereLeft(Conspiracy::getName)
                .equalTo("nsa")
                .andRight(Person::getLastName)
                .equalTo("smith")
                .toSql();

        assertEquals("DELETE FROM conspiracy_person WHERE conspiracy_name = ? AND person_last_name = ?", sql.trim());
    }

    @Test public void should_set_values() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);

        delete(Person.class)
                .where(Person::getLastName)
                .equalTo("weber")
                .and(Person::getFavouriteNumber)
                .equalTo(6)
                .execute(() -> mockConnection);

        verify(mockStatement).setString(1,"weber");
        verify(mockStatement).setInt(2,6);
    }
}
