#include <PZEM004Tv30.h>
#include <WiFi.h>
#include <FirebaseESP32.h>
#include <HTTPClient.h>
#include <DFRobot_mmWave_Radar.h>

#define WIFI_SSID "Redmi9"
#define WIFI_PASSWORD "arema1234"
#define FIREBASE_HOST "https://mokoli-561f5-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "AIzaSyAt3BsIiaPDqX5KcwQwjyQrC7R9CEuUNEg"
#define ENDPOINT_URL "http://8.215.46.96:3003/data"

#if defined(ESP32)
PZEM004Tv30 pzem(Serial2, 25, 26);
#else
PZEM004Tv30 pzem(Serial2);
#endif

const int RELAY_PIN = 18;
FirebaseData firebaseData;
HardwareSerial mySerial(1);
DFRobot_mmWave_Radar sensor(&mySerial);

unsigned long detectionTime = 0;
unsigned long relayOffDelay = 10 * 1000;
bool relayActive = false;
float previousTokenValue = 0.0; // Menyimpan nilai token sebelumnya
unsigned long lastEndpointSend = 0;
const unsigned long endpointInterval = 2 * 60 * 1000;

unsigned long presenceDetectedTime = 0;
unsigned long absenceDetectedTime = 0;
bool presenceDetected = false;
bool absenceDetected = false;

// Store last known values
float lastKnownEnergyValue = 0.0;
float lastKnownTokenValue = 0.0;

void setup() {
  Serial.begin(115200);
  mySerial.begin(115200, SERIAL_8N1, 21, 22);

  connectWiFi();
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");
  while (time(nullptr) < 1609459200) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println("\nTime set successfully");

  pinMode(RELAY_PIN, OUTPUT);

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  sensor.factoryReset();
  sensor.DetRangeCfg(0, 2); 
  sensor.OutputLatency(0, 0);
}

void loop() {
  // Baca nilai dari sensor tanpa memeriksa status WiFi
  static int previousVal = 0;
  int val = sensor.readPresenceDetection();

  Serial.print("Presence Detection Value: ");
  Serial.println(val);

  float voltage = pzem.voltage();
  float current = pzem.current();
  float power = pzem.power();
  float energy = pzem.energy();

  if (val != previousVal) {
    Serial.print("Value changed from ");
    Serial.print(previousVal);
    Serial.print(" to ");
    Serial.println(val);

    if (val == 1) {
      presenceDetectedTime = millis();
      presenceDetected = true;
      absenceDetected = false;
      Serial.println("Presence detected, starting timer...");
    } else if (val == 0) {
      absenceDetectedTime = millis();
      absenceDetected = true;
      presenceDetected = false;
      Serial.println("Absence detected, starting timer...");
    }
  }
  previousVal = val;

  controlRelay(val, energy);

  // Kirim data ke Firebase hanya jika WiFi terhubung
  if (WiFi.status() == WL_CONNECTED && millis() - lastEndpointSend >= endpointInterval) {
    readAndSendEnergyData(voltage, current, power, energy);
    lastEndpointSend = millis();
  }

  // Periksa status WiFi secara periodik dan coba sambungkan kembali jika terputus
  static unsigned long lastWiFiCheck = 0;
  const unsigned long wifiCheckInterval = 10 * 1000; // Interval untuk memeriksa koneksi WiFi

  if (millis() - lastWiFiCheck >= wifiCheckInterval) {
    lastWiFiCheck = millis();
    if (WiFi.status() != WL_CONNECTED) {
      Serial.println("WiFi connection lost. Reconnecting...");
      connectWiFi();
    }
  }
}

