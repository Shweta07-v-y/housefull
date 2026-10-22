# Housefull — Seat Booking System
A full-stack event seat-booking application built with Spring Boot, Java, MySQL, and React with Vite.

Features

Create events with a venue, date and time, and seat layout.

Configure seat prices by row.

View available, held, and booked seats.

Temporarily hold seats for five minutes while payment is pending.

Simulate successful or failed payments.

Generate booking tickets with booking codes and QR codes.

Validate tickets through staff check-in.

View organiser dashboard statistics for seats, bookings, revenue, and check-ins.

Cancel eligible bookings and release seats.

Prevent cancellation of tickets that have already been checked in.

Run automated backend tests with Maven.

Payments are simulated. This project does not process real payments or issue actual monetary refunds.

Technology Stack

Backend

Java

Spring Boot

Spring Web MVC

Spring Data JPA

Hibernate

MySQL

Maven

Frontend

React

Vite

npm

Prerequisites

Install the following before running the application:

Java Development Kit (JDK)

MySQL Server

Node.js and npm

The project includes a Maven Wrapper, so a separate Maven installation should not be required.

Database Setup

Start your MySQL server.

Create the database:

CREATE DATABASE housefull;

Configure the database credentials through environment variables. Do not commit passwords to Git.

The backend reads these properties from src/main/resources/application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/housefull
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

Run the Backend

Open a PowerShell terminal in the project root:

$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"

.\mvnw.cmd spring-boot:run

Replace YOUR_MYSQL_PASSWORD with your local MySQL password. Keep this terminal running.

The backend runs at:

http://localhost:8080

Run the Frontend

Open a second terminal:

cd frontend
npm install
npm run dev

Open the local URL printed by Vite, typically:

http://localhost:5173

Keep both backend and frontend running while using the application.

Run Automated Tests

From the project root, configure the database environment variables in the same terminal and run:

$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"

.\mvnw.cmd test

The test suite covers booking lifecycle behavior and application startup. Successful execution should finish with BUILD SUCCESS.

API Overview

The following routes are implemented in the application. Confirm the corresponding controller mappings before relying on this list as complete API documentation.

Method

Endpoint

Purpose

GET

/api/events/{eventId}/dashboard

Get organiser dashboard statistics

POST

/api/bookings/hold

Hold selected seats

POST

/api/bookings/{bookingCode}/payment

Simulate payment success or failure

POST

/api/bookings/{bookingCode}/cancel

Cancel an eligible booking

GET

/api/bookings/events/{eventId}

List bookings for an event

POST

/api/bookings/{bookingCode}/check-in

Validate a ticket and check in the customer

GET

/api/tickets/{bookingCode}

Retrieve ticket information

Event creation, event retrieval, and seat-layout endpoints may also be available through their respective controllers.

Booking Lifecycle

A customer selects available seats.

The backend temporarily holds those seats for five minutes.

The customer completes the simulated payment.

A successful payment confirms the booking and marks its seats as booked.

A failed payment or expired hold releases the seats.

A valid confirmed ticket can be checked in.

A checked-in booking cannot be cancelled.

Notes and Limitations

Payment and refund operations are simulated.

Database connection settings must be configured locally.

The application is intended as a challenge project, not a production ticketing or payment platform.

Do not commit credentials, private keys, or other secrets.