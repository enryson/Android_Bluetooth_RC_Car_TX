
#include <Servo.h>
//#include <SoftwareSerial.h>
#include <Arduino.h>

//SoftwareSerial mySerial(2, 3);
Servo myservo,esc;

//string
String inString = "";

unsigned long startTime;
unsigned long otherTime;

int prev = 150;
# define analog 2
float vPow = 1.99;
float r1 = 100000;
float r2 = 10000;

int voltcheck = 0;

void setup() {
  pinMode(13, OUTPUT);
  myservo.attach(6);//Carrinho
  esc.attach(5);
  digitalWrite(13, LOW);
  Serial.begin(9600);
  myservo.write(90);
  getv();
}

void loop() {
  
  if (Serial.available() > 0) {
    int inChar = Serial.read();
    
    if (isDigit(inChar)) {
      inString += (char)inChar; 
    }
    
    if (inChar == 'n'){
      int n = inString.toInt();
      if (n <= 180){
        //Serial.println(n);
        myservo.write(n);        
      }
      inString = "";
    }
    if (inChar == 'r') {
      int x = inString.toInt();
      if (x <= 180){
        //Serial.println(x);
        esc.write(x);      
      }
      inString = "";
    }    
    if (inChar == 'm') {
      
      if (voltcheck > 10){
      getv();
      voltcheck = 0;
      }
      else {
        voltcheck = voltcheck + 1;
      }
      digitalWrite(13, HIGH);
      startTime = millis();
    }
  }
}
static void getv() {
  float v = (analogRead(analog) * vPow) / 1023.0;
    float v2 = v / (r2 / (r1 + r2));
    Serial.println("{");
    Serial.println(v2);
    Serial.println("v");
}

