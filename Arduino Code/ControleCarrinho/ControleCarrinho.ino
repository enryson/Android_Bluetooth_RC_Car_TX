#include <SoftwareSerial.h>
SoftwareSerial mySerial(3, 4);
int analogPin = 6; 

String servo = "n";
String esc = "r";

int servoVal;
int escVal;

int val = 0;
int va = 0;

void setup() {  
  mySerial.begin(9600);
  Serial.begin(9600);
}
void loop() { 
  escVal = analogRead(6);  
  servoVal = analogRead(5); 
      
  servoVal = map(servoVal, 0, 1023, 60, 180); 
  escVal = map(escVal, 0, 1023, 180, 0); 
  
  int ser = (servoVal);
  mySerial.print(ser+servo);

  int es = (escVal);
  mySerial.print(es+esc);

  
  delay(10);
}
