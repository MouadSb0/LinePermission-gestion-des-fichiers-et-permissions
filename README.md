# LinePerm to AuditDB: Comprehensive Java Security & Audit System

Welcome to the **LinePerm & AuditDB** repository. This project is a progressive exploration of Java development, evolving from a simple console-based file permission simulator to a fully-fledged relational database management system with advanced analytics and Object-Oriented design patterns.

The project is divided into three distinct parts (Briefs), each building upon the last to introduce new technologies and architectural concepts.

---

## 📖 Project Overview

*   **Part 1 (LinePermission):** A console application that simulates a simplified Linux file permission system. It handles user authentication, session management, file creation, and access control (Owner vs. Others) using text files for persistence.
*   **Part 2 (LogAnalyzer):** Extends Part 1 by analyzing the generated `access.log` files. It uses Java Streams API and Lambda expressions to extract meaningful statistics from raw logs (e.g., most active users, denied access counts).
*   **Part 3 (AuditDB):** Migrates the data persistence layer from text files to a relational database using JDBC. It implements the DAO (Data Access Object) design pattern, advanced OOP principles (Abstraction, Inheritance, Polymorphism), and performs complex SQL queries for auditing.

---

## 🚀 Architecture & Tech Stack

*   **Language:** Java (JDK 17+ recommended)
*   **Storage:** Text Files (Part 1 & 2) → Relational Database (Part 3)
*   **Libraries:** `jBCrypt` (Password hashing), JDBC (Database connectivity)
*   **Concepts:** OOP, Streams API, Lambda Expressions, Design Patterns (Singleton, DAO), SQL (Joins, Aggregations, Transactions).

---

## 🗂️ Detailed Breakdown

### Part 1: LinePermission (Core & File Permissions)
**Goal:** Build a secure console shell for managing files.
*   **Authentication:** User registration (`signup`), login (`login`), and logout (`logout`). Passwords are hashed using `BCrypt`.
*   **File Management:** Create (`touch`), list (`ls -l`), read (`cat`), write/edit (`nano`), and change permissions (`chmod`).
*   **Permission Model:** Simplified Linux model. Two categories: **Owner** and **Others**. Three rights: **r** (read), **w** (write), **d** (delete). Format: `rwd|r--`.
*   **Access Control:** Only the owner can modify rights. If a user is not the owner, only the "Others" block applies. Missing rights result in `Permission denied.`
*   **Persistence:** Accounts (`users.txt`) and file metadata (`files.txt`) are stored locally. File contents are stored in a `data/` directory.

### Part 2: LogAnalyzer (Stream Analytics)
**Goal:** Extract insights from the access logs generated in Part 1.
*   **Log Format:** `date;time;user;action;file;result` (e.g., `2026-09-01;09:14;mdidech;LECTURE;rapport.txt;OK`).
*   **CLI:** A `stats` command opens an interactive menu with 8 analytical options.
*   **Analytics Engine (Java Streams):**
    1.  Total actions count.
    2.  Total denied accesses (`filter` + `count`).
    3.  Distinct users (`map` + `distinct`).
    4.  Actions per user (`groupingBy` + `counting`).
    5.  Top 3 most consulted files (`groupingBy` + `sorted` + `limit`).
    6.  Denied accesses for a specific user (Requires `Scanner` input).
    7.  Most active user (`groupingBy` + `max` using `Optional`).
    8.  Action distribution by type.

### Part 3: AuditDB (Relational Database & Advanced OOP)
**Goal:** Modernize the application by migrating to a relational database.
*   **Database Schema:** 3 interrelated tables: `users`, `fichiers` (files), and `logs`. Includes constraints (unique login, foreign keys, immutability of logs).
*   **Design Patterns:**
    *   **Singleton:** `DBConnection` ensures a single database connection instance.
    *   **DAO Pattern:** `Dao<T>` interface and `AbstractDao<T>` class provide a common contract and code reuse for CRUD operations.
*   **Advanced OOP:** Heavy use of Encapsulation, Inheritance, Abstraction, and Polymorphism across the DAO implementations (`UserDao`, `FichierDao`, `LogDao`).
*   **Auditing via SQL:** Replaces Java Streams from Part 2 with SQL Joins, `GROUP BY`, and `COUNT` for statistics, allowing the system to handle millions of rows without memory saturation.
*   **Exception Handling:** Graceful handling of SQL exceptions (connection failures, constraint violations) and user input errors.

