class Buttons:
    def __init__(self, car):
        self.car = car

    def processButtonsCommand(self, data):
        if 'STOP' in data:
            self.car.stop()
        elif 'START' in data:
            if 'forward' in data:
                self.car.moveForward()
            elif 'backwards' in data:
                self.car.moveBackward()
            elif 'left' in data:
                self.car.moveLeft()
            elif 'right' in data:
                self.car.moveRight()
