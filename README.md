benjiql
=======

Proof of concept for database interaction using new features in Java 8

It aims to be as typesafe as possible and do no compile-time code generation. 

It's best explained by example. 

```java

    @Test public void example_of_create_table_persist_retrieve_and_update_with_real_database() throws SQLException {
        create(Person.class)
            .field(Person::getFirstName)
            .field(Person::getLastName)
            .field(Person::getFavouriteNumber)
            .execute(this::openConnection);

        delete(Person.class)
            .where(Person::getLastName)
            .equalTo("weber")
            .execute(this::openConnection);

        Person benji = new Person("benji","weber");
        benji.setFavouriteNumber(9);

        insert(benji)
            .value(Person::getFirstName)
            .value(Person::getLastName)
            .value(Person::getFavouriteNumber)
            .execute(this::openConnection);

        benji.setFirstName("benji-updated");

        update(benji)
            .value(Person::getFirstName)
            .where(Person::getLastName)
            .equalTo("weber")
            .execute(this::openConnection);

        Mapper<Person> personMapper = Mapper.mapper(Person::new)
            .set(Person::setFirstName)
            .set(Person::setLastName)
            .set(Person::setFavouriteNumber);

        Optional<Person> result = from(Person.class)
            .where(Person::getFirstName)
            .like("%updated")
            .and(Person::getLastName)
            .equalTo("weber")
            .select(personMapper, this::openConnection);

        assertEquals("benji-updated", result.get().getFirstName());
        assertEquals("weber", result.get().getLastName());
        assertEquals((Integer)9, result.get().getFavouriteNumber());
    }

    @Test public void example_of_select_with_join() throws SQLException {
        create(Person.class)
            .field(Person::getFirstName)
            .field(Person::getLastName)
            .field(Person::getFavouriteNumber)
            .execute(this::openConnection);

        create(Conspiracy.class)
            .field(Conspiracy::getName)
            .execute(this::openConnection);

        create(relationship(Conspiracy.class, Person.class))
            .fieldLeft(Conspiracy::getName)
            .fieldRight(Person::getFirstName)
            .fieldRight(Person::getLastName)
            .execute(this::openConnection);

        delete(Person.class)
            .execute(this::openConnection);

        delete(Conspiracy.class)
            .execute(this::openConnection);

        delete(relationship(Conspiracy.class, Person.class))
            .execute(this::openConnection);

        Person smith = new Person("agent","smith");
        smith.setFavouriteNumber(6);

        insert(smith)
            .value(Person::getFirstName)
            .value(Person::getLastName)
            .value(Person::getFavouriteNumber)
            .execute(this::openConnection);

        Conspiracy nsa = new Conspiracy("nsa");
        nsa.getMembers().add(smith);

        insert(nsa)
            .value(Conspiracy::getName)
            .execute(this::openConnection);

        nsa.getMembers().forEach(agent -> {
            insert(nsa, agent)
                    .valueLeft(Conspiracy::getName)
                    .valueRight(Person::getLastName)
                    .valueRight(Person::getFirstName)
                    .execute(this::openConnection);
        });

        Mapper<Person> personMapper = Mapper.mapper(Person::new)
            .set(Person::setFirstName)
            .set(Person::setLastName)
            .set(Person::setFavouriteNumber);

        Optional<Person> person = from(Person.class)
            .where(Person::getLastName)
            .equalTo("smith")
            .join(relationship(Conspiracy.class, Person.class).invert())
            .using(Person::getFirstName, Person::getLastName)
            .join(Conspiracy.class)
            .using(Conspiracy::getName)
            .where(Conspiracy::getName)
            .equalTo("nsa")
            .select(personMapper, this::openConnection);

        assertEquals(smith, person.get());

        delete(relationship(Conspiracy.class, Person.class))
            .whereLeft(Conspiracy::getName)
            .equalTo("nsa")
            .andRight(Person::getLastName)
            .equalTo("smith")
            .execute(this::openConnection);

    }

```
