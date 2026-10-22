1. AI Tool Used

I used ChatGPT to understand the requirements, get help with coding, fix errors, and understand how different parts of the project work.

2. About the Project

Project Name: Housefull – Seat Booking Website

The aim of this project is to build a website where users can view events, select seats, book tickets, make payments, and get tickets. The main challenge is to make sure that two users cannot book the same seat when many people try to book at the same time.

Technologies used:

Frontend: React and Vite

Backend: Java and Spring Boot

Database: MySQL

3. How I Used ChatGPT

1. Understanding the project requirements

I used ChatGPT to understand the Housefull challenge and decide how to start building the application.

It helped me break the project into smaller parts, such as event creation, seat selection, booking, payment, ticket generation, check-in, and the organiser dashboard.

How I used the response: I used it as a guide while planning and developing the project.

2. Building the backend using Spring Boot

I used ChatGPT to understand how to create backend APIs and connect the different parts of the application.

It helped me work with controllers, services, repositories, and MySQL for managing events, seats, and bookings.

How I used the response: I used the guidance while implementing the backend and made changes whenever required during development.

3. Handling multiple users booking the same seat

One important part of the project was preventing two users from booking the same seat at the same time.

I used ChatGPT to understand concurrency issues and how database locking can help prevent duplicate seat bookings.

How I used the response: I implemented transactional booking logic with pessimistic locking and tested two requests trying to hold the same seat. One request succeeded with HTTP 201, while the other received HTTP 409 Conflict.

4. Releasing seats after five minutes

The challenge requires seats to be held for five minutes while the user completes payment. If payment is not completed in time, the seats should become available again.

I used ChatGPT to understand how to implement hold expiration and automatically release expired seats.

How I used the response: I tested the booking flow and verified that an expired seat hold returned the seat to AVAILABLE.

5. Fixing backend and database issues

During development, I faced issues related to application startup, database configuration, environment variables, and tests.

I used ChatGPT to understand the errors and identify what needed to be checked.

How I used the response: I checked the configuration, made the required corrections, and ran the application and tests again.

6. Testing the APIs

I used ChatGPT to understand how to test the APIs and verify whether the booking flow was working correctly.

I checked event and seat retrieval, booking holds, payment, cancellation, and check-in.

How I used the response: I used the API responses and seat statuses to check whether the application was behaving as expected.

4. Problems I Encountered and How I Checked Them

The challenge asks for three examples of incorrect AI-generated code and how I identified the problems. The following are candidate examples based on the issues encountered during development. I need to verify the original AI responses before claiming that each one was specifically caused by incorrect AI-generated code.

1. Database configuration issue

Problem: The application had trouble starting when the required database environment variables were not available.

How I identified it: I checked the application startup and test errors and found that the datasource configuration needed attention.

What I did: I checked the database configuration and environment variables, then ran the application again.

What I learned: I should not blindly copy configuration from AI. I need to check whether it matches my own environment.

2. Handling HTTP 409 responses

Problem: During booking tests, a request returned HTTP 409 Conflict because the seat was unavailable.

How I identified it: I compared the response with the expected booking behavior and checked whether the seat was already held or unavailable.

What I did: I treated the conflict as an expected result for competing booking requests rather than automatically considering it a server error.

What I learned: I need to understand the meaning of HTTP status codes before deciding whether a test has failed.
How I Verified the Work

I did not rely only on the code suggested by ChatGPT. I checked the application by:

Running the Spring Boot backend.

Checking API responses.

Testing seat booking and conflicting requests.

Verifying that expired holds released seats.

Running automated tests.

Checking the application after making changes.

This helped me understand the code better and identify issues during development.

6. Final Note

I used ChatGPT as a learning and development assistant while building Housefull. It helped me understand concepts, implement features, and debug problems. I tested the application and checked the results instead of assuming that every suggestion was correct.

I am responsible for understanding the code, making the necessary changes, and verifying that the final application meets the challenge requirements.