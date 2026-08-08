<?php
require_once '../../config/database.php';

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['resetToken']) || !isset($data['newPassword'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$resetToken = trim($data['resetToken']);
$newPassword = $data['newPassword'];

if (empty($resetToken) || empty($newPassword)) {
    echo json_encode(["success" => false, "message" => "Reset token and new password are required"]);
    exit;
}

if (strlen($newPassword) < 4) {
    echo json_encode(["success" => false, "message" => "Password must be at least 4 characters"]);
    exit;
}

try {
    // 1. Find the reset record by token
    $stmt = $pdo->prepare("SELECT * FROM password_resets WHERE reset_token = ? LIMIT 1");
    $stmt->execute([$resetToken]);
    $resetRecord = $stmt->fetch();

    if (!$resetRecord) {
        echo json_encode(["success" => false, "message" => "Invalid or expired reset token. Please start over"]);
        exit;
    }

    // 2. Check expiry
    $expiresAt = strtotime($resetRecord['expires_at']);
    if (time() > $expiresAt) {
        $stmt = $pdo->prepare("DELETE FROM password_resets WHERE id = ?");
        $stmt->execute([$resetRecord['id']]);
        echo json_encode(["success" => false, "message" => "Reset token has expired. Please start over"]);
        exit;
    }

    $userType = $resetRecord['user_type'];
    $userId = $resetRecord['user_id'];

    // 3. Hash the new password
    $hashedPassword = password_hash($newPassword, PASSWORD_BCRYPT);

    // 4. Update the password in the appropriate table
    if ($userType === 'provider') {
        $stmt = $pdo->prepare("UPDATE providers SET password = ? WHERE provider_id = ?");
    } else {
        $stmt = $pdo->prepare("UPDATE users SET password = ? WHERE id = ?");
    }
    $stmt->execute([$hashedPassword, $userId]);

    // 5. Delete all reset records for this user
    $stmt = $pdo->prepare("DELETE FROM password_resets WHERE user_type = ? AND user_id = ?");
    $stmt->execute([$userType, $userId]);

    echo json_encode([
        "success" => true,
        "message" => "Password reset successfully. You can now sign in with your new password"
    ]);

} catch (Exception $e) {
    error_log("Reset Password Error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
