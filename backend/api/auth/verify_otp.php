<?php
require_once '../../config/database.php';

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['identifier']) || !isset($data['otp'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$identifier = trim($data['identifier']);
$otp = trim($data['otp']);

if (empty($identifier) || empty($otp)) {
    echo json_encode(["success" => false, "message" => "Phone/email and OTP are required"]);
    exit;
}

try {
    // 1. Find the user
    $stmt = $pdo->prepare("SELECT id FROM users WHERE phone = ? OR email = ? LIMIT 1");
    $stmt->execute([$identifier, $identifier]);
    $user = $stmt->fetch();
    $userType = 'user';
    $userId = null;

    if ($user) {
        $userId = $user['id'];
    } else {
        $stmt = $pdo->prepare("SELECT provider_id FROM providers WHERE phone = ? OR email = ? LIMIT 1");
        $stmt->execute([$identifier, $identifier]);
        $provider = $stmt->fetch();
        if ($provider) {
            $userType = 'provider';
            $userId = $provider['provider_id'];
        }
    }

    if ($userId === null) {
        echo json_encode(["success" => false, "message" => "No account found"]);
        exit;
    }

    // 2. Look up the OTP in password_resets
    $stmt = $pdo->prepare("SELECT * FROM password_resets WHERE user_type = ? AND user_id = ? AND otp = ? LIMIT 1");
    $stmt->execute([$userType, $userId, $otp]);
    $resetRecord = $stmt->fetch();

    if (!$resetRecord) {
        echo json_encode(["success" => false, "message" => "Invalid OTP. Please check and try again"]);
        exit;
    }

    // 3. Check expiry
    $expiresAt = strtotime($resetRecord['expires_at']);
    if (time() > $expiresAt) {
        // Delete expired OTP
        $stmt = $pdo->prepare("DELETE FROM password_resets WHERE id = ?");
        $stmt->execute([$resetRecord['id']]);
        echo json_encode(["success" => false, "message" => "OTP has expired. Please request a new one"]);
        exit;
    }

    // 4. Generate a unique reset token
    $resetToken = bin2hex(random_bytes(32));

    // 5. Store the reset token (extends expiry by 5 more minutes for password entry)
    $newExpiry = date('Y-m-d H:i:s', time() + 300);
    $stmt = $pdo->prepare("UPDATE password_resets SET reset_token = ?, expires_at = ? WHERE id = ?");
    $stmt->execute([$resetToken, $newExpiry, $resetRecord['id']]);

    echo json_encode([
        "success" => true,
        "message" => "OTP verified successfully",
        "resetToken" => $resetToken
    ]);

} catch (Exception $e) {
    error_log("Verify OTP Error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
