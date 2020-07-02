# Tests

## Running tests

By default, tests are run against a MySQL database called `karuta_test`.
You can configure the database settings and credentials in the respective
`application.yml` file under the `test/resources` folders of the different
subprojects.

## What's tested

The different layers of the project are tested, here are the different
parts of the code that are tested and how:

* `karuta-model`:
  - Documents: the Jackson classes (documents) are tested at different
    levels. Legacy stuff from version 2 is tested for obvious reasons
    but general serialization and deserialization can be tested as well.
  - Entities: apart from a few edges cases, entity are not widely tested
    as they are solely POJOs.
* `karuta-consumer`:
  - Repositories: every single repository's method must be tested against
    a real database to ensure that the queries return the expected results.
* `karuta-business`:
  - Services: services must be tested as they are handling the real process
    behind the scene; repositories can be mocked rather than triggering real
    database queries.
  - Security annotations
* `karuta-webapp`:
  - Controllers: the controller stack is tested to ensure that 1) rights
    are properly checked, 2) the expected status code is returned and 3)
    the API documentation is automatically generated from these tests.

    You can mock any component if necessary but as much as possible, the
    underlying implementation of the services should be executed (i.e.
    rely on `@SpyBean` beans rather than `@MockBean`).

## Tests skeleton

### Model and document tests

Model and document tests are regular Java classes with `@Test` methods:

~~~java
class ModelTest {
    @Test
    public void someTest() {

    }
}
~~~

### Repository tests

Repository tests must be annotated with `@RunWith` and `@RepositoryTest`.
A fake Spring application is provided in the testing environment where you
can configure things if needed.

~~~java
@RunWith(SpringRunner.class)
@RepositoryTest
public class SomeRepositoryTest {
    @Test
    public void someTest() {

    }
}
~~~

### Service tests

Service tests must be annotated with `@RunWith` and `@ServiceTest`.
A fake Spring application is provided in the testing environment where you
can configure things if needed.

~~~java
@RunWith(SpringRunner.class)
@ServiceTest
public class SomeServiceTest {
    @Test
    public void someTest() {

    }
}
~~~

### Controler tests

Controller tests must extend the `ControllerTest` class, where some common
logic as well as all repositories and services already wired can be found.
You also need to annotation them with the `@WebMvcTest` annotation.

As almost all requests require the user to be authenticated, you can rely
on the `@AsUser`, `@AsDesigner` or `@AsAdmin` method on tests to execute
the test on behalf of a certain role.

~~~java
@WebMvcTest
public class SomeControllerTest extends ControllerTest {
    @Test
    @AsUser
    public void someTest() {

    }
}
~~~

Inside controller tests, you can always rely on the `userId` property
to get the current user ID. This is useful when mocking methods; down
the line, the mocked user has this ID.
