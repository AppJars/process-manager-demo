# Process Manager Demo

Explore how Process Manager schedules, runs, and monitors Spring-managed tasks from a Vaadin
application. The demo includes a public landing page, a fully functional Processes view, sample
tasks, persistent execution history, and a guided tour available from every view.

Process Manager runs in free mode without a license key. All features are available, with a limit
of two processes. The demo seeds one process, leaving room to create another one.

## Included sample data

The first startup creates the **Service update** process with a schedule that runs every five
seconds. Its executions quickly populate the history dialog and demonstrate live status changes.
The seed runs only when the process table is empty.

Three Spring-managed `Runnable` tasks are available when creating or editing a process:

- `ReminderTask` logs a reminder message.
- `ServerTask` logs a generic server-side job.
- `UpdateServiceTask` simulates a service update that takes about 1.5 seconds.

## Prerequisites

- Java 21
- Maven 3.9 or newer

Docker and an external database are not required. The demo uses a local file-backed H2 database.

## Run the demo

1. Clone the repository and enter its directory.
2. Start the application:

   ```bash
   mvn spring-boot:run
   ```

3. Open [http://localhost:8080](http://localhost:8080).

The first run downloads and builds the Vaadin frontend bundle, so it can take longer than later
starts.

## Explore the demo

- Start on **Home** for an overview of the main capabilities and the free-license limit.
- Open **Process Manager → Processes** to inspect the seeded process.
- Use **New Process** to create a second process and assign one of the sample tasks.
- Pause or resume scheduling from the row control.
- Open the row actions menu to use **Execute now**, edit or clear the schedule, review execution
  history, or delete the process.
- Hover over **Guided tour** in the top navigation bar and choose **Processes Menu** for a walkthrough.
- Open the H2 console at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) with
  JDBC URL `jdbc:h2:./data/processmanagerdb`, user `sa`, and an empty password.

## Configuration

### Process Manager

| Property | Demo value | Purpose |
| --- | --- | --- |
| `com.appjars.processmanager.url.process` | `pm/process` | Route of the Processes view. |
| `com.appjars.processmanager.executionsdialog.datetimeformat` | `dd/MM/yyyy HH:mm:ss.SS` | Date-time format in the execution history grid. |
| `com.appjars.processmanager.executionsdialog.dateformat` | `dd/MM/yyyy` | Date format in execution history filters. |

### Database and persistence

| Property | Demo value | Purpose |
| --- | --- | --- |
| `spring.datasource.url` | `jdbc:h2:./data/processmanagerdb` | Local H2 database file. |
| `spring.datasource.username` | `sa` | Database user. |
| `spring.datasource.password` | Empty | Database password. |
| `spring.h2.console.enabled` | `true` | Enables the browser-based H2 console. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Keeps the demo schema synchronized. |
| `spring.jpa.open-in-view` | `false` | Keeps persistence work outside view rendering. |

### Vaadin

| Property | Demo value | Purpose |
| --- | --- | --- |
| `vaadin.i18n.provider` | `com.appjars.utils.i18n.DefaultI18nProvider` | Loads translations from `messages_*.properties`. |

## Production build

Create an executable production JAR:

```bash
mvn clean package -Pproduction
```

Run it with:

```bash
java -jar target/appjars-process-manager-demo-2.0.0.jar
```

## Stop and reset

Press `Ctrl+C` in the terminal that runs Maven. To reset the sample database, stop the application
and delete the `data` directory; the seed will be recreated on the next startup.

## Public resources

- [AppJars](https://www.appjars.com)
- [AppJars documentation](https://docs.appjars.com/)
- [Process Manager 2.0.0 Maven artifact](https://maven.appjars.com/com/appjars/appjars-process-manager-flow/2.0.0/appjars-process-manager-flow-2.0.0.pom)
