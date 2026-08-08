<?php
require_once '../config/database.php';

try {
    // Check if categories table has data
    $stmt = $pdo->query("SELECT COUNT(*) FROM categories");
    $count = $stmt->fetchColumn();

    if ($count == 0) {
        $categories = [
            ['Hospital', 'LocalHospital', 'HSP'],
            ['Bank', 'AccountBalance', 'BNK'],
            ['Government Office', 'AssuredWorkload', 'GOV'],
            ['Retail Store', 'Storefront', 'RTL'],
            ['College', 'School', 'COL'],
            ['Railway Station', 'Train', 'RLY'],
            ['Passport Office', 'CardMembership', 'PSP'],
            ['Customer Service Center', 'HeadsetMic', 'CSC']
        ];

        $insStmt = $pdo->prepare("INSERT INTO categories (name, iconName, prefix) VALUES (?, ?, ?)");
        
        $pdo->beginTransaction();
        foreach ($categories as $cat) {
            $insStmt->execute($cat);
        }
        $pdo->commit();
        
        echo "Database seeded with default categories successfully!";
    } else {
        echo "Database already has categories. No action taken.";
    }
} catch (Exception $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    echo "Error seeding database: " . $e->getMessage();
}
?>
