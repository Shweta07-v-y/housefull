AI Tools Used
- ChatGPT — Used for technical guidance, debugging, implementation suggestions, test planning, and documentation support.
How I Used AI
I used AI as a development assistant while building Housefull. I used it to understand Spring Boot architecture, work through booking-related business logic, identify potential edge cases, improve tests, and prepare project documentation.

I reviewed the suggestions against my existing code and project requirements. I ran tests and checked application behaviour before considering a change complete. I treated generated code as a suggestion rather than assuming it was correct.

1. Understand the Project Requirements
Prompt summary:
I have a Housefull seat-booking challenge to complete. Help me understand the requirements and break the work into a practical implementation plan. I want to know how to structure the React frontend, Spring Boot backend, and MySQL database, and how the event creation, seat booking, temporary holds, payment, ticket generation, check-in, organiser dashboard, and tests should fit together.

Purpose: Break down the requirements into frontend, backend, database, booking workflow, and testing tasks.

Outcome: Used the guidance to structure the implementation around React, Spring Boot, and MySQL.
2. Implement Event Creation and Listing
Prompt summary:
Help me implement the event creation and event listing APIs in my existing Spring Boot project. Please follow the current project structure and explain which controller, service, repository, and entity files need to be changed. The organiser should be able to create an event with its date, time, venue, seats, and seat prices, and users should be able to retrieve the available events.

Purpose: Support organiser event creation and allow customers to browse available events.

Outcome: Used implementation guidance and adapted it to the existing project structure.
3. Implement Seat Selection and Temporary Holds
Prompt summary:
Help me implement the seat-booking flow in Housefull so a user can select one or more available seats and hold them for five minutes while completing payment. Check the existing booking and seat classes before suggesting changes. The backend should validate the selected seats, calculate the total from their stored prices, create the booking hold, and return a useful response to the frontend.

Purpose: Ensure seats are held temporarily while customers complete checkout.

Outcome: Used guidance for the booking service, expiry handling, and database locking. Reviewed the implementation against the booking requirements.
4. Improve Booking Expiry and Concurrency Handling
Prompt summary:
Review the booking and seat-reservation logic for race conditions and expired holds. Help me make sure two users cannot both successfully hold the same seat, and that a booking which passes its five-minute expiry releases only the seats associated with that expired hold. Explain the transaction and repository-locking changes needed, and suggest tests for conflicting booking requests and expiry behaviour.

Purpose: Handle expired bookings and reduce the risk of double-booking.

Outcome: Applied changes to the booking and repository logic and verified the behaviour through tests.
5. Implement Simulated Payment
Prompt summary:
Help me implement the challenge's simulated payment flow. The frontend needs a successful-payment option and a failed-payment option. Show how the frontend should call the Spring Boot API and how the service should update the booking and seat statuses for each outcome. Do not add a real payment gateway because simulated payment is sufficient for the required scope.

Purpose: Confirm bookings after successful payment and handle failed payment attempts correctly.

Outcome: Used guidance to implement the payment workflow and reviewed the resulting booking and seat statuses.
6. Implement Cancellation and Simulated Refunds
Prompt summary:
Help me implement booking cancellation and a simulated refund in the existing Housefull backend. Check the booking status before allowing cancellation, prevent cancellation of tickets that have already been checked in, update the booking and seat statuses consistently, and return a response the frontend can use to refresh the organiser dashboard.

Purpose: Allow organisers to cancel eligible bookings while preserving booking-state consistency.

Outcome: Adapted the suggested logic to the booking service and verified the relevant scenarios with tests.
7. Implement Organiser Dashboard and Check-In
Prompt summary:
Help me implement the organiser dashboard statistics and staff check-in flow. The dashboard should show sold, held, and available seats, booking information, revenue, and check-in counts. Staff should be able to enter a booking reference to validate a ticket, and the backend should prevent duplicate check-ins. Please follow the existing DTO, controller, service, and repository structure.

Purpose: Display booking and revenue information and validate tickets at the venue.

Outcome: Used guidance to implement the dashboard and check-in flows and test important behaviours.
8. Generate QR Tickets
Prompt summary:
Help me implement ticket retrieval after a booking is successfully paid. The ticket should show the event details, selected seats, booking reference, total amount, and a QR code. Explain how the backend response should supply the ticket data and how the React frontend should display the QR code.

Purpose: Provide customers with ticket details that can be validated during check-in.

Outcome: Integrated the ticket response with the frontend ticket display and reviewed the retrieval and validation flow.
9. Add and Improve Tests
Prompt summary:
Help me write or improve Maven tests for the most important Housefull booking rules: seat conflicts, successful and failed simulated payments, five-minute hold expiry, cancellation and refund behaviour, and staff check-in including duplicate check-in prevention. Use the existing classes and test setup, and explain what each test verifies.

Purpose: Verify important business rules and prevent regressions.

Outcome: Added and updated tests and ran the Maven test suite. The recorded test run completed with nine tests and no failures or errors


AI Suggestions That Required Verification
The following are examples of areas that required careful verification during development. They should only be treated as confirmed AI mistakes if the corresponding incident actually occurred.

Case 1: Verify Transaction and Seat-Locking Behaviour
Potential failure mode: A generated booking implementation might check seat availability without ensuring that concurrent transactions cannot both reserve the same seat.

How to verify: Inspect the transaction boundary and repository locking query, then test conflicting booking requests against the database.

Resolution: Keep the locking and transaction logic only after verifying that it works with the configured database and that the conflict tests cover the intended scenario.

Important: This is a verification concern, not proof that AI actually supplied incorrect code in this project.
Case 2: Verify Hold Expiry and Seat Release
Potential failure mode: An expiry implementation might mark a booking as expired without correctly releasing its held seats, or might release seats belonging to a newer booking.

How to verify: Test an expired hold, a still-valid hold, and a seat that has been held again after expiry. Check both booking and seat statuses.

Resolution: Retain the expiry implementation only after confirming that it updates the correct booking and seat records.

Important: This is another potential failure mode. Replace it with a real incident if the AI actually supplied incorrect code.
Reflection
AI helped accelerate implementation and explain unfamiliar concepts, but the final responsibility for correctness remained with me. I used code inspection, application behaviour, API checks, and automated tests to validate changes.

For future work, I would improve the workflow further by writing acceptance criteria before requesting code, asking for focused changes instead of broad rewrites, and verifying edge cases before merging changes.