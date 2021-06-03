import socket
from motors import Motors
from voice import Voice
from buttons import Buttons
from route import Route


class Main:
    def __init__(self):
        print('Program started')
        motors = Motors(17, 22, 23, 24, 25, 10)
        initSocket()


    def initSocket():
        host = "192.168.43.132"
        port = 6000
        serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        serverSocket.bind((host, port))
        serverSocket.listen(5)


    def run(self):
        route = Route(self.car)
        buttons = Buttons(self.motors)
        voice = Voice(self.motors)

        while True:
            client, address = serverSocket.accept()
            print('Got a connection from $s', str(address))

            while True:
                received = client.recv(1024)
                if '[DRAWING]' in data:
                    route.processRouteCommand(data.split('[DRAWING]')[1])
                elif '[BUTTONS]' in data:
                    buttons.processButtonsCommand(data.split('[BUTTONS]')[1])
                elif '[VOICE]' in data:
                    voice.processVoiceCommand(data.split('[VOICE]')[1])
                elif '[MAPS]' in data:
                    route.processRouteCommand(data.split('[MAPS]')[1])
