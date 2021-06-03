from gpiozero import Motor
import RPi.GPIO as GPIO


class Motors:
    def __init__(self, in1, in2, in3, in4, pwm1, pwm2):
        GPIO.setmode(GPIO.BCM)
        self.right = Motor(in1, in2)
        self.left = Motor(in3, in4)

        self.rightPwm = GPIO.PWM(pwm1, 100)
        self.leftPwm = GPIO.PWM(pwm2, 100)

        GPIO.setup(pwm1, GPIO.OUT)
        GPIO.setup(pwm2, GPIO.OUT)

        self.righPwm.start(50)
        self.leftPwm.start(50)

    def moveForward():
        self.right.forward()
        self.left.forward()

    def moveBackward():
        self.right.backward()
        self.left.backward()

    def moveLeft():
        self.right.forward()
        self.left.stop()

    def moveRight():
        self.right.stop()
        self.left.forward()

    def stop():
        self.right.stop()
        self.left.stop()
