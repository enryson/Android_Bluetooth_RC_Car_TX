#include <Metro.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(11, 10);

int s = 4;
int e = 6;

const int ledPin =  7;
boolean powerled = true;

Metro metro1 = Metro(50);
Metro metro2 = Metro(1000);

String servo = "n";
String esc = "r";
String bt = "m";

int battery;
int servoVal = 0,escVal = 0;

void setup() { 
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, HIGH);
  mySerial.begin(9600);
  Serial.begin(9600);
}
void loop() {
  if(metro1.check()){
    servoVal = analogRead(2);  
    escVal = analogRead(6);

    //servoVal = map(servoVal, 0, 1023, 20, 190);
    servoVal = map(servoVal,  400, 90, 60, 260)-22;
    escVal = map(escVal,    300, 1000, 190, 0);
    
    int ser = (servoVal);
   // if (ser > 105 && ser < 135 )    { ser = 120;  }
    mySerial.print(ser+servo);
    //Serial.println(ser+servo);
          
    int es = (escVal);
    if (es > 75 && es < 100 )    {  es = 90;  }    
    mySerial.print(es+esc);
    
    //Serial.println(es+esc);
  }   
  
  if(metro2.check()){
    //mySerial.print(bt);
    if (mySerial.available()>0){      
      String re = mySerial.readString();
      if (re = "l"){
        powerled = !powerled;
        digitalWrite(ledPin,powerled);
      }
    }
  }
  /*Serial.print("Servo: ");  Serial.println(ser);  Serial.print("ESC: ");
  Serial.println(es);*/  
}
