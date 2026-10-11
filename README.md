# Amar Dokan

A super shop management system I built with Java Swing and MySQL. It's a desktop app where a shop can manage products, stock, orders and users, and customers can browse and buy things.

The shop is set up for Bangladesh, so the demo products and prices are in BDT.

## What it can do

- Browse products without logging in
- Register and log in (new accounts wait for admin or manager approval)
- Five roles: Admin, Manager, Cashier, Customer and Supplier
- Customers can add products to a cart and check out
- Payment options: bKash, Nagad, Visa / Mastercard, Internet Banking (demo only, see below)
- Admin and Manager can add, edit and remove products, with pictures
- Stock tracking with low-stock and expiry dates
- Cashier billing with a 5% VAT calculation
- Sales reports
- Profile page with a profile picture

## What you need

- Java JDK 17 or newer
- MySQL Server 8 or newer
- MySQL Connector/J (already included in the `lib` folder)

## How to set it up

**1. Create the database**

Open a command prompt in the project folder and run:

```
mysql -u root -p < database/supershop.sql
```

This creates a database called `supershop` with all the tables and some demo data. If you've already imported it before, delete the old one first:

```
mysql -u root -p -e "DROP DATABASE IF EXISTS supershop;"
```

**2. Add your MySQL password**

Copy `db.properties.example` and rename the copy to `db.properties`. Open it and put your own MySQL password in:

```
url=jdbc:mysql://localhost:3306/supershop?useSSL=false&serverTimezone=UTC
user=root
password=YOUR_MYSQL_PASSWORD
```

`db.properties` is ignored by Git, so your password stays on your computer.

**3. Run it**

On Windows, double-click `run.bat`, or run it from the command prompt:

```
run.bat
```

On Linux or Mac:

```
./run.sh
```

If you use IntelliJ, open the project folder, add `lib/mysql-connector-j-9.2.0.jar` as a library, mark `src` as Sources Root, and run `Main.java`. Set the working directory to the project folder, otherwise the images and `db.properties` won't be found.

## Demo logins

| Role | Username | Password |
| --- | --- | --- |
| Admin | admin | admin123 |
| Manager | manager | manager123 |
| Cashier | cashier | cashier123 |
| Customer | customer | customer123 |
| Supplier | pantry_supplier | supplier123 |

These are just demo accounts for testing. Change them if you ever use this for real.

## About the payments

The payment screen is a demo. It doesn't connect to bKash, Nagad, any bank or any card service. No money is charged and nothing is verified. Please don't type real card numbers or PINs into it.

## Common problems

- **`ClassNotFoundException: com.mysql.cj.jdbc.Driver`**: the jar in `lib` isn't on the classpath. Use `run.bat`, or add the jar as a library in your IDE.
- **`Access denied for user 'root'`**: the password in `db.properties` is wrong.
- **`Communications link failure`**: MySQL isn't running. On Windows, start the MySQL service.
- **`Unknown database 'supershop'`**: you haven't imported `database/supershop.sql` yet.
- **`Table 'users' already exists`**: the database is already there. Drop it first, then import again.
- **Already have an older database?** Run `database/upgrade_payments.sql` once to add the payment columns. Don't run it on a fresh import.

## Project folders

```
src/model      the data classes
src/dao        database queries
src/service    the app logic
src/gui        all the Swing screens
src/images     product and profile pictures
database       SQL files
lib            MySQL driver
```

## Notes

The app is called Amar Dokan, but the database is still named `supershop`. I kept the old name so nothing breaks. If you rename it, change it in the SQL files and in `db.properties` too.