---

## 📁 Project Structure

```text
src/
├── main/
│   └── java/
│       └── ma/youcode/lineperm/
│           ├── Main.java                 # Entry point
│           ├── model/                    # Domain Objects
│           │   ├── User.java
│           │   ├── FichierProtege.java
│           │   └── AccessLog.java
│           ├── service/                  # Business Logic
│           │   ├── UserService.java
│           │   ├── FileService.java
│           │   └── LogAnalyzer.java
│           ├── access/                   # Access Control
│           │   └── ControleAcces.java
│           ├── dao/                      # Data Access Object (Part 3)
│           │   ├── Dao.java
│           │   ├── AbstractDao.java
│           │   ├── UserDao.java
│           │   ├── FichierDao.java
│           │   └── LogDao.java
│           ├── db/                       # Database Connection (Part 3)
│           │   └── DBConnection.java
│           └── ui/                       # User Interface
│               └── ConsoleApp.java
├── data/                                 # File contents (Part 1 & 2)
├── lib/                                  # External libraries (jBCrypt)
├── manifest.txt                          # JAR manifest (Part 2)
└── README.md
```

---

## 🛠️ Getting Started

### Prerequisites
*   Java Development Kit (JDK) 17 or higher.
*   A relational database (MySQL, PostgreSQL, or SQLite) for Part 3.
*   IDE (IntelliJ IDEA, Eclipse, or VS Code).

### Installation & Running
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/lineperm-auditdb.git
    cd lineperm-auditdb
    ```
2.  **Setup External Libraries:**
    Ensure `jbcrypt.jar` is placed in the `lib/` directory and added to your classpath.
3.  **Compile the project:**
    ```bash
    javac -d out -cp "lib/*" src/main/java/ma/youcode/lineperm/**/*.java
    ```
4.  **Run the application:**
    *   For Part 1 & 2 (Console mode):
        ```bash
        java -cp "out:lib/*" ma.youcode.lineperm.Main
        ```
    *   For Part 3 (Requires DB setup): Execute the provided SQL script to create tables, update `DBConnection.java` with your credentials, and run the main class.
5.  **Build JAR (Part 2 delivery):**
    ```bash
    jar cfm linepermission.jar manifest.txt -C out .
    java -jar linepermission.jar
    ```

---

## 💻 Command Reference

| Command | Usage | Description |
| :--- | :--- | :--- |
| `signup` | `signup` | Creates a new user account (prompts for login/password). |
| `login` | `login` | Authenticates a user and starts a session. |
| `logout` | `logout` | Ends the current session. |
| `exit` | `exit` | Quits the application. |
| `ls -l` | `ls -l` | Lists all files with their permissions (`rwd\|r-- owner filename`). |
| `touch` | `touch <filename>` | Creates an empty file (`rwd\|---`). |
| `cat` | `cat <filename>` | Displays file content (requires `r` right). |
| `nano` | `nano <filename>` | Multi-line editor replacing content (requires `w` right, ends with `EOF`). |
| `chmod` | `chmod <r\|w\|d> <filename>` | Grants a right to others (owner only). |
| `chmod -` | `chmod -<r\|w\|d> <filename>` | Revokes a right from others (owner only). |
| `stats` | `stats` | Opens the LogAnalyzer menu (Part 2). |

---

## 🎯 Key Learning Outcomes

By completing this project, the following competencies were demonstrated:
*   **Java Fundamentals:** String manipulation, loops, conditionals, arrays, and exception handling.
*   **OOP Mastery:** Encapsulation, inheritance, polymorphism, and abstraction (interfaces/abstract classes).
*   **Functional Programming:** Streams API, Lambda expressions, and Method References.
*   **Database Integration:** JDBC, `PreparedStatement`, SQL Joins, and data aggregation.
*   **Software Architecture:** Layered architecture (Model-Service-UI), DAO pattern, and Singleton pattern.
*   **Security:** Password hashing (BCrypt) and strict access control enforcement.
