<?php
require_once '../../config/database.php';

header('Content-Type: application/json');

try {
    $results = [];

    // 1. Get foreign keys on service_centers
    $stmt = $pdo->query("
        SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME 
        FROM information_schema.KEY_COLUMN_USAGE 
        WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'service_centers' 
          AND REFERENCED_TABLE_NAME IS NOT NULL
    ");
    $fks = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $results['current_fks'] = $fks;

    // Drop any FK on service_centers referencing 'admins' or referencing providerId/adminId to admins
    foreach ($fks as $fk) {
        $cName = $fk['CONSTRAINT_NAME'];
        $refTable = $fk['REFERENCED_TABLE_NAME'];
        if ($refTable === 'admins' || $fk['COLUMN_NAME'] === 'adminId' || $fk['COLUMN_NAME'] === 'providerId') {
            try {
                $pdo->exec("ALTER TABLE `service_centers` DROP FOREIGN KEY `$cName`");
                $results['actions'][] = "Dropped FK $cName pointing to $refTable";
            } catch (Exception $e) {
                $results['actions'][] = "Failed dropping FK $cName: " . $e->getMessage();
            }
        }
    }

    // 2. Make sure providers table exists
    $pdo->exec("
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
    ");
    $results['actions'][] = "Ensured providers table exists";

    // 3. Rename column adminId -> providerId if adminId exists
    $stmt = $pdo->query("SHOW COLUMNS FROM `service_centers` LIKE 'adminId'");
    if ($stmt->fetch()) {
        $pdo->exec("ALTER TABLE `service_centers` CHANGE `adminId` `providerId` int(11) NOT NULL");
        $results['actions'][] = "Renamed adminId column to providerId";
    }

    // 4. Add foreign key on service_centers(providerId) -> providers(provider_id)
    try {
        $pdo->exec("
            ALTER TABLE `service_centers` 
            ADD CONSTRAINT `fk_service_centers_provider` 
            FOREIGN KEY (`providerId`) REFERENCES `providers` (`provider_id`) ON DELETE CASCADE
        ");
        $results['actions'][] = "Added new FK constraint linking service_centers.providerId to providers.provider_id";
    } catch (Exception $e) {
        $results['actions'][] = "Add FK constraint note: " . $e->getMessage();
    }

    echo json_encode(["success" => true, "results" => $results], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    echo json_encode(["success" => false, "error" => $e->getMessage()]);
}
?>
