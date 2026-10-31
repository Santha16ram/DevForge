# DevForge

A small project board for a 2nd-year Java course: keep project cards, task status, and code snippets in one place. Backend is **Java 25 + Spring Boot + Maven**. Data is stored with **JDBC** (`JdbcTemplate`), not JPA. The screen is plain **HTML / CSS / JS**.

Open [http://localhost:8080](http://localhost:8080) after you start it.

## Run locally (no MySQL install)

Java 25 is enough. From this folder:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

This uses the `h2` profile: an on-disk H2 database in **MySQL compatibility mode**, so the SQL you write is still MySQL-style JDBC.

Windows: `mvnw.cmd spring-boot:run`

## Run against real MySQL

1. Start MySQL (XAMPP / WAMP / service).
2. Default login in `src/main/resources/application-mysql.properties` is `root` with an empty password. Change it if yours is different.
3. Start with the mysql profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

The app creates the `devforge` database if the account is allowed to. Tables come from `src/main/resources/schema.sql`.

## What to show in viva

**OOP**
- `PersistableEntity` interface
- `WorkspaceItem` abstract class
- `Project`, `TaskItem`, `CodeSnippet` subclasses with their own `summarize()`
- `TaskStatus` / `Priority` enums (each has a display name)
- Recent activity on the desk uses polymorphism (`item.kind()` / `item.summarize()`)

**Collections**
- `EnumMap` for the kanban columns
- `LinkedHashSet` for unique assignees and snippet tags
- `ArrayList` + `HashMap` on the dashboard

**Regex**
- `InputPatterns` checks project keys (`CAMP`), emails, titles, tags, languages
- Snippet search compiles a case-insensitive pattern

**JDBC**
- `ProjectDao`, `TaskDao`, `SnippetDao` use `JdbcTemplate` and `PreparedStatement`

## Layout

```
src/main/java/com/devforge/
  domain/     models
  dao/        JDBC
  service/    validation + board logic
  web/        REST + static UI is under resources/static
  util/       regex
```

Sample rows load once if the project table is empty (campus lost-and-found + lab tracker).

H2 console (only on the h2 profile): [http://localhost:8080/h2](http://localhost:8080/h2)  
JDBC URL `jdbc:h2:file:./data/devforge`, user `sa`, blank password.
