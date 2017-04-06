#include <Servo.h>
#include <SoftwareSerial.h>
#include <Arduino.h>

SoftwareSerial mySerial(2, 3)
Servo servo,esc;

String inString = "";

unsigned long startTime;
unsigned long otherTime;

int prev = 150;

float vPow = 5;
float r1 = 47000;
float r2 = 10000;

int voltcheck = 0;

void setup() {
  pinMode(13, OUTPUT);
  servo.attach(6);//Carrinho
  esc.attach(5);
  mySerial.begin(9600);
  digitalWrite(13, LOW);
  Serial.begin(9600);
  servo.write(90);
}

void loop() {
  
  if (mySerial.available() > 0) {
    int inChar = mySerial.read();
    
    if (isDigit(inChar)) {
      inString += (char)inChar; 
    }
    
    if (inChar == 'n'){
      int n = inString.toInt();
      if (n <= 180){
        //Serial.println(n);
        servo.write(n);        
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
      //getv();
      voltcheck = 0;
      }
      else {
        voltcheck = voltcheck + 1;
      }
      digitalWrite(13, HIGH);
      //startTime = millis();
    }
  }
  if ((millis() - startTime) > 210) {
    digitalWrite(13, LOW);
    //delay(30);
  }
}
static void getv() {
  float v = (analogRead(5) * vPow) / 1023.0;
    float v2 = v / (r2 / (r1 + r2));
    mySerial.print(v
