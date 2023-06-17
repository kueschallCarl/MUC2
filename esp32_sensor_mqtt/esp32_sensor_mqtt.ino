#include <Adafruit_MPU6050.h>  // Library for MPU6050 accelerometer and gyroscope
#include <Adafruit_Sensor.h>   // Library for generic sensor functions
#include <Wire.h>              // Library for I2C communication
#include <WiFi.h>              // Library for WiFi connectivity
#include <PubSubClient.h>      // Library for MQTT communication

WiFiClient espClient;          // Create a WiFi client object
PubSubClient mqttClient(espClient);  // Create a MQTT client object using the WiFi client
Adafruit_MPU6050 mpu;          // Create an MPU6050 object for accelerometer and gyroscope

// Define pins for LEDs
int led_red = 26;
int led_green = 14;
int led_yellow = 27;
int brightness = 0;      // How bright the LED is
int fadeAmount = 5;      // How many points to fade the LED by
bool pub_flag = false;   // Flag to control publishing

#define IC2_SDA 33   // Define I2C SDA pin
#define I2C_SCL 32   // Define I2C SCL pin

// Define WiFi credentials and MQTT broker details
const char* ssid = "MaraudersMap";
const char* password = "Page394%";
const char* mqttBroker = "192.168.0.89";
const int mqttPort = 1883;

// Define MQTT topics
const char* mpuTopic = "mpu/K05";
const char* tempTopic = "temp/K05";
const char* finishedTopic = "finished/K05";

// Timer variables for periodic tasks
hw_timer_t* timer = NULL;
portMUX_TYPE timerMux = portMUX_INITIALIZER_UNLOCKED;
volatile unsigned long previousMillis = 0;
const unsigned long interval = 1000; // Interval in milliseconds

// Timer ISR (Interrupt Service Routine)
void IRAM_ATTR onTimer() {
  portENTER_CRITICAL_ISR(&timerMux);
  previousMillis += interval;
  portEXIT_CRITICAL_ISR(&timerMux);
}

// Function to blink LEDs
void blink() {
  digitalWrite(led_green, HIGH);
  delay(400);
  digitalWrite(led_green, LOW);
}

// Callback function for MQTT message received
void onMqttMessageReceived(char* topic, byte* payload, unsigned int length) {
  char message[length + 1];
  memcpy(message, payload, length);
  message[length] = '\0';

  // Compare received topic and message to perform specific actions
  if (strcmp(topic, "finished/K05") == 0) {
    if (strcmp(message, "0") == 0) {
      pub_flag = true;
    }
    else if (strcmp(message, "1") == 0) {
      pub_flag = false;
      blink();
    }
    else {
      return;
    }
    Serial.println("pub_flag inside Func: ");
    Serial.print(pub_flag);
  }

  // Display the received message in the console
  Serial.print("Received message on topic: ");
  Serial.print(topic);
  Serial.print(", payload: ");
  Serial.println(message);
}

// Function to connect to WiFi
void connectToWifi() {
  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wi-Fi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
  }
  Serial.println("connected!");

  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

// Function to setup MQTT connection
void setupMqtt() {
  mqttClient.setServer(mqttBroker, mqttPort);
  mqttClient.setCallback(onMqttMessageReceived);

  Serial.print("Connecting to MQTT broker...");
  while (!mqttClient.connected()) {
    if (mqttClient.connect("ESP32Client")) {
      Serial.println("connected!");
      mqttClient.subscribe(finishedTopic);
    } else {
      Serial.print("failed, retrying in 5 seconds...");
      delay(5000);
    }
  }
}

void setup() {
  pinMode(led_green, OUTPUT);
  pinMode(led_red, OUTPUT);
  pinMode(led_yellow, OUTPUT);

  Wire.begin(33, 32);  // Initialize I2C communication

  Serial.begin(115200);
  while (!Serial)
    delay(10);

  Serial.println("Adafruit MPU6050 test!");

  // Check if MPU6050 chip is found
  if (!mpu.begin()) {
    Serial.println("Failed to find MPU6050 chip");
    while (1) {
      delay(10);
    }
  }
  Serial.println("MPU6050 Found!");

  // Set accelerometer and gyroscope ranges
  mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
  Serial.print("Accelerometer range set to: +-8G");

  mpu.setGyroRange(MPU6050_RANGE_500_DEG);
  Serial.print("Gyro range set to: +- 1000 deg/s");

  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
  Serial.print("Filter bandwidth set to: 44hz");
  Serial.println("");
  delay(3000);

  connectToWifi();
  setupMqtt();

  // Set up the hardware timer for periodic tasks
  timer = timerBegin(0, 80, true);              // Timer 0, prescaler 80 (1MHz tick), count up
  timerAttachInterrupt(timer, &onTimer, true);  // Attach the timer ISR
  timerAlarmWrite(timer, interval * 1000, true); // Set the alarm to trigger every interval (in microseconds)
  timerAlarmEnable(timer);                       // Enable the alarm
}

void loop() {
  Serial.println(pub_flag);

  static unsigned long previousTempMillis = 0;
  static char tempValue[8]; // Buffer to store temperature value

  portENTER_CRITICAL(&timerMux);
  unsigned long currentMillis = previousMillis;
  portEXIT_CRITICAL(&timerMux);

  // Read sensor data from MPU6050
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  // Store sensor values in a string
  String sensorValues = "(" +
                        String(a.acceleration.x) + "," +
                        String(a.acceleration.y) + "," +
                        String(a.acceleration.z) + "," +
                        String(g.gyro.x) + "," +
                        String(g.gyro.y) + "," +
                        String(g.gyro.z) + ")";

  Serial.print("Sensor Values Tuple: ");
  Serial.println(sensorValues);

  // Display accelerometer values in the console
  Serial.print("Acceleration X: ");
  Serial.print(a.acceleration.x);
  Serial.print(", Y: ");
  Serial.print(a.acceleration.y);
  Serial.print(", Z: ");
  Serial.print(a.acceleration.z);
  Serial.println(" m/s^2");

  // Display gyroscope values in the console
  Serial.print("Rotation X: ");
  Serial.print(g.gyro.x);
  Serial.print(", Y: ");
  Serial.print(g.gyro.y);
  Serial.print(", Z: ");
  Serial.print(g.gyro.z);
  Serial.println(" rad/s");

  if (pub_flag) {
    // Publish the list of sensor values
    mqttClient.publish(mpuTopic, sensorValues.c_str());

    // Publish temperature value at regular intervals
    if (currentMillis - previousTempMillis >= interval) {
      previousTempMillis = currentMillis;
      snprintf(tempValue, sizeof(tempValue), "%f", temp.temperature);
      mqttClient.publish(tempTopic, tempValue);
    }
  }

  // Display temperature value in the console
  Serial.print("Temperature: ");
  Serial.print(temp.temperature);
  Serial.println(" degC");

  Serial.println("");
  delay(100);

  mqttClient.loop();
}
