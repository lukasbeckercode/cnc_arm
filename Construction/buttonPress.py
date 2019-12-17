#buttonPress 

import RPi.GPIO as GPIO 
from time import sleep 

GPIO.setmode(GPIO.BCM) 

startPin = 0; #TODO: edit to real pin number 
stopPin = 0; #TODO: edit to real pin number 


GPIO.setup(startPin,GPIO.IN,pull_up_down=GPIO.PUD_UP); 
GPIO.setup(stopPin,GPIO.IN,pull_up_down=GPIO.PUD_UP); 

try:
while True: 
	if GPIO.input(startPin):
	
		#run the jar 
		sleep(0.1); 
	elif GPIO.input(stopPin):
		#kill the jar 
		sleep(0.1); 
	elif GPIO.input(startPin) and GPIO.input(stopPin):
	
		#kill the pi with sudo halt 
	
finally: 
	GPIO.cleanup()