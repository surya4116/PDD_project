<?php
require_once '../../config/database.php';
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['id']) || !isset($data['name']) || !isset($data['phone']) || !isset($data['email'])) {
    echo json_encode(["success" => false, "message" => "Missing fields"]);
    exit;
}

$id = (int)$data['id'];
$name = $data['name'];
$phone = $data['phone'];
$email = $data['email'];
$isDisabled = isset($data['isDisabled']) ? ($data['isDisabled'] ? 1 : 0) : null;

try {
    if ($isDisabled !== null) {
        $stmt = $pdo->prepare("UPDATE users SET fullname = ?, phone = ?, email = ?, isDisabled = ? WHERE id = ?");
        $stmt->execute([$name, $phone, $email, $isDisabled, $id]);
    } else {
        $stmt = $pdo->prepare("UPDATE users SET fullname = ?, phone = ?, email = ? WHERE id = ?");
        $stmt->execute([$name, $phone, $email, $id]);
    }
    
    echo json_encode(["success" => true, "message" => "Profile updated successfully"]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Server error: " . $e->getMessage()]);
}
?>
