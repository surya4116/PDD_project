-- SmartQueue Pro MySQL Database Setup Script
-- Import this script directly inside phpMyAdmin to instantly initialize the "smartqueue_db" database.

CREATE DATABASE IF NOT EXISTS `smartqueue_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `smartqueue_db`;

-- 1. categories Table
CREATE TABLE IF NOT EXISTS `categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `iconName` varchar(100) NOT NULL,
  `prefix` varchar(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed categories
INSERT INTO `categories` (`id`, `name`, `iconName`, `prefix`) VALUES
(1, 'Hospital', 'LocalHospital', 'HSP'),
(2, 'Bank', 'AccountBalance', 'BNK'),
(3, 'Government Office', 'Gavel', 'GOV'),
(4, 'Retail Store', 'Store', 'RTL'),
(5, 'College', 'School', 'CLG'),
(6, 'Railway Station', 'Train', 'RLW'),
(7, 'Passport Office', 'CardMembership', 'PPT'),
(8, 'Customer Service Center', 'SupportAgent', 'CSC')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 2. users Table
CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fullname` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL UNIQUE,
  `phone` varchar(20) NOT NULL UNIQUE,
  `password` varchar(255) NOT NULL,
  `isDisabled` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default user (password is "user123")
INSERT INTO `users` (`id`, `fullname`, `email`, `phone`, `password`, `isDisabled`) VALUES
(1, 'John Smith', 'user@smith.com', '9876543210', 'user123', 0)
ON DUPLICATE KEY UPDATE `fullname`=VALUES(`fullname`);

-- 3. providers Table (Service Providers)
CREATE TABLE IF NOT EXISTS `providers` (
  `provider_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL UNIQUE,
  `phone` varchar(20) NOT NULL UNIQUE,
  `password` varchar(255) NOT NULL,
  `shopName` varchar(255) NOT NULL,
  `categoryId` int(11) NOT NULL,
  `location` varchar(255) NOT NULL,
  PRIMARY KEY (`provider_id`),
  FOREIGN KEY (`categoryId`) REFERENCES `categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default Service Providers (passwords are "provider123")
INSERT INTO `providers` (`provider_id`, `name`, `email`, `phone`, `password`, `shopName`, `categoryId`, `location`) VALUES
(1, 'Dr. Alice Smith', 'hospital@smartq.com', '1111111111', 'provider123', 'City Care Hospital', 1, 'Metro Plaza, Block A'),
(2, 'James Financial', 'bank@smartq.com', '2222222222', 'provider123', 'National Trust Bank', 2, 'Financial Square, Block 4'),
(3, 'Officer Green', 'postoffice@smartq.com', '3333333333', 'provider123', 'Central Post Office', 3, 'Civil Plaza, Gate 2'),
(4, 'Manager Joe', 'retail@smartq.com', '4444444444', 'provider123', 'Supermart Retail', 4, 'High Street Mall, Ground Floor'),
(5, 'Dean Carter', 'college@smartq.com', '5555555555', 'provider123', 'State Science College', 5, 'University Boulevard, Campus East'),
(6, 'Station Master', 'railway@smartq.com', '6666666666', 'provider123', 'Central Railway Station', 6, 'Station Road, Junction A'),
(7, 'Passport Officer', 'passport@smartq.com', '7777777777', 'provider123', 'Regional Passport Office', 7, 'Visa Tower, 3rd Floor'),
(8, 'Lead Agent', 'csc@smartq.com', '8888888888', 'provider123', 'Customer Care Center', 8, 'Broadband Way, Sector 15')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 4. service_centers Table
CREATE TABLE IF NOT EXISTS `service_centers` (
  `center_id` int(11) NOT NULL AUTO_INCREMENT,
  `center_name` varchar(255) NOT NULL,
  `categoryId` int(11) NOT NULL,
  `address` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL DEFAULT '',
  `image` varchar(255) NOT NULL DEFAULT '',
  `status` varchar(50) NOT NULL DEFAULT 'Active',
  `providerId` int(11) NOT NULL,
  PRIMARY KEY (`center_id`),
  FOREIGN KEY (`categoryId`) REFERENCES `categories` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`providerId`) REFERENCES `providers` (`provider_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed service centers
INSERT INTO `service_centers` (`center_id`, `center_name`, `categoryId`, `address`, `phone`, `providerId`) VALUES
(1, 'City Care Hospital', 1, '102 Metro Health Ave, City Center', '1111111111', 1),
(2, 'National Trust Bank', 2, 'Financial Square, Block 4', '2222222222', 2),
(3, 'Central Post & Gov Office', 3, 'Civil Plaza, Gate 2', '3333333333', 3),
(4, 'Supermart Retail', 4, 'High Street Mall, Ground Floor', '4444444444', 4),
(5, 'State Science College', 5, 'University Boulevard, Campus East', '5555555555', 5),
(6, 'Central Railway Station', 6, 'Station Road, Junction A', '6666666666', 6),
(7, 'Regional Passport Office', 7, 'Visa Tower, 3rd Floor', '7777777777', 7),
(8, 'Telecom Customer Center', 8, 'Broadband Way, Sector 15', '8888888888', 8)
ON DUPLICATE KEY UPDATE `center_name`=VALUES(`center_name`);

-- 5. services Table
CREATE TABLE IF NOT EXISTS `services` (
  `service_id` int(11) NOT NULL AUTO_INCREMENT,
  `center_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `duration` varchar(50) NOT NULL,
  PRIMARY KEY (`service_id`),
  FOREIGN KEY (`center_id`) REFERENCES `service_centers` (`center_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed services
INSERT INTO `services` (`service_id`, `center_id`, `name`, `duration`) VALUES
(1, 1, 'General Consultation', '15 mins'),
(2, 1, 'Pediatrics Special', '20 mins'),
(3, 1, 'Dental Checkup', '30 mins'),
(4, 2, 'Account Opening & KYC', '15 mins'),
(5, 2, 'Loan Consultation', '25 mins'),
(6, 2, 'Cash Deposit & Withdrawal', '10 mins'),
(7, 3, 'Document Verification', '20 mins'),
(8, 3, 'License Application', '30 mins'),
(9, 4, 'Product Exchange / Return', '10 mins'),
(10, 4, 'Loyalty Card Setup', '15 mins'),
(11, 5, 'Admissions Office', '20 mins'),
(12, 5, 'Scholarship Queries', '15 mins'),
(13, 6, 'Ticket Counter Enquiry', '10 mins'),
(14, 6, 'Luggage Booking', '20 mins'),
(15, 7, 'Passport Renewal Verification', '15 mins'),
(16, 7, 'Biometrics Submission', '10 mins'),
(17, 8, 'Sim Card Activation', '15 mins'),
(18, 8, 'Billing Disputes', '20 mins')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 6. slots Table
CREATE TABLE IF NOT EXISTS `slots` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `serviceId` int(11) NOT NULL,
  `centerId` int(11) NOT NULL,
  `date` varchar(20) NOT NULL,
  `startTime` varchar(20) NOT NULL,
  `endTime` varchar(20) NOT NULL,
  `maxTokens` int(11) NOT NULL,
  `currentTokens` int(11) NOT NULL DEFAULT 0,
  `status` varchar(20) NOT NULL DEFAULT 'Upcoming',
  `delayMins` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`serviceId`) REFERENCES `services` (`service_id`) ON DELETE CASCADE,
  FOREIGN KEY (`centerId`) REFERENCES `service_centers` (`center_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed slots dynamically for General Consultation (Service id = 1, Center id = 1)
INSERT INTO `slots` (`id`, `serviceId`, `centerId`, `date`, `startTime`, `endTime`, `maxTokens`, `currentTokens`, `status`, `delayMins`) VALUES
(1, 1, 1, '2026-05-31', '09:00 AM', '09:30 AM', 15, 0, 'Running', 0),
(2, 1, 1, '2026-05-31', '11:30 AM', '12:00 PM', 15, 0, 'Upcoming', 0),
(3, 1, 1, '2026-06-01', '10:00 AM', '10:30 AM', 15, 0, 'Upcoming', 0),
(4, 2, 1, '2026-05-31', '09:00 AM', '09:30 AM', 15, 0, 'Running', 0),
(5, 3, 1, '2026-05-31', '11:30 AM', '12:00 PM', 15, 0, 'Upcoming', 0)
ON DUPLICATE KEY UPDATE `startTime`=VALUES(`startTime`);

-- 7. bookings Table
CREATE TABLE IF NOT EXISTS `bookings` (
  `booking_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `slot_id` int(11) NOT NULL,
  `center_id` int(11) NOT NULL,
  `token_number` varchar(20) NOT NULL,
  `queue_position` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'Waiting',
  `booking_time` bigint(20) NOT NULL,
  PRIMARY KEY (`booking_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`slot_id`) REFERENCES `slots` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`center_id`) REFERENCES `service_centers` (`center_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. tokens Table
CREATE TABLE IF NOT EXISTS `tokens` (
  `token_id` int(11) NOT NULL AUTO_INCREMENT,
  `booking_id` int(11) NOT NULL,
  `token_number` varchar(20) NOT NULL,
  `estimated_wait_time_mins` int(11) NOT NULL,
  `issued_at` bigint(20) NOT NULL,
  PRIMARY KEY (`token_id`),
  FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. queues Table
CREATE TABLE IF NOT EXISTS `queues` (
  `queue_id` int(11) NOT NULL AUTO_INCREMENT,
  `booking_id` int(11) NOT NULL,
  `token_number` varchar(20) NOT NULL,
  `position` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'Waiting',
  PRIMARY KEY (`queue_id`),
  FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. notifications Table
CREATE TABLE IF NOT EXISTS `notifications` (
  `notification_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. feedback Table
CREATE TABLE IF NOT EXISTS `feedback` (
  `feedback_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `rating` float NOT NULL,
  `comments` text NOT NULL,
  `timestamp` bigint(20) NOT NULL,
  PRIMARY KEY (`feedback_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. password_resets Table (for Forgot Password OTP flow)
CREATE TABLE IF NOT EXISTS `password_resets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_type` enum('user','provider') NOT NULL,
  `user_id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `otp` varchar(6) NOT NULL,
  `reset_token` varchar(255) DEFAULT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_lookup` (`user_type`, `user_id`),
  INDEX `idx_reset_token` (`reset_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
