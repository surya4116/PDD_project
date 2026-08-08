<?php
require_once '../../config/database.php';
try {
    $stmt = $pdo->query("SELECT * FROM tokens ORDER BY token_id DESC");
    $tokens = $stmt->fetchAll();
    echo json_encode(["success" => true, "tokens" => $tokens]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
