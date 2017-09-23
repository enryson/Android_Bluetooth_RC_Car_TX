#include <SoftwareSerial.h>
int analogPin = 6; 
String servo = "n";
String esc = "r";


SoftwareSerial mySerial(3, 4);
int servoVal;
int escVal;

int val = 0;
int va = 0;
void setup() {
  mySerial.begin(9600);
  Serial.begin(9600);
}


void loop() {
  servoVal = analogRead(5);          
  servoVal = map(servoVal, 0, 1023, 64, 180); 
  //val = analogRead(5);

  escVal = analogRead(6);          
  escVal = map(escVal, 0, 1023, 180, 0); 
  
  int ser = (servoVal);
  mySerial.print(ser+servo);
  //Serial.println(ser);

  //va = analogRead(6);
  int es = (escVal);
  mySerial.print(es+esc);
  
  delay(10);

}

