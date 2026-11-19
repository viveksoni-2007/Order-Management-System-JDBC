ORDER MANAGEMENT SYSTEM - JDBC + MYSQL

REQUIREMENTS
1. JDK 17 or later
2. MySQL Server
3. IntelliJ IDEA / Eclipse / VS Code with Java and Maven support

STEP 1 - DATABASE
Open MySQL Workbench (or MySQL command line).
Run src/main/resources/schema.sql.
This creates ecommerce_db and Orders table.

STEP 2 - SET MYSQL PASSWORD
Open Main.java and replace YOUR_MYSQL_PASSWORD with your MySQL root password.
If your MySQL username is not root, update USER too.

STEP 3 - OPEN PROJECT
Open the OrderManagementJDBC folder as a Maven project in IntelliJ IDEA or VS Code.
Wait for Maven to download mysql-connector-j.

STEP 4 - RUN
Run com.example.ecommerce.Main.
Choose menu options 1 to 6. Option 0 exits.
If the project is run again, option 1 may report duplicate IDs; this is expected and handled.

QUESTION MAPPING
1(a) insertThreeOrders(): PreparedStatement; duplicate primary-key exception handled.
1(b) displayByStatus(): status=?, quantity > 2, ORDER BY unit_price DESC.
1(c) increaseQuantity(): quantity = quantity + ?; checks affected row count.
1(d) updateStatus(): validates allowed statuses, checks existence, updates and displays row.
1(e) displayTotalValue(): SUM(quantity * unit_price); deleteCancelledOrder() deletes only Cancelled rows.

IMPORTANT
This project uses PreparedStatement for all user-supplied SQL values.
Do not share your database password. Keep it local on your computer.
