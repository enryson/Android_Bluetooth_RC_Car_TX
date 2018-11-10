#include <PWMServo.h>
#include <Arduino.h>
#include <SoftwareSerial.h>

PWMServo myservo,esc;

SoftwareSerial mySerial(5,6); 

String inString = "";

void setup() {
  myservo.attach(9);
  esc.attach(10);
  mySerial.begin(38400);
  myservo.write(122);
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
        myservo.write(n);   
      }
      inString = "";
    }
    if (inChar == 'r') {
      int x = inString.toInt();
      if (x <= 170){
        esc.write(x);
      }
      inString = "";
    }
  }
}
