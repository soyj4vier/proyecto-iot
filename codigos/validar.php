<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "tu_clave"; // <--- CAMBIAR
$dbname = "movilesiot";$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8");

if ($conn->connect_error) { die(json_encode(["status" => "error", "mensaje" => "Error DB: " . $conn->connect_error])); }

if (isset($_GET['uid'])) {
    $uid_input = $_GET['uid'];
    $uid_input = $conn->real_escape_string($uid_input);
    $uid_limpio = strtoupper(str_replace(' ', '', $uid_input));

    // Guardar último UID leído para la función "Escanear" de la App
    file_put_contents("ultimo_uid.txt", $uid_limpio);

    $sql = "SELECT s.id_sensor, s.estado AS estado_sensor, u.nombre AS nombre_usuario, d.numero AS numero_depto 
            FROM SENSORES s
            LEFT JOIN USUARIOS u ON s.id_usuario_asociado = u.id_usuario
            LEFT JOIN DEPARTAMENTOS d ON s.id_departamento = d.id_departamento
            WHERE REPLACE(s.codigo_sensor, ' ', '') = '$uid_limpio' LIMIT 1";

    $result = $conn->query($sql);

    if ($result && $result->num_rows > 0) {
        $row = $result->fetch_assoc();
        if ($row['estado_sensor'] === 'ACTIVO') {
            echo json_encode(["status" => "valido", "nombre" => $row['nombre_usuario'], "depto"  => "Depto " . $row['numero_depto']]);
            registrarEvento($conn, $row['id_sensor'], 'ACCESO_VALIDO', 'PERMITIDO');
        } else {
            echo json_encode(["status" => "denegado", "mensaje" => "Tarjeta Bloqueada o Inactiva"]);
            registrarEvento($conn, $row['id_sensor'], 'ACCESO_RECHAZADO', 'DENEGADO');
        }
    } else {
        echo json_encode(["status" => "denegado", "mensaje" => "Tarjeta no registrada"]);
    }
} else {
    echo json_encode(["status" => "error", "mensaje" => "Falta UID"]);
}
$conn->close();

function registrarEvento($conn, $id_sensor, $tipo, $resultado) {
    $fecha = date("Y-m-d H:i:s");
    $sql_log = "INSERT INTO EVENTOS_ACCESO (id_sensor, id_usuario_ejecutor, tipo_evento, fecha_hora, resultado) VALUES ('$id_sensor', NULL, '$tipo', '$fecha', '$resultado')";
    $conn->query($sql_log);
}
?>