# Sports Arena Booking System

Sports Arena Booking System is a web application for booking sports facilities such as football turfs, cricket nets, badminton and tennis courts, and swimming lanes. Customers create an account, add their teams, browse the available arenas with their prices and ratings, and book an hourly time slot for a chosen date. They can follow each booking as it moves from pending to confirmed, checked in and completed, cancel it while it is still pending, and rate the session afterwards.

Administrators use the same application to run the venue. They manage arena categories and packages, block time slots for maintenance, confirm and update customer bookings, and see dashboards and reports on bookings and ratings. The system prevents double bookings and stops slots that are already booked from being blocked. It is built as a React frontend talking to a set of small Java Spring Boot services, each with its own database.
