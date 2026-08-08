<?php
require_once '../../config/database.php';

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['identifier']) || empty(trim($data['identifier']))) {
    echo json_encode(["success" => false, "message" => "Please enter your phone number or email"]);
    exit;
}

$identifier = trim($data['identifier']);

try {
    // 1. Look up in users table (by phone or email)
    $stmt = $pdo->prepare("SELECT id, email, fullname FROM users WHERE phone = ? OR email = ? LIMIT 1");
    $stmt->execute([$identifier, $identifier]);
    $user = $stmt->fetch();
    $userType = 'user';
    $userId = null;
    $email = null;
    $name = null;

    if ($user) {
        $userId = $user['id'];
        $email = $user['email'];
        $name = $user['fullname'];
    } else {
        // 2. Look up in providers table
        $stmt = $pdo->prepare("SELECT provider_id, email, name FROM providers WHERE phone = ? OR email = ? LIMIT 1");
        $stmt->execute([$identifier, $identifier]);
        $provider = $stmt->fetch();
        if ($provider) {
            $userType = 'provider';
            $userId = $provider['provider_id'];
            $email = $provider['email'];
            $name = $provider['name'];
        }
    }

    if ($userId === null || $email === null) {
        echo json_encode(["success" => false, "message" => "No account found with this phone number or email"]);
        exit;
    }

    // 3. Generate 4-digit OTP
    $otp = str_pad(random_int(1000, 9999), 4, '0', STR_PAD_LEFT);

    // 4. Delete any existing OTPs for this user
    $stmt = $pdo->prepare("DELETE FROM password_resets WHERE user_type = ? AND user_id = ?");
    $stmt->execute([$userType, $userId]);

    // 5. Store OTP with 10-minute expiry
    $expiresAt = date('Y-m-d H:i:s', time() + 600); // 10 minutes
    $stmt = $pdo->prepare("INSERT INTO password_resets (user_type, user_id, email, otp, expires_at) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$userType, $userId, $email, $otp, $expiresAt]);

    require_once __DIR__ . '/../../config/smtp_mailer.php';

    // 6. Attempt to send email
    $subject = "SmartQueue Pro - Password Reset OTP";
    $body = "Hello $name,\n\nYour OTP for password reset is: $otp\n\nThis code is valid for 10 minutes.\n\nIf you didn't request this, please ignore this email.\n\n- SmartQueue Pro Team";

    $mailSent = SmtpMailer::sendMail($email, $subject, $body);
    if (!$mailSent) {
        // Fallback to PHP native mail function
        $headers = "From: noreply@smartqueue.com\r\nContent-Type: text/plain; charset=UTF-8";
        $mailSent = @mail($email, $subject, $body, $headers);
    }

    // 7. Mask email for display (e.g., "j***@gmail.com")
    $parts = explode('@', $email);
    $localPart = $parts[0];
    $domain = $parts[1];
    if (strlen($localPart) <= 2) {
        $maskedLocal = $localPart[0] . '***';
    } else {
        $maskedLocal = $localPart[0] . str_repeat('*', strlen($localPart) - 2) . substr($localPart, -1);
    }
    $maskedEmail = $maskedLocal . '@' . $domain;

    $response = [
        "success" => true,
        "message" => "OTP sent to your email",
        "maskedEmail" => $maskedEmail
    ];

    echo json_encode($response);

} catch (Exception $e) {
    error_log("Send OTP Error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
