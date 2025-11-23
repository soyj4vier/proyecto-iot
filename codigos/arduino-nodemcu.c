#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClient.h>
#include <SPI.h>
#include <MFRC522.h>
#include <Servo.h>

/* ==========================================
   CONFIGURACIÓN DE USUARIO
   ========================================== */
const char* ssid = "Javier";             // <--- TU WIFI
const char* password = "12345 12345";         // <--- TU CLAVE WIFI
String baseUrl = "http://34.202.178.9/"; // <--- TU SERVIDOR AWS

/* ==========================================
   CONEXIONES (PINOUT)
   ========================================== */
#define RST_PIN  D3   // Reset del RC522
#define SS_PIN   D4   // SDA (SS) del RC522
#define SERVO_PIN D1  // Cable de señal del Servo
#define LED_PIN   D2  // LED indicador (Verde/Rojo)

/* ==========================================
   OBJETOS Y VARIABLES
   ========================================== */
MFRC522 rfid(SS_PIN, RST_PIN);
Servo miServo;

// Variables para el control de tiempo (Polling)
unsigned long ultimoChequeo = 0;
const long intervaloChequeo = 2000; // Revisar la App cada 2 segundos (2000 ms)

void setup() {
  // 1. Iniciamos comunicación Serial
  Serial.begin(115200);
  Serial.println("\n\n--- INICIANDO SISTEMA CONTROL DE ACCESO ---");

  // 2. Iniciamos Hardware
  miServo.attach(SERVO_PIN);
  miServo.write(0); // Asegurar puerta cerrada al inicio
  
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW); // Apagado

  // 3. Iniciamos RFID
  SPI.begin();
  rfid.PCD_Init();

  // Diagnóstico rápido del lector RFID
  if (rfid.PCD_ReadRegister(rfid.VersionReg) == 0x00) {
    Serial.println("ERROR CRÍTICO: El lector RFID no responde.");
  } else {
    Serial.println("Lector RFID detectado correctamente.");
  }

  // 4. Conexión WiFi
  WiFi.begin(ssid, password);
  Serial.print("Conectando a WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConectado a WiFi.");
  Serial.print("IP Asignada: ");
  Serial.println(WiFi.localIP());

  Serial.println("-----------------------------------");
  Serial.println("LISTO: Esperando tarjetas o comando remoto...");
  Serial.println("-----------------------------------");
}

void loop() {
  
  // ======================================================
  // 1. POLLING: REVISAR ORDEN DESDE LA APP (Cada 2 seg)
  // ======================================================
  unsigned long tiempoActual = millis();
  
  if (tiempoActual - ultimoChequeo >= intervaloChequeo) {
    ultimoChequeo = tiempoActual;
    consultarEstadoRemoto();
  }

  // ======================================================
  // 2. LECTURA RFID: REVISAR SI HAY TARJETA
  // ======================================================
  // Si no hay tarjeta nueva, terminamos el loop aquí y volvemos a empezar
  if (!rfid.PICC_IsNewCardPresent()) return;
  if (!rfid.PICC_ReadCardSerial()) return;

  // Procesar UID de la tarjeta
  String uidLimpio = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    if(rfid.uid.uidByte[i] < 0x10) uidLimpio += "0";
    uidLimpio += String(rfid.uid.uidByte[i], HEX);
  }
  uidLimpio.toUpperCase();

  Serial.print("Tarjeta detectada: ");
  Serial.println(uidLimpio);
  
  // Validar en el servidor
  validarTarjeta(uidLimpio);

  // Detener lectura para no leer la misma tarjeta repetidamente
  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();
}

// ---------------------------------------------------------
// FUNCIÓN: Validar Tarjeta RFID contra validar.php
// ---------------------------------------------------------
void validarTarjeta(String uid) {
  if (WiFi.status() == WL_CONNECTED) {
    WiFiClient client;
    HTTPClient http;
    
    // Construir URL: http://IP/validar.php?uid=CODIGO
    String url = baseUrl + "validar.php?uid=" + uid;
    
    http.begin(client, url);
    int httpCode = http.GET();

    if (httpCode > 0) {
      String payload = http.getString();
      // Buscamos "valido" en la respuesta JSON
      if (payload.indexOf("valido") > 0) {
        Serial.println(">> RFID AUTORIZADO");
        abrirBarrera();
      } else {
        Serial.println(">> RFID DENEGADO");
        parpadearError();
      }
    } else {
      Serial.print("Error HTTP: ");
      Serial.println(httpCode);
    }
    http.end();
  } else {
    Serial.println("Error: WiFi desconectado");
  }
}

// ---------------------------------------------------------
// FUNCIÓN: Consultar archivo estado_barrera.txt (Polling)
// ---------------------------------------------------------
void consultarEstadoRemoto() {
  if (WiFi.status() == WL_CONNECTED) {
    WiFiClient client;
    HTTPClient http;
    
    // Leemos el archivo generado por control_barrera.php
    String url = baseUrl + "estado_barrera.txt";
    
    http.begin(client, url);
    int httpCode = http.GET();

    if (httpCode == 200) {
      String estado = http.getString();
      estado.trim(); // Limpiar espacios en blanco o saltos de línea
      
      // Si el archivo contiene "1", significa ABRIR
      if (estado == "1") {
        Serial.println(">> ORDEN REMOTA RECIBIDA: ABRIR");
        abrirBarrera();
        
        // OPCIONAL: Podrías enviar una petición aquí para resetear el archivo a "0"
        // Para evitar que se abra repetidamente en cada ciclo si el servidor no lo cambia.
        // resetearEstadoRemoto(); 
      }
    }
    http.end();
  }
}

// ---------------------------------------------------------
// ACCIONES FÍSICAS
// ---------------------------------------------------------
void abrirBarrera() {
  Serial.println("   [ACCION] Abriendo barrera...");
  
  digitalWrite(LED_PIN, HIGH); // Encender LED (Verde si es bicolor, o el que tengas)
  miServo.write(180);          // Mover Servo a 90 o 180 grados
  
  delay(5000);                 // Mantener abierta 5 segundos
  
  miServo.write(0);            // Cerrar Servo
  digitalWrite(LED_PIN, LOW);  // Apagar LED
  
  Serial.println("   [ACCION] Barrera cerrada.");
}

void parpadearError() {
  Serial.println("   [ACCION] Acceso Denegado.");
  // Parpadeo rápido para indicar error
  for(int i=0; i<3; i++){
    digitalWrite(LED_PIN, HIGH); delay(200);
    digitalWrite(LED_PIN, LOW); delay(200);
  }
}