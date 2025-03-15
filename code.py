import sys
import board
import time
import digitalio
import supervisor

# Define pins for stepper motors
motor1_step = digitalio.DigitalInOut(board.GP11)
motor1_step.direction = digitalio.Direction.OUTPUT
motor1_dir = digitalio.DigitalInOut(board.GP10)
motor1_dir.direction = digitalio.Direction.OUTPUT
motor2_step = digitalio.DigitalInOut(board.GP8)
motor2_step.direction = digitalio.Direction.OUTPUT
motor2_dir = digitalio.DigitalInOut(board.GP7)
motor2_dir.direction = digitalio.Direction.OUTPUT
enable = digitalio.DigitalInOut(board.GP9)
enable.direction = digitalio.Direction.OUTPUT

command_buffer = ""
last_serial_check = time.monotonic()
SERIAL_CHECK_INTERVAL = 0.1  # Check serial every 1ms

# Enable motors
enable.value = False
print("Motors enabled")

STEPS_PER_REVOLUTION = 200  # For a typical 1.8° stepper motor

# Motor state tracking
class MotorState:
    def __init__(self):
        self.running = False
        self.direction = True  # True for forward
        self.speed = 1.0
        self.last_step_time = 0

motor1 = MotorState()
motor2 = MotorState()

def calculate_step_delay(speed):
    return 1 / (STEPS_PER_REVOLUTION * speed)

def handle_command(command):
    parts = command.split()
    speed = 1  # Default speed
    if len(parts) > 1:
        try:
            speed = float(parts[1])
        except ValueError:
            print("Invalid speed value")
            return
    command = parts[0]

    if command == "forward1":
        motor1.running = True
        motor1.direction = True
        motor1.speed = speed
        motor1_dir.value = True
        print(f"Moving motor 1 forward at {speed} rotations per second...")

    elif command == "forward2":
        motor2.running = True
        motor2.direction = True
        motor2.speed = speed
        motor2_dir.value = True
        print(f"Moving motor 2 forward at {speed} rotations per second...")

    elif command == "reverse1":
        motor1.running = True
        motor1.direction = False
        motor1.speed = speed
        motor1_dir.value = False
        print(f"Moving motor 1 in reverse at {speed} rotations per second...")

    elif command == "reverse2":
        motor2.running = True
        motor2.direction = False
        motor2.speed = speed
        motor2_dir.value = False
        print(f"Moving motor 2 in reverse at {speed} rotations per second...")

    elif command == "stop1":
        motor1.running = False
        print("Motor 1 stopped")

    elif command == "stop2":
        motor2.running = False
        print("Motor 2 stopped")

    elif command == "stop":
        motor1.running = False
        motor2.running = False
        enable.value = True
        print("All motors stopped")

    else:
        print("Unknown command. Use 'forward1/2 [speed]', 'reverse1/2 [speed]' or 'stop1/2'")

def step_motor(motor_state, step_pin):
    if not motor_state.running:
        return

    current_time = time.monotonic()
    step_delay = calculate_step_delay(motor_state.speed)

    if (current_time - motor_state.last_step_time) >= step_delay:
        step_pin.value = not step_pin.value  # Toggle the pin
        motor_state.last_step_time = current_time

while True:
    current_time = time.monotonic()

    # Check serial input less frequently
    if current_time - last_serial_check >= SERIAL_CHECK_INTERVAL:
        if supervisor.runtime.serial_bytes_available:
            byte = sys.stdin.read(1)

            if byte in ('\x08', '\x7f'):
                if command_buffer:
                    command_buffer = command_buffer[:-1]
                    print('\x08 \x08', end='')
            elif byte == '\n' or byte == '\r':
                print()
                if command_buffer:
                    handle_command(command_buffer.strip().lower())
                    command_buffer = ""
            else:
                command_buffer += byte
                print(byte, end='')
        last_serial_check = current_time

    # Handle motor stepping
    step_motor(motor1, motor1_step)
    step_motor(motor2, motor2_step)
