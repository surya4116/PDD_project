<?php
require_once '../../config/database.php';

try {
    // Check if adminId column exists in service_centers
    $stmt = $pdo->query("SHOW COLUMNS FROM `service_centers` LIKE 'adminId'");
    $adminIdExists = $stmt->fetch();

    // Check if providerId column exists in service_centers
    $stmt = $pdo->query("SHOW COLUMNS FROM `service_centers` LIKE 'providerId'");
    $providerIdExists = $stmt->fetch();

    $messages = [];

    if ($adminIdExists && !$providerIdExists) {
        // Rename adminId to providerId
        $pdo->exec("ALTER TABLE `service_centers` CHANGE `adminId` `providerId` int(11) NOT NULL");
        $messages[] = "✅ Renamed 'adminId' to 'providerId' in service_centers table.";
    } elseif ($providerIdExists) {
        $messages[] = "✅ Column 'providerId' already exists - no action needed.";
    } else {
        // Neither column exists - add providerId
        $pdo->exec("ALTER TABLE `service_centers` ADD COLUMN `providerId` int(11) NOT NULL DEFAULT 1");
        $messages[] = "✅ Added 'providerId' column to service_centers table.";
    }

    // Also verify providers table has correct columns
    $stmt = $pdo->query("SHOW COLUMNS FROM `providers`");
    $cols = $stmt->fetchAll(PDO::FETCH_COLUMN);
    $messages[] = "ℹ️ providers table columns: " . implode(', ', $cols);

    // Verify service_centers table columns
    $stmt = $pdo->query("SHOW COLUMNS FROM `service_centers`");
    $cols = $stmt->fetchAll(PDO::FETCH_COLUMN);
    $messages[] = "ℹ️ service_centers table columns: " . implode(', ', $cols);

    echo json_encode([
        "success" => true,
        "messages" => $messages
    ], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ], JSON_PRETTY_PRINT);
}
?>
