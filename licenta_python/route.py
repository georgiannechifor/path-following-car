from time import sleep


class Route:

    def __init__(self, car):
        self.car = car

    def processRouteCommand(self, data):
        arrayData = data.split(',')
        for instruction in arrayData:
            [direction, value] = instruction.split("*")
            try:
                value = int(float(value))
            except:
                value = int(value)

            if 'goForward' in instruction:
                self.car.moveForward()
                sleep(value / 40.0) # dupa 1s = 40cm
            elif 'rotateCounterClockwise' in instruction:
                self.car.moveLeft()
                sleep(value / 60.0) # dupa 1s = 120deg
            elif 'rotateClockwise' in instruction:
                self.car.moveRight()
                sleep(value / 60.0)
            self.car.stop()
            sleep(0.5)