void readAndSendEnergyData(float voltage, float current, float power, float energy) {
  if (isnan(voltage) || isnan(current) || isnan(power) || isnan(energy)) {
    Serial.println("Error reading PZEM sensor data");
    return;
  }
  time_t timestamp = time(nullptr);
  char formattedTime[20];
  strftime(formattedTime, sizeof(formattedTime), "%Y-%m-%d %H:%M:%S", localtime(&timestamp));

  if (Firebase.setFloat(firebaseData, "/SensorData/Voltage", voltage) &&
      Firebase.setFloat(firebaseData, "/SensorData/Current", current) &&
      Firebase.setFloat(firebaseData, "/SensorData/Power", power) &&
      Firebase.setFloat(firebaseData, "/SensorData/Energy", energy) &&
      Firebase.setString(firebaseData, "/SensorData/Timestamp", formattedTime)) {
    Serial.println("Data sent to Firebase successfully!");
  } else {
    Serial.println("Failed to send data to Firebase");
    Serial.print("Firebase Error: ");
    Serial.println(firebaseData.errorReason());
  }

  String payload = "{\"energy\": " + String(energy) + ", \"timestamp\": \"" + formattedTime + "\"}";
  sendToEndpoint(payload);
}

void controlRelay(int val, float energyValue) {
  float tokenValue;
  if (WiFi.status() == WL_CONNECTED && Firebase.getFloat(firebaseData, "/SensorData/Token")) {
    tokenValue = firebaseData.floatData();
    lastKnownTokenValue = tokenValue;
  } else {
    tokenValue = lastKnownTokenValue;
    Serial.println("Using last known Token value");
  }
  Serial.print("Token Value: ");
  Serial.println(tokenValue);

  if (WiFi.status() == WL_CONNECTED && Firebase.getFloat(firebaseData, "/SensorData/Energy")) {
    energyValue = firebaseData.floatData();
    lastKnownEnergyValue = energyValue;
  } else {
    energyValue = lastKnownEnergyValue;
    Serial.println("Using last known Energy value");
  }
  Serial.print("Energy Value: ");
  Serial.println(energyValue);

  // Jika energyValue tiba-tiba sama dengan tokenValue, pertahankan nilai token sebelumnya
  if (energyValue == tokenValue) {
    tokenValue = previousTokenValue;
    Serial.println("Energy value equals token value, reverting to previous token value.");
  } else {
    previousTokenValue = tokenValue;
  }

  // Periksa apakah energyValue melebihi tokenValue untuk mematikan relay
  if (energyValue >= tokenValue) {
    digitalWrite(RELAY_PIN, LOW);
    Serial.println("Relay OFF (Energy exceeded)");
    relayActive = false;
    detectionTime = 0;
  } else {
    // Hanya nyalakan relay jika energyValue kurang dari tokenValue dan kehadiran terdeteksi
    if (presenceDetected && !relayActive && millis() - presenceDetectedTime >= 5000) {
      digitalWrite(RELAY_PIN, HIGH);
      Serial.println("Relay ON");
      relayActive = true;
    } else if (absenceDetected && relayActive && millis() - absenceDetectedTime >= 5000) {
      digitalWrite(RELAY_PIN, LOW);
      Serial.println("Relay OFF (No presence for 5 seconds)");
      relayActive = false;
      detectionTime = 0;
    }
  }
  
  sendRelayStatusToFirebase(relayActive, val);
}

void sendRelayStatusToFirebase(bool relayStatus, int presenceValue) {
  if (WiFi.status() == WL_CONNECTED &&
      Firebase.setBool(firebaseData, "SensorData/RelayStatus", relayStatus) &&
      Firebase.setInt(firebaseData, "SensorData/PresenceValue", presenceValue)) {
    Serial.println("Relay status and presence value sent to Firebase successfully!");
  } else {
    Serial.println("Failed to send relay status and presence value to Firebase");
    Serial.print("Firebase Error: ");
    Serial.println(firebaseData.errorReason());
  }
}

void sendToEndpoint(String payload) {
  WiFiClient client;
  HTTPClient http;

  if (WiFi.status() == WL_CONNECTED) {
    http.begin(client, ENDPOINT_URL);
    http.addHeader("Content-Type", "application/json");

    int httpResponseCode = http.POST(payload);

    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    Serial.println("Payload: " + payload);

    http.end();
  } else {
    Serial.println("Failed to send data to endpoint, no WiFi connection");
  }
}

void connectWiFi() {
  Serial.println("Connecting to WiFi...");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
}

void waitForConnection() {
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("WiFi connected");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
}
