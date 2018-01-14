#include <SoftwareSerial.h>
SoftwareSerial mySerial(3, 4);
int analogPin = 6; 

String servo = "n";
String esc = "r";

int servoVal = 0;
int escVal = 0;

void setup() {  
  mySerial.begin(9600);
  Serial.begin(9600);
}
void loop() { 
  escVal = analogRead(6);
  servoVal = analogRead(4);
  
  servoVal = map(servoVal, 0, 1023, 90, 180);
  //escVal = map(escVal, 0, 1023, 0, 180);
  
  Serial.println(escVal);
  
  int ser = (servoVal);
  mySerial.print(ser+servo);

  
  int es = (escVal);  
  Serial.println(escVal);
  mySerial.print(es+esc);
  
  delay(10);
}
