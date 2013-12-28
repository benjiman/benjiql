package uk.co.benjiweber.benjiql.ddl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static uk.co.benjiweber.benjiql.ddl.Create.create;

public class CreateTest {
    static class Address {
        public String getFirstLine() { return null; }
        public Integer getHouseNumber() { return null; }
    }

    @Test
    public void check_create_matches_example() throws Exception {
        String sql = create(Address.class)
            .field(Address::getFirstLine)
            .field(Address::getHouseNumber)
            .toSql();

        assertEquals("CREATE TABLE IF NOT EXISTS address ( first_line text, house_number integer );", sql.trim());

    }
}
