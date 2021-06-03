import socket
import RPi.GPIO as GPIO

from time import sleep
from gpiozero import Motor, OutputDevice

# SOCKET SETUP
serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
host = "192.168.43.132"
port = 6000
serverSocket.bind((host, port))
serverSocket.listen(5)

# PINS SETUP
ena = 25
enb = 10

GPIO.setmode(GPIO.BCM)
GPIO.setup(ena, GPIO.OUT)
GPIO.setup(enb, GPIO.OUT)

rightMotors = Motor(17, 22)
leftMotors = Motor(23, 24)

rightPWMEnable = GPIO.PWM(ena, 100)
leftPWMEnable = GPIO.PWM(enb, 100)
rightPWMEnable.start(50)
leftPWMEnable.start(50)

# MOTORS MOVEMENT
def moveForward():
    rightMotors.forward()
    leftMotors.forward()

def moveBackward():
    rightMotors.backward()
    leftMotors.backward()

def moveLeft():
    rightMotors.forward()
    leftMotors.stop()

def moveRight():
    rightMotors.stop()
    leftMotors.forward()

def stop():
    rightMotors.stop()
    leftMotors.stop()


# DATA PROCESSING
def processDrawingData(data):
    arrayData = data.split(',')
    for instruction in arrayData:
        [direction, value] = instruction.split("*")
        try:
            value = int(float(value))
        except:
            value = int(value)

        if 'goForward' in instruction:
            moveForward()
            sleep(value / 40.0) # dupa 1s = 40cm
        elif 'rotateCounterClockwise' in instruction:
            moveLeft()
            sleep(value / 60.0) # dupa 1s = 120deg
        elif 'rotateClockwise' in instruction:
            moveRight()
            sleep(value / 60.0)
        stop()
        sleep(0.5)

def processButtonsData(data):
    if 'STOP' in data:
        stop()
    elif 'START' in data:
        if 'forward' in data:
            moveForward()
        elif 'backwards' in data:
            moveBackward()
        elif 'left' in data:
            moveLeft()
        elif 'right' in data:
            moveRight()

def extractNumberFromString(data):
    for word in data.split():
        if word.isdigit():
            return word
    return 0

def processVoiceData(data):
    value = extractNumberFromString(data)
    sleepForwardValue = 1 if value == 0 else int(value) / 30.0
    sleepRotateValue = 1 if value == 0 else int(value) / 20.0

    print(sleepForwardValue, sleepRotateValue)

    if 'go' in data:
        if 'forward' in data or 'for' in data:
            moveForward()
            sleep(sleepForwardValue)
        elif 'backwards' in data or 'back' in data:
            moveBackward()
            slepe(sleepForwardValue)
    elif 'rotate' in data:
        if 'counterclockwise' in data:
            moveLeft()
            sleep(sleepRotateValue)
        elif 'clockwise' in data:
            moveRight()
            sleep(sleepRotateValue)
    elif 'spin' in data:
        moveLeft()
        sleep(3)
    elif 'dance' in data:
        moveForward()
        sleep(1)
        moveLeft()
        sleep(1)
        moveBackward()
        sleep(0.5)
        moveLeft()
        sleep(1.5)
        moveForward()
        sleep(1)
    stop()

def main():
    while True:
        clientSocket, address = serverSocket.accept()
        print("Got a connection from %s", str(address))

        while 1:
            data = clientSocket.recv(1024)
            print(data, data.split('[BUTTONS]'))
            if '[DRAWING]' in data:
                processDrawingData(data.split('[DRAWING]')[1])
            elif '[BUTTONS]' in data:
                processButtonsData(data)
            elif '[VOICE]' in data:
                processVoiceData(data.split('[VOICE]')[1])
            elif '[MAPS]' in data:
                processDrawingData(data.split('[MAPS]')[1])

            if(len(data) == 0):
                return;

if __name__ == "__main__":
    moveForward()
    sleep(1)
    stop()
    print ('Starting socket server...')
    main()
