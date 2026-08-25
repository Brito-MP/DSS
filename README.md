# McLei 

**McLei** is a restaurant order and operations management system developed in **Java** for the **Software Systems Development (Desenvolvimento de Sistemas de Software — DSS)** course at the **University of Minho**.

The project models the workflow of a fast-food restaurant, covering customer orders, payments, food preparation, stock management, employee authentication and workstations. The system follows a **layered architecture** and uses the **DAO pattern** with a **MariaDB** database for persistence.

## Features

### Customer

Customers can:

* Create **Take Away** or **Dine-In** orders;
* Browse available menus and individual products;
* Add products and notes to an order;
* Customize products by replacing ingredients with available alternatives;
* Pay directly through **MB Way** or choose payment at the cashier;
* Receive an invoice;
* Check the current order status.

### Employees

Employees authenticate using their worker ID and password and can occupy an available workstation.

The system supports different workstation types:

* **Cashier**

  * View unpaid orders;
  * Validate payments;
  * Read manager messages.

* **Kitchen**

  * View orders currently being prepared;
  * Start and finish order preparation;
  * Request ingredients;
  * Check ingredient availability;
  * Delay an order and update the preparation queue;
  * Read manager messages.

* **Packing / Plating**

  * View completed orders;
  * Mark orders as delivered.

### Administrator

Administrators have access to additional management operations:

* View average preparation time;
* Check ingredient stock;
* View available workstations;
* Register new employees;
* Send messages to employees.

## Architecture

The application follows a layered design, separating the user interface, business logic and persistence.

```text
User Interface
     │
     ▼
RestauranteFacade
     │
     ├── PedidosFacade
     ├── GestaoFacade
     └── PreparacoesFacade
             │
             ▼
            DAOs
             │
             ▼
          MariaDB
```

The main subsystems are:

* **Pedidos** — creation, customization, payment and lifecycle of orders;
* **Gestão** — employees, authentication, stock and management operations;
* **Preparações** — workstations, preparation queues and ingredient handling.

`RestauranteFacade` acts as the main facade of the application and coordinates communication between these subsystems.

## Persistence

The project uses **MariaDB** and JDBC for data persistence.

DAO classes are responsible for accessing the database:

```text
AlimentoDAO
FuncionarioDAO
PedidoDAO
PostoDAO
ProdutoDAO
```

When the application starts, the database structure and initial data are automatically initialized when necessary.

## Technologies

* **Java 21**
* **Gradle**
* **MariaDB**
* **JDBC**
* **JUnit 5**
* **Guava**
* DAO Pattern
* Facade Pattern
* Layered Architecture

## Project Structure

```text
DSS-PROJECT/
├── app/
│   ├── src/
│   │   ├── main/java/
│   │   │   ├── App.java
│   │   │   ├── NewMenu.java
│   │   │   ├── data/
│   │   │   └── model/
│   │   │       ├── gestao/
│   │   │       ├── pedidos/
│   │   │       └── preparacoes/
│   │   └── test/
│   └── build.gradle.kts
├── report/
│   └── DSS-report.pdf
├── gradle/
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

The repository also includes the project modelling files and documentation, containing domain models, use cases, class diagrams, sequence diagrams, package diagrams and component diagrams.

## Requirements

To run the project you need:

* **Java 21**
* **MariaDB**
* A database named `Restaurante`

The database connection can be configured in:

```text
app/src/main/java/data/DAOconfig.java
```

Update the database credentials according to your local MariaDB installation.

## Running the Project

Move to the project directory:

```bash
cd DSS-PROJECT
```

If necessary, give the Gradle wrapper execution permission:

```bash
chmod +x gradlew
```

Run the application:

```bash
./gradlew run --console plain -q
```

On Windows:

```powershell
gradlew.bat run --console plain -q
```

## Main Workflow

```text
Customer creates order
        ↓
Payment
        ↓
Order enters preparation queue
        ↓
Kitchen prepares order
        ↓
Order is completed
        ↓
Packing / Plating
        ↓
Order is delivered
```

## Documentation

A complete project report is available at:

```text
DSS-PROJECT/report/DSS-report.pdf
```

The report includes the domain model, use cases, class and sequence diagrams, DAO architecture, package and component diagrams, as well as a user manual.

## Future Improvements

* Dynamic creation and management of menus;
* Support for multiple restaurants in the same chain;
* Restaurant-specific data and statistics.

## Authors

* **Beatriz Araújo** — a107318
* **Gonçalo Silva** — a106811
* **Henrique Brito** — a107378
* **José Miguel Sampaio** — a106908
* **José Simão Campos** — a107321

## Academic Context

**University of Minho**
Bachelor's Degree in **Software Engineering (Engenharia Informática)**
Course: **Software Systems Development (Desenvolvimento de Sistemas de Software — DSS)**
