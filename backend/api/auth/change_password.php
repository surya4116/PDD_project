<?php
require_once '../../config/database.php';

$data = json_decode(file_get_contents('php://input'), true);

$userType = isset($data['userType']) ? trim($data['userType']) : 'Provider';
$id = isset($data['id']) ? (int)$data['id'] : (isset($data['userId']) ? (int)$data['userId'] : (isset($data['providerId']) ? (int)$data['providerId'] : 0));
$oldPassword = isset($data['oldPassword']) ? $data['oldPassword'] : '';
$newPassword = isset($data['newPassword']) ? $data['newPassword'] : '';

if ($id <= 0 || empty($oldPassword) || empty($newPassword)) {
    echo json_encode(["success" => false, "message" => "Please fill in all password fields"]);
    exit;
}

if (strlen($newPassword) < 4) {
    echo json_encode(["success" => false, "message" => "New password must be at least 4 characters"]);
    exit;
}

try {
    if (strtolower($userType) === 'provider') {
        $stmt = $pdo->prepare("SELECT provider_id, password FROM providers WHERE provider_id = ? LIMIT 1");
        $stmt->execute([$id]);
        $row = $stmt->fetch();

        if (!$row) {
            echo json_encode(["success" => false, "message" => "Provider account not found"]);
            exit;
        }

        $valid = password_verify($oldPassword, $row['password']) || ($oldPassword === $row['password']);
        if (!$valid) {
            echo json_encode(["success" => false, "message" => "Current password is incorrect"]);
            exit;
        }

        $newHash = password_hash($newPassword, PASSWORD_BCRYPT);
        $updateStmt = $pdo->prepare("UPDATE providers SET password = ? WHERE provider_id = ?");
        $updateStmt->execute([$newHash, $id]);

    } else {
        $stmt = $pdo->prepare("SELECT id, password FROM users WHERE id = ? LIMIT 1");
        $stmt->execute([$id]);
        $row = $stmt->fetch();

        if (!$row) {
            echo json_encode(["success" => false, "message" => "User account not found"]);
            exit;
        }

        $valid = password_verify($oldPassword, $row['password']) || ($oldPassword === $row['password']);
        if (!$valid) {
            echo json_encode(["success" => false, "message" => "Current password is incorrect"]);
            exit;
        }

        $newHash = password_hash($newPassword, PASSWORD_BCRYPT);
        $updateStmt = $pdo->prepare("UPDATE users SET password = ? WHERE id = ?");
        $updateStmt->execute([$newHash, $id]);
    }

    echo json_encode(["success" => true, "message" => "Password updated successfully"]);

} catch (Exception $e) {
    error_log("Change Password Error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
