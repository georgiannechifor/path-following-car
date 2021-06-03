from time import sleep


class Voice:
    def __init__(self, motors):
        self.car = motors

    def getDigitFromString(data):
        for word in data:
            if word.isdigit():
                return int(word)
        return 30

    def processVoiceCommand(self, data):
        firstWord = data.split()[0]
        sleepValue = getDigitFromString(data);

        if 'go' == firstWord or 'move' == firstWord:
            if any(item in ['forward', 'for', 'forwards', 'up'] for item in data):
                self.car.moveForward()
                sleep(sleepValue / 30.0)
            elif any(item in ['backward', 'backwards', 'back', 'down'] for item in data):
                self.car.moveBackward()
                sleep(sleepValue / 30.0)
        elif 'rotate' == firstWord:
            if 'counterclockwise' in data:
                self.car.moveLeft()
                sleep(sleepValue / 20.0)
            elif 'clockwise' in data:
                self.car.moveRight()
                sleep(sleepValue / 20.0)
        elif 'spin' == firstWord:
            self.car.moveLeft()
            sleep(3)
            self.car.moveRight()
            sleep(3)
        elif 'dance' == firstWord:
            self.car.moveForward()
            sleep(2)
            self.car.moveLeft()
            sleep(1.5)
            self.car.moveBackward()
            sleep(1)
            self.car.moveLeft()
            sleep(2)
            self.car.moveForward()
            sleep(3)
        self.car.stop()
