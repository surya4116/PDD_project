<?php
require_once __DIR__ . '/config/database.php';

$sql = file_get_contents(__DIR__ . '/smartqueue_db.sql');

try {
    $pdo->exec($sql);
    echo json_encode(["success" => true, "message" => "Database schema initialized successfully! password_resets table is ready."]);
} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => "Database error: " . $e->getMessage()]);
}
