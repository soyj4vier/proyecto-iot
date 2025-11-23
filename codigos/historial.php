<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "tu_clave"; // <--- CAMBIAR
$dbname = "movilesiot";

$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8");
if ($conn->connect_error) { die(json_encode(["status" => "error", "mensaje" => "Error DB"])); }

$id_usuario = isset($_GET['id_usuario']) ? intval($_GET['id_usuario']) : 0;

if ($id_usuario > 0) {
    $res = $conn->query("SELECT id_departamento FROM USUARIOS WHERE id_usuario = $id_usuario");
    if ($res && $res->num_rows > 0) {
        $row = $res->fetch_assoc();
        $id_departamento = $row['id_departamento'];

        $sql = "SELECT e.id_evento, e.tipo_evento, e.fecha_hora, e.resultado, COALESCE(s.codigo_sensor, 'Manual') as origen
                FROM EVENTOS_ACCESO e
                LEFT JOIN SENSORES s ON e.id_sensor = s.id_sensor
                LEFT JOIN USUARIOS u ON e.id_usuario_ejecutor = u.id_usuario
                WHERE (s.id_departamento = $id_departamento) OR (u.id_departamento = $id_departamento)
                ORDER BY e.fecha_hora DESC LIMIT 20";

        $result = $conn->query($sql);
        $eventos = [];
        while($r = $result->fetch_assoc()) { $eventos[] = $r; }
        echo json_encode(["status" => "success", "eventos" => $eventos]);
    } else {
        echo json_encode(["status" => "error", "mensaje" => "Usuario sin departamento"]);
    }
} else {
    echo json_encode(["status" => "error", "mensaje" => "Usuario invalido"]);
}
$conn->close();
?>