<?php
require_once __DIR__ . '/mail_config.php';

class SmtpMailer {
    public static function sendMail($toEmail, $subject, $body) {
        $smtpHost = 'smtp.gmail.com';
        $smtpPort = 587;
        $smtpUser = defined('SMTP_EMAIL') ? SMTP_EMAIL : '';
        $smtpPass = defined('SMTP_APP_PASSWORD') ? SMTP_APP_PASSWORD : '';

        // If credentials are placeholder defaults, return false
        if (empty($smtpUser) || empty($smtpPass) || $smtpUser === 'YOUR_GMAIL_ADDRESS@gmail.com') {
            return false;
        }

        $socket = @fsockopen($smtpHost, $smtpPort, $errno, $errstr, 4);
        if (!$socket) {
            error_log("SMTP Connection failed: $errstr ($errno)");
            return false;
        }

        stream_set_timeout($socket, 4);

        $response = fgets($socket, 512);
        fputs($socket, "EHLO " . gethostname() . "\r\n");
        while ($line = fgets($socket, 512)) {
            if (substr($line, 3, 1) == ' ') break;
        }

        fputs($socket, "STARTTLS\r\n");
        $response = fgets($socket, 512);
        if (substr($response, 0, 3) != '220') {
            fclose($socket);
            return false;
        }

        $cryptoMethod = STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | STREAM_CRYPTO_METHOD_TLSv1_3_CLIENT;
        if (!@stream_socket_enable_crypto($socket, true, $cryptoMethod)) {
            fclose($socket);
            return false;
        }

        fputs($socket, "EHLO " . gethostname() . "\r\n");
        while ($line = fgets($socket, 512)) {
            if (substr($line, 3, 1) == ' ') break;
        }

        fputs($socket, "AUTH LOGIN\r\n");
        fgets($socket, 512);

        fputs($socket, base64_encode($smtpUser) . "\r\n");
        fgets($socket, 512);

        fputs($socket, base64_encode($smtpPass) . "\r\n");
        $authRes = fgets($socket, 512);
        if (substr($authRes, 0, 3) != '235') {
            error_log("SMTP Authentication failed: $authRes");
            fclose($socket);
            return false;
        }

        fputs($socket, "MAIL FROM: <$smtpUser>\r\n");
        fgets($socket, 512);

        fputs($socket, "RCPT TO: <$toEmail>\r\n");
        fgets($socket, 512);

        fputs($socket, "DATA\r\n");
        fgets($socket, 512);

        $headers  = "From: SmartQueue Pro <$smtpUser>\r\n";
        $headers .= "To: <$toEmail>\r\n";
        $headers .= "Subject: $subject\r\n";
        $headers .= "MIME-Version: 1.0\r\n";
        $headers .= "Content-Type: text/plain; charset=UTF-8\r\n\r\n";

        fputs($socket, $headers . $body . "\r\n.\r\n");
        $sendRes = fgets($socket, 512);

        fputs($socket, "QUIT\r\n");
        fclose($socket);

        return (substr($sendRes, 0, 3) == '250');
    }
}
?>
